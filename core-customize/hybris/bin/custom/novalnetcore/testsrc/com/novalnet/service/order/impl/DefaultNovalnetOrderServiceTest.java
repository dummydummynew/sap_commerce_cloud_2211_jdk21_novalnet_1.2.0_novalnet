/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.order.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetCreditCardPaymentModeModel;
import com.novalnet.model.NovalnetGuaranteedInvoicePaymentModeModel;
import com.novalnet.model.NovalnetInvoicePaymentModeModel;
import static org.assertj.core.api.Assertions.assertThat;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetPrepaymentPaymentModeModel;
import com.novalnet.model.NovalnetPrzelewy24PaymentModeModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetOrderServiceTest {
    private static final String ORDER_CODE = "100001";
    private static final String PAYMENT_NOVALNET_INVOICE = "novalnetInvoice";
    private static final String PAYMENT_NOVALNET_CREDIT_CARD = "novalnetCreditCard";
    private static final String PAYMENT_NOVALNET_GUARANTEED_INVOICE = "novalnetGuaranteedInvoice";
    private static final String PAYMENT_NOVALNET_PREPAYMENT = "novalnetPrepayment";
    private static final String PAYMENT_NOVALNET_PRZELEWY24 = "novalnetPrzelewy24";
    private static final String PAYMENT_UNKNOWN = "unknownPayment";
    private static final String GATEWAY_STATUS_PENDING = "PENDING";
    private static final String GATEWAY_STATUS_ON_HOLD = "ON_HOLD";
    private static final String GATEWAY_STATUS_CONFIRMED = "CONFIRMED";

    private static final OrderStatus CONFIGURED_SUCCESS_STATUS = OrderStatus.COMPLETED;
    private static final OrderStatus CONFIGURED_ALTERNATE_STATUS = OrderStatus.READY;

    @Mock
    private ModelService modelService;

    @Mock
    private BaseStoreService baseStoreService;

    @Mock
    private PaymentModeService paymentModeService;

    @Mock
    private NovalnetDao novalnetDao;

    @Mock
    private OrderModel order;

    @Mock
    private NovalnetPaymentInfoModel paymentInfo;

    @Mock
    private BaseStoreModel baseStore;

    @InjectMocks
    private DefaultNovalnetOrderService service;

    private void mockOrderLookup() {
        final PK orderPk = PK.fromLong(100L);
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(List.of(order));
        when(order.getPk()).thenReturn(orderPk);
        when(modelService.get(orderPk)).thenReturn(order);
    }

    @Test
    void shouldUpdateOrderStatusAndMarkPaymentAsPaidForConfirmedCreditCard() {
        mockOrderLookup();
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_CREDIT_CARD);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_CONFIRMED);

        final NovalnetCreditCardPaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetCreditCardPaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_CREDIT_CARD)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        service.updateOrderStatus(ORDER_CODE, paymentInfo);

        verify(order).setStatus(CONFIGURED_SUCCESS_STATUS);
        verify(order).setPaymentStatus(PaymentStatus.PAID);
        verify(modelService).save(order);
    }

    @Test
    void shouldMarkInvoicePaymentAsNotPaidEvenWhenGatewayIsConfirmed() {
        mockOrderLookup();
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_INVOICE);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_CONFIRMED);

        final NovalnetInvoicePaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetInvoicePaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_INVOICE)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        service.updateOrderStatus(ORDER_CODE, paymentInfo);

        verify(order).setStatus(CONFIGURED_SUCCESS_STATUS);
        verify(order).setPaymentStatus(PaymentStatus.NOTPAID);
        verify(modelService).save(order);
    }

    @Test
    void shouldMarkPaymentAsNotPaidWhenGatewayStatusIsPending() {
        mockOrderLookup();
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_CREDIT_CARD);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_PENDING);

        final NovalnetCreditCardPaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetCreditCardPaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_CREDIT_CARD)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        service.updateOrderStatus(ORDER_CODE, paymentInfo);

        verify(order).setPaymentStatus(PaymentStatus.NOTPAID);
        verify(modelService).save(order);
    }

    @Test
    void shouldUpdateOrderStatusToCancelled() {
        mockOrderLookup();

        service.updateCancelStatus(ORDER_CODE);

        verify(order).setStatus(OrderStatus.CANCELLED);
        verify(modelService).save(order);
    }

    @Test
    void shouldUpdateCallbackOrderStatusForInvoice() {
        mockOrderLookup();
        final NovalnetInvoicePaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetInvoicePaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_INVOICE)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetCallbackOrderStatus()).thenReturn(CONFIGURED_ALTERNATE_STATUS);

        service.updateCallbackOrderStatus(ORDER_CODE, PAYMENT_NOVALNET_INVOICE);

        verify(order).setStatus(CONFIGURED_ALTERNATE_STATUS);
        verify(order).setPaymentStatus(PaymentStatus.PAID);
        verify(modelService).save(order);
    }

    @Test
    void shouldUpdateCallbackOrderStatusForUnknownPaymentMethodWithoutChangingExistingStatus() {
        mockOrderLookup();
        final PaymentModeModel paymentMode = org.mockito.Mockito.mock(PaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_UNKNOWN)).thenReturn(paymentMode);

        service.updateCallbackOrderStatus(ORDER_CODE, PAYMENT_UNKNOWN);

        verify(order).setPaymentStatus(PaymentStatus.PAID);
        verify(modelService).save(order);
    }

    @Test
    void shouldMarkOrderAsPartiallyPaid() {
        mockOrderLookup();

        service.updatePartPaidStatus(ORDER_CODE);

        verify(order).setPaymentStatus(PaymentStatus.PARTPAID);
        verify(modelService).save(order);
    }

    @Test
    void shouldReturnPaymentAuthorizedForCreditCardOnHoldStatus() {
        final NovalnetCreditCardPaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetCreditCardPaymentModeModel.class);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_CREDIT_CARD);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_ON_HOLD);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_CREDIT_CARD)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        final OrderStatus result = service.getOrderStatus(paymentInfo, baseStore);

        assertThat(result).isEqualTo(OrderStatus.PAYMENT_AUTHORIZED);
    }

    @Test
    void shouldReturnPaymentNotCapturedForGuaranteedInvoicePendingStatus() {
        final NovalnetGuaranteedInvoicePaymentModeModel paymentMode = org.mockito.Mockito
                .mock(NovalnetGuaranteedInvoicePaymentModeModel.class);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_GUARANTEED_INVOICE);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_PENDING);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_GUARANTEED_INVOICE)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        final OrderStatus result = service.getOrderStatus(paymentInfo, baseStore);

        assertThat(result).isEqualTo(OrderStatus.PAYMENT_NOT_CAPTURED);
    }

    @Test
    void shouldReturnPaymentAuthorizedForGuaranteedInvoiceOnHoldStatus() {
        final NovalnetGuaranteedInvoicePaymentModeModel paymentMode = org.mockito.Mockito
                .mock(NovalnetGuaranteedInvoicePaymentModeModel.class);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_GUARANTEED_INVOICE);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_ON_HOLD);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_GUARANTEED_INVOICE)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        final OrderStatus result = service.getOrderStatus(paymentInfo, baseStore);

        assertThat(result).isEqualTo(OrderStatus.PAYMENT_AUTHORIZED);
    }

    @Test
    void shouldReturnConfiguredSuccessStatusForPrepayment() {
        final NovalnetPrepaymentPaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetPrepaymentPaymentModeModel.class);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_PREPAYMENT);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_PENDING);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_PREPAYMENT)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_ALTERNATE_STATUS);

        final OrderStatus result = service.getOrderStatus(paymentInfo, baseStore);

        assertThat(result).isEqualTo(CONFIGURED_ALTERNATE_STATUS);
    }

    @Test
    void shouldReturnPaymentNotCapturedForPrzelewy24PendingStatus() {
        final NovalnetPrzelewy24PaymentModeModel paymentMode = org.mockito.Mockito.mock(NovalnetPrzelewy24PaymentModeModel.class);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_NOVALNET_PRZELEWY24);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_PENDING);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_NOVALNET_PRZELEWY24)).thenReturn(paymentMode);
        when(paymentMode.getNovalnetOrderSuccessStatus()).thenReturn(CONFIGURED_SUCCESS_STATUS);

        final OrderStatus result = service.getOrderStatus(paymentInfo, baseStore);

        assertThat(result).isEqualTo(OrderStatus.PAYMENT_NOT_CAPTURED);
    }

    @Test
    void shouldReturnCompletedForPaymentMethodWithoutResolver() {
        final PaymentModeModel paymentMode = org.mockito.Mockito.mock(PaymentModeModel.class);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_UNKNOWN);
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_CONFIRMED);
        when(paymentModeService.getPaymentModeForCode(PAYMENT_UNKNOWN)).thenReturn(paymentMode);

        final OrderStatus result = service.getOrderStatus(paymentInfo, baseStore);

        assertThat(result).isEqualTo(OrderStatus.COMPLETED);
    }

    @Test
    void shouldReturnOrderForGivenOrderCode() {
        mockOrderLookup();

        final OrderModel result = service.getOrder(ORDER_CODE);

        assertThat(result).isSameAs(order);
    }
}
