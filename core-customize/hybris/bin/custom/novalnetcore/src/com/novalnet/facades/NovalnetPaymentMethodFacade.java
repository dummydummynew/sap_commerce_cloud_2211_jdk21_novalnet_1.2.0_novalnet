/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.ui.Model;

import java.util.List;

/**
 * The interface Novalnet payment method facade.
 */
public interface NovalnetPaymentMethodFacade {

    /**
     * Gets payment methods.
     *
     * @return the payment methods
     */
    List<B2BPaymentTypeData> getPaymentMethods();

    /**
     * Add payment process novalnet payment details form.
     *
     * @param model                      the model
     * @param cartData                   the cart data
     * @param novalnetPaymentDetailsForm the novalnet payment details form
     * @return the novalnet payment details form
     * @throws CMSItemNotFoundException the cms item not found exception
     */
    NovalnetPaymentDetailsForm addPaymentProcess(Model model, CartData cartData, NovalnetPaymentDetailsForm novalnetPaymentDetailsForm) throws CMSItemNotFoundException;

    /**
     * Create payment request.
     *
     * @param paymentDetailsForm the payment details form
     * @param addressData        the address data
     * @param cartData           the cart data
     * @param request            the request
     */
    void createPaymentRequest(@Valid NovalnetPaymentDetailsForm paymentDetailsForm, AddressData addressData, CartData cartData, HttpServletRequest request);

    /**
     * Add payment process b 2 b novalnet payment details form.
     *
     * @param model                      the model
     * @param cartData                   the cart data
     * @param novalnetPaymentDetailsForm the novalnet payment details form
     * @return the novalnet payment details form
     * @throws CMSItemNotFoundException the cms item not found exception
     */
    NovalnetPaymentDetailsForm addPaymentProcessB2B(Model model, CartData cartData, NovalnetPaymentDetailsForm novalnetPaymentDetailsForm) throws CMSItemNotFoundException;

}
