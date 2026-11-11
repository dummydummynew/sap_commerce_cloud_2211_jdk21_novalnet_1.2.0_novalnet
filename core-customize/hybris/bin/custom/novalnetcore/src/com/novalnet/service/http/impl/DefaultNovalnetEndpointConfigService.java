package com.novalnet.service.http.impl;

import com.novalnet.service.http.NovalnetEndpointConfigService;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import jakarta.annotation.Resource;


/**
 * Default implementation of {@link NovalnetEndpointConfigService} for retrieving Novalnet API endpoint URLs.
 */
public class DefaultNovalnetEndpointConfigService implements NovalnetEndpointConfigService {

    @Resource
    private ConfigurationService configurationService;

    /**
     * Retrieves the Novalnet payment API URL.
     *
     * @return the configured payment API URL
     */
    @Override
    public String getPaymentUrl() {
        return configurationService.getConfiguration().getString("novalnet.payment.url");
    }

    /**
     * Retrieves the Novalnet hosted payment API URL.
     *
     * @return the configured hosted payment API URL
     */
    @Override
    public String getPaymentHostedUrl() {
        return configurationService.getConfiguration().getString("novalnet.payment.hosted.url");
    }

    /**
     * Retrieves the Novalnet authorization API URL.
     *
     * @return the configured authorization API URL
     */
    @Override
    public String getAuthorizeUrl() {
        return configurationService.getConfiguration().getString("novalnet.authorize.url");
    }

    /**
     * Retrieves the Novalnet hosted authorization API URL.
     *
     * @return the configured hosted authorization API URL
     */
    @Override
    public String getAuthorizeHostedUrl() {
        return configurationService.getConfiguration().getString("novalnet.authorize.hosted.url");
    }

    /**
     * Retrieves the Novalnet transaction capture API URL.
     *
     * @return the configured transaction capture API URL
     */
    @Override
    public String getTransactionCaptureUrl() {
        return configurationService.getConfiguration().getString("novalnet.transaction.capture.url");
    }

    /**
     * Retrieves the Novalnet transaction cancellation API URL.
     *
     * @return the configured transaction cancellation API URL
     */
    @Override
    public String getTransactionCancelUrl() {
        return configurationService.getConfiguration().getString("novalnet.transaction.cancel.url");
    }

    /**
     * Retrieves the Novalnet transaction refund API URL.
     *
     * @return the configured transaction refund API URL
     */
    @Override
    public String getTransactionRefundUrl() {
        return configurationService.getConfiguration().getString("novalnet.transaction.refund.url");
    }

    /**
     * Retrieves the Novalnet transaction details API URL.
     *
     * @return the configured transaction details API URL
     */
    @Override
    public String getTransactionDetailsUrl() {
        return configurationService.getConfiguration().getString("novalnet.transaction.details.url");
    }

    /**
     * Retrieves the Novalnet transaction update API URL.
     *
     * @return the configured transaction update API URL
     */
    @Override
    public String getTransactionUpdateUrl() {
        return configurationService.getConfiguration().getString("novalnet.transaction.update.url");
    }

    /**
     * Retrieves the Novalnet webhook configuration API URL.
     *
     * @return the configured webhook configuration API URL
     */
    @Override
    public String getWebhookConfigureUrl() {
        return configurationService.getConfiguration().getString("novalnet.webhook.configure.url");
    }

    /**
     * Retrieves the Novalnet merchant details API URL.
     *
     * @return the configured merchant details API URL
     */
    @Override
    public String getMerchantDetailsUrl() {
        return configurationService.getConfiguration().getString("novalnet.merchant.details.url");
    }
}


