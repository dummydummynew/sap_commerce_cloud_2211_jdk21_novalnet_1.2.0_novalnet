/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dto;

public class PaymentConfigResult {
    private int testMode = 0;
    private boolean verifyPaymentData = false;
    private boolean zeroAmountBooking = false;
    private boolean redirect = false;

    public int getTestMode() {
        return testMode;
    }

    public void setTestMode(final int testMode) {
        this.testMode = testMode;
    }

    public boolean isVerifyPaymentData() {
        return verifyPaymentData;
    }

    public void setVerifyPaymentData(final boolean verifyPaymentData) {
        this.verifyPaymentData = verifyPaymentData;
    }

    public boolean isZeroAmountBooking() {
        return zeroAmountBooking;
    }

    public void setZeroAmountBooking(final boolean zeroAmountBooking) {
        this.zeroAmountBooking = zeroAmountBooking;
    }

    public boolean isRedirect() {
        return redirect;
    }

    public void setRedirect(final boolean redirect) {
        this.redirect = redirect;
    }
}
