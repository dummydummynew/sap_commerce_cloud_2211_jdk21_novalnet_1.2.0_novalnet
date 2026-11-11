/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.model.NovalnetApplePayPaymentModeModel;
import com.novalnet.model.NovalnetCreditCardPaymentModeModel;
import com.novalnet.model.NovalnetDirectDebitAchPaymentModeModel;
import com.novalnet.model.NovalnetDirectDebitSepaPaymentModeModel;
import com.novalnet.model.NovalnetGooglePayPaymentModeModel;
import com.novalnet.model.NovalnetGuaranteedDirectDebitSepaPaymentModeModel;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;


/**
 * Handles one-click payment processing for supported Novalnet payment methods.
 */
public class DefaultNovalnetOneClickPaymentService {
    private static final String NOVALNET_ACCOUNT_HOLDER_REQUIRED = "novalnet.account.holder.required";
    private static final String NOVALNET_CREDIT_CARD = "novalnetCreditCard";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA = "novalnetDirectDebitSepa";
    private static final String NOVALNET_DIRECT_DEBIT_ACH = "novalnetDirectDebitAch";
    private static final String NOVALNET_GUARANTEED_INVOICE = "novalnetGuaranteedInvoice";
    private static final String NOVALNET_DIRECT_DEBIT_ACH_STORE_PAYMENT_DATA = "novalnetDirectDebitAchStorePaymentData";
    private static final String NOVALNET_CHECKOUT_ERROR = "novalnetCheckoutError";
    private static final String NOVALNET_GUARANTEED_SEPA_STORE_PAYMENT_DATA = "novalnetGuaranteedDirectDebitSepaStorePaymentData";
    private static final String NOVALNET_DIRECT_DEBIT_ACH_ROUTING_NUMBER = "novalnetDirectDebitAchRoutingNumber";
    private static final String NOVALNET_CREDIT_CARD_STORE_PAYMENT_DATA = "novalnetCreditCardStorePaymentData";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_IBAN = "novalnetDirectDebitSepaAccountIban";
    private static final String NOVALNET_IBAN_REQUIRED = "novalnet.iban.required";
    private static final String NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA = "novalnetGuaranteedDirectDebitSepa";
    private static final String NOVALNET_GUARANTEED_SEPA_ACCOUNT_IBAN = "novalnetGuaranteedDirectDebitSepaAccountIban";
    private static final String NOVALNET_GUARANTEED_SEPA_ACCOUNT_HOLDER = "novalnetGuaranteedDirectDebitSepaAccountHolder";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_HOLDER = "novalnetDirectDebitSepaAccountHolder";
    private static final String NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_NUMBER = "novalnetDirectDebitAchAchAccountNumber";
    private static final String NOVALNET_ROUTING_NUMBER_REQUIRED = "novalnet.routing.number.required";
    private static final String NOVALNET_GOOGLE_PAY = "novalnetGooglePay";
    private static final String NOVALNET_ACCOUNT_NUMBER_REQUIRED = "novalnet.account.number.required";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_BIC = "novalnetDirectDebitSepaAccountBic";
    private static final String NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_HOLDER = "novalnetDirectDebitAchAccountHolder";
    private static final String NOVALNET_APPLE_PAY = "novalnetApplePay";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA_TOKEN = "novalnetDirectDebitSepatoken";
    private static final String NOVALNET_DIRECT_DEBIT_SEPA_STORE_PAYMENT_DATA = "novalnetDirectDebitSepaStorePaymentData";
    private static final String NOVALNET_GUARANTEED_SEPA_ACCOUNT_BIC = "novalnetGuaranteedDirectDebitSepaAccountBic";
    private static final String PAYMENT_DETAILS_FORM_NULL = "paymentDetailsForm was null";
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNovalnetOneClickPaymentService.class);

    @Resource
    private SessionService sessionService;

    @Resource
    private NovalnetCheckoutService novalnetCheckoutService;

    @Resource
    private PaymentModeService paymentModeService;

    @Resource
    private NovalnetPaymentService novalnetPaymentService;

    @Resource
    private DefaultNovalnetGuaranteeValidationService novalnetGuaranteeValidationService;

    /**
     * Processes one-click payment data for the selected Novalnet payment method.
     *
     * @param currentPayment     the currently selected payment method
     * @param paymentDetailsForm the payment details submitted during checkout
     * @param model              the model used to store checkout data
     * @param cartData           the current cart data
     * @param deliveryAddress    the delivery address associated with the checkout
     * @return {@code true} if the payment data is processed successfully; otherwise, {@code false}
     * @throws CMSItemNotFoundException if a required CMS item cannot be found
     */
    public boolean processOneClickTokenData(String currentPayment, NovalnetPaymentDetailsForm paymentDetailsForm, Model model,
                                            CartData cartData, AddressData deliveryAddress) throws CMSItemNotFoundException {
        if (currentPayment == null) {
            return false;
        }
        switch (currentPayment) {
            case NOVALNET_DIRECT_DEBIT_SEPA:
                return handleSepa(paymentDetailsForm);
            case NOVALNET_DIRECT_DEBIT_ACH:
                return handleAch(paymentDetailsForm);
            case NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA:
                return handleGuaranteedSepa(paymentDetailsForm, model, cartData, deliveryAddress);
            case NOVALNET_GUARANTEED_INVOICE:
                return handleGuaranteedInvoice(paymentDetailsForm, model, cartData, deliveryAddress);
            case NOVALNET_CREDIT_CARD:
                return handleCreditCard(paymentDetailsForm, model, cartData);
            case NOVALNET_GOOGLE_PAY:
                return handleGooglePay(currentPayment);
            case NOVALNET_APPLE_PAY:
                return handleApplePay(currentPayment);
            default:
                return true;
        }
    }

    /**
     * Handles one-click payment data for Google Pay.
     *
     * @param currentPayment the selected Google Pay payment method
     * @return {@code true} if the Google Pay payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleGooglePay(String currentPayment) {
        if (currentPayment == null) {
            return false;
        }
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
        if (paymentModeModel == null) {
            return false;
        }
        NovalnetGooglePayPaymentModeModel googlePayPaymentMethod = (NovalnetGooglePayPaymentModeModel) paymentModeModel;
        boolean zeroAmountBooking = NovalnetUtils.isZeroAmountBooking(googlePayPaymentMethod.getOnholdActionTypeWithZeroAmount());
        sessionService.setAttribute("novalnetGooglePayStorePaymentData", zeroAmountBooking);
        return true;
    }

    /**
     * Handles one-click payment data for Apple Pay.
     *
     * @param currentPayment the selected Apple Pay payment method
     * @return {@code true} if the Apple Pay payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleApplePay(String currentPayment) {
        if (currentPayment == null) {
            return false;
        }
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
        if (paymentModeModel == null) {
            return false;
        }
        NovalnetApplePayPaymentModeModel applePayPaymentMethod = (NovalnetApplePayPaymentModeModel) paymentModeModel;
        boolean zeroAmountBooking = NovalnetUtils.isZeroAmountBooking(applePayPaymentMethod.getOnholdActionTypeWithZeroAmount());
        sessionService.setAttribute("novalnetApplePayStorePaymentData", zeroAmountBooking);
        return true;
    }

    /**
     * Validates a payment value and stores it in the current session.
     *
     * @param key             the session attribute key
     * @param value           the payment value to validate and store
     * @param errorMessageKey the localization key used when the value is missing
     * @return {@code true} if the value is present and stored; otherwise, {@code false}
     */
    private boolean requireAndStore(String key, String value, String errorMessageKey) {
        if (value != null && !"".equals(value.trim())) {
            sessionService.setAttribute(key, value.trim());
            return true;
        }
        sessionService.setAttribute(NOVALNET_CHECKOUT_ERROR, Localization.getLocalizedString(errorMessageKey));
        return false;
    }

    /**
     * Stores a payment value in the current session when the value is present.
     *
     * @param key   the session attribute key
     * @param value the payment value to store
     */
    private void storeIfPresent(String key, String value) {
        if (value != null && !"".equals(value.trim())) {
            sessionService.setAttribute(key, value.trim());
        }
    }

    /**
     * Handles SEPA direct debit payment data and one-click payment selection.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if the SEPA payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleSepa(NovalnetPaymentDetailsForm paymentDetailsForm) {
        PaymentModeModel paymentNovalnetDirectDebitSepaModeModel = paymentModeService
                .getPaymentModeForCode(NOVALNET_DIRECT_DEBIT_SEPA);
        NovalnetDirectDebitSepaPaymentModeModel novalnetDirectDebitSepaPaymentMethod = (NovalnetDirectDebitSepaPaymentModeModel) paymentNovalnetDirectDebitSepaModeModel;
        sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_STORE_PAYMENT_DATA, false);
        if (Boolean.TRUE.equals(novalnetCheckoutService.isGuestUser()) && !validateGuestSepaFields(paymentDetailsForm)) {
            return false;
        }
        boolean oneClickEligible = Boolean.TRUE.equals(novalnetDirectDebitSepaPaymentMethod.getNovalnetOneClickShopping())
                && Boolean.FALSE.equals(novalnetCheckoutService.isGuestUser());
        if (oneClickEligible) {
            return handleSepaOneClick(paymentDetailsForm);
        }
        storeIfPresent(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_IBAN, paymentDetailsForm.getAccountIban());
        storeIfPresent(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_HOLDER, paymentDetailsForm.getAccountHolder());
        storeIfPresent(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_BIC, paymentDetailsForm.getAccountBic());
        return true;
    }

    /**
     * Validates and stores the required SEPA payment fields for a guest customer.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if all required SEPA fields are valid; otherwise, {@code false}
     */
    private boolean validateGuestSepaFields(NovalnetPaymentDetailsForm paymentDetailsForm) {
        if (StringUtils.isBlank(paymentDetailsForm.getAccountIban())) {
            sessionService.setAttribute(NOVALNET_CHECKOUT_ERROR, Localization.getLocalizedString(NOVALNET_IBAN_REQUIRED));
            return false;
        }
        if (StringUtils.isBlank(paymentDetailsForm.getAccountHolder())) {
            sessionService.setAttribute(NOVALNET_CHECKOUT_ERROR, Localization.getLocalizedString(NOVALNET_ACCOUNT_HOLDER_REQUIRED));
            return false;
        }
        sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_IBAN, paymentDetailsForm.getAccountIban().trim());
        sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_HOLDER, paymentDetailsForm.getAccountHolder().trim());
        storeIfPresent(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_BIC, paymentDetailsForm.getAccountBic());
        return true;
    }

    /**
     * Processes the selected SEPA one-click payment option.
     *
     * @param paymentDetailsForm the payment details containing the one-click selection
     * @return {@code true} if the one-click payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleSepaOneClick(NovalnetPaymentDetailsForm paymentDetailsForm) {
        String oneClickData = paymentDetailsForm != null ? paymentDetailsForm.getDirectDebitSepaOneClickData1() : null;
        if (oneClickData == null) {
            oneClickData = "";
        }
        if (paymentDetailsForm == null) {
            LOGGER.info(PAYMENT_DETAILS_FORM_NULL);
        }
        if ("".equals(oneClickData)) {
            return storeSepaManualEntry(paymentDetailsForm);
        }
        String selection = paymentDetailsForm.getDirectDebitSepaOneClickData1().trim();
        if ("3".equals(selection)) {
            return storeSepaManualEntry(paymentDetailsForm);
        }
        if ("1".equals(selection)) {
            sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_TOKEN,
                    sessionService.getAttribute("novalnetDirectDebitSepaOneClickToken1"));
        }
        if ("2".equals(selection)) {
            sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_TOKEN,
                    sessionService.getAttribute("novalnetDirectDebitSepaOneClickToken2"));
        }
        return true;
    }

    /**
     * Stores manually entered SEPA payment details in the current session.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if all required SEPA details are stored successfully; otherwise, {@code false}
     */
    private boolean storeSepaManualEntry(NovalnetPaymentDetailsForm paymentDetailsForm) {
        if (Boolean.TRUE.equals(paymentDetailsForm.isDirectDebitSepaSaveData())) {
            sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_STORE_PAYMENT_DATA, true);
        }
        storeIfPresent(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_BIC, paymentDetailsForm.getAccountBic());
        if (!requireAndStore(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_IBAN, paymentDetailsForm.getAccountIban(), NOVALNET_IBAN_REQUIRED)) {
            return false;
        }
        return requireAndStore(NOVALNET_DIRECT_DEBIT_SEPA_ACCOUNT_HOLDER, paymentDetailsForm.getAccountHolder(),
                NOVALNET_ACCOUNT_HOLDER_REQUIRED);
    }

    /**
     * Handles ACH direct debit payment data and one-click payment selection.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if the ACH payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleAch(NovalnetPaymentDetailsForm paymentDetailsForm) {
        PaymentModeModel paymentNovalnetDirectDebitAchModeModel = paymentModeService
                .getPaymentModeForCode(NOVALNET_DIRECT_DEBIT_ACH);
        NovalnetDirectDebitAchPaymentModeModel novalnetDirectDebitAchPaymentMethod = (NovalnetDirectDebitAchPaymentModeModel) paymentNovalnetDirectDebitAchModeModel;
        sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_ACH_STORE_PAYMENT_DATA, false);
        if (Boolean.TRUE.equals(novalnetCheckoutService.isGuestUser()) && !validateGuestAchFields(paymentDetailsForm)) {
            return false;
        }
        boolean oneClickEligible = Boolean.TRUE.equals(novalnetDirectDebitAchPaymentMethod.getNovalnetOneClickShopping())
                && Boolean.FALSE.equals(novalnetCheckoutService.isGuestUser());
        if (oneClickEligible) {
            return handleAchOneClick(paymentDetailsForm);
        }
        storeIfPresent(NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_HOLDER, paymentDetailsForm.getAchAccountHolder());
        storeIfPresent(NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_NUMBER, paymentDetailsForm.getAchAccountNumber());
        storeIfPresent(NOVALNET_DIRECT_DEBIT_ACH_ROUTING_NUMBER, paymentDetailsForm.getAchRoutingNumber());
        return true;
    }

    /**
     * Validates and stores the required ACH payment fields for a guest customer.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if all required ACH fields are valid; otherwise, {@code false}
     */
    private boolean validateGuestAchFields(NovalnetPaymentDetailsForm paymentDetailsForm) {
        if (!requireAndStore(NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_HOLDER, paymentDetailsForm.getAchAccountHolder(),
                NOVALNET_ACCOUNT_HOLDER_REQUIRED)) {
            return false;
        }
        if (!requireAndStore(NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_NUMBER, paymentDetailsForm.getAchAccountNumber(),
                NOVALNET_ACCOUNT_NUMBER_REQUIRED)) {
            return false;
        }
        return requireAndStore(NOVALNET_DIRECT_DEBIT_ACH_ROUTING_NUMBER, paymentDetailsForm.getAchRoutingNumber(),
                NOVALNET_ROUTING_NUMBER_REQUIRED);
    }

    /**
     * Processes the selected ACH one-click payment option.
     *
     * @param paymentDetailsForm the payment details containing the one-click selection
     * @return {@code true} if the one-click payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleAchOneClick(NovalnetPaymentDetailsForm paymentDetailsForm) {
        String oneClickData = paymentDetailsForm != null ? paymentDetailsForm.getDirectDebitAchOneClickData1() : null;
        if (oneClickData == null) {
            oneClickData = "";
        }
        if (paymentDetailsForm == null) {
            LOGGER.info(PAYMENT_DETAILS_FORM_NULL);
        }
        if ("".equals(oneClickData)) {
            return storeAchManualEntry(paymentDetailsForm);
        }
        String selection = paymentDetailsForm.getDirectDebitAchOneClickData1().trim();
        if ("3".equals(selection)) {
            return storeAchManualEntry(paymentDetailsForm);
        }
        if ("1".equals(selection)) {
            sessionService.setAttribute("novalnetDirectDebitAchtoken",
                    sessionService.getAttribute("novalnetDirectDebitAchOneClickToken1"));
        }
        if ("2".equals(selection)) {
            sessionService.setAttribute("novalnetDirectDebitAchtoken",
                    sessionService.getAttribute("novalnetDirectDebitAchOneClickToken2"));
        }
        return true;
    }

    /**
     * Stores manually entered ACH payment details in the current session.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if all required ACH details are stored successfully; otherwise, {@code false}
     */
    private boolean storeAchManualEntry(NovalnetPaymentDetailsForm paymentDetailsForm) {
        if (Boolean.TRUE.equals(paymentDetailsForm.isDirectDebitAchSaveData())) {
            sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_ACH_STORE_PAYMENT_DATA, true);
        }
        if (!requireAndStore(NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_HOLDER, paymentDetailsForm.getAchAccountHolder(),
                NOVALNET_ACCOUNT_HOLDER_REQUIRED)) {
            return false;
        }
        if (!requireAndStore(NOVALNET_DIRECT_DEBIT_ACH_ACCOUNT_NUMBER, paymentDetailsForm.getAchAccountNumber(),
                NOVALNET_ACCOUNT_NUMBER_REQUIRED)) {
            return false;
        }
        return requireAndStore(NOVALNET_DIRECT_DEBIT_ACH_ROUTING_NUMBER, paymentDetailsForm.getAchRoutingNumber(),
                NOVALNET_ROUTING_NUMBER_REQUIRED);
    }

    /**
     * Handles guaranteed SEPA direct debit payment data and performs guarantee validation.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @param model              the model used to store checkout data
     * @param cartData           the current cart data
     * @param deliveryAddress    the delivery address associated with the checkout
     * @return {@code true} if the guaranteed SEPA payment data is processed successfully; otherwise, {@code false}
     * @throws CMSItemNotFoundException if a required CMS item cannot be found
     */
    private boolean handleGuaranteedSepa(NovalnetPaymentDetailsForm paymentDetailsForm, Model model, CartData cartData,
                                         AddressData deliveryAddress) throws CMSItemNotFoundException {
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA);
        NovalnetGuaranteedDirectDebitSepaPaymentModeModel novalnetPaymentMethod = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) paymentModeModel;
        sessionService.setAttribute(NOVALNET_GUARANTEED_SEPA_STORE_PAYMENT_DATA, false);
        if (Boolean.TRUE.equals(novalnetCheckoutService.isGuestUser()) && !validateGuestGuaranteedSepaFields(paymentDetailsForm)) {
            return false;
        }
        boolean oneClickEligible = Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping())
                && Boolean.FALSE.equals(novalnetCheckoutService.isGuestUser());
        if (oneClickEligible && !handleGuaranteedSepaOneClick(paymentDetailsForm)) {
            return false;
        }
        sessionService.setAttribute(NOVALNET_GUARANTEED_SEPA_ACCOUNT_IBAN, paymentDetailsForm.getGuaranteeAccountIban().trim());
        sessionService.setAttribute(NOVALNET_GUARANTEED_SEPA_ACCOUNT_HOLDER, paymentDetailsForm.getGuaranteeAccountHolder().trim());
        sessionService.setAttribute(NOVALNET_GUARANTEED_SEPA_ACCOUNT_BIC, paymentDetailsForm.getGuaranteeAccountBic().trim());
        String novalnetDirectDebitSepaGuaranteeError = novalnetGuaranteeValidationService.handleGuaranteeProcess(
                NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA, paymentDetailsForm.getNovalnetGuaranteedDirectDebitSepaDateOfBirth(),
                paymentDetailsForm, deliveryAddress);
        if (!"".equals(novalnetDirectDebitSepaGuaranteeError)) {
            novalnetPaymentService.addPaymentProcess(model, paymentDetailsForm, cartData);
        }
        return true;
    }

    /**
     * Validates and stores the required guaranteed SEPA payment fields for a guest customer.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if all required guaranteed SEPA fields are valid; otherwise, {@code false}
     */
    private boolean validateGuestGuaranteedSepaFields(NovalnetPaymentDetailsForm paymentDetailsForm) {
        if (!requireAndStore(NOVALNET_GUARANTEED_SEPA_ACCOUNT_IBAN, paymentDetailsForm.getGuaranteeAccountIban(),
                NOVALNET_IBAN_REQUIRED)) {
            return false;
        }
        if (!requireAndStore(NOVALNET_GUARANTEED_SEPA_ACCOUNT_HOLDER, paymentDetailsForm.getGuaranteeAccountHolder(),
                NOVALNET_ACCOUNT_HOLDER_REQUIRED)) {
            return false;
        }
        storeIfPresent(NOVALNET_GUARANTEED_SEPA_ACCOUNT_BIC, paymentDetailsForm.getGuaranteeAccountBic());
        return true;
    }

    /**
     * Processes the selected guaranteed SEPA one-click payment option.
     *
     * @param paymentDetailsForm the payment details containing the one-click selection
     * @return {@code true} if the one-click payment data is processed successfully; otherwise, {@code false}
     */
    private boolean handleGuaranteedSepaOneClick(NovalnetPaymentDetailsForm paymentDetailsForm) {
        String oneClickData = paymentDetailsForm != null ? paymentDetailsForm.getGuaranteedDirectDebitSepaOneClickData1() : null;
        if (oneClickData == null) {
            oneClickData = "";
        }
        if (paymentDetailsForm == null) {
            LOGGER.info(PAYMENT_DETAILS_FORM_NULL);
        }
        if ("".equals(oneClickData)) {
            return storeGuaranteedSepaManualEntry(paymentDetailsForm);
        }
        String selection = paymentDetailsForm.getGuaranteedDirectDebitSepaOneClickData1().trim();
        if ("3".equals(selection)) {
            return storeGuaranteedSepaManualEntry(paymentDetailsForm);
        }
        if ("1".equals(selection)) {
            sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_TOKEN,
                    sessionService.getAttribute("novalnetDirectDebitSepaOneClickToken1"));
        }
        if ("2".equals(selection)) {
            sessionService.setAttribute(NOVALNET_DIRECT_DEBIT_SEPA_TOKEN,
                    sessionService.getAttribute("novalnetDirectDebitSepaOneClickToken2"));
        }
        return true;
    }

    /**
     * Stores manually entered guaranteed SEPA payment details in the current session.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @return {@code true} if all required guaranteed SEPA details are stored successfully; otherwise, {@code false}
     */
    private boolean storeGuaranteedSepaManualEntry(NovalnetPaymentDetailsForm paymentDetailsForm) {
        if (Boolean.TRUE.equals(paymentDetailsForm.isGuaranteedDirectDebitSepaSaveData())) {
            sessionService.setAttribute(NOVALNET_GUARANTEED_SEPA_STORE_PAYMENT_DATA, true);
        }
        if (!requireAndStore(NOVALNET_GUARANTEED_SEPA_ACCOUNT_IBAN, paymentDetailsForm.getGuaranteeAccountIban(),
                NOVALNET_IBAN_REQUIRED)) {
            return false;
        }
        if (!requireAndStore(NOVALNET_GUARANTEED_SEPA_ACCOUNT_HOLDER, paymentDetailsForm.getGuaranteeAccountHolder(),
                NOVALNET_ACCOUNT_HOLDER_REQUIRED)) {
            return false;
        }
        storeIfPresent(NOVALNET_GUARANTEED_SEPA_ACCOUNT_BIC, paymentDetailsForm.getGuaranteeAccountBic());
        return true;
    }

    /**
     * Handles guaranteed invoice payment data and performs guarantee validation.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @param model              the model used to store checkout data
     * @param cartData           the current cart data
     * @param deliveryAddress    the delivery address associated with the checkout
     * @return {@code true} if the guaranteed invoice payment data is processed successfully; otherwise, {@code false}
     * @throws CMSItemNotFoundException if a required CMS item cannot be found
     */
    private boolean handleGuaranteedInvoice(NovalnetPaymentDetailsForm paymentDetailsForm, Model model, CartData cartData,
                                            AddressData deliveryAddress) throws CMSItemNotFoundException {
        String novalnetGuaranteedInvoiceGuaranteeError = novalnetGuaranteeValidationService.handleGuaranteeProcess(
                NOVALNET_GUARANTEED_INVOICE, paymentDetailsForm.getNovalnetGuaranteedInvoiceDateOfBirth(), paymentDetailsForm,
                deliveryAddress);
        if (!"".equals(novalnetGuaranteedInvoiceGuaranteeError)) {
            novalnetPaymentService.addPaymentProcess(model, paymentDetailsForm, cartData);
        }
        return true;
    }

    /**
     * Handles credit card payment data and one-click payment selection.
     *
     * @param paymentDetailsForm the payment details submitted during checkout
     * @param model              the model used to store checkout data
     * @param cartData           the current cart data
     * @return {@code true} if the credit card payment data is processed successfully; otherwise, {@code false}
     * @throws CMSItemNotFoundException if a required CMS item cannot be found
     */
    private boolean handleCreditCard(NovalnetPaymentDetailsForm paymentDetailsForm, Model model, CartData cartData)
            throws CMSItemNotFoundException {
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(NOVALNET_CREDIT_CARD);
        NovalnetCreditCardPaymentModeModel novalnetPaymentMethod = (NovalnetCreditCardPaymentModeModel) paymentModeModel;
        sessionService.setAttribute("novalnetCreditCardPanHash", paymentDetailsForm.getNovalnetCreditCardPanHash().trim());
        sessionService.setAttribute("novalnetCreditCardUniqueId", paymentDetailsForm.getNovalnetCreditCardUniqueId().trim());
        sessionService.setAttribute("do_redirect", paymentDetailsForm.getDo_redirect().trim());
        if ("".equals(paymentDetailsForm.getNovalnetCreditCardPanHash().trim())
                && Boolean.FALSE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping())) {
            novalnetPaymentService.addPaymentProcess(model, paymentDetailsForm, cartData);
        }
        sessionService.setAttribute(NOVALNET_CREDIT_CARD_STORE_PAYMENT_DATA, false);
        boolean oneClickEligible = Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping())
                && Boolean.FALSE.equals(novalnetCheckoutService.isGuestUser());
        if (oneClickEligible) {
            handleCreditCardOneClick(paymentDetailsForm);
        }
        return true;
    }

    /**
     * Processes the selected credit card one-click payment option.
     *
     * @param paymentDetailsForm the payment details containing the one-click selection
     */
    private void handleCreditCardOneClick(NovalnetPaymentDetailsForm paymentDetailsForm) {
        String oneClickData = paymentDetailsForm != null ? paymentDetailsForm.getCreditCardOneClickData1() : null;
        if (oneClickData == null) {
            oneClickData = "";
        }
        if (paymentDetailsForm == null) {
            LOGGER.info(PAYMENT_DETAILS_FORM_NULL);
        }
        if (!"".equals(oneClickData)) {
            applyCreditCardOneClickSelection(paymentDetailsForm);
        } else if (Boolean.TRUE.equals(paymentDetailsForm.isCreditcardSaveData())) {
            sessionService.setAttribute(NOVALNET_CREDIT_CARD_STORE_PAYMENT_DATA, true);
        }
    }

    /**
     * Applies the selected credit card one-click payment option.
     *
     * @param paymentDetailsForm the payment details containing the one-click selection
     */
    private void applyCreditCardOneClickSelection(NovalnetPaymentDetailsForm paymentDetailsForm) {
        String selection = paymentDetailsForm.getCreditCardOneClickData1().trim();
        if ("3".equals(selection) && Boolean.TRUE.equals(paymentDetailsForm.isCreditcardSaveData())) {
            sessionService.setAttribute(NOVALNET_CREDIT_CARD_STORE_PAYMENT_DATA, true);
        }
        if ("1".equals(selection)) {
            sessionService.setAttribute("novalnetCreditCardtoken", sessionService.getAttribute("novalnetCreditCardOneClickToken1"));
        }
        if ("2".equals(selection)) {
            sessionService.setAttribute("novalnetCreditCardtoken", sessionService.getAttribute("novalnetCreditCardOneClickToken2"));
        }
    }
}
