/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.session.SessionService;
import jakarta.annotation.Resource;

import java.util.Objects;


/**
 * Service for validating guarantee payment requirements, including billing address consistency and date of birth requirements.
 */
public class DefaultNovalnetGuaranteeValidationService {
    @Resource
    private SessionService sessionService;

    /**
     * Handles the validation process for a guarantee payment.
     *
     * @param paymentName        the name of the selected payment method
     * @param dob                the customer's date of birth
     * @param paymentDetailsForm the payment details containing billing address information
     * @param deliveryAddress    the delivery address used for address validation
     * @return the validation error message key, or an empty string when validation succeeds
     * @throws IllegalArgumentException if the payment details or delivery address is {@code null}
     */
    public String handleGuaranteeProcess(String paymentName, String dob, NovalnetPaymentDetailsForm paymentDetailsForm,
                                         AddressData deliveryAddress) {
        if (paymentDetailsForm == null) {
            throw new IllegalArgumentException("Payment details are required.");
        }
        if (deliveryAddress == null) {
            throw new IllegalArgumentException("Delivery address is required.");
        }
        if (Boolean.FALSE.equals(paymentDetailsForm.isUseDeliveryAddress())
                && hasBillingAddressMismatch(paymentDetailsForm, deliveryAddress)) {
            return "novalnet.address.error";
        }
        String guaranteeError = sessionService.getAttribute(paymentName + "GuaranteeError");
        if (guaranteeError != null) {
            return guaranteeError;
        }
        return validateGuaranteeDob(paymentName, dob);
    }

    /**
     * Checks whether the billing address differs from the delivery address.
     *
     * @param paymentDetailsForm the payment details containing the billing address
     * @param deliveryAddress    the delivery address to compare against
     * @return {@code true} if the billing address does not match the delivery address; otherwise, {@code false}
     */
    private boolean hasBillingAddressMismatch(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData deliveryAddress) {
        return !isBillingAddressMatching(paymentDetailsForm, deliveryAddress);
    }

    /**
     * Checks whether the billing address matches the delivery address.
     *
     * @param paymentDetailsForm the payment details containing the billing address
     * @param deliveryAddress    the delivery address to compare against
     * @return {@code true} if all relevant address fields match; otherwise, {@code false}
     */
    private boolean isBillingAddressMatching(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData deliveryAddress) {
        boolean addressMatches = Objects.equals(paymentDetailsForm.getBillTo_street1(), deliveryAddress.getLine1())
                && Objects.equals(paymentDetailsForm.getBillTo_street2(), deliveryAddress.getLine2())
                && Objects.equals(paymentDetailsForm.getBillTo_postalCode(), deliveryAddress.getPostalCode());
        boolean locationMatches = Objects.equals(paymentDetailsForm.getBillTo_city(), deliveryAddress.getTown())
                && isCountryMatching(paymentDetailsForm, deliveryAddress);
        return addressMatches && locationMatches;
    }

    /**
     * Checks whether the billing country matches the delivery country.
     *
     * @param paymentDetailsForm the payment details containing the billing country
     * @param deliveryAddress    the delivery address containing the country
     * @return {@code true} if the billing and delivery countries match; otherwise, {@code false}
     */
    private boolean isCountryMatching(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData deliveryAddress) {
        return Objects.equals(paymentDetailsForm.getBillTo_country(),
                deliveryAddress.getCountry() != null ? deliveryAddress.getCountry().getIsocode() : null);
    }

    /**
     * Validates the date of birth for the guarantee payment.
     *
     * @param paymentName the name of the selected payment method
     * @param dob         the customer's date of birth
     * @return the validation error message key, or an empty string when validation succeeds
     */
    private String validateGuaranteeDob(String paymentName, String dob) {
        if ("".equals(dob)) {
            return "novalnet.dob.error";
        }
        boolean isValidDob = NovalnetUtils.hasAgeRequirement(dob);
        if (Boolean.FALSE.equals(isValidDob)) {
            return "novalnet.age.error";
        } else if (Boolean.TRUE.equals(isValidDob)) {
            sessionService.setAttribute(paymentName + "DateOfBirth", dob.trim());
            sessionService.setAttribute(paymentName + "PaymentGuarantee", true);
        }
        return "";
    }
}
