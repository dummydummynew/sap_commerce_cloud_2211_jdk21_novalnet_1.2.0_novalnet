/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.AddressForm;
import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class DefaultNovalnetAddressServiceTest {
    private static final String SESSION_ATTRIBUTE = "novalnetPaymentRequest";

    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Smith";
    private static final String STREET = "Main Street 10";
    private static final String STREET2 = "Apartment 2";
    private static final String CITY = "Berlin";
    private static final String POSTAL_CODE = "10115";
    private static final String COUNTRY_ISO = "DE";
    private static final String REGION_ISO = "BE";
    private static final String TITLE_CODE = "mr";
    private static final String ADDRESS_ID = "address123";

    @InjectMocks
    private DefaultNovalnetAddressService novalnetAddressService;

    @Mock
    private NovalnetPaymentRequestBuilder novalnetPaymentRequestBuilder;

    @Mock
    private I18NFacade i18NFacade;

    @Mock
    private SessionService sessionService;

    @Mock
    private NovalnetPaymentDetailsForm paymentDetailsForm;

    @Mock
    private CartData cartData;

    @Mock
    private Customer customer;

    @Mock
    private AddressData addressData;

    @Mock
    private AddressData deliveryAddress;

    @Mock
    private AddressForm addressForm;

    @Mock
    private Billing billing;

    @Mock
    private Shipping shipping;

    @BeforeEach
    void setUp() {
        when(novalnetPaymentRequestBuilder.createBillingAddress(addressData)).thenReturn(billing);
        when(novalnetPaymentRequestBuilder.createShippingAddressSameAsBilling()).thenReturn(shipping);
    }

    @Test
    void populateCustomerAddressDetailsUsesDeliveryAddressWhenSelected() {
        when(paymentDetailsForm.isUseDeliveryAddress()).thenReturn(true);

        novalnetAddressService.populateCustomerAddressDetails(paymentDetailsForm, cartData, customer, addressData, billing,
                shipping);

        verify(novalnetPaymentRequestBuilder).createBillingAddress(addressData);
        verify(novalnetPaymentRequestBuilder).createShippingAddressSameAsBilling();

        verify(novalnetPaymentRequestBuilder).populateCustomerData(customer, null, null, null, null, null, null, billing, shipping);

        verify(sessionService).setAttribute(eq(SESSION_ATTRIBUTE), any(NovalnetPaymentRequest.class));
    }

    @Test
    void populateCustomerAddressDetailsUsesSeparateBillingAddress() {
        when(paymentDetailsForm.isUseDeliveryAddress()).thenReturn(false);
        when(paymentDetailsForm.getBillingAddress()).thenReturn(addressForm);
        when(cartData.getDeliveryAddress()).thenReturn(deliveryAddress);

        when(addressForm.getAddressId()).thenReturn(ADDRESS_ID);
        when(addressForm.getTitleCode()).thenReturn(TITLE_CODE);
        when(addressForm.getFirstName()).thenReturn(FIRST_NAME);
        when(addressForm.getLastName()).thenReturn(LAST_NAME);
        when(addressForm.getLine1()).thenReturn(STREET);
        when(addressForm.getLine2()).thenReturn(STREET2);
        when(addressForm.getTownCity()).thenReturn(CITY);
        when(addressForm.getPostcode()).thenReturn(POSTAL_CODE);
        when(addressForm.getCountryIso()).thenReturn(COUNTRY_ISO);
        when(addressForm.getRegionIso()).thenReturn(REGION_ISO);
        when(addressForm.getShippingAddress()).thenReturn(true);
        when(addressForm.getBillingAddress()).thenReturn(true);

        when(paymentDetailsForm.getBillTo_firstName()).thenReturn(FIRST_NAME);
        when(paymentDetailsForm.getBillTo_lastName()).thenReturn(LAST_NAME);
        when(paymentDetailsForm.getBillTo_street1()).thenReturn(STREET);
        when(paymentDetailsForm.getBillTo_street2()).thenReturn(STREET2);
        when(paymentDetailsForm.getBillTo_city()).thenReturn(CITY);
        when(paymentDetailsForm.getBillTo_postalCode()).thenReturn(POSTAL_CODE);
        when(paymentDetailsForm.getBillTo_country()).thenReturn(COUNTRY_ISO);
        when(paymentDetailsForm.getBillTo_titleCode()).thenReturn(TITLE_CODE);

        when(novalnetPaymentRequestBuilder.createShippingAddress(deliveryAddress)).thenReturn(shipping);

        when(novalnetPaymentRequestBuilder.createBillingAddress(FIRST_NAME, LAST_NAME, STREET, STREET2, CITY, POSTAL_CODE,
                COUNTRY_ISO)).thenReturn(billing);

        novalnetAddressService.populateCustomerAddressDetails(paymentDetailsForm, cartData, customer, addressData, billing,
                shipping);

        verify(addressData).setId(ADDRESS_ID);
        verify(addressData).setTitleCode(TITLE_CODE);
        verify(addressData).setFirstName(FIRST_NAME);
        verify(addressData).setLastName(LAST_NAME);
        verify(addressData).setLine1(STREET);
        verify(addressData).setLine2(STREET2);
        verify(addressData).setTown(CITY);
        verify(addressData).setPostalCode(POSTAL_CODE);
        verify(addressData).setShippingAddress(true);
        verify(addressData).setBillingAddress(true);

        verify(i18NFacade).getCountryForIsocode(COUNTRY_ISO);
        verify(i18NFacade).getRegion(COUNTRY_ISO, REGION_ISO);

        verify(novalnetPaymentRequestBuilder).createShippingAddress(deliveryAddress);

        verify(novalnetPaymentRequestBuilder).createBillingAddress(FIRST_NAME, LAST_NAME, STREET, STREET2, CITY, POSTAL_CODE,
                COUNTRY_ISO);

        verify(novalnetPaymentRequestBuilder).populateCustomerData(customer, FIRST_NAME, LAST_NAME, null, null, null, null, billing,
                shipping);

        verify(sessionService).setAttribute(eq(SESSION_ATTRIBUTE), any(NovalnetPaymentRequest.class));
    }

    @Test
    void populateCustomerAddressDetailsUsesSeparateBillingAddressWhenAddressFormIsNull() {
        when(paymentDetailsForm.isUseDeliveryAddress()).thenReturn(false);
        when(paymentDetailsForm.getBillingAddress()).thenReturn(null);
        when(cartData.getDeliveryAddress()).thenReturn(deliveryAddress);

        when(paymentDetailsForm.getBillTo_firstName()).thenReturn(FIRST_NAME);
        when(paymentDetailsForm.getBillTo_lastName()).thenReturn(LAST_NAME);
        when(paymentDetailsForm.getBillTo_street1()).thenReturn(STREET);
        when(paymentDetailsForm.getBillTo_street2()).thenReturn(STREET2);
        when(paymentDetailsForm.getBillTo_city()).thenReturn(CITY);
        when(paymentDetailsForm.getBillTo_postalCode()).thenReturn(POSTAL_CODE);
        when(paymentDetailsForm.getBillTo_country()).thenReturn(COUNTRY_ISO);
        when(paymentDetailsForm.getBillTo_titleCode()).thenReturn(TITLE_CODE);

        when(novalnetPaymentRequestBuilder.createShippingAddress(deliveryAddress)).thenReturn(shipping);

        when(novalnetPaymentRequestBuilder.createBillingAddress(FIRST_NAME, LAST_NAME, STREET, STREET2, CITY, POSTAL_CODE,
                COUNTRY_ISO)).thenReturn(billing);

        novalnetAddressService.populateCustomerAddressDetails(paymentDetailsForm, cartData, customer, addressData, billing,
                shipping);

        verify(addressData).setTitleCode(TITLE_CODE);
        verify(addressData).setFirstName(FIRST_NAME);
        verify(addressData).setLastName(LAST_NAME);
        verify(addressData).setLine1(STREET);
        verify(addressData).setLine2(STREET2);
        verify(addressData).setTown(CITY);
        verify(addressData).setPostalCode(POSTAL_CODE);

        verify(novalnetPaymentRequestBuilder).createShippingAddress(deliveryAddress);

        verify(novalnetPaymentRequestBuilder).createBillingAddress(FIRST_NAME, LAST_NAME, STREET, STREET2, CITY, POSTAL_CODE,
                COUNTRY_ISO);

        verify(novalnetPaymentRequestBuilder).populateCustomerData(customer, FIRST_NAME, LAST_NAME, null, null, null, null, billing,
                shipping);

        verify(sessionService).setAttribute(eq(SESSION_ATTRIBUTE), any(NovalnetPaymentRequest.class));
    }
}
