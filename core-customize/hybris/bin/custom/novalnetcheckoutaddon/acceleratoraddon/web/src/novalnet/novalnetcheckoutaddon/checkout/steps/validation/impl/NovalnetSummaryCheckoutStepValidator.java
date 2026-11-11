/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.checkout.steps.validation.impl;

import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.AbstractCheckoutStepValidator;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.validation.ValidationResults;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


public class NovalnetSummaryCheckoutStepValidator extends AbstractCheckoutStepValidator {

    @Override
    public ValidationResults validateOnEnter(final RedirectAttributes redirectAttributes) {
        ValidationResults cartResult = checkCartAndDelivery(redirectAttributes);
        if (cartResult != null) {
            return cartResult;
        }

        ValidationResults paymentResult = checkPaymentMethodAndPickup();
        if (paymentResult != null) {
            return paymentResult;
        }

        return ValidationResults.SUCCESS;
    }

    protected ValidationResults checkPaymentMethodAndPickup() {
        return null;
    }

    protected ValidationResults checkCartAndDelivery(final RedirectAttributes redirectAttributes) {
        if (!getCheckoutFlowFacade().hasValidCart()) {
            return ValidationResults.REDIRECT_TO_CART;
        }

        if (getCheckoutFlowFacade().hasNoDeliveryAddress()) {
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
                    "checkout.multi.deliveryAddress.notprovided");
            return ValidationResults.REDIRECT_TO_DELIVERY_ADDRESS;
        }

        if (getCheckoutFlowFacade().hasNoDeliveryMode()) {
            GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.INFO_MESSAGES_HOLDER,
                    "checkout.multi.deliveryMethod.notprovided");
            return ValidationResults.REDIRECT_TO_DELIVERY_METHOD;
        }
        return null;
    }
}
