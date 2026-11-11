
/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.OrderPaymentCommentsData;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.Shipping;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.ui.Model;

import java.util.Map;


/**
 * Defines operations for processing Novalnet payment transactions and payment data.
 */
public interface NovalnetPaymentFacade {

    /**
     * Adds the Novalnet payment process data to the model.
     *
     * @param model              the Spring model
     * @param paymentDetailsForm the Novalnet payment details form
     * @param cartData           the current cart data
     * @throws CMSItemNotFoundException if the required CMS item cannot be found
     */
    void addPaymentProcess(final Model model, final NovalnetPaymentDetailsForm paymentDetailsForm, final CartData cartData)
            throws CMSItemNotFoundException;


    /**
     * Populates the customer address details for the payment process.
     *
     * @param model              the Spring model
     * @param paymentDetailsForm the Novalnet payment details form
     * @param cartData           the current cart data
     * @param customer           the customer data
     * @param addressData        the customer address data
     * @param billing            the billing
     * @param shipping           the shipping
     */
    void populateCustomerAddressDetails(Model model, NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData,
                                        Customer customer, AddressData addressData, Billing billing, Shipping shipping);

    /**
     * Processes one-click token data for the selected payment method.
     *
     * @param currentPayment     the current payment method
     * @param paymentDetailsForm the Novalnet payment details form
     * @param model              the Spring model
     * @param cartData           the current cart data
     * @param deliveryAddress    the delivery address
     * @return true if the one-click token data was processed successfully; otherwise false
     * @throws CMSItemNotFoundException if the required CMS item cannot be found
     */
    boolean processOneClickTokenData(String currentPayment, NovalnetPaymentDetailsForm paymentDetailsForm, Model model,
                                     CartData cartData, AddressData deliveryAddress) throws CMSItemNotFoundException;

    /**
     * Processes the Novalnet transaction result.
     *
     * @param resultMap the transaction result data
     * @return true if the transaction was processed successfully; otherwise false
     */
    boolean processTransaction(Map<String, String> resultMap);

    /**
     * Creates a Novalnet transaction request.
     *
     * @param request         the HTTP servlet request
     * @param baseStore       the current base store
     * @param currentPayment  the current payment method
     * @param customerNo      the customer number
     * @param orderAmountCent the order amount in cents
     * @param cartData        the current cart data
     * @return the generated transaction request
     */
    String createTransaction(HttpServletRequest request, BaseStoreModel baseStore, String currentPayment, String customerNo,
                             Integer orderAmountCent, CartData cartData);


    /**
     * Books a Novalnet wallet transaction.
     *
     * @param request         the HTTP servlet request
     * @param baseStore       the current base store
     * @param customerNo      the customer number
     * @param orderAmountCent the order amount in cents
     * @param cartData        the current cart data
     * @return the result of the wallet transaction booking
     *
     */
    String bookWalletTransaction(HttpServletRequest request, BaseStoreModel baseStore, String customerNo,
                                 Integer orderAmountCent, CartData cartData);


    /**
     * Retrieves the guest email address from the current checkout session.
     *
     * @return the guest email address
     */
    String getGuestEmail();

    /**
     * Checks whether the current user is a guest user.
     *
     * @return {@code true} if the current user is a guest; otherwise {@code false}
     */
    boolean isGuestUser();

    /**
     * Handles reference transaction information for the selected payment method.
     *
     * @param response       the transaction response
     * @param customerNo     the customer identifier
     * @param currentPayment the current payment method
     */
    void handleReferenceTransactionInfo(String response, String customerNo, String currentPayment);

    /**
     * Builds the order payment comments from the transaction response.
     *
     * @param currentPayment        the current payment method
     * @param transactionJsonObject the transaction details
     * @param cartData              the current cart data
     * @return the order payment comments data
     */
    OrderPaymentCommentsData buildOrderAndPaymentComments(String currentPayment, JSONObject transactionJsonObject,
                                                          CartData cartData);

    /**
     * Saves the order data after successful payment processing.
     *
     * @param orderComments   the order payment comments
     * @param currentPayment  the current payment method
     * @param status          the transaction status
     * @param orderAmountCent the order amount in cents
     * @param currency        the transaction currency
     * @param tid             the transaction identifier
     * @param email           the customer email address
     * @param addressData     the customer address data
     * @param bankDetails     the bank details
     * @return the saved order data
     * @throws InvalidCartException if the cart is invalid
     */
    OrderData saveOrderData(String orderComments, String currentPayment, String status, Integer orderAmountCent, String currency,
                            String tid, String email, AddressData addressData, String bankDetails) throws InvalidCartException;

    /**
     * Retrieves the payment mode code of the customer's last order.
     *
     * @return the last order payment mode code
     */
    String getLastOrderPaymentModeCode();

    /**
     * Sends a request to the specified Novalnet API endpoint.
     *
     * @param endpoint the Novalnet API endpoint
     * @param request  the JSON request payload
     * @return the Novalnet API response
     */
    String sendRequest(String endpoint, String request);

    /**
     * Returns the Novalnet transaction details API URL.
     *
     * @return the transaction details API URL
     */
    String getTransactionDetailsUrl();

    /**
     * Returns the Novalnet transaction update API URL.
     *
     * @return the transaction update API URL
     */
    String getTransactionUpdateUrl();

    /**
     * Removes the saved payment information for the specified payment type and card index.
     *
     * @param paymentType the type of payment method
     * @param cardIndex   the index of the saved payment information
     * @return true if the saved payment information is successfully removed; otherwise, false
     */
    boolean removeSavedPaymentData(String paymentType, String cardIndex);

}


