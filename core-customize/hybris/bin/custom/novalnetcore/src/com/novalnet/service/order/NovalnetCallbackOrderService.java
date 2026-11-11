/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.order;

import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.order.OrderModel;
import org.json.JSONObject;

import java.util.Locale;


/**
 * Defines operations for managing Novalnet callback order information and status updates.
 */
public interface NovalnetCallbackOrderService {
    /**
     * Updates the order status and payment status based on the Novalnet payment information.
     *
     * @param orderCode        the code of the order to update
     * @param paymentInfoModel the Novalnet payment information
     * @param order            the order to update
     */
    void updateOrderStatus(String orderCode, NovalnetPaymentInfoModel paymentInfoModel, OrderModel order);


    /**
     * Updates the payment status of the specified order to partially paid.
     *
     * @param orderCode the code of the order to update
     */
    void updatePartPaidStatus(String orderCode);

    /**
     * Updates the payment gateway status of the payment information associated with an order.
     *
     * @param orderCode            the code of the order to update
     * @param paymentGatewayStatus the payment gateway status to set
     */
    void updatePaymentInfo(String orderCode, String paymentGatewayStatus);

    /**
     * Updates the callback transaction information with the callback transaction ID and paid amount.
     *
     * @param callbackTid the callback transaction ID
     * @param originalTid the original transaction ID
     * @param paidAmount  the paid transaction amount
     */
    void updateCallbackInfo(long callbackTid, String originalTid, int paidAmount);

    /**
     * Updates payment information with callback comments and creates an order history entry.
     *
     * @param comments          the callback comments
     * @param orderCode         the code of the associated order
     * @param transactionStatus the transaction status to store
     * @param entryComment      the description for the order history entry
     */
    void updateCallbackComments(String comments, String orderCode, String transactionStatus, String entryComment);

    /**
     * Updates the order with callback comments and transaction status.
     *
     * @param comments          the callback comments
     * @param orderCode         the unique code of the order
     * @param transactionStatus the Novalnet transaction status
     */
    void updateCallbackComments(String comments, String orderCode, String transactionStatus);


    /**
     * Sets the current session language based on the language configured for the order.
     *
     * @param order the order whose language should be used
     */
    void setSessionLanguage(OrderModel order);

    /**
     * Retrieves the locale associated with the language of the specified order.
     *
     * @param order the order whose locale should be retrieved
     * @return the locale associated with the order language
     */
    Locale getLocale(OrderModel order);

    /**
     * Retrieves the localized label for the specified key.
     *
     * @param key the localization key
     * @return the localized label
     */
    String getLabel(String key);

    /**
     * Builds order history notes from the Novalnet transaction information.
     *
     * @param transactionJson the Novalnet transaction details
     * @param formattedAmount the formatted transaction amount
     * @param inputValue      the payment method or input value used for the notes
     * @return the generated order history notes
     */
    String buildOrderHistoryNotes(JSONObject transactionJson, String formattedAmount, String inputValue);

}
