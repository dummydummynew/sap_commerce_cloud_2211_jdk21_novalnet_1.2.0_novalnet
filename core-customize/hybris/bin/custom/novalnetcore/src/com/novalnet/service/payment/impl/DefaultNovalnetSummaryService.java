/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.constants.GeneratedNovalnetcoreConstants.Enumerations.OnholdActionTypeWithZeroAmount;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.model.*;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.order.NovalnetOrderService;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.service.payment.NovalnetSummaryService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.order.CommerceCartService;
import de.hybris.platform.commerceservices.service.data.CommerceCartParameter;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * The type Default novalnet summary service.
 */
@Service
public class DefaultNovalnetSummaryService implements NovalnetSummaryService {


    private static final String PAYMENT_URL = "paymentUrl";
    private static final String TRANSACTION = "transaction";
    private static final String AMOUNT = "amount";
    private static final String STATUS = "status";
    private static final String PAYMENT_REQUEST = "paymentRequest";
    private static final String NOVALNET_CREDIT_CARD = "novalnetCreditCard";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA = "novalnetDirectDebitSepa";
    private static final String NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA = "novalnetGuaranteedDirectDebitSepa";
    private static final String NOVALNET_INVOICE = "novalnetInvoice";
    private static final String NOVALNET_GUARANTEED_INVOICE = "novalnetGuaranteedInvoice";
    private static final String NOVALNET_PREPAYMENT = "novalnetPrepayment";
    private static final String NOVALNET_PAY_PAL = "novalnetPayPal";
    private static final String NOVALNET_ONLINE_BANK_TRANSFER = "novalnetOnlineBankTransfer";
    private static final String NOVALNET_BANCONTACT = "novalnetBancontact";
    private static final String NOVALNET_MULTIBANCO = "novalnetMultibanco";
    private static final String NOVALNET_IDEAL = "novalnetIdeal";
    private static final String NOVALNET_MB_WAY = "novalnetMbWay";
    private static final String NOVALNET_WE_CHAT_PAY = "novalnetWeChatPay";
    private static final String NOVALNET_BLIK = "novalnetBlik";
    private static final String NOVALNET_ALI_PAY = "novalnetAlipay";
    private static final String NOVALNET_TRUSTLY = "novalnetTrustly";
    private static final String NOVALNET_GOOGLE_PAY = "novalnetGooglePay";
    private static final String NOVALNET_APPLE_PAY = "novalnetApplePay";
    private static final String NOVALNET_TWINT = "novalnetTwint";
    private static final String NOVALNET_EPS = "novalnetEps";
    private static final String NOVALNET_PRZELEWY_24 = "novalnetPrzelewy24";
    private static final String NOVALNET_POST_FINANCE_CARD = "novalnetPostFinanceCard";
    private static final String NOVALNET_POST_FINANCE = "novalnetPostFinance";
    private static final String NOVALNET_DIRECT_DEBIT_ACH = "novalnetDirectDebitAch";
    private static final String TEST_MODE = "test_mode";
    private static final String ACCOUNT_HOLDER = "account_holder";
    private static final String BANK_DETAILS = "bankDetails";
    private static final String ORDER_DETAILS = "orderDetails";
    private static final String EMAIL = "email";
    private static final String STATUS_TEXT = "status_text";
    private static final String PAYMENT_DATA = "payment_data";
    private static final String TOKEN = "token";
    private static final String CUSTOMER = "customer";
    private static final String CARD_EXPIRY_MONTH = "card_expiry_month";
    private static final String PENDING = "PENDING";
    private static final String ON_HOLD = "ON_HOLD";
    private static final Integer TWO = 2;

    private static final Map<String, Class<? extends PaymentModeModel>> PAYMENT_MODE_TYPES = Map.ofEntries(
            Map.entry(NOVALNET_CREDIT_CARD, NovalnetCreditCardPaymentModeModel.class),
            Map.entry(NOVALNET_DIRECT_DEBIT_SEPA, NovalnetDirectDebitSepaPaymentModeModel.class),
            Map.entry(NOVALNET_GUARANTEED_INVOICE, NovalnetGuaranteedInvoicePaymentModeModel.class),
            Map.entry(NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA, NovalnetGuaranteedDirectDebitSepaPaymentModeModel.class),
            Map.entry(NOVALNET_PAY_PAL, NovalnetPayPalPaymentModeModel.class),
            Map.entry(NOVALNET_INVOICE, NovalnetInvoicePaymentModeModel.class),
            Map.entry(NOVALNET_PREPAYMENT, NovalnetPrepaymentPaymentModeModel.class),
            Map.entry(NOVALNET_ONLINE_BANK_TRANSFER, NovalnetOnlineBankTransferPaymentModeModel.class),
            Map.entry(NOVALNET_MULTIBANCO, NovalnetMultibancoPaymentModeModel.class),
            Map.entry(NOVALNET_BANCONTACT, NovalnetBancontactPaymentModeModel.class),
            Map.entry(NOVALNET_POST_FINANCE_CARD, NovalnetPostFinanceCardPaymentModeModel.class),
            Map.entry(NOVALNET_POST_FINANCE, NovalnetPostFinancePaymentModeModel.class),
            Map.entry(NOVALNET_IDEAL, NovalnetIdealPaymentModeModel.class),
            Map.entry(NOVALNET_TWINT, NovalnetTwintPaymentModeModel.class),
            Map.entry(NOVALNET_GOOGLE_PAY, NovalnetGooglePayPaymentModeModel.class),
            Map.entry(NOVALNET_APPLE_PAY, NovalnetApplePayPaymentModeModel.class),
            Map.entry(NOVALNET_EPS, NovalnetEpsPaymentModeModel.class),
            Map.entry(NOVALNET_PRZELEWY_24, NovalnetPrzelewy24PaymentModeModel.class),
            Map.entry(NOVALNET_WE_CHAT_PAY, NovalnetWeChatPayPaymentModeModel.class),
            Map.entry(NOVALNET_DIRECT_DEBIT_ACH, NovalnetDirectDebitAchPaymentModeModel.class),
            Map.entry(NOVALNET_MB_WAY, NovalnetMbWayPaymentModeModel.class),
            Map.entry(NOVALNET_TRUSTLY, NovalnetTrustlyPaymentModeModel.class),
            Map.entry(NOVALNET_BLIK, NovalnetBlikPaymentModeModel.class),
            Map.entry(NOVALNET_ALI_PAY, NovalnetAliPayPaymentModeModel.class));

    @Resource
    private SessionService sessionService;
    @Resource
    private PaymentModeService paymentModeService;
    @Resource
    private CartService cartService;
    @Resource
    private CommerceCartService commerceCartService;
    @Resource
    private ModelService modelService;
    @Resource
    private Converter<AddressData, AddressModel> addressReverseConverter;
    @Resource
    @Qualifier("addressConverter")
    private Converter<AddressModel, AddressData> addressConverter;
    @Resource
    private CommonI18NService commonI18NService;
    @Resource
    private NovalnetDao novalnetDao;
    @Resource
    private NovalnetApiService novalnetApiService;
    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;
    @Resource
    private NovalnetOrderService novalnetOrderService;
    @Resource
    private NovalnetPaymentService novalnetPaymentService;
    @Resource
    private NovalnetCheckoutService novalnetCheckoutService;
    @Resource
    private NovalnetPaymentRequestBuilder novalnetPaymentRequestBuilder;

    @Override
    public String sendRequest() {
        String requestParam = sessionService.getAttribute(PAYMENT_REQUEST);
        String url = sessionService.getAttribute(PAYMENT_URL) == null ? novalnetEndpointConfigService.getPaymentUrl()
                : sessionService.getAttribute(PAYMENT_URL);
        return novalnetApiService.sendRequest(url, requestParam);
    }

    /**
     *
     * @param response       the response
     * @param currentPayment the current payment
     * @param cartData       the cart data
     * @return Get the summary details in the checkout
     */
    public String formSummaryCommands(JsonNode response, String currentPayment, CartData cartData) {
        JsonNode resultJsonObject = response.path("result");
        JsonNode transactionJsonObject = response.path(TRANSACTION);
        String[] successStatus = {"CONFIRMED", ON_HOLD, PENDING};
        Integer orderAmountCent = transactionJsonObject.findValue(AMOUNT).asInt();
        StringBuilder orderComments = new StringBuilder();
        StringBuilder bankDetails = new StringBuilder();

        if (Arrays.asList(successStatus).contains(transactionJsonObject.findValue(STATUS).asText())) {
            String paymentName = novalnetPaymentService.getPaymentName(currentPayment);
            String testMode = "";

            if ("1".equals(transactionJsonObject.findValue(TEST_MODE).toString())) {
                testMode = Localization.getLocalizedString("novalnet.testOrderText");
            }

            orderComments.append(Localization.getLocalizedString("novalnet.paymentname")).append(": ").append(paymentName).append("<br>");
            orderComments.append(Localization.getLocalizedString("novalnet.transactionId")).append(" ").append(transactionJsonObject.findValue("tid")).append("<br>");
            orderComments.append(testMode);
            String totalPrice = cartData.getTotalPriceWithTax().getFormattedValue();
            bankDetails = bankDetailsCommends(currentPayment, bankDetails, orderAmountCent, transactionJsonObject, totalPrice);
            final String orderCommentsValue = orderComments.toString();
            final String bankDetailsValue = bankDetails.toString();

            sessionService.setAttribute(BANK_DETAILS, bankDetailsValue);
            sessionService.setAttribute(ORDER_DETAILS, orderCommentsValue);
            sessionService.setAttribute("tid", orderCommentsValue + bankDetailsValue);
            sessionService.setAttribute(EMAIL, response.findValue(EMAIL));
        } else {
            final JsonNode statusTextNode = resultJsonObject.findValue(STATUS_TEXT);
            final String statusMessage = statusTextNode.asText() != null ? statusTextNode.asText()
                    : resultJsonObject.findValue("status_desc").asText();
            sessionService.setAttribute("novalnetCheckoutError", statusMessage);
        }

        if (transactionJsonObject.has(PAYMENT_DATA) && !currentPayment.equals(NOVALNET_GOOGLE_PAY)) {
            final JsonNode paymentDataJsonObject = transactionJsonObject.path(PAYMENT_DATA);
            if (paymentDataJsonObject.has(TOKEN) && !isGuestUser()) {
                handleReferenceTransactionInfo(response, currentPayment);
            } else {
                final JsonNode statusTextNode = resultJsonObject.findValue(STATUS_TEXT);
                final String statusMessage = statusTextNode.toString() != null ? statusTextNode.toString()
                        : resultJsonObject.findValue("status_desc").toString();
                sessionService.setAttribute("novalnetCheckoutError", statusMessage);
            }
        }
        return orderComments.toString();
    }

    private StringBuilder bankDetailsCommends(String currentPayment, StringBuilder bankDetails, Integer orderAmountCent, JsonNode transactionJsonObject, String totalPrice) {
        if ((NOVALNET_INVOICE.equals(currentPayment) || NOVALNET_PREPAYMENT.equals(currentPayment) || NOVALNET_GUARANTEED_INVOICE.equals(currentPayment))) {
            bankDetails.append("<br>");
            JsonNode bankDetailsJsonObject = transactionJsonObject.path("bank_details");
            bankDetails.append("<br>").append(String.format(Localization.getLocalizedString("novalnet.bankDetailsComments1"), totalPrice));
            if (transactionJsonObject.has("due_date") && !ON_HOLD.equals(transactionJsonObject.findValue(STATUS).asText())) {
                LocalDate localDate = LocalDate.parse(transactionJsonObject.findValue("due_date").asText());
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                String formattedDate = localDate.format(formatter);
                bankDetails.append(" ").append(String.format(Localization.getLocalizedString("novalnet.bankDetailsComments2"), formattedDate));
            }
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.bankDetailsAccountHolder")).append(" ").append(bankDetailsJsonObject.findValue(ACCOUNT_HOLDER).asText());
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.bankDetailsIban")).append(" ").append(bankDetailsJsonObject.findValue("iban").asText());
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.bankDetailsBic")).append(" ").append(bankDetailsJsonObject.findValue("bic").asText());
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.bankDetailsBank")).append(" ").append(bankDetailsJsonObject.findValue("bank_name").asText()).append("<br>");
            bankDetails.append(Localization.getLocalizedString("novalnet.bankPlace")).append(" ").append(bankDetailsJsonObject.findValue("bank_place").asText());
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.bankDetailspaymentRefernceMulti")).append("<br>")
                    .append(Localization.getLocalizedString("novalnet.bankDetailsPaymentReference")).append(" : ").append(transactionJsonObject.findValue("tid").asText()).append("<br>");
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.bankDetailsQrCommend")).append("<br>");
            bankDetails.append("<br>").append("<img src=").append(bankDetailsJsonObject.findValue("qr_image")).append(" alt=Payment QR>").append("<br>");
            if (NOVALNET_GUARANTEED_INVOICE.equals(currentPayment) && "75".equals(transactionJsonObject.findValue("status_code").asText())) {
                bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.status75"));
            }
        } else if (NOVALNET_MULTIBANCO.equals(currentPayment) && transactionJsonObject.has("partner_payment_reference")) {
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.multibancocomments1")).append(" ")
                    .append(totalPrice)
                    .append(Localization.getLocalizedString("novalnet.multibancocomments2")).append("<br>")
                    .append(Localization.getLocalizedString("novalnet.bankDetailsPaymentReference")).append(" : ")
                    .append(transactionJsonObject.findValue("partner_payment_reference").asText()).append("<br>")
                    .append(Localization.getLocalizedString("novalnet.multibancosupplierid")).append(" : ")
                    .append(transactionJsonObject.findValue("service_supplier_id").asText());
        } else if (NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA.equals(currentPayment) && "75".equals(transactionJsonObject.findValue("status_code").toString())) {
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.zeroAmountBooking"));
        }

        if (orderAmountCent == 0) {
            bankDetails.append("<br>").append(Localization.getLocalizedString("novalnet.zeroAmountBooking"));
        }
        return bankDetails;
    }

    @Override
    public void prepareCartForPlaceOrder(final String commands, final String currentPayment, final JsonNode response,
                                         final CartData cartData) {
        final CartModel cartModel = cartService.getSessionCart();
        final JsonNode transaction = response.path(TRANSACTION);
        final String transactionId = transaction.path("tid").asText();
        final String transactionStatus = transaction.path(STATUS).asText();
        final JsonNode customer = response.path(CUSTOMER);
        if (StringUtils.isBlank(transactionId)) {
            throw new IllegalStateException("Novalnet response does not contain a transaction ID");
        }

        final PaymentModeModel paymentMode = getSupportedPaymentMode(currentPayment);
        final AddressModel billingAddress = getBillingAddress(response, cartData, cartModel.getCode());

        billingAddress.setOwner(cartModel);
        final NovalnetPaymentInfoModel paymentInfoModel = modelService.create(NovalnetPaymentInfoModel.class);
        paymentInfoModel.setBillingAddress(billingAddress);
        paymentInfoModel.setPaymentProvider(currentPayment);
        paymentInfoModel.setPaymentEmailAddress(textOrNull(customer, EMAIL));
        paymentInfoModel.setDuplicate(Boolean.FALSE);
        paymentInfoModel.setSaved(Boolean.FALSE);
        paymentInfoModel.setUser(cartModel.getUser());
        paymentInfoModel.setPaymentInfo(commands);
        paymentInfoModel.setOrderHistoryNotes(stripBreaks(sessionService.getAttribute(BANK_DETAILS)));
        paymentInfoModel.setPaymentGatewayStatus(transactionStatus);
        paymentInfoModel.setCode(cartModel.getCode());

        final PaymentTransactionModel paymentTransaction = modelService.create(PaymentTransactionModel.class);
        paymentTransaction.setRequestId(transactionId);
        paymentTransaction.setPaymentProvider(currentPayment);
        paymentTransaction.setOrder(cartModel);
        paymentTransaction.setInfo(paymentInfoModel);

        final PaymentTransactionEntryModel authorizationEntry = novalnetPaymentService.createTransactionEntry(transactionId, cartModel,
                transaction.path(AMOUNT).asInt(), stripBreaks(sessionService.getAttribute(ORDER_DETAILS)),
                transaction.path("currency").asText());
        authorizationEntry.setPaymentTransaction(paymentTransaction);

        cartModel.setPaymentAddress(billingAddress);
        cartModel.setPaymentInfo(paymentInfoModel);
        cartModel.setPaymentMode(paymentMode);
        cartModel.setPaymentTransactions(List.of(paymentTransaction));
        modelService.saveAll(billingAddress, paymentInfoModel, paymentTransaction, authorizationEntry, cartModel);


        final CommerceCartParameter calculationParameter = new CommerceCartParameter();
        calculationParameter.setEnableHooks(true);
        calculationParameter.setCart(cartModel);
        commerceCartService.calculateCart(calculationParameter);

        if (!Boolean.TRUE.equals(cartModel.getCalculated())) {
            throw new IllegalStateException("Cart calculation failed after preparing Novalnet payment information");
        }
    }

    @Override
    public void prepareCartForReplenishment(final String currentPayment) {
        final CartModel cartModel = cartService.getSessionCart();
        if (cartModel.getDeliveryAddress() == null) {
            throw new IllegalStateException("Delivery address is missing for replenishment cart " + cartModel.getCode());
        }

        final Object paymentRequest = sessionService.getAttribute(PAYMENT_REQUEST);
        if (paymentRequest == null) {
            throw new IllegalStateException("Novalnet payment request is missing for replenishment cart " + cartModel.getCode());
        }

        final JsonNode paymentRequestNode;
        try {
            paymentRequestNode = paymentRequest instanceof JsonNode ? (JsonNode) paymentRequest : new ObjectMapper().readTree(String.valueOf(paymentRequest));
        } catch (final Exception e) {
            throw new IllegalStateException("Unable to read the Novalnet payment request for replenishment cart "
                    + cartModel.getCode(), e);
        }

        final AddressModel billingAddress = getBillingAddress(paymentRequestNode, null, cartModel.getCode());
        final JsonNode customer = paymentRequestNode.path(CUSTOMER);
        final NovalnetPaymentInfoModel paymentInfoModel = modelService.create(NovalnetPaymentInfoModel.class);
        paymentInfoModel.setBillingAddress(billingAddress);
        paymentInfoModel.setPaymentProvider(currentPayment);
        paymentInfoModel.setPaymentEmailAddress(textOrNull(customer, EMAIL));
        paymentInfoModel.setDuplicate(Boolean.FALSE);
        paymentInfoModel.setSaved(Boolean.FALSE);
        paymentInfoModel.setUser(cartModel.getUser());
        paymentInfoModel.setOrderHistoryNotes(stripBreaks(sessionService.getAttribute(BANK_DETAILS)));
        paymentInfoModel.setCode(cartModel.getCode());
        //need to remove in future case
        paymentInfoModel.setPaymentGatewayStatus("CREATED");
        paymentInfoModel.setPaymentInfo("");

        billingAddress.setOwner(cartModel);
        billingAddress.setBillingAddress(Boolean.TRUE);
        cartModel.setPaymentAddress(billingAddress);
        cartModel.setPaymentMode(getSupportedPaymentMode(currentPayment));

        modelService.saveAll(billingAddress, paymentInfoModel, cartModel);


        final CommerceCartParameter calculationParameter = new CommerceCartParameter();
        calculationParameter.setEnableHooks(true);
        calculationParameter.setCart(cartModel);
        commerceCartService.calculateCart(calculationParameter);


        if (!Boolean.TRUE.equals(cartModel.getCalculated())) {
            throw new IllegalStateException("Cart calculation failed after preparing Novalnet payment information");
        }
    }

    @Override
    public void saveOrderData(final String commands, final String currentPayment, final JsonNode response, final CartData cartData,
                              final String orderCode) {
        final String transactionId = response.path(TRANSACTION).path("tid").asText();
        final String transactionStatus = response.path(TRANSACTION).path(STATUS).asText();
        final String orderDetails = stripBreaks(sessionService.getAttribute(ORDER_DETAILS));

        final List<OrderModel> orders = getOrderInfoModel(orderCode);
        if (CollectionUtils.isEmpty(orders)) {
            throw new IllegalStateException("Order not found for code " + orderCode);
        }
        final OrderModel orderModel = modelService.get(orders.get(0).getPk());

        if (!(orderModel.getPaymentInfo() instanceof NovalnetPaymentInfoModel)) {
            throw new IllegalStateException("Novalnet payment information was not copied to order " + orderCode);
        }

        final NovalnetPaymentInfoModel paymentInfoModel = (NovalnetPaymentInfoModel) orderModel.getPaymentInfo();
        final PaymentTransactionModel paymentTransaction = orderModel.getPaymentTransactions().stream()
                .filter(transaction -> transactionId.equals(transaction.getRequestId())).findFirst().orElseThrow(
                        () -> new IllegalStateException("Novalnet authorization transaction was not copied to order " + orderCode));

        final AddressModel billingAddress = orderModel.getPaymentAddress();
        if (billingAddress == null) {
            throw new IllegalStateException("Billing address was not copied to order " + orderCode);
        }
        sessionService.setAttribute("novalnetAddressData", addressConverter.convert(billingAddress));
        paymentInfoModel.setBillingAddress(billingAddress);
        paymentInfoModel.setCode(orderModel.getCode());
        paymentInfoModel.setDuplicate(Boolean.FALSE);
        paymentTransaction.setPaymentProvider(currentPayment);
        paymentTransaction.setInfo(paymentInfoModel);

        orderModel.setPaymentMode(getSupportedPaymentMode(currentPayment));
        orderModel.setStatusInfo(orderDetails);
        novalnetOrderService.updateOrderStatus(orderModel.getCode(), paymentInfoModel);

        final OrderHistoryEntryModel orderEntry = modelService.create(OrderHistoryEntryModel.class);
        orderEntry.setTimestamp(new Date());
        orderEntry.setOrder(orderModel);
        orderEntry.setDescription(orderDetails);
        modelService.saveAll(paymentInfoModel, paymentTransaction, billingAddress, orderModel, orderEntry);

        final List<String> bankPayments = Arrays.asList(NOVALNET_INVOICE, NOVALNET_PREPAYMENT,
                NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA, NOVALNET_GUARANTEED_INVOICE);
        final int orderPaidAmount = bankPayments.contains(currentPayment) || PENDING.equals(transactionStatus) ? 0
                : response.path(TRANSACTION).path(AMOUNT).asInt();
        final long callbackTid = Long.parseLong(transactionId);

        final NovalnetCallbackInfoModel callbackInfo = modelService.create(NovalnetCallbackInfoModel.class);
        callbackInfo.setPaymentType(currentPayment);
        callbackInfo.setOrderAmount(response.path(TRANSACTION).path(AMOUNT).asInt());
        callbackInfo.setCallbackTid(callbackTid);
        callbackInfo.setOrginalTid(callbackTid);
        callbackInfo.setPaidAmount(orderPaidAmount);
        callbackInfo.setOrderNo(orderModel.getCode());
        modelService.save(callbackInfo);
    }

    private PaymentModeModel getSupportedPaymentMode(final String currentPayment) {
        final PaymentModeModel paymentMode = paymentModeService.getPaymentModeForCode(currentPayment);
        final Class<? extends PaymentModeModel> expectedType = PAYMENT_MODE_TYPES.get(currentPayment);
        if (paymentMode == null || expectedType == null || !expectedType.isInstance(paymentMode)) {
            throw new IllegalArgumentException("Unsupported payment mode: " + currentPayment);
        }
        return paymentMode;
    }

    private String stripBreaks(final String value) {
        return StringUtils.defaultString(value).replace("<br>", " ").replace("<br/>", " ");
    }

    private AddressModel getBillingAddress(final JsonNode response, final CartData cartData, final String orderCode) {
        final AddressData checkoutBillingAddress = cartData != null && cartData.getPaymentInfo() != null
                ? cartData.getPaymentInfo().getBillingAddress()
                : null;

        AddressModel billingAddress = checkoutBillingAddress != null ? addressReverseConverter.convert(checkoutBillingAddress)
                : modelService.create(AddressModel.class);

        final JsonNode customer = response.path(CUSTOMER);
        final JsonNode billing = customer.path("billing");

        if (StringUtils.isBlank(billingAddress.getFirstname())) {
            billingAddress.setFirstname(textOrNull(customer, "first_name"));
        }
        if (StringUtils.isBlank(billingAddress.getLastname())) {
            billingAddress.setLastname(textOrNull(customer, "last_name"));
        }
        if (StringUtils.isBlank(billingAddress.getLine1())) {
            billingAddress.setLine1(textOrNull(billing, "street"));
        }
        if (StringUtils.isBlank(billingAddress.getLine2())) {
            billingAddress.setLine2(textOrNull(billing, "house_no"));
        }
        if (StringUtils.isBlank(billingAddress.getTown())) {
            billingAddress.setTown(textOrNull(billing, "city"));
        }
        if (StringUtils.isBlank(billingAddress.getPostalcode())) {
            billingAddress.setPostalcode(textOrNull(billing, "zip"));
        }
        if (StringUtils.isBlank(billingAddress.getEmail())) {
            billingAddress.setEmail(textOrNull(customer, EMAIL));
        }

        final String countryIso = textOrNull(billing, "country_code");
        if (billingAddress.getCountry() == null && StringUtils.isNotBlank(countryIso)) {
            billingAddress.setCountry(commonI18NService.getCountry(countryIso));
        }

        if (StringUtils.isBlank(billingAddress.getLine1()) && cartData != null && cartData.getDeliveryAddress() != null) {
            billingAddress = addressReverseConverter.convert(cartData.getDeliveryAddress());
        }

        if (StringUtils.isBlank(billingAddress.getLine1())) {
            throw new IllegalStateException("Complete billing address is missing for order " + orderCode);
        }

        return billingAddress;
    }

    private String textOrNull(final JsonNode parent, final String fieldName) {
        final JsonNode value = parent.path(fieldName);
        return value.isMissingNode() || value.isNull() || StringUtils.isBlank(value.asText()) ? null : value.asText();
    }

    public void handleReferenceTransactionInfo(JsonNode response, String currentPayment) {

        JsonNode transactionDetails = response.path(TRANSACTION);
        JsonNode paymentData = transactionDetails.findPath(PAYMENT_DATA);
        NovalnetPaymentRefInfoModel novalnetPaymentRefInfo = modelService.create(NovalnetPaymentRefInfoModel.class);
        long customerID = Long.parseLong(response.findValue("customer_no").asText());
        long transactionID = transactionDetails.findValue("tid").asLong();

        if ("0".equals(transactionDetails.findValue(AMOUNT).asText())) {
            return;
        }
        currentPayment = NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA.equals(currentPayment) ? NOVALNET_DIRECT_DEBIT_SEPA : currentPayment;

        novalnetPaymentRefInfo.setCustomerNo(customerID);
        novalnetPaymentRefInfo.setPaymentType(currentPayment);
        novalnetPaymentRefInfo.setReferenceTransaction(false);
        novalnetPaymentRefInfo.setOrginalTid(transactionID);
        novalnetPaymentRefInfo.setToken(paymentData.findValue(TOKEN).asText());

        switch (currentPayment) {
            case NOVALNET_CREDIT_CARD:
                String expiryDate = (paymentData.findValue(CARD_EXPIRY_MONTH).asText().length() == 1)
                        ? ("0" + paymentData.findValue(CARD_EXPIRY_MONTH).asText())
                        : paymentData.findValue(CARD_EXPIRY_MONTH).asText();
                novalnetPaymentRefInfo.setCardType(paymentData.findValue("card_brand").asText());
                novalnetPaymentRefInfo.setCardHolder(paymentData.findValue("card_holder").asText());
                novalnetPaymentRefInfo.setMaskedCardNumber(paymentData.findValue("card_number").asText());
                novalnetPaymentRefInfo.setExpiryDate(expiryDate + " / " + paymentData.findValue("card_expiry_year").asText()
                        .substring(paymentData.findValue("card_expiry_year").asText().length() - TWO));
                break;
            case NOVALNET_DIRECT_DEBIT_SEPA:
                novalnetPaymentRefInfo.setMaskedAccountIban(paymentData.findValue("iban").asText());
                novalnetPaymentRefInfo.setAccountHolder(paymentData.findValue(ACCOUNT_HOLDER).asText());
                break;
            case NOVALNET_DIRECT_DEBIT_ACH:
                novalnetPaymentRefInfo.setAccountHolder(paymentData.findValue(ACCOUNT_HOLDER).asText());
                novalnetPaymentRefInfo.setMaskedAchAccountNumber(paymentData.findValue("account_number").asText());
                novalnetPaymentRefInfo.setMaskedAchRoutingNumber(paymentData.findValue("routing_number").asText());
                break;
            case NOVALNET_PAY_PAL:
                if (paymentData.has("paypal_transaction_id")) {
                    novalnetPaymentRefInfo.setPaypalTransactionID(paymentData.findValue("paypal_transaction_id").asText());
                }
                if (paymentData.has("paypal_account")) {
                    novalnetPaymentRefInfo.setPaypalEmailID(paymentData.findValue("paypal_account").asText());
                }
                break;
        }
        modelService.save(novalnetPaymentRefInfo);

    }

    @Override
    public void googlePayRequest(HttpServletRequest request) {
        String paymentRequest = sessionService.getAttribute(PAYMENT_REQUEST);
        JSONObject castAsJson = new JSONObject(paymentRequest);

        JSONObject transaction = castAsJson.getJSONObject(TRANSACTION);
        JSONObject paymentData = new JSONObject();
        paymentData.put("wallet_token", request.getParameter(TOKEN));
        transaction.put(PAYMENT_DATA, paymentData);
        NovalnetGooglePayPaymentModeModel paymentModeModel = (NovalnetGooglePayPaymentModeModel) paymentModeService.getPaymentModeForCode(NOVALNET_GOOGLE_PAY);
        if (paymentModeModel != null && paymentModeModel.getOnholdActionTypeWithZeroAmount() != null) {
            if (Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode())) {
                transaction.put(TEST_MODE, "1");
            } else {
                transaction.put(TEST_MODE, "0");
            }
            if (OnholdActionTypeWithZeroAmount.AUTHORIZE.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
            } else if (OnholdActionTypeWithZeroAmount.AUTHORIZE_WITH_ZERO_AMOUNT.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
                transaction.put(AMOUNT, "0");
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
        sessionService.setAttribute(PAYMENT_REQUEST, castAsJson.toString());
    }

    @Override
    public String paymentRequestOrUpdateOrder(CartData cartData, String currentPayment, String languageCode) {

        if (currentPayment.equals(NOVALNET_GOOGLE_PAY)) {
            NovalnetPaymentRequest novalnetPaymentRequest = novalnetPaymentRequestBuilder.createTransactionDetailsRequest(sessionService.getAttribute("wallet_tid"), languageCode);
            String response = novalnetApiService.sendRequest(novalnetEndpointConfigService.getTransactionDetailsUrl(), NovalnetUtils.convetToString(novalnetPaymentRequest));
            sessionService.removeAttribute("wallet_tid");
            return response;
        } else {
            return sendRequest();
        }
    }

    @Override
    public void updateTransaction(String tid, String orderCode, String languageCode) {
        NovalnetPaymentRequest novalnetPaymentRequest = novalnetPaymentRequestBuilder.createTransactionUpdate(tid, orderCode, languageCode);
        novalnetApiService.sendRequest(novalnetEndpointConfigService.getTransactionUpdateUrl(), NovalnetUtils.convetToString(novalnetPaymentRequest));
    }

    @Override
    public String getTransactionDetails(String tid, String languageCode) {
        NovalnetPaymentRequest novalnetPaymentRequest = novalnetPaymentRequestBuilder.createTransactionDetailsRequest(tid, languageCode);
        return novalnetApiService.sendRequest(novalnetEndpointConfigService.getTransactionDetailsUrl(), NovalnetUtils.convetToString(novalnetPaymentRequest));
    }

    public Boolean isGuestUser() {
        return novalnetCheckoutService.isGuestUser();
    }

    /**
     * Gets order info model.
     *
     * @param orderCode the order code
     * @return the order info model
     */
    public List<OrderModel> getOrderInfoModel(String orderCode) {
        return novalnetDao.getOrderInfoModel(orderCode);
    }

    /**
     * After place order.
     *
     * @param cartModel  the cart model
     * @param orderModel the order model
     */
    protected void afterPlaceOrder(final CartModel cartModel, final OrderModel orderModel) {
        if (orderModel != null && !modelService.isNew(orderModel)) {
            cartService.removeSessionCart();
            modelService.refresh(orderModel);
        }
    }
}
