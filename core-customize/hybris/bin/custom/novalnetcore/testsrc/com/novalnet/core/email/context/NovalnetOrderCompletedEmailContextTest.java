/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */

package com.novalnet.core.email.context;

import com.novalnet.model.NovalnetOrderCompletedEmailProcessModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorservices.model.cms2.pages.EmailPageModel;
import de.hybris.platform.acceleratorservices.urlencoder.UrlEncoderService;
import de.hybris.platform.acceleratorservices.urlresolver.SiteBaseUrlResolutionService;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commerceservices.customer.CustomerEmailResolutionService;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetOrderCompletedEmailContextTest {
    @InjectMocks
    private NovalnetOrderCompletedEmailContext context;

    @Mock
    private NovalnetOrderCompletedEmailProcessModel process;

    @Mock
    private EmailPageModel emailPageModel;

    @Mock
    private OrderModel order;

    @Mock
    private CustomerModel customer;

    @Mock
    private BaseSiteModel baseSite;

    @Mock
    private LanguageModel language;

    @Mock
    private CustomerEmailResolutionService customerEmailResolutionService;

    @Mock
    private SiteBaseUrlResolutionService siteBaseUrlResolutionService;

    @Mock
    private UrlEncoderService urlEncoderService;

    @BeforeEach
    void setUp() {
        context.setCustomerEmailResolutionService(customerEmailResolutionService);
        context.setSiteBaseUrlResolutionService(siteBaseUrlResolutionService);
        context.setUrlEncoderService(urlEncoderService);

        lenient().when(process.getOrder()).thenReturn(order);
        lenient().when(process.getSite()).thenReturn(baseSite);
        lenient().when(process.getLanguage()).thenReturn(language);
        lenient().when(order.getUser()).thenReturn(customer);

        lenient().when(language.getIsocode()).thenReturn("en");

        lenient().when(siteBaseUrlResolutionService.getWebsiteUrlForSite(baseSite, null, false, null))
                .thenReturn("http://localhost");

        lenient().when(urlEncoderService.getUrlEncodingPatternForEmail(process)).thenReturn("");
    }

    @Test
    void initShouldPopulateAllFieldsForRegisteredCustomer() {
        when(order.getCode()).thenReturn("ORDER1001");
        when(customer.getUid()).thenReturn("john.doe@example.com");
        when(customer.getDisplayName()).thenReturn("John Doe");
        when(customerEmailResolutionService.getEmailForCustomer(customer)).thenReturn("john.doe@example.com");

        context.init(process, emailPageModel);

        assertEquals("john.doe@example.com", context.get("email"));
        assertEquals("John Doe", context.get("displayName"));
        assertEquals("ORDER1001", context.get("orderCode"));
        assertEquals(order, context.get("order"));
        assertEquals(Boolean.FALSE, context.get("guestCustomer"));
    }

    @Test
    void initShouldMarkGuestCustomerTrueWhenUidContainsPipe() {
        when(order.getCode()).thenReturn("ORDER1002");
        when(customer.getUid()).thenReturn("guest|abc123|1698765432");
        when(customer.getDisplayName()).thenReturn("Guest User");
        when(customerEmailResolutionService.getEmailForCustomer(customer)).thenReturn("guest@example.com");

        context.init(process, emailPageModel);

        assertEquals(Boolean.TRUE, context.get("guestCustomer"));
    }

    @Test
    void initShouldMarkGuestCustomerFalseWhenUidIsNull() {
        when(order.getCode()).thenReturn("ORDER1003");
        when(customer.getUid()).thenReturn(null);
        when(customer.getDisplayName()).thenReturn("No Uid Customer");
        when(customerEmailResolutionService.getEmailForCustomer(customer)).thenReturn(null);

        context.init(process, emailPageModel);

        assertEquals(Boolean.FALSE, context.get("guestCustomer"));
        assertNull(context.get("email"));
    }

    @Test
    void initShouldMarkGuestCustomerFalseWhenUidDoesNotContainPipe() {
        when(order.getCode()).thenReturn("ORDER1004");
        when(customer.getUid()).thenReturn("regular.user@example.com");
        when(customer.getDisplayName()).thenReturn("Regular User");
        when(customerEmailResolutionService.getEmailForCustomer(customer)).thenReturn("regular.user@example.com");

        context.init(process, emailPageModel);

        assertFalse((Boolean) context.get("guestCustomer"));
    }

    @Test
    void getSiteShouldReturnSiteFromProcess() {
        final BaseSiteModel result = context.getSite(process);

        assertEquals(baseSite, result);
    }

    @Test
    void getEmailLanguageShouldReturnLanguageFromProcess() {
        final LanguageModel result = context.getEmailLanguage(process);

        assertEquals(language, result);
    }

    @Test
    void getCustomerShouldReturnCustomerFromOrder() {
        final CustomerModel result = context.getCustomer(process);

        assertEquals(customer, result);
    }

    @Test
    void initShouldThrowNPEWhenOrderIsNull() {
        when(process.getOrder()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> context.init(process, emailPageModel));
    }

    @Test
    void initShouldThrowNPEWhenCustomerIsNull() {
        when(order.getUser()).thenReturn(null);

        assertThrows(NullPointerException.class, () -> context.init(process, emailPageModel));
    }

    @Test
    void getCustomerShouldReturnNullWhenOrderUserIsNull() {
        when(order.getUser()).thenReturn(null);

        final CustomerModel result = context.getCustomer(process);

        assertNull(result);
    }

    @Test
    void getCustomerShouldThrowClassCastExceptionWhenUserIsNotCustomer() {
        final UserModel nonCustomerUser = mock(UserModel.class);

        when(order.getUser()).thenReturn(nonCustomerUser);

        assertThrows(ClassCastException.class, () -> context.getCustomer(process));
    }

    @Test
    void getSiteShouldReturnNullWhenProcessSiteIsNull() {
        when(process.getSite()).thenReturn(null);

        final BaseSiteModel result = context.getSite(process);

        assertNull(result);
    }

    @Test
    void getEmailLanguageShouldReturnNullWhenProcessLanguageIsNull() {
        when(process.getLanguage()).thenReturn(null);

        final LanguageModel result = context.getEmailLanguage(process);

        assertNull(result);
    }
}

