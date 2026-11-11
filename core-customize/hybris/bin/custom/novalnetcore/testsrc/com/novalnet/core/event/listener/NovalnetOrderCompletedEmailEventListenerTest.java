/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package com.novalnet.core.event.listener;

import com.novalnet.core.event.NovalnetOrderCompletedEmailEvent;
import com.novalnet.model.NovalnetOrderCompletedEmailProcessModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetOrderCompletedEmailEventListenerTest {
    private static final String ORDER_CODE = "100012345";
    private static final String PROCESS_DEFINITION = "novalnetOrderCompletedEmailProcess";
    private static final String PROCESS_CODE_PREFIX = "novalnetOrderCompletedEmail-" + ORDER_CODE + "-";

    @Mock
    private ModelService modelService;

    @Mock
    private BusinessProcessService businessProcessService;

    @Mock
    private NovalnetOrderCompletedEmailEvent event;

    @Mock
    private OrderModel order;

    @Mock
    private LanguageModel language;

    @Mock
    private CurrencyModel currency;

    @Mock
    private BaseSiteModel site;

    @Mock
    private BaseStoreModel baseStore;

    @Mock
    private NovalnetOrderCompletedEmailProcessModel process;

    @InjectMocks
    private NovalnetOrderCompletedEmailEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new NovalnetOrderCompletedEmailEventListener();
        listener.setModelService(modelService);
        listener.setBusinessProcessService(businessProcessService);
    }

    @Test
    void shouldCreateAndStartEmailProcessSuccessfully() {
        mockValidEvent();

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        final ArgumentCaptor<String> processCodeCaptor = ArgumentCaptor.forClass(String.class);

        verify(businessProcessService).createProcess(processCodeCaptor.capture(), eq(PROCESS_DEFINITION));

        final String processCode = processCodeCaptor.getValue();

        assertTrue(processCode.startsWith(PROCESS_CODE_PREFIX));

        verify(process).setOrder(order);
        verify(process).setLanguage(language);
        verify(process).setCurrency(currency);
        verify(process).setSite(site);
        verify(process).setStore(baseStore);

        verify(modelService).save(process);
        verify(businessProcessService).startProcess(process);
    }

    @Test
    void shouldSetOrderLanguageOnProcess() {
        mockValidEvent();

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        verify(process).setLanguage(language);
    }

    @Test
    void shouldSetEventDataOnProcess() {
        mockValidEvent();

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        verify(process).setCurrency(currency);
        verify(process).setSite(site);
        verify(process).setStore(baseStore);
    }

    @Test
    void shouldSaveProcessBeforeStartingIt() {
        mockValidEvent();

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        final InOrder inOrder = inOrder(modelService, businessProcessService);

        inOrder.verify(modelService).save(process);
        inOrder.verify(businessProcessService).startProcess(process);
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        when(event.getOrder()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> listener.onEvent(event));

        verify(event).getOrder();

        verify(businessProcessService, never()).createProcess(anyString(), eq(PROCESS_DEFINITION));

        verify(modelService, never()).save(process);

        verify(businessProcessService, never()).startProcess(process);
    }

    @Test
    void shouldThrowExceptionWhenProcessCreationReturnsNull() {
        when(event.getOrder()).thenReturn(order);

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(null);

        assertThrows(NullPointerException.class, () -> listener.onEvent(event));

        verify(event).getOrder();

        verify(businessProcessService).createProcess(anyString(), eq(PROCESS_DEFINITION));

        verify(modelService, never()).save(process);

        verify(businessProcessService, never()).startProcess(process);
    }

    @Test
    void shouldNotStartProcessWhenSaveFails() {
        mockValidEvent();

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        final RuntimeException exception = new RuntimeException("Unable to save process");

        doThrow(exception).when(modelService).save(process);

        final RuntimeException thrown = assertThrows(RuntimeException.class, () -> listener.onEvent(event));

        assertTrue(thrown.getMessage().contains("Unable to save process"));

        verify(modelService).save(process);

        verify(businessProcessService, never()).startProcess(process);
    }

    @Test
    void shouldPropagateExceptionWhenProcessStartFails() {
        mockValidEvent();

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        final RuntimeException exception = new RuntimeException("Unable to start process");

        doThrow(exception).when(businessProcessService).startProcess(process);

        final RuntimeException thrown = assertThrows(RuntimeException.class, () -> listener.onEvent(event));

        assertTrue(thrown.getMessage().contains("Unable to start process"));

        verify(modelService).save(process);
        verify(businessProcessService).startProcess(process);
    }

    @Test
    void shouldSetNullCurrencyWhenEventCurrencyIsNull() {
        mockValidEvent();

        when(event.getCurrency()).thenReturn(null);

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        verify(process).setCurrency(null);
    }

    @Test
    void shouldSetNullSiteWhenEventSiteIsNull() {
        mockValidEvent();

        when(event.getSite()).thenReturn(null);

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        verify(process).setSite(null);
    }

    @Test
    void shouldSetNullStoreWhenEventBaseStoreIsNull() {
        mockValidEvent();

        when(event.getBaseStore()).thenReturn(null);

        when(businessProcessService.createProcess(anyString(), eq(PROCESS_DEFINITION))).thenReturn(process);

        listener.onEvent(event);

        verify(process).setStore(null);
    }

    private void mockValidEvent() {
        when(event.getOrder()).thenReturn(order);
        when(event.getCurrency()).thenReturn(currency);
        when(event.getSite()).thenReturn(site);
        when(event.getBaseStore()).thenReturn(baseStore);

        when(order.getCode()).thenReturn(ORDER_CODE);
        when(order.getLanguage()).thenReturn(language);
    }
}

