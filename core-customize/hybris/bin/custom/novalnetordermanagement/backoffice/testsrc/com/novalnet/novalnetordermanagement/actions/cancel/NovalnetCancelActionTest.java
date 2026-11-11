/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package com.novalnet.novalnetordermanagement.actions.cancel;

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
class NovalnetCancelActionTest {
    private static final String NOTIFICATION_SOURCE = "General";
    private static final String CONFIRMATION_MESSAGE = "Are you sure you want to cancel this order?";

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
    private NovalnetCancelAction novalnetCancelAction;

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
    void shouldReturnTrueWhenFacadeAllowsCancellation() {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.canCancel(orderModel)).thenReturn(true);

        final boolean result = novalnetCancelAction.canPerform(actionContext);

        assertTrue(result);
        verify(novalnetTransactionFacade).canCancel(orderModel);
    }

    @Test
    void shouldReturnFalseWhenFacadeDisallowsCancellation() {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.canCancel(orderModel)).thenReturn(false);

        final boolean result = novalnetCancelAction.canPerform(actionContext);

        assertFalse(result);
    }

    @Test
    void shouldAlwaysRequireConfirmation() {
        assertTrue(novalnetCancelAction.needsConfirmation(actionContext));
    }

    @Test
    void shouldReturnLocalizedConfirmationMessage() {
        localizationMockedStatic.when(() -> Localization.getLocalizedString("novalnet.cancel.confirmation"))
                .thenReturn(CONFIRMATION_MESSAGE);

        final String message = novalnetCancelAction.getConfirmationMessage(actionContext);

        assertEquals(CONFIRMATION_MESSAGE, message);
    }

    @Test
    void shouldReturnErrorAndNotifyFailureWhenOrderIsNull() {
        when(actionContext.getData()).thenReturn(null);
        localizationMockedStatic.when(() -> Localization.getLocalizedString(anyString())).thenReturn("Order is null");

        final ActionResult<OrderModel> result = novalnetCancelAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE), anyString());
        verify(novalnetTransactionFacade, never()).cancelOrder(any());
    }

    @Test
    void shouldReturnSuccessAndMarkObjectPersistedWhenCancellationSucceeds() throws Exception {
        when(actionContext.getData()).thenReturn(orderModel);
        when(transactionResult.isSuccess()).thenReturn(true);
        when(transactionResult.getMessage()).thenReturn("Order cancelled successfully");
        when(novalnetTransactionFacade.cancelOrder(orderModel)).thenReturn(transactionResult);

        final ActionResult<OrderModel> result = novalnetCancelAction.perform(actionContext);

        assertEquals(ActionResult.SUCCESS, result.getResultCode());
        assertEquals(orderModel, result.getData());
        assertTrue(result.getStatusFlags().contains(StatusFlag.OBJECT_PERSISTED));
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.SUCCESS),
                eq("Order cancelled successfully"));
    }

    @Test
    void shouldReturnErrorWhenCancellationIsNotSuccessful() throws Exception {
        when(actionContext.getData()).thenReturn(orderModel);
        when(transactionResult.isSuccess()).thenReturn(false);
        when(transactionResult.getMessage()).thenReturn("Cancellation not allowed for this order");
        when(novalnetTransactionFacade.cancelOrder(orderModel)).thenReturn(transactionResult);

        final ActionResult<OrderModel> result = novalnetCancelAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE),
                eq("Cancellation not allowed for this order"));
    }

    @Test
    void shouldReturnErrorAndNotifyFailureWhenFacadeThrowsException() throws Exception {
        when(actionContext.getData()).thenReturn(orderModel);
        doThrow(new RuntimeException("Unexpected facade failure")).when(novalnetTransactionFacade).cancelOrder(orderModel);
        localizationMockedStatic.when(() -> Localization.getLocalizedString(anyString())).thenReturn("Failed to cancel order");

        final ActionResult<OrderModel> result = novalnetCancelAction.perform(actionContext);

        assertEquals(ActionResult.ERROR, result.getResultCode());
        assertNull(result.getData());
        verify(notificationService).notifyUser(eq(""), eq(NOTIFICATION_SOURCE), eq(NotificationEvent.Level.FAILURE), anyString());
    }
}
