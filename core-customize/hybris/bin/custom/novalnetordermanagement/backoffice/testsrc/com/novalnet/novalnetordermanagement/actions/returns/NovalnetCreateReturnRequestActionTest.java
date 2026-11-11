/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package com.novalnet.novalnetordermanagement.actions.returns;

import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.OrderModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetCreateReturnRequestActionTest {
    @Spy
    @InjectMocks
    private NovalnetCreateReturnRequestAction action;

    @Mock
    private NovalnetTransactionFacade novalnetTransactionFacade;

    @Mock
    private ActionContext<OrderModel> actionContext;

    @Mock
    private OrderModel orderModel;

    @Test
    void shouldReturnFalseWhenOrderIsNull() {
        when(actionContext.getData()).thenReturn(null);

        final boolean result = action.canPerform(actionContext);

        assertThat(result).isFalse();
        verify(novalnetTransactionFacade, never()).canCreateReturnRequest(any());
    }

    @Test
    void shouldReturnTrueWhenOrderIsNotNullAndFacadeAllowsReturn() {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.canCreateReturnRequest(orderModel)).thenReturn(true);

        final boolean result = action.canPerform(actionContext);

        assertThat(result).isTrue();
        verify(novalnetTransactionFacade).canCreateReturnRequest(orderModel);
    }

    @Test
    void shouldReturnFalseWhenOrderIsNotNullAndFacadeDisallowsReturn() {
        when(actionContext.getData()).thenReturn(orderModel);
        when(novalnetTransactionFacade.canCreateReturnRequest(orderModel)).thenReturn(false);

        final boolean result = action.canPerform(actionContext);

        assertThat(result).isFalse();
        verify(novalnetTransactionFacade).canCreateReturnRequest(orderModel);
    }

    @Test
    void shouldReturnErrorWhenOrderIsNull() {
        when(actionContext.getData()).thenReturn(null);

        final ActionResult<OrderModel> result = action.perform(actionContext);

        assertThat(result.getResultCode()).isEqualTo(ActionResult.ERROR);
        assertThat(result.getData()).isNull();
        verify(action, never()).sendOutput(any(String.class), any());
    }

    @Test
    void shouldReturnSuccessAndSendOutputWhenOrderIsValid() {
        when(actionContext.getData()).thenReturn(orderModel);
        doNothing().when(action).sendOutput(any(String.class), any());
        final ActionResult<OrderModel> result = action.perform(actionContext);
        assertThat(result.getResultCode()).isEqualTo(ActionResult.SUCCESS);
        assertThat(result.getData()).isEqualTo(orderModel);
        assertThat(result.getStatusFlags()).contains(ActionResult.StatusFlag.OBJECT_PERSISTED);
        verify(action).sendOutput(eq("createReturnRequestContext"), eq(orderModel));
    }

    @Test
    void shouldReturnNullWhenGettingConfirmationMessage() {
        final String result = action.getConfirmationMessage(actionContext);

        assertThat(result).isNull();
    }

    @Test
    void shouldReturnFalseWhenConfirmationIsNotRequired() {
        final boolean result = action.needsConfirmation(actionContext);

        assertThat(result).isFalse();
    }
}
