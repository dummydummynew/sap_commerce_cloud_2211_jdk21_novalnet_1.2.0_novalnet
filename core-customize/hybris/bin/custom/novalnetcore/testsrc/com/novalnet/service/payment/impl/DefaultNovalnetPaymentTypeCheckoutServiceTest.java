/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.NovalnetPaymentTypeForm;
import com.novalnet.model.NovalnetCreditCardPaymentModeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bcommercefacades.company.B2BCostCenterFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetPaymentTypeCheckoutServiceTest {
    private static final String PAYMENT_TYPE_ACCOUNT = CheckoutPaymentType.ACCOUNT.getCode();
    private static final String PAYMENT_TYPE_CARD = CheckoutPaymentType.CARD.getCode();
    private static final String PAYMENT_MODE_CARD = "novalnetCreditCard";
    private static final String COST_CENTER_CODE = "COST_CENTER_001";
    private static final String PURCHASE_ORDER_NUMBER = "PO-1001";
    private static final String SESSION_PAYMENT_METHOD_KEY = "selectedPaymentMethodId";

    @Mock
    private SessionService sessionService;

    @Mock
    private B2BCostCenterFacade costCenterFacade;

    @Mock
    private PaymentModeService paymentModeService;

    @Mock
    private NovalnetPaymentTypeForm paymentTypeForm;

    @InjectMocks
    private DefaultNovalnetPaymentTypeCheckoutService service;

    @Test
    void shouldReturnEmptyCostCentersWhenFacadeReturnsNull() {
        when(costCenterFacade.getActiveCostCenters()).thenReturn(null);

        final List<? extends B2BCostCenterData> result = service.getVisibleActiveCostCenters();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(costCenterFacade).getActiveCostCenters();
    }

    @Test
    void shouldReturnActiveCostCenters() {
        final B2BCostCenterData costCenter = new B2BCostCenterData();

        doReturn(Collections.singletonList(costCenter)).when(costCenterFacade).getActiveCostCenters();

        final List<? extends B2BCostCenterData> result = service.getVisibleActiveCostCenters();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(costCenter, result.get(0));

        verify(costCenterFacade).getActiveCostCenters();
    }

    @Test
    void shouldBuildCartDataForAccountPaymentType() {
        final int value = 2;
        when(paymentTypeForm.getPaymentType()).thenReturn(PAYMENT_TYPE_ACCOUNT);
        when(paymentTypeForm.getCostCenterId()).thenReturn(COST_CENTER_CODE);
        when(paymentTypeForm.getPurchaseOrderNumber()).thenReturn(PURCHASE_ORDER_NUMBER);

        final CartData result = service.updateCheckoutCart(paymentTypeForm);

        assertNotNull(result);
        assertNotNull(result.getPaymentType());
        assertEquals(PAYMENT_TYPE_ACCOUNT, result.getPaymentType().getCode());
        assertNotNull(result.getCostCenter());
        assertEquals(COST_CENTER_CODE, result.getCostCenter().getCode());
        assertEquals(PURCHASE_ORDER_NUMBER, result.getPurchaseOrderNumber());

        // The current production implementation calls this twice for ACCOUNT.
        verify(sessionService, times(value)).setAttribute(SESSION_PAYMENT_METHOD_KEY, PAYMENT_TYPE_ACCOUNT);
    }

    @Test
    void shouldBuildCartDataForNonAccountPaymentType() {
        when(paymentTypeForm.getPaymentType()).thenReturn(PAYMENT_TYPE_CARD);
        when(paymentTypeForm.getPurchaseOrderNumber()).thenReturn(PURCHASE_ORDER_NUMBER);

        final CartData result = service.updateCheckoutCart(paymentTypeForm);

        assertNotNull(result);
        assertNotNull(result.getPaymentType());
        assertEquals(PAYMENT_TYPE_CARD, result.getPaymentType().getCode());
        assertEquals(PURCHASE_ORDER_NUMBER, result.getPurchaseOrderNumber());
        assertNull(result.getCostCenter());

        verify(sessionService).setAttribute(SESSION_PAYMENT_METHOD_KEY, PAYMENT_TYPE_CARD);
    }

    @Test
    void shouldReturnFalseForUnsupportedPaymentType() {
        assertFalse(service.paymentIsActive("UNSUPPORTED_PAYMENT"));
    }

    @Test
    void shouldReturnTrueWhenCardPaymentModeIsActive() {
        final NovalnetCreditCardPaymentModeModel cardMode = org.mockito.Mockito.mock(NovalnetCreditCardPaymentModeModel.class);

        when(paymentModeService.getPaymentModeForCode(PAYMENT_MODE_CARD)).thenReturn(cardMode);
        when(cardMode.getActive()).thenReturn(Boolean.TRUE);

        assertTrue(service.paymentIsActive(PAYMENT_TYPE_CARD));

        verify(paymentModeService).getPaymentModeForCode(PAYMENT_MODE_CARD);
    }
}
