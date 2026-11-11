/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.impl;

import com.novalnet.facades.NovalnetAccountFacade;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.payment.NovalnetAccountService;
import de.hybris.platform.core.model.order.OrderModel;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * The type Default novalnet account facade.
 */
public class DefaultNovalnetAccountFacade implements NovalnetAccountFacade {

    @Resource
    private NovalnetAccountService novalnetAccountService;

    @Override
    public OrderModel getOrder(String orderCode) {
        return novalnetAccountService.getOrderByCode(orderCode);
    }

    @Override
    public List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode) {
        return novalnetAccountService.getNovalnetPaymentInfo(orderCode);
    }
}
