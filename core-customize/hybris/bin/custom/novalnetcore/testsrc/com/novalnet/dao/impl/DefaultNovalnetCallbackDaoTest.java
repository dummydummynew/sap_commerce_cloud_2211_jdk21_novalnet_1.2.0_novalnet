/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dao.impl;

import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class DefaultNovalnetCallbackDaoTest {
    private static final String ORDER_CODE = "100001";
    private static final String TID = "14500000012345";

    @Mock
    private FlexibleSearchService flexibleSearchService;

    @Mock
    private SearchResult<OrderModel> orderSearchResult;

    @Mock
    private OrderModel orderModel;

    @Mock
    private NovalnetPaymentInfoModel paymentInfoModel;

    @Mock
    private NovalnetCallbackInfoModel callbackInfoModel;

    @InjectMocks
    private DefaultNovalnetCallbackDao callbackDao;

    @Test
    public void shouldReturnOrderWhenFoundByCode() {
        when(flexibleSearchService.<OrderModel>search(any(FlexibleSearchQuery.class))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(List.of(orderModel));

        assertThat(callbackDao.findOrderByCode(ORDER_CODE)).isSameAs(orderModel);
    }

    @Test
    public void shouldReturnNullWhenOrderNotFoundByCode() {
        when(flexibleSearchService.<OrderModel>search(any(FlexibleSearchQuery.class))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(Collections.emptyList());

        assertThat(callbackDao.findOrderByCode(ORDER_CODE)).isNull();
    }

    @Test
    public void shouldPassOrderCodeAsQueryParameter() {
        when(flexibleSearchService.<OrderModel>search(any(FlexibleSearchQuery.class))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(Collections.emptyList());

        callbackDao.findOrderByCode(ORDER_CODE);

        final ArgumentCaptor<FlexibleSearchQuery> captor = ArgumentCaptor.forClass(FlexibleSearchQuery.class);
        verify(flexibleSearchService).search(captor.capture());
        assertThat(captor.getValue().getQueryParameters()).containsEntry("code", ORDER_CODE);
    }

    @Test
    public void shouldReturnLatestPaymentInfo() {
        when(flexibleSearchService.<NovalnetPaymentInfoModel>searchUnique(any(FlexibleSearchQuery.class)))
                .thenReturn(paymentInfoModel);

        assertThat(callbackDao.getLatestNovalnetPaymentInfo(ORDER_CODE)).isSameAs(paymentInfoModel);

        final ArgumentCaptor<FlexibleSearchQuery> captor = ArgumentCaptor.forClass(FlexibleSearchQuery.class);
        verify(flexibleSearchService).searchUnique(captor.capture());
        assertThat(captor.getValue().getQueryParameters()).containsEntry("code", ORDER_CODE);
    }

    @Test
    public void shouldReturnCallbackInfoByOriginalTid() {
        when(flexibleSearchService.<NovalnetCallbackInfoModel>searchUnique(any(FlexibleSearchQuery.class)))
                .thenReturn(callbackInfoModel);

        assertThat(callbackDao.findCallbackInfoByOriginalTid(TID)).isSameAs(callbackInfoModel);

        final ArgumentCaptor<FlexibleSearchQuery> captor = ArgumentCaptor.forClass(FlexibleSearchQuery.class);
        verify(flexibleSearchService).searchUnique(captor.capture());
        assertThat(captor.getValue().getQueryParameters()).containsEntry("tid", TID);
    }
}
