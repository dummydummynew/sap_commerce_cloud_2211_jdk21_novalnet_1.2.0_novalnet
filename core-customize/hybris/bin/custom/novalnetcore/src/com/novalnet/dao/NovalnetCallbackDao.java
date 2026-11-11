/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dao;

import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.order.OrderModel;


/**
 * Defines data access operations for Novalnet callback information.
 */
public interface NovalnetCallbackDao {
    /**
     * Finds an order using its unique order code.
     *
     * @param orderCode the unique code of the order
     * @return the order associated with the specified order code
     */
    OrderModel findOrderByCode(String orderCode);

    /**
     * Retrieves the latest Novalnet payment information for an order.
     *
     * @param orderCode the unique code of the order
     * @return the latest Novalnet payment information associated with the order
     */
    NovalnetPaymentInfoModel getLatestNovalnetPaymentInfo(String orderCode);

    /**
     * Finds callback information using the original transaction ID.
     *
     * @param originalTid the original Novalnet transaction ID
     * @return the callback information associated with the original transaction ID
     */
    NovalnetCallbackInfoModel findCallbackInfoByOriginalTid(String originalTid);


}
