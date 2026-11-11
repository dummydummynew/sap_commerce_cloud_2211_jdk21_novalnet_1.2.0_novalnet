/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetMerchantDetailsServiceTest {
    private static final String ACTIVATION_KEY = "activation-key";
    private static final String MERCHANT_URL = "https://merchant.example.test";
    private static final String RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"}}";

    private DefaultNovalnetMerchantDetailsService service;

    @Mock
    private NovalnetApiService apiService;
    @Mock
    private NovalnetEndpointConfigService endpointConfigService;
    @Mock
    private BaseStoreModel baseStore;
    @Mock
    private LanguageModel language;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetMerchantDetailsService();
        ReflectionTestUtils.setField(service, "novalnetApiService", apiService);
        ReflectionTestUtils.setField(service, "novalnetEndpointConfigService", endpointConfigService);
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper());
    }

    @Test
    void shouldBuildMerchantRequestAndReturnApiResponse() throws Exception {
        when(baseStore.getDefaultLanguage()).thenReturn(language);
        when(language.getIsocode()).thenReturn("en");
        when(endpointConfigService.getMerchantDetailsUrl()).thenReturn(MERCHANT_URL);
        when(apiService.fetchMerchantDetails(eq(MERCHANT_URL), any(String.class), eq(baseStore))).thenReturn(RESPONSE);

        assertEquals(RESPONSE, service.callNovalnetMerchantDetails(ACTIVATION_KEY, baseStore));
        verify(apiService).fetchMerchantDetails(eq(MERCHANT_URL), any(String.class), eq(baseStore));
    }
}
