/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
class DefaultNovalnetPaymentServiceTest {
    private static final String PAYMENT_CODE = "novalnetCreditCard";
    private static final String PAYMENT_NAME = "Credit Card";
    private static final String CUSTOMER_NUMBER = "12345";
    private static final String TRANSACTION_ID = "987654321";
    private static final String TOKEN = "payment-token";

    @Mock
    private ModelService modelService;
    @Mock
    private BaseStoreService baseStoreService;
    @Mock
    private NovalnetDao novalnetDao;
    @Mock
    private PaymentModeService paymentModeService;
    @Mock
    private BaseStoreModel baseStoreModel;
    @Mock
    private PaymentModeModel paymentModeModel;
    @Mock
    private NovalnetPaymentInfoModel paymentInfo;
    @Mock
    private NovalnetPaymentRefInfoModel referenceInfo;
    @Mock
    private PK paymentInfoPk;

    @InjectMocks
    private DefaultNovalnetPaymentService service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "modelService", modelService);
        ReflectionTestUtils.setField(service, "baseStoreService", baseStoreService);
        ReflectionTestUtils.setField(service, "novalnetDao", novalnetDao);
        ReflectionTestUtils.setField(service, "paymentModeService", paymentModeService);
    }

    @Test
    void shouldReturnPaymentModel() {
        when(paymentInfo.getPk()).thenReturn(paymentInfoPk);
        when(modelService.get(paymentInfoPk)).thenReturn(paymentInfo);

        assertEquals(paymentInfo, service.getPaymentModel(List.of(paymentInfo)));
    }

    @Test
    void shouldReturnPaymentNameWhenPaymentModeExists() {
        when(paymentModeService.getPaymentModeForCode(PAYMENT_CODE)).thenReturn(paymentModeModel);
        when(paymentModeModel.getName()).thenReturn(PAYMENT_NAME);

        assertEquals(PAYMENT_NAME, service.getPaymentName(PAYMENT_CODE));
    }

    @Test
    void shouldReturnEmptyPaymentNameWhenPaymentModeDoesNotExist() {
        when(paymentModeService.getPaymentModeForCode(PAYMENT_CODE)).thenReturn(null);

        assertEquals("", service.getPaymentName(PAYMENT_CODE));
    }

    @Test
    void shouldReturnCurrentBaseStore() {
        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStoreModel);

        assertEquals(baseStoreModel, service.getBaseStoreModel());
    }

    @Test
    void shouldUpdatePaymentGatewayStatus() {
        final String gatewayStatus = "CONFIRMED";
        when(paymentInfo.getPk()).thenReturn(paymentInfoPk);
        when(modelService.get(paymentInfoPk)).thenReturn(paymentInfo);

        service.updatePaymentInfo(List.of(paymentInfo), gatewayStatus);

        verify(paymentInfo).setPaymentGatewayStatus(gatewayStatus);
        verify(modelService).save(paymentInfo);
    }

    @Test
    void shouldSaveCreditCardReferenceInformation() {
        final String response = "{\"transaction\":{\"tid\":\"" + TRANSACTION_ID + "\",\"payment_data\":{\"token\":\"" + TOKEN
                + "\",\"card_brand\":\"VISA\",\"card_holder\":\"Test User\",\"card_number\":\"XXXX1111\",\"card_expiry_month\":\"09\",\"card_expiry_year\":\"2028\"}}}";

        service.handleReferenceTransactionInfo(response, CUSTOMER_NUMBER, PAYMENT_CODE);

        final ArgumentCaptor<NovalnetPaymentRefInfoModel> captor = ArgumentCaptor.forClass(NovalnetPaymentRefInfoModel.class);
        verify(modelService).save(captor.capture());
        assertNotNull(captor.getValue());
        assertEquals(Long.valueOf(CUSTOMER_NUMBER), captor.getValue().getCustomerNo());
        assertEquals(PAYMENT_CODE, captor.getValue().getPaymentType());
        assertEquals(Long.valueOf(TRANSACTION_ID), captor.getValue().getOrginalTid());
        assertEquals(TOKEN, captor.getValue().getToken());
        assertEquals("VISA", captor.getValue().getCardType());
        assertEquals("Test User", captor.getValue().getCardHolder());
    }

    @Test
    void shouldNormalizeGuaranteedSepaPaymentType() {
        final String response = "{\"transaction\":{\"tid\":\"" + TRANSACTION_ID + "\",\"payment_data\":{\"token\":\"" + TOKEN
                + "\",\"iban\":\"DE89370400440532013000\",\"account_holder\":\"Test User\"}}}";

        service.handleReferenceTransactionInfo(response, CUSTOMER_NUMBER, "novalnetGuaranteedDirectDebitSepa");

        final ArgumentCaptor<NovalnetPaymentRefInfoModel> captor = ArgumentCaptor.forClass(NovalnetPaymentRefInfoModel.class);
        verify(modelService).save(captor.capture());
        assertEquals("novalnetDirectDebitSepa", captor.getValue().getPaymentType());
        assertEquals("DE89370400440532013000", captor.getValue().getMaskedAccountIban());
    }
}
