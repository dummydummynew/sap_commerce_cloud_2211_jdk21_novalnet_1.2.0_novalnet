/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package com.novalnet.novalnetordermanagement.refund;

import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition;
import de.hybris.platform.returns.model.ReturnEntryModel;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSAL_FAILED;
import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSED;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.NOK;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetCaptureRefundActionTest {
    @InjectMocks
    private NovalnetCaptureRefundAction testObj;

    @Mock
    private NovalnetTransactionFacade novalnetTransactionFacade;

    @Mock
    private ReturnProcessModel process;

    @Mock
    private ReturnRequestModel returnRequest;

    @Mock
    private ModelService modelService;

    @Mock
    private ReturnEntryModel returnEntry;

    @BeforeEach
    public void setUp() {
        testObj.setModelService(modelService);
    }

    @Test
    public void shouldReturnNokWhenProcessIsNull() {
        Transition result = testObj.executeAction(null);
        assertThat(result).isEqualTo(NOK);
    }

    @Test
    public void shouldReturnNokWhenReturnRequestIsNull() {
        when(process.getReturnRequest()).thenReturn(null);
        Transition result = testObj.executeAction(process);
        assertThat(result).isEqualTo(NOK);
    }

    @Test
    public void shouldReturnOkWhenRefundIsSuccessful() {
        NovalnetTransactionResult transactionResult = mock(NovalnetTransactionResult.class);

        when(process.getReturnRequest()).thenReturn(returnRequest);
        when(novalnetTransactionFacade.refund(returnRequest)).thenReturn(transactionResult);
        when(transactionResult.isSuccess()).thenReturn(true);
        when(returnRequest.getReturnEntries()).thenReturn(Collections.singletonList(returnEntry));

        Transition result = testObj.executeAction(process);

        assertThat(result).isEqualTo(OK);

        verify(novalnetTransactionFacade).refund(returnRequest);
        verify(returnRequest).setStatus(PAYMENT_REVERSED);
        verify(returnEntry).setStatus(PAYMENT_REVERSED);
        verify(modelService).saveAll(Collections.singletonList(returnEntry));
        verify(modelService).save(returnRequest);
    }

    @Test
    public void shouldReturnNokWhenRefundFails() {
        final NovalnetTransactionResult transactionResult = mock(NovalnetTransactionResult.class);

        when(process.getReturnRequest()).thenReturn(returnRequest);
        when(novalnetTransactionFacade.refund(returnRequest)).thenReturn(transactionResult);
        when(transactionResult.isSuccess()).thenReturn(false);
        when(transactionResult.getMessage()).thenReturn("Refund failed");
        when(returnRequest.getReturnEntries()).thenReturn(Collections.singletonList(returnEntry));

        Transition result = testObj.executeAction(process);

        assertThat(result).isEqualTo(NOK);

        verify(novalnetTransactionFacade).refund(returnRequest);
        verify(returnRequest).setStatus(PAYMENT_REVERSAL_FAILED);
        verify(returnEntry).setStatus(PAYMENT_REVERSAL_FAILED);
        verify(modelService).saveAll(Collections.singletonList(returnEntry));
        verify(modelService).save(returnRequest);
    }

    @Test
    public void shouldReturnNokWhenRefundThrowsException() {
        when(process.getReturnRequest()).thenReturn(returnRequest);
        when(novalnetTransactionFacade.refund(returnRequest)).thenThrow(new RuntimeException("Refund service error"));
        when(returnRequest.getReturnEntries()).thenReturn(Collections.singletonList(returnEntry));

        Transition result = testObj.executeAction(process);

        assertThat(result).isEqualTo(NOK);

        verify(novalnetTransactionFacade).refund(returnRequest);
        verify(returnRequest).setStatus(PAYMENT_REVERSAL_FAILED);
        verify(returnEntry).setStatus(PAYMENT_REVERSAL_FAILED);
        verify(modelService).saveAll(Collections.singletonList(returnEntry));
        verify(modelService).save(returnRequest);
    }

    @Test
    public void shouldUpdateReturnRequestAndEntriesStatus() {
        when(returnRequest.getReturnEntries()).thenReturn(Collections.singletonList(returnEntry));

        testObj.setReturnRequestStatus(returnRequest, PAYMENT_REVERSED);

        verify(returnRequest).setStatus(PAYMENT_REVERSED);
        verify(returnEntry).setStatus(PAYMENT_REVERSED);
        verify(modelService).saveAll(Collections.singletonList(returnEntry));
        verify(modelService).save(returnRequest);
    }
}
