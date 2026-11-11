/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dao.impl;

import com.novalnet.dao.NovalnetReplenishmentDao;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import jakarta.annotation.Resource;


/**
 * Default implementation of Novalnet replenishment DAO operations.
 */
public class DefaultNovalnetReplenishmentDao implements NovalnetReplenishmentDao {
    private static final String FIND_REPLENISHMENT_PAYMENT_REFERENCE_QUERY = "SELECT {pk} FROM {NovalnetReplPayRefInfo} "
            + "WHERE {replenishmentOrderId} = ?replenishmentOrderId";

    private static final String FIND_PAYMENT_REFERENCE_BY_PK_AND_CUSTOMER_QUERY = "SELECT {pk} FROM {NovalnetPaymentRefInfo} "
            + "WHERE {pk} = ?paymentReferencePk " + "AND {customerNo} = ?customerNo";

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public NovalnetReplPayRefInfoModel findReplenishmentPaymentReference(final String replenishmentOrderId) {
        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_REPLENISHMENT_PAYMENT_REFERENCE_QUERY);
        query.addQueryParameter("replenishmentOrderId", replenishmentOrderId);
        query.setCount(1);

        final SearchResult<NovalnetReplPayRefInfoModel> result = flexibleSearchService.search(query);
        return result.getResult().isEmpty() ? null : result.getResult().get(0);
    }

    @Override
    public NovalnetPaymentRefInfoModel findPaymentReference(final CartModel cart, final PK paymentReferencePk) {
        final FlexibleSearchQuery query = new FlexibleSearchQuery(FIND_PAYMENT_REFERENCE_BY_PK_AND_CUSTOMER_QUERY);
        query.addQueryParameter("paymentReferencePk", paymentReferencePk);
        query.addQueryParameter("customerNo", cart.getUser().getPk().getLongValue());
        query.setCount(1);
        final SearchResult<NovalnetPaymentRefInfoModel> result = flexibleSearchService.search(query);
        return result.getResult().isEmpty() ? null : result.getResult().get(0);
    }
}
