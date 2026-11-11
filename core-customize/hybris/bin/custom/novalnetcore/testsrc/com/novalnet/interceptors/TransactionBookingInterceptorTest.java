/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.interceptors;

import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class TransactionBookingInterceptorTest {
    private static final Integer BOOK_AMOUNT = Integer.valueOf(100);

    @Mock
    private NovalnetTransactionFacade novalnetTransactionFacade;

    @Mock
    private OrderModel orderModel;

    @Mock
    private InterceptorContext interceptorContext;

    @InjectMocks
    private TransactionBookingInterceptor transactionBookingInterceptor;

    @BeforeEach
    public void setUp() {
        given(interceptorContext.isModified(orderModel, "bookAmount")).willReturn(true);
    }

    @Test
    public void shouldBookTransactionWhenBookAmountIsPresent() throws InterceptorException {
        given(orderModel.getBookAmount()).willReturn(BOOK_AMOUNT);

        assertThatCode(() -> transactionBookingInterceptor.onPrepare(orderModel, interceptorContext)).doesNotThrowAnyException();

        verify(novalnetTransactionFacade).bookTransaction(orderModel);
        verifyNoMoreInteractions(novalnetTransactionFacade);
    }


    @Test
    public void shouldSkipWhenBookAmountIsNull() throws InterceptorException {
        given(orderModel.getBookAmount()).willReturn(null);

        assertThatCode(() -> transactionBookingInterceptor.onPrepare(orderModel, interceptorContext)).doesNotThrowAnyException();

        verifyNoInteractions(novalnetTransactionFacade);
    }
}

