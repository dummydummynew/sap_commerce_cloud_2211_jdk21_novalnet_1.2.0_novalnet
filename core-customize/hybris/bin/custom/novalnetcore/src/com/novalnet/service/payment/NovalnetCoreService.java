/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;

import java.util.List;


/**
 * Implements the novalnet core payment logics
 */
public interface NovalnetCoreService {


    /**
     * Gets payment model.
     *
     * @param paymentInfo the payment info
     * @return the payment model
     */
    NovalnetPaymentInfoModel getPaymentModel(final List<NovalnetPaymentInfoModel> paymentInfo);

    /**
     * Gets order details.
     *
     * @param url       the url
     * @param jsonBody  the json body
     * @param baseStore the base store
     * @return the order details
     * @throws InterruptedException the interrupted exception
     */
    String getOrderDetails(String url, String jsonBody, BaseStoreModel baseStore);

    /**
     * Gets payment details info.
     *
     * @param orderCode the order code
     * @return the payment details info
     */
    List<NovalnetCallbackInfoModel> getPaymentDetailsInfo(String orderCode);

    /**
     * Gets novalnet payment info.
     *
     * @param orderCode the order code
     * @return the novalnet payment info
     */
    List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode);

    /**
     * Gets order by tid.
     *
     * @param tid the tid
     * @return the order by tid
     */
    OrderModel getOrderByTid(String tid);


    /**
     * Gets order.
     *
     * @param orderCode the order code
     * @return the order
     */
    OrderModel getOrder(String orderCode);

    BaseStoreModel getCurrentBaseStore();
}
