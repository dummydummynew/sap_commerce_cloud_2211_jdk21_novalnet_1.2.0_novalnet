/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades;

import de.novalnet.beans.NnCallbackRequestData;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The interface Novalnet call back facade.
 */
public interface NovalnetCallbackFacade {

    /**
     * Process callback string.
     *
     * @param callbackRequest the callback request
     * @param httpRequest     the http request
     * @return the string
     */
    String processCallback(NnCallbackRequestData callbackRequest, HttpServletRequest httpRequest);
}
