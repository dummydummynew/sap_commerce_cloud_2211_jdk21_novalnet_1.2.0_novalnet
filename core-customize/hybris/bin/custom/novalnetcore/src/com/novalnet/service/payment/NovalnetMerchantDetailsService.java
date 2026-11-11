/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.hybris.platform.store.BaseStoreModel;


/**
 * The interface Novalnet merchant details service.
 */
public interface NovalnetMerchantDetailsService {
    /**
     * Call novalnet merchant details string.
     *
     * @param productActivationKey the product activation key
     * @param baseStore            the base store
     * @return the string
     * @throws JsonProcessingException the json processing exception
     */
    String callNovalnetMerchantDetails(String productActivationKey, BaseStoreModel baseStore) throws JsonProcessingException;
}
