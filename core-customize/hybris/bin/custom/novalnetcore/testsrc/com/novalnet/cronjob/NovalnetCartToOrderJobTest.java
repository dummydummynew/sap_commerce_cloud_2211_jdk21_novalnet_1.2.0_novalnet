/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.cronjob;

import com.novalnet.dao.NovalnetReplenishmentDao;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.enums.ProcessState;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import de.hybris.platform.servicelayer.model.ModelService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetCartToOrderJobTest {
    private static final String CRON_JOB_CODE = "CRONJOB001";

    @Mock
    private BusinessProcessService businessProcessService;

    @Mock
    private NovalnetReplenishmentDao novalnetReplenishmentDao;

    @Mock
    private CartToOrderCronJobModel cronJob;

    @Mock
    private ReplenishmentProcessModel process;

    @Mock
    private ModelService modelService;

    @Spy
    @InjectMocks
    private NovalnetCartToOrderJob novalnetCartToOrderJob;

    @BeforeEach
    void setUp() {
        novalnetCartToOrderJob.setBusinessProcessService(businessProcessService);
        novalnetCartToOrderJob.setModelService(modelService);

        when(cronJob.getCode()).thenReturn(CRON_JOB_CODE);
    }

    @AfterEach
    void tearDown() {
        Thread.interrupted();
    }

    @Test
    void testPerformWhenProcessSucceeds() {
        when(novalnetReplenishmentDao.findReplenishmentPaymentReference(CRON_JOB_CODE))
                .thenReturn(mock(NovalnetReplPayRefInfoModel.class));

        when(businessProcessService.createProcess(anyString(), eq("novalnetReplenishmentOrderProcess"))).thenReturn(process);

        when(process.getProcessState()).thenReturn(ProcessState.SUCCEEDED);

        final PerformResult result = novalnetCartToOrderJob.perform(cronJob);

        assertEquals(CronJobResult.SUCCESS, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());

        verify(process).setCartToOrderCronJob(cronJob);
        verify(modelService).save(process);
        verify(businessProcessService).startProcess(process);
    }

    @Test
    void testPerformWhenProcessFails() {
        when(novalnetReplenishmentDao.findReplenishmentPaymentReference(CRON_JOB_CODE))
                .thenReturn(mock(NovalnetReplPayRefInfoModel.class));

        when(businessProcessService.createProcess(anyString(), eq("novalnetReplenishmentOrderProcess"))).thenReturn(process);

        when(process.getProcessState()).thenReturn(ProcessState.ERROR);

        final PerformResult result = novalnetCartToOrderJob.perform(cronJob);

        assertEquals(CronJobResult.ERROR, result.getResult());
        assertEquals(CronJobStatus.UNKNOWN, result.getStatus());

        verify(businessProcessService).startProcess(process);
    }

    @Test
    void testPerformWhenProcessInterrupted() {
        when(novalnetReplenishmentDao.findReplenishmentPaymentReference(CRON_JOB_CODE))
                .thenReturn(mock(NovalnetReplPayRefInfoModel.class));

        when(businessProcessService.createProcess(anyString(), eq("novalnetReplenishmentOrderProcess"))).thenReturn(process);

        when(process.getProcessState()).thenReturn(ProcessState.RUNNING);

        Thread.currentThread().interrupt();

        final PerformResult result = novalnetCartToOrderJob.perform(cronJob);

        assertEquals(CronJobResult.FAILURE, result.getResult());
        assertEquals(CronJobStatus.FINISHED, result.getStatus());
        assertTrue(Thread.currentThread().isInterrupted());
    }
}
