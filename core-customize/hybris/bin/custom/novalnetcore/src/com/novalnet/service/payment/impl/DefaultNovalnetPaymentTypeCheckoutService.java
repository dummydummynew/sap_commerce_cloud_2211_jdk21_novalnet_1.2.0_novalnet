/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dto.NovalnetPaymentTypeForm;
import com.novalnet.model.*;
import com.novalnet.service.payment.NovalnetPaymentTypeCheckoutService;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.b2bcommercefacades.company.B2BCostCenterFacade;
import de.hybris.platform.b2bcommercefacades.company.data.B2BCostCenterData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * The type Default novalnet payment type checkout service.
 */
@Service
public class DefaultNovalnetPaymentTypeCheckoutService implements NovalnetPaymentTypeCheckoutService {

    @Resource
    private SessionService sessionService;
    @Resource
    private B2BCostCenterFacade costCenterFacade;
    @Resource
    private PaymentModeService paymentModeService;

    /**
     * Gets visible active cost centers.
     *
     * @return the visible active cost centers
     */
    @ModelAttribute("costCenters")
    public List<? extends B2BCostCenterData> getVisibleActiveCostCenters() {
        final List<? extends B2BCostCenterData> costCenterData = costCenterFacade.getActiveCostCenters();
        return costCenterData == null ? Collections.<B2BCostCenterData>emptyList() : costCenterData;
    }

    public CartData updateCheckoutCart(final NovalnetPaymentTypeForm paymentTypeForm) {
        final CartData cartData = new CartData();

        // set payment type
        final B2BPaymentTypeData paymentTypeData = new B2BPaymentTypeData();
        paymentTypeData.setCode(paymentTypeForm.getPaymentType());
        cartData.setPaymentType(paymentTypeData);

        if (CheckoutPaymentType.ACCOUNT.getCode().equals(cartData.getPaymentType().getCode())) {
            final B2BCostCenterData costCenter = new B2BCostCenterData();
            costCenter.setCode(paymentTypeForm.getCostCenterId());

            cartData.setCostCenter(costCenter);
            sessionService.setAttribute("selectedPaymentMethodId", CheckoutPaymentType.ACCOUNT.getCode());
        }

        // set purchase order number
        sessionService.setAttribute("selectedPaymentMethodId", paymentTypeData.getCode());
        cartData.setPurchaseOrderNumber(paymentTypeForm.getPurchaseOrderNumber());

        return cartData;
    }

    public NovalnetPaymentTypeForm preparePaymentTypeForm(final CartData cartData) {
        final NovalnetPaymentTypeForm paymentTypeForm = new NovalnetPaymentTypeForm();

        // set payment type
        if (cartData.getPaymentType() != null && StringUtils.isNotBlank(cartData.getPaymentType().getCode())) {
            paymentTypeForm.setPaymentType(cartData.getPaymentType().getCode());
        } else {
            paymentTypeForm.setPaymentType(CheckoutPaymentType.ACCOUNT.getCode());
        }

        if (CheckoutPaymentType.ACCOUNT.equals(cartData.getPaymentType().getCode())) {
            // set cost center
            if (cartData.getCostCenter() != null && StringUtils.isNotBlank(cartData.getCostCenter().getCode())) {
                paymentTypeForm.setCostCenterId(cartData.getCostCenter().getCode());
            } else if (!CollectionUtils.isEmpty(getVisibleActiveCostCenters()) && getVisibleActiveCostCenters().size() == 1) {
                paymentTypeForm.setCostCenterId(getVisibleActiveCostCenters().get(0).getCode());
            }
        }

        paymentTypeForm.setPurchaseOrderNumber(cartData.getPurchaseOrderNumber());
        return paymentTypeForm;
    }

    @Override
    public Boolean paymentIsActive(String paymentType) {
        switch (paymentType) {
            case "NOVALNET_WALLET": {
                List<Boolean> paymentModes = handleNovalnetWallet();
                return paymentModes.stream().anyMatch(Boolean::booleanValue);
            }
            case "NOVALNET_ONLINE_TRANSFER": {
                List<Boolean> paymentModes = handleNovalnetOnlineTransfer();
                return paymentModes.stream().anyMatch(Boolean::booleanValue);
            }
            case "NOVALNET_INVOICE": {
                List<Boolean> paymentModes = handleNovalnetInvoice();
                return paymentModes.stream().anyMatch(Boolean::booleanValue);

            }
            case "NOVALNET_DIRECT_DEBIT": {
                List<Boolean> paymentModes = handleNovalnetDirectDebit();
                return paymentModes.stream().anyMatch(Boolean::booleanValue);
            }
            case "CARD": {
                NovalnetCreditCardPaymentModeModel novalnetCreditCardPaymentMethod = (NovalnetCreditCardPaymentModeModel) paymentModeService
                        .getPaymentModeForCode("novalnetCreditCard");
                return novalnetCreditCardPaymentMethod.getActive();
            }
            default: {
                return false;
            }
        }
    }

    private List<Boolean> handleNovalnetDirectDebit() {
        NovalnetDirectDebitSepaPaymentModeModel novalnetDirectDebitSepaPaymentMethod = (NovalnetDirectDebitSepaPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetDirectDebitSepa");
        NovalnetGuaranteedDirectDebitSepaPaymentModeModel novalnetGuaranteedDirectDebitSepaPaymentMethod = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetGuaranteedDirectDebitSepa");
        NovalnetDirectDebitAchPaymentModeModel novalnetDirectDebitAchPaymentMethod = (NovalnetDirectDebitAchPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetDirectDebitAch");
        return Arrays.asList(novalnetDirectDebitSepaPaymentMethod.getActive(),
                novalnetGuaranteedDirectDebitSepaPaymentMethod.getActive(), novalnetDirectDebitAchPaymentMethod.getActive());
    }

    private List<Boolean> handleNovalnetInvoice() {
        NovalnetInvoicePaymentModeModel novalnetInvoicePaymentMethod = (NovalnetInvoicePaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetInvoice");
        NovalnetPrepaymentPaymentModeModel novalnetPrepaymentPaymentMethod = (NovalnetPrepaymentPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetPrepayment");
        NovalnetMultibancoPaymentModeModel novalnetMultibancoPaymentMethod = (NovalnetMultibancoPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetMultibanco");
        NovalnetGuaranteedInvoicePaymentModeModel novalnetGuaranteedInvoicePaymentMethod = (NovalnetGuaranteedInvoicePaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetGuaranteedInvoice");

        return Arrays.asList(novalnetInvoicePaymentMethod.getActive(), novalnetGuaranteedInvoicePaymentMethod.getActive(),
                novalnetPrepaymentPaymentMethod.getActive(), novalnetMultibancoPaymentMethod.getActive());
    }

    private List<Boolean> handleNovalnetOnlineTransfer() {
        NovalnetBlikPaymentModeModel novalnetBlikPaymentModeModel = (NovalnetBlikPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetBlik");
        NovalnetTrustlyPaymentModeModel novalnetTrustlyPaymentModeModel = (NovalnetTrustlyPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetTrustly");
        NovalnetIdealPaymentModeModel novalnetIdealPaymentMethod = (NovalnetIdealPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetIdeal");
        NovalnetEpsPaymentModeModel novalnetEpsPaymentMethod = (NovalnetEpsPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetEps");
        NovalnetPrzelewy24PaymentModeModel novalnetPrzelewy24PaymentMethod = (NovalnetPrzelewy24PaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetPrzelewy24");
        NovalnetOnlineBankTransferPaymentModeModel novalnetOnlineBankTransferPaymentMethod = (NovalnetOnlineBankTransferPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetOnlineBankTransfer");
        NovalnetBancontactPaymentModeModel novalnetBancontactPaymentMethod = (NovalnetBancontactPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetBancontact");
        NovalnetPostFinancePaymentModeModel novalnetPostFinancePaymentMethod = (NovalnetPostFinancePaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetPostFinance");
        NovalnetPostFinanceCardPaymentModeModel novalnetPostFinanceCardPaymentMethod = (NovalnetPostFinanceCardPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetPostFinanceCard");

        return Arrays.asList(novalnetIdealPaymentMethod.getActive(), novalnetEpsPaymentMethod.getActive(),
                novalnetPrzelewy24PaymentMethod.getActive(), novalnetOnlineBankTransferPaymentMethod.getActive(),
                novalnetBancontactPaymentMethod.getActive(), novalnetPostFinancePaymentMethod.getActive(),
                novalnetPostFinanceCardPaymentMethod.getActive(), novalnetBlikPaymentModeModel.getActive(),
                novalnetTrustlyPaymentModeModel.getActive());
    }

    private List<Boolean> handleNovalnetWallet() {

        NovalnetWeChatPayPaymentModeModel paymentNovalnetWechatPayModeModel = (NovalnetWeChatPayPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetWeChatPay");
        NovalnetMbWayPaymentModeModel novalnetMbWayPaymentModeModel = (NovalnetMbWayPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetMbWay");
        NovalnetPayPalPaymentModeModel novalnetPayPalPaymentMethod = (NovalnetPayPalPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetPayPal");
        NovalnetGooglePayPaymentModeModel novalnetGooglePayPaymentMethod = (NovalnetGooglePayPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetGooglePay");
        NovalnetTwintPaymentModeModel novalnetTwintPaymentMethod = (NovalnetTwintPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetTwint");
        NovalnetApplePayPaymentModeModel novalnetApplePayPaymentMethod = (NovalnetApplePayPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetApplePay");
        NovalnetAliPayPaymentModeModel novalnetAliPayPaymentModeModel = (NovalnetAliPayPaymentModeModel) paymentModeService
                .getPaymentModeForCode("novalnetAlipay");
        return Arrays.asList(novalnetPayPalPaymentMethod.getActive(), novalnetGooglePayPaymentMethod.getActive(),
                novalnetTwintPaymentMethod.getActive(), novalnetApplePayPaymentMethod.getActive(),
                paymentNovalnetWechatPayModeModel.getActive(), novalnetMbWayPaymentModeModel.getActive(),
                novalnetAliPayPaymentModeModel.getActive());

    }
}
