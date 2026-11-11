/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import de.novalnet.beans.NnCallbackRequestData;

import jakarta.servlet.http.HttpServletRequest;


/**
 * Defines operations for processing Novalnet callback requests and transaction events.
 */
public interface NovalnetCallbackService {

    /**
     * Processes an incoming Novalnet callback request.
     *
     * @param request     the Novalnet callback request data
     * @param httpRequest the HTTP servlet request containing callback request information
     * @return the result of callback processing
     */
    String processCallback(NnCallbackRequestData request, HttpServletRequest httpRequest);
}
