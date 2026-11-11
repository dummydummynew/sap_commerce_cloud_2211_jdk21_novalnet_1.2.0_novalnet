package com.novalnet.backoffice.editors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.novalnet.facades.NovalnetMerchantDetailsFacade;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import java.util.HashMap;
import java.util.Map;


/**
 * Custom backoffice editor for handling the Novalnet product activation key.
 * <p>
 * Stores the activation key in the current session and retrieves Novalnet merchant
 * details to update the client key and tariff information for the selected base store.
 */
public class ProductActivationKeyEditor implements CockpitEditorRenderer<String> {
    protected static final String NOVALNET_TARIFF_EVENT = "onNovalnetTariffRefresh";
    private static final Logger LOG = Logger.getLogger(ProductActivationKeyEditor.class);
    private static final String PAYMENT_KEY_PREFIX = "PAYMENT_KEY_";
    private static final String CONTEXT_PARENT_OBJECT = "parentObject";
    private static final String PRODUCT_KEY_PREFIX = "PRODUCT_KEY_";
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String ERROR_TITLE = "Error";
    private ModelService modelService;
    private NovalnetMerchantDetailsFacade novalnetMerchantDetailsFacade;

    /**
     * Sets the merchant-details facade used to retrieve merchant details.
     *
     * @param novalnetMerchantDetailsFacade the merchant-details facade
     */
    public void setNovalnetMerchantDetailsFacade(NovalnetMerchantDetailsFacade novalnetMerchantDetailsFacade) {
        this.novalnetMerchantDetailsFacade = novalnetMerchantDetailsFacade;
    }

    /**
     * Sets the model service used to save and refresh the base store.
     *
     * @param modelService the model service
     */
    public void setModelService(ModelService modelService) {
        this.modelService = modelService;
    }

    /**
     * Renders the product activation key editor and registers a listener
     * to process Novalnet merchant details when the activation key changes.
     *
     * @param parent   the parent component for the editor
     * @param context  the editor context containing the current value and parameters
     * @param listener the listener notified when the activation key changes
     */
    @Override
    public void render(Component parent, EditorContext<String> context, EditorListener<String> listener) {
        BaseStoreModel baseStore = null;
        Object paramObj = context.getParameter(CONTEXT_PARENT_OBJECT);

        if (paramObj instanceof BaseStoreModel basestoremodel) {
            baseStore = basestoremodel;
        }

        if (baseStore == null) {
            LOG.warn("BaseStore not available, skipping editor render");
            return;
        }

        String storeId = baseStore.getUid();

        Textbox textbox = new Textbox();
        textbox.setWidth("75%");
        textbox.setParent(parent);

        String dbValue = context.getInitialValue();

        if (NovalnetUtils.isPopulated(dbValue)) {
            textbox.setValue(dbValue);
            Sessions.getCurrent().setAttribute(PRODUCT_KEY_PREFIX + storeId, dbValue.trim());

            try {
                processNovalnetMerchantDetails(context, storeId);
            } catch (Exception e) {
                LOG.error("Error while calling Novalnet API on init", e);
            }
        }

        textbox.addEventListener(Events.ON_CHANGE, event -> {
            String productKey = textbox.getValue();

            listener.onValueChanged(productKey);

            Sessions.getCurrent().setAttribute(PRODUCT_KEY_PREFIX + storeId, productKey.trim());

            try {
                processNovalnetMerchantDetails(context, storeId);
            } catch (Exception e) {
                LOG.error("Error while calling Novalnet API on change", e);
            }
        });
    }

    /**
     * Retrieves the stored Novalnet product and payment keys and calls the
     * Novalnet merchant details API to update client and tariff information.
     *
     * @param context the editor context containing the parent base store
     * @param storeId the identifier of the current base store
     * @throws Exception if the Novalnet merchant details request or response processing fails
     */
    private void processNovalnetMerchantDetails(EditorContext<String> context, String storeId) throws Exception {
        String productKey = (String) Sessions.getCurrent().getAttribute(PRODUCT_KEY_PREFIX + storeId);

        String paymentKey = (String) Sessions.getCurrent().getAttribute(PAYMENT_KEY_PREFIX + storeId);

        if (productKey == null || productKey.trim().isEmpty() || paymentKey == null || paymentKey.trim().isEmpty()) {
            LOG.warn("Missing Novalnet Keys for Store: " + storeId);
            return;
        }

        Object paramObj = context.getParameter(CONTEXT_PARENT_OBJECT);

        if (!(paramObj instanceof BaseStoreModel)) {
            LOG.warn("BaseStore not available");
            return;
        }

        BaseStoreModel baseStore = (BaseStoreModel) paramObj;

        String response = novalnetMerchantDetailsFacade.callNovalnetMerchantDetails(productKey, baseStore);

        Map<String, Object> jsonMap = MAPPER.readValue(response, Map.class);

        Map<String, Object> result = (Map<String, Object>) jsonMap.get("result");

        String status = (String) result.get("status");

        Map<String, String> tariffMapSession = new HashMap<>();

        if ("SUCCESS".equalsIgnoreCase(status)) {
            Map<String, Object> merchantMap = (Map<String, Object>) jsonMap.get("merchant");

            Map<String, Object> tariffMap = (Map<String, Object>) merchantMap.get("tariff");

            String clientKey = (String) merchantMap.get("client_key");

            baseStore.setNovalnetClientKey(clientKey);

            modelService.save(baseStore);
            modelService.refresh(baseStore);

            for (Map.Entry<String, Object> entry : tariffMap.entrySet()) {
                String tariffId = entry.getKey();

                Map<String, Object> tariff = (Map<String, Object>) entry.getValue();

                String name = (String) tariff.get("name");

                if (name != null) {
                    tariffMapSession.put(tariffId, name);
                }
            }

            Sessions.getCurrent().setAttribute("NOVALNET_TARIFF_NAMES_" + storeId, tariffMapSession);
        } else {
            Sessions.getCurrent().setAttribute("NOVALNET_TARIFF_NAMES_" + storeId, new HashMap<>());

            String statustext = (String) result.get("status_text");

            Messagebox.show(statustext, ERROR_TITLE, Messagebox.OK, Messagebox.ERROR);

            LOG.warn("Novalnet API failed: " + statustext);
        }

        EventQueue<Event> queue = EventQueues.lookup(NOVALNET_TARIFF_EVENT, EventQueues.DESKTOP, true);

        queue.publish(new Event(NOVALNET_TARIFF_EVENT));
    }
}
