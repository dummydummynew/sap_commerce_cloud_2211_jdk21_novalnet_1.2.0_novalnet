/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dao.impl;

import com.novalnet.dao.NovalnetCallbackDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import jakarta.annotation.Resource;


/**
 *
 * Default implementation of {@link NovalnetCallbackDao} for accessing callback and order data.
 */
public class DefaultNovalnetCallbackDao implements NovalnetCallbackDao {
    private static final String FIND_ORDER_BY_CODE = "SELECT {pk} FROM {Order AS o} WHERE {o.code}=?code ";

    private static final String FIND_LATEST_NOVALNET_PAYMENT_INFO = "SELECT {p.pk} " + "FROM {NovalnetPaymentInfo AS p} "
            + "WHERE {p.code} = ?code " + "ORDER BY {p.creationtime} DESC LIMIT 1";

    private static final String FIND_CALLBACK_INFO = "SELECT {pk} FROM {NovalnetCallbackInfo AS c} " + "WHERE {c.orginalTid}=?tid";

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public OrderModel findOrderByCode(String orderCode) {
        FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_ORDER_BY_CODE);

        query.addQueryParameter("code", orderCode);

        SearchResult<OrderModel> result = flexibleSearchService.search(query);

        return result.getResult().isEmpty() ? null : result.getResult().get(0);
    }

    @Override
    public NovalnetPaymentInfoModel getLatestNovalnetPaymentInfo(String orderCode) {
        FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_LATEST_NOVALNET_PAYMENT_INFO);

        query.addQueryParameter("code", orderCode);

        return flexibleSearchService.searchUnique(query);
    }

    @Override
    public NovalnetCallbackInfoModel findCallbackInfoByOriginalTid(String transactionId) {
        FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_CALLBACK_INFO);

        query.addQueryParameter("tid", transactionId);

        return flexibleSearchService.searchUnique(query);
    }
}
