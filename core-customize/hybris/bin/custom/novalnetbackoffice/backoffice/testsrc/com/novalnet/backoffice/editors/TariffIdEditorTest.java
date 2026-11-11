/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.backoffice.editors;

import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class TariffIdEditorTest {
    private static final String STORE_ID = "test-store";
    private static final String TARIFF_ID_ONE = "1001";
    private static final String TARIFF_ID_TWO = "1002";
    private static final String TARIFF_NAME_ONE = "Test Tariff";
    private static final String TARIFF_NAME_TWO = "Premium Tariff";
    private static final String TARIFF_NAMES_ATTRIBUTE = "NOVALNET_TARIFF_NAMES_" + STORE_ID;
    private static final String SELECTED_TARIFF_ATTRIBUTE = "SELECTED_TARIFF_VALUE_" + STORE_ID;
    private static final String CONTEXT_PARENT_OBJECT = "parentObject";
    private static final int EXPECTED_TARIFF_LOAD_COUNT = 2;
    private static final int EXPECTED_TARIFF_COUNT = 2;

    @Mock
    private Session session;

    @Mock
    private EventQueue<Event> eventQueue;

    @Mock
    private Component parentComponent;

    @Mock
    private EditorContext<Integer> editorContext;

    @Mock
    private EditorListener<Integer> editorListener;

    private TariffIdEditor tariffIdEditor;

    @BeforeEach
    void setUp() {
        tariffIdEditor = new TariffIdEditor();
    }

    @Test
    void shouldLoadTariffsSuccessfully() {
        Map<String, String> tariffs = createTariffs();
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(tariffs);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(null);

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, null, STORE_ID);

            assertEquals(EXPECTED_TARIFF_COUNT, combobox.getItems().size());
            assertEquals(TARIFF_NAME_ONE, combobox.getItems().get(0).getLabel());
            assertEquals(TARIFF_NAME_TWO, combobox.getItems().get(1).getLabel());
        }
    }

    @Test
    void shouldSelectTariffFromSession() {
        Map<String, String> tariffs = createTariffs();
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(tariffs);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(Integer.valueOf(TARIFF_ID_TWO));

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, null, STORE_ID);

            assertEquals(EXPECTED_TARIFF_COUNT, combobox.getItems().size());
            assertNotNull(combobox.getSelectedItem());
            assertEquals(Integer.valueOf(TARIFF_ID_TWO), combobox.getSelectedItem().getValue());
            assertEquals(TARIFF_NAME_TWO, combobox.getSelectedItem().getLabel());
        }
    }

    @Test
    void shouldSelectTariffFromDatabaseWhenSessionValueIsNull() {
        Map<String, String> tariffs = createTariffs();
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(tariffs);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(null);

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, Integer.valueOf(TARIFF_ID_ONE), STORE_ID);

            assertEquals(EXPECTED_TARIFF_COUNT, combobox.getItems().size());
            assertNotNull(combobox.getSelectedItem());
            assertEquals(Integer.valueOf(TARIFF_ID_ONE), combobox.getSelectedItem().getValue());
            assertEquals(TARIFF_NAME_ONE, combobox.getSelectedItem().getLabel());
        }
    }

    @Test
    void shouldPreferSessionTariffOverDatabaseTariff() {
        Map<String, String> tariffs = createTariffs();
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(tariffs);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(Integer.valueOf(TARIFF_ID_TWO));

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, Integer.valueOf(TARIFF_ID_ONE), STORE_ID);

            assertNotNull(combobox.getSelectedItem());
            assertEquals(Integer.valueOf(TARIFF_ID_TWO), combobox.getSelectedItem().getValue());
            assertEquals(TARIFF_NAME_TWO, combobox.getSelectedItem().getLabel());
        }
    }

    @Test
    void shouldSelectFirstTariffWhenNoTariffIsSelected() {
        Map<String, String> tariffs = createTariffs();
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(tariffs);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(null);

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, null, STORE_ID);

            assertEquals(EXPECTED_TARIFF_COUNT, combobox.getItems().size());
            assertNotNull(combobox.getSelectedItem(), "Combobox should default-select the first tariff");
            assertEquals(Integer.valueOf(TARIFF_ID_ONE), combobox.getSelectedItem().getValue());
            assertEquals(TARIFF_NAME_ONE, combobox.getSelectedItem().getLabel());

            ArgumentCaptor<Integer> valueCaptor = ArgumentCaptor.forClass(Integer.class);
            verify(session).setAttribute(eq(SELECTED_TARIFF_ATTRIBUTE), valueCaptor.capture());
            assertEquals(combobox.getSelectedItem().getValue(), valueCaptor.getValue());
        }
    }

    @Test
    void shouldHandleNullTariffResponse() {
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(null);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(null);

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, null, STORE_ID);

            assertTrue(combobox.getItems().isEmpty());
            assertNull(combobox.getSelectedItem());
        }
    }

    @Test
    void shouldHandleEmptyTariffResponse() {
        Map<String, String> tariffs = new LinkedHashMap<>();
        when(session.getAttribute(TARIFF_NAMES_ATTRIBUTE)).thenReturn(tariffs);
        when(session.getAttribute(SELECTED_TARIFF_ATTRIBUTE)).thenReturn(null);

        try (MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class)) {
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);
            Combobox combobox = new Combobox();

            tariffIdEditor.loadTariffs(combobox, null, STORE_ID);

            assertTrue(combobox.getItems().isEmpty());
            assertNull(combobox.getSelectedItem());
        }
    }

    @Test
    void shouldRenderEditorWhenBaseStoreIsValid() {
        BaseStoreModelWrapper baseStore = new BaseStoreModelWrapper(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStore.getBaseStore());
        when(editorContext.getInitialValue()).thenReturn(Integer.valueOf(TARIFF_ID_ONE));

        TariffIdEditor editorSpy = spy(tariffIdEditor);
        doNothing().when(editorSpy).loadTariffs(any(Combobox.class), eq(Integer.valueOf(TARIFF_ID_ONE)), eq(STORE_ID));

        try (MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedConstruction<Combobox> comboboxConstruction = mockConstruction(Combobox.class)) {
            eventQueuesMock
                    .when(() -> EventQueues.lookup(eq(TariffIdEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(null);

            editorSpy.render(parentComponent, editorContext, editorListener);

            Combobox combobox = comboboxConstruction.constructed().get(0);
            verify(combobox).setWidth("75%");
            verify(combobox).setParent(parentComponent);
            verify(editorSpy).loadTariffs(combobox, Integer.valueOf(TARIFF_ID_ONE), STORE_ID);
        }
    }

    @Test
    void shouldSkipRenderingWhenBaseStoreIsMissing() {
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(null);

        TariffIdEditor editorSpy = spy(tariffIdEditor);

        try (MockedConstruction<Combobox> comboboxConstruction = mockConstruction(Combobox.class)) {
            editorSpy.render(parentComponent, editorContext, editorListener);

            assertTrue(comboboxConstruction.constructed().isEmpty(), "No Combobox should be created when BaseStore is missing");
            verify(editorSpy, never()).loadTariffs(any(Combobox.class), any(), anyString());
        }
    }

    @Test
    void shouldSkipRenderingWhenParentObjectIsNotBaseStore() {
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn("invalid-parent");

        TariffIdEditor editorSpy = spy(tariffIdEditor);

        try (MockedConstruction<Combobox> comboboxConstruction = mockConstruction(Combobox.class)) {
            editorSpy.render(parentComponent, editorContext, editorListener);

            assertTrue(comboboxConstruction.constructed().isEmpty(), "No Combobox should be created for an invalid parent object");
            verify(editorSpy, never()).loadTariffs(any(Combobox.class), any(), anyString());
        }
    }

    @Test
    void shouldProcessSelectionEventWhenTariffIsSelected() throws Exception {
        BaseStoreModelWrapper baseStore = new BaseStoreModelWrapper(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStore.getBaseStore());
        when(editorContext.getInitialValue()).thenReturn(null);

        try (MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedConstruction<Combobox> comboboxConstruction = mockConstruction(Combobox.class, (combobox, context) -> {
                 Comboitem selectedItem = mock(Comboitem.class);
                 when(selectedItem.getValue()).thenReturn(Integer.valueOf(TARIFF_ID_TWO));
                 when(combobox.getSelectedItem()).thenReturn(selectedItem);
                 when(combobox.getItems()).thenReturn(new ArrayList<>());
             })) {
            eventQueuesMock
                    .when(() -> EventQueues.lookup(eq(TariffIdEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(null);
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);

            tariffIdEditor.render(parentComponent, editorContext, editorListener);

            Combobox combobox = comboboxConstruction.constructed().get(0);
            ArgumentCaptor<EventListener<Event>> listenerCaptor = ArgumentCaptor.forClass(EventListener.class);
            verify(combobox).addEventListener(eq(Events.ON_SELECT), listenerCaptor.capture());

            listenerCaptor.getValue().onEvent(mock(Event.class));

            verify(session).setAttribute(SELECTED_TARIFF_ATTRIBUTE, Integer.valueOf(TARIFF_ID_TWO));
            verify(editorListener).onValueChanged(Integer.valueOf(TARIFF_ID_TWO));
        }
    }

    @Test
    void shouldIgnoreSelectionEventWhenNoTariffIsSelected() throws Exception {
        BaseStoreModelWrapper baseStore = new BaseStoreModelWrapper(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStore.getBaseStore());
        when(editorContext.getInitialValue()).thenReturn(null);

        try (MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedConstruction<Combobox> comboboxConstruction = mockConstruction(Combobox.class,
                     (combobox, context) -> when(combobox.getSelectedItem()).thenReturn(null))) {
            eventQueuesMock
                    .when(() -> EventQueues.lookup(eq(TariffIdEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(null);
            sessionsMock.when(Sessions::getCurrent).thenReturn(session);

            tariffIdEditor.render(parentComponent, editorContext, editorListener);

            Combobox combobox = comboboxConstruction.constructed().get(0);
            ArgumentCaptor<EventListener<Event>> listenerCaptor = ArgumentCaptor.forClass(EventListener.class);
            verify(combobox).addEventListener(eq(Events.ON_SELECT), listenerCaptor.capture());

            clearInvocations(session, editorListener);
            listenerCaptor.getValue().onEvent(mock(Event.class));

            verifyNoInteractions(session, editorListener);
        }
    }

    @Test
    void shouldSubscribeToTariffRefreshEvent() throws Exception {
        BaseStoreModelWrapper baseStore = new BaseStoreModelWrapper(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStore.getBaseStore());
        when(editorContext.getInitialValue()).thenReturn(Integer.valueOf(TARIFF_ID_ONE));

        TariffIdEditor editorSpy = spy(tariffIdEditor);
        doNothing().when(editorSpy).loadTariffs(any(Combobox.class), eq(Integer.valueOf(TARIFF_ID_ONE)), eq(STORE_ID));

        try (MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedConstruction<Combobox> comboboxConstruction = mockConstruction(Combobox.class,
                     (mock, context) -> when(mock.getItems()).thenReturn(mock(List.class)))) {
            eventQueuesMock
                    .when(() -> EventQueues.lookup(eq(TariffIdEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(eventQueue);

            editorSpy.render(parentComponent, editorContext, editorListener);

            Combobox combobox = comboboxConstruction.constructed().get(0);
            List<?> items = combobox.getItems();
            ArgumentCaptor<EventListener<Event>> refreshListenerCaptor = ArgumentCaptor.forClass(EventListener.class);
            verify(eventQueue).subscribe(refreshListenerCaptor.capture());
            verify(editorSpy).loadTariffs(combobox, Integer.valueOf(TARIFF_ID_ONE), STORE_ID);

            refreshListenerCaptor.getValue().onEvent(mock(Event.class));

            verify(items).clear();
            verify(editorSpy, times(EXPECTED_TARIFF_LOAD_COUNT)).loadTariffs(combobox, Integer.valueOf(TARIFF_ID_ONE), STORE_ID);
        }
    }

    private Map<String, String> createTariffs() {
        Map<String, String> tariffs = new LinkedHashMap<>();
        tariffs.put(TARIFF_ID_ONE, TARIFF_NAME_ONE);
        tariffs.put(TARIFF_ID_TWO, TARIFF_NAME_TWO);
        return tariffs;
    }

    private static class BaseStoreModelWrapper {
        private final BaseStoreModel baseStore;

        BaseStoreModelWrapper(String storeId) {
            baseStore = mock(BaseStoreModel.class);
            when(baseStore.getUid()).thenReturn(storeId);
        }

        BaseStoreModel getBaseStore() {
            return baseStore;
        }
    }
}
