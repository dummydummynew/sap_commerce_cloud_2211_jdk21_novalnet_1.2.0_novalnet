package com.novalnet.process.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dao.NovalnetReplenishmentDao;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetB2BPaymentMethodService;
import de.hybris.platform.b2bacceleratoraddon.actions.replenishment.AuthorizePaymentAction;
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.AddressService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Collections;
import java.util.Set;

/**
 * Charges the token explicitly recorded on the schedule. This action deliberately has no
 * SessionService dependency because replenishment runs in a background cron-job session.
 */
public class NovalnetTokenizedPaymentAction extends AuthorizePaymentAction {

    private static final Set<String> TOKENIZED_PAYMENT_TYPES = Set.of("novalnetCreditCard", "novalnetDirectDebitSepa", "novalnetGuaranteedDirectDebitSepa", "novalnetDirectDebitAch");
    private static final String TRANSACTION = "transaction";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    private ModelService modelService;
    @Resource
    private NovalnetB2BPaymentMethodService novalnetB2BPaymentMethodService;
    @Resource
    private AddressService addressService;
    @Resource
    private PaymentModeService paymentModeService;
    @Resource
    private NovalnetReplenishmentDao novalnetReplenishmentDao;
    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Override
    public Transition executeAction(final ReplenishmentProcessModel process) throws Exception {
        final BusinessProcessParameterModel cartParameter = getProcessParameterHelper()
                .getProcessParameterByName(process, "cart");
        final CartModel cart = (CartModel) cartParameter.getValue();
        modelService.refresh(cart);

        final String replenishmentOrderId = process.getCartToOrderCronJob().getCode();

        final NovalnetReplPayRefInfoModel paymentReference = novalnetReplenishmentDao.findReplenishmentPaymentReference(replenishmentOrderId);
        if (paymentReference == null) {
            return Transition.NOK;
        }

        if (paymentReference.getCustomerNo() == null
                || paymentReference.getCustomerNo().longValue() != cart.getUser().getPk().getLongValue()
                || paymentReference.getToken() == null || paymentReference.getToken().isBlank()
                || !TOKENIZED_PAYMENT_TYPES.contains(paymentReference.getPaymentType())) {
            return Transition.NOK;
        }

        final JsonNode response = executePayment(cart, paymentReference);

        if (!isSuccessful(response)) {
            return Transition.NOK;
        }

        storeSuccessfulPayment(cart, paymentReference, response);
        return Transition.OK;
    }

    protected JsonNode executePayment(final CartModel cart,
                                      final NovalnetReplPayRefInfoModel paymentReference) throws Exception {
        final BaseStoreModel baseStore = cart.getStore();
        if (baseStore == null || baseStore.getNovalnetAPIKey() == null || baseStore.getNovalnetPaymentAccessKey() == null) {
            throw new IllegalStateException("Novalnet credentials are not configured for the scheduled cart base store");
        }

        final String request = novalnetB2BPaymentMethodService.createPaymentRequest(cart, paymentReference);

        final HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(novalnetEndpointConfigService.getPaymentUrl()))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("X-NN-Access-Key", Base64.getEncoder().encodeToString(baseStore.getNovalnetPaymentAccessKey().getBytes(StandardCharsets.UTF_8)))
                .POST(HttpRequest.BodyPublishers.ofString(request)).build();

        final HttpResponse<String> response = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(60)).build()
                .send(httpRequest, HttpResponse.BodyHandlers.ofString());

        return objectMapper.readTree(response.body());
    }

    protected boolean isSuccessful(final JsonNode response) {
        final String statusCode = response.path("result").path("status_code").asText();
        final String status = response.path(TRANSACTION).path("status").asText();
        return "100".equals(statusCode) && ("CONFIRMED".equals(status) || "ON_HOLD".equals(status) || "PENDING".equals(status));
    }

    protected void storeSuccessfulPayment(final CartModel cart,
                                          final NovalnetReplPayRefInfoModel paymentReference,
                                          final JsonNode response) {
        final String transactionId = response.path(TRANSACTION).path("tid").asText();
        final String transactionStatus = response.path(TRANSACTION).path("status").asText();
        final String paymentType = paymentReference.getPaymentType();
        final String orderHistoryNotes = buildOrderHistoryNotes(paymentType, response);
        final AddressModel sourceBillingAddress = cart.getPaymentAddress();
        final int two = 2;
        if (sourceBillingAddress == null) {
            throw new IllegalStateException("Billing address is missing for replenishment cart " + cart.getCode());
        }
        final AddressModel billingAddress = addressService.cloneAddressForOwner(sourceBillingAddress, cart);
        billingAddress.setBillingAddress(Boolean.TRUE);

        final NovalnetPaymentInfoModel paymentInfo = modelService.create(NovalnetPaymentInfoModel.class);
        paymentInfo.setUser(cart.getUser());
        paymentInfo.setBillingAddress(billingAddress);
        paymentInfo.setPaymentEmailAddress(resolvePaymentEmailAddress(cart, response));
        paymentInfo.setPaymentProvider(paymentType);
        paymentInfo.setPaymentGatewayStatus(transactionStatus);
        paymentInfo.setPaymentInfo(orderHistoryNotes);
        paymentInfo.setOrderHistoryNotes(orderHistoryNotes);
        paymentInfo.setCode(cart.getCode());
        paymentInfo.setSaved(Boolean.TRUE);
        paymentInfo.setDuplicate(Boolean.FALSE);

        final PaymentTransactionEntryModel entry = modelService.create(PaymentTransactionEntryModel.class);
        entry.setRequestId(transactionId);
        entry.setCode(cart.getCode());
        entry.setType(PaymentTransactionType.AUTHORIZATION);
        entry.setTransactionStatus(TransactionStatus.ACCEPTED.name());
        entry.setAmount(BigDecimal.valueOf(response.path(TRANSACTION).path("amount").asLong()).movePointLeft(two));
        entry.setCurrency(cart.getCurrency());

        final PaymentTransactionModel transaction = modelService.create(PaymentTransactionModel.class);
        transaction.setRequestId(transactionId);
        transaction.setPaymentProvider(paymentType);
        transaction.setInfo(paymentInfo);
        transaction.setOrder(cart);
        transaction.setEntries(Collections.singletonList(entry));

        final PaymentModeModel paymentMode = paymentModeService.getPaymentModeForCode(paymentType);
        cart.setPaymentMode(paymentMode);
        cart.setPaymentAddress(billingAddress);
        cart.setPaymentInfo(paymentInfo);
        cart.setPaymentTransactions(Collections.singletonList(transaction));
        modelService.saveAll(billingAddress, paymentInfo, entry, transaction, cart);
    }

    protected String buildOrderHistoryNotes(final String paymentType, final JsonNode response) {
        final JsonNode transaction = response.path(TRANSACTION);
        final PaymentModeModel paymentMode = paymentModeService.getPaymentModeForCode(paymentType);
        final String paymentName = paymentMode != null && paymentMode.getName() != null
                && !paymentMode.getName().isBlank() ? paymentMode.getName() : paymentType;

        final StringBuilder notes = new StringBuilder()
                .append(Localization.getLocalizedString("novalnet.paymentname"))
                .append(": ").append(paymentName).append("<br>")
                .append(Localization.getLocalizedString("novalnet.transactionID"))
                .append(" ").append(transaction.path("tid").asText()).append("<br>");
        if ("1".equals(transaction.path("test_mode").asText())) {
            notes.append(Localization.getLocalizedString("novalnet.testOrderText"));
        }
        return notes.toString();
    }

    protected String resolvePaymentEmailAddress(final CartModel cart, final JsonNode response) {
        final String responseEmail = response.path("customer").path("email").asText(null);
        if (responseEmail != null && !responseEmail.isBlank()) {
            return responseEmail;
        }
        if (cart.getPaymentAddress() != null && cart.getPaymentAddress().getEmail() != null
                && !cart.getPaymentAddress().getEmail().isBlank()) {
            return cart.getPaymentAddress().getEmail();
        }
        final String userId = cart.getUser().getUid();
        final int guestSeparator = userId == null ? -1 : userId.indexOf('|');
        return guestSeparator >= 0 ? userId.substring(guestSeparator + 1) : userId;
    }

}
