/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.payment.NovalnetAccountService;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Updates the account details for the specified customer.
 */
@Service
public class DefaultNovalnetAccountService implements NovalnetAccountService {

    @Resource
    private ModelService modelService;

    @Resource
    private NovalnetDao novalnetDao;

    @Override
    public OrderModel getOrderByCode(String orderCode) {
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);
        OrderModel orderModel = null;
        if (CollectionUtils.isNotEmpty(orderInfoModel)) {
            orderModel = modelService.get(orderInfoModel.get(0).getPk());
        }

        return orderModel;
    }

    /**
     * Retrieves the order associated with the specified order code.
     *
     * @param orderCode the unique order code
     * @return the order model associated with the given order code
     */
    public List<OrderModel> getOrderInfoModel(String orderCode) {
        return novalnetDao.getOrderInfoModel(orderCode);
    }

    @Override
    public List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode) {
        return novalnetDao.getNovalnetPaymentInfo(orderCode);
    }
}
