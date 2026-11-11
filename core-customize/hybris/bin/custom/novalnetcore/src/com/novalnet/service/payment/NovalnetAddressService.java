package com.novalnet.service.payment;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.Shipping;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;

/**
 * The interface Novalnet address service.
 */
public interface NovalnetAddressService {

    /**
     * Populates the customer address details required for payment processing (B2C).
     *
     * @param paymentDetailsForm the payment details form
     * @param cartData           the cart data
     * @param customer           the customer
     * @param addressData        the address data
     * @param billing            the billing
     * @param shipping           the shipping
     */
    void populateCustomerAddressDetails(NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData, Customer customer, AddressData addressData, Billing billing, Shipping shipping);
}
