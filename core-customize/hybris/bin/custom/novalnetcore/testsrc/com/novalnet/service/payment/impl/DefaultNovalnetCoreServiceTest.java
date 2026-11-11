/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetCoreServiceTest {
    private static final String ORDER_CODE = "ORDER-100";
    private static final String TRANSACTION_ID = "123456789";
    private DefaultNovalnetCoreService service;
    private CartService cartService;
    private FlexibleSearchService flexibleSearchService;
    private ModelService modelService;
    private BaseStoreService baseStoreService;
    private NovalnetDao novalnetDao;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetCoreService();
        cartService = mock(CartService.class);
        flexibleSearchService = mock(FlexibleSearchService.class);
        modelService = mock(ModelService.class);
        baseStoreService = mock(BaseStoreService.class);
        novalnetDao = mock(NovalnetDao.class);
        ReflectionTestUtils.setField(service, "cartService", cartService);
        ReflectionTestUtils.setField(service, "flexibleSearchService", flexibleSearchService);
        ReflectionTestUtils.setField(service, "modelService", modelService);
        ReflectionTestUtils.setField(service, "baseStoreService", baseStoreService);
        ReflectionTestUtils.setField(service, "novalnetDao", novalnetDao);
    }

    @Test
    void shouldReturnCurrentBaseStore() {
        final int workingTimes = 2;
        BaseStoreModel baseStore = mock(BaseStoreModel.class);
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        assertSame(baseStore, service.getCurrentBaseStore());
        assertSame(baseStore, service.getBaseStoreModel());
        verify(baseStoreService, times(workingTimes)).getCurrentBaseStore();
    }

    @Test
    void shouldReturnOrderInfoFromDao() {
        List<OrderModel> orders = List.of(mock(OrderModel.class));
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(orders);
        assertSame(orders, service.getOrderInfoModel(ORDER_CODE));
    }

    @Test
    void shouldReturnPaymentModelForFirstPaymentInfo() {
        final Long pk = 100L;
        NovalnetPaymentInfoModel paymentInfo = mock(NovalnetPaymentInfoModel.class);
        when(paymentInfo.getPk()).thenReturn(de.hybris.platform.core.PK.fromLong(pk));
        when(modelService.get(paymentInfo.getPk())).thenReturn(paymentInfo);
        assertSame(paymentInfo, service.getPaymentModel(List.of(paymentInfo)));
    }

    @Test
    void shouldReturnOrderByLoadingFirstOrderFromDao() {
        OrderModel order = mock(OrderModel.class);
        OrderModel searchOrder = mock(OrderModel.class);
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(List.of(searchOrder));
        when(modelService.get(searchOrder.getPk())).thenReturn(order);
        assertSame(order, service.getOrder(ORDER_CODE));
    }

    @Test
    void shouldReturnDuplicatePaymentInfoFromFlexibleSearch() {
        @SuppressWarnings("unchecked")
        SearchResult<NovalnetPaymentInfoModel> result = mock(SearchResult.class);

        List<NovalnetPaymentInfoModel> paymentInfos = List.of(mock(NovalnetPaymentInfoModel.class));
        doReturn(result).when(flexibleSearchService).search(any(FlexibleSearchQuery.class));
        when(result.getResult()).thenReturn(paymentInfos);
        assertSame(paymentInfos, service.getNovalnetPaymentInfo(ORDER_CODE));
        verify(flexibleSearchService).search(any(FlexibleSearchQuery.class));
    }

    @Test
    void shouldIdentifyGuestCustomer() {
        CartModel cart = mock(CartModel.class);
        CustomerModel customer = mock(CustomerModel.class);
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.GUEST);
        assertTrue(service.isGuestUser());
    }

    @Test
    void shouldIdentifyRegisteredCustomerAsNonGuest() {
        CartModel cart = mock(CartModel.class);
        CustomerModel customer = mock(CustomerModel.class);
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.REGISTERED);
        assertFalse(service.isGuestUser());
    }

    @Test
    void shouldReturnCallbackInfoFromDao() {
        List<NovalnetCallbackInfoModel> callbacks = List.of(mock(NovalnetCallbackInfoModel.class));
        when(novalnetDao.getCallbackInfo(TRANSACTION_ID)).thenReturn(callbacks);
        assertSame(callbacks, service.getCallbackInfo(TRANSACTION_ID));
    }

    @Test
    void shouldReturnPaymentDetailsInfoFromDao() {
        List<NovalnetCallbackInfoModel> callbacks = List.of(mock(NovalnetCallbackInfoModel.class));
        when(novalnetDao.getPaymentDetailsInfo(ORDER_CODE)).thenReturn(callbacks);
        assertSame(callbacks, service.getPaymentDetailsInfo(ORDER_CODE));
    }

    @Test
    void shouldReturnNullWhenNoCallbackExistsForTid() {
        when(novalnetDao.getCallbackInfo(TRANSACTION_ID)).thenReturn(Collections.emptyList());
        assertNull(service.getOrderByTid(TRANSACTION_ID));
        verify(novalnetDao, never()).getOrderInfoModel(anyString());
    }

    @Test
    void shouldReturnNullWhenCallbackHasNoOrderNumber() {
        NovalnetCallbackInfoModel callback = mock(NovalnetCallbackInfoModel.class);
        when(callback.getOrderNo()).thenReturn("");
        when(novalnetDao.getCallbackInfo(TRANSACTION_ID)).thenReturn(List.of(callback));
        assertNull(service.getOrderByTid(TRANSACTION_ID));
        verify(novalnetDao, never()).getOrderInfoModel(anyString());
    }

    @Test
    void shouldReturnNullWhenOrderDoesNotExistForTid() {
        NovalnetCallbackInfoModel callback = mock(NovalnetCallbackInfoModel.class);
        when(callback.getOrderNo()).thenReturn(ORDER_CODE);
        when(novalnetDao.getCallbackInfo(TRANSACTION_ID)).thenReturn(List.of(callback));
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(Collections.emptyList());
        assertNull(service.getOrderByTid(TRANSACTION_ID));
    }

    @Test
    void shouldReturnOrderForTid() {
        NovalnetCallbackInfoModel callback = mock(NovalnetCallbackInfoModel.class);
        OrderModel order = mock(OrderModel.class);
        when(callback.getOrderNo()).thenReturn(ORDER_CODE);
        when(novalnetDao.getCallbackInfo(TRANSACTION_ID)).thenReturn(List.of(callback));
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(List.of(order));
        assertSame(order, service.getOrderByTid(TRANSACTION_ID));
    }
}
