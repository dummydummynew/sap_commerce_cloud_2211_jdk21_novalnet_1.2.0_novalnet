/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades;

import com.novalnet.dto.NovalnetPaymentTypeForm;
import de.hybris.platform.commercefacades.order.data.CartData;

/**
 * The interface Novalnet payment type facade.
 */
public interface NovalnetPaymentTypeFacade {

    /**
     * Update checkout cart cart data.
     *
     * @param paymentTypeForm the payment type form
     * @return the cart data
     */
    CartData updateCheckoutCart(final NovalnetPaymentTypeForm paymentTypeForm);

    /**
     * Prepare payment type form novalnet payment type form.
     *
     * @param cartData the cart data
     * @return the novalnet payment type form
     */
    NovalnetPaymentTypeForm preparePaymentTypeForm(final CartData cartData);

    /**
     * Payment is active boolean.
     *
     * @param code the code
     * @return the boolean
     */
    boolean paymentIsActive(String code);
}
