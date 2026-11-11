/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package com.novalnet.core.event;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetOrderCompletedEmailEventTest {
    @Mock
    private OrderModel order;

    @Mock
    private BaseStoreModel baseStore;

    @Mock
    private BaseSiteModel site;

    @Mock
    private CurrencyModel currency;

    private NovalnetOrderCompletedEmailEvent event;

    @BeforeEach
    void setUp() {
        event = new NovalnetOrderCompletedEmailEvent(order, baseStore, site, currency);
    }

    @Test
    void shouldCreateEventWithAllValidValues() {
        assertNotNull(event);
        assertEquals(order, event.getOrder());
        assertEquals(baseStore, event.getBaseStore());
        assertEquals(site, event.getSite());
        assertEquals(currency, event.getCurrency());
    }

    @Test
    void shouldReturnOrderSetInConstructor() {
        assertEquals(order, event.getOrder());
    }

    @Test
    void shouldUpdateOrderUsingSetter() {
        OrderModel newOrder = mock(OrderModel.class);

        event.setOrder(newOrder);

        assertEquals(newOrder, event.getOrder());
    }

    @Test
    void shouldCreateEventWithNullOrder() {
        NovalnetOrderCompletedEmailEvent nullOrderEvent = new NovalnetOrderCompletedEmailEvent(null, baseStore, site, currency);

        assertNull(nullOrderEvent.getOrder());
    }

    @Test
    void shouldCreateEventWithNullBaseStore() {
        NovalnetOrderCompletedEmailEvent nullStoreEvent = new NovalnetOrderCompletedEmailEvent(order, null, site, currency);

        assertNull(nullStoreEvent.getBaseStore());
    }

    @Test
    void shouldCreateEventWithNullSite() {
        NovalnetOrderCompletedEmailEvent nullSiteEvent = new NovalnetOrderCompletedEmailEvent(order, baseStore, null, currency);

        assertNull(nullSiteEvent.getSite());
    }

    @Test
    void shouldCreateEventWithNullCurrency() {
        NovalnetOrderCompletedEmailEvent nullCurrencyEvent = new NovalnetOrderCompletedEmailEvent(order, baseStore, site, null);

        assertNull(nullCurrencyEvent.getCurrency());
    }

    @Test
    void shouldSetOrderToNullUsingSetter() {
        event.setOrder(null);

        assertNull(event.getOrder());
    }
}

