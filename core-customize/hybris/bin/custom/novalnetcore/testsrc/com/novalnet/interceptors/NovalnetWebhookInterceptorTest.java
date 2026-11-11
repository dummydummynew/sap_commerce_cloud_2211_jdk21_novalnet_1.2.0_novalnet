/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.interceptors;

import com.novalnet.facades.NovalnetWebhookConfigFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetWebhookInterceptorTest {
    private static final String PRODUCT_KEY = "productKey123";
    private static final String PAYMENT_KEY = "paymentKey456";
    private static final String WEBHOOK_URL = "https://test.com/novalnet/webhook";

    @Mock
    private NovalnetWebhookConfigFacade novalnetWebhookConfigFacade;

    @Mock
    private BaseStoreModel baseStoreModel;

    @Mock
    private InterceptorContext interceptorContext;

    @InjectMocks
    private NovalnetWebhookInterceptor novalnetWebhookInterceptor;

    @BeforeEach
    public void setUp() {
        given(interceptorContext.isModified(baseStoreModel, BaseStoreModel.NOTIFICATIONWEBHOOKURL)).willReturn(true);
    }


    @Test
    public void shouldConfigureWebhookWhenWebhookUrlIsPresent() throws Exception {
        given(baseStoreModel.getNotificationWebhookUrl()).willReturn(WEBHOOK_URL);
        given(baseStoreModel.getNovalnetAPIKey()).willReturn(PRODUCT_KEY);
        given(baseStoreModel.getNovalnetPaymentAccessKey()).willReturn(PAYMENT_KEY);

        assertThatCode(() -> novalnetWebhookInterceptor.onPrepare(baseStoreModel, interceptorContext)).doesNotThrowAnyException();

        verify(novalnetWebhookConfigFacade).configureWebhook(PRODUCT_KEY, PAYMENT_KEY, WEBHOOK_URL, baseStoreModel);

        verifyNoMoreInteractions(novalnetWebhookConfigFacade);
    }


    @Test
    public void shouldSkipWhenWebhookUrlIsNull() throws InterceptorException {
        given(baseStoreModel.getNotificationWebhookUrl()).willReturn(null);

        assertThatCode(() -> novalnetWebhookInterceptor.onPrepare(baseStoreModel, interceptorContext)).doesNotThrowAnyException();

        verifyNoInteractions(novalnetWebhookConfigFacade);
    }
}

