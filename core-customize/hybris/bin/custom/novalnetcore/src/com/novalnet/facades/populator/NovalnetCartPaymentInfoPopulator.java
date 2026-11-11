/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades.populator;

import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.converters.Populator;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.springframework.util.Assert;

/**
 * Maps Novalnet payment information to the standard payment-info field that
 * the B2B checkout validator reads from CartData.
 */
public class NovalnetCartPaymentInfoPopulator implements Populator<CartModel, CartData> {

    private Converter<AddressModel, AddressData> addressConverter;

    @Override
    public void populate(final CartModel source, final CartData target) throws ConversionException {
        Assert.notNull(source, "Parameter source cannot be null");
        Assert.notNull(target, "Parameter target cannot be null");

        final PaymentInfoModel paymentInfo = source.getPaymentInfo();
        if (!(paymentInfo instanceof NovalnetPaymentInfoModel novalnetPaymentInfo)) {
            return;
        }
        final CCPaymentInfoData paymentInfoData = new CCPaymentInfoData();
        paymentInfoData.setId(novalnetPaymentInfo.getPk() == null
                ? novalnetPaymentInfo.getCode()
                : novalnetPaymentInfo.getPk().toString());
        paymentInfoData.setSaved(novalnetPaymentInfo.isSaved());

        if (novalnetPaymentInfo.getBillingAddress() != null) {
            paymentInfoData.setBillingAddress(
                    addressConverter.convert(novalnetPaymentInfo.getBillingAddress()));
        }

        target.setPaymentInfo(paymentInfoData);
    }

    public void setAddressConverter(final Converter<AddressModel, AddressData> addressConverter) {
        this.addressConverter = addressConverter;
    }
}
