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


@FunctionalInterface
public interface PaymentConfigurator {
    void configure(PaymentModeModel paymentModeModel, Transaction transaction, PaymentData paymentData, Customer customer,
                   Integer orderAmountCent, HttpServletRequest request, HostedPage hostedPage, PaymentConfigResult result);
}
