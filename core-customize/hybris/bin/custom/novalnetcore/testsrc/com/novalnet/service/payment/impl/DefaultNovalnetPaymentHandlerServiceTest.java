/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.service.checkout.NovalnetCheckoutService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verifyNoInteractions;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetPaymentHandlerServiceTest {
    private static final String UNSUPPORTED_PAYMENT = "unsupportedPayment";

    private DefaultNovalnetPaymentHandlerService service;
    @Mock
    private SessionService sessionService;
    @Mock
    private NovalnetCheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetPaymentHandlerService();
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        ReflectionTestUtils.setField(service, "novalnetCheckoutService", checkoutService);
    }

    @Test
    void shouldReturnEmptyConfigurationForUnsupportedPaymentType() {
        final var result = service.handlePayment(UNSUPPORTED_PAYMENT, null, null, null, null, 1000, null, null);
        assertNotNull(result);
        verifyNoInteractions(sessionService, checkoutService);
    }
}
