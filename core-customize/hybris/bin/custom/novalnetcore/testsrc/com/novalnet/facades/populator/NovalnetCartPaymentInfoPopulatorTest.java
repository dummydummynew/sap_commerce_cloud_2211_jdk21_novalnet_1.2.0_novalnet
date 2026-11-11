/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.facades.populator;

import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetCartPaymentInfoPopulatorTest {
    private static final String PAYMENT_INFO_CODE = "NOVALNET_PAYMENT_001";
    private static final String PAYMENT_INFO_PK = "8796093054977";

    @InjectMocks
    private NovalnetCartPaymentInfoPopulator testObj;

    @Mock
    private CartModel source;

    @Mock
    private CartData target;

    @Mock
    private NovalnetPaymentInfoModel novalnetPaymentInfo;

    @Mock
    private PaymentInfoModel paymentInfo;

    @Mock
    private AddressModel billingAddress;

    @Mock
    private AddressData billingAddressData;

    @Mock
    private Converter<AddressModel, AddressData> addressConverter;

    @BeforeEach
    void setUp() {
        testObj.setAddressConverter(addressConverter);
    }

    @Test
    void populateShouldThrowExceptionWhenSourceIsNull() {
        assertThrows(IllegalArgumentException.class, () -> testObj.populate(null, target));
    }

    @Test
    void populateShouldThrowExceptionWhenTargetIsNull() {
        assertThrows(IllegalArgumentException.class, () -> testObj.populate(source, null));
    }

    @Test
    void populateShouldNotSetPaymentInfoWhenPaymentInfoIsNotNovalnet() {
        when(source.getPaymentInfo()).thenReturn(paymentInfo);

        testObj.populate(source, target);

        verify(target, never()).setPaymentInfo(any(CCPaymentInfoData.class));
        verifyNoInteractions(addressConverter);
    }

    @Test
    void populateShouldSetPaymentInfoWithPk() {
        final PK pk = PK.fromLong(Long.parseLong(PAYMENT_INFO_PK));

        when(source.getPaymentInfo()).thenReturn(novalnetPaymentInfo);
        when(novalnetPaymentInfo.getPk()).thenReturn(pk);
        when(novalnetPaymentInfo.isSaved()).thenReturn(true);
        when(novalnetPaymentInfo.getBillingAddress()).thenReturn(null);

        testObj.populate(source, target);

        final ArgumentCaptor<CCPaymentInfoData> captor = ArgumentCaptor.forClass(CCPaymentInfoData.class);

        verify(target).setPaymentInfo(captor.capture());

        assertEquals(PAYMENT_INFO_PK, captor.getValue().getId());
        assertEquals(true, captor.getValue().isSaved());
        assertNull(captor.getValue().getBillingAddress());
    }

    @Test
    void populateShouldUseCodeWhenPkIsNull() {
        when(source.getPaymentInfo()).thenReturn(novalnetPaymentInfo);
        when(novalnetPaymentInfo.getPk()).thenReturn(null);
        when(novalnetPaymentInfo.getCode()).thenReturn(PAYMENT_INFO_CODE);
        when(novalnetPaymentInfo.isSaved()).thenReturn(false);
        when(novalnetPaymentInfo.getBillingAddress()).thenReturn(null);

        testObj.populate(source, target);

        final ArgumentCaptor<CCPaymentInfoData> captor = ArgumentCaptor.forClass(CCPaymentInfoData.class);

        verify(target).setPaymentInfo(captor.capture());

        assertEquals(PAYMENT_INFO_CODE, captor.getValue().getId());
        assertEquals(false, captor.getValue().isSaved());
    }

    @Test
    void populateShouldConvertBillingAddress() {
        when(source.getPaymentInfo()).thenReturn(novalnetPaymentInfo);
        when(novalnetPaymentInfo.getPk()).thenReturn(null);
        when(novalnetPaymentInfo.getCode()).thenReturn(PAYMENT_INFO_CODE);
        when(novalnetPaymentInfo.isSaved()).thenReturn(true);
        when(novalnetPaymentInfo.getBillingAddress()).thenReturn(billingAddress);
        when(addressConverter.convert(billingAddress)).thenReturn(billingAddressData);

        testObj.populate(source, target);

        final ArgumentCaptor<CCPaymentInfoData> captor = ArgumentCaptor.forClass(CCPaymentInfoData.class);

        verify(target).setPaymentInfo(captor.capture());

        assertSame(billingAddressData, captor.getValue().getBillingAddress());
        verify(addressConverter).convert(billingAddress);
    }

    @Test
    void populateShouldNotConvertAddressWhenBillingAddressIsNull() {
        when(source.getPaymentInfo()).thenReturn(novalnetPaymentInfo);
        when(novalnetPaymentInfo.getPk()).thenReturn(null);
        when(novalnetPaymentInfo.getCode()).thenReturn(PAYMENT_INFO_CODE);
        when(novalnetPaymentInfo.isSaved()).thenReturn(false);
        when(novalnetPaymentInfo.getBillingAddress()).thenReturn(null);

        testObj.populate(source, target);

        verifyNoInteractions(addressConverter);
        verify(target).setPaymentInfo(any(CCPaymentInfoData.class));
    }
}
