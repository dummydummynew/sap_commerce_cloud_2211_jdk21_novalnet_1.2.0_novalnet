/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.order;

import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;


/**
 * Defines operations for managing Novalnet order status and callback information.
 */
public interface NovalnetOrderService {
    /**
     * Updates the order status based on the provided Novalnet payment information.
     *
     * @param orderCode the unique code of the order
     * @param paymentInfoModel the Novalnet payment information
     */
    void updateOrderStatus(String orderCode, NovalnetPaymentInfoModel paymentInfoModel);

    /**
     * Updates the order status to cancelled.
     *
     * @param orderCode the unique code of the order
     */
    void updateCancelStatus(String orderCode);

    /**
     * Updates the order status based on the callback payment method.
     *
     * @param orderCode     the unique code of the order
     * @param paymentMethod the payment method received in the callback
     */
    void updateCallbackOrderStatus(String orderCode, String paymentMethod);

    /**
     * Updates the order status to indicate a partial payment.
     *
     * @param orderCode the unique code of the order
     */
    void updatePartPaidStatus(String orderCode);

    /**
     * Retrieves an order using its order code.
     *
     * @param orderCode the unique code of the order
     * @return the order matching the specified order code
     */
    OrderModel getOrder(String orderCode);

    /**
     * Determines the order status based on the Novalnet payment information and base store.
     *
     * @param paymentInfoModel the Novalnet payment information
     * @param baseStore        the base store associated with the order
     * @return the corresponding order status
     */
    OrderStatus getOrderStatus(NovalnetPaymentInfoModel paymentInfoModel, BaseStoreModel baseStore);

}
