package com.novalnet.backoffice.editors;

import com.hybris.cockpitng.editors.CockpitEditorRenderer;
import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import de.hybris.platform.store.BaseStoreModel;
import org.apache.log4j.Logger;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import java.util.Map;


/**
 * Custom backoffice editor for selecting a Novalnet tariff ID.
 * <p>
 * Loads available tariffs from the current session and updates the selected tariff when the user changes the selection.
 */
public class TariffIdEditor implements CockpitEditorRenderer<Integer> {
    protected static final String NOVALNET_TARIFF_EVENT = "onNovalnetTariffRefresh";
    private static final Logger LOG = Logger.getLogger(TariffIdEditor.class);
    private static final String SELECTED_TARIFF_VALUE_PREFIX = "SELECTED_TARIFF_VALUE_";
    private static final String CONTEXT_PARENT_OBJECT = "parentObject";
    private static final String NOVALNET_TARIFF_NAMES_PREFIX = "NOVALNET_TARIFF_NAMES_";

    /**
     * Renders the tariff selection editor for the current base store.
     *
     * @param parent   parent ZK component
     * @param context  editor context containing the current value and parameters
     * @param listener listener notified when the tariff selection changes
     *
     */
    @Override
    public void render(Component parent, EditorContext<Integer> context, EditorListener<Integer> listener) {
        BaseStoreModel baseStore = null;
        Object paramObj = context.getParameter(CONTEXT_PARENT_OBJECT);

        if (paramObj instanceof BaseStoreModel basestoremodel) {
            baseStore = basestoremodel;
        }

        if (baseStore == null) {
            LOG.warn("BaseStore is NULL");
            return;
        }

        String storeId = baseStore.getUid();
        Combobox combobox = new Combobox();
        combobox.setWidth("75%");
        combobox.setParent(parent);

        Integer dbValue = context.getInitialValue();
        loadTariffs(combobox, dbValue, storeId);

        EventQueue<Event> queue = EventQueues.lookup(NOVALNET_TARIFF_EVENT, EventQueues.DESKTOP, true);

        if (queue != null) {
            queue.subscribe(event -> {
                combobox.getItems().clear();
                loadTariffs(combobox, dbValue, storeId);
            });
        }

        combobox.addEventListener(Events.ON_SELECT, event -> {
            Comboitem selected = combobox.getSelectedItem();

            if (selected != null) {
                Integer tariffId = (Integer) selected.getValue();
                Sessions.getCurrent().setAttribute(SELECTED_TARIFF_VALUE_PREFIX + storeId, tariffId);
                listener.onValueChanged(tariffId);
            }
        });
    }

    /**
     * Loads the available tariffs from the current session and selects the previously selected or database tariff.
     *
     * @param combobox tariff selection combobox
     * @param dbValue  tariff value stored in the database
     * @param storeId  current base store identifier
     *
     */
    public void loadTariffs(Combobox combobox, Integer dbValue, String storeId) {
        Map<String, String> tariffs = (Map<String, String>) Sessions.getCurrent()
                .getAttribute(NOVALNET_TARIFF_NAMES_PREFIX + storeId);

        combobox.getItems().clear();
        combobox.setSelectedItem(null);
        combobox.setValue("");

        Integer sessionSelected = (Integer) Sessions.getCurrent().getAttribute(SELECTED_TARIFF_VALUE_PREFIX + storeId);

        if (tariffs == null || tariffs.isEmpty()) {
            LOG.warn("Tariff list not available yet");
            return;
        }

        Comboitem selectedItem = null;

        for (Map.Entry<String, String> entry : tariffs.entrySet()) {
            String tariffId = entry.getKey();
            String tariffName = entry.getValue();
            Integer tariffValue = Integer.valueOf(tariffId);

            Comboitem item = combobox.appendItem(tariffName);
            item.setValue(tariffValue);

            if (isSelectedTariff(sessionSelected, dbValue, tariffValue)) {
                selectedItem = item;
            }
        }

        if (selectedItem != null) {
            combobox.setSelectedItem(selectedItem);
        } else if (!tariffs.isEmpty()) {
            combobox.setSelectedIndex(0);
            Sessions.getCurrent().setAttribute(SELECTED_TARIFF_VALUE_PREFIX + storeId, combobox.getSelectedItem().getValue());
        }
    }

    /**
     * Determines whether the specified tariff should be selected. The session value takes precedence over the database
     * value.
     *
     * @param sessionSelected tariff selected in the current session
     * @param dbValue         tariff value stored in the database
     * @param tariffId        tariff ID being evaluated
     * @return true if the tariff should be selected; otherwise false
     */
    private boolean isSelectedTariff(Integer sessionSelected, Integer dbValue, Integer tariffId) {
        if (sessionSelected != null) {
            return sessionSelected.equals(tariffId);
        }

        return dbValue != null && dbValue.equals(tariffId);
    }
}
