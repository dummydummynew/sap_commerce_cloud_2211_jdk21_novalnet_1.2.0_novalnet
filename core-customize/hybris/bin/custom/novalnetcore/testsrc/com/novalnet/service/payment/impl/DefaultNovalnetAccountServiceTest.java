/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetAccountServiceTest {
    private static final String ORDER_CODE = "ORDER-1001";

    private static final PK ORDER_PK = PK.fromLong(123456789L);

    @Mock
    private ModelService modelService;

    @Mock
    private NovalnetDao novalnetDao;

    @InjectMocks
    private DefaultNovalnetAccountService service;

    @Mock
    private OrderModel order;

    @Mock
    private NovalnetPaymentInfoModel paymentInfo;

    @Test
    void shouldReturnOrderWhenOrderIsFound() {
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(Collections.singletonList(order));

        when(order.getPk()).thenReturn(ORDER_PK);
        when(modelService.get(ORDER_PK)).thenReturn(order);

        final OrderModel result = service.getOrderByCode(ORDER_CODE);

        assertSame(order, result);

        verify(novalnetDao).getOrderInfoModel(ORDER_CODE);
        verify(order).getPk();
        verify(modelService).get(ORDER_PK);
    }

    @Test
    void shouldReturnNullWhenOrderListIsEmpty() {
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(Collections.emptyList());

        final OrderModel result = service.getOrderByCode(ORDER_CODE);

        assertNull(result);

        verify(novalnetDao).getOrderInfoModel(ORDER_CODE);
        verifyNoInteractions(modelService);
    }

    @Test
    void shouldReturnNullWhenOrderListIsNull() {
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(null);

        final OrderModel result = service.getOrderByCode(ORDER_CODE);

        assertNull(result);

        verify(novalnetDao).getOrderInfoModel(ORDER_CODE);
        verifyNoInteractions(modelService);
    }

    @Test
    void shouldReturnOrderInfoModelFromDao() {
        final List<OrderModel> orders = Collections.singletonList(order);

        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(orders);

        final List<OrderModel> result = service.getOrderInfoModel(ORDER_CODE);

        assertEquals(1, result.size());
        assertSame(order, result.get(0));

        verify(novalnetDao).getOrderInfoModel(ORDER_CODE);
    }

    @Test
    void shouldReturnNovalnetPaymentInfoFromDao() {
        final List<NovalnetPaymentInfoModel> paymentInfos = Collections.singletonList(paymentInfo);

        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(paymentInfos);

        final List<NovalnetPaymentInfoModel> result = service.getNovalnetPaymentInfo(ORDER_CODE);

        assertEquals(1, result.size());
        assertSame(paymentInfo, result.get(0));

        verify(novalnetDao).getNovalnetPaymentInfo(ORDER_CODE);
    }

    @Test
    void shouldReturnEmptyPaymentInfoWhenDaoReturnsEmptyList() {
        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(Collections.emptyList());

        final List<NovalnetPaymentInfoModel> result = service.getNovalnetPaymentInfo(ORDER_CODE);

        assertEquals(0, result.size());

        verify(novalnetDao).getNovalnetPaymentInfo(ORDER_CODE);
    }
}
