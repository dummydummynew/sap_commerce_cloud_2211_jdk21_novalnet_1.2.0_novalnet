package com.novalnet.facades.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novalnet.facades.NovalnetMerchantDetailsFacade;
import com.novalnet.service.payment.NovalnetMerchantDetailsService;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.annotation.Resource;

/**
 * The type Default novalnet merchant details facade.
 */
public class DefaultNovalnetMerchantDetailsFacade implements NovalnetMerchantDetailsFacade {

    @Resource
    private NovalnetMerchantDetailsService novalnetMerchantDetailsService;

    @Override
    public String callNovalnetMerchantDetails(final String productActivationKey, final BaseStoreModel baseStore) throws JsonProcessingException {
        return novalnetMerchantDetailsService.callNovalnetMerchantDetails(productActivationKey, baseStore);
    }
}
