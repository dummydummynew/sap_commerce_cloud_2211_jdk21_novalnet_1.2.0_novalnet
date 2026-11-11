/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.exception;

import de.hybris.platform.servicelayer.interceptor.InterceptorException;

/**
 * Custom exception used for Novalnet interceptor-related errors.
 */
public class NovalnetInterceptorException extends InterceptorException {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a Novalnet interceptor exception with the specified message.
     *
     * @param message the detail message describing the exception
     */
    public NovalnetInterceptorException(final String message) {
        super(message);
    }

    /**
     * Creates a Novalnet interceptor exception with the specified message and cause.
     *
     * @param message the detail message describing the exception
     * @param cause   the underlying cause of the exception
     */
    public NovalnetInterceptorException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
