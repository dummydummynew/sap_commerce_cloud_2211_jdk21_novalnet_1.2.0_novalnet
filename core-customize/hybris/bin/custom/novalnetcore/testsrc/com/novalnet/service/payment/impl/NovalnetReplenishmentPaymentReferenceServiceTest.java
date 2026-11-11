/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.service.payment.impl;

import com.novalnet.model.NovalnetPaymentRefInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetReplenishmentPaymentReferenceServiceTest {

    private static final String REPLENISHMENT_CODE = "REPLENISHMENT-100";
    private static final Long CUSTOMER_NUMBER = 123456L;
    private static final String PAYMENT_TYPE = "novalnetCreditCard";
    private static final String PAYMENT_TOKEN = "token-value";

    private NovalnetReplenishmentPaymentReferenceService service;
    private ModelService modelService;
    private CartToOrderCronJobModel cronJob;
    private NovalnetPaymentRefInfoModel source;
    private NovalnetReplPayRefInfoModel createdReference;

    @BeforeEach
    void setUp() {
        service = new NovalnetReplenishmentPaymentReferenceService();

        modelService = mock(ModelService.class);
        cronJob = mock(CartToOrderCronJobModel.class);
        source = mock(NovalnetPaymentRefInfoModel.class);
        createdReference = mock(NovalnetReplPayRefInfoModel.class);

        ReflectionTestUtils.setField(service, "modelService", modelService);

        when(modelService.create(NovalnetReplPayRefInfoModel.class)).thenReturn(createdReference);

        when(cronJob.getCode()).thenReturn(REPLENISHMENT_CODE);
        when(source.getCustomerNo()).thenReturn(CUSTOMER_NUMBER);
        when(source.getPaymentType()).thenReturn(PAYMENT_TYPE);
        when(source.getToken()).thenReturn(PAYMENT_TOKEN);
    }

    @Test
    void shouldCreateCopyOfPaymentReferenceForReplenishment() {
        NovalnetReplPayRefInfoModel result = service.createPaymentReference(cronJob, source);

        assertSame(createdReference, result);

        verify(createdReference).setReplenishmentOrderId(REPLENISHMENT_CODE);
        verify(createdReference).setCustomerNo(CUSTOMER_NUMBER);
        verify(createdReference).setPaymentType(PAYMENT_TYPE);
        verify(createdReference).setToken(PAYMENT_TOKEN);
        verify(modelService).save(createdReference);
    }

    void shouldCreateNewReferenceForEachCall() {
        final Integer TWO = 2;
        NovalnetReplPayRefInfoModel secondReference = mock(NovalnetReplPayRefInfoModel.class);
        when(modelService.create(NovalnetReplPayRefInfoModel.class)).thenReturn(createdReference, secondReference);
        assertSame(createdReference, service.createPaymentReference(cronJob, source));
        assertSame(secondReference, service.createPaymentReference(cronJob, source));
        verify(modelService, times(TWO)).save(any(NovalnetReplPayRefInfoModel.class));
    }
}
