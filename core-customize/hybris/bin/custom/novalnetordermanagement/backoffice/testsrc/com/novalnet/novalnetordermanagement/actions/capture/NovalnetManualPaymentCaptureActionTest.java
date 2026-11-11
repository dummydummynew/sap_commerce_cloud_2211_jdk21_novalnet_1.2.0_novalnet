/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package com.novalnet.novalnetordermanagement.actions.capture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.ActionResult.StatusFlag;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.util.localization.Localization;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetManualPaymentCaptureActionTest {
    private static final String NOTIFICATION_SOURCE = "General";
    private static final String CONFIRMATION_MESSAGE = "Are you sure you want to capture this payment?";

    @Mock
    private NotificationService notificationService;

    @Mock
    private NovalnetTransactionFacade novalnetTransactionFacade;

    @Mock
    private ActionContext<OrderModel> actionContext;

    @Mock
    private OrderModel orderModel;

    @Mock
    private NovalnetTransactionResult transactionResult;

    @InjectMocks
    private NovalnetManualPaymentCaptureAction novalnetManualPaymentCaptureAction;

    private MockedStatic<Localization> localizationMockedStatic;

    @BeforeEach
    void setUp() {
        localizationMockedStatic = Mockito.mockStatic(Localization.class);
    }

    @AfterEach
    void tearDown() {
        localizationMockedStatic.close();
    }

    @Test
    void shouldReturnTrueWhenFacadeAllowsCapture() {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.canCapture(orderModel)).thenReturn(true);

        boolean result = novalnetManualPaymentCaptureAction.canPerform(actionContext);

        assertTrue(result);
        verify(novalnetTransactionFacade).canCapture(orderModel);
    }

    @Test
    void shouldReturnFalseWhenFacadeDisallowsCapture() {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.canCapture(orderModel)).thenReturn(false);

        boolean result = novalnetManualPaymentCaptureAction.canPerform(actionContext);

        assertFalse(result);
    }

    @Test
    void shouldAlwaysRequireConfirmation() {
        assertTrue(novalnetManualPaymentCaptureAction.needsConfirmation(actionContext));
    }

    @Test
    void shouldReturnLocalizedConfirmationMessage() {
        localizationMockedStatic.when(() -> Localization.getLocalizedString("novalnet.capture")).thenReturn(CONFIRMATION_MESSAGE);

        String message = novalnetManualPaymentCaptureAction.getConfirmationMessage(actionContext);

        assertEquals(CONFIRMATION_MESSAGE, message);
    }

    @Test
    void shouldReturnErrorAndNotifyFailureWhenOrderIsNull() throws JsonProcessingException {
        when(actionContext.getData()).thenReturn(null);
        localizationMockedStatic.when(() -> Localization.getLocalizedString(anyString())).thenReturn("Order is null");

        ActionResult<OrderModel> result = novalnetManualPaymentCaptureAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE), anyString());
        verify(novalnetTransactionFacade, never()).captureOrder(any());
    }

    @Test
    void shouldReturnErrorAndNotifyFailureWhenCaptureResultIsNull() throws JsonProcessingException {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.captureOrder(orderModel)).thenReturn(null);
        localizationMockedStatic.when(() -> Localization.getLocalizedString(anyString())).thenReturn("Capture failed");

        ActionResult<OrderModel> result = novalnetManualPaymentCaptureAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE), anyString());
    }

    @Test
    void shouldReturnSuccessAndMarkObjectPersistedWhenCaptureSucceeds() throws JsonProcessingException {
        when(actionContext.getData()).thenReturn(orderModel);
        when(transactionResult.isSuccess()).thenReturn(true);
        when(transactionResult.getMessage()).thenReturn("Payment captured successfully");
        when(novalnetTransactionFacade.captureOrder(orderModel)).thenReturn(transactionResult);

        final ActionResult<OrderModel> result = novalnetManualPaymentCaptureAction.perform(actionContext);

        assertEquals(ActionResult.SUCCESS, result.getResultCode());
        assertEquals(orderModel, result.getData());
        assertTrue(result.getStatusFlags().contains(StatusFlag.OBJECT_PERSISTED));
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.SUCCESS),
                eq("Payment captured successfully"));
    }

    @Test
    void shouldReturnErrorWhenCaptureIsNotSuccessful() throws JsonProcessingException {
        when(actionContext.getData()).thenReturn(orderModel);
        when(transactionResult.isSuccess()).thenReturn(false);
        when(transactionResult.getMessage()).thenReturn("Capture not allowed for this order");
        when(novalnetTransactionFacade.captureOrder(orderModel)).thenReturn(transactionResult);

        ActionResult<OrderModel> result = novalnetManualPaymentCaptureAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE),
                eq("Capture not allowed for this order"));
    }

    @Test
    void shouldReturnErrorAndNotifyFailureWhenFacadeThrowsJsonProcessingException() throws JsonProcessingException {
        when(actionContext.getData()).thenReturn(orderModel);
        doThrow(Mockito.mock(JsonProcessingException.class)).when(novalnetTransactionFacade).captureOrder(orderModel);
        localizationMockedStatic.when(() -> Localization.getLocalizedString(anyString())).thenReturn("Capture failed");

        ActionResult<OrderModel> result = novalnetManualPaymentCaptureAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE), anyString());
    }
}
