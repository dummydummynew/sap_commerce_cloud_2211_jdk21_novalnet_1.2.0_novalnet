/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.DeliveryModeData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.util.localization.Localization;
import org.easymock.Mock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetB2BAcceleratorCheckoutFacadeTest {
    @Spy
    private DefaultNovalnetB2BAcceleratorCheckoutFacade checkoutFacade;

    @Mock
    private CartData cartData;
    private MockedStatic<Localization> localizationMock;

    @BeforeEach
    void setUp() {
        localizationMock = mockStatic(Localization.class);
        localizationMock.when(() -> Localization.getLocalizedString(anyString()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        checkoutFacade = spy(new DefaultNovalnetB2BAcceleratorCheckoutFacade());
        cartData = mock(CartData.class);
        doReturn(cartData).when(checkoutFacade).getCheckoutCart();
    }

    @AfterEach
    void tearDown() {
        localizationMock.close();
    }

    @Test
    void shouldThrowExceptionWhenCartIsNotCalculated() {
        when(cartData.isCalculated()).thenReturn(false);

        assertThrows(EntityValidationException.class, () -> checkoutFacade.isValidCheckoutCart(new PlaceOrderData()));
    }

    @Test
    void shouldThrowExceptionWhenDeliveryAddressIsMissing() {
        when(cartData.isCalculated()).thenReturn(true);
        when(cartData.getDeliveryAddress()).thenReturn(null);

        assertThrows(EntityValidationException.class, () -> checkoutFacade.isValidCheckoutCart(new PlaceOrderData()));
    }

    @Test
    void shouldThrowExceptionWhenDeliveryModeIsMissing() {
        when(cartData.isCalculated()).thenReturn(true);
        when(cartData.getDeliveryAddress()).thenReturn(mock(AddressData.class));
        when(cartData.getDeliveryMode()).thenReturn(null);

        assertThrows(EntityValidationException.class, () -> checkoutFacade.isValidCheckoutCart(new PlaceOrderData()));
    }

    @Test
    void shouldThrowExceptionWhenNegotiatedQuoteIsNotAllowed() {
        final PlaceOrderData placeOrderData = new PlaceOrderData();
        placeOrderData.setNegotiateQuote(Boolean.TRUE);

        when(cartData.isCalculated()).thenReturn(true);
        when(cartData.getDeliveryAddress()).thenReturn(mock(AddressData.class));
        when(cartData.getDeliveryMode()).thenReturn(mock(DeliveryModeData.class));
        when(cartData.getQuoteAllowed()).thenReturn(Boolean.FALSE);

        assertThrows(EntityValidationException.class, () -> checkoutFacade.isValidCheckoutCart(placeOrderData));
    }
}
