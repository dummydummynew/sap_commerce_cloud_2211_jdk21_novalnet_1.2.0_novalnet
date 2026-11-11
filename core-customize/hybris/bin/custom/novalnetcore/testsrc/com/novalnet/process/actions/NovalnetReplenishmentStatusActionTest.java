/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.process.actions;

import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.order.NovalnetOrderService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel;
import de.hybris.platform.core.model.ItemModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.helpers.ProcessParameterHelper;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetReplenishmentStatusActionTest {
    private static final String ORDER_CODE = "ORDER001";
    private static final String ORDER_PARAMETER = "order";
    private static final String CART_PARAMETER = "cart";
    private static final String ORDER_HISTORY_NOTES = "Novalnet payment processed";
    private static final String PAYMENT_PROVIDER = "Novalnet";

    @InjectMocks
    private NovalnetReplenishmentStatusAction testObj;

    @Mock
    private NovalnetOrderService novalnetOrderService;

    @Mock
    private ProcessParameterHelper processParameterHelper;

    @Mock
    private ModelService modelService;

    @Mock
    private ReplenishmentProcessModel process;

    @Mock
    private BusinessProcessParameterModel orderParameter;

    @Mock
    private BusinessProcessParameterModel cartParameter;

    @Mock
    private OrderModel order;

    @Mock
    private CartModel cart;

    @Mock
    private NovalnetPaymentInfoModel paymentInfo;

    @Mock
    private PaymentInfoModel standardPaymentInfo;

    @Mock
    private AddressModel billingAddress;

    @Mock
    private PaymentTransactionModel paymentTransaction;

    @BeforeEach
    void setUp() {
        when(processParameterHelper.getProcessParameterByName(process, ORDER_PARAMETER)).thenReturn(orderParameter);
        when(processParameterHelper.getProcessParameterByName(process, CART_PARAMETER)).thenReturn(cartParameter);

        when(orderParameter.getValue()).thenReturn(order);
        when(cartParameter.getValue()).thenReturn(cart);
    }

    @Test
    void executeActionShouldUpdateOrderAndPaymentInformation() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getPaymentAddress()).thenReturn(billingAddress);
        when(order.getPaymentTransactions()).thenReturn(List.of(paymentTransaction));
        when(order.getCode()).thenReturn(ORDER_CODE);

        when(paymentInfo.getPaymentProvider()).thenReturn(PAYMENT_PROVIDER);
        when(paymentInfo.getOrderHistoryNotes()).thenReturn(ORDER_HISTORY_NOTES);

        testObj.executeAction(process);

        verify(modelService).refresh(order);
        verify(modelService).refresh(cart);

        verify(paymentInfo).setBillingAddress(billingAddress);
        verify(paymentInfo).setCode(ORDER_CODE);

        verify(paymentTransaction).setOrder(order);
        verify(paymentTransaction).setInfo(paymentInfo);
        verify(paymentTransaction).setPaymentProvider(PAYMENT_PROVIDER);

        verify(order).setPaymentInfo(paymentInfo);
        verify(order).setPaymentAddress(billingAddress);
        verify(order).setPaymentMode(cart.getPaymentMode());
        verify(order).setStatusInfo(ORDER_HISTORY_NOTES);

        verify(modelService).saveAll(paymentInfo, billingAddress, order);
        verify(modelService).saveAll(List.of(paymentTransaction));

        verify(novalnetOrderService).updateOrderStatus(ORDER_CODE, paymentInfo);
    }

    @Test
    void executeActionShouldThrowExceptionWhenPaymentInfoIsNotNovalnet() {
        when(order.getPaymentInfo()).thenReturn(standardPaymentInfo);
        when(order.getCode()).thenReturn(ORDER_CODE);

        assertThrows(IllegalStateException.class, () -> testObj.executeAction(process));

        verify(modelService).refresh(order);
        verify(modelService).refresh(cart);

        verify(modelService, never()).saveAll(any(ItemModel[].class));
        verifyNoInteractions(novalnetOrderService);
    }

    @Test
    void executeActionShouldThrowExceptionWhenBillingAddressIsNull() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getPaymentAddress()).thenReturn(null);
        when(order.getCode()).thenReturn(ORDER_CODE);

        assertThrows(IllegalStateException.class, () -> testObj.executeAction(process));

        verify(modelService).refresh(order);
        verify(modelService).refresh(cart);

        verify(modelService, never()).saveAll(any(ItemModel[].class));
        verifyNoInteractions(novalnetOrderService);
    }

    @Test
    void executeActionShouldThrowExceptionWhenPaymentTransactionsAreNull() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getPaymentAddress()).thenReturn(billingAddress);
        when(order.getPaymentTransactions()).thenReturn(null);
        when(order.getCode()).thenReturn(ORDER_CODE);

        assertThrows(IllegalStateException.class, () -> testObj.executeAction(process));

        verify(modelService).refresh(order);
        verify(modelService).refresh(cart);

        verify(modelService, never()).saveAll(any(ItemModel[].class));
        verifyNoInteractions(novalnetOrderService);
    }

    @Test
    void executeActionShouldThrowExceptionWhenPaymentTransactionsAreEmpty() {
        when(order.getPaymentInfo()).thenReturn(paymentInfo);
        when(order.getPaymentAddress()).thenReturn(billingAddress);
        when(order.getPaymentTransactions()).thenReturn(List.of());
        when(order.getCode()).thenReturn(ORDER_CODE);

        assertThrows(IllegalStateException.class, () -> testObj.executeAction(process));

        verify(modelService).refresh(order);
        verify(modelService).refresh(cart);

        verify(modelService, never()).saveAll(any(ItemModel[].class));
        verifyNoInteractions(novalnetOrderService);
    }
}
