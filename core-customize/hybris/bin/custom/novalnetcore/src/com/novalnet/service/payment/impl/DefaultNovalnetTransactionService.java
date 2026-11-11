package com.novalnet.service.payment.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.NovalnetTransactionResult;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import com.novalnet.service.order.NovalnetOrderService;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.service.payment.NovalnetTransactionCommentService;
import com.novalnet.service.payment.NovalnetTransactionService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.payment.AdapterException;
import de.hybris.platform.payment.PaymentService;
import de.hybris.platform.payment.commands.request.FollowOnRefundRequest;
import de.hybris.platform.payment.constants.GeneratedPaymentConstants.Enumerations.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.returns.model.ReturnRequestModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.warehousing.returns.service.RefundAmountCalculationService;
import jakarta.annotation.Resource;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import static de.hybris.platform.basecommerce.enums.ConsignmentStatus.PICKUP_COMPLETE;
import static de.hybris.platform.basecommerce.enums.ConsignmentStatus.SHIPPED;


/**
 * * Default implementation of {@link NovalnetTransactionService} for managing Novalnet transactions. *
 * <p>
 * * Handles transaction cancellation, capture, refunds, return requests, and payment transaction details.
 */
public class DefaultNovalnetTransactionService implements NovalnetTransactionService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultNovalnetTransactionService.class);

    private static final String ON_HOLD = "ON_HOLD";
    private static final String NOVALNET_PAYMENT_PREFIX = "novalnet";
    private static final String NOVALNET_STATUS_SUCCESS = "SUCCESS";
    private static final String CAPTURED = "CAPTURE";
    private static final long AMOUNT_MULTIPLIER_CENTS = 100;

    private static final String STATUS = "status";
    private static final String SHOP_INVOKED = "shop_invoked";
    private static final String RESULT = "result";
    private static final String TRANSACTION = "transaction";
    private static final String STATUS_TEXT = "status_text";
    private static final String ORDER_NO = "order_no";
    private static final String NOVALNET_ORDER_NOTFOUND = "novalnet.order.notfound";
    private static final String AMOUNT = "amount";
    private static final String CUSTOM = "custom";
    private static final int CURRENCY_DECIMAL_PLACES = 2;

    @Resource(name = "novalnetPaymentService")
    private NovalnetPaymentService novalnetPaymentService;

    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Resource(name = "novalnetTransactionCommentService")
    private NovalnetTransactionCommentService novalnetTransactionCommentService;

    @Resource
    private NovalnetDao novalnetDao;


    @Resource(name = "novalnetOrderService")
    private NovalnetOrderService novalnetOrderService;

    @Resource(name = "novalnetApiService")
    private NovalnetApiService novalnetApiService;


    @Resource
    private PaymentService paymentService;

    @Resource
    private RefundAmountCalculationService refundAmountCalculationService;

    @Resource
    private ModelService modelService;

    @Resource
    private NovalnetCallbackOrderService novalnetCallbackOrderService;


    @Override
    public boolean canCancel(OrderModel order) {
        if (order == null) {
            return false;
        }

        String paymentMethod = order.getPaymentMode() != null ? order.getPaymentMode().getCode() : null;

        if (paymentMethod != null && paymentMethod.startsWith(NOVALNET_PAYMENT_PREFIX)) {
            List<NovalnetPaymentInfoModel> paymentInfoList = novalnetDao.getNovalnetPaymentInfo(order.getCode());

            if (paymentInfoList != null) {
                return paymentInfoList.stream()
                        .anyMatch(paymentInfo -> ON_HOLD.equalsIgnoreCase(paymentInfo.getPaymentGatewayStatus()));
            }
        }

        return false;
    }

    @Override
    public NovalnetTransactionResult cancelOrder(OrderModel order) {
        List<NovalnetCallbackInfoModel> orderReferences = novalnetDao.getPaymentDetailsInfo(order.getCode());

        if (orderReferences == null || orderReferences.isEmpty()) {
            LOG.warn("No order reference found for order: {}", order.getCode());
            return new NovalnetTransactionResult(false, NOVALNET_ORDER_NOTFOUND);
        }

        long originalTid = orderReferences.get(0).getOrginalTid();

        try {
            return cancelPayment(originalTid, order.getStore(), order);
        } catch (Exception e) {
            LOG.error("Failed to perform order cancel", e);
            return new NovalnetTransactionResult(false, "novalnet.order.cancel.failed");
        }
    }

    private NovalnetTransactionResult cancelPayment(long tid, BaseStoreModel baseStore, OrderModel order) {
        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);

        JSONObject custom = new JSONObject();
        custom.put("lang", NovalnetUtils.getLanguageCode(order));
        custom.put(SHOP_INVOKED, 1);

        JSONObject request = new JSONObject();
        request.put(TRANSACTION, transaction);
        request.put(CUSTOM, custom);

        String response = novalnetApiService.followupSendRequest(novalnetEndpointConfigService.getTransactionCancelUrl(),
                request.toString(), baseStore);

        JSONObject jsonResponse = new JSONObject(response);
        JSONObject result = jsonResponse.getJSONObject(RESULT);

        if (NOVALNET_STATUS_SUCCESS.equals(result.getString(STATUS))) {
            return handleCancelSuccess(jsonResponse);
        }

        String statusText = result.getString(STATUS_TEXT);

        LOG.warn("Novalnet cancel failed: {}", statusText);

        return new NovalnetTransactionResult(false, statusText);
    }

    private NovalnetTransactionResult handleCancelSuccess(JSONObject jsonResponse) {
        JSONObject transaction = jsonResponse.getJSONObject(TRANSACTION);

        String transactionStatus = transaction.getString(STATUS);
        String nnOrderNo = transaction.getString(ORDER_NO);

        List<NovalnetPaymentInfoModel> paymentInfoList = novalnetDao.getNovalnetPaymentInfo(nnOrderNo);

        String extensionComments = novalnetTransactionCommentService.buildCancelComments();

        novalnetPaymentService.updatePaymentInfo(paymentInfoList, transactionStatus);
        novalnetOrderService.updateCancelStatus(nnOrderNo);

        novalnetCallbackOrderService.updateCallbackComments(extensionComments, nnOrderNo, transactionStatus);

        return new NovalnetTransactionResult(true, extensionComments);
    }

    @Override
    public NovalnetTransactionResult captureOrder(OrderModel order) throws JsonProcessingException {
        if (order == null) {
            LOG.warn("Order is null in capture service");
            return new NovalnetTransactionResult(false, "novalnet.order.null");
        }

        BaseStoreModel baseStore = order.getStore();

        if (baseStore == null) {
            LOG.warn("BaseStore is missing for order: {}", order.getCode());
            return new NovalnetTransactionResult(false, "novalnet.order.capture.failed");
        }

        List<NovalnetCallbackInfoModel> orderReferences = novalnetDao.getPaymentDetailsInfo(order.getCode());

        if (orderReferences == null || orderReferences.isEmpty()) {
            LOG.warn("No order reference found for order: {}", order.getCode());
            return new NovalnetTransactionResult(false, NOVALNET_ORDER_NOTFOUND);
        }

        long originalTid = orderReferences.get(0).getOrginalTid();

        return capturePayment(originalTid, baseStore, order);
    }

    private NovalnetTransactionResult capturePayment(long tid, BaseStoreModel baseStore, OrderModel order) {
        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);

        JSONObject custom = new JSONObject();
        custom.put("lang", NovalnetUtils.getLanguageCode(order));
        custom.put(SHOP_INVOKED, 1);

        JSONObject request = new JSONObject();
        request.put(TRANSACTION, transaction);
        request.put(CUSTOM, custom);

        String response = novalnetApiService.followupSendRequest(novalnetEndpointConfigService.getTransactionCaptureUrl(),
                request.toString(), baseStore);

        JSONObject jsonResponse = new JSONObject(response);
        JSONObject result = jsonResponse.getJSONObject(RESULT);

        if (NOVALNET_STATUS_SUCCESS.equals(result.getString(STATUS))) {
            return handleCaptureSuccess(jsonResponse);
        }

        String statusText = result.getString(STATUS_TEXT);

        LOG.warn("Novalnet capture failed: {}", statusText);

        return new NovalnetTransactionResult(false, statusText);
    }

    private NovalnetTransactionResult handleCaptureSuccess(JSONObject jsonResponse) {
        JSONObject transaction = jsonResponse.getJSONObject(TRANSACTION);

        String transactionStatus = transaction.getString(STATUS);
        String nnOrderNo = transaction.getString(ORDER_NO);

        List<NovalnetPaymentInfoModel> paymentInfoList = novalnetDao.getNovalnetPaymentInfo(nnOrderNo);

        NovalnetPaymentInfoModel paymentInfoModel = novalnetPaymentService.getPaymentModel(paymentInfoList);

        String extensionComments = novalnetTransactionCommentService.buildCaptureComments(transaction);

        novalnetPaymentService.updatePaymentInfo(paymentInfoList, CAPTURED);

        novalnetOrderService.updateOrderStatus(nnOrderNo, paymentInfoModel);

        novalnetCallbackOrderService.updateCallbackComments(extensionComments, nnOrderNo, transactionStatus);

        return new NovalnetTransactionResult(true, extensionComments);
    }

    @Override
    public boolean canCapture(OrderModel order) {
        if (order == null) {
            return false;
        }

        String paymentMethod = order.getPaymentMode() != null ? order.getPaymentMode().getCode() : null;

        if (paymentMethod != null && paymentMethod.startsWith(NOVALNET_PAYMENT_PREFIX)) {
            List<NovalnetPaymentInfoModel> paymentInfoList = novalnetDao.getNovalnetPaymentInfo(order.getCode());

            if (paymentInfoList != null) {
                return paymentInfoList.stream()
                        .anyMatch(paymentInfo -> ON_HOLD.equalsIgnoreCase(paymentInfo.getPaymentGatewayStatus()));
            }
        }

        return false;
    }

    @Override
    public boolean canCreateReturnRequest(OrderModel order) {
        if (order == null) {
            return false;
        }

        if (isFullyRefunded(order)) {
            return false;
        }

        boolean returnable = isReturnable(order);
        boolean refundable = isRefundable(order);
        return returnable && refundable;

    }

    public boolean isReturnable(OrderModel order) {
        if (order == null || order.getConsignments() == null || order.getEntries() == null) {
            return false;
        }

        return order.getConsignments().stream()
                .anyMatch(consignment -> SHIPPED.equals(consignment.getStatus()) || PICKUP_COMPLETE.equals(consignment.getStatus()));

    }

    @Override
    public boolean isRefundable(OrderModel order) {
        if (!(order.getPaymentInfo() instanceof NovalnetPaymentInfoModel)) {
            return false;
        }

        List<NovalnetPaymentInfoModel> paymentInfoList = novalnetDao.getNovalnetPaymentInfo(order.getCode());

        if (paymentInfoList == null || paymentInfoList.isEmpty()) {
            return false;
        }

        String status = paymentInfoList.get(0).getPaymentGatewayStatus();

        if ("REFUND".equalsIgnoreCase(status)) {
            return false;
        }

        return status != null && "CONFIRMED".equalsIgnoreCase(status);
    }

    @Override
    public boolean isFullyRefunded(OrderModel order) {
        if (order.getPaymentTransactions() == null || order.getPaymentTransactions().isEmpty()) {
            LOG.info("Order {} has no payment transactions", order.getCode());
            return false;
        }

        long totalRefundedCents = 0;

        for (PaymentTransactionModel transaction : order.getPaymentTransactions()) {

            for (PaymentTransactionEntryModel entry : transaction.getEntries()) {

                if (PaymentTransactionType.REFUND_FOLLOW_ON.equals(entry.getType())
                        && "ACCEPTED".equalsIgnoreCase(entry.getTransactionStatus())) {
                    long amountCents = entry.getAmount().multiply(BigDecimal.valueOf(AMOUNT_MULTIPLIER_CENTS)).longValue();

                    totalRefundedCents += amountCents;

                }
            }
        }

        long orderTotalCents = BigDecimal.valueOf(order.getTotalPrice()).multiply(BigDecimal.valueOf(AMOUNT_MULTIPLIER_CENTS))
                .longValue();

        return totalRefundedCents >= orderTotalCents;

    }

    @SuppressWarnings("java:S1541")
    @Override
    public NovalnetTransactionResult refund(ReturnRequestModel returnRequest) {
        if (returnRequest == null || returnRequest.getOrder() == null) {
            LOG.error("Return request or order is null");
            return new NovalnetTransactionResult(false, NOVALNET_ORDER_NOTFOUND);
        }

        List<PaymentTransactionModel> transactions = returnRequest.getOrder().getPaymentTransactions();

        if (transactions == null || transactions.isEmpty()) {
            LOG.warn("No payment transaction found for return request: {}", returnRequest.getCode());

            return new NovalnetTransactionResult(false, NOVALNET_ORDER_NOTFOUND);
        }

        PaymentTransactionModel transaction = transactions.get(0);

        BigDecimal customRefundAmount = refundAmountCalculationService.getCustomRefundAmount(returnRequest);

        BigDecimal amountToRefund;

        if (customRefundAmount != null && customRefundAmount.compareTo(BigDecimal.ZERO) > 0) {
            amountToRefund = customRefundAmount;

        } else {
            amountToRefund = refundAmountCalculationService.getOriginalRefundAmount(returnRequest);

        }

        if (amountToRefund == null || amountToRefund.compareTo(BigDecimal.ZERO) <= 0) {
            LOG.warn("Invalid refund amount {} for return request {}", amountToRefund, returnRequest.getCode());

            return new NovalnetTransactionResult(false, "novalnet.refund.amount.invalid");
        }

        try {
            PaymentTransactionEntryModel refundEntry = paymentService.refundFollowOn(transaction, amountToRefund);

            if (refundEntry == null || !"ACCEPTED".equalsIgnoreCase(refundEntry.getTransactionStatus())) {
                LOG.error("Refund was not accepted for return request {}", returnRequest.getCode());

                return new NovalnetTransactionResult(false, "novalnet.order.refund.failed");
            }

            LOG.info("Refund successful for return request {}", returnRequest.getCode());

            return new NovalnetTransactionResult(true, "novalnet.order.refund.success");
        } catch (AdapterException e) {
            LOG.error("Refund failed for return request {}", returnRequest.getCode(), e);

            return new NovalnetTransactionResult(false, "novalnet.order.refund.failed");
        }
    }

    @Override
    public NovalnetTransactionResult processRefund(FollowOnRefundRequest request) throws JsonProcessingException {
        if (request == null) {
            LOG.error("Refund request is null");
            return new NovalnetTransactionResult(false, "Refund request is null");
        }

        String tid = request.getRequestId();
        BigDecimal totalAmount = request.getTotalAmount();

        if (tid == null || tid.isBlank()) {
            LOG.error("Refund TID is missing");
            return new NovalnetTransactionResult(false, "Refund TID is missing");
        }

        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            LOG.error("Invalid refund amount for TID: {}", tid);
            return new NovalnetTransactionResult(false, "Invalid refund amount");
        }

        return executeRefund(tid, totalAmount);
    }

    private NovalnetTransactionResult executeRefund(String tid, BigDecimal totalAmount) {

        OrderModel order = novalnetDao.getOrderByTid(tid);

        if (order == null) {
            LOG.error("No order found for TID: {}", tid);
            return new NovalnetTransactionResult(false, "Order not found");
        }

        BigDecimal orderTotal = order.getTotalPrice() != null ? BigDecimal.valueOf(order.getTotalPrice()) : BigDecimal.ZERO;

        if (totalAmount.compareTo(orderTotal) > 0) {
            LOG.error("Refund amount {} exceeds order total {} for order {}", totalAmount, orderTotal, order.getCode());

            return new NovalnetTransactionResult(false, "Refund amount exceeds order total");
        }

        BaseStoreModel baseStore = order.getStore();

        if (baseStore == null) {
            LOG.error("BaseStore is null for order: {}", order.getCode());
            return new NovalnetTransactionResult(false, "BaseStore is missing");
        }

        JSONObject response = sendRefundRequest(tid, totalAmount, order, baseStore);

        JSONObject result = response.getJSONObject(RESULT);

        if (!NOVALNET_STATUS_SUCCESS.equalsIgnoreCase(result.getString(STATUS))) {
            String statusText = result.optString(STATUS_TEXT, "Refund failed");

            LOG.error("Novalnet refund failed for TID {}: {}", tid, statusText);

            return new NovalnetTransactionResult(false, statusText);
        }

        return handleRefundSuccess(tid, response);
    }

    private JSONObject sendRefundRequest(String tid, BigDecimal totalAmount, OrderModel order, BaseStoreModel baseStore) {
        int amountInCents = totalAmount.multiply(BigDecimal.valueOf(AMOUNT_MULTIPLIER_CENTS)).setScale(0, RoundingMode.HALF_UP)
                .intValue();

        JSONObject transaction = new JSONObject();
        transaction.put("tid", tid);
        transaction.put(AMOUNT, amountInCents);

        JSONObject custom = new JSONObject();
        custom.put("lang", NovalnetUtils.getLanguageCode(order));
        custom.put(SHOP_INVOKED, 1);

        JSONObject data = new JSONObject();
        data.put(TRANSACTION, transaction);
        data.put(CUSTOM, custom);

        String responseStr = novalnetApiService.followupSendRequest(novalnetEndpointConfigService.getTransactionRefundUrl(),
                data.toString(), baseStore);

        return new JSONObject(responseStr);
    }

    private NovalnetTransactionResult handleRefundSuccess(String tid, JSONObject response) {
        JSONObject transactionObject = response.getJSONObject(TRANSACTION);

        String transactionStatus = transactionObject.getString(STATUS);

        String nnOrderNo = transactionObject.getString(ORDER_NO);

        JSONObject refundObject = transactionObject.optJSONObject("refund");

        if (refundObject == null) {
            LOG.error("Refund object missing in Novalnet response for TID: {}", tid);

            return new NovalnetTransactionResult(false, "Refund response is invalid");
        }

        long newTid = refundObject.getLong("tid");
        int refundedAmount = refundObject.getInt(AMOUNT);

        BigDecimal formattedRefundAmount = BigDecimal.valueOf(refundedAmount).movePointLeft(CURRENCY_DECIMAL_PLACES);

        List<NovalnetPaymentInfoModel> paymentInfo = novalnetDao.getNovalnetPaymentInfo(nnOrderNo);

        String currency = transactionObject.optString("currency", "");

        String extensionComments = novalnetTransactionCommentService.buildRefundComments(tid, formattedRefundAmount.toPlainString(),
                currency, newTid);

        int orderAmount = transactionObject.getInt(AMOUNT);
        int totalRefundedAmount = refundObject.getInt(AMOUNT);

        String refundStatus = totalRefundedAmount >= orderAmount ? "REFUND" : "PARTIAL_REFUND";

        novalnetPaymentService.updatePaymentInfo(paymentInfo, refundStatus);

        novalnetCallbackOrderService.updateCallbackComments(extensionComments, nnOrderNo, refundStatus);

        return new NovalnetTransactionResult(true, extensionComments);
    }


}

