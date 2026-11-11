/*

 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.

 */

package com.novalnet.service.http.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.novalnet.exception.NovalnetException;
import com.novalnet.service.http.NovalnetApiService;

import com.novalnet.service.http.NovalnetEndpointConfigService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import jakarta.annotation.Resource;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link NovalnetApiService} for communicating with the Novalnet API.
 *
 * <p>
 * <p>
 * Handles HTTP requests for transaction processing, follow-up requests, merchant details, and webhook configuration using the payment access
 * <p>
 * key configured for the current base store.
 *
 * </p>
 */

public class DefaultNovalnetApiService implements NovalnetApiService {

    private static final Logger LOGGER = Logger.getLogger(DefaultNovalnetApiService.class);
    private static final int CONNECT_TIMEOUT_MS = 20000;
    private static final int READ_TIMEOUT_MS = 60000;
    private static final String HEADER_CONTENT_TYPE = "Content-Type";
    private static final String HEADER_ACCEPT = "Accept";
    private static final String HEADER_ACCESS_KEY = "X-NN-Access-Key";
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String ERROR_CALLING_NOVALNET_API = "Error while calling Novalnet API";

    @Resource
    private BaseStoreService baseStoreService;


    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    /**
     * Encodes the Novalnet payment access key using Base64 encoding.
     *
     * @param** accessKey
     * <p>
     * the Novalnet payment access key
     * @return** the Base64-encoded access key
     */

    private static String encodeAccessKey(String accessKey) {
        return Base64.getEncoder().encodeToString(accessKey.trim().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Sends a request to the Novalnet API using the payment access key configured for the current base store.
     *
     * @param** url
     * <p>
     * the Novalnet API URL
     * @param** jsonString
     * <p>
     * the JSON request body
     * @return** the API response as a {@link String}; an empty response if the request fails
     */

    @Override
    public String sendRequest(String url, String jsonString) {
        BaseStoreModel baseStore = baseStoreService.getCurrentBaseStore();
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(READ_TIMEOUT_MS))
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON).header(HEADER_ACCEPT, CONTENT_TYPE_JSON)
                .header(HEADER_ACCESS_KEY, encodeAccessKey(baseStore.getNovalnetPaymentAccessKey()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonString)).build();
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.error(ERROR_CALLING_NOVALNET_API, ex);
            return "";
        }
    }

    /**
     * Sends a follow-up request to the Novalnet API using the payment access key configured for the specified base store.
     *
     * @param** url
     * <p>
     * the Novalnet API URL
     * @param** jsonString
     * <p>
     * the JSON request body
     * @param** baseStore
     * <p>
     * the base store containing the Novalnet payment access key
     * @return** the API response as a {@link String}; an empty response if the request fails
     * @throws** IllegalArgumentException
     * <p>
     * if the base store is null
     */

    @Override

    public String followupSendRequest(String url, String jsonString, BaseStoreModel baseStore) {
        if (baseStore == null) {
            throw new IllegalArgumentException("BaseStore cannot be null");
        }

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(READ_TIMEOUT_MS))

                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON).header(HEADER_ACCEPT, CONTENT_TYPE_JSON)

                .header(HEADER_ACCESS_KEY, encodeAccessKey(baseStore.getNovalnetPaymentAccessKey()))

                .POST(HttpRequest.BodyPublishers.ofString(jsonString)).build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (IOException | InterruptedException ex) {

            if (ex instanceof InterruptedException) {

                Thread.currentThread().interrupt();

            }

            LOGGER.error(ERROR_CALLING_NOVALNET_API, ex);

            return "";

        }

    }

    /**
     * Sends a request to the Novalnet API to book a transaction amount.
     *
     * @param** url
     * <p>
     * the Novalnet API URL
     * @param** jsonString
     * <p>
     * the JSON request body
     * @param** baseStore
     * <p>
     * the base store containing the Novalnet payment access key
     * @return** the API response as a {@link String}; an empty response if the request fails
     */

    @Override

    public String bookTransactionAmount(String url, String jsonString, BaseStoreModel baseStore) {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(READ_TIMEOUT_MS))

                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON).header(HEADER_ACCEPT, CONTENT_TYPE_JSON)

                .header(HEADER_ACCESS_KEY, encodeAccessKey(baseStore.getNovalnetPaymentAccessKey()))

                .POST(HttpRequest.BodyPublishers.ofString(jsonString)).build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (IOException | InterruptedException ex) {

            if (ex instanceof InterruptedException) {

                Thread.currentThread().interrupt();

            }

            LOGGER.error(ERROR_CALLING_NOVALNET_API, ex);

            return "";

        }

    }

    /**
     * Fetches merchant details from the Novalnet API.
     *
     * @param** url
     * <p>
     * the Novalnet API URL
     * @param** jsonBody
     * <p>
     * the JSON request body
     * @param** baseStore
     * <p>
     * the base store containing the Novalnet payment access key
     * @return** the merchant details response, or an empty string if the request fails
     */

    @Override

    public String fetchMerchantDetails(String url, String jsonBody, BaseStoreModel baseStore) {

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofMillis(READ_TIMEOUT_MS))

                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON).header(HEADER_ACCEPT, CONTENT_TYPE_JSON)

                .header(HEADER_ACCESS_KEY, encodeAccessKey(baseStore.getNovalnetPaymentAccessKey()))

                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (IOException | InterruptedException ex) {

            if (ex instanceof InterruptedException) {

                Thread.currentThread().interrupt();

            }

            LOGGER.error(ERROR_CALLING_NOVALNET_API, ex);

            return "";

        }

    }

    /**
     * Configures the webhook URL for the specified base store through the Novalnet API.
     *
     * @param** productActivationKey
     * <p>
     * the Novalnet product activation key
     * @param** paymentAccessKey
     * <p>
     * the Novalnet payment access key
     * @param** webhookUrl
     * <p>
     * the webhook URL to configure
     * @param** baseStore
     * <p>
     * the base store associated with the webhook configuration
     * @return** the Novalnet API response, or an empty string if the request fails
     * @throws** NovalnetException
     * <p>
     * if an error occurs while configuring the webhook
     */

    @Override

    public String configureWebhookUrl(String productActivationKey, String paymentAccessKey, String webhookUrl,
                                      BaseStoreModel baseStore) throws NovalnetException {

        String languageIso = baseStore.getDefaultLanguage().getIsocode().toUpperCase();

        Map<String, Object> requestMap = new HashMap<>();

        Map<String, Object> merchantMap = new HashMap<>();

        merchantMap.put("signature", productActivationKey);

        Map<String, Object> webhookMap = new HashMap<>();

        webhookMap.put("url", webhookUrl);

        Map<String, Object> customMap = new HashMap<>();

        customMap.put("lang", languageIso);

        requestMap.put("merchant", merchantMap);

        requestMap.put("webhook", webhookMap);

        requestMap.put("custom", customMap);

        ObjectMapper mapper = new ObjectMapper();

        String jsonBody;

        try {

            jsonBody = mapper.writeValueAsString(requestMap);

        } catch (JsonProcessingException e) {

            throw new NovalnetException("Unable to create Novalnet webhook request JSON", e);

        }

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(CONNECT_TIMEOUT_MS)).build();

        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(novalnetEndpointConfigService.getWebhookConfigureUrl()))

                .timeout(Duration.ofMillis(READ_TIMEOUT_MS)).header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)

                .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)

                .header(HEADER_ACCESS_KEY, Base64.getEncoder().encodeToString(paymentAccessKey.getBytes(StandardCharsets.UTF_8)))

                .POST(HttpRequest.BodyPublishers.ofString(jsonBody)).build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            return response.body();

        } catch (IOException | InterruptedException ex) {

            if (ex instanceof InterruptedException) {

                Thread.currentThread().interrupt();

            }

            LOGGER.error(ERROR_CALLING_NOVALNET_API, ex);

            return "";

        }

    }

}


