/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.exception.NovalnetException;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

import java.util.Map;


/**
 * Unified Novalnet payment method service (B2C + B2B).
 */
public interface NovalnetPaymentMethodService {
    /**
     * Adds the Novalnet payment process to the checkout model (B2C).
     *
     * @param model              the model
     * @param paymentDetailsForm the payment details form
     * @param cartData           the cart data
     * @throws CMSItemNotFoundException the cms item not found exception
     */
    void addPaymentProcess(Model model, NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData)
            throws CMSItemNotFoundException;

    /**
     * Processes the transaction result returned by Novalnet.
     *
     * @param resultMap the result map
     * @return the boolean
     */
    boolean processTransaction(Map<String, String> resultMap);

    /**
     * Creates a Novalnet transaction request (B2C).
     *
     * @param request         the request
     * @param baseStore       the base store
     * @param currentPayment  the current payment
     * @param customerNo      the customer no
     * @param orderAmountCent the order amount cent
     * @param cartData        the cart data
     * @return the string
     */
    String createTransaction(HttpServletRequest request, BaseStoreModel baseStore, String currentPayment, String customerNo,
                             Integer orderAmountCent, CartData cartData);

    /**
     * Books a wallet transaction with Novalnet.
     *
     * @param request         the request
     * @param baseStore       the base store
     * @param customerNo      the customer no
     * @param orderAmountCent the order amount cent
     * @param cartData        the cart data
     * @return the string
     * @throws NovalnetException the novalnet exception
     */
    String bookWalletTransaction(HttpServletRequest request, BaseStoreModel baseStore, String customerNo,
                                 Integer orderAmountCent, CartData cartData) throws NovalnetException;

    /**
     * Handles storing payment information based on the selected payment method and Novalnet response.
     *
     * @param currentPayment     the current payment
     * @param response           the response
     * @param customerJsonObject the customer json object
     */
    void handleStorePayment(String currentPayment, String response, JsonNode customerJsonObject);

    /**
     * Processes one-click token data for the selected payment method.
     *
     * @param currentPayment     the current payment
     * @param paymentDetailsForm the payment details form
     * @param model              the model
     * @param cartData           the cart data
     * @param deliveryAddress    the delivery address
     * @return the boolean
     * @throws CMSItemNotFoundException the cms item not found exception
     */
    boolean processOneClickTokenData(String currentPayment, NovalnetPaymentDetailsForm paymentDetailsForm, Model model,
                                     CartData cartData, AddressData deliveryAddress) throws CMSItemNotFoundException;

    /**
     * Handles guarantee payment validation for the selected payment method.
     *
     * @param paymentName        the payment name
     * @param dob                the dob
     * @param paymentDetailsForm the payment details form
     * @param deliveryAddress    the delivery address
     * @return the string
     */
    String handleGuaranteeProcess(String paymentName, String dob, NovalnetPaymentDetailsForm paymentDetailsForm,
                                  AddressData deliveryAddress);

}
