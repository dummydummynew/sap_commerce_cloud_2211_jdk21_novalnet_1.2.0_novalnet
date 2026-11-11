package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.AddressForm;
import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Custom;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.Merchant;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.PaymentData;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.dto.payment.Transaction;
import com.novalnet.enums.OnholdActionType;
import com.novalnet.enums.OnholdActionTypeWithZeroAmount;
import com.novalnet.enums.OnholdActionTypeWithoutAuthorize;
import com.novalnet.exception.NovalnetException;
import com.novalnet.model.*;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetB2BPaymentMethodService;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.UserService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.util.Config;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * The type Default novalnet b 2 b payment method service.
 */
@Service
public class DefaultNovalnetB2BPaymentMethodService implements NovalnetB2BPaymentMethodService {

    public static final String REDIRECT_PREFIX = "redirect:";
    protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/novalnet/orderConfirmation/";

    private static final String SELECTED_PAYMENT_REFERENCE_PK_FOR_REPLENISHMENT = "novalnetSelectedPaymentReferencePkForReplenishment";
    private static final String CREDIT_CARD_REFERENCE_PK = "novalnetCreditCardOneClickReferencePk";
    private static final String DIRECT_DEBIT_SEPA_REFERENCE_PK = "novalnetDirectDebitSepaOneClickReferencePk";
    private static final String DIRECT_DEBIT_ACH_REFERENCE_PK = "novalnetDirectDebitAchOneClickReferencePk";

    private static final String CART_DATA_ATTR = "cartData";
    private static final String PAYMENT_URL = "paymentUrl";
    private static final String NOVALNET_SEPA_ONE_CLICK = "novalnetDirectDebitSepaOneClickToken";

    private static final String NOVALNET_PAYMENT_METHOD = "multi/novalnet-payment-method/payments";
    private static final String NOVALNET_WALLET_TRANSACTION = "summary/novalnet/bookWalletTransaction";
    private static final String NOVALNET_HOP_RESPONSE = "multi/novalnet/hop-response";

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
    private static final Integer STATUS_VALUE = 100;
    @Resource
    private I18NFacade i18NFacade;

    @Resource
    private SessionService sessionService;

    @Resource
    private PaymentModeService paymentModeService;

    @Resource
    private NovalnetCheckoutService novalnetCheckoutService;

    @Resource
    private BaseStoreService baseStoreService;

    @Resource
    private UserService userService;

    @Resource
    private CustomerFacade customerFacade;

    @Resource
    private NovalnetDao novalnetDao;

    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Resource
    private NovalnetPaymentRequestBuilder novalnetPaymentRequestBuilder;

    @Override
    public NovalnetPaymentDetailsForm addPaymentProcess(Model model, CartData cartData, NovalnetPaymentDetailsForm paymentDetailsForm) {

        AddressForm addressForm = getAddressForm(cartData, model);
        paymentDetailsForm.setBillingAddress(addressForm);
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());

        final BaseStoreModel baseStoreModel = baseStoreService.getCurrentBaseStore();
        model.addAttribute("novalnetBaseStoreConfiguration", baseStoreModel);

        final Locale language = JaloSession.getCurrentSession().getSessionContext().getLocale();
        String paymentTypeCode = sessionService.getAttribute("selectedPaymentMethodId");

        if (paymentTypeCode == null || paymentTypeCode.isEmpty()) {
            paymentTypeCode = cartData.getPaymentType().getCode();
        } else {
            final B2BPaymentTypeData paymentTypeData = new B2BPaymentTypeData();
            paymentTypeData.setCode(paymentTypeCode);
            cartData.setPaymentType(paymentTypeData);
        }

        switch (paymentTypeCode) {
            case "CARD":
                NovalnetCreditCardPaymentModeModel novalnetCreditCardPaymentModeModel = (NovalnetCreditCardPaymentModeModel) findPaymentModeModel(NOVALNET_CREDIT_CARD);
                model.addAttribute(NOVALNET_CREDIT_CARD, novalnetCreditCardPaymentModeModel);
                if (novalnetCreditCardPaymentModeModel.getActive() && novalnetCreditCardPaymentModeModel.getNovalnetOneClickShopping()) {
                    showOneClickShopping(model, novalnetCreditCardPaymentModeModel.getCode(), paymentDetailsForm);
                }
                break;
            case "NOVALNET_WALLET":
                model.addAttribute(NOVALNET_APPLE_PAY, findPaymentModeModel(NOVALNET_APPLE_PAY));
                model.addAttribute(NOVALNET_GOOGLE_PAY, findPaymentModeModel(NOVALNET_GOOGLE_PAY));
                model.addAttribute(NOVALNET_PAY_PAL, findPaymentModeModel(NOVALNET_PAY_PAL));
                model.addAttribute(NOVALNET_TWINT, findPaymentModeModel(NOVALNET_TWINT));
                model.addAttribute(NOVALNET_MB_WAY, findPaymentModeModel(NOVALNET_MB_WAY));
                model.addAttribute(NOVALNET_WE_CHAT_PAY, findPaymentModeModel(NOVALNET_WE_CHAT_PAY));
                model.addAttribute(NOVALNET_ALI_PAY, findPaymentModeModel(NOVALNET_ALI_PAY));
                break;
            case "NOVALNET_ONLINE_TRANSFER":
                model.addAttribute(NOVALNET_IDEAL, findPaymentModeModel(NOVALNET_IDEAL));
                model.addAttribute(NOVALNET_BLIK, findPaymentModeModel(NOVALNET_BLIK));
                model.addAttribute(NOVALNET_TRUSTLY, findPaymentModeModel(NOVALNET_TRUSTLY));
                model.addAttribute(NOVALNET_PRZELEWY_24, findPaymentModeModel(NOVALNET_PRZELEWY_24));
                model.addAttribute(NOVALNET_EPS, findPaymentModeModel(NOVALNET_EPS));
                model.addAttribute(NOVALNET_ONLINE_BANK_TRANSFER, findPaymentModeModel(NOVALNET_ONLINE_BANK_TRANSFER));
                model.addAttribute(NOVALNET_BANCONTACT, findPaymentModeModel(NOVALNET_BANCONTACT));
                model.addAttribute(NOVALNET_POST_FINANCE, findPaymentModeModel(NOVALNET_POST_FINANCE));
                model.addAttribute(NOVALNET_POST_FINANCE_CARD, findPaymentModeModel(NOVALNET_POST_FINANCE_CARD));
                break;
            case "NOVALNET_INVOICE":
                model.addAttribute(NOVALNET_INVOICE, findPaymentModeModel(NOVALNET_INVOICE));
                model.addAttribute(NOVALNET_GUARANTEED_INVOICE, findPaymentModeModel(NOVALNET_GUARANTEED_INVOICE));
                model.addAttribute(NOVALNET_PREPAYMENT, findPaymentModeModel(NOVALNET_PREPAYMENT));
                model.addAttribute(NOVALNET_MULTIBANCO, findPaymentModeModel(NOVALNET_MULTIBANCO));
                break;
            case "NOVALNET_DIRECT_DEBIT":
                NovalnetDirectDebitSepaPaymentModeModel novalnetDirectDebitSepaPaymentModeModel = (NovalnetDirectDebitSepaPaymentModeModel) findPaymentModeModel(NOVALNET_DIRECT_DEBIT_SEPA);
                NovalnetGuaranteedDirectDebitSepaPaymentModeModel novalnetGuaranteedDirectDebitSepaPaymentModeModel = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) findPaymentModeModel(NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA);
                NovalnetDirectDebitAchPaymentModeModel novalnetDirectDebitAchPaymentModeModel = (NovalnetDirectDebitAchPaymentModeModel) findPaymentModeModel(NOVALNET_DIRECT_DEBIT_ACH);
                model.addAttribute(NOVALNET_DIRECT_DEBIT_SEPA, novalnetDirectDebitSepaPaymentModeModel);
                model.addAttribute(NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA, novalnetGuaranteedDirectDebitSepaPaymentModeModel);
                model.addAttribute(NOVALNET_DIRECT_DEBIT_ACH, novalnetDirectDebitAchPaymentModeModel);

                if ((novalnetDirectDebitSepaPaymentModeModel.getActive()
                        && novalnetDirectDebitSepaPaymentModeModel.getNovalnetOneClickShopping())
                        || (novalnetGuaranteedDirectDebitSepaPaymentModeModel.getActive()
                        && novalnetGuaranteedDirectDebitSepaPaymentModeModel.getNovalnetOneClickShopping())) {
                    showOneClickShopping(model, novalnetDirectDebitSepaPaymentModeModel.getCode(), paymentDetailsForm);
                }
                if (novalnetDirectDebitAchPaymentModeModel.getActive()
                        && novalnetDirectDebitAchPaymentModeModel.getNovalnetOneClickShopping()) {
                    showOneClickShopping(model, novalnetDirectDebitAchPaymentModeModel.getCode(), paymentDetailsForm);
                }
                break;
        }

        model.addAttribute("endswith", Localization.getLocalizedString("novalnet.endswith"));
        model.addAttribute("expires", Localization.getLocalizedString("novalnet.expires"));
        model.addAttribute("creditcardAddNew", Localization.getLocalizedString("novalnet.creditcardaddnew"));
        model.addAttribute("sepaAddNew", Localization.getLocalizedString("novalnet.sepaaddnew"));
        model.addAttribute("achAddNew", Localization.getLocalizedString("novalnet.achaddnew"));
        model.addAttribute("paypalAddNew", Localization.getLocalizedString("novalnet.paypaladdnew"));
        model.addAttribute("displayLogo", baseStoreModel.getNovalnetPaymentLogo());

        model.addAttribute("lang", language.toString().toUpperCase(Locale.ROOT));
        model.addAttribute("currency", cartData.getTotalPriceWithTax().getCurrencyIso());
        BigDecimal totalAmount = cartData.getTotalPriceWithTax().getValue();
        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(STATUS_VALUE)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        model.addAttribute("orderAmountCent", amountInCents);
        String guestEmail = novalnetCheckoutService.getUserEmail();
        final String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getName();
        model.addAttribute("email", emailAddress);
        return paymentDetailsForm;
    }

    @Override
    public String createPaymentRequest(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData addressData, CartData cartData, HttpServletRequest request) {

        AddressForm addressForm = paymentDetailsForm.getBillingAddress();
        Customer customer = new Customer();
        Billing billing = new Billing();
        Shipping shipping = new Shipping();
        PaymentData paymentData = new PaymentData();

        Merchant merchant = novalnetPaymentRequestBuilder.createMerchant(baseStoreService.getCurrentBaseStore());
        Custom custom = new Custom();
        HostedPage hostedPage = new HostedPage();
        String currentPayment = paymentDetailsForm.getSelectedPaymentMethodId();
        final Locale language = JaloSession.getCurrentSession().getSessionContext().getLocale();
        final String languageCode = language != null ? language.toString().toUpperCase(Locale.ROOT) : "EN";
        custom.setLang(languageCode);
        if (currentPayment == null) {
            currentPayment = "ACCOUNT";
        }
        sessionService.setAttribute("currentPaymentName", currentPayment);
        sessionService.removeAttribute(SELECTED_PAYMENT_REFERENCE_PK_FOR_REPLENISHMENT);

        String email = novalnetCheckoutService.getUserEmail();
        String customerNo = userService.getCurrentUser().getPk().toString();
        String clientIp = NovalnetUtils.extractClientIp(request);

        billingForm(paymentDetailsForm, addressData, cartData, addressForm, billing, shipping);
        customer = novalnetPaymentRequestBuilder.populateCustomerData(customer, addressData.getFirstName(), addressData.getLastName(),
                email, customerNo, clientIp, null, billing, shipping);

        BigDecimal totalAmount = cartData.getTotalPriceWithTax().getValue();
        long amountInCents = totalAmount.multiply(BigDecimal.valueOf(STATUS_VALUE)).setScale(0, RoundingMode.HALF_UP).longValueExact();
        Transaction transaction = novalnetPaymentRequestBuilder.createBaseTransaction(NovalnetUtils.getPaymentType(currentPayment), cartData.getTotalPriceWithTax().getCurrencyIso(),
                amountInCents, "SAP Commerce Cloud B2B", Config.getParameter("build.version") + "-NN1.0.0");

        switch (currentPayment) {
            case NOVALNET_DIRECT_DEBIT_SEPA:
                handleDirectDebitSeps(paymentDetailsForm, paymentData, transaction, currentPayment);
                break;
            case NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA:
                handleGuaranteedDirectDebitSepa(paymentDetailsForm, paymentData, transaction, currentPayment, billing);
                break;
            case NOVALNET_CREDIT_CARD:
                handleCreditCard(paymentDetailsForm, paymentData, transaction, currentPayment, request);
                break;
            case NOVALNET_DIRECT_DEBIT_ACH:
                handleDirectDebitAch(paymentDetailsForm, paymentData, transaction, currentPayment);
                break;
            case NOVALNET_GUARANTEED_INVOICE:
                billing.setCompany(paymentDetailsForm.getCompanyName());
                handleGuaranteedInvoice(transaction, currentPayment);
                break;
            case NOVALNET_INVOICE:
                handleInvoice(transaction, currentPayment);
                break;
            case NOVALNET_APPLE_PAY:
                handleApplePay(transaction, currentPayment, hostedPage, request);
                break;
            default:
                handleTestMode(transaction, currentPayment);
                break;

        }
        customer.setBilling(billing);
        String[] redirectPaymentsPayments = {NOVALNET_PRZELEWY_24, NOVALNET_POST_FINANCE_CARD, NOVALNET_POST_FINANCE, NOVALNET_EPS, NOVALNET_TWINT, NOVALNET_IDEAL,
                NOVALNET_BANCONTACT, NOVALNET_PAY_PAL, NOVALNET_ONLINE_BANK_TRANSFER, NOVALNET_MB_WAY, NOVALNET_WE_CHAT_PAY,
                NOVALNET_BLIK, NOVALNET_ALI_PAY, NOVALNET_TRUSTLY, NOVALNET_MULTIBANCO};
        if (Arrays.asList(redirectPaymentsPayments).contains(currentPayment)) {
            String currentUrl = request.getRequestURL().toString();
            String returnUrl = currentUrl.replace((!NOVALNET_GOOGLE_PAY.contains(currentPayment)) ? NOVALNET_PAYMENT_METHOD : NOVALNET_WALLET_TRANSACTION, NOVALNET_HOP_RESPONSE);

            transaction.setReturn_url(returnUrl);
            transaction.setError_return_url(currentUrl);
            sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
        }

        NovalnetPaymentRequest paymentRequest = novalnetPaymentRequestBuilder.createPaymentRequest(merchant, customer, transaction, custom, hostedPage);
        String jsonBody = NovalnetUtils.convetToString(paymentRequest);
        sessionService.setAttribute("paymentRequest", jsonBody);
        return jsonBody;
    }

    @Override
    public String createPaymentRequest(final CartModel cart, final NovalnetReplPayRefInfoModel paymentReference) {
        final BaseStoreModel baseStore = cart.getStore();
        if (baseStore == null || baseStore.getNovalnetAPIKey() == null) {
            throw new IllegalStateException("Novalnet API credentials are not configured for the scheduled cart base store");
        }

        final String paymentType = NovalnetUtils.getPaymentType(paymentReference.getPaymentType());
        if (paymentType == null) {
            throw new IllegalArgumentException("Unsupported Novalnet payment type: " + paymentReference.getPaymentType());
        }

        final Merchant merchant = novalnetPaymentRequestBuilder.createMerchant(baseStore);

        final PaymentData paymentData = new PaymentData();
        paymentData.setToken(paymentReference.getToken());

        final Transaction transaction = new Transaction();
        transaction.setAmount(cartAmountInCents(cart));
        transaction.setCurrency(cart.getCurrency().getIsocode());
        transaction.setPayment_type(paymentType);
        transaction.setTest_mode(resolveTestMode(paymentReference.getPaymentType()));
        transaction.setSystem_name("SAP Commerce Cloud B2B");
        transaction.setSystem_version(Config.getParameter("build.version") + "-NN1.0.0");
        transaction.setPayment_data(paymentData);

        final UserModel user = cart.getUser();
        final AddressModel billingAddress = cart.getPaymentAddress();
        final Customer customer = new Customer();
        customer.setCustomer_no(user.getPk().toString());
        customer.setEmail(user.getUid());
        if (billingAddress != null) {
            customer.setFirst_name(billingAddress.getFirstname());
            customer.setLast_name(billingAddress.getLastname());
            customer.setBilling(createBilling(billingAddress));
        } else if (user instanceof CustomerModel customerModel) {
            customer.setFirst_name(customerModel.getName());
            customer.setLast_name(customerModel.getName());
        }
        customer.setShipping(createShipping(cart.getDeliveryAddress(), billingAddress));

        final Custom custom = new Custom();
        custom.setLang(user.getSessionLanguage() == null ? "EN" : user.getSessionLanguage().getIsocode().toUpperCase(Locale.ROOT));

        final NovalnetPaymentRequest paymentRequest = new NovalnetPaymentRequest();
        paymentRequest.setMerchant(merchant);
        paymentRequest.setCustomer(customer);
        paymentRequest.setTransaction(transaction);
        paymentRequest.setCustom(custom);
        paymentRequest.setHosted_page(new HostedPage());

        try {
            return new ObjectMapper().writeValueAsString(paymentRequest);
        } catch (JsonProcessingException e) {
            throw new NovalnetException("Unable to create replenishment payment request", e);
        }
    }

    private Long cartAmountInCents(final CartModel cart) {
        return BigDecimal.valueOf(cart.getTotalPrice()).multiply(BigDecimal.valueOf(STATUS_VALUE)).setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }

    private String resolveTestMode(final String paymentCode) {
        final PaymentModeModel paymentMode = paymentModeService.getPaymentModeForCode(paymentCode);
        final Boolean testMode;
        if (paymentMode instanceof NovalnetCreditCardPaymentModeModel creditCard) {
            testMode = creditCard.getNovalnetTestMode();
        } else if (paymentMode instanceof NovalnetDirectDebitSepaPaymentModeModel directDebitSepa) {
            testMode = directDebitSepa.getNovalnetTestMode();
        } else if (paymentMode instanceof NovalnetGuaranteedDirectDebitSepaPaymentModeModel guaranteedDirectDebitSepa) {
            testMode = guaranteedDirectDebitSepa.getNovalnetTestMode();
        } else if (paymentMode instanceof NovalnetDirectDebitAchPaymentModeModel directDebitAch) {
            testMode = directDebitAch.getNovalnetTestMode();
        } else {
            throw new IllegalArgumentException("No supported Novalnet payment mode found for: " + paymentCode);
        }
        return Boolean.TRUE.equals(testMode) ? "1" : "0";
    }

    private Billing createBilling(final AddressModel address) {
        final Billing billing = new Billing();
        billing.setFirst_name(address.getFirstname());
        billing.setLast_name(address.getLastname());
        billing.setStreet(StringUtils.defaultString(address.getStreetname()) + StringUtils.defaultString(address.getStreetnumber()));
        billing.setHouse_no(address.getStreetnumber());
        billing.setCity(address.getTown());
        billing.setZip(address.getPostalcode());
        if (address.getCountry() != null) {
            billing.setCountry_code(address.getCountry().getIsocode());
        }
        billing.setTel(address.getPhone1());
        billing.setCompany(address.getCompany());
        return billing;
    }

    private Shipping createShipping(final AddressModel shippingAddress, final AddressModel billingAddress) {
        final Shipping shipping = new Shipping();
        if (shippingAddress == null || shippingAddress.equals(billingAddress)) {
            shipping.setSame_as_billing("1");
            return shipping;
        }
        shipping.setFirst_name(shippingAddress.getFirstname());
        shipping.setLast_name(shippingAddress.getLastname());
        shipping.setStreet(StringUtils.defaultString(shippingAddress.getStreetname()) + StringUtils.defaultString(shippingAddress.getStreetnumber()));
        shipping.setCity(shippingAddress.getTown());
        shipping.setZip(shippingAddress.getPostalcode());
        if (shippingAddress.getCountry() != null) {
            shipping.setCountry_code(shippingAddress.getCountry().getIsocode());
        }
        return shipping;
    }

    /**
     * Handle test mode.
     *
     * @param transaction    the transaction
     * @param currentPayment the current payment
     */
    public void handleTestMode(Transaction transaction, String currentPayment) {
        PaymentModeModel paymentModeModel = findPaymentModeModel(currentPayment);
        switch (currentPayment) {
            case NOVALNET_PRZELEWY_24:
                NovalnetPrzelewy24PaymentModeModel przelewy24PaymentModeModel = (NovalnetPrzelewy24PaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(przelewy24PaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_POST_FINANCE_CARD:
                NovalnetPostFinanceCardPaymentModeModel novalnetPostFinanceCardPaymentModeModel = (NovalnetPostFinanceCardPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetPostFinanceCardPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_POST_FINANCE:
                NovalnetPostFinancePaymentModeModel novalnetPostFinancePaymentModeModel = (NovalnetPostFinancePaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetPostFinancePaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_EPS:
                NovalnetEpsPaymentModeModel novalnetEpsPaymentModeModel = (NovalnetEpsPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetEpsPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_TWINT:
                NovalnetTwintPaymentModeModel novalnetTwintPaymentModeModel = (NovalnetTwintPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetTwintPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_IDEAL:
                NovalnetIdealPaymentModeModel novalnetIdealPaymentModeModel = (NovalnetIdealPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetIdealPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_BANCONTACT:
                NovalnetBancontactPaymentModeModel novalnetBancontactPaymentModeModel = (NovalnetBancontactPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetBancontactPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_PAY_PAL:
                NovalnetPayPalPaymentModeModel novalnetPayPalPaymentModeModel = (NovalnetPayPalPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetPayPalPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_ONLINE_BANK_TRANSFER:
                NovalnetOnlineBankTransferPaymentModeModel novalnetOnlineBankTransferPaymentModeModel = (NovalnetOnlineBankTransferPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(
                        Boolean.TRUE.equals(novalnetOnlineBankTransferPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_MB_WAY:
                NovalnetMbWayPaymentModeModel novalnetMbWayPaymentModeModel = (NovalnetMbWayPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetMbWayPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_WE_CHAT_PAY:
                NovalnetWeChatPayPaymentModeModel novalnetWeChatPayPaymentModeModel = (NovalnetWeChatPayPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetWeChatPayPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_BLIK: // kept exactly as in the original B2B code (see notes)
                NovalnetBlikPaymentModeModel novalnetBlikPaymentModeModel = (NovalnetBlikPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetBlikPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_ALI_PAY:
                NovalnetAliPayPaymentModeModel novalnetAliPayPaymentModeModel = (NovalnetAliPayPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetAliPayPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_TRUSTLY:
                NovalnetTrustlyPaymentModeModel novalnetTrustlyPaymentModeModel = (NovalnetTrustlyPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetTrustlyPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_MULTIBANCO:
                NovalnetMultibancoPaymentModeModel novalnetMultibancoPaymentModeModel = (NovalnetMultibancoPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetMultibancoPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                break;
            case NOVALNET_PREPAYMENT:
                NovalnetPrepaymentPaymentModeModel novalnetPrepaymentPaymentModeModel = (NovalnetPrepaymentPaymentModeModel) paymentModeModel;
                transaction.setTest_mode(Boolean.TRUE.equals(novalnetPrepaymentPaymentModeModel.getNovalnetTestMode()) ? "1" : "0");
                if (novalnetPrepaymentPaymentModeModel.getNovalnetDueDate() != null) {
                    transaction.setDue_date(NovalnetUtils.formatDate(novalnetPrepaymentPaymentModeModel.getNovalnetDueDate()));
                }
                break;
        }
    }

    private void handleApplePay(Transaction transaction, String currentPayment, HostedPage hostedPage, HttpServletRequest request) {
        NovalnetApplePayPaymentModeModel paymentModeModel = (NovalnetApplePayPaymentModeModel) findPaymentModeModel(currentPayment);
        String[] hideBlocks = {"ADDRESS_FORM", "SHOP_INFO", "LANGUAGE_MENU", "HEADER", "TARIFF"};
        String[] skipPages = {"CONFIRMATION_PAGE", "SUCCESS_PAGE"};
        String[] displayPayment = {"APPLEPAY"};
        if (paymentModeModel != null && paymentModeModel.getOnholdActionTypeWithZeroAmount() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            hostedPage.setDisplay_payments(Arrays.asList(displayPayment));
            hostedPage.setHide_blocks(Arrays.asList(hideBlocks));
            hostedPage.setSkip_pages(Arrays.asList(skipPages));

            String currentUrl = request.getRequestURL().toString();
            String returnUrl = currentUrl.replace((!NOVALNET_GOOGLE_PAY.contains(currentPayment)) ? NOVALNET_PAYMENT_METHOD : NOVALNET_WALLET_TRANSACTION, NOVALNET_HOP_RESPONSE);

            transaction.setReturn_url(returnUrl);
            transaction.setError_return_url(currentUrl);

            Integer minAmount = paymentModeModel.getNovalnetOnholdAmount() != null ? paymentModeModel.getNovalnetOnholdAmount() : 0;
            if (OnholdActionTypeWithZeroAmount.AUTHORIZE.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount()) && (transaction.getAmount() != null && transaction.getAmount() >= minAmount.longValue())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeHostedUrl());
            } else if (OnholdActionTypeWithZeroAmount.AUTHORIZE_WITH_ZERO_AMOUNT.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentHostedUrl());
                transaction.setAmount(0L);
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentHostedUrl());
            }
        }
    }

    private void handleCreditCard(NovalnetPaymentDetailsForm paymentDetailsForm, PaymentData paymentData, Transaction transaction,
                                  String currentPayment, HttpServletRequest request) {
        if (paymentDetailsForm.getCreditCardOneClickData1() != null && ("1".equals(paymentDetailsForm.getCreditCardOneClickData1()) || "2".equals(paymentDetailsForm.getCreditCardOneClickData1()))) {
            selectSavedPaymentReference(paymentDetailsForm.getCreditCardOneClickData1(), "novalnetCreditCardOneClickToken", CREDIT_CARD_REFERENCE_PK, paymentData);
        } else if (paymentDetailsForm.isCreditcardSaveData()) {
            transaction.setCreate_token("1");
        }
        paymentData.setPan_hash(paymentDetailsForm.getNovalnetCreditCardPanHash());
        paymentData.setUnique_id(paymentDetailsForm.getNovalnetCreditCardUniqueId());
        transaction.setPayment_data(paymentData);
        NovalnetCreditCardPaymentModeModel paymentModeModel = (NovalnetCreditCardPaymentModeModel) findPaymentModeModel(currentPayment);
        if ((paymentDetailsForm.getDo_redirect() != null && "1".equals(paymentDetailsForm.getDo_redirect()))) {

            if (Boolean.TRUE.equals(paymentModeModel.getNovalnetEnforce3D())) {
                final Integer enforceValue = 1;
                transaction.setEnforce_3d(enforceValue);
            }
            String currentUrl = request.getRequestURL().toString();
            String returnUrl = currentUrl.replace((!NOVALNET_GOOGLE_PAY.contains(currentPayment)) ? NOVALNET_PAYMENT_METHOD : NOVALNET_WALLET_TRANSACTION, NOVALNET_HOP_RESPONSE);

            transaction.setReturn_url(returnUrl);
            transaction.setError_return_url(currentUrl);
        }

        if (paymentModeModel != null && paymentModeModel.getOnholdActionTypeWithZeroAmount() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            Integer minAmount = paymentModeModel.getNovalnetOnholdAmount() != null ? paymentModeModel.getNovalnetOnholdAmount() : 0;
            if (OnholdActionTypeWithZeroAmount.AUTHORIZE.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount()) && (transaction.getAmount() != null && transaction.getAmount() >= minAmount.longValue())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
            } else if (OnholdActionTypeWithZeroAmount.AUTHORIZE_WITH_ZERO_AMOUNT.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
                transaction.setAmount(0L);
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
    }

    private void handleDirectDebitSeps(NovalnetPaymentDetailsForm paymentDetailsForm, PaymentData paymentData, Transaction transaction, String currentPayment) {
        if (paymentDetailsForm.getDirectDebitSepaOneClickData1() != null && ("1".equals(paymentDetailsForm.getDirectDebitSepaOneClickData1()) || "2".equals(paymentDetailsForm.getDirectDebitSepaOneClickData1()))) {
            selectSavedPaymentReference(paymentDetailsForm.getDirectDebitSepaOneClickData1(), NOVALNET_SEPA_ONE_CLICK,
                    DIRECT_DEBIT_SEPA_REFERENCE_PK, paymentData);
        } else {
            paymentData.setAccount_holder(paymentDetailsForm.getAccountHolder());
            paymentData.setIban(paymentDetailsForm.getAccountIban());
            paymentData.setBic(paymentDetailsForm.getAccountBic());
            if (paymentDetailsForm.isDirectDebitSepaSaveData()) {
                transaction.setCreate_token("1");
            }
        }
        NovalnetDirectDebitSepaPaymentModeModel paymentModeModel = (NovalnetDirectDebitSepaPaymentModeModel) findPaymentModeModel(
                currentPayment);
        transaction.setPayment_data(paymentData);

        if (paymentModeModel != null && paymentModeModel.getOnholdActionTypeWithZeroAmount() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            if (paymentModeModel.getNovalnetDueDate() != null) {
                transaction.setDue_date(NovalnetUtils.formatDate(paymentModeModel.getNovalnetDueDate()));
            }
            Integer minAmount = paymentModeModel.getNovalnetOnholdAmount() != null ? paymentModeModel.getNovalnetOnholdAmount() : 0;
            if (OnholdActionTypeWithZeroAmount.AUTHORIZE.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount()) && (transaction.getAmount() != null && transaction.getAmount() >= minAmount.longValue())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
            } else if (OnholdActionTypeWithZeroAmount.AUTHORIZE_WITH_ZERO_AMOUNT.equals(paymentModeModel.getOnholdActionTypeWithZeroAmount())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
                transaction.setAmount(0L);
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
    }

    private void handleGuaranteedDirectDebitSepa(NovalnetPaymentDetailsForm paymentDetailsForm, PaymentData paymentData, Transaction transaction, String currentPayment, Billing billing) {
        if (paymentDetailsForm.getGuaranteedDirectDebitSepaOneClickData1() != null && ("1".equals(paymentDetailsForm.getGuaranteedDirectDebitSepaOneClickData1()) || "2".equals(paymentDetailsForm.getGuaranteedDirectDebitSepaOneClickData1()))) {
            selectSavedPaymentReference(paymentDetailsForm.getGuaranteedDirectDebitSepaOneClickData1(), NOVALNET_SEPA_ONE_CLICK, DIRECT_DEBIT_SEPA_REFERENCE_PK, paymentData);
            billing.setCompany(paymentDetailsForm.getCompanyName());
        } else {
            paymentData.setAccount_holder(paymentDetailsForm.getGuaranteeAccountHolder());
            paymentData.setIban(paymentDetailsForm.getGuaranteeAccountIban());
            paymentData.setBic(paymentDetailsForm.getGuaranteeAccountBic());
            billing.setCompany(paymentDetailsForm.getCompanyName());
            if (paymentDetailsForm.isGuaranteedDirectDebitSepaSaveData()) {
                transaction.setCreate_token("1");
            }
        }
        transaction.setPayment_data(paymentData);
        NovalnetGuaranteedDirectDebitSepaPaymentModeModel paymentModeModel = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) findPaymentModeModel(currentPayment);

        if (paymentModeModel != null && paymentModeModel.getNovalnetOnholdAction() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            Integer minAmount = paymentModeModel.getNovalnetOnholdAmount() != null ? paymentModeModel.getNovalnetOnholdAmount() : 0;
            if (OnholdActionType.AUTHORIZE.equals(paymentModeModel.getNovalnetOnholdAction()) && (transaction.getAmount() != null && transaction.getAmount() >= minAmount.longValue())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
    }

    private void handleDirectDebitAch(NovalnetPaymentDetailsForm paymentDetailsForm, PaymentData paymentData, Transaction transaction, String currentPayment) {
        if (paymentDetailsForm.getNovalnetDirectDebitAchOneClickData1() != null && ("1".equals(paymentDetailsForm.getNovalnetDirectDebitAchOneClickData1()) || "2".equals(paymentDetailsForm.getNovalnetDirectDebitAchOneClickData1()))) {
            selectSavedPaymentReference(paymentDetailsForm.getNovalnetDirectDebitAchOneClickData1(), "novalnetDirectDebitAchOneClickToken", DIRECT_DEBIT_ACH_REFERENCE_PK, paymentData);
        } else {
            paymentData.setAccount_holder(paymentDetailsForm.getAccountHolder());
            paymentData.setAccount_number(paymentDetailsForm.getAchAccountNumber());
            paymentData.setRouting_number(paymentDetailsForm.getAchRoutingNumber());
            if (paymentDetailsForm.isNovalnetDirectDebitAchSaveData()) {
                transaction.setCreate_token("1");
            }
        }
        transaction.setPayment_data(paymentData);
        NovalnetDirectDebitAchPaymentModeModel paymentModeModel = (NovalnetDirectDebitAchPaymentModeModel) findPaymentModeModel(currentPayment);

        if (paymentModeModel != null && paymentModeModel.getOnholdActionTypeWithoutAuthorize() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            if (OnholdActionTypeWithoutAuthorize.AUTHORIZE_WITH_ZERO_AMOUNT.equals(paymentModeModel.getOnholdActionTypeWithoutAuthorize())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
                transaction.setAmount(0L);
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
    }

    private void selectSavedPaymentReference(String selection, String tokenAttributePrefix, String referencePkAttributePrefix, PaymentData paymentData) {
        String token = sessionService.getAttribute(tokenAttributePrefix + selection);
        String paymentReferencePk = sessionService.getAttribute(referencePkAttributePrefix + selection);
        if (token == null || token.isBlank() || paymentReferencePk == null || paymentReferencePk.isBlank()) {
            throw new IllegalArgumentException("The selected saved Novalnet payment reference is unavailable");
        }
        paymentData.setToken(token);
        sessionService.setAttribute(SELECTED_PAYMENT_REFERENCE_PK_FOR_REPLENISHMENT, paymentReferencePk);
    }

    private void handleInvoice(Transaction transaction, String currentPayment) {
        NovalnetInvoicePaymentModeModel paymentModeModel = (NovalnetInvoicePaymentModeModel) findPaymentModeModel(currentPayment);
        if (paymentModeModel != null && paymentModeModel.getNovalnetOnholdAction() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            if (paymentModeModel.getNovalnetDueDate() != null) {
                transaction.setDue_date(NovalnetUtils.formatDate(paymentModeModel.getNovalnetDueDate()));
            }
            Integer minAmount = paymentModeModel.getNovalnetOnholdAmount() != null ? paymentModeModel.getNovalnetOnholdAmount() : 0;
            if (OnholdActionType.AUTHORIZE.equals(paymentModeModel.getNovalnetOnholdAction()) && (transaction.getAmount() != null && transaction.getAmount() >= minAmount.longValue())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
    }

    private void handleGuaranteedInvoice(Transaction transaction, String currentPayment) {
        NovalnetGuaranteedInvoicePaymentModeModel paymentModeModel = (NovalnetGuaranteedInvoicePaymentModeModel) findPaymentModeModel(currentPayment);
        if (paymentModeModel != null && paymentModeModel.getNovalnetOnholdAction() != null) {
            transaction.setTest_mode(Boolean.TRUE.equals(paymentModeModel.getNovalnetTestMode()) ? "1" : "0");
            Integer minAmount = paymentModeModel.getNovalnetOnholdAmount() != null ? paymentModeModel.getNovalnetOnholdAmount() : 0;
            if (OnholdActionType.AUTHORIZE.equals(paymentModeModel.getNovalnetOnholdAction()) && (transaction.getAmount() != null && transaction.getAmount() >= minAmount.longValue())) {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getAuthorizeUrl());
            } else {
                sessionService.setAttribute(PAYMENT_URL, novalnetEndpointConfigService.getPaymentUrl());
            }
        }
    }

    private PaymentModeModel findPaymentModeModel(String paymentType) {
        return paymentModeService.getPaymentModeForCode(paymentType);
    }

    private void showOneClickShopping(Model model, String code, NovalnetPaymentDetailsForm paymentDetailsForm) {

        model.addAttribute(code + "OneClick", false);
        model.addAttribute(code + "OneClickEnabled", false);
        UserModel currentUser = userService.getCurrentUser();
        final CustomerData customerData = customerFacade.getCurrentCustomer();
        if (!userService.isAnonymousUser(currentUser)) {
            paymentDetailsForm.setAccountHolder(customerData.getFirstName() + " " + customerData.getLastName());
            paymentDetailsForm.setGuaranteeAccountHolder(customerData.getFirstName() + " " + customerData.getLastName());
            paymentDetailsForm.setAchAccountHolder(customerData.getFirstName() + " " + customerData.getLastName());
            final List<NovalnetPaymentRefInfoModel> paymentInfo = novalnetDao.getPaymentRefInfo(currentUser.getPk().toString(), code);

            if (paymentInfo == null || paymentInfo.isEmpty()) {
                return;
            }

            int index = 1;

            for (NovalnetPaymentRefInfoModel paymentInfoModel : paymentInfo) {
                switch (paymentInfoModel.getPaymentType()) {

                    case NOVALNET_CREDIT_CARD:
                        addCreditCardInfo(model, paymentInfoModel, index);
                        index++;
                        break;
                    case NOVALNET_DIRECT_DEBIT_ACH:
                        paymentDetailsForm.setNovalnetAchOneClickAccountHolder(paymentInfoModel.getAccountHolder());
                        paymentDetailsForm.setNovalnetAchOneClickAccountNumber(paymentInfoModel.getMaskedAchAccountNumber());
                        addAchInfo(model, paymentInfoModel, index);
                        index++;
                        break;

                    case NOVALNET_DIRECT_DEBIT_SEPA, NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA:
                        paymentDetailsForm.setNovalnetDirectDebitSepaOneClickAccountHolder(paymentInfoModel.getAccountHolder());
                        paymentDetailsForm.setNovalnetDirectDebitSepaOneClickAccountIban(paymentInfoModel.getMaskedAccountIban());
                        addSepaInfo(model, paymentInfoModel, index);
                        index++;
                        break;
                }
            }
        }
    }

    private void addCreditCardInfo(Model model, NovalnetPaymentRefInfoModel card, int index) {
        final int value = 4;
        model.addAttribute("novalnetCreditCardOneClickCardType" + index, card.getCardType().toLowerCase());
        model.addAttribute("novalnetCreditCardOneClickCardHolder" + index, card.getCardHolder());
        String maskedNumber = card.getMaskedCardNumber();
        model.addAttribute("novalnetCreditCardOneClickMaskedCardNumber" + index, maskedNumber.substring(maskedNumber.length() - value));
        model.addAttribute("novalnetCreditCardOneClickCardExpiry" + index, card.getExpiryDate());

        sessionService.setAttribute("novalnetCreditCardOneClickToken" + index, card.getToken());
        sessionService.setAttribute(CREDIT_CARD_REFERENCE_PK + index, card.getPk().toString());
    }

    private void addAchInfo(Model model, NovalnetPaymentRefInfoModel account, int index) {

        model.addAttribute("novalnetDirectDebitAchAccountHolder" + index, account.getAccountHolder());
        model.addAttribute("novalnetDirectDebitAchAccountNumber" + index, account.getMaskedAchAccountNumber());
        model.addAttribute("novalnetDirectDebitAchValue" + index, account.getPk().toString());

        sessionService.setAttribute("novalnetDirectDebitAchOneClickToken" + index, account.getToken());
        sessionService.setAttribute(DIRECT_DEBIT_ACH_REFERENCE_PK + index, account.getPk().toString());
    }

    private void addSepaInfo(Model model, NovalnetPaymentRefInfoModel account, int index) {

        model.addAttribute("novalnetDirectDebitSepaAccountHolder" + index, account.getAccountHolder());

        model.addAttribute("novalnetDirectDebitSepaAccountIban" + index, account.getMaskedAccountIban());

        model.addAttribute("novalnetDirectDebitSepaValue" + index, account.getPk().toString());

        sessionService.setAttribute(NOVALNET_SEPA_ONE_CLICK + index, account.getToken());
        sessionService.setAttribute(DIRECT_DEBIT_SEPA_REFERENCE_PK + index, account.getPk().toString());
    }

    private AddressForm getAddressForm(CartData cartData, Model model) {
        AddressForm addressForm = new AddressForm();
        if (cartData.getPaymentInfo() != null && cartData.getPaymentInfo().getBillingAddress() != null) {
            addressForm = populateAddressForm(cartData.getPaymentInfo().getBillingAddress());
        } else if (cartData.getDeliveryAddress() != null) {
            addressForm = populateAddressForm(cartData.getDeliveryAddress());
        }

        if (StringUtils.isNotBlank(addressForm.getCountryIso())) {
            model.addAttribute("regions", i18NFacade.getRegionsForCountryIso(addressForm.getCountryIso()));
            model.addAttribute("country", addressForm.getCountryIso());
        }
        return addressForm;
    }

    private AddressForm populateAddressForm(final AddressData addressData) {
        final AddressForm addressForm = new AddressForm();
        addressForm.setAddressId(addressData.getId());
        addressForm.setFirstName(addressData.getFirstName());
        addressForm.setLastName(addressData.getLastName());
        addressForm.setLine1(addressData.getLine1());
        if (addressData.getLine2() != null) {
            addressForm.setLine2(addressData.getLine2());
        }
        addressForm.setTownCity(addressData.getTown());
        addressForm.setPostcode(addressData.getPostalCode());
        addressForm.setCountryIso(addressData.getCountry().getIsocode());
        if (addressData.getRegion() != null) {
            addressForm.setRegionIso(addressData.getRegion().getIsocode());
        }
        addressForm.setShippingAddress(addressData.isShippingAddress());
        addressForm.setBillingAddress(addressData.isBillingAddress());
        if (addressData.getPhone() != null) {
            addressForm.setPhone(addressData.getPhone());
        }
        return addressForm;

    }

    /**
     * Billing form.
     *
     * @param paymentDetailsForm the payment details form
     * @param addressData        the address data
     * @param cartData           the cart data
     * @param addressForm        the address form
     * @param billing            the billing
     * @param shipping           the shipping
     */
    public void billingForm(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData addressData, CartData cartData, AddressForm addressForm, Billing billing, Shipping shipping) {
        if (paymentDetailsForm.isUseDeliveryAddress()) {
            billing.setFirst_name(addressData.getFirstName());
            billing.setLast_name(addressData.getLastName());
            billing.setStreet(addressData.getLine1() + addressData.getLine2());
            billing.setCity(addressData.getTown());
            billing.setZip(addressData.getPostalCode());
            billing.setCountry_code(addressData.getCountry().getIsocode());
            shipping.setSame_as_billing("1");
        } else {
            shipping.setFirst_name(cartData.getDeliveryAddress().getFirstName());
            shipping.setLast_name(cartData.getDeliveryAddress().getLastName());
            shipping.setStreet(cartData.getDeliveryAddress().getLine1() + cartData.getDeliveryAddress().getLine2());
            shipping.setCity(cartData.getDeliveryAddress().getTown());
            shipping.setZip(cartData.getDeliveryAddress().getPostalCode());
            shipping.setCountry_code(cartData.getDeliveryAddress().getCountry().getIsocode());
            if (addressForm != null) {
                addressData.setId(addressForm.getAddressId());
                addressData.setTitleCode(addressForm.getTitleCode());
                addressData.setFirstName(addressForm.getFirstName());
                addressData.setLastName(addressForm.getLastName());
                addressData.setLine1(addressForm.getLine1());
                addressData.setLine2(addressForm.getLine2());
                addressData.setTown(addressForm.getTownCity());
                addressData.setPostalCode(addressForm.getPostcode());
                if (addressForm.getCountryIso() != null) {
                    addressData.setCountry(i18NFacade.getCountryForIsocode(addressForm.getCountryIso()));
                }
                if (addressForm.getRegionIso() != null) {
                    addressData.setRegion(i18NFacade.getRegion(addressForm.getCountryIso(), addressForm.getRegionIso()));
                }
                addressData.setShippingAddress(Boolean.TRUE.equals(addressForm.getShippingAddress()));
                addressData.setBillingAddress(Boolean.TRUE.equals(addressForm.getBillingAddress()));
            }
            billing.setFirst_name(paymentDetailsForm.getBillTo_firstName());
            billing.setLast_name(paymentDetailsForm.getBillTo_lastName());
            billing.setStreet(paymentDetailsForm.getBillTo_street1() + paymentDetailsForm.getBillTo_street2());
            billing.setCity(paymentDetailsForm.getBillTo_city());
            billing.setZip(paymentDetailsForm.getBillTo_postalCode());
            billing.setCountry_code(paymentDetailsForm.getBillTo_country());
            addressData.setTitleCode(paymentDetailsForm.getBillTo_titleCode());
            addressData.setFirstName(paymentDetailsForm.getBillTo_firstName());
            addressData.setLastName(paymentDetailsForm.getBillTo_lastName());
            addressData.setLine1(paymentDetailsForm.getBillTo_street1());
            addressData.setLine2(paymentDetailsForm.getBillTo_street2());
            addressData.setTown(paymentDetailsForm.getBillTo_city());
            addressData.setPostalCode(paymentDetailsForm.getBillTo_postalCode());

        }
    }
}
