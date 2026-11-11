/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades.populator;

import com.novalnet.facades.NovalnetPaymentTypeFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.core.model.enumeration.EnumerationValueModel;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetB2BPaymentTypePopulatorTest {
    private static final String API_KEY = "test-api-key";
    private static final String PAYMENT_ACCESS_KEY = "test-payment-access-key";
    private static final Integer TARIFF_ID = 123;
    private static final String ACCOUNT_CODE = "ACCOUNT";
    private static final String CARD_CODE = "CARD";
    private static final String ACCOUNT_NAME = "Account";
    private static final String CARD_NAME = "Credit Card";

    @InjectMocks
    private NovalnetB2BPaymentTypePopulator testObj;

    @Mock
    private BaseStoreService baseStoreService;

    @Mock
    private NovalnetPaymentTypeFacade novalnetPaymentTypeFacade;

    @Mock
    private TypeService typeService;

    @Mock
    private BaseStoreModel baseStore;

    @Mock
    private EnumerationValueModel enumerationValue;

    @Mock
    private B2BPaymentTypeData target;

    @BeforeEach
    void setUp() {
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        when(baseStore.getNovalnetTariffId()).thenReturn(TARIFF_ID);
        when(baseStore.getNovalnetAPIKey()).thenReturn(API_KEY);
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(PAYMENT_ACCESS_KEY);
    }

    @Test
    void populateShouldSetAccountPaymentType() {
        when(typeService.getEnumerationValue(CheckoutPaymentType.ACCOUNT)).thenReturn(enumerationValue);
        when(enumerationValue.getName()).thenReturn(ACCOUNT_NAME);
        when(novalnetPaymentTypeFacade.paymentIsActive(ACCOUNT_CODE)).thenReturn(false);

        testObj.populate(CheckoutPaymentType.ACCOUNT, target);

        verify(target).setCode(ACCOUNT_CODE);
        verify(target).setDisplayName(ACCOUNT_NAME);
    }

    @Test
    void populateShouldDefaultToAccountWhenSourceIsNull() {
        when(typeService.getEnumerationValue(CheckoutPaymentType.ACCOUNT)).thenReturn(enumerationValue);
        when(enumerationValue.getName()).thenReturn(ACCOUNT_NAME);
        when(novalnetPaymentTypeFacade.paymentIsActive(ACCOUNT_CODE)).thenReturn(false);

        testObj.populate(null, target);

        verify(target).setCode(ACCOUNT_CODE);
        verify(target).setDisplayName(ACCOUNT_NAME);
    }

    @Test
    void populateShouldSetCardWhenPaymentIsActive() {
        when(typeService.getEnumerationValue(CheckoutPaymentType.CARD)).thenReturn(enumerationValue);
        when(enumerationValue.getName()).thenReturn(CARD_NAME);
        when(novalnetPaymentTypeFacade.paymentIsActive(CARD_CODE)).thenReturn(true);

        testObj.populate(CheckoutPaymentType.CARD, target);

        verify(target).setCode(CARD_CODE);
        verify(target).setDisplayName(CARD_NAME);
    }

    @Test
    void populateShouldNotSetPaymentTypeWhenPaymentIsInactive() {
        when(novalnetPaymentTypeFacade.paymentIsActive(CARD_CODE)).thenReturn(false);

        testObj.populate(CheckoutPaymentType.CARD, target);

        verify(target, never()).setCode(anyString());
        verify(target, never()).setDisplayName(anyString());
    }

    @Test
    void populateShouldNotSetPaymentTypeWhenTariffIsNull() {
        when(baseStore.getNovalnetTariffId()).thenReturn(null);

        testObj.populate(CheckoutPaymentType.CARD, target);

        verify(novalnetPaymentTypeFacade, never()).paymentIsActive(anyString());
        verify(target, never()).setCode(anyString());
        verify(target, never()).setDisplayName(anyString());
    }

    @Test
    void populateShouldNotSetPaymentTypeWhenApiKeyIsEmpty() {
        when(baseStore.getNovalnetAPIKey()).thenReturn("");

        testObj.populate(CheckoutPaymentType.CARD, target);

        verify(novalnetPaymentTypeFacade, never()).paymentIsActive(anyString());
        verify(target, never()).setCode(anyString());
        verify(target, never()).setDisplayName(anyString());
    }

    @Test
    void populateShouldNotSetPaymentTypeWhenPaymentAccessKeyIsEmpty() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn("");

        testObj.populate(CheckoutPaymentType.CARD, target);

        verify(novalnetPaymentTypeFacade, never()).paymentIsActive(anyString());
        verify(target, never()).setCode(anyString());
        verify(target, never()).setDisplayName(anyString());
    }
}
