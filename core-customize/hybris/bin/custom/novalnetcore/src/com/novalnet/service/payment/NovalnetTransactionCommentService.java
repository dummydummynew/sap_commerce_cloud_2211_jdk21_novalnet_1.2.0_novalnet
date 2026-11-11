/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.novalnet.dto.OrderPaymentCommentsData;
import de.hybris.platform.commercefacades.order.data.CartData;
import org.json.JSONObject;


/**
 * Generates Novalnet order, payment, cancellation, capture, and refund comments.
 */
public interface NovalnetTransactionCommentService {
    /**
     * Build order and payment comments order payment comments data.
     *
     * @param currentPayment        the current payment
     * @param transactionJsonObject the transaction json object
     * @param cartData              the cart data
     * @return the order payment comments data
     */
    OrderPaymentCommentsData buildOrderAndPaymentComments(String currentPayment, JSONObject transactionJsonObject,
                                                          CartData cartData);

    /**
     * Build cancel comments string.
     *
     * @return the string
     */
    String buildCancelComments();

    /**
     * Build capture comments string.
     *
     * @param transaction the transaction
     * @return the string
     */
    String buildCaptureComments(JSONObject transaction);

    /**
     * Build refund comments string.
     *
     * @param tid                   the tid
     * @param formattedRefundAmount the formatted refund amount
     * @param currency              the currency
     * @param newTid                the new tid
     * @return the string
     */
    String buildRefundComments(String tid, String formattedRefundAmount, String currency, long newTid);
}
