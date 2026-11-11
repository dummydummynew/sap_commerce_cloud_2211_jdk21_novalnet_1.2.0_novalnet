/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades;

import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;

/**
 * The interface Novalnet core facade.
 */
public interface NovalnetCoreFacade {


    /**
     * Gets order.
     *
     * @param orderCode the order code
     * @return the order
     */
    OrderModel getOrder(String orderCode);

    /**
     * Gets novalnet payment info.
     *
     * @param orderCode the order code
     * @return the novalnet payment info
     */
    List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode);

    /**
     * Gets current base store.
     *
     * @return the current base store
     */
    BaseStoreModel getCurrentBaseStore();
}
