/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.impl;

import com.novalnet.dto.NovalnetPaymentTypeForm;
import com.novalnet.facades.NovalnetPaymentTypeFacade;
import com.novalnet.service.payment.NovalnetPaymentTypeCheckoutService;
import de.hybris.platform.commercefacades.order.data.CartData;
import jakarta.annotation.Resource;

/**
 * The type Default novalnet payment type facade.
 */
public class DefaultNovalnetPaymentTypeFacade implements NovalnetPaymentTypeFacade {

    @Resource
    private NovalnetPaymentTypeCheckoutService novalnetPaymentTypeCheckoutService;

    @Override
    public CartData updateCheckoutCart(NovalnetPaymentTypeForm paymentTypeForm) {
        return novalnetPaymentTypeCheckoutService.updateCheckoutCart(paymentTypeForm);
    }

    @Override
    public NovalnetPaymentTypeForm preparePaymentTypeForm(CartData cartData) {
        return novalnetPaymentTypeCheckoutService.preparePaymentTypeForm(cartData);
    }

    @Override
    public boolean paymentIsActive(String code) {
        return novalnetPaymentTypeCheckoutService.paymentIsActive(code);
    }


}
