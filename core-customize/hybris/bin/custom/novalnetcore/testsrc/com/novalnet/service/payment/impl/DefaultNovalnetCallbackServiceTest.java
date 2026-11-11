/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dao.NovalnetCallbackDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import com.novalnet.service.order.NovalnetOrderService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.novalnet.beans.NnCallbackCollectionData;
import de.novalnet.beans.NnCallbackCustomData;
import de.novalnet.beans.NnCallbackEventData;
import de.novalnet.beans.NnCallbackRequestData;
import de.novalnet.beans.NnCallbackTransactionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetCallbackServiceTest {
    private static final String ORDER_CODE = "ORDER123";
    private static final String TID = "123456789";
    private static final String PARENT_TID = "987654321";
    private static final String STATUS = "CONFIRMED";

    @InjectMocks
    private DefaultNovalnetCallbackService callbackService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private NovalnetCallbackDao novalnetCallbackDao;

    @Mock
    private NovalnetCallbackOrderService novalnetCallbackOrderService;

    @Mock
    private BaseStoreService baseStoreService;

    @Mock
    private NovalnetOrderService novalnetOrderService;

    @Mock
    private OrderModel order;

    @Mock
    private NovalnetCallbackInfoModel callbackInfo;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(callbackService, "currentDate", "03-11-2026, 10:00:00");
    }

    private NnCallbackTransactionData createTransaction() {
        NnCallbackTransactionData transaction = new NnCallbackTransactionData();

        transaction.setOrder_no(ORDER_CODE);
        transaction.setTid(TID);
        transaction.setStatus(STATUS);
        transaction.setPayment_type("CREDITCARD");
        transaction.setAmount("1234");
        transaction.setCurrency("USD");

        return transaction;
    }

    private NnCallbackEventData createEvent(String eventType) {
        NnCallbackEventData event = new NnCallbackEventData();

        event.setType(eventType);
        event.setParent_tid(PARENT_TID);

        return event;
    }

    private NnCallbackRequestData createRequest(String eventType) {
        NnCallbackRequestData request = new NnCallbackRequestData();

        request.setTransaction(createTransaction());
        request.setEvent(createEvent(eventType));
        request.setCustom(new NnCallbackCustomData());

        return request;
    }

    @Test
    void testHandleTransactionCapture() {
        NnCallbackRequestData request = createRequest("TRANSACTION_CAPTURE");

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackConfirmUpdateText")).thenReturn("Payment confirmed on %s");

        String result = callbackService.handleTransactionCapture(request);

        assertTrue(result.contains("Payment confirmed"));

        verify(novalnetCallbackDao).findOrderByCode(ORDER_CODE);
        verify(novalnetCallbackOrderService).setSessionLanguage(order);
        verify(novalnetCallbackOrderService).updatePaymentInfo(ORDER_CODE, STATUS);
        verify(novalnetCallbackOrderService).updateOrderStatus(eq(ORDER_CODE), isNull(), eq(order));
        verify(novalnetCallbackOrderService).updateCallbackComments(eq(result), eq(ORDER_CODE), eq(STATUS), anyString());
    }

    @Test
    void testHandleTransactionCancel() {
        NnCallbackRequestData request = createRequest("TRANSACTION_CANCEL");

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackCancelUpdateText")).thenReturn("Payment cancelled on %s");

        String result = callbackService.handleTransactionCancel(request);

        assertTrue(result.contains("Payment cancelled"));

        verify(novalnetOrderService).updateCancelStatus(ORDER_CODE);
        verify(novalnetCallbackDao).findOrderByCode(ORDER_CODE);
        verify(novalnetCallbackOrderService).setSessionLanguage(order);
        verify(novalnetCallbackOrderService).updateCallbackComments(result, ORDER_CODE, STATUS, result);
    }

    @Test
    void testHandleTransactionUpdateWithConfirmedStatus() {
        NnCallbackRequestData request = createRequest("TRANSACTION_UPDATE");

        request.getTransaction().setUpdate_type("STATUS");

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackConfirmUpdateText")).thenReturn("Payment confirmed on %s");

        String result = callbackService.handleTransactionUpdate(request);

        assertTrue(result.contains("Payment confirmed"));

        verify(novalnetCallbackOrderService).updatePaymentInfo(ORDER_CODE, STATUS);
        verify(novalnetCallbackOrderService).updateOrderStatus(eq(ORDER_CODE), isNull(), eq(order));
        verify(novalnetCallbackOrderService).updateCallbackComments(eq(result), eq(ORDER_CODE), eq(STATUS), anyString());
    }

    @Test
    void testFormatAmount() {
        when(novalnetCallbackOrderService.getLocale(order)).thenReturn(Locale.US);

        String result = callbackService.formatAmount("1234", "USD", order);

        assertEquals("$12.34", result);
    }

    @Test
    void testHandleCredit() {
        final Integer paidAmount = 100;
        final Integer orderAmount = 2000;
        final Integer callBackAmount = 1334;
        NnCallbackRequestData request = createRequest("CREDIT");

        request.getTransaction().setPayment_type("INVOICE_CREDIT");

        when(novalnetCallbackDao.findCallbackInfoByOriginalTid(PARENT_TID)).thenReturn(callbackInfo);

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(callbackInfo.getPaidAmount()).thenReturn(paidAmount);
        when(callbackInfo.getOrderAmount()).thenReturn(orderAmount);

        when(novalnetCallbackOrderService.getLocale(order)).thenReturn(Locale.US);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackCreditText"))
                .thenReturn("Credit received for %s: %s on %s. TID: %s");

        String result = callbackService.handleCredit(request);

        assertTrue(result.contains(PARENT_TID));
        assertTrue(result.contains("$12.34"));

        verify(novalnetCallbackOrderService).updateCallbackInfo(Long.parseLong(TID), PARENT_TID, callBackAmount);
        verify(novalnetCallbackOrderService).updateCallbackComments(result, ORDER_CODE, STATUS, result);
    }

    @Test
    void testHandleRefundWithoutRefundDetails() {
        NnCallbackRequestData request = createRequest("TRANSACTION_REFUND");

        request.getTransaction().setRefund(null);

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLocale(order)).thenReturn(Locale.US);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackRefundTextOrgTid")).thenReturn("Refund for TID %s: %s");

        String result = callbackService.handleRefund(request);

        assertTrue(result.contains(TID));
        assertTrue(result.contains("$0.00"));

        verify(novalnetCallbackOrderService).updateCallbackComments(result, ORDER_CODE, STATUS, result);

        verify(novalnetOrderService, never()).updateCancelStatus(anyString());
    }

    @Test
    void testHandleRefundWhenTransactionIsDeactivated() {
        NnCallbackRequestData request = createRequest("TRANSACTION_REFUND");

        request.getTransaction().setStatus("DEACTIVATED");
        request.getTransaction().setRefund(null);

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLocale(order)).thenReturn(Locale.US);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackRefundTextOrgTid")).thenReturn("Refund for TID %s: %s");

        callbackService.handleRefund(request);

        verify(novalnetOrderService).updateCancelStatus(ORDER_CODE);
    }

    @Test
    void testHandleReminderFirstReminder() {
        NnCallbackRequestData request = createRequest("PAYMENT_REMINDER_1");

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackReminderText")).thenReturn("Payment reminder %s");

        String result = callbackService.handleReminder(request);

        assertEquals("Payment reminder 1", result);

        verify(novalnetCallbackOrderService).updateCallbackComments(result, ORDER_CODE, STATUS, result);
    }

    @Test
    void testHandleCollectionWithReference() {
        NnCallbackRequestData request = createRequest("SUBMISSION_TO_COLLECTION_AGENCY");

        NnCallbackCollectionData collection = new NnCallbackCollectionData();

        collection.setReference("COLLECTION-123");
        request.setCollection(collection);

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackCollectionText")).thenReturn("Submitted to collection agency");

        when(novalnetCallbackOrderService.getLabel("novalnet.callbackCollectionReferenceText")).thenReturn("Reference: %s");

        String result = callbackService.handleCollection(request);

        assertTrue(result.contains("COLLECTION-123"));
        assertTrue(result.contains("Submitted to collection agency"));

        verify(novalnetCallbackOrderService).updateCallbackComments(result, ORDER_CODE, STATUS, result);
    }

    @Test
    void testHandlePayment() {
        NnCallbackRequestData request = createRequest("PAYMENT");

        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        String result = callbackService.handlePayment(request);

        assertEquals("Novalnet webhook script executed. Status updated for initial transaction", result);

        verify(novalnetCallbackOrderService).updatePaymentInfo(ORDER_CODE, STATUS);

        verify(novalnetCallbackOrderService).updateOrderStatus(eq(ORDER_CODE), isNull(), eq(order));
    }
}
