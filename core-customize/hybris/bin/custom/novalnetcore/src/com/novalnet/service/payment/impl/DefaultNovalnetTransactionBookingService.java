/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.service.payment.impl;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Custom;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.Merchant;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.dto.payment.PaymentData;
import com.novalnet.dto.payment.Transaction;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.order.NovalnetCallbackOrderService;
import com.novalnet.service.payment.NovalnetTransactionBookingService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.servicelayer.config.ConfigurationService;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.Locale;


/**
 * Default implementation of {@link NovalnetTransactionBookingService} for booking transactions.
 */
public class DefaultNovalnetTransactionBookingService implements NovalnetTransactionBookingService {
    private static final Logger LOG = Logger.getLogger(DefaultNovalnetTransactionBookingService.class);

    private static final int SUCCESS_STATUS_CODE = 100;
    private static final String GENDER_UNKNOWN = "u";
    private static final String CUSTOM_SHOP_INVOKED = "1";
    private static final String DEFAULT_LANG = "EN";
    private static final int CURRENCY_DECIMAL_PLACES = 2;


    @Resource
    private NovalnetApiService novalnetApiService;

    @Resource
    private ConfigurationService configurationService;

    @Resource
    private NovalnetDao novalnetDao;

    @Resource
    private NovalnetCallbackOrderService novalnetCallbackOrderService;

    /**
     * Gets payment type.
     *
     * @param paymentProvider the payment provider
     * @return the payment type
     */
    public static String getPaymentType(String paymentProvider) {
        switch (paymentProvider) {
            case "novalnetCreditCard":
                return "CREDITCARD";

            case "novalnetDirectDebitSepa":
                return "DIRECT_DEBIT_SEPA";

            case "novalnetGooglePay":
                return "GOOGLEPAY";

            case "novalnetApplePay":
                return "APPLEPAY";

            case "novalnetDirectDebitAch":
                return "DIRECT_DEBIT_ACH";

            default:
                return paymentProvider;
        }
    }

    @Override
    public void bookTransaction(OrderModel order) throws InterceptorException {
        try {
            BigDecimal bookAmount = BigDecimal.valueOf(order.getBookAmount());
            int amountCent = bookAmount.intValue();

            if (amountCent <= 0) {
                throw new InterceptorException("Invalid booking amount for order " + order.getCode());
            }

            NovalnetPaymentInfoModel paymentInfo = (NovalnetPaymentInfoModel) order.getPaymentInfo();

            String paymentType = getPaymentType(paymentInfo.getPaymentProvider());

            AddressModel billingAddress = resolveUsableBillingAddress(order);

            String paymentToken = getToken(order);

            if (paymentToken == null || paymentToken.isEmpty()) {
                throw new InterceptorException("No stored Novalnet payment token found on order " + order.getCode());
            }

            NovalnetPaymentRequest request = new NovalnetPaymentRequest();
            request.setMerchant(buildMerchant(order));
            request.setCustomer(buildCustomer(order, billingAddress));
            request.setTransaction(buildTransaction(order, paymentType, amountCent, paymentToken));
            request.setCustom(buildCustom(order));

            Gson gson = new GsonBuilder().create();
            String json = gson.toJson(request);

            String paymentApiUrl = configurationService.getConfiguration().getString("novalnet.payment.url");

            String response = novalnetApiService.bookTransactionAmount(paymentApiUrl, json, order.getStore());


            JSONObject responseJson = new JSONObject(response);
            JSONObject result = responseJson.getJSONObject("result");
            int statusCode = result.getInt("status_code");

            if (statusCode != SUCCESS_STATUS_CODE) {
                String statusText = result.has("status_text") ? result.getString("status_text")
                        : result.optString("status_desc", "Unknown error");

                throw new InterceptorException(
                        "Novalnet update booking failed for order " + order.getCode() + ". Reason: " + statusText);
            }

            JSONObject transactionResponse = responseJson.getJSONObject("transaction");

            String newTid = String.valueOf(transactionResponse.get("tid"));
            String amount = BigDecimal.valueOf(amountCent).movePointLeft(CURRENCY_DECIMAL_PLACES).toPlainString();

            String currency = order.getCurrency().getIsocode();

            String comments = String.format(Localization.getLocalizedString("novalnet.booking.success"), amount, currency, newTid);

            novalnetCallbackOrderService.updateCallbackComments(comments, order.getCode(), paymentInfo.getPaymentGatewayStatus());

            order.setPaidAmount(amountCent);
            order.setBookAmount(null);
        } catch (InterceptorException ie) {
            throw ie;
        } catch (Exception e) {
            throw new InterceptorException("Novalnet API call failed for order " + order.getCode(), e);
        }
    }

    private Merchant buildMerchant(OrderModel order) {
        Merchant merchant = new Merchant();
        merchant.setSignature(order.getStore().getNovalnetAPIKey());
        merchant.setTariff(order.getStore().getNovalnetTariffId().toString());

        return merchant;
    }

    private Customer buildCustomer(OrderModel order, AddressModel billingAddress) {
        Customer customer = new Customer();
        customer.setFirst_name(billingAddress.getFirstname());
        customer.setLast_name(billingAddress.getLastname());
        customer.setEmail(resolveEmail(order, billingAddress));
        customer.setCustomer_no(order.getUser().getPk().toString());
        customer.setGender(GENDER_UNKNOWN);
        customer.setBilling(buildBilling(billingAddress));

        return customer;
    }

    private Billing buildBilling(AddressModel billingAddress) {
        Billing billing = new Billing();

        if (billingAddress.getStreetname() != null) {
            String streetNumber = billingAddress.getStreetnumber() != null ? (" " + billingAddress.getStreetnumber()) : "";
            billing.setStreet(billingAddress.getStreetname() + streetNumber);
        }

        billing.setCity(billingAddress.getTown());
        billing.setZip(billingAddress.getPostalcode());

        if (billingAddress.getCountry() != null) {
            billing.setCountry_code(billingAddress.getCountry().getIsocode());
        }

        return billing;
    }

    private Transaction buildTransaction(OrderModel order, String paymentType, int amountCent, String paymentToken) {
        PaymentData paymentData = new PaymentData();
        paymentData.setToken(paymentToken);

        Transaction transaction = new Transaction();
        transaction.setPayment_type(paymentType);
        transaction.setAmount(Long.valueOf(amountCent));
        transaction.setCurrency(order.getCurrency().getIsocode());
        transaction.setOrder_no(order.getCode());
        transaction.setPayment_data(paymentData);

        return transaction;
    }

    private Custom buildCustom(OrderModel order) {
        Custom custom = new Custom();
        custom.setLang(getOrderLanguage(order));
        custom.setShop_invoked(CUSTOM_SHOP_INVOKED);

        return custom;
    }

    private AddressModel resolveUsableBillingAddress(OrderModel order) throws InterceptorException {
        AddressModel paymentAddress = order.getPaymentAddress();

        if (isUsableAddress(paymentAddress)) {
            return paymentAddress;
        }

        AddressModel deliveryAddress = order.getDeliveryAddress();

        if (isUsableAddress(deliveryAddress)) {
            LOG.warn("Falling back to delivery address for order " + order.getCode());

            return deliveryAddress;
        }

        AddressModel customerDefault = findUsableCustomerAddress(order);

        if (customerDefault != null) {
            LOG.warn("Falling back to a saved customer address for order " + order.getCode());

            return customerDefault;
        }

        throw new InterceptorException("No usable billing address found for order " + order.getCode()
                + " (payment, delivery, and saved addresses are all incomplete)");
    }

    private AddressModel findUsableCustomerAddress(OrderModel order) {
        if (order.getUser() instanceof CustomerModel) {
            CustomerModel customer = (CustomerModel) order.getUser();

            for (AddressModel candidate : customer.getAddresses()) {
                if (isUsableAddress(candidate)) {
                    return candidate;
                }
            }
        }

        return null;
    }

    private boolean isUsableAddress(AddressModel address) {
        if (address == null || address.getCountry() == null) {
            return false;
        }

        return isPersonalDetailsValid(address) && isLocationDetailsValid(address);
    }

    private boolean isPersonalDetailsValid(AddressModel address) {
        return isNotBlank(address.getFirstname()) && isNotBlank(address.getLastname());
    }

    private boolean isLocationDetailsValid(AddressModel address) {
        return isNotBlank(address.getTown()) && isNotBlank(address.getPostalcode());
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.isBlank();
    }

    private String resolveEmail(OrderModel order, AddressModel billingAddress) {
        if (billingAddress.getEmail() != null && !billingAddress.getEmail().isBlank()) {
            return billingAddress.getEmail();
        }

        if (order.getUser() != null && order.getUser().getUid() != null) {
            return order.getUser().getUid();
        }

        return null;
    }

    private String getToken(OrderModel order) {
        return novalnetDao.getStoredPaymentToken(order);
    }

    private String getOrderLanguage(OrderModel order) {
        LanguageModel language = order.getLanguage();

        if (language != null && language.getIsocode() != null && !language.getIsocode().isEmpty()) {
            return language.getIsocode().toUpperCase(Locale.ROOT);
        }

        LOG.warn("No language found on order " + order.getCode() + ", falling back to " + DEFAULT_LANG);

        return DEFAULT_LANG;
    }
}


