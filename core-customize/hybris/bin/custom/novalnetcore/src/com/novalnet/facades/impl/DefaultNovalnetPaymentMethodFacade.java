/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.impl;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.facades.NovalnetPaymentMethodFacade;
import com.novalnet.service.payment.NovalnetB2BPaymentMethodService;
import com.novalnet.service.payment.NovalnetPaymentMethodService;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.enumeration.EnumerationService;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

import java.util.List;

/**
 * The type Default novalnet payment method facade.
 */
public class DefaultNovalnetPaymentMethodFacade implements NovalnetPaymentMethodFacade {

    @Resource
    private Converter<CheckoutPaymentType, B2BPaymentTypeData> novalnetPaymentTypeDataConverter;

    @Resource
    private EnumerationService enumerationService;

    @Resource
    private NovalnetPaymentMethodService novalnetPaymentMethodService;

    @Resource
    private NovalnetB2BPaymentMethodService novalnetB2BPaymentMethodService;


    @Override
    public List<B2BPaymentTypeData> getPaymentMethods() {
        List<CheckoutPaymentType> allValues = enumerationService.getEnumerationValues(CheckoutPaymentType.class);
        return novalnetPaymentTypeDataConverter.convertAll(allValues);
    }

    @Override
    public NovalnetPaymentDetailsForm addPaymentProcess(
            Model model,
            CartData cartData,
            NovalnetPaymentDetailsForm novalnetPaymentDetailsForm)
            throws CMSItemNotFoundException {

        novalnetPaymentMethodService.addPaymentProcess(model, novalnetPaymentDetailsForm, cartData);

        return novalnetPaymentDetailsForm;
    }

    @Override
    public NovalnetPaymentDetailsForm addPaymentProcessB2B(
            Model model,
            CartData cartData,
            NovalnetPaymentDetailsForm novalnetPaymentDetailsForm) {

        novalnetB2BPaymentMethodService.addPaymentProcess(model, cartData, novalnetPaymentDetailsForm);

        return novalnetPaymentDetailsForm;
    }


    @Override
    public void createPaymentRequest(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData addressData, CartData cartData, HttpServletRequest request) {
        novalnetB2BPaymentMethodService.createPaymentRequest(paymentDetailsForm, addressData, cartData, request);
    }
}
