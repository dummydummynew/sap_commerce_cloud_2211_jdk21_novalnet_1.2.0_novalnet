/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.http.impl;

import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.sun.net.httpserver.HttpServer;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetApiServiceTest {
    private static final String SUCCESS_RESPONSE = "SUCCESS";
    private static final String ACCESS_KEY = "test-access-key";
    private static final String PAYMENT_ACCESS_KEY = "payment-access-key";
    private static final String ACTIVATION_KEY = "activation-key";
    private static final String BASE_URL = "http://localhost:";
    private static final String PAYMENT_ENDPOINT = "/payment";
    private static final String FOLLOWUP_ENDPOINT = "/followup";
    private static final String BOOKING_ENDPOINT = "/booking";
    private static final String MERCHANT_ENDPOINT = "/merchant";
    private static final String WEBHOOK_ENDPOINT = "/webhook";
    private static final String WEBHOOK_URL = "https://example.com/webhook";
    private static final String ACCESS_KEY_HEADER = "X-NN-Access-Key";
    private static final String REQUEST_BODY_AMOUNT = "{\"amount\":100}";
    private static final String REQUEST_BODY_BOOKING = "{\"amount\":500}";
    private static final String REQUEST_BODY_TRANSACTION = "{\"transaction\":123}";
    private static final String REQUEST_BODY_MERCHANT = "{\"merchant\":\"test\"}";
    private static final String LANGUAGE_JSON = "\"lang\":\"EN\"";
    private static final String EMPTY_JSON = "{}";
    private static final String LANGUAGE_ISO = "en";
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> accessKey = new AtomicReference<>();
    private final AtomicReference<String> requestPath = new AtomicReference<>();
    @Mock
    private BaseStoreService baseStoreService;
    @Mock
    private NovalnetEndpointConfigService endpointConfigService;
    @Mock
    private BaseStoreModel baseStore;
    @Mock
    private LanguageModel language;
    @InjectMocks
    private DefaultNovalnetApiService novalnetApiService;
    private HttpServer server;
    private String serverUrl;

    @BeforeEach
    void setUp() throws IOException {
        final int responseCode = 200;
        requestBody.set(null);
        accessKey.set(null);
        requestPath.set(null);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            accessKey.set(exchange.getRequestHeaders().getFirst(ACCESS_KEY_HEADER));
            requestPath.set(exchange.getRequestURI().getPath());

            final byte[] response = SUCCESS_RESPONSE.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(responseCode, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        server.start();
        serverUrl = BASE_URL + server.getAddress().getPort();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void testSendRequestSuccess() {
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        mockBaseStoreAccessKey();

        final String response = novalnetApiService.sendRequest(serverUrl + PAYMENT_ENDPOINT, REQUEST_BODY_AMOUNT);

        assertEquals(SUCCESS_RESPONSE, response);
        assertEquals(REQUEST_BODY_AMOUNT, requestBody.get());
        assertEquals(PAYMENT_ENDPOINT, requestPath.get());
        assertEquals(encodeExpectedAccessKey(ACCESS_KEY), accessKey.get());
        verify(baseStoreService).getCurrentBaseStore();
    }

    @Test
    void testFollowupSendRequestSuccess() {
        mockBaseStoreAccessKey();

        final String response = novalnetApiService.followupSendRequest(serverUrl + FOLLOWUP_ENDPOINT, REQUEST_BODY_TRANSACTION,
                baseStore);

        assertEquals(SUCCESS_RESPONSE, response);
        assertEquals(REQUEST_BODY_TRANSACTION, requestBody.get());
        assertEquals(FOLLOWUP_ENDPOINT, requestPath.get());
        assertEquals(encodeExpectedAccessKey(ACCESS_KEY), accessKey.get());
    }

    @Test
    void testFollowupSendRequestWithNullBaseStore() {
        assertThrows(IllegalArgumentException.class,
                () -> novalnetApiService.followupSendRequest(serverUrl + FOLLOWUP_ENDPOINT, EMPTY_JSON, null));
    }

    @Test
    void testConfigureWebhookUrlSuccess() {
        when(baseStore.getDefaultLanguage()).thenReturn(language);
        when(language.getIsocode()).thenReturn(LANGUAGE_ISO);
        when(endpointConfigService.getWebhookConfigureUrl()).thenReturn(serverUrl + WEBHOOK_ENDPOINT);

        final String response = novalnetApiService.configureWebhookUrl(ACTIVATION_KEY, PAYMENT_ACCESS_KEY, WEBHOOK_URL, baseStore);

        assertEquals(SUCCESS_RESPONSE, response);
        assertEquals(WEBHOOK_ENDPOINT, requestPath.get());
        assertTrue(requestBody.get().contains(ACTIVATION_KEY));
        assertTrue(requestBody.get().contains(WEBHOOK_URL));
        assertTrue(requestBody.get().contains(LANGUAGE_JSON));
        assertEquals(encodeExpectedAccessKey(PAYMENT_ACCESS_KEY), accessKey.get());
    }

    @Test
    void testBookTransactionAmountSuccess() {
        mockBaseStoreAccessKey();

        final String response = novalnetApiService.bookTransactionAmount(serverUrl + BOOKING_ENDPOINT, REQUEST_BODY_BOOKING,
                baseStore);

        assertEquals(SUCCESS_RESPONSE, response);
        assertEquals(REQUEST_BODY_BOOKING, requestBody.get());
        assertEquals(BOOKING_ENDPOINT, requestPath.get());
        assertEquals(encodeExpectedAccessKey(ACCESS_KEY), accessKey.get());
    }

    @Test
    void testFetchMerchantDetailsSuccess() {
        mockBaseStoreAccessKey();

        final String response = novalnetApiService.fetchMerchantDetails(serverUrl + MERCHANT_ENDPOINT, REQUEST_BODY_MERCHANT,
                baseStore);

        assertEquals(SUCCESS_RESPONSE, response);
        assertEquals(REQUEST_BODY_MERCHANT, requestBody.get());
        assertEquals(MERCHANT_ENDPOINT, requestPath.get());
        assertEquals(encodeExpectedAccessKey(ACCESS_KEY), accessKey.get());
    }

    private void mockBaseStoreAccessKey() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(ACCESS_KEY);
    }

    private String encodeExpectedAccessKey(final String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
    }
}
