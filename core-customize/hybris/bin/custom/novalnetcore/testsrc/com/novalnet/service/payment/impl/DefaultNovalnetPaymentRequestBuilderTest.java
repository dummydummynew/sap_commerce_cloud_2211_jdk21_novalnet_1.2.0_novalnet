/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Custom;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.Merchant;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.dto.payment.Transaction;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetPaymentRequestBuilderTest {
    private static final String API_KEY = "test-api-key";
    private static final Integer TARIFF_ID = 100;
    private static final String SANDBOX_TARIFF = "100";
    private static final String PAYMENT_TYPE = "CREDITCARD";
    private static final String CURRENCY = "EUR";
    private DefaultNovalnetPaymentRequestBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new DefaultNovalnetPaymentRequestBuilder();
    }

    @Test
    void shouldCreateMerchantFromBaseStore() {
        BaseStoreModel store = mock(BaseStoreModel.class);
        when(store.getNovalnetAPIKey()).thenReturn(API_KEY);
        when(store.getNovalnetTariffId()).thenReturn(TARIFF_ID);
        Merchant result = builder.createMerchant(store);
        assertEquals(API_KEY, result.getSignature());
        assertEquals(SANDBOX_TARIFF, result.getTariff());
    }

    @Test
    void shouldRejectMissingBaseStoreCredentials() {
        BaseStoreModel store = mock(BaseStoreModel.class);
        when(store.getNovalnetAPIKey()).thenReturn(null);
        assertThrows(IllegalStateException.class, () -> builder.createMerchant(store));
        assertThrows(IllegalStateException.class, () -> builder.createMerchant(null));
    }

    @Test
    void shouldCreateCustomDataWithDefaultLanguage() {
        Custom result = builder.createCustomData(" ", "Credit Card");
        assertEquals("EN", result.getLang());
        assertEquals("paymentName", result.getInput1());
        assertEquals("Credit Card", result.getInputval1());
    }

    @Test
    void shouldOmitPaymentNameWhenBlank() {
        Custom result = builder.createCustomData("de", " ");
        assertEquals("de", result.getLang());
        assertNull(result.getInput1());
    }

    @Test
    void shouldConvertAmountsToMinorUnits() {
        final Long amount = 1235L;
        final double minorAmount = 12.35d;
        assertEquals(amount, builder.convertToMinorUnits(new BigDecimal("12.345")));
        assertEquals(amount, builder.convertToMinorUnits(minorAmount));
    }

    @Test
    void shouldRejectNullAmount() {
        assertThrows(IllegalArgumentException.class, () -> builder.convertToMinorUnits((BigDecimal) null));
    }

    @Test
    void shouldCreateBaseTransaction() {
        final Long amount = 1250L;
        Transaction result = builder.createBaseTransaction(PAYMENT_TYPE, CURRENCY, amount, "SAP Commerce", "2211");
        assertEquals(PAYMENT_TYPE, result.getPayment_type());
        assertEquals(CURRENCY, result.getCurrency());
        assertEquals(amount, result.getAmount());
        assertEquals("SAP Commerce", result.getSystem_name());
        assertEquals("2211", result.getSystem_version());
    }

    @Test
    void shouldCreateBillingAddressFromFields() {
        Billing result = builder.createBillingAddress("Jane", "Doe", "Main Street", "12", "Berlin", "10115", "DE");
        assertEquals("Jane", result.getFirst_name());
        assertEquals("Doe", result.getLast_name());
        assertEquals("Main Street 12", result.getStreet());
        assertEquals("Berlin", result.getCity());
        assertEquals("10115", result.getZip());
        assertEquals("DE", result.getCountry_code());
    }

    @Test
    void shouldReturnNullForNullAddressData() {
        assertNull(builder.createBillingAddress((AddressData) null));
    }

    @Test
    void shouldReturnNullForNullAddressModel() {
        assertNull(builder.createBillingAddress((AddressModel) null));
    }

    @Test
    void shouldCreateShippingSameAsBilling() {
        Shipping result = builder.createShippingAddressSameAsBilling();
        assertEquals("1", result.getSame_as_billing());
    }

    @Test
    void shouldCreateShippingFromAddressData() {
        AddressData address = mock(AddressData.class);
        when(address.getFirstName()).thenReturn("Jane");
        when(address.getLastName()).thenReturn("Doe");
        when(address.getLine1()).thenReturn("Main Street");
        when(address.getLine2()).thenReturn("12");
        when(address.getTown()).thenReturn("Berlin");
        when(address.getPostalCode()).thenReturn("10115");
        Shipping result = builder.createShippingAddress(address);
        assertEquals("Jane", result.getFirst_name());
        assertEquals("Main Street 12", result.getStreet());
        assertEquals("Berlin", result.getCity());
    }

    @Test
    void shouldPopulateCustomerWithoutOverwritingBlankValues() {
        Customer customer = new Customer();
        Billing billing = new Billing();
        Shipping shipping = new Shipping();
        Customer result = builder.populateCustomerData(customer, "Jane", "Doe", "jane@example.com", "C100", "127.0.0.1", "F",
                billing, shipping);
        assertSame(customer, result);
        assertEquals("Jane", result.getFirst_name());
        assertEquals("Doe", result.getLast_name());
        assertEquals("jane@example.com", result.getEmail());
        assertEquals("C100", result.getCustomer_no());
        assertSame(billing, result.getBilling());
        assertSame(shipping, result.getShipping());
    }

    @Test
    void shouldRejectNullCustomerTarget() {
        assertThrows(IllegalArgumentException.class,
                () -> builder.populateCustomerData(null, null, null, null, null, null, null, null, null));
    }

    @Test
    void shouldCreatePaymentRequestAndOnlyIncludePopulatedHostedPage() {
        Merchant merchant = new Merchant();
        Customer customer = new Customer();
        Transaction transaction = new Transaction();
        Custom custom = new Custom();
        HostedPage hostedPage = new HostedPage();
        hostedPage.setDisplay_payments(Collections.singletonList("CREDITCARD"));
        NovalnetPaymentRequest result = builder.createPaymentRequest(merchant, customer, transaction, custom, hostedPage);
        assertSame(merchant, result.getMerchant());
        assertSame(customer, result.getCustomer());
        assertSame(transaction, result.getTransaction());
        assertSame(custom, result.getCustom());
        assertSame(hostedPage, result.getHosted_page());
    }

    @Test
    void shouldCreateTransactionUpdateAndDetailsRequests() {
        NovalnetPaymentRequest update = builder.createTransactionUpdate("123456", "ORDER-1", "en");
        assertEquals("123456", update.getTransaction().getTid());
        assertEquals("ORDER-1", update.getTransaction().getOrder_no());
        assertEquals("en", update.getCustom().getLang());
        NovalnetPaymentRequest details = builder.createTransactionDetailsRequest("123456", "de");
        assertEquals("123456", details.getTransaction().getTid());
        assertEquals("de", details.getCustom().getLang());
    }

    @Test
    void shouldSerializePaymentRequest() {
        NovalnetPaymentRequest request = builder.createPaymentRequest(new Merchant(), new Customer(), new Transaction(),
                new Custom(), null);
        String json = builder.serializePaymentRequest(request);
        assertNotNull(json);
        assertTrue(json.contains("merchant"));
        assertTrue(json.contains("customer"));
    }

    @Test
    void shouldSerializeTransactionUpdate() {
        Transaction transaction = new Transaction();
        transaction.setTid("123456");
        String json = builder.serializeTransactionUpdate(transaction);
        assertTrue(json.contains("transaction"));
        assertTrue(json.contains("123456"));
    }
}
