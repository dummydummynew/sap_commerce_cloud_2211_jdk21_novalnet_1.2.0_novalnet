/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import de.hybris.platform.commercefacades.order.data.CartData;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The interface Novalnet summary service.
 */
public interface NovalnetSummaryService {

    /**
     * Send request string.
     *
     * @return the string
     */
    String sendRequest();

    /**
     * Form summary commands string.
     *
     * @param response       the response
     * @param currentPayment the current payment
     * @param cartData       the cart data
     * @return the string
     */
    String formSummaryCommands(JsonNode response, String currentPayment, CartData cartData);

    /**
     * Stores the Novalnet payment information and successful authorization on
     * the session cart so B2B checkout validation can place the order.
     *
     * @param commands       the commands
     * @param currentPayment the current payment
     * @param response       the response
     * @param cartData       the cart data
     */
    void prepareCartForPlaceOrder(String commands, String currentPayment, JsonNode response, CartData cartData);

    /**
     * Stores the selected billing address and payment mode on the session cart
     * before the cart is converted into a replenishment schedule.
     *
     * @param currentPayment the current payment
     */
    void prepareCartForReplenishment(String currentPayment);

    /**
     * Save order data order data.
     *
     * @param commands       the commands
     * @param currentPayment the current payment
     * @param response       the response
     * @param cartData       the cart data
     * @param orderCode      the order code
     * @return the order data
     */
    void saveOrderData(String commands, String currentPayment, JsonNode response, CartData cartData, String orderCode);


    /**
     * Is guest user boolean.
     *
     * @return the boolean
     */
    Boolean isGuestUser();

    /**
     * Handle reference transaction info.
     *
     * @param response       the response
     * @param currentPayment the current payment
     */
    void handleReferenceTransactionInfo(JsonNode response, String currentPayment);

    /**
     * Google pay request.
     *
     * @param request the request
     */
    void googlePayRequest(HttpServletRequest request);

    /**
     * Update order n umber to novalnet string.
     *
     * @param cartData       the cart data
     * @param currentPayment the current payment
     * @param languageCode   the language code
     * @return the string
     */
    String paymentRequestOrUpdateOrder(CartData cartData, String currentPayment, String languageCode);


    /**
     * Update transaction.
     *
     * @param tid          the tid
     * @param orderCode    the order code
     * @param languageCode the language code
     */
    void updateTransaction(String tid, String orderCode, String languageCode);

    /**
     * Gets transaction details.
     *
     * @param tid          the tid
     * @param languageCode the language code
     * @return the transaction details
     */
    String getTransactionDetails(String tid, String languageCode);
}
