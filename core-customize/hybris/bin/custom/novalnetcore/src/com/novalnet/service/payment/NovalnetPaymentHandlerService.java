/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.payment;

import com.novalnet.dto.PaymentConfigResult;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.HostedPage;
import com.novalnet.dto.payment.PaymentData;
import com.novalnet.dto.payment.Transaction;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import jakarta.servlet.http.HttpServletRequest;


/**
 * Defines operations for handling Novalnet payment requests.
 */
public interface NovalnetPaymentHandlerService
{
	/**
	 * Handles a Novalnet payment request using the selected payment method and payment data.
	 *
	 * @param currentPayment the currently selected payment method
	 * @param paymentModeModel the SAP Commerce payment mode
	 * @param transaction the transaction details
	 * @param paymentData the payment-specific data
	 * @param customer the customer information
	 * @param orderAmountCent the order amount in cents
	 * @param request the HTTP servlet request
	 * @param hostedPage the hosted payment page configuration
	 * @return the result of payment configuration and processing
	 */
	 PaymentConfigResult handlePayment(String currentPayment, PaymentModeModel paymentModeModel, Transaction transaction,
			PaymentData paymentData, Customer customer, Integer orderAmountCent, HttpServletRequest request, HostedPage hostedPage);
}
