/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.novalnet.facades.NovalnetSummaryFacade;
import com.novalnet.service.payment.NovalnetSummaryService;
import de.hybris.platform.commercefacades.order.data.CartData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The type Default novalnet summary facade.
 */
public class DefaultNovalnetSummaryFacade implements NovalnetSummaryFacade {

    @Resource
    private NovalnetSummaryService novalnetSummaryService;


    @Override
    public String initiateOrderCall() {
        return novalnetSummaryService.sendRequest();
    }

    @Override
    public String formSummaryCommands(JsonNode response, String currentPayment, CartData cartData) {
        return novalnetSummaryService.formSummaryCommands(response, currentPayment, cartData);
    }

    @Override
    public void prepareCartForPlaceOrder(String commands, String currentPayment, JsonNode response, CartData cartData) {
        novalnetSummaryService.prepareCartForPlaceOrder(commands, currentPayment, response, cartData);
    }

    @Override
    public void prepareCartForReplenishment(String currentPayment) {
        novalnetSummaryService.prepareCartForReplenishment(currentPayment);
    }

    @Override
    public void saveOrderData(String commands, String currentPayment, JsonNode response, CartData cartData, String orderCode) {
        novalnetSummaryService.saveOrderData(commands, currentPayment, response, cartData, orderCode);
    }

    @Override
    public boolean isGuestUser() {
        return novalnetSummaryService.isGuestUser();
    }

    @Override
    public void handleReferenceTransactionInfo(JsonNode response, String novalnetCreditCard) {
        novalnetSummaryService.handleReferenceTransactionInfo(response, novalnetCreditCard);
    }

    @Override
    public void createGooglePayRequest(HttpServletRequest request) {
        novalnetSummaryService.googlePayRequest(request);
    }

    @Override
    public String paymentCallRequest(CartData cartData, String currentPayment, String languageCode) {
        return novalnetSummaryService.paymentRequestOrUpdateOrder(cartData, currentPayment, languageCode);
    }

    @Override
    public void updateTransaction(String tid, String orderCode, String languageCode) {
        novalnetSummaryService.updateTransaction(tid, orderCode, languageCode);
    }

    @Override
    public String getTransactionDetails(String tid, String languageCode) {
        return novalnetSummaryService.getTransactionDetails(tid, languageCode);
    }


}
