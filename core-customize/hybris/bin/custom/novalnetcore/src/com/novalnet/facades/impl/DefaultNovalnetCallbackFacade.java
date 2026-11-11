/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.impl;

import com.novalnet.facades.NovalnetCallbackFacade;
import com.novalnet.service.payment.NovalnetCallbackService;
import de.novalnet.beans.NnCallbackRequestData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

/**
 * The type Default novalnet call back facade.
 */
public class DefaultNovalnetCallbackFacade implements NovalnetCallbackFacade {

    @Resource
    private NovalnetCallbackService novalnetCallbackService;

    @Override
    public String processCallback(NnCallbackRequestData callbackRequest, HttpServletRequest httpRequest) {
        return novalnetCallbackService.processCallback(callbackRequest, httpRequest);
    }
}
