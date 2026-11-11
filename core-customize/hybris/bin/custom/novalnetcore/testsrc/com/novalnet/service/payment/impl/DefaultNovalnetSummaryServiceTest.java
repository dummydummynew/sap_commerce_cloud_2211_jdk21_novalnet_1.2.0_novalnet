/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetSummaryServiceTest {
    private static final String PAYMENT_URL = "https://payment.example.test";
    private static final String PAYMENT_REQUEST = "{\"transaction\":{}}";
    private static final String ORDER_CODE = "ORDER-100";
    private static final String API_RESPONSE = "success";

    private DefaultNovalnetSummaryService service;

    @Mock
    private SessionService sessionService;
    @Mock
    private NovalnetEndpointConfigService endpointConfigService;
    @Mock
    private NovalnetApiService apiService;
    @Mock
    private NovalnetCheckoutService checkoutService;
    @Mock
    private NovalnetDao novalnetDao;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetSummaryService();
        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        ReflectionTestUtils.setField(service, "novalnetEndpointConfigService", endpointConfigService);
        ReflectionTestUtils.setField(service, "novalnetApiService", apiService);
        ReflectionTestUtils.setField(service, "novalnetCheckoutService", checkoutService);
        ReflectionTestUtils.setField(service, "novalnetDao", novalnetDao);
    }

    @Test
    void shouldSendRequestUsingConfiguredPaymentUrlWhenSessionUrlIsMissing() {
        when(sessionService.getAttribute("paymentRequest")).thenReturn(PAYMENT_REQUEST);
        when(sessionService.getAttribute("paymentUrl")).thenReturn(null);
        when(endpointConfigService.getPaymentUrl()).thenReturn(PAYMENT_URL);
        when(apiService.sendRequest(PAYMENT_URL, PAYMENT_REQUEST)).thenReturn(API_RESPONSE);

        assertEquals(API_RESPONSE, service.sendRequest());
        verify(apiService).sendRequest(PAYMENT_URL, PAYMENT_REQUEST);
    }

    @Test
    void shouldSendRequestUsingPaymentUrlFromSession() {
        final String sessionUrl = "https://session-payment.example.test";
        when(sessionService.getAttribute("paymentRequest")).thenReturn(PAYMENT_REQUEST);
        when(sessionService.getAttribute("paymentUrl")).thenReturn(sessionUrl);
        when(apiService.sendRequest(sessionUrl, PAYMENT_REQUEST)).thenReturn(API_RESPONSE);

        assertEquals(API_RESPONSE, service.sendRequest());
        verify(apiService).sendRequest(sessionUrl, PAYMENT_REQUEST);
    }

    @Test
    void shouldDelegateGuestUserCheck() {
        when(checkoutService.isGuestUser()).thenReturn(Boolean.TRUE);
        assertTrue(service.isGuestUser());
        verify(checkoutService).isGuestUser();
    }

    @Test
    void shouldReturnOrderInfoFromDao() {
        when(novalnetDao.getOrderInfoModel(ORDER_CODE))
                .thenReturn(Collections.singletonList(org.mockito.Mockito.mock(OrderModel.class)));
        assertEquals(1, service.getOrderInfoModel(ORDER_CODE).size());
        verify(novalnetDao).getOrderInfoModel(ORDER_CODE);
    }

    @Test
    void shouldReturnEmptyOrderInfoWhenDaoReturnsEmptyList() {
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(Collections.emptyList());
        assertTrue(service.getOrderInfoModel(ORDER_CODE).isEmpty());
    }
}
