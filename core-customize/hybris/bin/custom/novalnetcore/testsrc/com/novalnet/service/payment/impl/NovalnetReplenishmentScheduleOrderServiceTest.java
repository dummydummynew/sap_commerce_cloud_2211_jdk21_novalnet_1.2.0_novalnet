/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetReplenishmentDao;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetReplenishmentScheduleOrderServiceTest {
    private static final String SELECTED_PAYMENT_REFERENCE_PK = "novalnetSelectedPaymentReferencePkForReplenishment";

    private static final String PAYMENT_REFERENCE_PK_VALUE = "987654321";
    private static final String INVALID_PAYMENT_REFERENCE_PK = "INVALID_PK";
    private static final String BLANK_PAYMENT_REFERENCE_PK = " ";

    private static final String UNSUPPORTED_PAYMENT_TYPE = "unsupportedPaymentType";

    private static final String PAYMENT_TOKEN = "novalnet-token-123";

    private static final PK PAYMENT_REFERENCE_PK = PK.fromLong(987654321L);

    @Mock
    private SessionService sessionService;

    @Mock
    private NovalnetReplenishmentPaymentReferenceService novalnetReplenishmentPaymentReferenceService;

    @Mock
    private NovalnetReplenishmentDao novalnetReplenishmentDao;

    @Mock
    private de.hybris.platform.servicelayer.user.AddressService addressService;

    @Mock
    private ModelService modelService;

    @Mock
    private CartModel cart;

    @Mock
    private CartModel scheduledCart;

    @Mock
    private AddressModel deliveryAddress;

    @Mock
    private AddressModel paymentAddress;

    @Mock
    private AddressModel scheduledDeliveryAddress;

    @Mock
    private AddressModel scheduledPaymentAddress;

    @Mock
    private CartToOrderCronJobModel cronJob;

    @Mock
    private NovalnetPaymentRefInfoModel paymentReference;

    @InjectMocks
    private NovalnetReplenishmentScheduleOrderService service;


    @Test
    void findPaymentReferenceReturnsModelWhenReferenceExists() {
        when(novalnetReplenishmentDao.findPaymentReference(cart, PAYMENT_REFERENCE_PK)).thenReturn(paymentReference);

        final NovalnetPaymentRefInfoModel result = service.findPaymentReference(cart, PAYMENT_REFERENCE_PK_VALUE);

        assertSame(paymentReference, result);

        verify(novalnetReplenishmentDao).findPaymentReference(cart, PAYMENT_REFERENCE_PK);
    }

    @Test
    void findPaymentReferenceReturnsNullWhenReferenceDoesNotExist() {
        when(novalnetReplenishmentDao.findPaymentReference(cart, PAYMENT_REFERENCE_PK)).thenReturn(null);

        final NovalnetPaymentRefInfoModel result = service.findPaymentReference(cart, PAYMENT_REFERENCE_PK_VALUE);

        assertNull(result);

        verify(novalnetReplenishmentDao).findPaymentReference(cart, PAYMENT_REFERENCE_PK);
    }

    @Test
    void findPaymentReferenceThrowsExceptionWhenPkIsInvalid() {
        final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.findPaymentReference(cart, INVALID_PAYMENT_REFERENCE_PK));

        assertEquals("The selected Novalnet payment reference PK is invalid", exception.getMessage());

        verifyNoInteractions(novalnetReplenishmentDao);
    }

    @Test
    void findPaymentReferenceThrowsExceptionWhenPkIsBlank() {
        assertThrows(IllegalArgumentException.class, () -> service.findPaymentReference(cart, BLANK_PAYMENT_REFERENCE_PK));

        verifyNoInteractions(novalnetReplenishmentDao);
    }


    @Test
    void createOrderFromCartCronJobThrowsExceptionWhenReferenceDoesNotExist() {
        when(sessionService.getAttribute(SELECTED_PAYMENT_REFERENCE_PK)).thenReturn(PAYMENT_REFERENCE_PK_VALUE);

        when(novalnetReplenishmentDao.findPaymentReference(cart, PAYMENT_REFERENCE_PK)).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.createOrderFromCartCronJob(cart, deliveryAddress, paymentAddress, null, null));
    }

    @Test
    void createOrderFromCartCronJobThrowsExceptionWhenTokenIsNull() {
        when(sessionService.getAttribute(SELECTED_PAYMENT_REFERENCE_PK)).thenReturn(PAYMENT_REFERENCE_PK_VALUE);

        when(novalnetReplenishmentDao.findPaymentReference(cart, PAYMENT_REFERENCE_PK)).thenReturn(paymentReference);

        when(paymentReference.getToken()).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> service.createOrderFromCartCronJob(cart, deliveryAddress, paymentAddress, null, null));
    }

    @Test
    void createOrderFromCartCronJobThrowsExceptionWhenTokenIsBlank() {
        when(sessionService.getAttribute(SELECTED_PAYMENT_REFERENCE_PK)).thenReturn(PAYMENT_REFERENCE_PK_VALUE);

        when(novalnetReplenishmentDao.findPaymentReference(cart, PAYMENT_REFERENCE_PK)).thenReturn(paymentReference);

        when(paymentReference.getToken()).thenReturn(" ");

        assertThrows(IllegalArgumentException.class,
                () -> service.createOrderFromCartCronJob(cart, deliveryAddress, paymentAddress, null, null));
    }

    @Test
    void createOrderFromCartCronJobThrowsExceptionWhenPaymentTypeIsUnsupported() {
        when(sessionService.getAttribute(SELECTED_PAYMENT_REFERENCE_PK)).thenReturn(PAYMENT_REFERENCE_PK_VALUE);

        when(novalnetReplenishmentDao.findPaymentReference(cart, PAYMENT_REFERENCE_PK)).thenReturn(paymentReference);

        when(paymentReference.getToken()).thenReturn(PAYMENT_TOKEN);
        when(paymentReference.getPaymentType()).thenReturn(UNSUPPORTED_PAYMENT_TYPE);

        assertThrows(IllegalArgumentException.class,
                () -> service.createOrderFromCartCronJob(cart, deliveryAddress, paymentAddress, null, null));
    }

    @Test
    void snapshotAddressesClonesDeliveryAndPaymentAddresses() {
        when(cronJob.getCart()).thenReturn(scheduledCart);

        when(addressService.cloneAddressForOwner(deliveryAddress, scheduledCart)).thenReturn(scheduledDeliveryAddress);

        when(addressService.cloneAddressForOwner(paymentAddress, scheduledCart)).thenReturn(scheduledPaymentAddress);

        service.snapshotAddresses(cronJob, deliveryAddress, paymentAddress);

        verify(addressService).cloneAddressForOwner(deliveryAddress, scheduledCart);
        verify(addressService).cloneAddressForOwner(paymentAddress, scheduledCart);

        verify(scheduledCart).setDeliveryAddress(scheduledDeliveryAddress);
        verify(cronJob).setDeliveryAddress(scheduledDeliveryAddress);

        verify(scheduledPaymentAddress).setBillingAddress(Boolean.TRUE);
        verify(scheduledCart).setPaymentAddress(scheduledPaymentAddress);
        verify(cronJob).setPaymentAddress(scheduledPaymentAddress);

        verify(modelService).saveAll(scheduledCart, cronJob);
    }

    @Test
    void snapshotAddressesClonesOnlyDeliveryAddressWhenPaymentAddressIsNull() {
        when(cronJob.getCart()).thenReturn(scheduledCart);

        when(addressService.cloneAddressForOwner(deliveryAddress, scheduledCart)).thenReturn(scheduledDeliveryAddress);

        service.snapshotAddresses(cronJob, deliveryAddress, null);

        verify(addressService).cloneAddressForOwner(deliveryAddress, scheduledCart);

        verify(scheduledCart).setDeliveryAddress(scheduledDeliveryAddress);
        verify(cronJob).setDeliveryAddress(scheduledDeliveryAddress);

        verify(modelService).saveAll(scheduledCart, cronJob);
    }

    @Test
    void snapshotAddressesClonesOnlyPaymentAddressWhenDeliveryAddressIsNull() {
        when(cronJob.getCart()).thenReturn(scheduledCart);

        when(addressService.cloneAddressForOwner(paymentAddress, scheduledCart)).thenReturn(scheduledPaymentAddress);

        service.snapshotAddresses(cronJob, null, paymentAddress);

        verify(addressService).cloneAddressForOwner(paymentAddress, scheduledCart);

        verify(scheduledPaymentAddress).setBillingAddress(Boolean.TRUE);
        verify(scheduledCart).setPaymentAddress(scheduledPaymentAddress);
        verify(cronJob).setPaymentAddress(scheduledPaymentAddress);

        verify(modelService).saveAll(scheduledCart, cronJob);
    }

    @Test
    void snapshotAddressesSavesCartAndCronJobWhenBothAddressesAreNull() {
        when(cronJob.getCart()).thenReturn(scheduledCart);

        service.snapshotAddresses(cronJob, null, null);

        verify(modelService).saveAll(scheduledCart, cronJob);

        verifyNoInteractions(addressService);
    }
}
