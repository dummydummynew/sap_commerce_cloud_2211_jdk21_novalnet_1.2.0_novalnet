package com.novalnet.core.event;

import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.event.AbstractCommerceUserEvent;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;


public class NovalnetOrderCompletedEmailEvent extends AbstractCommerceUserEvent {
    private OrderModel order;

    public NovalnetOrderCompletedEmailEvent(final OrderModel order, final BaseStoreModel baseStore, final BaseSiteModel site,
                                            final CurrencyModel currency) {
        this.order = order;
        setBaseStore(baseStore);
        setSite(site);
        setCurrency(currency);
    }

    public OrderModel getOrder() {
        return order;
    }

    public void setOrder(final OrderModel order) {
        this.order = order;
    }
}

