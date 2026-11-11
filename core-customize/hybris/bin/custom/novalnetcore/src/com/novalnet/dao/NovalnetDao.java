/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dao;

import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;

import java.util.List;

/**
 * Defines data access operations for Novalnet transactions and payment information.
 */
public interface NovalnetDao {
    /**
     * Retrieves order information for the specified order code.
     *
     * @param orderCode the unique code of the order
     * @return a list of orders matching the specified order code
     */
    List<OrderModel> getOrderInfoModel(String orderCode);

    /**
     * Retrieves payment reference information for the specified customer and payment type.
     *
     * @param customerNo  the customer number
     * @param paymentType the payment type
     * @return a list of payment reference information matching the specified customer and payment type
     */
    List<NovalnetPaymentRefInfoModel> getPaymentRefInfo(String customerNo, String paymentType);

    /**
     * Retrieves Novalnet payment information for the specified order code.
     *
     * @param orderCode the unique code of the order
     * @return a list of Novalnet payment information associated with the order
     */
    List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode);

    /**
     * Retrieves callback information for the specified transaction ID.
     *
     * @param transactionId the Novalnet transaction ID
     * @return a list of callback information associated with the transaction
     */
    List<NovalnetCallbackInfoModel> getCallbackInfo(String transactionId);

    /**
     * Retrieves payment details for the specified order code.
     *
     * @param orderCode the unique code of the order
     * @return a list of callback information containing payment details for the order
     */
    List<NovalnetCallbackInfoModel> getPaymentDetailsInfo(String orderCode);

    /**
     * Retrieves an order using its transaction ID.
     *
     * @param tid the Novalnet transaction ID
     * @return the order associated with the specified transaction ID
     */
    OrderModel getOrderByTid(String tid);

    /**
     * Retrieves the stored payment token for the specified order.
     *
     * @param order the order for which the stored payment token is retrieved
     * @return the stored payment token associated with the order
     */
    String getStoredPaymentToken(OrderModel order);

    /**
     * Retrieves the currency matching the specified ISO currency code.
     *
     * @param currencyIsoCode the ISO code of the currency
     * @return the currency associated with the specified ISO code
     */
    CurrencyModel getCurrencyForIsoCode(String currencyIsoCode);

    /**
     * Retrieves the most recent order placed by the specified user.
     *
     * @param user the customer
     * @return the latest order, or null if the user has no order
     */
    OrderModel getLastOrderForUser(UserModel user);
}
