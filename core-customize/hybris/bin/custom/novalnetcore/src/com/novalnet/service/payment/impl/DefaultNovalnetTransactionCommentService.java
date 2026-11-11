package com.novalnet.service.payment.impl;

import com.novalnet.dto.OrderPaymentCommentsData;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.service.payment.NovalnetTransactionCommentService;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


/**
 * The type Default novalnet transaction comment service.
 */
public class DefaultNovalnetTransactionCommentService implements NovalnetTransactionCommentService {
    private static final String ON_HOLD = "ON_HOLD";
    private static final String STATUS = "status";
    private static final String DUE_DATE = "due_date";
    private static final String QR_IMAGE = "qr_image";
    private static final int CURRENCY_DECIMAL_PLACES = 2;

    @Resource(name = "novalnetPaymentService")
    private NovalnetPaymentService novalnetPaymentService;

    @Resource(name = "sessionService")
    private SessionService sessionService;

    @Override
    public OrderPaymentCommentsData buildOrderAndPaymentComments(final String currentPayment,
                                                                 final JSONObject transactionJsonObject, final CartData cartData) {
        final String orderComments = buildOrderComments(currentPayment, transactionJsonObject);

        final String bankDetails = buildBankDetails(currentPayment, transactionJsonObject, cartData);

        return new OrderPaymentCommentsData(orderComments, bankDetails);
    }

    private String buildOrderComments(final String currentPayment, final JSONObject transactionJsonObject) {
        final String paymentName = novalnetPaymentService.getPaymentName(currentPayment);

        String testMode = "";

        if ("1".equals(transactionJsonObject.get("test_mode").toString())) {
            testMode = " " + Localization.getLocalizedString("novalnet.testOrderText");
        }

        String orderComments = Localization.getLocalizedString("novalnet.paymentname") + ": " + paymentName + "<br>";

        orderComments += Localization.getLocalizedString("novalnet.transactionId") + " : " + transactionJsonObject.get("tid")
                + "<br>" + testMode + "<br>";

        final Boolean isZeroAmountBooking = sessionService.getAttribute("novalnetZeroAmountBooking");

        if (Boolean.TRUE.equals(isZeroAmountBooking)) {
            orderComments += "<br>" + Localization.getLocalizedString("novalnet.zeroAmountBooking");
        }

        sessionService.removeAttribute("novalnetZeroAmountBooking");

        return orderComments;
    }

    private String buildBankDetails(final String currentPayment, final JSONObject transactionJsonObject, final CartData cartData) {
        if ("novalnetInvoice".equals(currentPayment) || "novalnetPrepayment".equals(currentPayment)
                || "novalnetGuaranteedInvoice".equals(currentPayment)) {
            return buildInvoiceLikeBankDetails(currentPayment, transactionJsonObject, cartData);
        }

        if ("novalnetMultibanco".equals(currentPayment) && transactionJsonObject.has("partner_payment_reference")) {
            return buildMultibancoBankDetails(transactionJsonObject, cartData);
        }

        if ("novalnetGuaranteedDirectDebitSepa".equals(currentPayment)
                && "75".equals(transactionJsonObject.get("status_code").toString())) {
            return "<br>" + Localization.getLocalizedString("novalnet.sepa.status75");
        }

        return "";
    }

    private String buildInvoiceLikeBankDetails(final String currentPayment, final JSONObject transactionJsonObject,
                                               final CartData cartData) {
        final JSONObject bankdeatailsJsonObject = transactionJsonObject.getJSONObject("bank_details");

        String bankDetails = "<br>" + String.format(Localization.getLocalizedString("novalnet.bankDetailsComments1"),
                cartData.getTotalPriceWithTax().getFormattedValue());

        if (transactionJsonObject.has(DUE_DATE) && !ON_HOLD.equals(transactionJsonObject.get(STATUS).toString())) {
            final LocalDate localDate = LocalDate.parse(transactionJsonObject.get(DUE_DATE).toString());

            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

            final String formattedDate = localDate.format(formatter);

            bankDetails += " " + String.format(Localization.getLocalizedString("novalnet.bankDetailsComments2"), formattedDate);
        }

        bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsAccountHolder") + " "
                + bankdeatailsJsonObject.get("account_holder").toString();

        bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsIban") + " "
                + bankdeatailsJsonObject.get("iban").toString();

        bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsBic") + " "
                + bankdeatailsJsonObject.get("bic").toString();

        bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailsBank") + " "
                + bankdeatailsJsonObject.get("bank_name").toString();

        bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankPlace") + " "
                + bankdeatailsJsonObject.get("bank_place").toString();

        bankDetails += "<br>" + Localization.getLocalizedString("novalnet.bankDetailspaymentRefernceMulti") + "<br>"
                + Localization.getLocalizedString("novalnet.bankDetailsPaymentReference") + " : "
                + transactionJsonObject.get("tid").toString() + "<br>";

        bankDetails = appendQrCodeIfPresent(bankDetails, bankdeatailsJsonObject);

        if ("novalnetGuaranteedInvoice".equals(currentPayment) && "75".equals(transactionJsonObject.get("status_code").toString())) {
            bankDetails = "<br>" + Localization.getLocalizedString("novalnet.status75");
        }

        return bankDetails;
    }

    private String appendQrCodeIfPresent(final String bankDetails, final JSONObject bankdeatailsJsonObject) {
        String updatedBankDetails = bankDetails;

        if (bankdeatailsJsonObject.has(QR_IMAGE) && !bankdeatailsJsonObject.optString(QR_IMAGE).isEmpty()) {
            updatedBankDetails += "<br>";
            updatedBankDetails += Localization.getLocalizedString("novalnet.qrCodeComments");
            updatedBankDetails += "<br>";
            updatedBankDetails += "<img alt='nn_qr_code' src='" + bankdeatailsJsonObject.optString(QR_IMAGE) + "'>";
            updatedBankDetails += "<br>";
        }

        return updatedBankDetails;
    }

    private String buildMultibancoBankDetails(final JSONObject transactionJsonObject, final CartData cartData) {
        return "<br>" + Localization.getLocalizedString("novalnet.multibancocomments1") + " "
                + cartData.getTotalPriceWithTax().getFormattedValue()
                + Localization.getLocalizedString("novalnet.multibancocomments2") + "<br>"
                + Localization.getLocalizedString("novalnet.bankDetailsPaymentReference") + " : "
                + transactionJsonObject.get("partner_payment_reference").toString() + "<br>"
                + Localization.getLocalizedString("novalnet.multibancosupplierid") + " : "
                + transactionJsonObject.get("service_supplier_id").toString();
    }

    @Override
    public String buildCancelComments() {
        final String currentDate = NovalnetUtils.getCurrentDate();

        return Localization.getLocalizedString("novalnet.transaction.cancel") + " " + currentDate + " "
                + Localization.getLocalizedString("novalnet.cancel.confirm.suffix");
    }

    @Override
    public String buildCaptureComments(final JSONObject transaction) {
        final String currentDate = NovalnetUtils.getCurrentDate();
        final String paymentType = transaction.getString("payment_type");

        if ("GUARANTEED_INVOICE".equals(paymentType) || "INVOICE".equals(paymentType)) {
            final int amountInCents = transaction.getInt("amount");

            final String amount = BigDecimal.valueOf(amountInCents).movePointLeft(CURRENCY_DECIMAL_PLACES).toPlainString();

            final String dueDate = transaction.getString(DUE_DATE);

            return Localization.getLocalizedString("novalnet.invoice.capture.confirm") + " " + currentDate
                    + Localization.getLocalizedString("novalnet.invoice.capture.confirm.suffix") + " "
                    + Localization.getLocalizedString("novalnet.capture.transfer") + " " + amount + " "
                    + Localization.getLocalizedString("novalnet.capture.before") + " " + dueDate;
        }

        return Localization.getLocalizedString("novalnet.capture.confirm") + " " + currentDate;
    }

    @Override
    public String buildRefundComments(final String tid, final String formattedRefundAmount, final String currency,
                                      final long newTid) {
        String extensionComments = Localization.getLocalizedString("novalnet.transaction.refund") + " " + tid + " "
                + Localization.getLocalizedString("novalnet.with.amount1") + " " + formattedRefundAmount + " " + currency + ".";

        if (newTid > 0) {
            extensionComments += " " + Localization.getLocalizedString("novalnet.new.tid") + " " + newTid;
        }

        return extensionComments;
    }
}
