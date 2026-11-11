/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import com.novalnet.service.order.NovalnetOrderService;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.service.payment.NovalnetTransactionCommentService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.enums.ConsignmentStatus;
import de.hybris.platform.core.model.order.AbstractOrderEntryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.ordersplitting.model.ConsignmentModel;
import de.hybris.platform.payment.AdapterException;
import de.hybris.platform.payment.PaymentService;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.returns.service.RefundAmountCalculationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetTransactionServiceTest {
    private static final String ORDER_CODE = "ORDER001";
    private static final String NN_ORDER_NO = "NN-ORDER-001";
    private static final String TID = "123456789";
    private static final long ORIGINAL_TID = 123456789L;
    private static final long NEW_TID = 987654321L;
    private static final String CANCEL_URL = "https://pay.example.test/cancel";
    private static final String CAPTURE_URL = "https://pay.example.test/capture";
    private static final String REFUND_URL = "https://pay.example.test/refund";
    private static final String COMMENTS = "Transaction comments";
    private static final String NOVALNET_PAYMENT_MODE = "novalnetCreditCard";
    private static final String OTHER_PAYMENT_MODE = "creditcard";
    private static final String ORDER_NOT_FOUND = "novalnet.order.notfound";
    private static final Double TOTAL_AMOUNT = 100.0;
    private static final String SUCCESS_CANCEL_RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"},\"transaction\":{\"status\":\"DEACTIVATED\",\"order_no\":\""
            + NN_ORDER_NO + "\"}}";
    private static final String SUCCESS_CAPTURE_RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"},\"transaction\":{\"status\":\"CONFIRMED\",\"order_no\":\""
            + NN_ORDER_NO + "\"}}";
    private static final String FAILURE_RESPONSE = "{\"result\":{\"status\":\"FAILURE\",\"status_text\":\"Gateway error\"}}";
    private static final String FULL_REFUND_RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"},\"transaction\":{\"status\":\"CONFIRMED\",\"order_no\":\""
            + NN_ORDER_NO + "\",\"amount\":5000,\"currency\":\"EUR\",\"refund\":{\"tid\":987654321,\"amount\":5000}}}";
    private static final String PARTIAL_REFUND_RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"},\"transaction\":{\"status\":\"CONFIRMED\",\"order_no\":\""
            + NN_ORDER_NO + "\",\"amount\":10000,\"currency\":\"EUR\",\"refund\":{\"tid\":987654321,\"amount\":5000}}}";
    private static final String REFUND_WITHOUT_OBJECT_RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"},\"transaction\":{\"status\":\"CONFIRMED\",\"order_no\":\""
            + NN_ORDER_NO + "\",\"amount\":5000}}";

    @Mock
    private NovalnetPaymentService novalnetPaymentService;

    @Mock
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Mock
    private NovalnetTransactionCommentService novalnetTransactionCommentService;

    @Mock
    private NovalnetDao novalnetDao;

    @Mock
    private NovalnetOrderService novalnetOrderService;

    @Mock
    private NovalnetApiService novalnetApiService;

    @Mock
    private PaymentService paymentService;

    @Mock
    private RefundAmountCalculationService refundAmountCalculationService;

    @Mock
    private ModelService modelService;

    @Mock
    private NovalnetCallbackOrderService novalnetCallbackOrderService;

    @Mock
    private OrderModel order;

    @Mock
    private PaymentModeModel paymentMode;

    @Mock
    private BaseStoreModel baseStore;

    @Mock
    private NovalnetCallbackInfoModel callbackInfo;

    @Mock
    private NovalnetPaymentInfoModel paymentInfo;

    @Mock
    private PaymentInfoModel genericPaymentInfo;

    @Mock
    private ConsignmentModel consignment;

    @Mock
    private AbstractOrderEntryModel orderEntry;

    @Mock
    private PaymentTransactionModel paymentTransaction;

    @Mock
    private PaymentTransactionEntryModel transactionEntry;

    @Mock
    private ReturnRequestModel returnRequest;

    @Mock
    private FollowOnRefundRequest refundRequest;

    private DefaultNovalnetTransactionService service;

    private MockedStatic<NovalnetUtils> novalnetUtils;

    @BeforeEach
    void setUp() {
        service = spy(new DefaultNovalnetTransactionService());

        ReflectionTestUtils.setField(service, "novalnetPaymentService", novalnetPaymentService);
        ReflectionTestUtils.setField(service, "novalnetEndpointConfigService", novalnetEndpointConfigService);
        ReflectionTestUtils.setField(service, "novalnetTransactionCommentService", novalnetTransactionCommentService);
        ReflectionTestUtils.setField(service, "novalnetDao", novalnetDao);
        ReflectionTestUtils.setField(service, "novalnetOrderService", novalnetOrderService);
        ReflectionTestUtils.setField(service, "novalnetApiService", novalnetApiService);
        ReflectionTestUtils.setField(service, "paymentService", paymentService);
        ReflectionTestUtils.setField(service, "refundAmountCalculationService", refundAmountCalculationService);
        ReflectionTestUtils.setField(service, "modelService", modelService);
        ReflectionTestUtils.setField(service, "novalnetCallbackOrderService", novalnetCallbackOrderService);

        novalnetUtils = mockStatic(NovalnetUtils.class, invocation -> "en");
    }

    @AfterEach
    void tearDown() {
        novalnetUtils.close();
    }

    @Test
    void canCancelShouldReturnFalseForNullOrder() {
        assertFalse(service.canCancel(null));
    }

    @Test
    void canCancelShouldReturnFalseWhenPaymentModeIsMissing() {
        when(order.getPaymentMode()).thenReturn(null);

        assertFalse(service.canCancel(order));
        verifyNoInteractions(novalnetDao);
    }

    @Test
    void canCancelShouldReturnFalseForNonNovalnetPayment() {
        when(order.getPaymentMode()).thenReturn(paymentMode);
        when(paymentMode.getCode()).thenReturn(OTHER_PAYMENT_MODE);

        assertFalse(service.canCancel(order));
        verifyNoInteractions(novalnetDao);
    }

    @Test
    void canCancelShouldReturnTrueWhenNovalnetPaymentIsOnHold() {
        stubNovalnetPaymentMode();
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(paymentInfo));
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn("on_hold");

        assertTrue(service.canCancel(order));
    }

    @Test
    void canCancelShouldReturnFalseWhenPaymentIsNotOnHold() {
        stubNovalnetPaymentMode();
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(paymentInfo));
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn("CONFIRMED");

        assertFalse(service.canCancel(order));
    }

    @Test
    void canCancelShouldReturnFalseWhenPaymentInfoListIsNull() {
        stubNovalnetPaymentMode();
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(null);

        assertFalse(service.canCancel(order));
    }

    @Test
    void canCaptureShouldReturnFalseForNullOrder() {
        assertFalse(service.canCapture(null));
    }

    @Test
    void canCaptureShouldReturnFalseForNonNovalnetPayment() {
        when(order.getPaymentMode()).thenReturn(paymentMode);
        when(paymentMode.getCode()).thenReturn(OTHER_PAYMENT_MODE);

        assertFalse(service.canCapture(order));
    }

    @Test
    void canCaptureShouldReturnTrueWhenNovalnetPaymentIsOnHold() {
        stubNovalnetPaymentMode();
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(paymentInfo));
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn("ON_HOLD");

        assertTrue(service.canCapture(order));
    }

    @Test
    void canCaptureShouldReturnFalseWhenPaymentInfoListIsNull() {
        stubNovalnetPaymentMode();
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(null);

        assertFalse(service.canCapture(order));
    }

    @Test
    void cancelOrderShouldFailWhenNoOrderReferenceExists() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getPaymentDetailsInfo(ORDER_CODE)).thenReturn(Collections.emptyList());

        assertResult(service.cancelOrder(order), false, ORDER_NOT_FOUND);
        verifyNoInteractions(novalnetApiService);
    }

    @Test
    void cancelOrderShouldFailWhenOrderReferencesAreNull() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getPaymentDetailsInfo(ORDER_CODE)).thenReturn(null);

        assertResult(service.cancelOrder(order), false, ORDER_NOT_FOUND);
    }

    @Test
    void cancelOrderShouldReturnFailureWhenApiCallThrowsException() {
        stubCallbackInfo();
        when(order.getStore()).thenReturn(baseStore);
        when(novalnetEndpointConfigService.getTransactionCancelUrl()).thenReturn(CANCEL_URL);
        when(novalnetApiService.followupSendRequest(eq(CANCEL_URL), anyString(), eq(baseStore)))
                .thenThrow(new IllegalStateException("boom"));

        assertResult(service.cancelOrder(order), false, "novalnet.order.cancel.failed");
    }

    @Test
    void cancelOrderShouldReturnGatewayStatusTextWhenCancelFails() {
        stubCallbackInfo();
        when(order.getStore()).thenReturn(baseStore);
        when(novalnetEndpointConfigService.getTransactionCancelUrl()).thenReturn(CANCEL_URL);
        when(novalnetApiService.followupSendRequest(eq(CANCEL_URL), anyString(), eq(baseStore))).thenReturn(FAILURE_RESPONSE);

        assertResult(service.cancelOrder(order), false, "Gateway error");
        verifyNoInteractions(novalnetOrderService, novalnetCallbackOrderService);
    }

    @Test
    void cancelOrderShouldUpdateStatusesAndCommentsWhenCancelSucceeds() {
        stubCallbackInfo();
        when(order.getStore()).thenReturn(baseStore);
        when(novalnetEndpointConfigService.getTransactionCancelUrl()).thenReturn(CANCEL_URL);
        when(novalnetApiService.followupSendRequest(eq(CANCEL_URL), anyString(), eq(baseStore)))
                .thenReturn(SUCCESS_CANCEL_RESPONSE);
        when(novalnetDao.getNovalnetPaymentInfo(NN_ORDER_NO)).thenReturn(List.of(paymentInfo));
        when(novalnetTransactionCommentService.buildCancelComments()).thenReturn(COMMENTS);

        assertResult(service.cancelOrder(order), true, COMMENTS);

        verify(novalnetPaymentService).updatePaymentInfo(List.of(paymentInfo), "DEACTIVATED");
        verify(novalnetOrderService).updateCancelStatus(NN_ORDER_NO);
        verify(novalnetCallbackOrderService).updateCallbackComments(COMMENTS, NN_ORDER_NO, "DEACTIVATED");
    }

    @Test
    void captureOrderShouldFailWhenOrderIsNull() throws Exception {
        assertResult(service.captureOrder(null), false, "novalnet.order.null");
    }

    @Test
    void captureOrderShouldFailWhenBaseStoreIsMissing() throws Exception {
        when(order.getStore()).thenReturn(null);

        assertResult(service.captureOrder(order), false, "novalnet.order.capture.failed");
        verifyNoInteractions(novalnetDao);
    }

    @Test
    void captureOrderShouldFailWhenNoOrderReferenceExists() throws Exception {
        when(order.getStore()).thenReturn(baseStore);
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getPaymentDetailsInfo(ORDER_CODE)).thenReturn(Collections.emptyList());

        assertResult(service.captureOrder(order), false, ORDER_NOT_FOUND);
        verifyNoInteractions(novalnetApiService);
    }

    @Test
    void captureOrderShouldReturnGatewayStatusTextWhenCaptureFails() throws Exception {
        stubCallbackInfo();
        when(order.getStore()).thenReturn(baseStore);
        when(novalnetEndpointConfigService.getTransactionCaptureUrl()).thenReturn(CAPTURE_URL);
        when(novalnetApiService.followupSendRequest(eq(CAPTURE_URL), anyString(), eq(baseStore))).thenReturn(FAILURE_RESPONSE);

        assertResult(service.captureOrder(order), false, "Gateway error");
        verifyNoInteractions(novalnetOrderService, novalnetCallbackOrderService);
    }

    @Test
    void captureOrderShouldUpdateStatusesAndCommentsWhenCaptureSucceeds() throws Exception {
        stubCallbackInfo();
        when(order.getStore()).thenReturn(baseStore);
        when(novalnetEndpointConfigService.getTransactionCaptureUrl()).thenReturn(CAPTURE_URL);
        when(novalnetApiService.followupSendRequest(eq(CAPTURE_URL), anyString(), eq(baseStore)))
                .thenReturn(SUCCESS_CAPTURE_RESPONSE);
        when(novalnetDao.getNovalnetPaymentInfo(NN_ORDER_NO)).thenReturn(List.of(paymentInfo));
        when(novalnetPaymentService.getPaymentModel(List.of(paymentInfo))).thenReturn(paymentInfo);
        when(novalnetTransactionCommentService.buildCaptureComments(any())).thenReturn(COMMENTS);

        assertResult(service.captureOrder(order), true, COMMENTS);

        verify(novalnetPaymentService).updatePaymentInfo(List.of(paymentInfo), "CAPTURE");
        verify(novalnetOrderService).updateOrderStatus(NN_ORDER_NO, paymentInfo);
        verify(novalnetCallbackOrderService).updateCallbackComments(COMMENTS, NN_ORDER_NO, "CONFIRMED");
    }

    @Test
    void canCreateReturnRequestShouldReturnFalseForNullOrder() {
        assertFalse(service.canCreateReturnRequest(null));
    }

    @Test
    void canCreateReturnRequestShouldReturnFalseWhenFullyRefunded() {
        doReturn(true).when(service).isFullyRefunded(order);

        assertFalse(service.canCreateReturnRequest(order));
        verify(service, never()).isReturnable(order);
        verify(service, never()).isRefundable(order);
    }

    @Test
    void canCreateReturnRequestShouldReturnTrueWhenReturnableAndRefundable() {
        doReturn(false).when(service).isFullyRefunded(order);
        doReturn(true).when(service).isReturnable(order);
        doReturn(true).when(service).isRefundable(order);

        assertTrue(service.canCreateReturnRequest(order));
    }

    @Test
    void canCreateReturnRequestShouldReturnFalseWhenNotReturnable() {
        doReturn(false).when(service).isFullyRefunded(order);
        doReturn(false).when(service).isReturnable(order);
        doReturn(true).when(service).isRefundable(order);

        assertFalse(service.canCreateReturnRequest(order));
    }

    @Test
    void canCreateReturnRequestShouldReturnFalseWhenNotRefundable() {
        doReturn(false).when(service).isFullyRefunded(order);
        doReturn(true).when(service).isReturnable(order);
        doReturn(false).when(service).isRefundable(order);

        assertFalse(service.canCreateReturnRequest(order));
    }

    @Test
    void isReturnableShouldReturnFalseForNullOrder() {
        assertFalse(service.isReturnable(null));
    }

    @Test
    void isReturnableShouldReturnFalseWhenConsignmentsAreNull() {
        when(order.getConsignments()).thenReturn(null);

        assertFalse(service.isReturnable(order));
    }

    @Test
    void isReturnableShouldReturnFalseWhenEntriesAreNull() {
        when(order.getConsignments()).thenReturn(Set.of(consignment));
        when(order.getEntries()).thenReturn(null);

        assertFalse(service.isReturnable(order));
    }

    @Test
    void isReturnableShouldReturnTrueForShippedConsignment() {
        when(order.getConsignments()).thenReturn(Set.of(consignment));
        when(order.getEntries()).thenReturn(List.of(orderEntry));
        when(consignment.getStatus()).thenReturn(ConsignmentStatus.SHIPPED);

        assertTrue(service.isReturnable(order));
    }

    @Test
    void isReturnableShouldReturnTrueForPickupCompleteConsignment() {
        when(order.getConsignments()).thenReturn(Set.of(consignment));
        when(order.getEntries()).thenReturn(List.of(orderEntry));
        when(consignment.getStatus()).thenReturn(ConsignmentStatus.PICKUP_COMPLETE);

        assertTrue(service.isReturnable(order));
    }

    @Test
    void isReturnableShouldReturnFalseForOtherConsignmentStatus() {
        when(order.getConsignments()).thenReturn(Set.of(consignment));
        when(order.getEntries()).thenReturn(List.of(orderEntry));
        when(consignment.getStatus()).thenReturn(ConsignmentStatus.WAITING);

        assertFalse(service.isReturnable(order));
    }

    @Test
    void isRefundableShouldReturnFalseWhenPaymentInfoIsNotNovalnet() {
        when(order.getPaymentInfo()).thenReturn(genericPaymentInfo);

        assertFalse(service.isRefundable(order));
        verifyNoInteractions(novalnetDao);
    }

    @Test
    void isRefundableShouldReturnFalseWhenPaymentInfoListIsEmpty() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(Collections.emptyList());

        assertFalse(service.isRefundable(order));
    }

    @Test
    void isRefundableShouldReturnTrueWhenStatusIsConfirmed() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(paymentInfo));
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn("confirmed");

        assertTrue(service.isRefundable(order));
    }

    @Test
    void isRefundableShouldReturnFalseWhenStatusIsNotConfirmed() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(paymentInfo));
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn("ON_HOLD");

        assertFalse(service.isRefundable(order));
    }

    @Test
    void isRefundableShouldReturnFalseWhenStatusIsNull() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(paymentInfo));
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(null);

        assertFalse(service.isRefundable(order));
    }

    @Test
    void isFullyRefundedShouldReturnFalseWhenNoPaymentTransactionsExist() {
        when(order.getPaymentTransactions()).thenReturn(Collections.emptyList());

        assertFalse(service.isFullyRefunded(order));
    }

    @Test
    void isFullyRefundedShouldReturnFalseWhenPaymentTransactionsAreNull() {
        when(order.getPaymentTransactions()).thenReturn(null);

        assertFalse(service.isFullyRefunded(order));
    }

    @Test
    void isFullyRefundedShouldReturnFalseWhenNoRefundEntriesExist() {
        when(order.getPaymentTransactions()).thenReturn(List.of(paymentTransaction));
        when(paymentTransaction.getEntries()).thenReturn(List.of(transactionEntry));
        when(order.getTotalPrice()).thenReturn(TOTAL_AMOUNT);

        assertFalse(service.isFullyRefunded(order));
    }

    @Test
    void refundShouldFailWhenReturnRequestIsNull() {
        assertResult(service.refund(null), false, ORDER_NOT_FOUND);
    }

    @Test
    void refundShouldFailWhenReturnRequestHasNoOrder() {
        when(returnRequest.getOrder()).thenReturn(null);

        assertResult(service.refund(returnRequest), false, ORDER_NOT_FOUND);
    }

    @Test
    void refundShouldFailWhenNoPaymentTransactionsExist() {
        when(returnRequest.getOrder()).thenReturn(order);
        when(order.getPaymentTransactions()).thenReturn(Collections.emptyList());

        assertResult(service.refund(returnRequest), false, ORDER_NOT_FOUND);
        verifyNoInteractions(paymentService);
    }

    @Test
    void refundShouldFailWhenRefundAmountIsInvalid() {
        stubReturnRequestWithTransaction();
        when(refundAmountCalculationService.getCustomRefundAmount(returnRequest)).thenReturn(null);
        when(refundAmountCalculationService.getOriginalRefundAmount(returnRequest)).thenReturn(BigDecimal.ZERO);

        assertResult(service.refund(returnRequest), false, "novalnet.refund.amount.invalid");
        verifyNoInteractions(paymentService);
    }

    @Test
    void refundShouldUseCustomAmountWhenPositive() throws Exception {
        final BigDecimal customAmount = new BigDecimal("15.00");

        stubReturnRequestWithTransaction();
        when(refundAmountCalculationService.getCustomRefundAmount(returnRequest)).thenReturn(customAmount);
        when(paymentService.refundFollowOn(paymentTransaction, customAmount)).thenReturn(transactionEntry);
        when(transactionEntry.getTransactionStatus()).thenReturn("ACCEPTED");

        assertResult(service.refund(returnRequest), true, "novalnet.order.refund.success");
        verify(refundAmountCalculationService, never()).getOriginalRefundAmount(returnRequest);
    }

    @Test
    void refundShouldFallBackToOriginalAmountWhenCustomAmountIsNotPositive() throws Exception {
        final BigDecimal originalAmount = new BigDecimal("25.00");

        stubReturnRequestWithTransaction();
        when(refundAmountCalculationService.getCustomRefundAmount(returnRequest)).thenReturn(BigDecimal.ZERO);
        when(refundAmountCalculationService.getOriginalRefundAmount(returnRequest)).thenReturn(originalAmount);
        when(paymentService.refundFollowOn(paymentTransaction, originalAmount)).thenReturn(transactionEntry);
        when(transactionEntry.getTransactionStatus()).thenReturn("ACCEPTED");

        assertResult(service.refund(returnRequest), true, "novalnet.order.refund.success");
    }

    @Test
    void refundShouldFailWhenRefundEntryIsNull() throws Exception {
        final BigDecimal amount = new BigDecimal("10.00");

        stubReturnRequestWithTransaction();
        when(refundAmountCalculationService.getCustomRefundAmount(returnRequest)).thenReturn(amount);
        when(paymentService.refundFollowOn(paymentTransaction, amount)).thenReturn(null);

        assertResult(service.refund(returnRequest), false, "novalnet.order.refund.failed");
    }

    @Test
    void refundShouldFailWhenRefundIsNotAccepted() throws Exception {
        final BigDecimal amount = new BigDecimal("10.00");

        stubReturnRequestWithTransaction();
        when(refundAmountCalculationService.getCustomRefundAmount(returnRequest)).thenReturn(amount);
        when(paymentService.refundFollowOn(paymentTransaction, amount)).thenReturn(transactionEntry);
        when(transactionEntry.getTransactionStatus()).thenReturn("REJECTED");

        assertResult(service.refund(returnRequest), false, "novalnet.order.refund.failed");
    }

    @Test
    void refundShouldFailWhenPaymentServiceThrowsAdapterException() throws Exception {
        final BigDecimal amount = new BigDecimal("10.00");

        stubReturnRequestWithTransaction();
        when(refundAmountCalculationService.getCustomRefundAmount(returnRequest)).thenReturn(amount);
        when(paymentService.refundFollowOn(paymentTransaction, amount)).thenThrow(new AdapterException("failed"));

        assertResult(service.refund(returnRequest), false, "novalnet.order.refund.failed");
    }

    @Test
    void processRefundShouldFailWhenRequestIsNull() throws Exception {
        assertResult(service.processRefund(null), false, "Refund request is null");
    }

    @Test
    void processRefundShouldRejectRequestWithoutTransactionId() throws Exception {
        when(refundRequest.getRequestId()).thenReturn(null);

        assertResult(service.processRefund(refundRequest), false, "Refund TID is missing");
    }

    @Test
    void processRefundShouldRejectRequestWithBlankTransactionId() throws Exception {
        when(refundRequest.getRequestId()).thenReturn("  ");

        assertResult(service.processRefund(refundRequest), false, "Refund TID is missing");
    }

    @Test
    void processRefundShouldRejectNullAmount() throws Exception {
        when(refundRequest.getRequestId()).thenReturn(TID);
        when(refundRequest.getTotalAmount()).thenReturn(null);

        assertResult(service.processRefund(refundRequest), false, "Invalid refund amount");
    }

    @Test
    void processRefundShouldRejectZeroAmount() throws Exception {
        when(refundRequest.getRequestId()).thenReturn(TID);
        when(refundRequest.getTotalAmount()).thenReturn(BigDecimal.ZERO);

        assertResult(service.processRefund(refundRequest), false, "Invalid refund amount");
    }

    @Test
    void processRefundShouldFailWhenOrderIsNotFound() throws Exception {
        stubRefundRequest("50.00");
        when(novalnetDao.getOrderByTid(TID)).thenReturn(null);

        assertResult(service.processRefund(refundRequest), false, "Order not found");
    }

    @Test
    void processRefundShouldFailWhenAmountExceedsOrderTotal() throws Exception {
        stubRefundRequest("150.00");
        when(novalnetDao.getOrderByTid(TID)).thenReturn(order);
        when(order.getTotalPrice()).thenReturn(TOTAL_AMOUNT);

        assertResult(service.processRefund(refundRequest), false, "Refund amount exceeds order total");
        verifyNoInteractions(novalnetApiService);
    }

    @Test
    void processRefundShouldFailWhenBaseStoreIsMissing() throws Exception {
        stubRefundRequest("50.00");
        when(novalnetDao.getOrderByTid(TID)).thenReturn(order);
        when(order.getTotalPrice()).thenReturn(TOTAL_AMOUNT);
        when(order.getStore()).thenReturn(null);

        assertResult(service.processRefund(refundRequest), false, "BaseStore is missing");
    }

    @Test
    void processRefundShouldReturnGatewayStatusTextWhenRefundFails() throws Exception {
        stubRefundRequest("50.00");
        stubRefundOrder();
        when(novalnetApiService.followupSendRequest(eq(REFUND_URL), anyString(), eq(baseStore))).thenReturn(FAILURE_RESPONSE);

        assertResult(service.processRefund(refundRequest), false, "Gateway error");
        verifyNoInteractions(novalnetCallbackOrderService);
    }

    @Test
    void processRefundShouldFailWhenRefundObjectIsMissingInResponse() throws Exception {
        stubRefundRequest("50.00");
        stubRefundOrder();
        when(novalnetApiService.followupSendRequest(eq(REFUND_URL), anyString(), eq(baseStore)))
                .thenReturn(REFUND_WITHOUT_OBJECT_RESPONSE);

        assertResult(service.processRefund(refundRequest), false, "Refund response is invalid");
        verifyNoInteractions(novalnetCallbackOrderService);
    }

    @Test
    void processRefundShouldMarkStatusAsRefundWhenRefundIsFull() throws Exception {
        stubRefundRequest("50.00");
        stubRefundOrder();
        when(novalnetApiService.followupSendRequest(eq(REFUND_URL), anyString(), eq(baseStore))).thenReturn(FULL_REFUND_RESPONSE);
        when(novalnetDao.getNovalnetPaymentInfo(NN_ORDER_NO)).thenReturn(List.of(paymentInfo));
        when(novalnetTransactionCommentService.buildRefundComments(TID, "50.00", "EUR", NEW_TID)).thenReturn(COMMENTS);

        assertResult(service.processRefund(refundRequest), true, COMMENTS);

        verify(novalnetPaymentService).updatePaymentInfo(List.of(paymentInfo), "REFUND");
        verify(novalnetCallbackOrderService).updateCallbackComments(COMMENTS, NN_ORDER_NO, "REFUND");
        verify(novalnetOrderService, never()).updateCancelStatus(anyString());
        verify(modelService, never()).refresh(any(OrderModel.class));
    }

    @Test
    void processRefundShouldMarkStatusAsPartialRefundWhenRefundIsPartial() throws Exception {
        stubRefundRequest("50.00");
        stubRefundOrder();
        when(novalnetApiService.followupSendRequest(eq(REFUND_URL), anyString(), eq(baseStore)))
                .thenReturn(PARTIAL_REFUND_RESPONSE);
        when(novalnetDao.getNovalnetPaymentInfo(NN_ORDER_NO)).thenReturn(List.of(paymentInfo));
        when(novalnetTransactionCommentService.buildRefundComments(TID, "50.00", "EUR", NEW_TID)).thenReturn(COMMENTS);

        assertResult(service.processRefund(refundRequest), true, COMMENTS);

        verify(novalnetPaymentService).updatePaymentInfo(List.of(paymentInfo), "PARTIAL_REFUND");
        verify(novalnetCallbackOrderService).updateCallbackComments(COMMENTS, NN_ORDER_NO, "PARTIAL_REFUND");
        verify(novalnetOrderService, never()).updateCancelStatus(anyString());
        verify(modelService, never()).refresh(any(OrderModel.class));
    }

    private void stubNovalnetPaymentMode() {
        when(order.getPaymentMode()).thenReturn(paymentMode);
        when(paymentMode.getCode()).thenReturn(NOVALNET_PAYMENT_MODE);
    }

    private void stubCallbackInfo() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(novalnetDao.getPaymentDetailsInfo(ORDER_CODE)).thenReturn(List.of(callbackInfo));
        when(callbackInfo.getOrginalTid()).thenReturn(ORIGINAL_TID);
    }

    private void stubReturnRequestWithTransaction() {
        when(returnRequest.getOrder()).thenReturn(order);
        when(order.getPaymentTransactions()).thenReturn(List.of(paymentTransaction));
    }

    private void stubRefundRequest(final String amount) {
        when(refundRequest.getRequestId()).thenReturn(TID);
        when(refundRequest.getTotalAmount()).thenReturn(new BigDecimal(amount));
    }

    private void stubRefundOrder() {
        when(novalnetDao.getOrderByTid(TID)).thenReturn(order);
        when(order.getTotalPrice()).thenReturn(TOTAL_AMOUNT);
        when(order.getStore()).thenReturn(baseStore);
        when(novalnetEndpointConfigService.getTransactionRefundUrl()).thenReturn(REFUND_URL);
    }

    private void assertResult(final NovalnetTransactionResult result, final boolean expectedSuccess, final String expectedMessage) {
        assertEquals(expectedSuccess, result.isSuccess());
        assertEquals(expectedMessage, result.getMessage());
    }
}
