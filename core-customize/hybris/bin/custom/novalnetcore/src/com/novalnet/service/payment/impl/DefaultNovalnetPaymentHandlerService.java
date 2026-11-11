/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.PaymentConfigResult;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.PaymentData;
import com.novalnet.dto.payment.Transaction;
import com.novalnet.model.*;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.payment.NovalnetPaymentHandlerService;
import com.novalnet.service.payment.PaymentConfigurator;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.servicelayer.session.SessionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * Default implementation of {@link NovalnetPaymentHandlerService} for handling Novalnet payment configurations.
 * <p>
 * Configures payment-specific settings, including test mode, redirects, authorization, and payment tokens.
 */
@Service("novalnetPaymentHandlerService")
public class DefaultNovalnetPaymentHandlerService implements NovalnetPaymentHandlerService {
    public static final int PREPAYMENT_FROM_DATE = 7;
    public static final int PREPAYMENT_TILL_DATE = 28;
    private static final Log LOGGER = LogFactory.getLog(DefaultNovalnetPaymentHandlerService.class);
    private static final String PAYMENT_AUTHORIZE = "AUTHORIZE";
    private static final String AUTHORIZE_WITH_ZERO_AMOUNT = "AUTHORIZE_WITH_ZERO_AMOUNT";
    private static final int SEPA_DUE_DATE_MIN_DAYS = 3;
    private static final int SEPA_DUE_DATE_MAX_DAYS = 14;
    private static final int INVOICE_DUE_DATE_MIN_DAYS = 7;

    private static final String SESSION_ATTR_CREDIT_CARD_TOKEN = "novalnetCreditCardtoken";
    private static final String ONHOLD_ORDER_AMOUNT_NULL_LOG = "onhold order amount is null";
    private static final String SESSION_ATTR_DIRECT_DEBIT_ACH_TOKEN = "novalnetDirectDebitAchtoken";
    private static final String SESSION_ATTR_DIRECT_DEBIT_SEPA_TOKEN = "novalnetDirectDebitSepatoken";
    private static final String SESSION_ATTR_APPLE_PAY_TOKEN = "novalnetApplePaytoken";
    private static final String SESSION_ATTR_GUARANTEED_SEPA_BIC = "novalnetGuaranteedDirectDebitSepaAccountBic";
    private static final int PREPAYMENT_MIN_DUE_DATE_DAYS = 7;
    private final Map<String, PaymentConfigurator> paymentConfigurators = buildPaymentConfigurators();
    @Resource
    private SessionService sessionService;
    @Resource
    private NovalnetCheckoutService novalnetCheckoutService;

    private Map<String, PaymentConfigurator> buildPaymentConfigurators() {
        Map<String, PaymentConfigurator> configurators = new HashMap<>();

        configurators.put("novalnetDirectDebitSepa", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                      result) -> configureSepa((NovalnetDirectDebitSepaPaymentModeModel) mode, transaction, paymentData, amount, result));

        configurators.put("novalnetDirectDebitAch", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                     result) -> configureAch((NovalnetDirectDebitAchPaymentModeModel) mode, transaction, paymentData, result));

        configurators
                .put("novalnetGuaranteedDirectDebitSepa",
                        (mode, transaction, paymentData, customer, amount, request, hostedPage, result) -> configureGuaranteedSepa(
                                (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) mode, transaction, paymentData, customer, amount,
                                result));

        configurators.put("novalnetPayPal", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                             result) -> configurePayPal((NovalnetPayPalPaymentModeModel) mode, amount, result));

        configurators.put("novalnetCreditCard", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                 result) -> configureCreditCard((NovalnetCreditCardPaymentModeModel) mode, transaction, paymentData, amount, result));

        configurators.put("novalnetInvoice", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                              result) -> configureInvoice((NovalnetInvoicePaymentModeModel) mode, transaction, amount, result));

        configurators.put("novalnetPrepayment", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                 result) -> configurePrepayment((NovalnetPrepaymentPaymentModeModel) mode, transaction, result));

        configurators.put("novalnetMultibanco", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                 result) -> configureMultibanco((NovalnetMultibancoPaymentModeModel) mode, result));

        configurators.put("novalnetTwint", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                            result) -> configureTwint((NovalnetTwintPaymentModeModel) mode, result));

        configurators.put("novalnetMbWay", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                            result) -> configureMbWay((NovalnetMbWayPaymentModeModel) mode, result));

        configurators.put("novalnetTrustly", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                              result) -> configureTrustly((NovalnetTrustlyPaymentModeModel) mode, result));

        configurators.put("novalnetBlik", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                           result) -> configureBlik((NovalnetBlikPaymentModeModel) mode, result));

        configurators.put("novalnetWeChatPay", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                result) -> configureWechatPay((NovalnetWeChatPayPaymentModeModel) mode, result));

        configurators.put("novalnetAlipay", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                             result) -> configureAlipay((NovalnetAliPayPaymentModeModel) mode, result));

        configurators.put("novalnetGuaranteedInvoice", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                        result) -> configureGuaranteedInvoice((NovalnetGuaranteedInvoicePaymentModeModel) mode, customer, amount, result));

        configurators.put("novalnetOnlineBankTransfer", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                         result) -> configureOnlineBankTransfer((NovalnetOnlineBankTransferPaymentModeModel) mode, result));

        configurators.put("novalnetBancontact", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                 result) -> configureBancontact((NovalnetBancontactPaymentModeModel) mode, result));

        configurators.put("novalnetIdeal", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                            result) -> configureIdeal((NovalnetIdealPaymentModeModel) mode, result));

        configurators.put("novalnetGooglePay",
                (mode, transaction, paymentData, customer, amount, request, hostedPage, result) -> configureGooglePay(
                        (NovalnetGooglePayPaymentModeModel) mode, request, paymentData, transaction, amount, result));

        configurators.put("novalnetApplePay",
                (mode, transaction, paymentData, customer, amount, request, hostedPage, result) -> configureApplePay(
                        (NovalnetApplePayPaymentModeModel) mode, hostedPage, amount, result, transaction, paymentData));

        configurators.put("novalnetEps", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                          result) -> configureEps((NovalnetEpsPaymentModeModel) mode, result));

        configurators.put("novalnetPostFinance", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                  result) -> configurePostFinance((NovalnetPostFinancePaymentModeModel) mode, result));

        configurators.put("novalnetPostFinanceCard", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                      result) -> configurePostFinanceCard((NovalnetPostFinanceCardPaymentModeModel) mode, result));

        configurators.put("novalnetPrzelewy24", (mode, transaction, paymentData, customer, amount, request, hostedPage,
                                                 result) -> configurePrzelewy24((NovalnetPrzelewy24PaymentModeModel) mode, result));

        return configurators;
    }

    @Override
    public PaymentConfigResult handlePayment(String currentPayment, PaymentModeModel paymentModeModel, Transaction transaction,
                                             PaymentData paymentData, Customer customer, Integer orderAmountCent, HttpServletRequest request, HostedPage hostedPage) {
        PaymentConfigResult result = new PaymentConfigResult();

        PaymentConfigurator configurator = paymentConfigurators.get(currentPayment);

        if (configurator != null) {
            configurator.configure(paymentModeModel, transaction, paymentData, customer, orderAmountCent, request, hostedPage,
                    result);
        } else {
            LOGGER.warn("Unsupported payment type: " + currentPayment);
        }

        return result;
    }

    private Integer resolveOnholdOrderAmount(boolean paymentMethodPresent, Supplier<Integer> amountSupplier, boolean useErrorLevel) {
        if (paymentMethodPresent) {
            Integer onholdOrderAmount = amountSupplier.get();
            return onholdOrderAmount == null ? 0 : onholdOrderAmount;
        }

        if (useErrorLevel) {
            LOGGER.error(ONHOLD_ORDER_AMOUNT_NULL_LOG);
        } else {
            LOGGER.info(ONHOLD_ORDER_AMOUNT_NULL_LOG);
        }

        return 0;
    }

    private String resolveToken(String sessionKey) {
        String token = sessionService.getAttribute(sessionKey);
        if (token != null) {
            return token;
        }

        return "";
    }

    private void applyVerifyPaymentData(String onholdActionType, Integer orderAmountCent, Integer onholdOrderAmount,
                                        PaymentConfigResult result) {
        if (PAYMENT_AUTHORIZE.equals(onholdActionType) && orderAmountCent >= onholdOrderAmount) {
            result.setVerifyPaymentData(true);
        }
    }

    private void applyZeroAmountBooking(String onholdActionType, PaymentConfigResult result) {
        if (AUTHORIZE_WITH_ZERO_AMOUNT.equals(onholdActionType)) {
            result.setZeroAmountBooking(true);
        }
    }

    private void applyOneClickTokenCreation(boolean eligible, Transaction transaction) {
        if (eligible) {
            transaction.setCreate_token("1");
        }
    }

    private void applyOneClickTokenUsage(boolean eligible, String token, PaymentData paymentData, String tokenSessionKey) {
        if (eligible) {
            paymentData.setToken(token);
            sessionService.setAttribute(tokenSessionKey, null);
        }
    }

    private void configureSepa(NovalnetDirectDebitSepaPaymentModeModel novalnetPaymentMethod, Transaction transaction,
                               PaymentData paymentData, Integer orderAmountCent, PaymentConfigResult result) {
        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }

        Integer sepaDueDate = novalnetPaymentMethod.getNovalnetDueDate();
        if (sepaDueDate != null && sepaDueDate >= SEPA_DUE_DATE_MIN_DAYS && sepaDueDate <= SEPA_DUE_DATE_MAX_DAYS) {
            transaction.setDue_date(NovalnetUtils.formatDate(sepaDueDate));
        }

        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), true);

        applyVerifyPaymentData(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), orderAmountCent,
                onholdOrderAmount, result);

        applyZeroAmountBooking(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), result);

        boolean novalnetDirectDebitSepaStorePaymentData = sessionService.getAttribute("novalnetDirectDebitSepaStorePaymentData");

        String token = resolveToken(SESSION_ATTR_DIRECT_DEBIT_SEPA_TOKEN);
        boolean isGuestUser = novalnetCheckoutService.isGuestUser();
        boolean oneClickShopping = novalnetPaymentMethod.getNovalnetOneClickShopping();

        applyOneClickTokenCreation(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping)
                && Boolean.TRUE.equals(novalnetDirectDebitSepaStorePaymentData), transaction);

        applyOneClickTokenUsage(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping) && !"".equals(token),
                token, paymentData, SESSION_ATTR_DIRECT_DEBIT_SEPA_TOKEN);

        if ("".equals(token)) {
            applySepaAccountDetails(paymentData);
        }
    }

    private void applySepaAccountDetails(PaymentData paymentData) {
        String accountHolder = (String) sessionService.getAttribute("novalnetDirectDebitSepaAccountHolder");
        paymentData.setIban((String) sessionService.getAttribute("novalnetDirectDebitSepaAccountIban"));

        String bic = (String) sessionService.getAttribute("novalnetDirectDebitSepaAccountBic");
        if (bic != null && !bic.isEmpty()) {
            paymentData.setBic(bic);
            sessionService.setAttribute("novalnetDirectDebitSepaAccountBic", null);
        }

        if (accountHolder != null) {
            paymentData.setAccount_holder(accountHolder.replace("&", ""));
        }

        sessionService.setAttribute("novalnetDirectDebitSepaAccountIban", null);
        sessionService.setAttribute("novalnetDirectDebitSepaAccountHolder", null);
    }

    private void configureAch(NovalnetDirectDebitAchPaymentModeModel novalnetPaymentMethod, Transaction transaction,
                              PaymentData paymentData, PaymentConfigResult result) {
        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }

        applyZeroAmountBooking(novalnetPaymentMethod.getOnholdActionTypeWithoutAuthorize().toString(), result);

        boolean novalnetDirectDebitAchStorePaymentData = sessionService.getAttribute("novalnetDirectDebitAchStorePaymentData");

        String token = resolveToken(SESSION_ATTR_DIRECT_DEBIT_ACH_TOKEN);
        boolean isGuestUser = novalnetCheckoutService.isGuestUser();
        boolean oneClickShopping = novalnetPaymentMethod.getNovalnetOneClickShopping();

        applyOneClickTokenCreation(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping)
                && Boolean.TRUE.equals(novalnetDirectDebitAchStorePaymentData), transaction);

        applyOneClickTokenUsage(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping) && !"".equals(token),
                token, paymentData, SESSION_ATTR_DIRECT_DEBIT_ACH_TOKEN);

        if ("".equals(token)) {
            applyAchAccountDetails(paymentData);
        }
    }

    private void applyAchAccountDetails(PaymentData paymentData) {
        String accountHolder = (String) sessionService.getAttribute("novalnetDirectDebitAchAccountHolder");

        paymentData.setAccount_number((String) sessionService.getAttribute("novalnetDirectDebitAchAchAccountNumber"));

        String routingNumber = (String) sessionService.getAttribute("novalnetDirectDebitAchRoutingNumber");

        if (routingNumber != null && !routingNumber.isEmpty()) {
            paymentData.setRouting_number(routingNumber);
            sessionService.setAttribute("novalnetDirectDebitAchRoutingNumber", null);
        }

        paymentData.setAccount_holder(accountHolder.replace("&", ""));
        sessionService.setAttribute("novalnetDirectDebitAchAchAccountNumber", null);
        sessionService.setAttribute("novalnetDirectDebitAchAccountHolder", null);
    }

    private void configureGuaranteedSepa(NovalnetGuaranteedDirectDebitSepaPaymentModeModel novalnetPaymentMethod,
                                         Transaction transaction, PaymentData paymentData, Customer customer, Integer orderAmountCent, PaymentConfigResult result) {
        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }

        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), true);

        boolean novalnetGuaranteedDirectDebitSepaStorePaymentData = sessionService
                .getAttribute("novalnetGuaranteedDirectDebitSepaStorePaymentData");

        String token = resolveToken(SESSION_ATTR_DIRECT_DEBIT_SEPA_TOKEN);
        boolean isGuestUser = novalnetCheckoutService.isGuestUser();
        boolean oneClickShopping = novalnetPaymentMethod.getNovalnetOneClickShopping();

        applyOneClickTokenCreation(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping)
                && Boolean.TRUE.equals(novalnetGuaranteedDirectDebitSepaStorePaymentData), transaction);

        applyOneClickTokenUsage(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping) && !"".equals(token),
                token, paymentData, SESSION_ATTR_DIRECT_DEBIT_SEPA_TOKEN);

        if ("".equals(token)) {
            applyGuaranteedSepaAccountDetails(paymentData);
        }

        String dob = sessionService.getAttribute("novalnetGuaranteedDirectDebitSepaDateOfBirth");
        customer.setBirth_date(dob);

        applyVerifyPaymentData(novalnetPaymentMethod.getNovalnetOnholdAction().toString(), orderAmountCent, onholdOrderAmount,
                result);
    }

    private void applyGuaranteedSepaAccountDetails(PaymentData paymentData) {
        String accountHolder = sessionService.getAttribute("novalnetGuaranteedDirectDebitSepaAccountHolder");

        paymentData.setIban(sessionService.getAttribute("novalnetGuaranteedDirectDebitSepaAccountIban").toString());

        if (sessionService.getAttribute(SESSION_ATTR_GUARANTEED_SEPA_BIC) != null
                && !"".equals(sessionService.getAttribute(SESSION_ATTR_GUARANTEED_SEPA_BIC).toString())) {
            paymentData.setBic(sessionService.getAttribute(SESSION_ATTR_GUARANTEED_SEPA_BIC).toString());

            sessionService.setAttribute(SESSION_ATTR_GUARANTEED_SEPA_BIC, null);
        }

        paymentData.setAccount_holder(accountHolder.replace("&", ""));
        sessionService.setAttribute("novalnetGuaranteedDirectDebitSepaAccountIban", null);
        sessionService.setAttribute("novalnetGuaranteedDirectDebitSepaAccountHolder", null);
    }

    private void configurePayPal(NovalnetPayPalPaymentModeModel novalnetPaymentMethod, Integer orderAmountCent,
                                 PaymentConfigResult result) {
        result.setRedirect(true);

        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), false);

        applyVerifyPaymentData(novalnetPaymentMethod.getNovalnetOnholdAction().toString(), orderAmountCent, onholdOrderAmount,
                result);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    @SuppressWarnings("java:S117")
    private void configureCreditCard(NovalnetCreditCardPaymentModeModel novalnetPaymentMethod, Transaction transaction,
                                     PaymentData paymentData, Integer orderAmountCent, PaymentConfigResult result) {
        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), false);

        if (Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetEnforce3D())) {
            transaction.setEnforce_3d(1);
        }

        applyVerifyPaymentData(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), orderAmountCent,
                onholdOrderAmount, result);

        applyZeroAmountBooking(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), result);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }

        String token = resolveToken(SESSION_ATTR_CREDIT_CARD_TOKEN);

        boolean novalnetCreditCardStorePaymentData = sessionService.getAttribute("novalnetCreditCardStorePaymentData");

        boolean isGuestUser = novalnetCheckoutService.isGuestUser();
        boolean oneClickShopping = novalnetPaymentMethod.getNovalnetOneClickShopping();

        applyOneClickTokenCreation(!isGuestUser && oneClickShopping && Boolean.TRUE.equals(novalnetCreditCardStorePaymentData),
                transaction);

        if (!isGuestUser && oneClickShopping && !"".equals(token)) {
            paymentData.setToken(token);
            sessionService.setAttribute(SESSION_ATTR_CREDIT_CARD_TOKEN, null);
        } else {
            paymentData.setPan_hash((String) sessionService.getAttribute("novalnetCreditCardPanHash"));
            paymentData.setUnique_id((String) sessionService.getAttribute("novalnetCreditCardUniqueId"));

            String do_redirect = sessionService.getAttribute("do_redirect");

            if (!"".equals(do_redirect)) {
                result.setRedirect(true);
            }

            sessionService.setAttribute("novalnetCreditCardPanHash", null);
        }
    }

    private void configureInvoice(NovalnetInvoicePaymentModeModel novalnetPaymentMethod, Transaction transaction,
                                  Integer orderAmountCent, PaymentConfigResult result) {
        Integer invoiceDueDate = novalnetPaymentMethod.getNovalnetDueDate();

        if (invoiceDueDate != null && invoiceDueDate > INVOICE_DUE_DATE_MIN_DAYS) {
            transaction.setDue_date(NovalnetUtils.formatDate(invoiceDueDate));
        }

        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), false);

        applyVerifyPaymentData(novalnetPaymentMethod.getNovalnetOnholdAction().toString(), orderAmountCent, onholdOrderAmount,
                result);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configurePrepayment(NovalnetPrepaymentPaymentModeModel novalnetPaymentMethod, Transaction transaction,
                                     PaymentConfigResult result) {
        Integer prepaymentDueDate = novalnetPaymentMethod.getNovalnetDueDate();

        if (prepaymentDueDate != null && PREPAYMENT_FROM_DATE >= PREPAYMENT_MIN_DUE_DATE_DAYS
                && prepaymentDueDate <= PREPAYMENT_TILL_DATE) {
            transaction.setDue_date(NovalnetUtils.formatDate(prepaymentDueDate));
        }

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureMultibanco(NovalnetMultibancoPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureTwint(NovalnetTwintPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureMbWay(NovalnetMbWayPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureTrustly(NovalnetTrustlyPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureBlik(NovalnetBlikPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureWechatPay(NovalnetWeChatPayPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureAlipay(NovalnetAliPayPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureGuaranteedInvoice(NovalnetGuaranteedInvoicePaymentModeModel novalnetPaymentMethod, Customer customer,
                                            Integer orderAmountCent, PaymentConfigResult result) {
        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), false);

        applyVerifyPaymentData(novalnetPaymentMethod.getNovalnetOnholdAction().toString(), orderAmountCent, onholdOrderAmount,
                result);

        String dob = sessionService.getAttribute("novalnetGuaranteedInvoiceDateOfBirth");
        customer.setBirth_date(dob);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureOnlineBankTransfer(NovalnetOnlineBankTransferPaymentModeModel novalnetPaymentMethod,
                                             PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureBancontact(NovalnetBancontactPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureIdeal(NovalnetIdealPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureGooglePay(NovalnetGooglePayPaymentModeModel novalnetPaymentMethod, HttpServletRequest request,
                                    PaymentData paymentData, Transaction transaction, Integer orderAmountCent, PaymentConfigResult result) {
        paymentData.setWallet_token(request.getParameter("token"));

        if ("true".equals(request.getParameter("doRedirect"))) {
            result.setRedirect(true);
        }

        if (Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetEnforce3D())) {
            transaction.setEnforce_3d(1);
        }

        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), false);

        applyVerifyPaymentData(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), orderAmountCent,
                onholdOrderAmount, result);

        applyZeroAmountBooking(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), result);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configureApplePay(NovalnetApplePayPaymentModeModel novalnetPaymentMethod, HostedPage hostedPage,
                                   Integer orderAmountCent, PaymentConfigResult result, Transaction transaction, PaymentData paymentData) {
        hostedPage.setDisplay_payments(Arrays.asList("APPLEPAY"));
        hostedPage.setHide_blocks(Arrays.asList("ADDRESS_FORM", "SHOP_INFO", "LANGUAGE_MENU", "HEADER", "TARIFF"));
        hostedPage.setSkip_pages(Arrays.asList("CONFIRMATION_PAGE", "SUCCESS_PAGE"));

        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }

        Integer onholdOrderAmount = resolveOnholdOrderAmount(novalnetPaymentMethod != null,
                () -> novalnetPaymentMethod.getNovalnetOnholdAmount(), false);

        applyVerifyPaymentData(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), orderAmountCent,
                onholdOrderAmount, result);

        applyZeroAmountBooking(novalnetPaymentMethod.getOnholdActionTypeWithZeroAmount().toString(), result);

        boolean novalnetApplePayStorePaymentData = sessionService.getAttribute("novalnetApplePayStorePaymentData");

        String token = resolveToken(SESSION_ATTR_APPLE_PAY_TOKEN);
        boolean isGuestUser = novalnetCheckoutService.isGuestUser();
        boolean oneClickShopping = novalnetPaymentMethod.getNovalnetOneClickShopping();

        applyOneClickTokenCreation(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping)
                && Boolean.TRUE.equals(novalnetApplePayStorePaymentData), transaction);

        applyOneClickTokenUsage(Boolean.FALSE.equals(isGuestUser) && Boolean.TRUE.equals(oneClickShopping) && !"".equals(token),
                token, paymentData, SESSION_ATTR_APPLE_PAY_TOKEN);
    }

    private void configureEps(NovalnetEpsPaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configurePostFinance(NovalnetPostFinancePaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configurePostFinanceCard(NovalnetPostFinanceCardPaymentModeModel novalnetPaymentMethod,
                                          PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }

    private void configurePrzelewy24(NovalnetPrzelewy24PaymentModeModel novalnetPaymentMethod, PaymentConfigResult result) {
        result.setRedirect(true);

        if (novalnetPaymentMethod.getNovalnetTestMode()) {
            result.setTestMode(1);
        } else {
            result.setTestMode(0);
        }
    }
}
