package com.novalnet.facades;


import com.fasterxml.jackson.core.JsonProcessingException;
import de.hybris.platform.store.BaseStoreModel;

/**
 * The interface Novalnet merchant details facade.
 */
public interface NovalnetMerchantDetailsFacade {

    /**
     * Retrieves merchant details from Novalnet.
     *
     * @param productActivationKey the Novalnet product activation key
     * @param baseStore            the base store associated with the request
     * @return the merchant details response
     * @throws JsonProcessingException if the merchant details request fails
     */
    String callNovalnetMerchantDetails(String productActivationKey, BaseStoreModel baseStore) throws JsonProcessingException;

}
