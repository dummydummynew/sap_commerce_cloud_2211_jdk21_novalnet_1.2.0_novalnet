package com.novalnet.interceptors;

import com.novalnet.exception.NovalnetInterceptorException;
import com.novalnet.facades.NovalnetWebhookConfigFacade;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.annotation.Resource;
import org.apache.log4j.Logger;


/**
 * Interceptor for configuring the Novalnet webhook when the notification webhook URL
 * of a base store is modified.
 * <p>
 * Validates the required webhook configuration values and delegates the webhook
 * configuration to the {@link NovalnetWebhookConfigFacade}.
 */
public class NovalnetWebhookInterceptor implements PrepareInterceptor<BaseStoreModel> {
    private static final Logger LOG = Logger.getLogger(NovalnetWebhookInterceptor.class);


    @Resource(name = "novalnetWebhookConfigFacade")
    private NovalnetWebhookConfigFacade novalnetWebhookConfigFacade;

    /**
     * Configures the Novalnet webhook when the notification webhook URL
     * of the base store is modified.
     *
     * @param baseStore the base store whose webhook configuration was modified
     * @param ctx       the interceptor context containing the modification information
     * @throws InterceptorException if an error occurs while preparing the base store
     */
    @Override
    public void onPrepare(BaseStoreModel baseStore, InterceptorContext ctx) throws InterceptorException {
        if (!ctx.isModified(baseStore, BaseStoreModel.NOTIFICATIONWEBHOOKURL)) {
            return;
        }

        if (baseStore.getNotificationWebhookUrl() == null) {
            return;
        }

        try {
            String productKey = baseStore.getNovalnetAPIKey();
            String paymentKey = baseStore.getNovalnetPaymentAccessKey();
            String webhookUrl = baseStore.getNotificationWebhookUrl();

            if (isEmpty(productKey) || isEmpty(paymentKey) || isEmpty(webhookUrl)) {
                LOG.warn("Missing required webhook configuration values. Skipping API call.");
                return;
            }

            novalnetWebhookConfigFacade.configureWebhook(productKey, paymentKey, webhookUrl, baseStore);
        } catch (Exception e) {
            throw new NovalnetInterceptorException("Unable to configure Novalnet webhook for BaseStore: " + baseStore.getUid(), e);
        }
    }

    /**
     * Checks whether the specified value is null, empty, or contains only whitespace.
     *
     * @param val the value to check
     * @return true if the value is null, empty, or contains only whitespace; otherwise false
     */
    private boolean isEmpty(String val) {
        return val == null || val.trim().isEmpty();
    }
}


