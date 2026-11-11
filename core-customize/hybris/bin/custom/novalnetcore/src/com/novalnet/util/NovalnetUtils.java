/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.util;

import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.store.BaseStoreModel;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.exception.NovalnetException;

import de.novalnet.beans.NnCallbackEventData;
import de.novalnet.beans.NnCallbackRequestData;
import de.novalnet.beans.NnCallbackResultData;
import de.novalnet.beans.NnCallbackTransactionData;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.bind.DatatypeConverter;


/**
 * Provides utility methods for Novalnet payment processing and callback handling.
 */
public final class NovalnetUtils {
    public static final int AGE_REQUIREMENT = 18;
    public static final String NOVALNET_GOOGLE_PAY = "novalnetGooglePay";
    public static final String NOVALNET_APPLE_PAY = "novalnetApplePay";
    public static final String NOVALNET_CREDIT_CARD = "novalnetCreditCard";
    public static final String NOVALNET_DIRECT_DEBIT_SEPA = "novalnetDirectDebitSepa";
    public static final String NOVALNET_DIRECT_DEBIT_ACH = "novalnetDirectDebitAch";
    public static final String NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA = "novalnetGuaranteedDirectDebitSepa";
    public static final String NOVALNET_INVOICE = "novalnetInvoice";
    public static final String NOVALNET_GUARANTEED_INVOICE = "novalnetGuaranteedInvoice";
    public static final String NOVALNET_PREPAYMENT = "novalnetPrepayment";
    public static final String NOVALNET_PAYPAL = "novalnetPayPal";
    public static final String NOVALNET_ONLINE_BANK_TRANSFER = "novalnetOnlineBankTransfer";
    public static final String NOVALNET_BANCONTACT = "novalnetBancontact";
    public static final String NOVALNET_MULTIBANCO = "novalnetMultibanco";
    public static final String NOVALNET_IDEAL = "novalnetIdeal";
    public static final String NOVALNET_TWINT = "novalnetTwint";
    public static final String NOVALNET_MB_WAY = "novalnetMbWay";
    public static final String NOVALNET_ALIPAY = "novalnetAlipay";
    public static final String NOVALNET_TRUSTLY = "novalnetTrustly";
    public static final String NOVALNET_BLIK = "novalnetBlik";
    public static final String NOVALNET_WECHAT_PAY = "novalnetWeChatPay";
    public static final String NOVALNET_EPS = "novalnetEps";
    private static final String AUTHORIZE_WITH_ZERO_AMOUNT = "AUTHORIZE_WITH_ZERO_AMOUNT";
    private static final Logger LOGGER = Logger.getLogger(NovalnetUtils.class);
    private static final String SHA_256 = "SHA-256";
    private static final int KEY_VALUE_PAIR_STEP = 2;
    private static final String LOCALHOST_IP = "127.0.0.1";
    private static final String NOVALNET_PAY_PAL = "novalnetPayPal";
    private static final String NOVALNET_WE_CHAT_PAY = "novalnetWeChatPay";
    private static final String NOVALNET_PRZELEWY_24 = "novalnetPrzelewy24";
    private static final String NOVALNET_POST_FINANCE_CARD = "novalnetPostFinanceCard";
    private static final String NOVALNET_POST_FINANCE = "novalnetPostFinance";

    private NovalnetUtils() {
    }

    /**
     * Retrieves the language code associated with the specified order.
     *
     * @param order the order from which the language code is retrieved
     * @return the uppercase language code, or {@code EN} if no language is configured
     */
    public static String getLanguageCode(final OrderModel order) {
        if (order != null && order.getLanguage() != null) {
            return order.getLanguage().getIsocode().toUpperCase();
        }

        return "EN";
    }

    /**
     * Checks whether the specified string contains a non-empty value.
     *
     * @param val the string to validate
     * @return {@code true} if the value is not null and contains non-whitespace characters; {@code false} otherwise
     */
    public static boolean isPopulated(final String val) {
        return val != null && !val.trim().isEmpty();
    }

    /**
     * Formats the specified amount by converting a comma decimal separator to a dot decimal separator.
     *
     * @param amount the amount to format
     * @return the formatted amount
     */
    public static String formatAmount(String amount) {
        if (amount.contains(",")) {
            try {
                final NumberFormat formattedAmount = NumberFormat.getNumberInstance(Locale.GERMANY);
                final double formattedValue = formattedAmount.parse(amount).doubleValue();

                amount = Double.toString(formattedValue);
            } catch (final ParseException e) {
                amount = amount.replace(",", ".");
            }
        }

        return amount;
    }

    /**
     * Checks whether a person has reached the required age based on the specified date of birth.
     *
     * @param dateInString the date of birth in string format
     * @return {@code true} if the person meets the required age; {@code false} otherwise
     */
    public static boolean hasAgeRequirement(final String dateInString) {
        try {
            final LocalDate birthDate = LocalDate.parse(dateInString, DateTimeFormatter.ISO_LOCAL_DATE);

            final LocalDate currentDate = LocalDate.now();

            return Period.between(birthDate, currentDate).getYears() >= AGE_REQUIREMENT;
        } catch (final java.time.format.DateTimeParseException ex) {
            return false;
        }
    }

    /**
     * Checks whether the specified action represents a zero-amount booking.
     *
     * @param actionType the action type to evaluate
     * @return {@code true} if the action represents a zero-amount booking; {@code false} otherwise
     */
    public static boolean isZeroAmountBooking(final Object actionType) {
        return actionType != null && AUTHORIZE_WITH_ZERO_AMOUNT.equals(actionType.toString());
    }

    /**
     * Retrieves the Novalnet payment type corresponding to the specified payment name.
     *
     * @param paymentName the Novalnet payment method name
     * @return the corresponding Novalnet payment type, or {@code null} if no mapping exists
     */
    public static String getPaymentType(final String paymentName) {
        final Map<String, String> paymentType = new HashMap<>();

        paymentType.put(NOVALNET_CREDIT_CARD, "CREDITCARD");
        paymentType.put(NOVALNET_DIRECT_DEBIT_SEPA, "DIRECT_DEBIT_SEPA");
        paymentType.put(NOVALNET_DIRECT_DEBIT_ACH, "DIRECT_DEBIT_ACH");
        paymentType.put(NOVALNET_GUARANTEED_DIRECT_DEBIT_SEPA, "GUARANTEED_DIRECT_DEBIT_SEPA");
        paymentType.put(NOVALNET_INVOICE, "INVOICE");
        paymentType.put(NOVALNET_GUARANTEED_INVOICE, "GUARANTEED_INVOICE");
        paymentType.put(NOVALNET_PREPAYMENT, "PREPAYMENT");
        paymentType.put(NOVALNET_PAY_PAL, "PAYPAL");
        paymentType.put(NOVALNET_ONLINE_BANK_TRANSFER, "ONLINE_BANK_TRANSFER");
        paymentType.put(NOVALNET_BANCONTACT, "BANCONTACT");
        paymentType.put(NOVALNET_MULTIBANCO, "MULTIBANCO");
        paymentType.put(NOVALNET_IDEAL, "IDEAL");
        paymentType.put(NOVALNET_GOOGLE_PAY, "GOOGLEPAY");
        paymentType.put(NOVALNET_APPLE_PAY, "APPLEPAY");
        paymentType.put(NOVALNET_TWINT, "TWINT");
        paymentType.put(NOVALNET_MB_WAY, "MBWAY");
        paymentType.put(NOVALNET_ALIPAY, "ALIPAY");
        paymentType.put(NOVALNET_TRUSTLY, "TRUSTLY");
        paymentType.put(NOVALNET_BLIK, "BLIK");
        paymentType.put(NOVALNET_WE_CHAT_PAY, "WECHATPAY");
        paymentType.put(NOVALNET_EPS, "EPS");
        paymentType.put(NOVALNET_PRZELEWY_24, "PRZELEWY24");
        paymentType.put(NOVALNET_POST_FINANCE_CARD, "POSTFINANCE_CARD");
        paymentType.put(NOVALNET_POST_FINANCE, "POSTFINANCE");

        return paymentType.get(paymentName);
    }

    /**
     * Formats a date by adding the specified number of days to the current date.
     *
     * @param date the number of days to add to the current date
     * @return the resulting date formatted as {@code dd.MM.yyyy}
     */
    public static String formatDate(final int date) {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        return LocalDate.now().plusDays(date).format(dateFormatter);
    }

    /**
     * Retrieves the remote IP address from the specified HTTP request.
     *
     * @param request the HTTP request containing the remote address
     * @return the remote IP address, or the localhost IP address if the address cannot be resolved
     */
    public static String getRemoteIpAddr(final HttpServletRequest request) {
        try {
            final InetAddress ipAddr = InetAddress.getByName(request.getRemoteAddr());

            if (ipAddr instanceof Inet4Address) {
                return ipAddr.getHostAddress();
            } else if (ipAddr instanceof Inet6Address) {
                return LOCALHOST_IP;
            }
        } catch (final UnknownHostException ex) {
            LOGGER.error("UnknownHostException ", ex);
        }

        return LOCALHOST_IP;
    }

    /**
     * Generates a SHA-256 checksum for the specified token string.
     *
     * @param tokenString the string used to generate the checksum
     * @return the generated hexadecimal SHA-256 checksum
     */
    public static String generateChecksum(final String tokenString) {
        String checksum = "";

        try {
            final MessageDigest digest = MessageDigest.getInstance(SHA_256);
            final byte[] hash = digest.digest(tokenString.getBytes(StandardCharsets.UTF_8));
            final StringBuilder hexString = new StringBuilder();

            for (int i = 0; i < hash.length; i++) {
                final String hex = Integer.toHexString(0xff & hash[i]);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            checksum = hexString.toString();
        } catch (final RuntimeException ex) {
            LOGGER.error("RuntimeException" + ex);
        } catch (final NoSuchAlgorithmException ex) {
            LOGGER.error("UnsupportedEncodingException" + ex);
        }

        return checksum;
    }

    /**
     * Finds a title matching the specified code.
     *
     * @param titles the list of available titles
     * @param code   the code of the title to find
     * @return the matching title, or {@code null} if no matching title exists
     */
    public static TitleData findTitleForCode(final List<TitleData> titles, final String code) {
        if (code != null && !code.isEmpty() && titles != null && !titles.isEmpty()) {
            for (final TitleData title : titles) {
                if (code.equals(title.getCode())) {
                    return title;
                }
            }
        }

        return null;
    }

    /**
     * Resolves the current session language and converts it to the Novalnet language format.
     *
     * @return the uppercase language code, or {@code EN} when no session language is available
     */
    public static String resolveLanguageCode() {
        Locale language = JaloSession.getCurrentSession().getSessionContext().getLocale();
        return language != null ? language.toString().toUpperCase(Locale.ENGLISH) : "EN";
    }

    /**
     * Retrieves the current date and time using the locale of the current session.
     *
     * @return the current date and time formatted according to the current session locale
     */
    public static String getCurrentDate() {
        final Locale locale = JaloSession.getCurrentSession().getSessionContext().getLocale();

        final String pattern = Locale.GERMAN.getLanguage().equals(locale.getLanguage()) ? "dd-MM-yyyy 'um' HH:mm:ss"
                : "dd-MM-yyyy, HH:mm:ss";

        return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern, locale));
    }

    /**
     * Validates that a callback request originates from the expected Novalnet IP address.
     *
     * @param request   the HTTP request containing the callback source address
     * @param baseStore the base store containing the Novalnet configuration
     */
    public static void validateCallbackIp(final HttpServletRequest request, final BaseStoreModel baseStore) {
        final boolean testMode = Boolean.TRUE.equals(baseStore.getNovalnetVendorscriptTestMode());

        final String callerIp = extractClientIp(request);
        final String novalnetIp = resolveNovalnetIp();

        if (!testMode && !novalnetIp.equals(callerIp)) {
            throw new IllegalStateException("Unauthorized callback source IP: " + callerIp);
        }
    }

    /**
     * Validates that all mandatory callback parameters are present.
     *
     * @param request the callback request containing the parameters to validate
     */
    public static void validateMandatoryParams(final NnCallbackRequestData request) {
        if (request.getEvent() == null || request.getTransaction() == null || request.getResult() == null) {
            throw new SystemException("Mandatory callback parameters missing");
        }
    }

    /**
     * Validates the checksum of a Novalnet callback request using the configured payment access key.
     *
     * @param request   the callback request containing the checksum and transaction details
     * @param baseStore the base store containing the Novalnet payment access key
     */
    public static void validateCallbackChecksum(final NnCallbackRequestData request, final BaseStoreModel baseStore) {
        final String accessKey = baseStore.getNovalnetPaymentAccessKey();

        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalStateException("Novalnet payment access key is not configured");
        }

        final String calculatedChecksum = generateChecksum(request, accessKey);

        final String receivedChecksum = request.getEvent().getChecksum();

        if (!calculatedChecksum.equals(receivedChecksum)) {
            throw new IllegalStateException("Checksum validation failed. Callback payload may be tampered");
        }
    }

    public static String extractClientIp(final HttpServletRequest request) {
        final String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private static String resolveNovalnetIp() {
        try {
            return InetAddress.getByName("pay-nn.de").getHostAddress();
        } catch (final UnknownHostException ex) {
            throw new IllegalStateException("Failed to resolve Novalnet host IP", ex);
        }
    }

    private static String generateChecksum(final NnCallbackRequestData request, final String accessKey) {
        final NnCallbackEventData event = request.getEvent();
        final NnCallbackTransactionData txn = request.getTransaction();
        final NnCallbackResultData result = request.getResult();

        final StringBuilder token = new StringBuilder().append(event.getTid()).append(event.getType()).append(result.getStatus());

        if (txn.getAmount() != null) {
            token.append(txn.getAmount());
        }

        if (txn.getCurrency() != null) {
            token.append(txn.getCurrency());
        }

        token.append(new StringBuilder(accessKey.trim()).reverse());

        try {
            final MessageDigest digest = MessageDigest.getInstance(SHA_256);
            final byte[] hash = digest.digest(token.toString().getBytes(StandardCharsets.UTF_8));

            return toHex(hash);
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Failed to generate checksum", ex);
        }
    }

    public static boolean isWalletPayment(String paymentModeCode) {
        return NOVALNET_GOOGLE_PAY.equals(paymentModeCode) || NOVALNET_APPLE_PAY.equals(paymentModeCode);
    }

    private static String toHex(final byte[] bytes) {
        final StringBuilder hex = new StringBuilder(bytes.length * 2);

        for (final byte b : bytes) {
            hex.append(String.format("%02x", b));
        }

        return hex.toString();
    }

    public static String convetToString(NovalnetPaymentRequest novalnetPaymentRequest) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonBody;
        try {
            jsonBody = mapper.writeValueAsString(novalnetPaymentRequest);
        } catch (JsonProcessingException e) {
            throw new NovalnetException("Unable to parse json response", e);
        }
        return jsonBody;
    }

    /**
     * Encodes the specified input value using Base64 encoding.
     *
     * @param input the value to encode
     * @return the Base64-encoded value, or the exception message if encoding fails
     */
    public String getEncodedValue(final String input) {
        try {
            final byte[] data = input.getBytes(StandardCharsets.UTF_8);
            return DatatypeConverter.printBase64Binary(data);
        } catch (final Exception e) {
            return e.getMessage();
        }
    }

    /**
     * Retrieves the IP address of the server.
     *
     * @return the server IP address, or the localhost IP address if the address cannot be resolved
     */
    public String getServerIpAddr() {
        try {
            final InetAddress ipAddr = InetAddress.getLocalHost();

            if (ipAddr instanceof Inet4Address) {
                return ipAddr.getHostAddress();
            } else if (ipAddr instanceof Inet6Address) {
                return LOCALHOST_IP;
            }
        } catch (final UnknownHostException ex) {
            LOGGER.error("UnknownHostException ", ex);
        }

        return LOCALHOST_IP;
    }
}
