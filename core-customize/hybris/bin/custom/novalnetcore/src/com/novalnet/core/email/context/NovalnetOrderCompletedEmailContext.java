package com.novalnet.core.email.context;

import com.novalnet.model.NovalnetOrderCompletedEmailProcessModel;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.process.email.context.AbstractEmailContext;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.user.CustomerModel;


public class NovalnetOrderCompletedEmailContext extends AbstractEmailContext<NovalnetOrderCompletedEmailProcessModel> {
    private static final String ORDER_CODE = "orderCode";
    private static final String ORDER = "order";
    private static final String GUEST_CUSTOMER = "guestCustomer";

    @Override
    public void init(final NovalnetOrderCompletedEmailProcessModel process, final EmailPageModel emailPageModel) {
        super.init(process, emailPageModel);

        put(EMAIL, getCustomerEmailResolutionService().getEmailForCustomer(getCustomer(process)));

        put(DISPLAY_NAME, getCustomer(process).getDisplayName());

        put(ORDER_CODE, process.getOrder().getCode());

        put(ORDER, process.getOrder());

        CustomerModel customer = getCustomer(process);
        String uid = (customer != null) ? customer.getUid() : null;
        boolean isGuest = uid != null && uid.contains("|");
        put(GUEST_CUSTOMER, Boolean.valueOf(isGuest));

    }

    @Override
    protected BaseSiteModel getSite(final NovalnetOrderCompletedEmailProcessModel process) {
        return process.getSite();
    }

    @Override
    protected CustomerModel getCustomer(final NovalnetOrderCompletedEmailProcessModel process) {
        return (CustomerModel) process.getOrder().getUser();
    }

    @Override
    protected LanguageModel getEmailLanguage(final NovalnetOrderCompletedEmailProcessModel process) {
        return process.getLanguage();
    }
}

