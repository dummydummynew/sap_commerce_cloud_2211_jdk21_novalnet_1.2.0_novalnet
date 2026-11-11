/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;


/**
 * Defines operations for booking Novalnet transactions.
 */
public interface NovalnetTransactionBookingService {
    /**
     * Books a Novalnet transaction for the specified order.
     *
     * @param order the order for which the transaction is booked
     * @throws InterceptorException if an error occurs while booking the transaction
     */
    void bookTransaction(OrderModel order) throws InterceptorException;
}
