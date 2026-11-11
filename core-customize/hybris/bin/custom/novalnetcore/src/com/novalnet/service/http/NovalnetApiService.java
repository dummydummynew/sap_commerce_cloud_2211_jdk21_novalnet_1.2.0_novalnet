/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.http;

import com.novalnet.exception.NovalnetException;
import de.hybris.platform.store.BaseStoreModel;


/**
 * Defines operations for communicating with the Novalnet API.
 */
public interface NovalnetApiService
{
	/**
	 * Sends a request to the specified Novalnet API URL.
	 *
	 * @param url the Novalnet API URL
	 * @param requestBody the request body
	 * @return the API response
	 */
	 String sendRequest(String url, String requestBody);

	/**
	 * Sends a follow-up request to the Novalnet API.
	 *
	 * @param url the Novalnet API URL
	 * @param requestBody the request body
	 * @param baseStore the base store associated with the request
	 * @return the API response
	 */
	 String followupSendRequest(String url, String requestBody, BaseStoreModel baseStore);

	/**
	 * Sends a request to book a transaction amount with Novalnet.
	 *
	 * @param url the Novalnet API URL
	 * @param requestBody the request body
	 * @param baseStore the base store associated with the request
	 * @return the API response
	 */
	 String bookTransactionAmount(String url, String requestBody, BaseStoreModel baseStore);

	/**
	 * Fetches merchant details from the Novalnet API.
	 *
	 * @param url the Novalnet API URL
	 * @param requestBody the request body
	 * @param baseStore the base store associated with the request
	 * @return the merchant details response
	 */
	 String fetchMerchantDetails(String url, String requestBody, BaseStoreModel baseStore);

    /**
     * Configures the Novalnet webhook URL.
     *
     * @param productActivationKey the Novalnet product activation key
     * @param paymentAccessKey     the Novalnet payment access key
     * @param webhookUrl           the webhook URL to configure
     * @param baseStore            the base store associated with the webhook
     * @return the webhook configuration response
     * @throws NovalnetException if an error occurs while configuring the webhook
     */
    String configureWebhookUrl(String productActivationKey, String paymentAccessKey, String webhookUrl,
                               BaseStoreModel baseStore)
            throws NovalnetException;
}


