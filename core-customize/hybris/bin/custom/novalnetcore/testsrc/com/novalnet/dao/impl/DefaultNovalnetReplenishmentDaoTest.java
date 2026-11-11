/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dao.impl;

import com.novalnet.model.NovalnetPaymentRefInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetReplenishmentDaoTest {
    private static final String REPLENISHMENT_ORDER_ID = "CRONJOB001";

    private static final long CUSTOMER_PK_VALUE = 123456789L;

    private static final PK PAYMENT_REFERENCE_PK = PK.fromLong(987654321L);
    private static final PK CUSTOMER_PK = PK.fromLong(CUSTOMER_PK_VALUE);

    private static final String REPLENISHMENT_QUERY = "SELECT {pk} FROM {NovalnetReplPayRefInfo} WHERE {replenishmentOrderId} = ?replenishmentOrderId";

    private static final String PAYMENT_REFERENCE_QUERY = "SELECT {pk} FROM {NovalnetPaymentRefInfo} WHERE {pk} = ?paymentReferencePk AND {customerNo} = ?customerNo";

    private static final String REPLENISHMENT_ORDER_ID_PARAM = "replenishmentOrderId";
    private static final String PAYMENT_REFERENCE_PK_PARAM = "paymentReferencePk";
    private static final String CUSTOMER_NO_PARAM = "customerNo";

    private static final int EXPECTED_QUERY_COUNT = 1;

    @Mock
    private FlexibleSearchService flexibleSearchService;

    @Mock
    private SearchResult<NovalnetReplPayRefInfoModel> replenishmentSearchResult;

    @Mock
    private SearchResult<NovalnetPaymentRefInfoModel> paymentSearchResult;

    @Mock
    private NovalnetReplPayRefInfoModel replenishmentPaymentReference;

    @Mock
    private NovalnetPaymentRefInfoModel paymentReference;

    @Mock
    private CartModel cart;

    @Mock
    private UserModel user;

    @Captor
    private ArgumentCaptor<FlexibleSearchQuery> queryCaptor;

    @InjectMocks
    private DefaultNovalnetReplenishmentDao dao;

    // ---------- findReplenishmentPaymentReference ----------

    @Test
    void findReplenishmentPaymentReferenceReturnsModelWhenResultExists() {
        when(flexibleSearchService.<NovalnetReplPayRefInfoModel>search(queryCaptor.capture()))
                .thenReturn(replenishmentSearchResult);

        when(replenishmentSearchResult.getResult()).thenReturn(List.of(replenishmentPaymentReference));

        final NovalnetReplPayRefInfoModel result = dao.findReplenishmentPaymentReference(REPLENISHMENT_ORDER_ID);

        assertSame(replenishmentPaymentReference, result);

        final FlexibleSearchQuery query = queryCaptor.getValue();

        assertEquals(REPLENISHMENT_QUERY, query.getQuery());

        assertEquals(Map.of(REPLENISHMENT_ORDER_ID_PARAM, REPLENISHMENT_ORDER_ID), query.getQueryParameters());

        assertEquals(EXPECTED_QUERY_COUNT, query.getCount());
    }

    @Test
    void findReplenishmentPaymentReferenceReturnsNullWhenNoResult() {
        when(flexibleSearchService.<NovalnetReplPayRefInfoModel>search(queryCaptor.capture()))
                .thenReturn(replenishmentSearchResult);

        when(replenishmentSearchResult.getResult()).thenReturn(Collections.emptyList());

        final NovalnetReplPayRefInfoModel result = dao.findReplenishmentPaymentReference(REPLENISHMENT_ORDER_ID);

        assertNull(result);

        assertEquals(REPLENISHMENT_ORDER_ID, queryCaptor.getValue().getQueryParameters().get(REPLENISHMENT_ORDER_ID_PARAM));
    }

    @Test
    void findReplenishmentPaymentReferenceReturnsFirstElementWhenMultipleResults() {
        final NovalnetReplPayRefInfoModel second = org.mockito.Mockito.mock(NovalnetReplPayRefInfoModel.class);

        when(flexibleSearchService.<NovalnetReplPayRefInfoModel>search(queryCaptor.capture()))
                .thenReturn(replenishmentSearchResult);

        when(replenishmentSearchResult.getResult()).thenReturn(List.of(replenishmentPaymentReference, second));

        assertSame(replenishmentPaymentReference, dao.findReplenishmentPaymentReference(REPLENISHMENT_ORDER_ID));
    }

    // ---------- findPaymentReference ----------

    @Test
    void findPaymentReferenceReturnsModelWhenResultExists() {
        when(cart.getUser()).thenReturn(user);
        when(user.getPk()).thenReturn(CUSTOMER_PK);

        when(flexibleSearchService.<NovalnetPaymentRefInfoModel>search(queryCaptor.capture())).thenReturn(paymentSearchResult);

        when(paymentSearchResult.getResult()).thenReturn(List.of(paymentReference));

        final NovalnetPaymentRefInfoModel result = dao.findPaymentReference(cart, PAYMENT_REFERENCE_PK);

        assertSame(paymentReference, result);

        final FlexibleSearchQuery query = queryCaptor.getValue();

        assertEquals(PAYMENT_REFERENCE_QUERY, query.getQuery());

        assertEquals(PAYMENT_REFERENCE_PK, query.getQueryParameters().get(PAYMENT_REFERENCE_PK_PARAM));

        assertEquals(CUSTOMER_PK_VALUE, query.getQueryParameters().get(CUSTOMER_NO_PARAM));

        assertEquals(EXPECTED_QUERY_COUNT, query.getCount());
    }

    @Test
    void findPaymentReferenceReturnsNullWhenNoResult() {
        when(cart.getUser()).thenReturn(user);
        when(user.getPk()).thenReturn(CUSTOMER_PK);

        when(flexibleSearchService.<NovalnetPaymentRefInfoModel>search(queryCaptor.capture())).thenReturn(paymentSearchResult);

        when(paymentSearchResult.getResult()).thenReturn(Collections.emptyList());

        final NovalnetPaymentRefInfoModel result = dao.findPaymentReference(cart, PAYMENT_REFERENCE_PK);

        assertNull(result);

        assertEquals(CUSTOMER_PK_VALUE, queryCaptor.getValue().getQueryParameters().get(CUSTOMER_NO_PARAM));
    }

    @Test
    void findPaymentReferenceThrowsNullPointerExceptionWhenCartHasNoUser() {
        when(cart.getUser()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> dao.findPaymentReference(cart, PAYMENT_REFERENCE_PK));

        verifyNoMoreInteractions(flexibleSearchService);
    }
}
