/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.facades.populator;

import com.novalnet.facades.NovalnetPaymentTypeFacade;
import de.hybris.platform.b2b.enums.CheckoutPaymentType;
import de.hybris.platform.b2bacceleratorfacades.order.data.B2BPaymentTypeData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.type.TypeService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.util.localization.Localization;
import jakarta.annotation.Resource;


public class NovalnetB2BPaymentTypePopulator implements Populator<CheckoutPaymentType, B2BPaymentTypeData> {


    @Resource
    private BaseStoreService baseStoreService;
    @Resource
    private NovalnetPaymentTypeFacade novalnetPaymentTypeFacade;
    @Resource
    private TypeService typeService;

    @Override
    public void populate(CheckoutPaymentType source, B2BPaymentTypeData target) throws ConversionException {
        CheckoutPaymentType checkoutPaymentTypeValue = source;
        final BaseStoreModel baseStore = baseStoreService.getCurrentBaseStore();

        final Integer tariff = baseStore.getNovalnetTariffId();
        final String apiKey = baseStore.getNovalnetAPIKey();
        final String paymentAccessKey = baseStore.getNovalnetPaymentAccessKey();

        if (checkoutPaymentTypeValue == null) {
            checkoutPaymentTypeValue = CheckoutPaymentType.ACCOUNT;
        }

        if ("ACCOUNT".equals(checkoutPaymentTypeValue.getCode())) {
            target.setCode(checkoutPaymentTypeValue.getCode());
            target.setDisplayName(typeService.getEnumerationValue(checkoutPaymentTypeValue).getName());
        }

        if (tariff != null && !apiKey.isEmpty() && !paymentAccessKey.isEmpty() && novalnetPaymentTypeFacade.paymentIsActive(checkoutPaymentTypeValue.getCode())) {
            switch (checkoutPaymentTypeValue.getCode()) {
                case "CARD":
                    target.setCode(checkoutPaymentTypeValue.getCode());
                    target.setDisplayName(typeService.getEnumerationValue(checkoutPaymentTypeValue).getName());
                    break;
                case "NOVALNET_WALLET":
                    target.setCode(checkoutPaymentTypeValue.getCode());
                    target.setDisplayName(Localization.getLocalizedString("novalnet.checkoutpayment.type.wallet"));
                    break;
                case "NOVALNET_ONLINE_TRANSFER":
                    target.setCode(checkoutPaymentTypeValue.getCode());
                    target.setDisplayName(Localization.getLocalizedString("novalnet.checkoutpayment.type.onlinetransfer"));
                    break;
                case "NOVALNET_INVOICE":
                    target.setCode(checkoutPaymentTypeValue.getCode());
                    target.setDisplayName(Localization.getLocalizedString("novalnet.checkoutpayment.type.invoice"));
                    break;
                case "NOVALNET_DIRECT_DEBIT":
                    target.setCode(checkoutPaymentTypeValue.getCode());
                    target.setDisplayName(Localization.getLocalizedString("novalnet.checkoutpayment.type.directdebit"));
                    break;
            }

        }
    }

}
