/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.nimbusds.jose.shaded.gson.Gson;
import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.PaymentConfigResult;
import com.novalnet.dto.payment.Custom;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.Merchant;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.PaymentData;
import com.novalnet.dto.payment.Transaction;
import com.novalnet.exception.NovalnetException;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetPaymentHandlerService;
import com.novalnet.service.payment.NovalnetPaymentMethodService;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.util.Config;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.ui.Model;

import java.util.Arrays;
import java.util.Map;


/**
 * Unified implementation of the Novalnet payment method service (B2C + B2B).
 */
public class DefaultNovalnetPaymentMethodService implements NovalnetPaymentMethodService {
    public static final String REDIRECT_PREFIX = "redirect:";

    private static final String NOVALNET_VERSION = "1.2.0";
    private static final String TRANSACTION = "transaction";
    private static final String TXN_SECRET = "txn_secret";
    private static final String NOVALNET_ZERO_AMOUNT_BOOKING = "novalnetZeroAmountBooking";
    private static final String NOVALNET_GOOGLE_PAY = "novalnetGooglePay";
    private static final String NOVALNET_APPLE_PAY = "novalnetApplePay";
    private static final Gson GSON = new Gson();


    @Resource
    private SessionService sessionService;

    @Resource
    private PaymentModeService paymentModeService;

    @Resource
    private NovalnetPaymentRequestBuilder novalnetPaymentRequestBuilder;

    @Resource
    private NovalnetApiService novalnetApiService;

    @Resource
    private NovalnetPaymentService novalnetPaymentService;

    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Resource
    private NovalnetPaymentHandlerService novalnetPaymentHandlerService;

    @Resource
    private DefaultNovalnetOneClickPaymentService novalnetOneClickPaymentService;

    @Resource
    private DefaultNovalnetTransactionResultService novalnetTransactionResultService;

    @Resource
    private DefaultNovalnetGuaranteeValidationService novalnetGuaranteeValidationService;

    @Override
    public void addPaymentProcess(Model model, NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData)
            throws CMSItemNotFoundException {
        novalnetPaymentService.addPaymentProcess(model, paymentDetailsForm, cartData);
    }

    @Override
    public boolean processOneClickTokenData(String currentPayment, NovalnetPaymentDetailsForm paymentDetailsForm, Model model,
                                            CartData cartData, AddressData deliveryAddress) throws CMSItemNotFoundException {
        return novalnetOneClickPaymentService.processOneClickTokenData(currentPayment, paymentDetailsForm, model, cartData,
                deliveryAddress);
    }

    @Override
    public boolean processTransaction(Map<String, String> resultMap) {
        return novalnetTransactionResultService.processTransaction(resultMap);
    }

    @Override
    public void handleStorePayment(String currentPayment, String response, JsonNode customerJsonObject) {
        novalnetTransactionResultService.handleStorePayment(currentPayment, response, customerJsonObject);
    }

    @Override
    public String createTransaction(HttpServletRequest request, BaseStoreModel baseStore, String currentPayment, String customerNo,
                                    Integer orderAmountCent, CartData cartData) {
        HostedPage hostedPage = new HostedPage();
        PaymentData paymentData = new PaymentData();

        NovalnetPaymentRequest paymentRequest = sessionService.getAttribute("novalnetPaymentRequest");

        if (paymentRequest == null || paymentRequest.getCustomer() == null) {
            throw new IllegalStateException("Customer data not found");
        }

        Customer customer = paymentRequest.getCustomer();
        String hybrisVersion = Config.getString("build.version", "unknown");
        String currency = cartData.getTotalPriceWithTax().getCurrencyIso();
        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
        Integer sessionOrderAmountCent = sessionService.getAttribute("novalnetOrderAmount");

        Merchant merchant = novalnetPaymentRequestBuilder.createMerchant(baseStore);

        novalnetPaymentRequestBuilder.populateCustomerData(customer, customer.getFirst_name(), customer.getLast_name(), null,
                customerNo, NovalnetUtils.getRemoteIpAddr(request), "u", null, null);

        Transaction transaction = novalnetPaymentRequestBuilder.createBaseTransaction(NovalnetUtils.getPaymentType(currentPayment),
                currency, sessionOrderAmountCent.longValue(), "SAP Commerce Cloud", hybrisVersion + "-NN" + NOVALNET_VERSION);

        String paymentName = novalnetPaymentService.getPaymentName(currentPayment);
        Custom custom = novalnetPaymentRequestBuilder.createCustomData(NovalnetUtils.resolveLanguageCode(), paymentName);

        PaymentConfigResult configResult = novalnetPaymentHandlerService.handlePayment(currentPayment, paymentModeModel,
                transaction, paymentData, customer, sessionOrderAmountCent, request, hostedPage);

        boolean verifyPaymentData = configResult.isVerifyPaymentData();
        transaction.setTest_mode(String.valueOf(configResult.getTestMode()));

        applyRedirectSettings(transaction, paymentData, request, currentPayment, configResult.isRedirect());
        applyZeroAmountBookingFlag(transaction, configResult.isZeroAmountBooking());

        NovalnetPaymentRequest requestToSend = novalnetPaymentRequestBuilder.createPaymentRequest(merchant, customer, transaction,
                custom, hostedPage);
        String jsonString = novalnetPaymentRequestBuilder.serializePaymentRequest(requestToSend);

        String url = resolveTransactionUrl(currentPayment, verifyPaymentData);
        return novalnetApiService.sendRequest(url, jsonString);
    }

    private void applyRedirectSettings(Transaction transaction, PaymentData paymentData, HttpServletRequest request,
                                       String currentPayment, boolean redirect) {
        if (Boolean.TRUE.equals(redirect)) {
            String currentUrl = request.getRequestURL().toString();
            String[] walletPayments =
                    {NOVALNET_GOOGLE_PAY};

            String returnUrl = currentUrl
                    .replace(!Arrays.asList(walletPayments).contains(currentPayment) ? "novalnet/summary/placeOrder"
                            : "novalnet/summary/bookWalletTransaction", "novalnet/hop-response");

            transaction.setReturn_url(returnUrl);
            transaction.setError_return_url(returnUrl);
        }

        if (hasPaymentData(paymentData)) {
            transaction.setPayment_data(paymentData);
        }
    }

    private boolean hasPaymentData(final PaymentData data) {
        return data != null && GSON.toJsonTree(data).getAsJsonObject().size() > 0;
    }

    private void applyZeroAmountBookingFlag(Transaction transaction, boolean zeroAmountBooking) {
        if (Boolean.TRUE.equals(zeroAmountBooking)) {
            transaction.setAmount(0L);
            sessionService.setAttribute(NOVALNET_ZERO_AMOUNT_BOOKING, Boolean.TRUE);
        } else {
            sessionService.setAttribute(NOVALNET_ZERO_AMOUNT_BOOKING, Boolean.FALSE);
        }
    }

    private String resolveTransactionUrl(String currentPayment, boolean verifyPaymentData) {
        if (NOVALNET_APPLE_PAY.equals(currentPayment)) {
            return verifyPaymentData ? novalnetEndpointConfigService.getAuthorizeHostedUrl()
                    : novalnetEndpointConfigService.getPaymentHostedUrl();
        }

        return verifyPaymentData ? novalnetEndpointConfigService.getAuthorizeUrl() : novalnetEndpointConfigService.getPaymentUrl();
    }

    @Override
    public String bookWalletTransaction(HttpServletRequest request, BaseStoreModel baseStore, String customerNo,
                                        Integer orderAmountCent, CartData cartData) throws NovalnetException {

        String response = createTransaction(request, baseStore, NOVALNET_GOOGLE_PAY, customerNo, orderAmountCent, cartData);

        JSONObject tomJsonObject = new JSONObject(response);
        JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");

        if (resultJsonObject.has("redirect_url")) {
            sessionService.setAttribute("txn_check", baseStore.getNovalnetPaymentAccessKey().trim());
        }

        if (tomJsonObject.has(TRANSACTION)) {
            JSONObject transactionJsonObject = tomJsonObject.getJSONObject(TRANSACTION);

            if (transactionJsonObject.has(TXN_SECRET)) {
                sessionService.setAttribute(TXN_SECRET, transactionJsonObject.get(TXN_SECRET).toString());
            }

            if (transactionJsonObject.has("tid")) {
                sessionService.setAttribute("wallet_tid", transactionJsonObject.get("tid").toString());
            }
        }

        return response;
    }

    @Override
    public String handleGuaranteeProcess(String paymentName, String dob, NovalnetPaymentDetailsForm paymentDetailsForm,
                                         AddressData deliveryAddress) {
        return novalnetGuaranteeValidationService.handleGuaranteeProcess(paymentName, dob, paymentDetailsForm, deliveryAddress);
    }


}
