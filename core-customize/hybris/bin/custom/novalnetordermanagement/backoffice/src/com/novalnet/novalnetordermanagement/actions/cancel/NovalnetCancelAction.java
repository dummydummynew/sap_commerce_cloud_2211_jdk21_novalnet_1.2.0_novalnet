package com.novalnet.novalnetordermanagement.actions.cancel;

import com.hybris.backoffice.widgets.notificationarea.NotificationService;
import com.hybris.backoffice.widgets.notificationarea.event.NotificationEvent;
import com.hybris.cockpitng.actions.ActionContext;
import com.hybris.cockpitng.actions.ActionResult;
import com.hybris.cockpitng.actions.ActionResult.StatusFlag;
import com.hybris.cockpitng.actions.CockpitAction;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.apache.log4j.Logger;


/**
 * Backoffice action responsible for cancelling Novalnet transactions for orders.
 * <p>
 * Validates whether an order can be cancelled, performs the cancellation, and notifies the user about the result.
 */
public class NovalnetCancelAction implements CockpitAction<OrderModel, OrderModel> {
    private static final Logger LOG = Logger.getLogger(NovalnetCancelAction.class);

    private static final String NOTIFICATION_SOURCE = "General";

    @Resource(name = "notificationService")
    private NotificationService notificationService;

    @Resource(name = "novalnetTransactionFacade")
    private NovalnetTransactionFacade novalnetTransactionFacade;

    /**
     * Determines whether the cancellation action can be performed for the selected order.
     *
     * @param ctx the action context containing the selected order
     * @return {@code true} if the order can be cancelled; otherwise {@code false}
     */
    @Override
    public boolean canPerform(final ActionContext<OrderModel> ctx) {
        final OrderModel order = ctx.getData();

        return novalnetTransactionFacade.canCancel(order);
    }

    /**
     * Indicates whether user confirmation is required before cancelling the order.
     *
     * @param ctx the action context containing the selected order
     * @return {@code true} because cancellation requires confirmation
     */
    @Override
    public boolean needsConfirmation(final ActionContext<OrderModel> ctx) {
        return true;
    }

    /**
     * Returns the localized confirmation message displayed before cancellation.
     *
     * @param ctx the action context containing the selected order
     * @return the localized cancellation confirmation message
     */
    @Override
    public String getConfirmationMessage(final ActionContext<OrderModel> ctx) {
        return Localization.getLocalizedString("novalnet.cancel.confirmation");
    }


    /**
     * Performs the Novalnet transaction cancellation and notifies the user of the result.
     *
     * @param ctx the action context containing the selected order
     * @return the action result containing the cancellation status and order
     */
    @Override
    public ActionResult<OrderModel> perform(final ActionContext<OrderModel> ctx) {
        final OrderModel order = ctx.getData();

        if (order == null) {
            LOG.warn("Order is null in action context");

            notifyUserFailure("novalnet.order.null");

            return new ActionResult<>(ActionResult.ERROR, null);
        }
        try {
            final NovalnetTransactionResult result = novalnetTransactionFacade.cancelOrder(order);

            if (result.isSuccess()) {
                notificationService.notifyUser("", NOTIFICATION_SOURCE, NotificationEvent.Level.SUCCESS, result.getMessage());

                final ActionResult<OrderModel> actionResult = new ActionResult<>(ActionResult.SUCCESS, order);

                actionResult.getStatusFlags().add(StatusFlag.OBJECT_PERSISTED);

                return actionResult;
            }

            notificationService.notifyUser("", NOTIFICATION_SOURCE, NotificationEvent.Level.FAILURE, result.getMessage());

            return new ActionResult<>(ActionResult.ERROR, null);
        } catch (final Exception e) {
            LOG.error("Failed to perform order cancel", e);

            notifyUserFailure("novalnet.order.cancel.failed");

            return new ActionResult<>(ActionResult.ERROR, null);
        }
    }

    /**
     * Sends a failure notification to the backoffice user.
     *
     * @param message the message to display in the notification
     *
     */
    private void notifyUserFailure(final String message) {
        notificationService.notifyUser("", NOTIFICATION_SOURCE, NotificationEvent.Level.FAILURE, message);
    }
}
