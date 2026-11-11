/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.service.order.impl;

import com.novalnet.dao.NovalnetCallbackDao;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.enums.NovalnetPaymentGatewayStatus;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Locale;


/**
 * Default implementation of {@link NovalnetCallbackOrderService} for managing callback order information and updating order, payment, and
 * callback statuses.
 */
public class DefaultNovalnetCallbackOrderService implements NovalnetCallbackOrderService {
    private static final String BR = "<br/>";
    private static final String STATUS_ON_HOLD = "ON_HOLD";
    private static final String QR_IMAGE = "qr_image";
    private static final String ORDER_CODE = "orderCode";
    private static final int ORDER_HISTORY_NOTES_INITIAL_CAPACITY = 256;

    @Resource
    private ModelService modelService;

    @Resource(name = "novalnetCallbackDao")
    private NovalnetCallbackDao novalnetCallbackDao;

    @Resource
    private CommonI18NService commonI18NService;

    @Resource
    private NovalnetDao novalnetDao;

    /**
     * Updates the order and payment status based on the Novalnet payment gateway status.
     *
     * @param orderCode   the code of the order to update
     * @param paymentInfo the Novalnet payment information containing the gateway status
     * @param order       the order to update
     */
    @Override
    public void updateOrderStatus(String orderCode, NovalnetPaymentInfoModel paymentInfo, OrderModel order) {
        requireNotBlank(orderCode, ORDER_CODE);
        requireNotNull(paymentInfo, "paymentInfo");

        NovalnetPaymentGatewayStatus status = NovalnetPaymentGatewayStatus.fromValue(paymentInfo.getPaymentGatewayStatus())
                .orElseThrow(() -> new RequestParameterException("Invalid payment gateway status", RequestParameterException.INVALID,
                        "paymentGatewayStatus"));

        switch (status) {
            case PENDING:
                order.setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
                order.setPaymentStatus(PaymentStatus.NOTPAID);
                break;

            case ON_HOLD:
                order.setStatus(OrderStatus.PAYMENT_AUTHORIZED);
                order.setPaymentStatus(PaymentStatus.NOTPAID);
                break;

            case CONFIRMED:
                order.setStatus(OrderStatus.COMPLETED);
                order.setPaymentStatus(PaymentStatus.PAID);
                break;

            case FAILURE, DEACTIVATED:
                order.setStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.NOTPAID);
                break;

            default:
                break;
        }

        modelService.save(order);
    }

    /**
     * Updates the payment status of the specified order to partially paid.
     *
     * @param orderCode the code of the order to update
     */
    @Override
    public void updatePartPaidStatus(String orderCode) {
        requireNotBlank(orderCode, ORDER_CODE);

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);
        order.setPaymentStatus(PaymentStatus.PARTPAID);

        modelService.save(order);
    }

    /**
     * Updates the payment gateway status of the latest Novalnet payment information associated with the specified order.
     *
     * @param orderCode            the code of the order
     * @param paymentGatewayStatus the new payment gateway status
     */
    @Override
    public void updatePaymentInfo(String orderCode, String paymentGatewayStatus) {
        requireNotBlank(orderCode, ORDER_CODE);
        requireNotBlank(paymentGatewayStatus, "paymentGatewayStatus");

        NovalnetPaymentInfoModel paymentInfo = novalnetCallbackDao.getLatestNovalnetPaymentInfo(orderCode);
        paymentInfo.setPaymentGatewayStatus(paymentGatewayStatus);

        modelService.save(paymentInfo);
    }

    /**
     * Updates callback transaction information with the callback transaction ID and paid amount.
     *
     * @param callbackTid the callback transaction ID
     * @param originalTid the original transaction ID
     * @param paidAmount  the paid transaction amount
     */
    @Override
    public void updateCallbackInfo(long callbackTid, String originalTid, int paidAmount) {
        requireNotBlank(originalTid, "originalTid");

        NovalnetCallbackInfoModel callback = novalnetCallbackDao.findCallbackInfoByOriginalTid(originalTid);
        callback.setCallbackTid(callbackTid);
        callback.setPaidAmount(paidAmount);

        modelService.save(callback);
    }

    /**
     * Updates payment information with callback comments and creates an order history entry.
     *
     * @param comments          the comments received from the callback
     * @param orderCode         the code of the associated order
     * @param transactionStatus the transaction status to store
     * @param entryComment      the description for the order history entry
     */
    @Override
    public void updateCallbackComments(String comments, String orderCode, String transactionStatus, String entryComment) {
        requireNotBlank(orderCode, ORDER_CODE);
        requireNotBlank(transactionStatus, "transactionStatus");

        NovalnetPaymentInfoModel paymentInfo = novalnetCallbackDao.getLatestNovalnetPaymentInfo(orderCode);

        String existing = paymentInfo.getOrderHistoryNotes() == null ? "" : paymentInfo.getOrderHistoryNotes();

        paymentInfo.setOrderHistoryNotes(existing + "<br/><br/>" + comments);
        paymentInfo.setPaymentGatewayStatus(transactionStatus);

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        OrderHistoryEntryModel entry = modelService.create(OrderHistoryEntryModel.class);

        entry.setOrder(order);
        entry.setTimestamp(Timestamp.from(Instant.now()));
        entry.setDescription(entryComment);

        modelService.saveAll(paymentInfo, entry);
    }

    /**
     * Updates the payment information and order history with callback comments.
     *
     * @param comments          the callback comments to add
     * @param orderCode         the code of the associated order
     * @param transactionStatus the transaction status to store
     */
    @Override
    public void updateCallbackComments(String comments, String orderCode, String transactionStatus) {
        List<NovalnetPaymentInfoModel> paymentInfo = novalnetDao.getNovalnetPaymentInfo(orderCode);
        NovalnetPaymentInfoModel paymentInfoModel = modelService.get(paymentInfo.get(0).getPk());

        String previousComments = paymentInfoModel.getOrderHistoryNotes();
        paymentInfoModel.setOrderHistoryNotes(previousComments + "<br><br>" + comments);
        paymentInfoModel.setPaymentGatewayStatus(transactionStatus);

        List<OrderModel> orderInfoModel = novalnetDao.getOrderInfoModel(orderCode);
        OrderModel orderModel = modelService.get(orderInfoModel.get(0).getPk());

        OrderHistoryEntryModel orderEntry = modelService.create(OrderHistoryEntryModel.class);
        orderEntry.setTimestamp(Timestamp.from(Instant.now()));
        orderEntry.setOrder(orderModel);
        orderEntry.setDescription(comments);

        modelService.saveAll(paymentInfoModel, orderEntry);
    }

    /**
     * Sets the current session language to the language configured for the specified order.
     *
     * @param order the order whose language should be set as the current language
     */
    @Override
    public void setSessionLanguage(OrderModel order) {
        LanguageModel language = order.getLanguage();
        commonI18NService.setCurrentLanguage(language);
    }

    /**
     * Retrieves the locale associated with the language of the specified order.
     *
     * @param order the order whose locale should be retrieved
     * @return the locale associated with the order language
     */
    @Override
    public Locale getLocale(OrderModel order) {
        return commonI18NService.getLocaleForLanguage(order.getLanguage());
    }

    /**
     * Retrieves the currency configured for the specified currency code.
     *
     * @param currency the currency code
     * @return the corresponding currency model
     */
    public CurrencyModel getCurrency(String currency) {
        return commonI18NService.getCurrency(currency);
    }

    /**
     * Validates that the specified string value is not blank.
     *
     * @param value the value to validate
     * @param field the name of the field being validated
     * @throws RequestParameterException if the value is null, empty, or contains only whitespace
     */
    private void requireNotBlank(String value, String field) {
        if (StringUtils.isBlank(value)) {
            throw new RequestParameterException(field + " must not be empty", RequestParameterException.INVALID, field);
        }
    }

    /**
     * Validates that the specified value is not null.
     *
     * @param value the value to validate
     * @param field the name of the field being validated
     * @throws RequestParameterException if the value is null
     */
    private void requireNotNull(Object value, String field) {
        if (value == null) {
            throw new RequestParameterException(field + " must not be null", RequestParameterException.INVALID, field);
        }
    }

    /**
     * Retrieves a localized label for the specified localization key.
     *
     * @param key the localization key
     * @return the localized text associated with the key
     */
    @Override
    public String getLabel(String key) {
        return Localization.getLocalizedString(key);
    }

    /**
     * Builds order history notes from the Novalnet transaction information.
     *
     * @param transaction the Novalnet transaction details
     * @param amount      the transaction amount
     * @param paymentName the payment method name
     * @return the generated order history notes
     */
    @Override
    public String buildOrderHistoryNotes(JSONObject transaction, String amount, String paymentName) {
        StringBuilder notes = new StringBuilder(ORDER_HISTORY_NOTES_INITIAL_CAPACITY);

        appendPaymentHeader(notes, transaction, paymentName);
        appendTestMode(notes, transaction);
        appendBankDetails(notes, transaction, amount);
        appendPartnerPaymentReference(notes, transaction, amount);

        return notes.toString();
    }

    /**
     * Appends payment method and transaction information to the order history notes.
     *
     * @param notes       the order history notes being built
     * @param transaction the Novalnet transaction details
     * @param paymentName the payment method name
     */
    private void appendPaymentHeader(StringBuilder notes, JSONObject transaction, String paymentName) {
        notes.append(getLabel("novalnet.paymentname")).append(" : ").append(paymentName).append(BR)
                .append(getLabel("novalnet.transactionID")).append(" ").append(transaction.optString("tid")).append(BR);

        if ("0".equals(transaction.optString("amount"))) {
            notes.append(getLabel("novalnet.zeroAmountTransactionText")).append(BR);
        }
    }

    /**
     * Appends the test mode information when the transaction was processed in test mode.
     *
     * @param notes       the order history notes being built
     * @param transaction the Novalnet transaction details
     */
    private void appendTestMode(StringBuilder notes, JSONObject transaction) {
        if ("1".equals(transaction.optString("test_mode"))) {
            notes.append(getLabel("novalnet.testOrderText"));
        }
    }

    /**
     * Appends bank details and payment reference information to the order history notes.
     *
     * @param notes       the order history notes being built
     * @param transaction the Novalnet transaction details
     * @param amount      the transaction amount
     */
    private void appendBankDetails(StringBuilder notes, JSONObject transaction, String amount) {
        JSONObject bankDetails = transaction.optJSONObject("bank_details");
        String status = transaction.optString("status");

        if (!"75".equals(transaction.optString("status_code"))) {
            if (bankDetails != null) {
                notes.append(BR).append(BR).append(String.format(getLabel("novalnet.bankDetailsComments1"), amount));

                if (transaction.has("due_date") && !STATUS_ON_HOLD.equals(status)) {
                    notes.append(" ")
                            .append(String.format(getLabel("novalnet.bankDetailsComments2"), transaction.optString("due_date")));
                }

                appendLabelValue(notes, "novalnet.bankDetailsAccountHolder", bankDetails.optString("account_holder"));

                appendLabelValue(notes, "novalnet.bankDetailsIban", bankDetails.optString("iban"));

                appendLabelValue(notes, "novalnet.bankDetailsBic", bankDetails.optString("bic"));

                appendLabelValue(notes, "novalnet.bankDetailsBank", bankDetails.optString("bank_name"));

                appendLabelValue(notes, "novalnet.bankPlace", bankDetails.optString("bank_place"));

                notes.append(BR).append(getLabel("novalnet.bankDetailspaymentRefernceMulti")).append(BR)
                        .append(getLabel("novalnet.bankDetailsPaymentReference")).append(" : TID ").append(transaction.optString("tid"))
                        .append(BR);

                if (StringUtils.isNotBlank(bankDetails.optString(QR_IMAGE))) {
                    notes.append(getLabel("novalnet.qrCodeComments")).append(BR).append(BR).append("<img alt='nn_qr_code' src='")
                            .append(bankDetails.optString(QR_IMAGE)).append("'>").append(BR);
                }
            }
        } else {
            notes.append(getLabel("novalnet.guaranteePendingNote"));
        }
    }

    /**
     * Appends the partner payment reference to the order history notes when available.
     *
     * @param notes       the order history notes being built
     * @param transaction the Novalnet transaction details
     * @param amount      the transaction amount
     */
    private void appendPartnerPaymentReference(StringBuilder notes, JSONObject transaction, String amount) {
        if (!transaction.has("partner_payment_reference")) {
            return;
        }

        notes.append(BR).append(BR).append(getLabel("novalnet.multibancocomments1")).append(" ").append(amount).append(" ")
                .append(getLabel("novalnet.multibancocomments2")).append(BR).append(getLabel("novalnet.bankDetailsPaymentReference"))
                .append(" : ").append(transaction.optString("partner_payment_reference"));
    }

    /**
     * Appends a localized label and its value to the order history notes when the value is not blank.
     *
     * @param sb       the string builder containing the order history notes
     * @param labelKey the localization key for the label
     * @param value    the value to append
     */
    private void appendLabelValue(StringBuilder sb, String labelKey, String value) {
        if (StringUtils.isNotBlank(value)) {
            sb.append(BR).append(getLabel(labelKey)).append(" ").append(value);
        }
    }
}
