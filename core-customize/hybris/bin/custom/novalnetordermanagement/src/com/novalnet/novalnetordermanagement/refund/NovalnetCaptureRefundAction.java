package com.novalnet.novalnetordermanagement.refund;

import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.platform.basecommerce.enums.ReturnStatus;
import de.hybris.platform.processengine.action.AbstractSimpleDecisionAction;
import de.hybris.platform.returns.model.ReturnProcessModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.task.RetryLaterException;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSAL_FAILED;
import static de.hybris.platform.basecommerce.enums.ReturnStatus.PAYMENT_REVERSED;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.NOK;
import static de.hybris.platform.processengine.action.AbstractSimpleDecisionAction.Transition.OK;


/**
 * Process action responsible for executing Novalnet refunds for return requests.
 * <p>
 * Updates the return request and return entry statuses based on the refund result.
 */
public class NovalnetCaptureRefundAction extends AbstractSimpleDecisionAction<ReturnProcessModel> {
    private static final Logger LOG = LoggerFactory.getLogger(NovalnetCaptureRefundAction.class);

    @Resource(name = "novalnetTransactionFacade")
    private NovalnetTransactionFacade novalnetTransactionFacade;

    /**
     * Executes the refund action for the return request associated with the process.
     *
     * @param process the return process containing the return request
     * @return {@link Transition#OK} when the refund is successful; otherwise {@link Transition#NOK}
     * @throws RetryLaterException if the action should be retried later
     */
    @Override
    public Transition executeAction(final ReturnProcessModel process) throws RetryLaterException {
        if (process == null || process.getReturnRequest() == null) {
            LOG.error("Process or ReturnRequest is null");
            return NOK;
        }

        final ReturnRequestModel returnRequest = process.getReturnRequest();

        try {
            final NovalnetTransactionResult result = novalnetTransactionFacade.refund(returnRequest);

            if (result.isSuccess()) {
                setReturnRequestStatus(returnRequest, PAYMENT_REVERSED);
                return OK;
            }

            LOG.error("Refund failed for return request {}: {}", returnRequest.getCode(), result.getMessage());
            setReturnRequestStatus(returnRequest, PAYMENT_REVERSAL_FAILED);
            return NOK;
        } catch (final Exception e) {
            LOG.error("Exception during refund for return request {}", returnRequest.getCode(), e);
            setReturnRequestStatus(returnRequest, PAYMENT_REVERSAL_FAILED);
            return NOK;
        }
    }

    /**
     * Updates the status of the return request and all associated return entries.
     *
     * @param returnRequest the return request to update
     * @param status        the new return status
     */
    protected void setReturnRequestStatus(final ReturnRequestModel returnRequest, final ReturnStatus status) {
        returnRequest.setStatus(status);
        returnRequest.getReturnEntries().forEach(entry -> entry.setStatus(status));
        getModelService().saveAll(returnRequest.getReturnEntries());
        getModelService().save(returnRequest);
    }
}
