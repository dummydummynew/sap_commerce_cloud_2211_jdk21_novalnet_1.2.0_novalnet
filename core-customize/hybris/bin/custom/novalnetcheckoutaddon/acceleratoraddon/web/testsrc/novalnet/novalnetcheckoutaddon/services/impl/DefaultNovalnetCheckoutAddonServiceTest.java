/*
 *
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 *
 */
package novalnet.novalnetcheckoutaddon.services.impl;

import static de.hybris.platform.commercefacades.constants.CommerceFacadesConstants.CONSENT_GIVEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ConsentForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestRegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.security.AutoLoginStrategy;
import de.hybris.platform.commercefacades.consent.ConsentFacade;
import de.hybris.platform.commercefacades.consent.CustomerConsentDataStrategy;
import de.hybris.platform.commercefacades.consent.data.AnonymousConsentData;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CustomerData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.servicelayer.session.SessionService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class DefaultNovalnetCheckoutAddonServiceTest {
    private static final String ORDER_CODE = "ORDER123";
    private static final String TEST_PASSWORD = "test-password";
    private static final String GUEST_EMAIL = "guest@test.com";
    private static final String CUSTOMER_EMAIL = "customer@test.com";
    private static final String EXISTING_EMAIL = "existing@test.com";
    private static final String CHECKOUT_URL = "/checkout";
    private static final String ROOT_URL = "/";
    private static final String REDIRECT_ROOT = "redirect:/";
    private static final String TRANSACTION_ID = "123456";
    private static final String TEMPLATE_ID = "template123";
    private static final String COOKIE_TEMPLATE_ID = "cookie-template";
    private static final String ORDER_GUID = "ORDER-GUID";
    private static final String DIFFERENT_GUID = "DIFFERENT-GUID";
    private static final String PRODUCT_CODE = "PRODUCT123";
    private static final String TID_ATTRIBUTE = "tid";
    private static final String EMAIL_ATTRIBUTE = "email";
    private static final String NOVALNET_ADDRESS_DATA_ATTRIBUTE = "novalnetAddressData";
    private static final String DUPLICATE_UID_MESSAGE = "Duplicate UID";
    private static final int CONSENT_TEMPLATE_VERSION = 1;

    @Mock
    private SessionService sessionService;

    @Mock
    private CustomerFacade customerFacade;

    @Mock
    private AutoLoginStrategy autoLoginStrategy;

    @Mock
    private ConsentFacade consentFacade;

    @Mock
    private OrderFacade orderFacade;

    @Mock
    private ProductFacade productFacade;

    @Mock
    private CustomerConsentDataStrategy customerConsentDataStrategy;

    @Mock
    private Model model;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private GuestRegisterForm guestRegisterForm;

    private DefaultNovalnetCheckoutAddonService service;

    @BeforeEach
    void setUp() {
        service = new DefaultNovalnetCheckoutAddonService();

        ReflectionTestUtils.setField(service, "sessionService", sessionService);
        ReflectionTestUtils.setField(service, "customerFacade", customerFacade);
        ReflectionTestUtils.setField(service, "autoLoginStrategy", autoLoginStrategy);
        ReflectionTestUtils.setField(service, "consentFacade", consentFacade);
        ReflectionTestUtils.setField(service, "orderFacade", orderFacade);
        ReflectionTestUtils.setField(service, "productFacade", productFacade);
        ReflectionTestUtils.setField(service, "customerConsentDataStrategy", customerConsentDataStrategy);
    }

    @Test
    void shouldRegisterGuestUserSuccessfully() throws DuplicateUidException {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);
        form.setUid(GUEST_EMAIL);

        when(request.getCookies()).thenReturn(null);
        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));

        String result = service.registerGuestUser(form, model, request, response, redirectAttributes);

        verify(customerFacade).changeGuestToCustomer(TEST_PASSWORD, ORDER_CODE);
        verify(autoLoginStrategy).login(GUEST_EMAIL, TEST_PASSWORD, request, response);
        verify(sessionService).removeAttribute(WebConstants.ANONYMOUS_CHECKOUT);
        verify(customerConsentDataStrategy).populateCustomerConsentDataInSession();

        assertEquals(REDIRECT_ROOT, result);
    }

    @Test
    void shouldHandleDuplicateGuestUser() throws DuplicateUidException {
        GuestRegisterForm form = guestRegisterForm;

        when(form.getPwd()).thenReturn(TEST_PASSWORD);
        when(form.getOrderCode()).thenReturn(ORDER_CODE);
        when(form.getUid()).thenReturn(EXISTING_EMAIL);
        when(request.getHeader("Referer")).thenReturn(CHECKOUT_URL);

        DuplicateUidException exception = new DuplicateUidException(DUPLICATE_UID_MESSAGE);

        doThrow(exception).when(customerFacade).changeGuestToCustomer(TEST_PASSWORD, ORDER_CODE);

        String result = service.registerGuestUser(form, model, request, response, redirectAttributes);

        assertEquals("redirect:" + CHECKOUT_URL, result);

        verify(form).setTermsCheck(false);
        verify(model).addAttribute(any(GuestRegisterForm.class));
        verify(autoLoginStrategy, never()).login(any(), any(), any(), any());
        verify(sessionService, never()).removeAttribute(WebConstants.ANONYMOUS_CHECKOUT);
    }

    @Test
    void shouldGiveConsentWhenConsentIsGiven() throws DuplicateUidException {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);

        ConsentForm consentForm = new ConsentForm();
        consentForm.setConsentGiven(true);
        consentForm.setConsentTemplateId(TEMPLATE_ID);
        consentForm.setConsentTemplateVersion(Integer.valueOf(CONSENT_TEMPLATE_VERSION));

        form.setConsentForm(consentForm);

        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));
        when(request.getCookies()).thenReturn(null);

        service.registerGuestUser(form, model, request, response, redirectAttributes);

        verify(consentFacade).giveConsent(TEMPLATE_ID, Integer.valueOf(CONSENT_TEMPLATE_VERSION));
    }

    @Test
    void shouldNotGiveConsentWhenConsentIsNotGiven() throws DuplicateUidException {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);

        ConsentForm consentForm = new ConsentForm();
        consentForm.setConsentGiven(false);

        form.setConsentForm(consentForm);

        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));
        when(request.getCookies()).thenReturn(null);

        service.registerGuestUser(form, model, request, response, redirectAttributes);

        verify(consentFacade, never()).giveConsent(any(), any());
    }

    @Test
    void shouldHandleNullConsentForm() throws DuplicateUidException {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);
        form.setConsentForm(null);

        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));
        when(request.getCookies()).thenReturn(null);

        String result = service.registerGuestUser(form, model, request, response, redirectAttributes);

        assertEquals(REDIRECT_ROOT, result);

        verify(consentFacade, never()).giveConsent(any(), any());
    }

    @Test
    void shouldContinueWhenConsentCreationThrowsException() throws DuplicateUidException {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);

        ConsentForm consentForm = new ConsentForm();
        consentForm.setConsentGiven(true);
        consentForm.setConsentTemplateId(TEMPLATE_ID);
        consentForm.setConsentTemplateVersion(Integer.valueOf(CONSENT_TEMPLATE_VERSION));

        form.setConsentForm(consentForm);

        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));
        when(request.getCookies()).thenReturn(null);

        doThrow(new RuntimeException("Consent error")).when(consentFacade).giveConsent(TEMPLATE_ID,
                Integer.valueOf(CONSENT_TEMPLATE_VERSION));

        String result = service.registerGuestUser(form, model, request, response, redirectAttributes);

        assertEquals(REDIRECT_ROOT, result);

        verify(consentFacade).giveConsent(TEMPLATE_ID, Integer.valueOf(CONSENT_TEMPLATE_VERSION));
    }

    @Test
    void shouldProcessAnonymousConsentCookie() throws Exception {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);

        AnonymousConsentData consentData = new AnonymousConsentData();
        consentData.setConsentState(CONSENT_GIVEN);
        consentData.setTemplateCode(COOKIE_TEMPLATE_ID);
        consentData.setTemplateVersion(CONSENT_TEMPLATE_VERSION);

        String json = "[{\"consentState\":\"GIVEN\"," + "\"templateCode\":\"" + COOKIE_TEMPLATE_ID + "\","
                + "\"templateVersion\":\"" + CONSENT_TEMPLATE_VERSION + "\"}]";

        String encoded = URLEncoder.encode(json, StandardCharsets.UTF_8);

        Cookie cookie = new Cookie(WebConstants.ANONYMOUS_CONSENT_COOKIE, encoded);

        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));
        when(request.getCookies()).thenReturn(new Cookie[]
                {cookie});

        service.registerGuestUser(form, model, request, response, redirectAttributes);

        verify(consentFacade).giveConsent(COOKIE_TEMPLATE_ID, Integer.valueOf(CONSENT_TEMPLATE_VERSION));

        verify(customerConsentDataStrategy).populateCustomerConsentDataInSession();
    }

    @Test
    void shouldIgnoreMissingAnonymousConsentCookie() throws DuplicateUidException {
        GuestRegisterForm form = new GuestRegisterForm();
        form.setOrderCode(ORDER_CODE);
        form.setPwd(TEST_PASSWORD);

        when(customerFacade.getCurrentCustomer()).thenReturn(createCustomer(GUEST_EMAIL));
        when(request.getCookies()).thenReturn(null);

        service.registerGuestUser(form, model, request, response, redirectAttributes);

        verify(consentFacade, never()).giveConsent(any(), any());
    }

    @Test
    void shouldReturnOrderConfirmationDetailsSuccessfully() {
        String orderCode = ORDER_CODE;

        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(false);
        orderData.setEntries(Collections.emptyList());
        orderData.setAppliedOrderPromotions(Collections.emptyList());

        CustomerData user = new CustomerData();
        user.setUid(CUSTOMER_EMAIL);
        orderData.setUser(user);

        when(orderFacade.getOrderDetailsForCode(orderCode)).thenReturn(orderData);
        when(sessionService.getAttribute(TID_ATTRIBUTE)).thenReturn(TRANSACTION_ID);
        when(sessionService.getAttribute(NOVALNET_ADDRESS_DATA_ATTRIBUTE)).thenReturn(new AddressData());
        when(sessionService.getAttribute(WebConstants.CONTINUE_URL)).thenReturn(CHECKOUT_URL);

        OrderData result = service.getOrderConfirmationDetails(orderCode, model, request);

        assertEquals(orderData, result);

        verify(orderFacade).getOrderDetailsForCode(orderCode);
        verify(model).addAttribute("orderCode", orderCode);
        verify(model).addAttribute("orderData", orderData);
        verify(model).addAttribute("allItems", orderData.getEntries());
        verify(model).addAttribute("pageType", PageType.ORDERCONFIRMATION.name());
        verify(model).addAttribute("tid", TRANSACTION_ID);
        verify(model).addAttribute("continueUrl", CHECKOUT_URL);
    }

    @Test
    void shouldReturnNullWhenGuestOrderBelongsToDifferentSession() {
        String orderCode = ORDER_CODE;

        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(true);

        CustomerData user = new CustomerData();
        user.setUid(GUEST_EMAIL);
        orderData.setUser(user);

        when(orderFacade.getOrderDetailsForCode(orderCode)).thenReturn(orderData);
        when(sessionService.getAttribute(WebConstants.ANONYMOUS_CHECKOUT_GUID)).thenReturn(DIFFERENT_GUID);

        OrderData result = service.getOrderConfirmationDetails(orderCode, model, request);

        assertNull(result);

        verify(model, never()).addAttribute("orderData", orderData);
    }

    @Test
    void shouldLoadGuestOrderWhenSessionGuidMatches() {
        String orderCode = ORDER_CODE;

        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(true);

        CustomerData user = new CustomerData();
        user.setUid(ORDER_GUID + "|" + GUEST_EMAIL);
        orderData.setUser(user);

        orderData.setEntries(Collections.emptyList());
        orderData.setAppliedOrderPromotions(Collections.emptyList());

        when(sessionService.getAttribute(TID_ATTRIBUTE)).thenReturn(TRANSACTION_ID);
        when(orderFacade.getOrderDetailsForCode(orderCode)).thenReturn(orderData);
        when(sessionService.getAttribute(WebConstants.ANONYMOUS_CHECKOUT_GUID)).thenReturn(ORDER_GUID);
        when(sessionService.getAttribute(EMAIL_ATTRIBUTE)).thenReturn(GUEST_EMAIL);
        when(sessionService.getAttribute(WebConstants.CONTINUE_URL)).thenReturn(null);
        when(sessionService.getAttribute(NOVALNET_ADDRESS_DATA_ATTRIBUTE)).thenReturn(new AddressData());

        OrderData result = service.getOrderConfirmationDetails(orderCode, model, request);

        assertEquals(orderData, result);

        verify(model).addAttribute("orderData", orderData);
        verify(model).addAttribute("continueUrl", ROOT_URL);
    }

    @Test
    void shouldPopulateProductDetailsForOrderEntries() {
        String orderCode = ORDER_CODE;

        OrderData orderData = new OrderData();

        OrderEntryData entry = new OrderEntryData();

        ProductData originalProduct = new ProductData();
        originalProduct.setCode(PRODUCT_CODE);

        entry.setProduct(originalProduct);

        orderData.setEntries(List.of(entry));
        orderData.setGuestCustomer(false);
        orderData.setAppliedOrderPromotions(Collections.emptyList());

        CustomerData user = new CustomerData();
        user.setUid(CUSTOMER_EMAIL);
        orderData.setUser(user);

        when(sessionService.getAttribute(TID_ATTRIBUTE)).thenReturn(TRANSACTION_ID);

        ProductData loadedProduct = new ProductData();
        loadedProduct.setCode(PRODUCT_CODE);

        when(orderFacade.getOrderDetailsForCode(orderCode)).thenReturn(orderData);
        when(productFacade.getProductForCodeAndOptions(eq(PRODUCT_CODE), anyList())).thenReturn(loadedProduct);
        when(sessionService.getAttribute(WebConstants.CONTINUE_URL)).thenReturn(ROOT_URL);
        when(sessionService.getAttribute(NOVALNET_ADDRESS_DATA_ATTRIBUTE)).thenReturn(new AddressData());

        OrderData result = service.getOrderConfirmationDetails(orderCode, model, request);

        assertEquals(orderData, result);
        assertEquals(loadedProduct, entry.getProduct());

        verify(productFacade).getProductForCodeAndOptions(eq(PRODUCT_CODE),
                eq(List.of(ProductOption.BASIC, ProductOption.PRICE, ProductOption.CATEGORIES)));
    }

    @Test
    void shouldHandleOrderWithoutEntries() {
        String orderCode = ORDER_CODE;

        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(false);
        orderData.setEntries(null);
        orderData.setAppliedOrderPromotions(Collections.emptyList());

        CustomerData user = new CustomerData();
        user.setUid(CUSTOMER_EMAIL);
        orderData.setUser(user);

        when(sessionService.getAttribute(TID_ATTRIBUTE)).thenReturn(TRANSACTION_ID);
        when(orderFacade.getOrderDetailsForCode(orderCode)).thenReturn(orderData);
        when(sessionService.getAttribute(WebConstants.CONTINUE_URL)).thenReturn(null);
        when(sessionService.getAttribute(NOVALNET_ADDRESS_DATA_ATTRIBUTE)).thenReturn(new AddressData());

        OrderData result = service.getOrderConfirmationDetails(orderCode, model, request);

        assertEquals(orderData, result);

        verify(productFacade, never()).getProductForCodeAndOptions(any(), anyList());
    }

    @Test
    void shouldCreateGuestRegisterFormForGuestCustomer() {
        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(true);
        orderData.setGuid(ORDER_GUID);

        when(model.containsAttribute("guestRegisterForm")).thenReturn(false);
        when(sessionService.getAttribute(EMAIL_ATTRIBUTE)).thenReturn(GUEST_EMAIL);

        service.processEmailAddress(model, orderData);

        verify(model).addAttribute(any(GuestRegisterForm.class));
        verify(model).addAttribute(EMAIL_ATTRIBUTE, GUEST_EMAIL);
    }

    @Test
    void shouldNotCreateGuestRegisterFormWhenAlreadyPresent() {
        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(true);
        orderData.setGuid(ORDER_GUID);

        CustomerData user = new CustomerData();
        user.setUid(GUEST_EMAIL);
        orderData.setUser(user);

        when(model.containsAttribute("guestRegisterForm")).thenReturn(true);

        service.processEmailAddress(model, orderData);

        verify(model, never()).addAttribute(any(GuestRegisterForm.class));
    }

    @Test
    void shouldUseRegisteredCustomerEmailForNonGuestOrder() {
        OrderData orderData = new OrderData();
        orderData.setGuestCustomer(false);

        CustomerData user = new CustomerData();
        user.setUid(CUSTOMER_EMAIL);
        orderData.setUser(user);

        service.processEmailAddress(model, orderData);

        verify(model).addAttribute(EMAIL_ATTRIBUTE, CUSTOMER_EMAIL);

        verify(sessionService, never()).getAttribute(EMAIL_ATTRIBUTE);
    }

    private CustomerData createCustomer(String uid) {
        CustomerData customer = new CustomerData();
        customer.setUid(uid);
        return customer;
    }
}
