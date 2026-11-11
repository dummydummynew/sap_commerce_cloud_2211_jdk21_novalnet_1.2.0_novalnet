/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.novalnet.dao.NovalnetCallbackDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import com.novalnet.service.order.NovalnetOrderService;
import com.novalnet.service.payment.NovalnetCallbackService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.novalnet.beans.NnCallbackCollectionData;
import de.novalnet.beans.NnCallbackCustomData;
import de.novalnet.beans.NnCallbackEventData;
import de.novalnet.beans.NnCallbackRefundData;
import de.novalnet.beans.NnCallbackRequestData;
import de.novalnet.beans.NnCallbackTransactionData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.Locale;
import java.util.Set;


/**
 * Default implementation of {@link NovalnetCallbackService} for processing Novalnet callback events.
 * <p>
 * Handles transaction capture, cancellation, updates, payments, credits, refunds, reminders, and collection events.
 */
public class DefaultNovalnetCallbackService implements NovalnetCallbackService {
    private static final Logger LOG = Logger.getLogger(DefaultNovalnetCallbackService.class);

    private static final Set<String> UPDATE_WITH_DATE_AMOUNT = Set.of("DUE_DATE", "AMOUNT_DUE_DATE");

    private static final Set<String> BANK_DETAILS_PAYMENT_TYPES = Set.of("INVOICE", "GUARANTEED_INVOICE");

    private static final Set<String> CREDIT_PAYMENT_TYPES = Set.of("INVOICE_CREDIT", "CASHPAYMENT_CREDIT", "MULTIBANCO_CREDIT");

    private static final int CURRENCY_DECIMAL_PLACES = 2;

    private String currentDate;

    @Resource(name = "objectMapper")
    private ObjectMapper objectMapper;

    @Resource(name = "novalnetCallbackDao")
    private NovalnetCallbackDao novalnetCallbackDao;

    @Resource(name = "novalnetCallbackOrderService")
    private NovalnetCallbackOrderService novalnetCallbackOrderService;

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;

    @Resource
    private NovalnetOrderService novalnetOrderService;

    /**
     * Processes an incoming Novalnet callback request after validating the request.
     *
     * @param request     the Novalnet callback request data
     * @param httpRequest the HTTP servlet request
     * @return the result of processing the callback event
     */
    @Override
    public String processCallback(NnCallbackRequestData request, HttpServletRequest httpRequest) {
        BaseStoreModel baseStore = baseStoreService.getCurrentBaseStore();

        NovalnetUtils.validateCallbackIp(httpRequest, baseStore);
        NovalnetUtils.validateMandatoryParams(request);
        NovalnetUtils.validateCallbackChecksum(request, baseStore);

        currentDate = getCurrentDate();

        String eventType = request.getEvent().getType();

        logEvent("CALLBACK_RECEIVED", request);

        switch (eventType) {
            case "TRANSACTION_CAPTURE":
                return handleTransactionCapture(request);

            case "TRANSACTION_CANCEL":
                return handleTransactionCancel(request);

            case "TRANSACTION_UPDATE":
                return handleTransactionUpdate(request);

            case "PAYMENT":
                return handlePayment(request);

            case "CREDIT":
                return handleCredit(request);

            case "TRANSACTION_REFUND":
                return handleRefund(request);

            case "CHARGEBACK":
                return handleRefund(request);

            case "PAYMENT_REMINDER_1", "PAYMENT_REMINDER_2":
                return handleReminder(request);

            case "SUBMISSION_TO_COLLECTION_AGENCY":
                return handleCollection(request);

            default:
                return "Unsupported callback event type: " + eventType;
        }
    }

    /**
     * Handles a transaction capture callback from Novalnet.
     *
     * @param request the Novalnet callback request data
     * @return the callback processing message
     */
    public String handleTransactionCapture(NnCallbackRequestData request) {
        NnCallbackTransactionData txn = request.getTransaction();
        NnCallbackCustomData custom = request.getCustom();

        String orderCode = txn.getOrder_no();

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);
        novalnetCallbackOrderService.updatePaymentInfo(orderCode, txn.getStatus());
        novalnetCallbackOrderService.updateOrderStatus(orderCode, novalnetCallbackDao.getLatestNovalnetPaymentInfo(orderCode), order);

        String paymentType = txn.getPayment_type();

        boolean isInvoice = "GUARANTEED_INVOICE".equals(paymentType) || "INVOICE".equals(paymentType);

        String msg = isInvoice
                ? String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackCaptureUpdateText"), txn.getTid(), txn.getDue_date())
                : String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackConfirmUpdateText"), currentDate);

        String shortComment = msg;

        msg = buildInitialComments(msg, txn, custom, order);

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, txn.getStatus(), shortComment);

        logEvent("TRANSACTION_CAPTURE", request);

        return msg;
    }

    /**
     * Builds the initial callback comments for payment types that require bank details.
     *
     * @param msg         the initial callback message
     * @param transaction the Novalnet transaction data
     * @param custom      the custom callback data
     * @param order       the associated order
     * @return the updated callback message
     */
    private String buildInitialComments(String msg, final NnCallbackTransactionData transaction, final NnCallbackCustomData custom,
                                        final OrderModel order) {

        if (BANK_DETAILS_PAYMENT_TYPES.contains(transaction.getPayment_type())) {
            ObjectNode transactionObject = convertToJson(transaction);
            JSONObject transactionJson = new JSONObject(transactionObject.toString());
            String initialComments = novalnetCallbackOrderService.buildOrderHistoryNotes(transactionJson,
                    formatAmount(transaction.getAmount(), transaction.getCurrency(), order), custom.getInputval1());
            msg = msg + "<br>" + initialComments;
        }

        return msg;
    }

    /**
     * Converts the specified callback transaction data into a JSON object node.
     *
     * @param data the Novalnet transaction data
     * @return the transaction data represented as a JSON object node
     */
    public ObjectNode convertToJson(NnCallbackTransactionData data) {
        return objectMapper.valueToTree(data);
    }

    /**
     * Handles a transaction cancellation callback from Novalnet.
     *
     * @param request the Novalnet callback request data
     * @return the callback cancellation message
     */
    public String handleTransactionCancel(NnCallbackRequestData request) {
        String orderCode = request.getTransaction().getOrder_no();

        novalnetOrderService.updateCancelStatus(orderCode);

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);

        String msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackCancelUpdateText"), currentDate);

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, request.getTransaction().getStatus(), msg);

        logEvent("TRANSACTION_CANCEL", request);

        return msg;
    }

    /**
     * Handles a transaction update callback and updates the corresponding order and payment information.
     *
     * @param request the Novalnet callback request data
     * @return the callback update message
     */
    public String handleTransactionUpdate(NnCallbackRequestData request) {
        NnCallbackTransactionData transaction = request.getTransaction();
        NnCallbackCustomData custom = request.getCustom();

        String tid = transaction.getTid();
        String status = transaction.getStatus();
        String updateType = transaction.getUpdate_type();
        String orderCode = transaction.getOrder_no();

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);
        novalnetCallbackOrderService.updatePaymentInfo(orderCode, status);

        String msg = "";
        String shortComment = "";

        if (UPDATE_WITH_DATE_AMOUNT.contains(updateType)) {
            msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackAmountDueDateUpdateText"),
                    formatAmount(transaction.getAmount(), transaction.getCurrency(), order), transaction.getDue_date());
        } else if ("AMOUNT".equals(updateType)) {
            msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackAmountUpdateText"), tid,
                    formatAmount(transaction.getAmount(), transaction.getCurrency(), order), currentDate);
        } else if ("STATUS".equals(updateType)) {
            switch (status) {
                case "DEACTIVATED":
                    msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackCancelUpdateText"), currentDate);
                    break;

                case "ON_HOLD":
                    msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackOnhodUpdateText"), tid, currentDate);

                    shortComment = msg;
                    msg = buildInitialComments(msg, transaction, custom, order);
                    break;

                case "CONFIRMED":
                    msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackConfirmUpdateText"), currentDate);

                    shortComment = msg;
                    msg = buildInitialComments(msg, transaction, custom, order);
                    break;

                default:
                    msg = String.format("Transaction status updated for TID %s to %s", tid, status);
                    break;
            }

            novalnetCallbackOrderService.updateOrderStatus(orderCode, novalnetCallbackDao.getLatestNovalnetPaymentInfo(orderCode), order);
        }

        if ("".equals(shortComment)) {
            shortComment = msg;
        }

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, status, shortComment);

        logEvent("TRANSACTION_UPDATE", request);

        return msg;
    }

    /**
     * Formats the transaction amount using the order locale and currency.
     *
     * @param amount   the transaction amount in the smallest currency unit
     * @param currency the transaction currency code
     * @param order    the associated order
     * @return the formatted currency amount
     */
    public String formatAmount(String amount, String currency, OrderModel order) {
        double orderAmount = new BigDecimal(amount).movePointLeft(CURRENCY_DECIMAL_PLACES).doubleValue();

        Locale locale = novalnetCallbackOrderService.getLocale(order);

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(locale != null ? locale : Locale.getDefault());

        currencyFormat.setCurrency(Currency.getInstance(currency));

        return currencyFormat.format(orderAmount);
    }

    /**
     * Handles a credit callback and updates the payment information when the required amount has been received.
     *
     * @param request the Novalnet callback request data
     * @return the callback credit message
     */
    public String handleCredit(NnCallbackRequestData request) {
        NnCallbackTransactionData txn = request.getTransaction();

        String parentTid = request.getEvent().getParent_tid();

        NovalnetCallbackInfoModel ref = novalnetCallbackDao.findCallbackInfoByOriginalTid(parentTid);

        String orderCode = txn.getOrder_no();

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);

        if (CREDIT_PAYMENT_TYPES.contains(txn.getPayment_type())) {
            int currentPaid = Math.toIntExact(ref.getPaidAmount());

            int receivedAmount = Integer.parseInt(txn.getAmount());

            int totalPaid = currentPaid + receivedAmount;

            novalnetCallbackOrderService.updateCallbackInfo(Long.parseLong(txn.getTid()), parentTid, totalPaid);

            if (totalPaid >= ref.getOrderAmount()) {
                novalnetCallbackOrderService.updatePaymentInfo(orderCode, txn.getStatus());

                novalnetCallbackOrderService.updateOrderStatus(orderCode, novalnetCallbackDao.getLatestNovalnetPaymentInfo(orderCode), order);
            }
        }

        String msg = buildCreditMessage(txn, parentTid, order);

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, txn.getStatus(), msg);

        logEvent("CREDIT", request);

        return msg;
    }

    /**
     * Builds the message for a credit callback.
     *
     * @param txn       the Novalnet transaction data
     * @param parentTid the original transaction ID
     * @param order     the associated order
     * @return the formatted credit callback message
     */
    private String buildCreditMessage(NnCallbackTransactionData txn, String parentTid, OrderModel order) {
        return String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackCreditText"), parentTid,
                formatAmount(txn.getAmount(), txn.getCurrency(), order), getCurrentDate(), txn.getTid());
    }

    /**
     * Handles a refund or chargeback callback and updates the associated order.
     *
     * @param request the Novalnet callback request data
     * @return the callback refund or chargeback message
     */
    public String handleRefund(NnCallbackRequestData request) {
        NnCallbackTransactionData transaction = request.getTransaction();

        NnCallbackEventData event = request.getEvent();

        String orderCode = transaction.getOrder_no();

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);

        String transactionStatus = transaction.getStatus();

        String msg = buildRefundMessage(transaction, event, order);

        if ("DEACTIVATED".equals(transactionStatus)) {
            novalnetOrderService.updateCancelStatus(orderCode);
        }

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, transactionStatus, msg);

        logEvent("REFUND", request);

        return msg;
    }

    /**
     * Builds the message for a refund or chargeback callback.
     *
     * @param transaction the Novalnet transaction data
     * @param event       the callback event data
     * @param order       the associated order
     * @return the formatted refund or chargeback message
     */
    private String buildRefundMessage(NnCallbackTransactionData transaction, NnCallbackEventData event, OrderModel order) {
        String eventType = (event != null) ? event.getType() : null;

        String currency = transaction.getCurrency();

        String originalTid = getOriginalTid(event, transaction);

        if (!"TRANSACTION_REFUND".equals(eventType)) {
            return String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackChargebackText"), originalTid,
                    formatAmount(transaction.getAmount(), currency, order), getCurrentDate(), transaction.getTid());
        }

        NnCallbackRefundData refund = transaction.getRefund();

        if (refund == null || refund.getTid() == null) {
            String refundAmount = (refund != null) ? refund.getAmount() : "0";

            return String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackRefundTextOrgTid"), transaction.getTid(),
                    formatAmount(refundAmount, currency, order));
        }

        String newTid = (event.getTid() != null) ? event.getTid() : refund.getTid();

        return String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackRefundText"), originalTid,
                formatAmount(refund.getAmount(), currency, order), newTid);
    }

    /**
     * Determines the original transaction ID from the callback event or transaction data.
     *
     * @param event       the callback event data
     * @param transaction the transaction data
     * @return the original transaction ID
     */
    private String getOriginalTid(NnCallbackEventData event, NnCallbackTransactionData transaction) {
        return (event != null && event.getParent_tid() != null) ? event.getParent_tid() : transaction.getTid();
    }

    /**
     * Handles a payment reminder callback from Novalnet.
     *
     * @param request the Novalnet callback request data
     * @return the callback reminder message
     */
    public String handleReminder(NnCallbackRequestData request) {
        NnCallbackTransactionData transaction = request.getTransaction();

        NnCallbackEventData event = request.getEvent();

        String orderCode = transaction.getOrder_no();

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);

        String eventType = event != null ? event.getType() : null;

        String count = "PAYMENT_REMINDER_1".equals(eventType) ? "1" : "2";

        String msg = String.format(novalnetCallbackOrderService.getLabel("novalnet.callbackReminderText"), count);

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, transaction.getStatus(), msg);

        logEvent("REMINDER", request);

        return msg;
    }

    /**
     * Handles a submission to collection agency callback from Novalnet.
     *
     * @param request the Novalnet callback request data
     * @return the callback collection message
     */
    public String handleCollection(NnCallbackRequestData request) {
        NnCallbackTransactionData transaction = request.getTransaction();

        NnCallbackCollectionData collection = request.getCollection();

        String orderCode = transaction.getOrder_no();

        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.setSessionLanguage(order);

        String label = novalnetCallbackOrderService.getLabel("novalnet.callbackCollectionText");

        StringBuilder msgBuilder = new StringBuilder(label);

        if (collection != null && collection.getReference() != null) {
            String reference = collection.getReference();

            String refLabel = novalnetCallbackOrderService.getLabel("novalnet.callbackCollectionReferenceText");

            msgBuilder.append(' ').append(String.format(refLabel, reference));
        }

        String msg = msgBuilder.toString();

        novalnetCallbackOrderService.updateCallbackComments(msg, orderCode, transaction.getStatus(), msg);

        logEvent("COLLECTION", request);

        return msg;
    }

    /**
     * Logs the details of a processed Novalnet callback event.
     *
     * @param type    the callback event type
     * @param request the Novalnet callback request data
     */
    private void logEvent(String type, NnCallbackRequestData request) {
        LOG.info(
                String.format("{ \"event\":\"%s\", \"tid\":\"%s\", \"parentTid\":\"%s\", \"status\":\"%s\", \"timestamp\":\"%s\" }",
                        type, request.getTransaction().getTid(), request.getEvent().getParent_tid(),
                        request.getTransaction().getStatus(), Instant.now()));
    }

    /**
     * Returns the current date and time in the configured callback date format.
     *
     * @return the current date and time formatted as dd-MM-yyyy, HH:mm:ss
     */
    private String getCurrentDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss");
        return LocalDateTime.now().format(formatter);
    }

    /**
     * Handles a Novalnet payment webhook callback and updates the corresponding order and payment information based on the transaction
     * status.
     *
     * @param request the Novalnet callback request containing transaction details
     * @return a message indicating that the webhook was processed successfully
     */
    public String handlePayment(NnCallbackRequestData request) {
        NnCallbackTransactionData txn = request.getTransaction();
        String status = txn.getStatus();
        String orderCode = txn.getOrder_no();
        OrderModel order = novalnetCallbackDao.findOrderByCode(orderCode);

        novalnetCallbackOrderService.updatePaymentInfo(orderCode, status);
        novalnetCallbackOrderService.updateOrderStatus(orderCode, novalnetCallbackDao.getLatestNovalnetPaymentInfo(orderCode), order);
        logEvent("PAYMENT", request);
        return "Novalnet webhook script executed. Status updated for initial transaction";
    }
}
