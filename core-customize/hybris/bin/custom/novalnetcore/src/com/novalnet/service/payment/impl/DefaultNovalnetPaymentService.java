/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.AddressForm;
import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.model.NovalnetApplePayPaymentModeModel;
import com.novalnet.model.NovalnetCreditCardPaymentModeModel;
import com.novalnet.model.NovalnetDirectDebitAchPaymentModeModel;
import com.novalnet.model.NovalnetDirectDebitSepaPaymentModeModel;
import com.novalnet.model.NovalnetGooglePayPaymentModeModel;
import com.novalnet.model.NovalnetGuaranteedDirectDebitSepaPaymentModeModel;
import com.novalnet.model.NovalnetGuaranteedInvoicePaymentModeModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.springframework.ui.Model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;


/**
 * * Default implementation of {@link NovalnetPaymentService} for managing Novalnet payment information. *
 * <p>
 * * Handles payment data, transaction entries, payment modes, customer information, and one-click payment processing.
 */
@SuppressWarnings({"java:S1200", "java:S1149"})
public class DefaultNovalnetPaymentService implements NovalnetPaymentService {
    private static final int CONVERT_TO_CENT = 100;
    private static final String CART_DATA_ATTR = "cartData";

    private static final String PAYMENT_SEPA = "novalnetDirectDebitSepa";
    private static final String PAYMENT_GUARANTEED_SEPA = "novalnetGuaranteedDirectDebitSepa";
    private static final String PAYMENT_ACH = "novalnetDirectDebitAch";
    private static final String PAYMENT_CREDIT_CARD = "novalnetCreditCard";
    private static final String PAYMENT_PAYPAL = "novalnetPayPal";
    private static final String PAYMENT_INVOICE = "novalnetInvoice";
    private static final String PAYMENT_PREPAYMENT = "novalnetPrepayment";
    private static final String PAYMENT_MULTIBANCO = "novalnetMultibanco";
    private static final String PAYMENT_GUARANTEED_INVOICE = "novalnetGuaranteedInvoice";
    private static final String PAYMENT_ONLINE_BANK_TRANSFER = "novalnetOnlineBankTransfer";
    private static final String PAYMENT_BANCONTACT = "novalnetBancontact";
    private static final String PAYMENT_IDEAL = "novalnetIdeal";
    private static final String PAYMENT_TWINT = "novalnetTwint";
    private static final String PAYMENT_MBWAY = "novalnetMbWay";
    private static final String PAYMENT_TRUSTLY = "novalnetTrustly";
    private static final String PAYMENT_BLIK = "novalnetBlik";
    private static final String PAYMENT_WECHAT_PAY = "novalnetWeChatPay";
    private static final String PAYMENT_ALIPAY = "novalnetAlipay";
    private static final String PAYMENT_GOOGLE_PAY = "novalnetGooglePay";
    private static final String PAYMENT_APPLE_PAY = "novalnetApplePay";
    private static final String PAYMENT_EPS = "novalnetEps";
    private static final String PAYMENT_POST_FINANCE = "novalnetPostFinance";
    private static final String PAYMENT_POST_FINANCE_CARD = "novalnetPostFinanceCard";
    private static final String PAYMENT_PRZELEWY24 = "novalnetPrzelewy24";
    private static final int GUARANTEED_MIN_AMOUNT = 999;
    private static final int EXPIRY_YEAR_DIGITS = 2;

    private static final String ONE_CLICK = "OneClick";

    private static final String JSON_PAYMENT_DATA = "payment_data";
    private static final String JSON_TOKEN = "token";
    private static final String JSON_CARD_BRAND = "card_brand";
    private static final String JSON_CARD_NUMBER = "card_number";
    private static final String JSON_CARD_HOLDER = "card_holder";
    private static final String JSON_CARD_EXPIRY_MONTH = "card_expiry_month";
    private static final String JSON_CARD_EXPIRY_YEAR = "card_expiry_year";
    private static final String JSON_IBAN = "iban";
    private static final String JSON_ACCOUNT_HOLDER = "account_holder";
    private static final String JSON_ACCOUNT_NUMBER = "account_number";
    private static final String JSON_ROUTING_NUMBER = "routing_number";
    private static final String JSON_PAYPAL_TRANSACTION_ID = "paypal_transaction_id";
    private static final String JSON_PAYPAL_ACCOUNT = "paypal_account";
    private static final String JSON_TRANSACTION = "transaction";
    private static final String JSON_TID = "tid";
    private static final String JSON_CUSTOMER_NO = "customer_no";
    private static final String TOKEN = "Token";
    private static final int MAX_ONE_CLICK_PAYMENT_RECORDS = 2;


    private static final List<String> NOVALNET_PAYMENT_CODES = List.of(PAYMENT_SEPA, PAYMENT_GUARANTEED_SEPA, PAYMENT_ACH,
            PAYMENT_CREDIT_CARD, PAYMENT_PAYPAL, PAYMENT_INVOICE, PAYMENT_TWINT, PAYMENT_MBWAY, PAYMENT_TRUSTLY, PAYMENT_BLIK,
            PAYMENT_WECHAT_PAY, PAYMENT_ALIPAY, PAYMENT_GUARANTEED_INVOICE, PAYMENT_PREPAYMENT, PAYMENT_IDEAL, PAYMENT_APPLE_PAY,
            PAYMENT_GOOGLE_PAY, PAYMENT_PRZELEWY24, PAYMENT_EPS, PAYMENT_ONLINE_BANK_TRANSFER, PAYMENT_MULTIBANCO,
            PAYMENT_BANCONTACT, PAYMENT_POST_FINANCE, PAYMENT_POST_FINANCE_CARD);

    private static final int MASKED_CARD_LAST_DIGITS = 4;
    boolean creditCardZeroAmountBooking = false;
    boolean sepaZeroAmountBooking = false;
    boolean achZeroAmountBooking = false;
    boolean googlePayZeroAmountBooking = false;
    boolean applePayZeroAmountBooking = false;
    @Resource
    private ModelService modelService;
    @Resource
    private BaseStoreService baseStoreService;
    @Resource
    private NovalnetDao novalnetDao;
    @Resource
    private PaymentModeService paymentModeService;
    @Resource
    private NovalnetCheckoutService novalnetCheckoutService;
    @Resource
    private CustomerFacade customerFacade;
    @Resource
    private SessionService sessionService;
    @Resource
    private I18NFacade i18NFacade;

    @Override
    public NovalnetPaymentInfoModel getPaymentModel(List<NovalnetPaymentInfoModel> paymentInfo) {
        return modelService.get(paymentInfo.get(0).getPk());
    }

    @Override
    public String getPaymentName(String currentPayment) {
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
        return paymentModeModel != null ? paymentModeModel.getName() : "";
    }


    @Override
    public BaseStoreModel getBaseStoreModel() {
        return baseStoreService.getCurrentBaseStore();
    }


    @Override
    public void updatePaymentInfo(List<NovalnetPaymentInfoModel> orderReference, String tidStatus) {
        NovalnetPaymentInfoModel paymentInfoModel = modelService.get(orderReference.get(0).getPk());

        paymentInfoModel.setPaymentGatewayStatus(tidStatus);
        modelService.save(paymentInfoModel);
    }


    @Override
    public void handleReferenceTransactionInfo(String response, String customerNo, String currentPayment) {
        currentPayment = PAYMENT_GUARANTEED_SEPA.equals(currentPayment) ? PAYMENT_SEPA : currentPayment;

        JSONObject transactionJsonObject = new JSONObject(response).getJSONObject(JSON_TRANSACTION);
        JSONObject paymentDataJsonObject = transactionJsonObject.getJSONObject(JSON_PAYMENT_DATA);

        NovalnetPaymentRefInfoModel novalnetPaymentRefInfo = buildBaseRefInfo(customerNo, currentPayment, transactionJsonObject,
                paymentDataJsonObject);

        switch (currentPayment) {
            case PAYMENT_CREDIT_CARD:
                populateCreditCardRefInfo(novalnetPaymentRefInfo, paymentDataJsonObject);
                break;
            case PAYMENT_SEPA:
                populateSepaRefInfo(novalnetPaymentRefInfo, paymentDataJsonObject);
                break;
            case PAYMENT_ACH:
                populateAchRefInfo(novalnetPaymentRefInfo, paymentDataJsonObject);
                break;
            case PAYMENT_PAYPAL:
                populatePaypalRefInfo(novalnetPaymentRefInfo, paymentDataJsonObject);
                break;
            case PAYMENT_GOOGLE_PAY, PAYMENT_APPLE_PAY:
                populateWalletRefInfo(novalnetPaymentRefInfo, paymentDataJsonObject);
                break;
            default:
                break;
        }

        modelService.save(novalnetPaymentRefInfo);
    }

    private NovalnetPaymentRefInfoModel buildBaseRefInfo(String customerNo, String currentPayment,
                                                         JSONObject transactionJsonObject, JSONObject paymentDataJsonObject) {
        NovalnetPaymentRefInfoModel novalnetPaymentRefInfo = new NovalnetPaymentRefInfoModel();

        novalnetPaymentRefInfo.setCustomerNo(Long.parseLong(customerNo));
        novalnetPaymentRefInfo.setPaymentType(currentPayment);
        novalnetPaymentRefInfo.setReferenceTransaction(false);
        novalnetPaymentRefInfo.setOrginalTid(Long.parseLong(transactionJsonObject.get(JSON_TID).toString()));
        novalnetPaymentRefInfo.setToken(paymentDataJsonObject.get(JSON_TOKEN).toString());

        return novalnetPaymentRefInfo;
    }

    private void populateCreditCardRefInfo(NovalnetPaymentRefInfoModel refInfo, JSONObject paymentDataJsonObject) {
        refInfo.setCardType(paymentDataJsonObject.get(JSON_CARD_BRAND).toString());
        refInfo.setCardHolder(paymentDataJsonObject.get(JSON_CARD_HOLDER).toString());
        refInfo.setMaskedCardNumber(paymentDataJsonObject.get(JSON_CARD_NUMBER).toString());
        refInfo.setExpiryDate(formatExpiryDate(paymentDataJsonObject.get(JSON_CARD_EXPIRY_MONTH).toString(),
                paymentDataJsonObject.get(JSON_CARD_EXPIRY_YEAR).toString()));
    }

    private void populateSepaRefInfo(NovalnetPaymentRefInfoModel refInfo, JSONObject paymentDataJsonObject) {
        refInfo.setMaskedAccountIban(paymentDataJsonObject.get(JSON_IBAN).toString());
        refInfo.setAccountHolder(paymentDataJsonObject.get(JSON_ACCOUNT_HOLDER).toString());
    }

    private void populateAchRefInfo(NovalnetPaymentRefInfoModel refInfo, JSONObject paymentDataJsonObject) {
        refInfo.setAchAccountHolder(paymentDataJsonObject.get(JSON_ACCOUNT_HOLDER).toString());
        refInfo.setMaskedAchAccountNumber(paymentDataJsonObject.get(JSON_ACCOUNT_NUMBER).toString());
        refInfo.setMaskedAchRoutingNumber(paymentDataJsonObject.get(JSON_ROUTING_NUMBER).toString());
    }

    private void populatePaypalRefInfo(NovalnetPaymentRefInfoModel refInfo, JSONObject paymentDataJsonObject) {
        if (paymentDataJsonObject.has(JSON_PAYPAL_TRANSACTION_ID)) {
            refInfo.setPaypalTransactionID(paymentDataJsonObject.get(JSON_PAYPAL_TRANSACTION_ID).toString());
        }
        if (paymentDataJsonObject.has(JSON_PAYPAL_ACCOUNT)) {
            refInfo.setPaypalEmailID(paymentDataJsonObject.get(JSON_PAYPAL_ACCOUNT).toString());
        }
    }

    private void populateWalletRefInfo(NovalnetPaymentRefInfoModel refInfo, JSONObject paymentDataJsonObject) {
        if (paymentDataJsonObject.has(JSON_CARD_BRAND)) {
            refInfo.setCardType(paymentDataJsonObject.get(JSON_CARD_BRAND).toString());
        }
        if (paymentDataJsonObject.has(JSON_CARD_NUMBER)) {
            refInfo.setMaskedCardNumber(paymentDataJsonObject.get(JSON_CARD_NUMBER).toString());
        }
        if (paymentDataJsonObject.has(JSON_CARD_HOLDER)) {
            refInfo.setCardHolder(paymentDataJsonObject.get(JSON_CARD_HOLDER).toString());
        }
        if (paymentDataJsonObject.has(JSON_CARD_EXPIRY_MONTH) && paymentDataJsonObject.has(JSON_CARD_EXPIRY_YEAR)) {
            refInfo.setExpiryDate(formatExpiryDate(paymentDataJsonObject.get(JSON_CARD_EXPIRY_MONTH).toString(),
                    paymentDataJsonObject.get(JSON_CARD_EXPIRY_YEAR).toString()));
        }
    }

    private String formatExpiryDate(String month, String year) {
        String paddedMonth = month.length() == 1 ? ("0" + month) : month;
        String shortYear = year.substring(Math.max(0, year.length() - EXPIRY_YEAR_DIGITS));
        return paddedMonth + " / " + shortYear;
    }

    private void prefillRegisteredCustomerData(NovalnetPaymentDetailsForm paymentDetailsForm, Model model, AddressForm addressForm) {
        if (Boolean.FALSE.equals(novalnetCheckoutService.isGuestUser())) {
            CustomerData customerData = customerFacade.getCurrentCustomer();
            String fullName = customerData.getFirstName() + " " + customerData.getLastName();
            paymentDetailsForm.setGuaranteeAccountHolder(fullName.trim());
            paymentDetailsForm.setAccountHolder(fullName.trim());
            paymentDetailsForm.setAchAccountHolder(fullName.trim());
            model.addAttribute("customerFirstName", customerData.getFirstName().trim());
            model.addAttribute("customerLastName", customerData.getLastName().trim());
        } else {
            String fullName = addressForm.getFirstName() + " " + addressForm.getLastName();
            paymentDetailsForm.setGuaranteeAccountHolder(fullName.trim());
            paymentDetailsForm.setAccountHolder(fullName.trim());
            paymentDetailsForm.setAchAccountHolder(fullName.trim());
            model.addAttribute("customerFirstName", addressForm.getFirstName().trim());
            model.addAttribute("customerLastName", addressForm.getLastName().trim());
        }
    }

    private boolean isOneClickEligible(Boolean oneClickShoppingEnabled) {
        return Boolean.TRUE.equals(oneClickShoppingEnabled) && Boolean.FALSE.equals(novalnetCheckoutService.isGuestUser());
    }


    private void handleGooglePay(Model model) {
        NovalnetGooglePayPaymentModeModel googlePayModel = (NovalnetGooglePayPaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_GOOGLE_PAY);

        googlePayZeroAmountBooking = NovalnetUtils.isZeroAmountBooking(googlePayModel.getOnholdActionTypeWithZeroAmount());

        model.addAttribute("novalnetGooglePayZeroAmountBooking", googlePayZeroAmountBooking);

    }


    private void handleApplePay(Model model) {
        NovalnetApplePayPaymentModeModel applePayModel = (NovalnetApplePayPaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_APPLE_PAY);

        applePayZeroAmountBooking = NovalnetUtils.isZeroAmountBooking(applePayModel.getOnholdActionTypeWithZeroAmount());

        model.addAttribute("novalnetApplePayZeroAmountBooking", applePayZeroAmountBooking);
    }


    private void handleSepa(NovalnetPaymentDetailsForm form, Model model) {
        NovalnetDirectDebitSepaPaymentModeModel sepaMethod = (NovalnetDirectDebitSepaPaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_SEPA);

        sepaZeroAmountBooking = NovalnetUtils.isZeroAmountBooking(sepaMethod.getOnholdActionTypeWithZeroAmount());

        model.addAttribute("novalnetDirectDebitSepaZeroAmountBooking", sepaZeroAmountBooking);

        if (Boolean.TRUE.equals(sepaMethod.getActive())) {
            boolean oneClickCondition = isOneClickEligible(sepaMethod.getNovalnetOneClickShopping());

            showOneClickShopping(PAYMENT_SEPA, oneClickCondition, form, model);
        }
    }


    private void handleAch(NovalnetPaymentDetailsForm form, Model model) {
        NovalnetDirectDebitAchPaymentModeModel achMethod = (NovalnetDirectDebitAchPaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_ACH);

        achZeroAmountBooking = NovalnetUtils.isZeroAmountBooking(achMethod.getOnholdActionTypeWithoutAuthorize());

        model.addAttribute("novalnetDirectDebitAchZeroAmountBooking", achZeroAmountBooking);

        if (Boolean.TRUE.equals(achMethod.getActive())) {
            boolean oneClickCondition = isOneClickEligible(achMethod.getNovalnetOneClickShopping());

            showOneClickShopping(PAYMENT_ACH, oneClickCondition, form, model);
        }
    }


    private void handleGuaranteedSepa(NovalnetPaymentDetailsForm form, Model model) {
        NovalnetGuaranteedDirectDebitSepaPaymentModeModel method = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_GUARANTEED_SEPA);

        if (!Boolean.TRUE.equals(method.getActive())) {
            return;
        }

        boolean oneClickCondition = isOneClickEligible(method.getNovalnetOneClickShopping());

        showOneClickShopping(PAYMENT_GUARANTEED_SEPA, oneClickCondition, form, model);

        Integer minAmount = method.getNovalnetMinimumGuaranteeAmount() != null ? method.getNovalnetMinimumGuaranteeAmount()
                : GUARANTEED_MIN_AMOUNT;

        model.addAttribute("novalnetGuaranteedDirectDebitSepaMinAmount", minAmount);
    }


    private void handleGuaranteedInvoice(Model model) {
        NovalnetGuaranteedInvoicePaymentModeModel method = (NovalnetGuaranteedInvoicePaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_GUARANTEED_INVOICE);

        if (!Boolean.TRUE.equals(method.getActive())) {
            return;
        }

        Integer minAmount = method.getNovalnetMinimumGuaranteeAmount() != null ? method.getNovalnetMinimumGuaranteeAmount()
                : GUARANTEED_MIN_AMOUNT;

        model.addAttribute("novalnetGuaranteedInvoiceMinAmount", minAmount);
    }


    private void handleCreditCard(NovalnetPaymentDetailsForm form, Model model) {
        NovalnetCreditCardPaymentModeModel method = (NovalnetCreditCardPaymentModeModel) paymentModeService
                .getPaymentModeForCode(PAYMENT_CREDIT_CARD);

        creditCardZeroAmountBooking = NovalnetUtils.isZeroAmountBooking(method.getOnholdActionTypeWithZeroAmount());

        model.addAttribute("novalnetCreditCardZeroAmountBooking", creditCardZeroAmountBooking);

        if (Boolean.TRUE.equals(method.getActive())) {
            boolean oneClickCondition = isOneClickEligible(method.getNovalnetOneClickShopping());

            showOneClickShopping(PAYMENT_CREDIT_CARD, oneClickCondition, form, model);
        }
    }


    private void showOneClickShopping(String paymentName, boolean novalnetOneClickCondition,
                                      NovalnetPaymentDetailsForm novalnetPaymentDetailsForm, Model model) {
        model.addAttribute(paymentName + ONE_CLICK, false);

        model.addAttribute(paymentName + ONE_CLICK + "Enabled", false);

        if (!novalnetOneClickCondition) {
            return;
        }

        model.addAttribute(paymentName + ONE_CLICK + "Enabled", true);

        String customerNo = JaloSession.getCurrentSession().getUser().getPK().toString();

        String payment = PAYMENT_GUARANTEED_SEPA.equals(paymentName) ? PAYMENT_SEPA : paymentName;

        List<NovalnetPaymentRefInfoModel> paymentInfo = novalnetDao.getPaymentRefInfo(customerNo, payment);

        if (paymentInfo == null || paymentInfo.isEmpty()) {
            return;
        }

        switch (paymentName) {
            case PAYMENT_CREDIT_CARD:
                populateCreditCardOneClick(paymentInfo, model);
                model.addAttribute(paymentName + ONE_CLICK, true);
                break;

            case PAYMENT_SEPA, PAYMENT_GUARANTEED_SEPA:
                populateSepaOneClick(paymentInfo, novalnetPaymentDetailsForm, model);

                model.addAttribute(paymentName + ONE_CLICK, true);
                break;

            case PAYMENT_ACH:
                populateAchOneClick(paymentInfo, model);

                model.addAttribute(paymentName + ONE_CLICK, true);
                break;

            default:
                return;
        }

        sessionService.setAttribute(paymentName + "ReferenceTid", paymentInfo.get(0).getOrginalTid().toString());

        if (paymentInfo.get(0).getToken() != null) {
            sessionService.setAttribute(paymentName + "ReferenceToken", paymentInfo.get(0).getToken());
        }
    }


    private void populateCreditCardOneClick(List<NovalnetPaymentRefInfoModel> paymentInfo, Model model) {
        for (int index = 0; index < Math.min(paymentInfo.size(), MAX_ONE_CLICK_PAYMENT_RECORDS); index++) {
            NovalnetPaymentRefInfoModel card = paymentInfo.get(index);

            String suffix = index == 0 ? "" : "2";

            model.addAttribute(PAYMENT_CREDIT_CARD + ONE_CLICK + "CardType" + suffix,
                    card.getCardType() != null ? card.getCardType().toLowerCase() : "");

            model.addAttribute(PAYMENT_CREDIT_CARD + ONE_CLICK + "CardHolder" + suffix, card.getCardHolder());

            if (card.getMaskedCardNumber() != null) {
                String maskedCardNumber = card.getMaskedCardNumber();

                model.addAttribute(PAYMENT_CREDIT_CARD + ONE_CLICK + "MaskedCardNumber" + suffix,
                        maskedCardNumber.substring(Math.max(0, maskedCardNumber.length() - MASKED_CARD_LAST_DIGITS)));
            }

            model.addAttribute(PAYMENT_CREDIT_CARD + ONE_CLICK + "CardExpiry" + suffix, card.getExpiryDate());

            model.addAttribute(PAYMENT_CREDIT_CARD + ONE_CLICK + TOKEN + (index + 1), card.getToken());

            sessionService.setAttribute(PAYMENT_CREDIT_CARD + ONE_CLICK + TOKEN + (index + 1), card.getToken());
        }
    }


    private void populateSepaOneClick(List<NovalnetPaymentRefInfoModel> paymentInfo, NovalnetPaymentDetailsForm form, Model model) {
        for (int index = 0; index < Math.min(paymentInfo.size(), MAX_ONE_CLICK_PAYMENT_RECORDS); index++) {
            NovalnetPaymentRefInfoModel account = paymentInfo.get(index);

            String suffix = index == 0 ? "" : "2";

            if (index == 0) {
                form.setNovalnetDirectDebitSepaOneClickAccountHolder(account.getAccountHolder());

                form.setNovalnetDirectDebitSepaOneClickMaskedAccountIban(account.getMaskedAccountIban());
            }

            model.addAttribute(PAYMENT_SEPA + "AccountHolder" + suffix, account.getAccountHolder());

            model.addAttribute(PAYMENT_SEPA + "AccountIban" + suffix, account.getMaskedAccountIban());

            model.addAttribute(PAYMENT_SEPA + ONE_CLICK + TOKEN + (index + 1), account.getToken());

            sessionService.setAttribute(PAYMENT_SEPA + ONE_CLICK + TOKEN + (index + 1), account.getToken());
        }
    }


    private void populateAchOneClick(List<NovalnetPaymentRefInfoModel> paymentInfo, Model model) {
        for (int index = 0; index < Math.min(paymentInfo.size(), MAX_ONE_CLICK_PAYMENT_RECORDS); index++) {
            NovalnetPaymentRefInfoModel account = paymentInfo.get(index);
            String suffix = index == 0 ? "" : "2";

            model.addAttribute("novalnetDirectDebitAchAccountHolder" + suffix, account.getAchAccountHolder());

            model.addAttribute("novalnetDirectDebitAchAccountNumber" + suffix, account.getMaskedAchAccountNumber());

            model.addAttribute("novalnetDirectDebitAchRoutingNumber" + suffix, account.getMaskedAchRoutingNumber());

            model.addAttribute(PAYMENT_ACH + "AccountHolder", account.getAchAccountHolder());

            model.addAttribute(PAYMENT_ACH + ONE_CLICK + TOKEN + (index + 1), account.getToken());

            sessionService.setAttribute(PAYMENT_ACH + ONE_CLICK + TOKEN + (index + 1), account.getToken());
        }

    }


    private AddressForm getAddressForm(CartData cartData, Model model) {
        AddressForm addressForm = new AddressForm();

        if (existBillingAddressInCartData(cartData)) {
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


    private boolean existBillingAddressInCartData(CartData cartData) {
        return cartData.getPaymentInfo() != null && cartData.getPaymentInfo().getBillingAddress() != null;
    }


    private void populatePaymentModes(Model model) {
        for (String code : NOVALNET_PAYMENT_CODES) {
            model.addAttribute(code, paymentModeService.getPaymentModeForCode(code));
        }
    }


    private void addLocalizedLabels(Model model) {
        model.addAttribute("endswith", Localization.getLocalizedString("novalnet.endswith"));

        model.addAttribute("expires", Localization.getLocalizedString("novalnet.expires"));

        model.addAttribute("creditcardAddNew", Localization.getLocalizedString("novalnet.creditcardaddnew"));

        model.addAttribute("sepaAddNew", Localization.getLocalizedString("novalnet.sepaaddnew"));

        model.addAttribute("achaddnew", Localization.getLocalizedString("novalnet.achaddnew"));
    }

    /**
     * Stores the customer's payment reference data when the customer is a registered user and has opted to save the payment details.
     *
     * @param currentPayment     the currently selected payment method
     * @param response           the transaction response containing payment information
     * @param customerJsonObject the customer data containing the customer number
     */
    public void handleStorePayment(String currentPayment, String response, JsonNode customerJsonObject) {
        if (PAYMENT_CREDIT_CARD.equals(currentPayment) && !novalnetCheckoutService.isGuestUser()) {
            boolean novalnetCreditCardStorePaymentData = sessionService.getAttribute("novalnetCreditCardStorePaymentData");

            if (novalnetCreditCardStorePaymentData) {
                handleReferenceTransactionInfo(response, customerJsonObject.path(JSON_CUSTOMER_NO).asText(), PAYMENT_CREDIT_CARD);
            }
        }
    }


    @Override
    public PaymentTransactionEntryModel createTransactionEntry(String requestId, CartModel cartModel, int amount,
                                                               String backendTransactionComments, String currencyCode) {
        PaymentTransactionEntryModel paymentTransactionEntry = modelService.create(PaymentTransactionEntryModel.class);
        paymentTransactionEntry.setRequestId(requestId);
        paymentTransactionEntry.setType(PaymentTransactionType.AUTHORIZATION);
        paymentTransactionEntry.setTransactionStatus(TransactionStatus.ACCEPTED.name());
        paymentTransactionEntry.setTransactionStatusDetails(backendTransactionComments);
        paymentTransactionEntry.setCode(cartModel.getCode());

        CurrencyModel currency = novalnetDao.getCurrencyForIsoCode(currencyCode);
        paymentTransactionEntry.setCurrency(currency);

        final BigDecimal transactionAmount = BigDecimal.valueOf(amount).movePointLeft(2);
        paymentTransactionEntry.setAmount(transactionAmount);
        paymentTransactionEntry.setTime(Timestamp.from(Instant.now()));
        return paymentTransactionEntry;
    }


    @Override
    public void addPaymentProcess(Model model, NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData)
            throws CMSItemNotFoundException {

        AddressForm addressForm = getAddressForm(cartData, model);

        paymentDetailsForm.setBillingAddress(addressForm);

        model.addAttribute(CART_DATA_ATTR, cartData);

        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());

        BaseStoreModel baseStore = getBaseStoreModel();

        model.addAttribute("novalnetBaseStoreConfiguration", baseStore);

        Locale language = JaloSession.getCurrentSession().getSessionContext().getLocale();

        populatePaymentModes(model);
        addLocalizedLabels(model);

        String languageCode = (language != null) ? language.toString().toUpperCase(Locale.ROOT) : "EN";

        model.addAttribute("lang", languageCode);

        String totalAmount = NovalnetUtils.formatAmount(String.valueOf(cartData.getTotalPriceWithTax().getValue()));

        BigDecimal orderAmount = new BigDecimal(totalAmount);

        model.addAttribute("orderAmount", orderAmount);

        BigDecimal orderAmountCents = orderAmount.multiply(BigDecimal.valueOf(CONVERT_TO_CENT)).setScale(0, RoundingMode.HALF_UP);

        Integer orderAmountCent = orderAmountCents.intValue();

        model.addAttribute("orderAmountCent", orderAmountCent);

        String currency = cartData.getTotalPriceWithTax().getCurrencyIso();

        model.addAttribute("currency", currency);

        String guestEmail = novalnetCheckoutService.getUserEmail();

        String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();

        model.addAttribute("email", emailAddress);

        prefillRegisteredCustomerData(paymentDetailsForm, model, addressForm);

        handleGooglePay(model);
        handleApplePay(model);
        handleSepa(paymentDetailsForm, model);
        handleAch(paymentDetailsForm, model);
        handleGuaranteedSepa(paymentDetailsForm, model);
        handleGuaranteedInvoice(model);
        handleCreditCard(paymentDetailsForm, model);
    }


    @Override
    public boolean removeSavedPaymentData(final String paymentType, final String cardIndex) {
        if (StringUtils.isBlank(paymentType) || !("1".equals(cardIndex) || "2".equals(cardIndex))) {
            return false;
        }
        final String payment = PAYMENT_GUARANTEED_SEPA.equals(paymentType) ? PAYMENT_SEPA : paymentType;

        final String sessionKey = payment + ONE_CLICK + TOKEN + cardIndex;
        final String token = sessionService.getAttribute(sessionKey);
        if (StringUtils.isBlank(token)) {
            return false;
        }

        final String customerNo = JaloSession.getCurrentSession().getUser().getPK().toString();
        for (final NovalnetPaymentRefInfoModel info : novalnetDao.getPaymentRefInfo(customerNo, payment)) {
            if (token.equals(info.getToken())) {
                modelService.remove(info);
                sessionService.removeAttribute(sessionKey);
                return true;
            }
        }
        return false;
    }

}

