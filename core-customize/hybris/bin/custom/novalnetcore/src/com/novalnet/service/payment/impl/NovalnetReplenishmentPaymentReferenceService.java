package com.novalnet.service.payment.impl;

import com.novalnet.model.NovalnetPaymentRefInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.servicelayer.model.ModelService;
import jakarta.annotation.Resource;

/**
 * Stores and resolves the token snapshot assigned to a replenishment schedule.
 */
public class NovalnetReplenishmentPaymentReferenceService {
    @Resource
    private ModelService modelService;

    /**
     * Create payment reference novalnet repl pay ref info model.
     *
     * @param cronJob the cron job
     * @param source  the source
     * @return the novalnet repl pay ref info model
     */
    public NovalnetReplPayRefInfoModel createPaymentReference(final CartToOrderCronJobModel cronJob, final NovalnetPaymentRefInfoModel source) {
        final NovalnetReplPayRefInfoModel reference = modelService.create(NovalnetReplPayRefInfoModel.class);
        reference.setReplenishmentOrderId(cronJob.getCode());
        reference.setCustomerNo(source.getCustomerNo());
        reference.setPaymentType(source.getPaymentType());
        reference.setToken(source.getToken());
        modelService.save(reference);
        return reference;
    }

}
