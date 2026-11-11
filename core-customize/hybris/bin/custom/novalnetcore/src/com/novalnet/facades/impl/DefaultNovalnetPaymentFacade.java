package com.novalnet.facades.impl;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.OrderPaymentCommentsData;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.facades.NovalnetPaymentFacade;
import com.novalnet.service.checkout.NovalnetCheckoutService;
import com.novalnet.service.http.NovalnetApiService;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetAddressService;
import com.novalnet.service.payment.NovalnetPaymentMethodService;
import com.novalnet.service.payment.NovalnetPaymentService;
import com.novalnet.service.payment.NovalnetTransactionCommentService;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.store.BaseStoreModel;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.json.JSONObject;
import org.springframework.ui.Model;

import java.util.Map;


/**
 * Default implementation of {@link NovalnetPaymentFacade}.
 * <p>
 * Delegates Novalnet payment processing operations to the {@link NovalnetPaymentMethodService}.
 */

public class DefaultNovalnetPaymentFacade implements NovalnetPaymentFacade {
    @Resource
    private NovalnetPaymentMethodService novalnetPaymentMethodService;

    @Resource
    private NovalnetCheckoutService novalnetCheckoutService;

    @Resource
    private NovalnetPaymentService novalnetPaymentService;


    @Resource
    private NovalnetApiService novalnetApiService;

    @Resource
    private NovalnetAddressService novalnetAddressService;

    @Resource
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Resource(name = "novalnetTransactionCommentService")
    private NovalnetTransactionCommentService novalnetTransactionCommentService;

    @Override
    public void addPaymentProcess(Model model, NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData)
            throws CMSItemNotFoundException {
        novalnetPaymentMethodService.addPaymentProcess(model, paymentDetailsForm, cartData);
    }

    @Override
    public void populateCustomerAddressDetails(Model model, NovalnetPaymentDetailsForm paymentDetailsForm, CartData cartData,
                                               Customer customer, AddressData addressData, Billing billing, Shipping shipping) {
        novalnetAddressService.populateCustomerAddressDetails(paymentDetailsForm, cartData, customer, addressData, billing, shipping);
    }

    @Override
    public boolean processOneClickTokenData(String currentPayment, NovalnetPaymentDetailsForm paymentDetailsForm, Model model,
                                            CartData cartData, AddressData deliveryAddress) throws CMSItemNotFoundException {
        return novalnetPaymentMethodService.processOneClickTokenData(currentPayment, paymentDetailsForm, model, cartData,
                deliveryAddress);
    }

    @Override
    public boolean processTransaction(Map<String, String> resultMap) {
        return novalnetPaymentMethodService.processTransaction(resultMap);
    }

    @Override
    public String createTransaction(HttpServletRequest request, BaseStoreModel baseStore, String currentPayment, String customerNo,
                                    Integer orderAmountCent, CartData cartData) {
        return novalnetPaymentMethodService.createTransaction(request, baseStore, currentPayment, customerNo, orderAmountCent,
                cartData);
    }

    @Override
    public String bookWalletTransaction(HttpServletRequest request, BaseStoreModel baseStore, String customerNo,
                                        Integer orderAmountCent, CartData cartData) {
        return novalnetPaymentMethodService.bookWalletTransaction(request, baseStore, customerNo, orderAmountCent, cartData);
    }

    @Override
    public String getGuestEmail() {
        return novalnetCheckoutService.getUserEmail();
    }

    @Override
    public boolean isGuestUser() {
        return novalnetCheckoutService.isGuestUser();
    }


    @Override
    public void handleReferenceTransactionInfo(final String response, final String customerNo, final String currentPayment) {
        novalnetPaymentService.handleReferenceTransactionInfo(response, customerNo, currentPayment);
    }

    @Override
    public OrderPaymentCommentsData buildOrderAndPaymentComments(final String currentPayment, final JSONObject transactionJsonObject, final CartData cartData) {
        return novalnetTransactionCommentService.buildOrderAndPaymentComments(currentPayment, transactionJsonObject, cartData);
    }

    @Override
    public OrderData saveOrderData(final String orderComments, final String currentPayment, final String status,
                                   final Integer orderAmountCent, final String currency, final String tid, final String email,
                                   final AddressData addressData, final String bankDetails) throws InvalidCartException {
        return novalnetCheckoutService.saveOrderData(orderComments, currentPayment, status, orderAmountCent, currency, tid, email,
                addressData, bankDetails);
    }

    @Override
    public String getLastOrderPaymentModeCode() {
        return novalnetCheckoutService.getLastOrderPaymentModeCode();
    }

    @Override
    public String sendRequest(final String endpoint, final String request) {
        return novalnetApiService.sendRequest(endpoint, request);
    }

    @Override
    public String getTransactionDetailsUrl() {
        return novalnetEndpointConfigService.getTransactionDetailsUrl();
    }

    @Override
    public String getTransactionUpdateUrl() {
        return novalnetEndpointConfigService.getTransactionUpdateUrl();
    }

    @Override
    public boolean removeSavedPaymentData(final String paymentType, final String cardIndex) {
        return novalnetPaymentService.removeSavedPaymentData(paymentType, cardIndex);
    }
}
