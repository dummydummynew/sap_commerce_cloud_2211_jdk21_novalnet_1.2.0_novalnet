package com.novalnet.cronjob;

import com.novalnet.dao.NovalnetReplenishmentDao;
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel;
import de.hybris.platform.b2bacceleratorservices.order.impl.B2BAcceleratorCartToOrderJob;
import de.hybris.platform.cronjob.enums.CronJobResult;
import de.hybris.platform.cronjob.enums.CronJobStatus;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.processengine.enums.ProcessState;
import de.hybris.platform.servicelayer.cronjob.PerformResult;
import jakarta.annotation.Resource;
import org.apache.log4j.Logger;


/**
 * Starts the Novalnet-specific process for schedules that carry a selected token.
 */
public class NovalnetCartToOrderJob extends B2BAcceleratorCartToOrderJob {
    private static final Logger LOG = Logger.getLogger(NovalnetCartToOrderJob.class);
    private BusinessProcessService businessProcessService;
    @Resource
    private NovalnetReplenishmentDao novalnetReplenishmentDao;

    @Override
    public PerformResult perform(final CartToOrderCronJobModel cronJob) {
        if (novalnetReplenishmentDao.findReplenishmentPaymentReference(cronJob.getCode()) == null) {
            return super.perform(cronJob);
        }

        final String processCode = "NovalnetCartToOrderJob" + cronJob.getCode() + System.currentTimeMillis();
        LOG.info("Starting Novalnet replenishment process " + processCode + " for replenishment order " + cronJob.getCode());
        final ReplenishmentProcessModel process = (ReplenishmentProcessModel) getBusinessProcessService().createProcess(processCode, "novalnetReplenishmentOrderProcess");
        process.setCartToOrderCronJob(cronJob);
        modelService.save(process);
        getBusinessProcessService().startProcess(process);

        do {
            modelService.refresh(process);
            if (process.getProcessState() == ProcessState.RUNNING || process.getProcessState() == ProcessState.CREATED) {
                try {
                    Thread.sleep(1000);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return new PerformResult(CronJobResult.FAILURE, CronJobStatus.FINISHED);
                }
            }
        }
        while (process.getProcessState() == ProcessState.RUNNING || process.getProcessState() == ProcessState.CREATED);
        LOG.info("Finished Novalnet replenishment process " + processCode + " with state " + process.getProcessState());
        return process.getProcessState() == ProcessState.SUCCEEDED
                ? new PerformResult(CronJobResult.SUCCESS, CronJobStatus.FINISHED)
                : new PerformResult(CronJobResult.ERROR, CronJobStatus.UNKNOWN);
    }

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    public void setBusinessProcessService(final BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }
}
