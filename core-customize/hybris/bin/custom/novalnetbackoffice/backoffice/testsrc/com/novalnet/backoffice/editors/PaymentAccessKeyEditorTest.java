/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.backoffice.editors;

import com.hybris.cockpitng.editors.EditorContext;
import com.hybris.cockpitng.editors.EditorListener;
import com.novalnet.facades.NovalnetMerchantDetailsFacade;
import com.novalnet.util.NovalnetUtils;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.servicelayer.model.ModelService;
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
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class PaymentAccessKeyEditorTest {
    private static final String STORE_ID = "testStore";
    private static final String PRODUCT_KEY_SESSION_ATTR = "PRODUCT_KEY_" + STORE_ID;
    private static final String PAYMENT_KEY_SESSION_ATTR = "PAYMENT_KEY_" + STORE_ID;
    private static final String TARIFF_SESSION_ATTR = "NOVALNET_TARIFF_NAMES_" + STORE_ID;
    private static final String CONTEXT_PARENT_OBJECT = "parentObject";
    private static final String PRODUCT_KEY = "PRODUCT-KEY-123";
    private static final String EXISTING_PAYMENT_KEY = "EXISTING-PAYMENT-KEY";
    private static final String NEW_PAYMENT_KEY = "NEW-PAYMENT-KEY";
    private static final String SUCCESS_RESPONSE = "{\"result\":{\"status\":\"SUCCESS\"},\"merchant\":{\"client_key\":\"CLIENT-KEY-999\",\"tariff\":{\"1\":{\"name\":\"Standard\"},\"2\":{\"name\":\"Premium\"}}}}";
    private static final String FAILURE_RESPONSE = "{\"result\":{\"status\":\"FAILURE\",\"status_text\":\"Invalid product key\"}}";
    @Mock
    private ModelService modelService;
    @Mock
    private NovalnetMerchantDetailsFacade novalnetMerchantDetailsFacade;
    @Mock
    private EditorContext<String> editorContext;
    @Mock
    private EditorListener<String> editorListener;
    @Mock
    private Component parentComponent;
    @Mock
    private BaseStoreModel baseStoreModel;
    @Mock
    private Session zkSession;
    @Mock
    private EventQueue<Event> eventQueue;
    private PaymentAccessKeyEditor editor;

    @BeforeEach
    void setUp() {
        editor = new PaymentAccessKeyEditor();
        editor.setModelService(modelService);
        editor.setNovalnetMerchantDetailsFacade(novalnetMerchantDetailsFacade);
    }

    @Test
    void shouldShowErrorAndSkipTextboxCreationWhenBaseStoreIsMissing() {
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(null);
        try (MockedStatic<Messagebox> messageboxMock = mockStatic(Messagebox.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class)) {
            editor.render(parentComponent, editorContext, editorListener);
            messageboxMock.verify(() -> Messagebox.show(anyString(), eq("Error"), eq(Messagebox.OK), eq(Messagebox.ERROR)));
            assertTrue(textboxConstruction.constructed().isEmpty(),
                    "No Textbox should be created when the BaseStore parameter is missing");
        }
        verifyNoInteractions(novalnetMerchantDetailsFacade, modelService);
    }

    @Test
    void shouldConfigureTextboxAndProcessMerchantDetailsWhenInitialValueIsPopulated() throws Exception {
        when(baseStoreModel.getUid()).thenReturn(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStoreModel);
        when(editorContext.getInitialValue()).thenReturn(EXISTING_PAYMENT_KEY);
        when(zkSession.getAttribute(PRODUCT_KEY_SESSION_ATTR)).thenReturn(PRODUCT_KEY);
        when(zkSession.getAttribute(PAYMENT_KEY_SESSION_ATTR)).thenReturn(EXISTING_PAYMENT_KEY);
        when(novalnetMerchantDetailsFacade.callNovalnetMerchantDetails(PRODUCT_KEY, baseStoreModel)).thenReturn(SUCCESS_RESPONSE);
        try (MockedStatic<NovalnetUtils> utilsMock = mockStatic(NovalnetUtils.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class)) {
            utilsMock.when(() -> NovalnetUtils.isPopulated(EXISTING_PAYMENT_KEY)).thenReturn(true);
            sessionsMock.when(Sessions::getCurrent).thenReturn(zkSession);
            eventQueuesMock.when(
                            () -> EventQueues.lookup(eq(PaymentAccessKeyEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(eventQueue);
            editor.render(parentComponent, editorContext, editorListener);
            Textbox textbox = textboxConstruction.constructed().get(0);
            verify(textbox).setWidth("75%");
            verify(textbox).setParent(parentComponent);
            verify(textbox).setValue(EXISTING_PAYMENT_KEY);
            verify(zkSession).setAttribute(PAYMENT_KEY_SESSION_ATTR, EXISTING_PAYMENT_KEY);
            verify(baseStoreModel).setNovalnetClientKey("CLIENT-KEY-999");
            verify(modelService).save(baseStoreModel);
            verify(modelService).refresh(baseStoreModel);
            ArgumentCaptor<Map<String, String>> tariffCaptor = ArgumentCaptor.forClass(Map.class);
            verify(zkSession).setAttribute(eq(TARIFF_SESSION_ATTR), tariffCaptor.capture());
            Map<String, String> expectedTariffs = new HashMap<>();
            expectedTariffs.put("1", "Standard");
            expectedTariffs.put("2", "Premium");
            assertEquals(expectedTariffs, tariffCaptor.getValue(),
                    "Tariff id-to-name map should be built from the merchant details response");
            verify(eventQueue).publish(any(Event.class));
        }
    }

    @Test
    void shouldSkipMerchantDetailProcessingWhenInitialValueIsBlank() {
        when(baseStoreModel.getUid()).thenReturn(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStoreModel);
        when(editorContext.getInitialValue()).thenReturn(null);
        try (MockedStatic<NovalnetUtils> utilsMock = mockStatic(NovalnetUtils.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class)) {
            utilsMock.when(() -> NovalnetUtils.isPopulated(null)).thenReturn(false);
            editor.render(parentComponent, editorContext, editorListener);
            Textbox textbox = textboxConstruction.constructed().get(0);
            verify(textbox, never()).setValue(anyString());
        }
        verifyNoInteractions(novalnetMerchantDetailsFacade, modelService);
    }

    @Test
    void shouldNotifyListenerAndReprocessMerchantDetailsWhenTextboxValueChanges() throws Exception {
        when(baseStoreModel.getUid()).thenReturn(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStoreModel);
        when(editorContext.getInitialValue()).thenReturn(null);
        when(zkSession.getAttribute(PRODUCT_KEY_SESSION_ATTR)).thenReturn(PRODUCT_KEY);
        when(zkSession.getAttribute(PAYMENT_KEY_SESSION_ATTR)).thenReturn(NEW_PAYMENT_KEY);
        when(novalnetMerchantDetailsFacade.callNovalnetMerchantDetails(PRODUCT_KEY, baseStoreModel)).thenReturn(SUCCESS_RESPONSE);
        try (MockedStatic<NovalnetUtils> utilsMock = mockStatic(NovalnetUtils.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class,
                     (mockTextbox, context) -> when(mockTextbox.getValue()).thenReturn(NEW_PAYMENT_KEY))) {
            utilsMock.when(() -> NovalnetUtils.isPopulated(null)).thenReturn(false);
            sessionsMock.when(Sessions::getCurrent).thenReturn(zkSession);
            eventQueuesMock.when(
                            () -> EventQueues.lookup(eq(PaymentAccessKeyEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(eventQueue);
            editor.render(parentComponent, editorContext, editorListener);
            Textbox textbox = textboxConstruction.constructed().get(0);
            ArgumentCaptor<EventListener<Event>> listenerCaptor = ArgumentCaptor.forClass(EventListener.class);
            verify(textbox).addEventListener(eq(Events.ON_CHANGE), listenerCaptor.capture());
            listenerCaptor.getValue().onEvent(mock(Event.class));
            verify(editorListener).onValueChanged(NEW_PAYMENT_KEY);
            verify(zkSession).setAttribute(PAYMENT_KEY_SESSION_ATTR, NEW_PAYMENT_KEY);
            verify(novalnetMerchantDetailsFacade).callNovalnetMerchantDetails(PRODUCT_KEY, baseStoreModel);
            verify(eventQueue).publish(any(Event.class));
        }
    }

    @Test
    void shouldSkipMerchantDetailProcessingWhenProductKeyIsMissing() {
        when(baseStoreModel.getUid()).thenReturn(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStoreModel);
        when(editorContext.getInitialValue()).thenReturn(EXISTING_PAYMENT_KEY);
        when(zkSession.getAttribute(PRODUCT_KEY_SESSION_ATTR)).thenReturn(null);
        when(zkSession.getAttribute(PAYMENT_KEY_SESSION_ATTR)).thenReturn(EXISTING_PAYMENT_KEY);
        try (MockedStatic<NovalnetUtils> utilsMock = mockStatic(NovalnetUtils.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class)) {
            utilsMock.when(() -> NovalnetUtils.isPopulated(EXISTING_PAYMENT_KEY)).thenReturn(true);
            sessionsMock.when(Sessions::getCurrent).thenReturn(zkSession);
            assertDoesNotThrow(() -> editor.render(parentComponent, editorContext, editorListener),
                    "A missing product key should be handled gracefully, not thrown");
        }
        verifyNoInteractions(novalnetMerchantDetailsFacade, modelService);
    }

    @Test
    void shouldSkipMerchantDetailProcessingWhenParentIsNotBaseStore() {
        when(baseStoreModel.getUid()).thenReturn(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStoreModel, "teststore");
        when(editorContext.getInitialValue()).thenReturn(EXISTING_PAYMENT_KEY);
        when(zkSession.getAttribute(PRODUCT_KEY_SESSION_ATTR)).thenReturn(PRODUCT_KEY);
        when(zkSession.getAttribute(PAYMENT_KEY_SESSION_ATTR)).thenReturn(EXISTING_PAYMENT_KEY);
        try (MockedStatic<NovalnetUtils> utilsMock = mockStatic(NovalnetUtils.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class)) {
            utilsMock.when(() -> NovalnetUtils.isPopulated(EXISTING_PAYMENT_KEY)).thenReturn(true);
            sessionsMock.when(Sessions::getCurrent).thenReturn(zkSession);
            editor.render(parentComponent, editorContext, editorListener);
        }
        verifyNoInteractions(novalnetMerchantDetailsFacade, modelService);
    }

    @Test
    void shouldClearTariffsAndShowErrorWhenMerchantDetailsReturnFailure() throws Exception {
        when(baseStoreModel.getUid()).thenReturn(STORE_ID);
        when(editorContext.getParameter(CONTEXT_PARENT_OBJECT)).thenReturn(baseStoreModel);
        when(editorContext.getInitialValue()).thenReturn(EXISTING_PAYMENT_KEY);
        when(zkSession.getAttribute(PRODUCT_KEY_SESSION_ATTR)).thenReturn(PRODUCT_KEY);
        when(zkSession.getAttribute(PAYMENT_KEY_SESSION_ATTR)).thenReturn(EXISTING_PAYMENT_KEY);
        when(novalnetMerchantDetailsFacade.callNovalnetMerchantDetails(PRODUCT_KEY, baseStoreModel)).thenReturn(FAILURE_RESPONSE);
        try (MockedStatic<NovalnetUtils> utilsMock = mockStatic(NovalnetUtils.class);
             MockedStatic<Sessions> sessionsMock = mockStatic(Sessions.class);
             MockedStatic<EventQueues> eventQueuesMock = mockStatic(EventQueues.class);
             MockedStatic<Messagebox> messageboxMock = mockStatic(Messagebox.class);
             MockedConstruction<Textbox> textboxConstruction = mockConstruction(Textbox.class)) {
            utilsMock.when(() -> NovalnetUtils.isPopulated(EXISTING_PAYMENT_KEY)).thenReturn(true);
            sessionsMock.when(Sessions::getCurrent).thenReturn(zkSession);
            eventQueuesMock.when(
                            () -> EventQueues.lookup(eq(PaymentAccessKeyEditor.NOVALNET_TARIFF_EVENT), eq(EventQueues.DESKTOP), anyBoolean()))
                    .thenReturn(eventQueue);
            editor.render(parentComponent, editorContext, editorListener);
            verify(zkSession).setAttribute(eq(TARIFF_SESSION_ATTR), eq(new HashMap<>()));
            messageboxMock.verify(() -> Messagebox.show("Invalid product key", "Error", Messagebox.OK, Messagebox.ERROR));
            verify(eventQueue).publish(any(Event.class));
        }
        verify(baseStoreModel, never()).setNovalnetClientKey(anyString());
        verify(modelService, never()).save(any());
    }
}
