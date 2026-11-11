/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.service.http.NovalnetApiService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetWebhookServiceTest {
    private static final String PRODUCT_KEY = "product-key";
    private static final String PAYMENT_KEY = "payment-key";
    private static final String WEBHOOK_URL = "https://shop.example.test/webhook";
    private static final String API_RESPONSE = "Webhook configured";

    private DefaultNovalnetWebhookService service;
    @Mock
    private NovalnetApiService apiService;
    @Mock
    private BaseStoreModel baseStore;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetWebhookService();
        ReflectionTestUtils.setField(service, "novalnetApiService", apiService);
    }

    @Test
    void shouldDelegateWebhookConfiguration() {
        when(apiService.configureWebhookUrl(PRODUCT_KEY, PAYMENT_KEY, WEBHOOK_URL, baseStore)).thenReturn(API_RESPONSE);
        service.configureWebhook(PRODUCT_KEY, PAYMENT_KEY, WEBHOOK_URL, baseStore);
        verify(apiService).configureWebhookUrl(PRODUCT_KEY, PAYMENT_KEY, WEBHOOK_URL, baseStore);
    }
}
