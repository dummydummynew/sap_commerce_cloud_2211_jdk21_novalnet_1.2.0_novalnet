/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetGuaranteeValidationServiceTest {
    private static final String PAYMENT_NAME = "novalnetGuaranteedInvoice";
    private static final String DOB = "01.01.1990";
    private static final String DELIVERY_STREET = "Test Street";

    private DefaultNovalnetGuaranteeValidationService service;
    @Mock
    private SessionService sessionService;
    @Mock
    private NovalnetPaymentDetailsForm form;
    @Mock
    private AddressData deliveryAddress;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetGuaranteeValidationService();
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
    }

    @Test
    void shouldRejectNullPaymentDetails() {
        assertThrows(IllegalArgumentException.class,
                () -> service.handleGuaranteeProcess(PAYMENT_NAME, DOB, null, deliveryAddress));
    }

    @Test
    void shouldRejectNullDeliveryAddress() {
        assertThrows(IllegalArgumentException.class, () -> service.handleGuaranteeProcess(PAYMENT_NAME, DOB, form, null));
    }

    @Test
    void shouldReturnBillingAddressMismatchError() {
        when(form.isUseDeliveryAddress()).thenReturn(Boolean.FALSE);
        when(form.getBillTo_street1()).thenReturn("Different Street");
        when(deliveryAddress.getLine1()).thenReturn(DELIVERY_STREET);

        assertEquals("novalnet.address.error", service.handleGuaranteeProcess(PAYMENT_NAME, DOB, form, deliveryAddress));
    }

    @Test
    void shouldReturnSessionGuaranteeErrorBeforeDobValidation() {
        when(form.isUseDeliveryAddress()).thenReturn(Boolean.TRUE);
        when(sessionService.getAttribute(PAYMENT_NAME + "GuaranteeError")).thenReturn("guarantee.failed");

        assertEquals("guarantee.failed", service.handleGuaranteeProcess(PAYMENT_NAME, DOB, form, deliveryAddress));
    }

    @Test
    void shouldReturnDobRequiredErrorWhenDobIsEmpty() {
        when(form.isUseDeliveryAddress()).thenReturn(Boolean.TRUE);
        when(sessionService.getAttribute(PAYMENT_NAME + "GuaranteeError")).thenReturn(null);

        assertEquals("novalnet.dob.error", service.handleGuaranteeProcess(PAYMENT_NAME, "", form, deliveryAddress));
    }
}
