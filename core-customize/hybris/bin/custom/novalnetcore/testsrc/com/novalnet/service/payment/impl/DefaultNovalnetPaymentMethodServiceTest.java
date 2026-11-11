/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetPaymentHandlerService;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import com.novalnet.service.payment.NovalnetPaymentService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.servicelayer.session.SessionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetPaymentMethodServiceTest {
    private static final String PAYMENT_METHOD = "novalnetCreditCard";
    private static final String RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"}}";

    @InjectMocks
    private DefaultNovalnetPaymentMethodService service;

    @Mock
    private NovalnetDao novalnetDao;
    @Mock
    private SessionService sessionService;
    @Mock
    private NovalnetCheckoutService checkoutService;
    @Mock
    private NovalnetPaymentRequestBuilder requestBuilder;
    @Mock
    private NovalnetApiService apiService;
    @Mock
    private NovalnetPaymentService paymentService;
    @Mock
    private NovalnetEndpointConfigService endpointConfigService;
    @Mock
    private NovalnetPaymentHandlerService paymentHandlerService;
    @Mock
    private DefaultNovalnetOneClickPaymentService oneClickPaymentService;
    @Mock
    private DefaultNovalnetTransactionResultService transactionResultService;
    @Mock
    private DefaultNovalnetGuaranteeValidationService guaranteeValidationService;
    @Mock
    private Model model;


    @Test
    void shouldDelegateAddPaymentProcess() throws Exception {
        final NovalnetPaymentDetailsForm form = new NovalnetPaymentDetailsForm();
        final CartData cartData = new CartData();

        service.addPaymentProcess(model, form, cartData);

        verify(paymentService).addPaymentProcess(model, form, cartData);
    }

    @Test
    void shouldDelegateProcessOneClickTokenData() throws Exception {
        final NovalnetPaymentDetailsForm form = new NovalnetPaymentDetailsForm();
        final CartData cartData = new CartData();
        final AddressData address = new AddressData();
        when(oneClickPaymentService.processOneClickTokenData(PAYMENT_METHOD, form, model, cartData, address)).thenReturn(true);

        assertTrue(service.processOneClickTokenData(PAYMENT_METHOD, form, model, cartData, address));
        verify(oneClickPaymentService).processOneClickTokenData(PAYMENT_METHOD, form, model, cartData, address);
    }

    @Test
    void shouldDelegateProcessTransaction() {
        final Map<String, String> result = new HashMap<>();
        result.put("tid", "123456789");
        when(transactionResultService.processTransaction(result)).thenReturn(true);

        assertTrue(service.processTransaction(result));
        verify(transactionResultService).processTransaction(result);
    }

    @Test
    void shouldDelegateStorePayment() {
        final JsonNode customer = new ObjectMapper().createObjectNode();

        service.handleStorePayment(PAYMENT_METHOD, RESPONSE, customer);

        verify(transactionResultService).handleStorePayment(PAYMENT_METHOD, RESPONSE, customer);
    }

    @Test
    void shouldDelegateGuaranteeProcess() {
        final NovalnetPaymentDetailsForm form = new NovalnetPaymentDetailsForm();
        final AddressData address = new AddressData();
        when(guaranteeValidationService.handleGuaranteeProcess("Invoice", "2000-01-01", form, address)).thenReturn("success");

        assertEquals("success", service.handleGuaranteeProcess("Invoice", "2000-01-01", form, address));
        verify(guaranteeValidationService).handleGuaranteeProcess("Invoice", "2000-01-01", form, address);
    }
}
