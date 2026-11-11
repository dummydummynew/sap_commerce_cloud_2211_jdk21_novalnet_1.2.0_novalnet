/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.CountryModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetTransactionBookingServiceTest {
    private static final String ORDER_CODE = "ORDER-100";
    private static final String PAYMENT_PROVIDER = "novalnetCreditCard";

    @Mock
    private NovalnetApiService novalnetApiService;

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private NovalnetDao novalnetDao;

    @Mock
    private NovalnetCallbackOrderService novalnetCallbackOrderService;

    @Mock
    private OrderModel order;

    @Mock
    private NovalnetPaymentInfoModel paymentInfo;

    @Mock
    private AddressModel paymentAddress;

    @Mock
    private BaseStoreModel store;

    @InjectMocks
    private DefaultNovalnetTransactionBookingService service;

    @Test
    void shouldRejectZeroBookingAmount() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(order.getBookAmount()).thenReturn(Integer.valueOf(0));

        InterceptorException exception = assertThrows(InterceptorException.class, () -> service.bookTransaction(order));

        assertTrue(exception.getMessage().contains("Invalid booking amount for order " + ORDER_CODE));

        verify(novalnetApiService, never()).bookTransactionAmount(anyString(), anyString(), any());
    }

    @Test
    void shouldRejectNegativeBookingAmount() {
        final Integer bookingAmount = -100;
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(order.getBookAmount()).thenReturn(bookingAmount);

        assertThrows(InterceptorException.class, () -> service.bookTransaction(order));

        verify(novalnetDao, never()).getStoredPaymentToken(order);
    }

    @Test
    void shouldRejectOrderWithoutUsableBillingOrDeliveryAddress() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(order.getBookAmount()).thenReturn(Integer.valueOf(1000));
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_PROVIDER);
        when(order.getPaymentAddress()).thenReturn(null);
        when(order.getDeliveryAddress()).thenReturn(null);

        InterceptorException exception = assertThrows(InterceptorException.class, () -> service.bookTransaction(order));

        assertTrue(exception.getMessage().contains("No usable billing address found for order " + ORDER_CODE
                + " (payment, delivery, and saved addresses are all incomplete)"));

        verify(novalnetDao, never()).getStoredPaymentToken(order);
    }

    @Test
    void shouldRejectAddressWithoutRequiredPersonalDetails() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(order.getBookAmount()).thenReturn(Integer.valueOf(1000));
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_PROVIDER);
        when(order.getPaymentAddress()).thenReturn(paymentAddress);
        when(paymentAddress.getCountry()).thenReturn(null);
        when(order.getDeliveryAddress()).thenReturn(null);

        assertThrows(InterceptorException.class, () -> service.bookTransaction(order));

        verify(novalnetDao, never()).getStoredPaymentToken(order);
    }

    @Test
    void shouldRejectWhenStoredPaymentTokenIsMissing() {
        when(order.getCode()).thenReturn(ORDER_CODE);
        when(order.getBookAmount()).thenReturn(Integer.valueOf(1000));
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_PROVIDER);
        when(order.getPaymentAddress()).thenReturn(paymentAddress);
        when(paymentAddress.getCountry()).thenReturn(mock(CountryModel.class));
        when(paymentAddress.getFirstname()).thenReturn("Test");
        when(paymentAddress.getLastname()).thenReturn("User");
        when(paymentAddress.getTown()).thenReturn("Chennai");
        when(paymentAddress.getPostalcode()).thenReturn("600001");
        when(novalnetDao.getStoredPaymentToken(order)).thenReturn("");

        InterceptorException exception = assertThrows(InterceptorException.class, () -> service.bookTransaction(order));

        assertTrue(exception.getMessage().contains("No stored Novalnet payment token found on order " + ORDER_CODE));

        verify(novalnetApiService, never()).bookTransactionAmount(anyString(), anyString(), any());
    }

    @Test
    void shouldMapKnownPaymentProviders() {
        assertEquals("CREDITCARD", DefaultNovalnetTransactionBookingService.getPaymentType("novalnetCreditCard"));

        assertEquals("DIRECT_DEBIT_SEPA", DefaultNovalnetTransactionBookingService.getPaymentType("novalnetDirectDebitSepa"));

        assertEquals("GOOGLEPAY", DefaultNovalnetTransactionBookingService.getPaymentType("novalnetGooglePay"));

        assertEquals("APPLEPAY", DefaultNovalnetTransactionBookingService.getPaymentType("novalnetApplePay"));

        assertEquals("DIRECT_DEBIT_ACH", DefaultNovalnetTransactionBookingService.getPaymentType("novalnetDirectDebitAch"));

        assertEquals("CUSTOM_PAYMENT", DefaultNovalnetTransactionBookingService.getPaymentType("CUSTOM_PAYMENT"));
    }
}
