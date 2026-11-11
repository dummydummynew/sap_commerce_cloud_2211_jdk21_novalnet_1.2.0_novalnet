/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Custom;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.Merchant;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.dto.payment.Transaction;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.store.BaseStoreModel;

import java.math.BigDecimal;


public interface NovalnetPaymentRequestBuilder {
    Merchant createMerchant(BaseStoreModel baseStore);

    Custom createCustomData(String languageCode, String paymentName);

    long convertToMinorUnits(BigDecimal amount);

    long convertToMinorUnits(double amount);

    Transaction createBaseTransaction(String paymentType, String currencyIso, long amountInMinorUnits, String systemName,
                                      String systemVersion);

    Billing createBillingAddress(String firstName, String lastName, String line1, String line2, String city, String zip,
                                 String countryIso);

    Billing createBillingAddress(AddressData address);

    Billing createBillingAddress(AddressModel address);

    Shipping createShippingAddress(String firstName, String lastName, String line1, String line2, String city, String zip,
                                   String countryIso);

    Shipping createShippingAddress(AddressData address);

    Shipping createShippingAddress(AddressModel shippingAddress, AddressModel billingAddress);

    Shipping createShippingAddressSameAsBilling();

    Customer populateCustomerData(Customer target, String firstName, String lastName, String email, String customerNo,
                                  String customerIp, String gender, Billing billing, Shipping shipping);

    NovalnetPaymentRequest createPaymentRequest(Merchant merchant, Customer customer, Transaction transaction, Custom custom,
                                                HostedPage hostedPage);

    String serializePaymentRequest(NovalnetPaymentRequest paymentRequest);

    NovalnetPaymentRequest createTransactionUpdate(String transactionId, String orderCode, String languageCode);

    String serializeTransactionUpdate(Transaction transaction);

    NovalnetPaymentRequest createTransactionDetailsRequest(String transactionId, String languageCode);
}
