/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.facades.impl;

import com.novalnet.exception.NovalnetException;
import com.novalnet.facades.NovalnetWebhookConfigFacade;
import com.novalnet.service.payment.NovalnetWebhookService;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.annotation.Resource;


/**
 * * Default implementation of {@link NovalnetWebhookConfigFacade}. *
 * <p>
 * * Delegates webhook configuration to the {@link NovalnetWebhookService}.
 */
public class DefaultNovalnetWebhookConfigFacade implements NovalnetWebhookConfigFacade {
    @Resource(name = "novalnetWebhookService")
    private NovalnetWebhookService novalnetWebhookService;

    /**
     * Configures the Novalnet webhook for the given base store.
     *
     * @param productKey the Novalnet product key
     * @param paymentKey the Novalnet payment key
     * @param webhookUrl the URL to be configured as the webhook endpoint
     * @param baseStore  the base store for which the webhook is configured
     * @throws NovalnetException if an error occurs while configuring the webhook
     */
    @Override
    public void configureWebhook(String productKey, String paymentKey, String webhookUrl, BaseStoreModel baseStore)
            throws NovalnetException {
        novalnetWebhookService.configureWebhook(productKey, paymentKey, webhookUrl, baseStore);
    }
}
