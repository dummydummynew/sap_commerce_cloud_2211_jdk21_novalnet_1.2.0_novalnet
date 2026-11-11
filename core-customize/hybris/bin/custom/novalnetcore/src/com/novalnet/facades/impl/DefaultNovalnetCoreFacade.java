/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.impl;

import com.novalnet.facades.NovalnetCoreFacade;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.payment.NovalnetCoreService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * The type Default novalnet core facade.
 */
public class DefaultNovalnetCoreFacade implements NovalnetCoreFacade {

    @Resource
    private NovalnetCoreService novalnetCoreService;

    @Override
    public OrderModel getOrder(String orderCode) {
        return novalnetCoreService.getOrder(orderCode);
    }

    @Override
    public List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode) {
        return novalnetCoreService.getNovalnetPaymentInfo(orderCode);
    }

    @Override
    public BaseStoreModel getCurrentBaseStore() {
        return novalnetCoreService.getCurrentBaseStore();
    }
}
