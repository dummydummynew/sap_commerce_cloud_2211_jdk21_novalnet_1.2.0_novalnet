/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment;

import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.order.OrderModel;

import java.util.List;


/**
 * Service interface for managing account-related operations.
 */
public interface NovalnetAccountService {


    /**
     * Gets order by code.
     *
     * @param orderCode the order code
     * @return the order by code
     */
    OrderModel getOrderByCode(String orderCode);

    /**
     * Gets novalnet payment info.
     *
     * @param orderCode the order code
     * @return the novalnet payment info
     */
    List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode);
}
