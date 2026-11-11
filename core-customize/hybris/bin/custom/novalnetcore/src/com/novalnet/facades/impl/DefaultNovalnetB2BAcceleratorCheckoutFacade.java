/*
 * Copyright (c) 2026 Novalnet AG. All rights reserved.
 */
package com.novalnet.facades.impl;

import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.checkout.data.PlaceOrderData;
import de.hybris.platform.b2bacceleratorfacades.exception.EntityValidationException;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BCommentData;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.b2bacceleratorfacades.order.data.TriggerData;

import de.hybris.platform.b2bacceleratorfacades.order.impl.DefaultB2BAcceleratorCheckoutFacade;
import de.hybris.platform.commercefacades.order.data.AbstractOrderData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.SAPGenericPaymentInfoModel;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static de.hybris.platform.util.localization.Localization.getLocalizedString;

/**
 * Keeps Novalnet payment information on the checkout DTO.
 *
 * <p>The standard checkout facade overwrites the value populated by the cart
 * converter because its payment-details lookup only supports
 * {@code CreditCardPaymentInfoModel}. Novalnet uses its own
 * {@code PaymentInfoModel} subtype, so its populator must run after the
 * standard checkout facade has finished assembling the DTO.</p>
 */
public class DefaultNovalnetB2BAcceleratorCheckoutFacade extends DefaultB2BAcceleratorCheckoutFacade {


    private static final String CART_CHECKOUT_NOT_CALCULATED = "cart.not.calculated";
    private static final String CART_CHECKOUT_DELIVERYADDRESS_INVALID = "cart.deliveryAddress.invalid";
    private static final String CART_CHECKOUT_DELIVERYMODE_INVALID = "cart.deliveryMode.invalid";
    private static final String CART_CHECKOUT_QUOTE_REQUIREMENTS_NOT_SATISFIED = "cart.quote.requirements.not.satisfied";
    private static final String CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED = "cart.transation.notAuthorized";
    private static final String CART_CHECKOUT_NO_QUOTE_DESCRIPTION = "cart.no.quote.description";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE = "cart.replenishment.no.startdate";
    private static final String CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY = "cart.replenishment.no.frequency";
    private Populator<CartModel, CartData> novalnetCartPaymentInfoPopulator;

    @Override
    public CartData getCheckoutCart() {
        final CartData cartData = super.getCheckoutCart();
        final CartModel cartModel = getCart();

        if (cartData != null && cartModel != null) {
            novalnetCartPaymentInfoPopulator.populate(cartModel, cartData);
        }

        return cartData;
    }

    public void setNovalnetCartPaymentInfoPopulator(
            final Populator<CartModel, CartData> novalnetCartPaymentInfoPopulator) {
        this.novalnetCartPaymentInfoPopulator = novalnetCartPaymentInfoPopulator;
    }

    @Override
    public <T extends AbstractOrderData> T placeOrder(final PlaceOrderData placeOrderData) throws InvalidCartException {

        final boolean isCardtPaymentType = CheckoutPaymentType.CARD.equals(getCart().getPaymentType());
        if (isCardtPaymentType && Boolean.FALSE.equals(placeOrderData.getReplenishmentOrder()) && !(getCart().getPaymentInfo() instanceof SAPGenericPaymentInfoModel)) {
            validateTransactionIsAuthorized();
        }

        if (isValidCheckoutCart(placeOrderData)) {
            validateQuoteNegotiationAndSetB2BComment(placeOrderData);

            if (placeOrderData.getReplenishmentOrder() != null && placeOrderData.getReplenishmentOrder().equals(Boolean.TRUE)) {
                final TriggerData triggerData = validateReplenishmentAndGetTriggerData(placeOrderData);
                return (T) scheduleOrder(triggerData);
            }

            return (T) super.placeOrder();
        }

        return null;
    }

    private void validateTransactionIsAuthorized() {
        final List<PaymentTransactionModel> transactions = getCart().getPaymentTransactions();
        boolean authorized = false;
        for (final PaymentTransactionModel transaction : transactions) {

            for (final PaymentTransactionEntryModel entry : transaction.getEntries()) {

                if (entry.getType().equals(PaymentTransactionType.AUTHORIZATION)
                        && TransactionStatus.ACCEPTED.name().equals(entry.getTransactionStatus())) {
                    authorized = true;
                    break;
                }
            }
        }
        if (!authorized) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_TRANSACTION_NOT_AUTHORIZED));
        }
    }

    private void validateQuoteNegotiationAndSetB2BComment(final PlaceOrderData placeOrderData) {
        if (placeOrderData.getNegotiateQuote() != null && placeOrderData.getNegotiateQuote().equals(Boolean.TRUE)) {
            if (StringUtils.isBlank(placeOrderData.getQuoteRequestDescription())) {
                throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_NO_QUOTE_DESCRIPTION));
            } else {
                final B2BCommentData b2BComment = new B2BCommentData();
                b2BComment.setComment(placeOrderData.getQuoteRequestDescription());
                final CartData cartData = new CartData();
                cartData.setB2BComment(b2BComment);
                updateCheckoutCart(cartData);
            }
        }
    }

    private TriggerData validateReplenishmentAndGetTriggerData(final PlaceOrderData placeOrderData) {
        if (placeOrderData.getReplenishmentStartDate() == null) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_REPLENISHMENT_NO_STARTDATE));
        }

        if (placeOrderData.getReplenishmentRecurrence().equals(B2BReplenishmentRecurrenceEnum.WEEKLY)
                && CollectionUtils.isEmpty(placeOrderData.getNDaysOfWeek())) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_REPLENISHMENT_NO_FREQUENCY));
        }

        final TriggerData triggerData = new TriggerData();
        populateTriggerDataFromPlaceOrderData(placeOrderData, triggerData);
        return triggerData;
    }

    protected boolean isValidCheckoutCart(final PlaceOrderData placeOrderData) {
        final CartData cartData = getCheckoutCart();
        final boolean valid = true;

        if (!cartData.isCalculated()) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_NOT_CALCULATED));
        }

        if (cartData.getDeliveryAddress() == null) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_DELIVERYADDRESS_INVALID));
        }

        if (cartData.getDeliveryMode() == null) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_DELIVERYMODE_INVALID));
        }


        if (Boolean.TRUE.equals(placeOrderData.getNegotiateQuote()) && !cartData.getQuoteAllowed()) {
            throw new EntityValidationException(getLocalizedString(CART_CHECKOUT_QUOTE_REQUIREMENTS_NOT_SATISFIED));
        }

        return valid;
    }
}
