/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.http.impl;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import org.apache.commons.configuration2.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetEndpointConfigServiceTest {
    private static final String PAYMENT_URL_KEY = "novalnet.payment.url";
    private static final String PAYMENT_HOSTED_URL_KEY = "novalnet.payment.hosted.url";
    private static final String AUTHORIZE_URL_KEY = "novalnet.authorize.url";
    private static final String AUTHORIZE_HOSTED_URL_KEY = "novalnet.authorize.hosted.url";
    private static final String TRANSACTION_CAPTURE_URL_KEY = "novalnet.transaction.capture.url";
    private static final String TRANSACTION_CANCEL_URL_KEY = "novalnet.transaction.cancel.url";
    private static final String TRANSACTION_REFUND_URL_KEY = "novalnet.transaction.refund.url";
    private static final String TRANSACTION_DETAILS_URL_KEY = "novalnet.transaction.details.url";
    private static final String TRANSACTION_UPDATE_URL_KEY = "novalnet.transaction.update.url";
    private static final String WEBHOOK_CONFIGURE_URL_KEY = "novalnet.webhook.configure.url";
    private static final String MERCHANT_DETAILS_URL_KEY = "novalnet.merchant.details.url";

    private static final String PAYMENT_URL = "https://test.novalnet.com/payment";
    private static final String PAYMENT_HOSTED_URL = "https://test.novalnet.com/payment/hosted";
    private static final String AUTHORIZE_URL = "https://test.novalnet.com/authorize";
    private static final String AUTHORIZE_HOSTED_URL = "https://test.novalnet.com/authorize/hosted";
    private static final String TRANSACTION_CAPTURE_URL = "https://test.novalnet.com/transaction/capture";
    private static final String TRANSACTION_CANCEL_URL = "https://test.novalnet.com/transaction/cancel";
    private static final String TRANSACTION_REFUND_URL = "https://test.novalnet.com/transaction/refund";
    private static final String TRANSACTION_DETAILS_URL = "https://test.novalnet.com/transaction/details";
    private static final String TRANSACTION_UPDATE_URL = "https://test.novalnet.com/transaction/update";
    private static final String WEBHOOK_CONFIGURE_URL = "https://test.novalnet.com/webhook/configure";
    private static final String MERCHANT_DETAILS_URL = "https://test.novalnet.com/merchant/details";

    @Mock
    private ConfigurationService configurationService;

    @Mock
    private Configuration configuration;

    @InjectMocks
    private DefaultNovalnetEndpointConfigService endpointConfigService;

    @BeforeEach
    void setUp() {
        when(configurationService.getConfiguration()).thenReturn(configuration);
    }

    @Test
    void testGetPaymentUrl() {
        when(configuration.getString(PAYMENT_URL_KEY)).thenReturn(PAYMENT_URL);

        assertEquals(PAYMENT_URL, endpointConfigService.getPaymentUrl());

        verify(configuration).getString(PAYMENT_URL_KEY);
    }

    @Test
    void testGetPaymentHostedUrl() {
        when(configuration.getString(PAYMENT_HOSTED_URL_KEY)).thenReturn(PAYMENT_HOSTED_URL);

        assertEquals(PAYMENT_HOSTED_URL, endpointConfigService.getPaymentHostedUrl());

        verify(configuration).getString(PAYMENT_HOSTED_URL_KEY);
    }

    @Test
    void testGetAuthorizeUrl() {
        when(configuration.getString(AUTHORIZE_URL_KEY)).thenReturn(AUTHORIZE_URL);

        assertEquals(AUTHORIZE_URL, endpointConfigService.getAuthorizeUrl());

        verify(configuration).getString(AUTHORIZE_URL_KEY);
    }

    @Test
    void testGetAuthorizeHostedUrl() {
        when(configuration.getString(AUTHORIZE_HOSTED_URL_KEY)).thenReturn(AUTHORIZE_HOSTED_URL);

        assertEquals(AUTHORIZE_HOSTED_URL, endpointConfigService.getAuthorizeHostedUrl());

        verify(configuration).getString(AUTHORIZE_HOSTED_URL_KEY);
    }

    @Test
    void testGetTransactionCaptureUrl() {
        when(configuration.getString(TRANSACTION_CAPTURE_URL_KEY)).thenReturn(TRANSACTION_CAPTURE_URL);

        assertEquals(TRANSACTION_CAPTURE_URL, endpointConfigService.getTransactionCaptureUrl());

        verify(configuration).getString(TRANSACTION_CAPTURE_URL_KEY);
    }

    @Test
    void testGetTransactionCancelUrl() {
        when(configuration.getString(TRANSACTION_CANCEL_URL_KEY)).thenReturn(TRANSACTION_CANCEL_URL);

        assertEquals(TRANSACTION_CANCEL_URL, endpointConfigService.getTransactionCancelUrl());

        verify(configuration).getString(TRANSACTION_CANCEL_URL_KEY);
    }

    @Test
    void testGetTransactionRefundUrl() {
        when(configuration.getString(TRANSACTION_REFUND_URL_KEY)).thenReturn(TRANSACTION_REFUND_URL);

        assertEquals(TRANSACTION_REFUND_URL, endpointConfigService.getTransactionRefundUrl());

        verify(configuration).getString(TRANSACTION_REFUND_URL_KEY);
    }

    @Test
    void testGetTransactionDetailsUrl() {
        when(configuration.getString(TRANSACTION_DETAILS_URL_KEY)).thenReturn(TRANSACTION_DETAILS_URL);

        assertEquals(TRANSACTION_DETAILS_URL, endpointConfigService.getTransactionDetailsUrl());

        verify(configuration).getString(TRANSACTION_DETAILS_URL_KEY);
    }

    @Test
    void testGetTransactionUpdateUrl() {
        when(configuration.getString(TRANSACTION_UPDATE_URL_KEY)).thenReturn(TRANSACTION_UPDATE_URL);

        assertEquals(TRANSACTION_UPDATE_URL, endpointConfigService.getTransactionUpdateUrl());

        verify(configuration).getString(TRANSACTION_UPDATE_URL_KEY);
    }

    @Test
    void testGetWebhookConfigureUrl() {
        when(configuration.getString(WEBHOOK_CONFIGURE_URL_KEY)).thenReturn(WEBHOOK_CONFIGURE_URL);

        assertEquals(WEBHOOK_CONFIGURE_URL, endpointConfigService.getWebhookConfigureUrl());

        verify(configuration).getString(WEBHOOK_CONFIGURE_URL_KEY);
    }

    @Test
    void testGetMerchantDetailsUrl() {
        when(configuration.getString(MERCHANT_DETAILS_URL_KEY)).thenReturn(MERCHANT_DETAILS_URL);

        assertEquals(MERCHANT_DETAILS_URL, endpointConfigService.getMerchantDetailsUrl());

        verify(configuration).getString(MERCHANT_DETAILS_URL_KEY);
    }
}
