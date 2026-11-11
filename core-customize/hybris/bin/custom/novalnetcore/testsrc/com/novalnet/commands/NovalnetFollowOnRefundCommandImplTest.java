/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.commands.result.RefundResult;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetFollowOnRefundCommandImplTest {
    private static final BigDecimal REFUND_AMOUNT = BigDecimal.valueOf(100.00);

    @Mock
    private NovalnetTransactionFacade novalnetTransactionFacade;

    @Mock
    private FollowOnRefundRequest followOnRefundRequestMock;

    @Mock
    private NovalnetTransactionResult novalnetTransactionResultMock;

    @InjectMocks
    private NovalnetFollowOnRefundCommand testObj;

    @Test
    public void shouldReturnSuccessfulRefundResult() throws JsonProcessingException {
        when(followOnRefundRequestMock.getTotalAmount()).thenReturn(REFUND_AMOUNT);
        when(novalnetTransactionFacade.processRefund(followOnRefundRequestMock)).thenReturn(novalnetTransactionResultMock);
        when(novalnetTransactionResultMock.isSuccess()).thenReturn(true);

        RefundResult result = testObj.perform(followOnRefundRequestMock);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionStatus()).isEqualTo(TransactionStatus.ACCEPTED);
        assertThat(result.getTransactionStatusDetails()).isEqualTo(TransactionStatusDetails.SUCCESFULL);
        assertThat(result.getTotalAmount()).isEqualTo(REFUND_AMOUNT);

        verify(novalnetTransactionFacade).processRefund(followOnRefundRequestMock);
    }

    @Test
    public void shouldReturnRejectedRefundResultWhenRefundFails() throws JsonProcessingException {
        when(novalnetTransactionFacade.processRefund(followOnRefundRequestMock)).thenReturn(novalnetTransactionResultMock);
        when(novalnetTransactionResultMock.isSuccess()).thenReturn(false);

        RefundResult result = testObj.perform(followOnRefundRequestMock);

        assertThat(result).isNotNull();
        assertThat(result.getTransactionStatus()).isEqualTo(TransactionStatus.REJECTED);
        assertThat(result.getTransactionStatusDetails()).isEqualTo(TransactionStatusDetails.UNKNOWN_CODE);

        verify(novalnetTransactionFacade).processRefund(followOnRefundRequestMock);
    }
}

