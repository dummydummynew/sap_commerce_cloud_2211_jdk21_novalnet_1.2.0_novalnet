/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dto;

import de.hybris.platform.commercefacades.user.data.AddressData;


/**
 * NovalnetPaymentInfoData
 */
public class NovalnetPaymentInfoData implements java.io.Serializable {
    private String transactionDetails;

    private String paymentName;

    private AddressData billingAddress;

    private String paymentEmailAddress;

    private String orderHistoryNotes;

    private String paymentGatewayStatus;

    private String id;

    /**
     * @return the transactionDetails
     */
    public String getTransactionDetails() {
        return transactionDetails;
    }

    /**
     * @param transactionDetails the transactionDetails to set
     */
    public void setTransactionDetails(final String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    /**
     * @return the paymentName
     */
    public String getPaymentName() {
        return paymentName;
    }

    /**
     * @param paymentName the paymentName to set
     */
    public void setPaymentName(final String paymentName) {
        this.paymentName = paymentName;
    }

    /**
     * @return the billingAddress
     */
    public AddressData getBillingAddress() {
        return billingAddress;
    }

    /**
     * @param billingAddress the billingAddress to set
     */
    public void setBillingAddress(final AddressData billingAddress) {
        this.billingAddress = billingAddress;
    }

    /**
     * @return the paymentEmailAddress
     */
    public String getPaymentEmailAddress() {
        return paymentEmailAddress;
    }

    /**
     * @param paymentEmailAddress the paymentEmailAddress to set
     */
    public void setPaymentEmailAddress(final String paymentEmailAddress) {
        this.paymentEmailAddress = paymentEmailAddress;
    }

    /**
     * @return the orderHistoryNotes
     */
    public String getPaymentGatewayStatus() {
        return paymentGatewayStatus;
    }

    public void setPaymentGatewayStatus(final String paymentGatewayStatus) {
        this.paymentGatewayStatus = paymentGatewayStatus;
    }

    /**
     * @return the orderHistoryNotes
     */
    public String getOrderHistoryNotes() {
        return orderHistoryNotes;
    }

    /**
     * @param orderHistoryNotes the orderHistoryNotes to set
     */
    public void setOrderHistoryNotes(final String orderHistoryNotes) {
        this.orderHistoryNotes = orderHistoryNotes;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param orderHistoryNidotes the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }
}

