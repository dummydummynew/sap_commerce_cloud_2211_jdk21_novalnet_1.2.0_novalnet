/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novalnet.dto.NovalnetTransactionResult;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.returns.model.ReturnRequestModel;


/**
 * Defines operations for managing Novalnet transactions, cancellations, captures, and refunds.
 */
public interface NovalnetTransactionService {

    /**
     * Cancels the specified order transaction.
     *
     * @param order the order to cancel
     * @return the result of the cancellation operation
     */
    NovalnetTransactionResult cancelOrder(OrderModel order);

    /**
     * Determines whether the specified order can be cancelled.
     *
     * @param order the order to check
     * @return true if the order can be cancelled; otherwise false
     */
    boolean canCancel(OrderModel order);

    /**
     * Captures the transaction associated with the specified order.
     *
     * @param order the order for which the transaction should be captured
     * @return the result of the capture operation
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    NovalnetTransactionResult captureOrder(OrderModel order) throws JsonProcessingException;

    /**
     * Determines whether the specified order can be captured.
     *
     * @param order the order to check
     * @return true if the order can be captured; otherwise false
     */
    boolean canCapture(OrderModel order);

    /**
     * Determines whether a return request can be created for the specified order.
     *
     * @param order the order to check
     * @return true if a return request can be created; otherwise false
     */
    boolean canCreateReturnRequest(OrderModel order);

    /**
     * Determines whether the specified order is eligible for return.
     *
     * @param order the order to check
     * @return true if the order is returnable; otherwise false
     */
    boolean isReturnable(OrderModel order);

    /**
     * Determines whether the specified order is eligible for a refund.
     *
     * @param order the order to check
     * @return true if the order is refundable; otherwise false
     */
    boolean isRefundable(OrderModel order);

    /**
     * Determines whether the specified order has been fully refunded.
     *
     * @param order the order to check
     * @return true if the order has been fully refunded; otherwise false
     */
    boolean isFullyRefunded(OrderModel order);

    /**
     * Processes a refund for the specified return request.
     *
     * @param returnRequest the return request containing the refund information
     * @return the result of the refund operation
     */
    NovalnetTransactionResult refund(ReturnRequestModel returnRequest);

    /**
     * Processes a follow-on refund request.
     *
     * @param request the follow-on refund request
     * @return the result of the refund operation
     * @throws JsonProcessingException if an error occurs while processing JSON
     */
    NovalnetTransactionResult processRefund(FollowOnRefundRequest request) throws JsonProcessingException;

}
