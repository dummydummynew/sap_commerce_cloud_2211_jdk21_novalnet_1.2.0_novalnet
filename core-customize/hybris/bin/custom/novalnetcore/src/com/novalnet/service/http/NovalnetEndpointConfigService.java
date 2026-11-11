package com.novalnet.service.http;

/**
 * Defines operations for retrieving Novalnet API endpoint configuration.
 */
public interface NovalnetEndpointConfigService {
    /**
     * Retrieves the Novalnet payment API URL.
     *
     * @return the payment API URL
     */
    String getPaymentUrl();

    /**
     * Retrieves the Novalnet hosted payment API URL.
     *
     * @return the hosted payment API URL
     */
    String getPaymentHostedUrl();

    /**
     * Retrieves the Novalnet authorization API URL.
     *
     * @return the authorization API URL
     */
    String getAuthorizeUrl();

    /**
     * Retrieves the Novalnet hosted authorization API URL.
     *
     * @return the hosted authorization API URL
     */
    String getAuthorizeHostedUrl();

    /**
     * Retrieves the Novalnet transaction capture API URL.
     *
     * @return the transaction capture API URL
     */
    String getTransactionCaptureUrl();

    /**
     * Retrieves the Novalnet transaction cancellation API URL.
     *
     * @return the transaction cancellation API URL
     */
    String getTransactionCancelUrl();

    /**
     * Retrieves the Novalnet transaction refund API URL.
     *
     * @return the transaction refund API URL
     */
    String getTransactionRefundUrl();

    /**
     * Retrieves the Novalnet transaction details API URL.
     *
     * @return the transaction details API URL
     */
    String getTransactionDetailsUrl();

    /**
     * Retrieves the Novalnet transaction update API URL.
     *
     * @return the transaction update API URL
     */
    String getTransactionUpdateUrl();

    /**
     * Retrieves the Novalnet webhook configuration API URL.
     *
     * @return the webhook configuration API URL
     */
    String getWebhookConfigureUrl();

    /**
     * Retrieves the Novalnet merchant details API URL.
     *
     * @return the merchant details API URL
     */
    String getMerchantDetailsUrl();

}

