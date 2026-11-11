
/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades;

import com.novalnet.exception.NovalnetException;
import de.hybris.platform.store.BaseStoreModel;


/**
 * Defines operations for configuring the Novalnet webhook.
 */
public interface NovalnetWebhookConfigFacade {
    /**
     * Configures the Novalnet webhook for the given base store.
     *
     * @param productKey the Novalnet product key
     * @param paymentKey the Novalnet payment key
     * @param webhookUrl the URL to be configured as the webhook endpoint
     * @param baseStore  the base store for which the webhook is configured
     * @throws NovalnetException if an error occurs while configuring the webhook
     */
    void configureWebhook(String productKey, String paymentKey, String webhookUrl, BaseStoreModel baseStore)
            throws NovalnetException;

}


