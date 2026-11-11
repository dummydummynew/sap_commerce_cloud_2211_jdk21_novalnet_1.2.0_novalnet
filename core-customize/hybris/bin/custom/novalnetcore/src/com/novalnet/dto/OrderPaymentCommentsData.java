/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dto;

public class OrderPaymentCommentsData {
    private final String orderComments;
    private final String bankDetails;

    public OrderPaymentCommentsData(final String orderComments, final String bankDetails) {
        this.orderComments = orderComments;
        this.bankDetails = bankDetails;
    }

    public String getOrderComments() {
        return orderComments;
    }

    public String getBankDetails() {
        return bankDetails;
    }
}
