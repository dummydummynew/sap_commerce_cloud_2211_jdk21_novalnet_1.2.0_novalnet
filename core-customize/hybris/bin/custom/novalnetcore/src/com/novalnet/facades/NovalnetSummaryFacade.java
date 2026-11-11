/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades;

import com.fasterxml.jackson.databind.JsonNode;
import de.hybris.platform.commercefacades.order.data.CartData;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The interface Novalnet summary facade.
 */
public interface NovalnetSummaryFacade {
    /**
     * Initiate order call string.
     *
     * @return the string
     */
    String initiateOrderCall();

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
     *
     * Stores the Novalnet payment information and successful authorization on
     * the session cart before order placement.
     *
     * @param commands       the commands
     * @param currentPayment the current payment
     * @param response       the response
     * @param cartData       the cart data
     */
    void prepareCartForPlaceOrder(String commands, String currentPayment, JsonNode response, CartData cartData);

    /**
     * Stores the checkout addresses and selected payment mode on the cart
     * before replenishment scheduling.
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
     */
    void saveOrderData(String commands, String currentPayment, JsonNode response, CartData cartData, String orderCode);


    /**
     * Is guest user boolean.
     *
     * @return the boolean
     */
    boolean isGuestUser();

    /**
     * Handle reference transaction info.
     *
     * @param response           the response
     * @param novalnetCreditCard the novalnet credit card
     */
    void handleReferenceTransactionInfo(JsonNode response, String novalnetCreditCard);

    /**
     * Create google pay request.
     *
     * @param request the request
     */
    void createGooglePayRequest(HttpServletRequest request);


    /**
     * Update order number string.
     *
     * @param cartData       the cart data
     * @param currentPayment the current payment
     * @param languageCode   the language code
     * @return the string
     */
    String paymentCallRequest(CartData cartData, String currentPayment, String languageCode);


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
