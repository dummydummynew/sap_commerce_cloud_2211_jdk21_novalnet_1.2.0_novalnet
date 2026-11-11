
/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novalnet.dto.NovalnetTransactionResult;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;


/**
 * Defines operations for managing Novalnet transactions.
 */
public interface NovalnetTransactionFacade {
    /**
     * Cancels a Novalnet transaction for the given order.
     *
     * @param order the order whose transaction should be cancelled
     * @return the result of the cancellation
     */
    NovalnetTransactionResult cancelOrder(OrderModel order);

    /**
     * Determines whether the transaction for the given order can be cancelled.
     *
     * @param order the order to check
     * @return true if the order can be cancelled; otherwise false
     */
    boolean canCancel(OrderModel order);

    /**
     * Captures a Novalnet transaction for the given order.
     *
     * @param order the order whose transaction should be captured
     * @return the result of the capture operation
     * @throws JsonProcessingException if an error occurs while processing JSON data
     */
    NovalnetTransactionResult captureOrder(OrderModel order) throws JsonProcessingException;

    /**
     * Captures the transaction for the given order.
     *
     * @param order the order to capture
     * @return the result of the capture operation
     * @throws JsonProcessingException if an error occurs while processing JSON data
     */
    boolean canCapture(OrderModel order);

    /**
     * Determines whether a return request can be created for the given order.
     *
     * @param order the order to check
     * @return true if a return request can be created; otherwise false
     */
    boolean canCreateReturnRequest(OrderModel order);

    /**
     * Refunds a return request through Novalnet.
     *
     * @param returnRequest the return request to refund
     * @return the result of the refund operation
     */
    NovalnetTransactionResult refund(ReturnRequestModel returnRequest);

    /**
     * Processes the follow-on refund request.
     *
     * @param request the follow-on refund request
     * @return the result of the refund operation
     * @throws JsonProcessingException if an error occurs while processing JSON data
     */
    NovalnetTransactionResult processRefund(FollowOnRefundRequest request) throws JsonProcessingException;

    /**
     * Books a Novalnet transaction for the given order.
     *
     * @param order the order for which the transaction should be booked
     * @throws InterceptorException if an error occurs while booking the transaction
     */
    void bookTransaction(OrderModel order) throws InterceptorException;

}


