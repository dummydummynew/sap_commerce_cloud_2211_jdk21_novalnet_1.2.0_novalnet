/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.payment.Transaction;
import com.novalnet.model.NovalnetPrzelewy24PaymentModeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetB2BPaymentMethodServiceTest {
    private static final String PRZELEWY24_CODE = "novalnetPrzelewy24";
    private static final String API_KEY = "test-api-key";
    private DefaultNovalnetB2BPaymentMethodService service;
    private PaymentModeService paymentModeService;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetB2BPaymentMethodService();
        paymentModeService = mock(PaymentModeService.class);
        ReflectionTestUtils.setField(service, "paymentModeService", paymentModeService);
    }

    @Test
    void shouldSetTestModeWhenPaymentModeIsConfiguredForTest() {
        NovalnetPrzelewy24PaymentModeModel paymentMode = mock(NovalnetPrzelewy24PaymentModeModel.class);
        when(paymentMode.getNovalnetTestMode()).thenReturn(Boolean.TRUE);
        when(paymentModeService.getPaymentModeForCode(PRZELEWY24_CODE)).thenReturn(paymentMode);
        Transaction transaction = new Transaction();
        service.handleTestMode(transaction, PRZELEWY24_CODE);
        assertEquals("1", transaction.getTest_mode());
    }

    @Test
    void shouldDisableTestModeWhenPaymentModeIsLive() {
        NovalnetPrzelewy24PaymentModeModel paymentMode = mock(NovalnetPrzelewy24PaymentModeModel.class);
        when(paymentMode.getNovalnetTestMode()).thenReturn(Boolean.FALSE);
        when(paymentModeService.getPaymentModeForCode(PRZELEWY24_CODE)).thenReturn(paymentMode);
        Transaction transaction = new Transaction();
        service.handleTestMode(transaction, PRZELEWY24_CODE);
        assertEquals("0", transaction.getTest_mode());
    }

    @Test
    void shouldTreatNullTestModeAsLive() {
        NovalnetPrzelewy24PaymentModeModel paymentMode = mock(NovalnetPrzelewy24PaymentModeModel.class);
        when(paymentMode.getNovalnetTestMode()).thenReturn(null);
        when(paymentModeService.getPaymentModeForCode(PRZELEWY24_CODE)).thenReturn(paymentMode);
        Transaction transaction = new Transaction();
        service.handleTestMode(transaction, PRZELEWY24_CODE);
        assertEquals("0", transaction.getTest_mode());
    }

    @Test
    void shouldRejectReplenishmentRequestWhenBaseStoreIsMissing() {
        CartModel cart = mock(CartModel.class);
        when(cart.getStore()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.createPaymentRequest(cart, null));
    }

    @Test
    void shouldRejectReplenishmentRequestWhenApiKeyIsMissing() {
        CartModel cart = mock(CartModel.class);
        BaseStoreModel baseStore = mock(BaseStoreModel.class);
        when(cart.getStore()).thenReturn(baseStore);
        when(baseStore.getNovalnetAPIKey()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> service.createPaymentRequest(cart, null));
    }

    @Test
    void shouldRejectUnsupportedReplenishmentPaymentType() {
        CartModel cart = mock(CartModel.class);
        BaseStoreModel baseStore = mock(BaseStoreModel.class);
        when(cart.getStore()).thenReturn(baseStore);
        when(baseStore.getNovalnetAPIKey()).thenReturn(API_KEY);
        com.novalnet.model.NovalnetReplPayRefInfoModel paymentReference = mock(
                com.novalnet.model.NovalnetReplPayRefInfoModel.class);
        when(paymentReference.getPaymentType()).thenReturn("UNSUPPORTED_PAYMENT");
        assertThrows(IllegalArgumentException.class, () -> service.createPaymentRequest(cart, paymentReference));
    }
}
