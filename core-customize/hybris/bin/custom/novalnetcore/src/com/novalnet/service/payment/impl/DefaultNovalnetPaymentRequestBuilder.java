/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.gson.Gson;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Custom;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.Merchant;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.dto.payment.Transaction;
import com.novalnet.exception.NovalnetException;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;


public class DefaultNovalnetPaymentRequestBuilder implements NovalnetPaymentRequestBuilder {
    private static final BigDecimal MINOR_UNITS_MULTIPLIER = BigDecimal.valueOf(100);
    private static final String PAYMENT_NAME_KEY = "paymentName";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Merchant createMerchant(final BaseStoreModel baseStore) {
        if (baseStore == null || baseStore.getNovalnetAPIKey() == null || baseStore.getNovalnetTariffId() == null) {
            throw new IllegalStateException("Novalnet API key / tariff is not configured on the base store");
        }
        final Merchant merchant = new Merchant();
        merchant.setSignature(baseStore.getNovalnetAPIKey());
        merchant.setTariff(String.valueOf(baseStore.getNovalnetTariffId()));
        return merchant;
    }

    @Override
    public Custom createCustomData(final String languageCode, final String paymentName) {
        final Custom custom = new Custom();
        custom.setLang(StringUtils.defaultIfBlank(languageCode, "EN"));
        if (StringUtils.isNotBlank(paymentName)) {
            custom.setInput1(PAYMENT_NAME_KEY);
            custom.setInputval1(paymentName);
        }
        return custom;
    }

    @Override
    public long convertToMinorUnits(final BigDecimal amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Payment amount must not be null");
        }
        return amount.multiply(MINOR_UNITS_MULTIPLIER).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    @Override
    public long convertToMinorUnits(final double amount) {
        return convertToMinorUnits(BigDecimal.valueOf(amount));
    }

    @Override
    public Transaction createBaseTransaction(final String paymentType, final String currencyIso, final long amountInMinorUnits,
                                             final String systemName, final String systemVersion) {
        final Transaction transaction = new Transaction();
        transaction.setPayment_type(paymentType);
        transaction.setCurrency(currencyIso);
        transaction.setAmount(amountInMinorUnits);
        transaction.setSystem_name(systemName);
        transaction.setSystem_version(systemVersion);
        return transaction;
    }

    @Override
    public Billing createBillingAddress(final String firstName, final String lastName, final String line1, final String line2,
                                        final String city, final String zip, final String countryIso) {
        final Billing billing = new Billing();
        billing.setFirst_name(firstName);
        billing.setLast_name(lastName);
        billing.setStreet(combineAddressLines(line1, line2));
        billing.setCity(city);
        billing.setZip(zip);
        billing.setCountry_code(countryIso);
        return billing;
    }

    @Override
    public Billing createBillingAddress(final AddressData address) {
        if (address == null) {
            return null;
        }
        return createBillingAddress(address.getFirstName(), address.getLastName(), address.getLine1(), address.getLine2(),
                address.getTown(), address.getPostalCode(), address.getCountry() != null ? address.getCountry().getIsocode() : null);
    }

    @Override
    public Billing createBillingAddress(final AddressModel address) {
        if (address == null) {
            return null;
        }
        final Billing billing = createBillingAddress(address.getFirstname(), address.getLastname(), address.getStreetname(),
                address.getStreetnumber(), address.getTown(), address.getPostalcode(),
                address.getCountry() != null ? address.getCountry().getIsocode() : null);
        billing.setHouse_no(address.getStreetnumber());
        billing.setTel(address.getPhone1());
        billing.setCompany(address.getCompany());
        return billing;
    }

    @Override
    public Shipping createShippingAddress(final String firstName, final String lastName, final String line1, final String line2,
                                          final String city, final String zip, final String countryIso) {
        final Shipping shipping = new Shipping();
        shipping.setFirst_name(firstName);
        shipping.setLast_name(lastName);
        shipping.setStreet(combineAddressLines(line1, line2));
        shipping.setCity(city);
        shipping.setZip(zip);
        shipping.setCountry_code(countryIso);
        return shipping;
    }

    @Override
    public Shipping createShippingAddress(final AddressData address) {
        if (address == null) {
            return null;
        }
        return createShippingAddress(address.getFirstName(), address.getLastName(), address.getLine1(), address.getLine2(),
                address.getTown(), address.getPostalCode(), address.getCountry() != null ? address.getCountry().getIsocode() : null);
    }

    @Override
    public Shipping createShippingAddress(final AddressModel shippingAddress, final AddressModel billingAddress) {
        if (shippingAddress == null) {
            return billingAddress != null ? createShippingAddressSameAsBilling() : null;
        }
        if (shippingAddress.equals(billingAddress)) {
            return createShippingAddressSameAsBilling();
        }
        return createShippingAddress(shippingAddress.getFirstname(), shippingAddress.getLastname(), shippingAddress.getStreetname(),
                shippingAddress.getStreetnumber(), shippingAddress.getTown(), shippingAddress.getPostalcode(),
                shippingAddress.getCountry() != null ? shippingAddress.getCountry().getIsocode() : null);
    }

    @Override
    public Shipping createShippingAddressSameAsBilling() {
        final Shipping shipping = new Shipping();
        shipping.setSame_as_billing("1");
        return shipping;
    }

    @Override
    public Customer populateCustomerData(final Customer target, final String firstName, final String lastName, final String email,
                                         final String customerNo, final String customerIp, final String gender, final Billing billing, final Shipping shipping) {

        if (target == null) {
            throw new IllegalArgumentException("Customer target must not be null");
        }
        if (StringUtils.isNotBlank(firstName)) {
            target.setFirst_name(firstName);
        }
        if (StringUtils.isNotBlank(lastName)) {
            target.setLast_name(lastName);
        }
        if (StringUtils.isNotBlank(customerNo)) {
            target.setCustomer_no(customerNo);
        }
        if (StringUtils.isNotBlank(customerIp)) {
            target.setCustomer_ip(customerIp);
        }
        if (StringUtils.isNotBlank(email)) {
            target.setEmail(email);
        }
        if (StringUtils.isNotBlank(gender)) {
            target.setGender(gender);
        }
        if (billing != null) {
            target.setBilling(billing);
        }
        if (shipping != null) {
            target.setShipping(shipping);
        }
        return target;
    }

    private String combineAddressLines(final String line1, final String line2) {
        return StringUtils.join(new String[]
                {StringUtils.trimToNull(line1), StringUtils.trimToNull(line2)}, " ").trim();
    }

    @Override
    public NovalnetPaymentRequest createPaymentRequest(final Merchant merchant, final Customer customer,
                                                       final Transaction transaction, final Custom custom, final HostedPage hostedPage) {
        final NovalnetPaymentRequest request = new NovalnetPaymentRequest();
        request.setMerchant(merchant);
        request.setCustomer(customer);
        request.setTransaction(transaction);
        request.setCustom(custom);
        if (hostedPage != null && (hostedPage.getDisplay_payments() != null || hostedPage.getHide_blocks() != null
                || hostedPage.getSkip_pages() != null)) {
            request.setHosted_page(hostedPage);
        }
        return request;
    }

    @Override
    public String serializePaymentRequest(final NovalnetPaymentRequest paymentRequest) {
        try {
            return objectMapper.writeValueAsString(paymentRequest);
        } catch (final JsonProcessingException e) {
            throw new NovalnetException("Unable to serialize Novalnet payment request", e);
        }
    }

    @Override
    public NovalnetPaymentRequest createTransactionUpdate(final String transactionId, final String orderCode, final String languageCode) {
        Transaction transaction = new Transaction();
        transaction.setTid(transactionId);
        transaction.setOrder_no(orderCode);

        Custom custom = new Custom();
        custom.setLang(languageCode);

        NovalnetPaymentRequest paymentRequest = new NovalnetPaymentRequest();
        paymentRequest.setTransaction(transaction);
        paymentRequest.setCustom(custom);

        return paymentRequest;
    }

    @Override
    public String serializeTransactionUpdate(final Transaction transaction) {
        final Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("transaction", transaction);

        return new Gson().toJson(updateRequest);
    }

    @Override
    public NovalnetPaymentRequest createTransactionDetailsRequest(final String transactionId, final String languageCode) {

        final Transaction transaction = new Transaction();
        transaction.setTid(transactionId);

        final Custom custom = new Custom();
        custom.setLang(languageCode);

        final NovalnetPaymentRequest request = new NovalnetPaymentRequest();
        request.setTransaction(transaction);
        request.setCustom(custom);

        return request;
    }
}
