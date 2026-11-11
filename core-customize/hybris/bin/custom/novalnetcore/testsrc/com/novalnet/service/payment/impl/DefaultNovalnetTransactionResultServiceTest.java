/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.localization.Localization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetTransactionResultServiceTest {
    private static final String TRANSACTION_ID = "123456789";
    private static final String LANGUAGE = "en";
    private static final String TRANSACTION_DETAILS_URL = "https://pay.example.test/transaction/details";
    private static final String FAILED_RESPONSE = "{\"result\":{\"status_text\":\"Payment declined\"},"
            + "\"transaction\":{\"status\":\"FAILURE\"}}";
    private static final String NO_STATUS_TEXT_RESPONSE = "{\"result\":{},\"transaction\":{\"status\":\"FAILURE\"}}";
    private static final String CONFIRMED_RESPONSE = "{\"result\":{\"status_code\":\"100\",\"status_text\":\"Success\"},"
            + "\"customer\":{\"email\":\"customer@example.com\"},"
            + "\"transaction\":{\"tid\":\"123456789\",\"status\":\"CONFIRMED\",\"amount\":1250,\"currency\":\"EUR\"}}";
    private static final String WALLET_RESPONSE = "{\"transaction\":{\"payment_data\":{\"token\":\"abc\"}}}";
    private static final String WALLET_RESPONSE_WITHOUT_TOKEN = "{\"transaction\":{\"payment_data\":{}}}";
    private static final String CUSTOMER_JSON = "{\"customer_no\":\"C1\"}";

    private static final String CHECKOUT_ERROR = "novalnetCheckoutError";
    private static final String ORDER_CURRENCY = "novalnetOrderCurrency";
    private static final String ORDER_AMOUNT = "novalnetOrderAmount";
    private static final String UNKNOWN_ERROR_KEY = "novalnet.unknown.error";
    private static final String CREDIT_CARD = "novalnetCreditCard";
    private static final String GOOGLE_PAY = "novalnetGooglePay";
    private static final String STORE_CARD_KEY = "novalnetCreditCardStorePaymentData";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @InjectMocks
    private DefaultNovalnetTransactionResultService service;

    @Mock
    private NovalnetApiService apiService;

    @Mock
    private NovalnetEndpointConfigService endpointConfigService;

    @Mock
    private SessionService sessionService;

    @Mock
    private NovalnetCheckoutService checkoutService;

    @Mock
    private NovalnetPaymentService paymentService;

    @Mock
    private NovalnetPaymentRequestBuilder requestBuilder;

    private MockedStatic<NovalnetUtils> novalnetUtils;

    private MockedStatic<Localization> localization;

    @BeforeEach
    void setUp() {
        novalnetUtils = mockStatic(NovalnetUtils.class);
        novalnetUtils.when(NovalnetUtils::resolveLanguageCode).thenReturn(LANGUAGE);

        localization = mockStatic(Localization.class);
        localization.when(() -> Localization.getLocalizedString(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        localization.close();
        novalnetUtils.close();
    }

    @Test
    void shouldReturnFalseAndStoreGatewayErrorWhenTransactionFails() {
        stubDetailsRequest(FAILED_RESPONSE);

        assertFalse(service.processTransaction(createTransactionResult()));

        verify(sessionService).setAttribute(CHECKOUT_ERROR, "Payment declined");
        verify(sessionService).setAttribute(ORDER_CURRENCY, null);
        verify(sessionService).setAttribute(ORDER_AMOUNT, null);
    }

    @Test
    void shouldUseFallbackStatusDescriptionWhenGatewayStatusTextIsMissing() {
        stubDetailsRequest(NO_STATUS_TEXT_RESPONSE);

        assertFalse(service.processTransaction(createTransactionResult()));

        verify(sessionService).setAttribute(CHECKOUT_ERROR, "Fallback error");
    }

    @Test
    void shouldReturnFalseAndStoreUnknownErrorWhenResponseIsNotValidJson() {
        stubDetailsRequest("{invalid");

        assertFalse(service.processTransaction(createTransactionResult()));

        verify(sessionService).setAttribute(CHECKOUT_ERROR, UNKNOWN_ERROR_KEY);
    }

    @Test
    void handleStorePaymentShouldStoreCreditCardReferenceWhenEnabled() throws Exception {
        final JsonNode customer = MAPPER.readTree(CUSTOMER_JSON);

        when(checkoutService.isGuestUser()).thenReturn(false);
        when(sessionService.<Boolean>getAttribute(STORE_CARD_KEY)).thenReturn(Boolean.TRUE);

        service.handleStorePayment(CREDIT_CARD, CONFIRMED_RESPONSE, customer);

        verify(paymentService).handleReferenceTransactionInfo(CONFIRMED_RESPONSE, "C1", CREDIT_CARD);
    }

    @Test
    void handleStorePaymentShouldNotStoreCreditCardReferenceWhenDisabled() throws Exception {
        final JsonNode customer = MAPPER.readTree(CUSTOMER_JSON);

        when(checkoutService.isGuestUser()).thenReturn(false);
        when(sessionService.<Boolean>getAttribute(STORE_CARD_KEY)).thenReturn(Boolean.FALSE);

        service.handleStorePayment(CREDIT_CARD, CONFIRMED_RESPONSE, customer);

        verify(paymentService, never()).handleReferenceTransactionInfo(any(), any(), any());
    }

    @Test
    void handleStorePaymentShouldIgnoreCreditCardForGuestUser() throws Exception {
        final JsonNode customer = MAPPER.readTree(CUSTOMER_JSON);

        when(checkoutService.isGuestUser()).thenReturn(true);

        service.handleStorePayment(CREDIT_CARD, CONFIRMED_RESPONSE, customer);

        verify(paymentService, never()).handleReferenceTransactionInfo(any(), any(), any());
    }

    @Test
    void handleStorePaymentShouldStoreWalletReferenceWhenTokenAndConsentExist() throws Exception {
        final JsonNode customer = MAPPER.readTree(CUSTOMER_JSON);

        when(checkoutService.isGuestUser()).thenReturn(false);
        when(sessionService.<Boolean>getAttribute(GOOGLE_PAY + "StorePaymentData")).thenReturn(Boolean.TRUE);

        service.handleStorePayment(GOOGLE_PAY, WALLET_RESPONSE, customer);

        verify(paymentService).handleReferenceTransactionInfo(WALLET_RESPONSE, "C1", GOOGLE_PAY);
    }

    @Test
    void handleStorePaymentShouldSkipWalletWhenTokenIsMissing() throws Exception {
        final JsonNode customer = MAPPER.readTree(CUSTOMER_JSON);

        service.handleStorePayment(GOOGLE_PAY, WALLET_RESPONSE_WITHOUT_TOKEN, customer);

        verify(paymentService, never()).handleReferenceTransactionInfo(any(), any(), any());
    }

    @Test
    void handleStorePaymentShouldDoNothingForOtherPaymentMethods() throws Exception {
        final JsonNode customer = MAPPER.readTree(CUSTOMER_JSON);

        service.handleStorePayment("novalnetInvoice", CONFIRMED_RESPONSE, customer);

        verify(paymentService, never()).handleReferenceTransactionInfo(any(), any(), any());
    }

    private void stubDetailsRequest(final String response) {
        when(requestBuilder.createTransactionDetailsRequest(TRANSACTION_ID, LANGUAGE)).thenReturn(new NovalnetPaymentRequest());
        when(endpointConfigService.getTransactionDetailsUrl()).thenReturn(TRANSACTION_DETAILS_URL);
        when(apiService.sendRequest(eq(TRANSACTION_DETAILS_URL), anyString())).thenReturn(response);
    }

    private Map<String, String> createTransactionResult() {
        final Map<String, String> resultMap = new HashMap<>();
        resultMap.put("tid", TRANSACTION_ID);
        resultMap.put("status_desc", "Fallback error");
        return resultMap;
    }
}
