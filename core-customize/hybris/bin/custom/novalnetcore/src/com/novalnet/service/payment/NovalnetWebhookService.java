/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.novalnet.exception.NovalnetException;
import de.hybris.platform.store.BaseStoreModel;


/**
 * Defines operations for configuring the Novalnet webhook.
 */
public interface NovalnetWebhookService {
    /**
     * Configures the Novalnet webhook URL for the specified base store.
     *
     * @param productKey the Novalnet product activation key
     * @param paymentKey the Novalnet payment access key
     * @param webhookUrl the URL to be configured as the webhook endpoint
     * @param baseStore  the base store associated with the webhook configuration
     * @throws NovalnetException if an error occurs while configuring the webhook
     */
    void configureWebhook(String productKey, String paymentKey, String webhookUrl, BaseStoreModel baseStore)
            throws NovalnetException;
}
