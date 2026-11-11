/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.payment.NovalnetPaymentService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetOneClickPaymentServiceTest {
    private static final String UNSUPPORTED_PAYMENT = "unsupportedPayment";

    private DefaultNovalnetOneClickPaymentService service;
    @Mock
    private SessionService sessionService;
    @Mock
    private NovalnetCheckoutService checkoutService;
    @Mock
    private PaymentModeService paymentModeService;
    @Mock
    private NovalnetPaymentService paymentService;
    @Mock
    private DefaultNovalnetGuaranteeValidationService guaranteeValidationService;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetOneClickPaymentService();
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        ReflectionTestUtils.setField(service, "novalnetCheckoutService", checkoutService);
        ReflectionTestUtils.setField(service, "paymentModeService", paymentModeService);
        ReflectionTestUtils.setField(service, "novalnetPaymentService", paymentService);
        ReflectionTestUtils.setField(service, "novalnetGuaranteeValidationService", guaranteeValidationService);
    }

    @Test
    void shouldReturnFalseWhenPaymentTypeIsNull() throws Exception {
        assertFalse(service.processOneClickTokenData(null, null, null, null, null));
    }

    @Test
    void shouldReturnTrueForUnsupportedPaymentType() throws Exception {
        assertTrue(service.processOneClickTokenData(UNSUPPORTED_PAYMENT, null, null, null, null));
    }
}
