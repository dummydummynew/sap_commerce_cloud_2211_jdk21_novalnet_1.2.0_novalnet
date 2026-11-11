/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.commands;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.platform.payment.commands.FollowOnRefundCommand;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.commands.result.RefundResult;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.dto.TransactionStatusDetails;
import jakarta.annotation.Resource;


/**
 * Default implementation of {@link NovalnetFollowOnRefundCommand} for processing follow-on refunds.
 */
public class NovalnetFollowOnRefundCommand implements FollowOnRefundCommand<FollowOnRefundRequest> {
    @Resource(name = "novalnetTransactionFacade")
    private NovalnetTransactionFacade novalnetTransactionFacade;


    /**
     * Processes a follow-on refund request and creates the corresponding refund result.
     *
     * @param request the follow-on refund request containing the transaction details
     * @return the refund result indicating whether the refund was accepted or rejected
     */
    @Override
    public RefundResult perform(FollowOnRefundRequest request) {
        NovalnetTransactionResult result = null;
        try {
            result = novalnetTransactionFacade.processRefund(request);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        RefundResult refundResult = new RefundResult();

        if (result.isSuccess()) {
            refundResult.setTransactionStatus(TransactionStatus.ACCEPTED);
            refundResult.setTransactionStatusDetails(TransactionStatusDetails.SUCCESFULL);
            refundResult.setTotalAmount(request.getTotalAmount());
        } else {
            refundResult.setTransactionStatus(TransactionStatus.REJECTED);
            refundResult.setTransactionStatusDetails(TransactionStatusDetails.UNKNOWN_CODE);
        }

        return refundResult;
    }
}

