/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment;

import com.novalnet.dto.NovalnetPaymentTypeForm;
import de.hybris.platform.commercefacades.order.data.CartData;

/**
 * The interface Novalnet payment type checkout service.
 */
public interface NovalnetPaymentTypeCheckoutService {

    /**
     * Update checkout cart data.
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
     * @param paymentTypeForm the payment type form
     * @return the boolean
     */
    Boolean paymentIsActive(final String paymentTypeForm);
}
