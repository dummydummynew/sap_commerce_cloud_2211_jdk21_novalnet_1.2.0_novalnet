package com.novalnet.core.event.listener;

import com.novalnet.core.event.NovalnetOrderCompletedEmailEvent;
import com.novalnet.model.NovalnetOrderCompletedEmailProcessModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.processengine.BusinessProcessService;
import de.hybris.platform.servicelayer.event.impl.AbstractEventListener;
import de.hybris.platform.servicelayer.model.ModelService;


public class NovalnetOrderCompletedEmailEventListener extends AbstractEventListener<NovalnetOrderCompletedEmailEvent> {
    private ModelService modelService;
    private BusinessProcessService businessProcessService;

    protected BusinessProcessService getBusinessProcessService() {
        return businessProcessService;
    }

    public void setBusinessProcessService(final BusinessProcessService businessProcessService) {
        this.businessProcessService = businessProcessService;
    }

    protected ModelService getModelService() {
        return modelService;
    }

    public void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }

    @Override
    protected void onEvent(final NovalnetOrderCompletedEmailEvent event) {
        OrderModel order = event.getOrder();

        NovalnetOrderCompletedEmailProcessModel process = (NovalnetOrderCompletedEmailProcessModel) getBusinessProcessService()
                .createProcess("novalnetOrderCompletedEmail-" + order.getCode() + "-" + System.currentTimeMillis(),
                        "novalnetOrderCompletedEmailProcess");

        process.setOrder(order);
        process.setLanguage(order.getLanguage());
        process.setCurrency(event.getCurrency());
        process.setSite(event.getSite());
        process.setStore(event.getBaseStore());
        getModelService().save(process);

        getBusinessProcessService().startProcess(process);
    }
}

