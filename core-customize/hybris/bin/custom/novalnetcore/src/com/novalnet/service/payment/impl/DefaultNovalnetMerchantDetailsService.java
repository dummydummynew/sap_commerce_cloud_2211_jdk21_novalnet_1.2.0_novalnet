package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetMerchantDetailsService;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.annotation.Resource;

import java.util.HashMap;
import java.util.Map;


/**
 * Default implementation for retrieving Novalnet merchant details.
 */
public class DefaultNovalnetMerchantDetailsService implements NovalnetMerchantDetailsService {


    @Resource
    private NovalnetApiService novalnetApiService;
    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public String callNovalnetMerchantDetails(final String productActivationKey, final BaseStoreModel baseStore) throws JsonProcessingException {

        final Map<String, Object> requestMap = new HashMap<>();
        final Map<String, Object> merchantMap = new HashMap<>();
        merchantMap.put("signature", productActivationKey);

        final Map<String, Object> customMap = new HashMap<>();
        customMap.put("lang", baseStore.getDefaultLanguage().getIsocode().toUpperCase());

        requestMap.put("merchant", merchantMap);
        requestMap.put("custom", customMap);

        final String requestBody = objectMapper.writeValueAsString(requestMap);

        return novalnetApiService.fetchMerchantDetails(novalnetEndpointConfigService.getMerchantDetailsUrl(), requestBody, baseStore);


    }
}
