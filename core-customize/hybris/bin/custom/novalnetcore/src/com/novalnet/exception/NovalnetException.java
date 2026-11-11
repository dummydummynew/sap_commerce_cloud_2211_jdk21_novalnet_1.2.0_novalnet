/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.exception;


/**
 * Exception thrown when a Novalnet-specific runtime error occurs.
 */
public class NovalnetException extends RuntimeException {
    /**
     * Creates a new {@code NovalnetException} with the specified message and cause.
     *
     * @param message the detail message
     * @param cause   the underlying cause of the exception
     *
     */
    public NovalnetException(String message, Throwable cause) {
        super(message, cause);
    }

}
