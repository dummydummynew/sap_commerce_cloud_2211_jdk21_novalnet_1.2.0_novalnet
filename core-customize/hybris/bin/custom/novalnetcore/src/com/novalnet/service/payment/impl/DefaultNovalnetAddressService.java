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
import com.novalnet.service.payment.NovalnetAddressService;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import de.hybris.platform.commercefacades.i18n.I18NFacade;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.session.SessionService;
import jakarta.annotation.Resource;


/**
 * The type Default novalnet address service.
 */
public class DefaultNovalnetAddressService implements NovalnetAddressService {

    @Resource
    private NovalnetPaymentRequestBuilder novalnetPaymentRequestBuilder;

    @Resource
    private I18NFacade i18NFacade;

    @Resource
    private SessionService sessionService;

    @Override
    public void populateCustomerAddressDetails(NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData, Customer customer,
                                               AddressData addressData, Billing billing, Shipping shipping) {
        AddressForm addressForm = paymentDetailsForm.getBillingAddress();
        NovalnetPaymentRequest request = new NovalnetPaymentRequest();

        if (Boolean.TRUE.equals(paymentDetailsForm.isUseDeliveryAddress())) {
            billing = novalnetPaymentRequestBuilder.createBillingAddress(addressData);
            shipping = novalnetPaymentRequestBuilder.createShippingAddressSameAsBilling();
        } else {
            shipping = novalnetPaymentRequestBuilder.createShippingAddress(cartData.getDeliveryAddress());

            if (addressForm != null) {
                addressData.setId(addressForm.getAddressId());
                addressData.setTitleCode(addressForm.getTitleCode());
                addressData.setFirstName(addressForm.getFirstName());
                addressData.setLastName(addressForm.getLastName());
                addressData.setLine1(addressForm.getLine1());
                addressData.setLine2(addressForm.getLine2());
                addressData.setTown(addressForm.getTownCity());
                addressData.setPostalCode(addressForm.getPostcode());

                if (addressForm.getCountryIso() != null) {
                    addressData.setCountry(i18NFacade.getCountryForIsocode(addressForm.getCountryIso()));
                }

                if (addressForm.getRegionIso() != null) {
                    addressData.setRegion(i18NFacade.getRegion(addressForm.getCountryIso(), addressForm.getRegionIso()));
                }

                addressData.setShippingAddress(Boolean.TRUE.equals(addressForm.getShippingAddress()));
                addressData.setBillingAddress(Boolean.TRUE.equals(addressForm.getBillingAddress()));
            }

            billing = novalnetPaymentRequestBuilder.createBillingAddress(paymentDetailsForm.getBillTo_firstName(),
                    paymentDetailsForm.getBillTo_lastName(), paymentDetailsForm.getBillTo_street1(),
                    paymentDetailsForm.getBillTo_street2(), paymentDetailsForm.getBillTo_city(),
                    paymentDetailsForm.getBillTo_postalCode(), paymentDetailsForm.getBillTo_country());

            addressData.setTitleCode(paymentDetailsForm.getBillTo_titleCode());
            addressData.setFirstName(paymentDetailsForm.getBillTo_firstName());
            addressData.setLastName(paymentDetailsForm.getBillTo_lastName());
            addressData.setLine1(paymentDetailsForm.getBillTo_street1());
            addressData.setLine2(paymentDetailsForm.getBillTo_street2());
            addressData.setTown(paymentDetailsForm.getBillTo_city());
            addressData.setPostalCode(paymentDetailsForm.getBillTo_postalCode());
        }

        novalnetPaymentRequestBuilder.populateCustomerData(customer, addressData.getFirstName(), addressData.getLastName(), null,
                null, null, null, billing, shipping);

        request.setCustomer(customer);

        sessionService.setAttribute("novalnetPaymentRequest", request);
    }
}
