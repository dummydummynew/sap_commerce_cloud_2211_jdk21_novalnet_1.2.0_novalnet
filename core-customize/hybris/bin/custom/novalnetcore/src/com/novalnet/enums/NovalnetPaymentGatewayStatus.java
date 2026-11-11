/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.enums;

import java.util.Arrays;
import java.util.Optional;

/**
 * Represents the transaction statuses supported by the Novalnet payment gateway.
 */
public enum NovalnetPaymentGatewayStatus {

    PENDING, ON_HOLD, CONFIRMED, FAILURE, DEACTIVATED;

    public static Optional<NovalnetPaymentGatewayStatus> fromValue(String value) {
        if (value == null) {
            return Optional.empty();
        }
        return Arrays.stream(values()).filter(status -> status.name().equalsIgnoreCase(value)).findFirst();
    }

    public boolean isPending() {
        return this == PENDING || this == ON_HOLD;
    }

    public boolean isSuccessful() {
        return this == CONFIRMED;
    }
}
