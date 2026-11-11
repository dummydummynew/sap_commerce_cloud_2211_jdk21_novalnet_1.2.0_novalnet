/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dto;

public class NovalnetPaymentTypeForm {

    private String paymentType;
    private String costCenterId;
    private String purchaseOrderNumber;
    private String nnPanhash;
    private String nnUniqid;


    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getCostCenterId() {
        return costCenterId;
    }

    public void setCostCenterId(String costCenterId) {
        this.costCenterId = costCenterId;
    }

    public String getPurchaseOrderNumber() {
        return purchaseOrderNumber;
    }

    public void setPurchaseOrderNumber(String purchaseOrderNumber) {
        this.purchaseOrderNumber = purchaseOrderNumber;
    }

    public String getNnPanhash() {
        return nnPanhash;
    }

    public void setNnPanhash(String nnPanhash) {
        this.nnPanhash = nnPanhash;
    }

    public String getNnUniqid() {
        return nnUniqid;
    }

    public void setNnUniqid(String nnUniqid) {
        this.nnUniqid = nnUniqid;
    }

}
