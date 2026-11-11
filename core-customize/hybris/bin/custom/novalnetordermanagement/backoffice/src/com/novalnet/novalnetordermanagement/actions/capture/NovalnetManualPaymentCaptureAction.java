package com.novalnet.novalnetordermanagement.actions.capture;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.CockpitAction;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.apache.log4j.Logger;


/**
 * Backoffice action responsible for manually capturing Novalnet payments for orders.
 * <p>
 * Validates whether the payment can be captured, performs the capture operation, and notifies the backoffice user about
 * the result.
 */
public class NovalnetManualPaymentCaptureAction implements CockpitAction<OrderModel, OrderModel> {
    private static final Logger LOG = Logger.getLogger(NovalnetManualPaymentCaptureAction.class);

    private static final String NOTIFICATION_SOURCE = "General";

    @Resource(name = "novalnetTransactionFacade")
    private NovalnetTransactionFacade novalnetTransactionFacade;

    @Resource(name = "notificationService")
    private NotificationService notificationService;

    /**
     * Determines whether the payment capture action can be performed for the selected order.
     *
     * @param ctx the action context containing the selected order
     * @return {@code true} if the payment can be captured; otherwise {@code false}
     */
    @Override
    public boolean canPerform(final ActionContext<OrderModel> ctx) {
        return novalnetTransactionFacade.canCapture(ctx.getData());
    }

    /**
     * Indicates whether user confirmation is required before capturing the payment.
     *
     * @param ctx the action context containing the selected order
     * @return {@code true} because payment capture requires confirmation
     */
    @Override
    public boolean needsConfirmation(final ActionContext<OrderModel> ctx) {
        return true;
    }

    /**
     * Returns the localized confirmation message displayed before payment capture.
     *
     * @param ctx the action context containing the selected order
     * @return the localized payment capture confirmation message
     */
    @Override
    public String getConfirmationMessage(final ActionContext<OrderModel> ctx) {
        return Localization.getLocalizedString("novalnet.capture");
    }

    /**
     * Performs the Novalnet payment capture and notifies the user of the result.
     *
     * @param ctx the action context containing the selected order
     * @return the action result containing the capture status and order
     */
    @Override
    public ActionResult<OrderModel> perform(final ActionContext<OrderModel> ctx) {
        final OrderModel order = ctx.getData();

        if (order == null) {
            LOG.warn("Order is null in action context");
            notifyFailure(Localization.getLocalizedString("novalnet.order.null"));
            return new ActionResult<>(ActionResult.ERROR, null);
        }

        try {
            final NovalnetTransactionResult result = novalnetTransactionFacade.captureOrder(order);

            if (result == null) {
                LOG.warn("Capture result is null for order");
                notifyFailure(Localization.getLocalizedString("novalnet.capture.failed"));
                return new ActionResult<>(ActionResult.ERROR, null);
            }

            if (result.isSuccess()) {
                notificationService.notifyUser("", NOTIFICATION_SOURCE, NotificationEvent.Level.SUCCESS, result.getMessage());

                final ActionResult<OrderModel> actionResult = new ActionResult<>(ActionResult.SUCCESS, order);

                actionResult.getStatusFlags().add(ActionResult.StatusFlag.OBJECT_PERSISTED);
                return actionResult;
            }

            notifyFailure(result.getMessage());
            return new ActionResult<>(ActionResult.ERROR, null);
        } catch (final JsonProcessingException e) {
            LOG.error("Failed to capture Novalnet payment", e);
            notifyFailure(Localization.getLocalizedString("novalnet.capture.failed"));
            return new ActionResult<>(ActionResult.ERROR, null);
        }
    }

    /**
     * Sends a failure notification to the backoffice user.
     *
     * @param message the message to display in the notification
     *
     */
    private void notifyFailure(final String message) {
        notificationService.notifyUser("", NOTIFICATION_SOURCE, NotificationEvent.Level.FAILURE, message);
    }
}
