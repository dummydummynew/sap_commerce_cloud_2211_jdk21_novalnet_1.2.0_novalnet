
/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.checkout;

import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.InvalidCartException;


/**
 * Defines operations for managing Novalnet checkout data and customer information.
 */
public interface NovalnetCheckoutService {
    /**
     * Saves order data for the Novalnet checkout process.
     *
     * @param orderComments     the comments associated with the order
     * @param currentPayment    the selected payment method
     * @param transactionStatus the transaction status
     * @param orderAmountCent   the order amount in cents
     * @param currency          the order currency
     * @param transactionID     the Novalnet transaction ID
     * @param email             the customer's email address
     * @param addressData       the customer's address data
     * @param bankDetails       the bank details associated with the transaction
     * @return the saved order data
     * @throws InvalidCartException if the cart is invalid
     */
    OrderData saveOrderData(String orderComments, String currentPayment, String transactionStatus, int orderAmountCent,
                            String currency, String transactionID, String email, AddressData addressData, String bankDetails)
            throws InvalidCartException;


    /**
     * Retrieves the current user.
     *
     * @return the current user
     */
    UserModel getCurrentUser();

    /**
     * Retrieves the billing address of the current checkout.
     *
     * @return the billing address
     */
    AddressModel getBillingAddress();

    /**
     * Determines whether the current user is a guest user.
     *
     * @return true if the current user is a guest user; otherwise false
     */
    boolean isGuestUser();

    /**
     * Retrieves the email address of the current guest user.
     *
     * @return the guest user's email address
     */
    String getUserEmail();

    /**
     * Retrieves the payment mode code of the current customer's most recent order.
     *
     * @return the payment mode code, or null if the customer is a guest, has no order, or the order has no payment mode
     */
    String getLastOrderPaymentModeCode();

    /**
     * Determines whether the current customer's most recent order was paid with Google Pay or Apple Pay.
     *
     * @return true if the last order payment mode is Google Pay or Apple Pay; otherwise false
     */
    boolean isWalletPayment();
}


