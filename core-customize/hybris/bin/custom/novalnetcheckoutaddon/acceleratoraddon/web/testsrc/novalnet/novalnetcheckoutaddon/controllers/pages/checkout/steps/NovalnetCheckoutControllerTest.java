/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.acceleratorfacades.flow.impl.SessionOverrideCheckoutFlowFacade;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestRegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.GuestRegisterValidator;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.AbstractPageModel;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;
import novalnet.novalnetcheckoutaddon.services.NovalnetCheckoutAddonService;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetCheckoutControllerTest {
    private static final String ORDER_CODE = "100001";
    private static final String CHECKOUT_URL = "redirect:/checkout/multi";
    private final Model model = new ExtendedModelMap();
    @Mock
    private CheckoutFacade checkoutFacade;
    @Mock
    private CheckoutFlowFacade checkoutFlowFacade;
    @Mock
    private GuestRegisterValidator guestRegisterValidator;
    @Mock
    private NovalnetCheckoutAddonService novalnetCheckoutAddonService;
    @Mock
    private ContentPageModel contentPage;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private RedirectAttributes redirectAttributes;
    @Mock
    private BindingResult bindingResult;
    @Mock
    private GuestRegisterForm form;
    @Mock
    private OrderData orderData;
    @InjectMocks
    private TestableCheckoutController controller;
    private MockedStatic<SessionOverrideCheckoutFlowFacade> sessionOverrides;

    @BeforeEach
    void setUp() {
        sessionOverrides = mockStatic(SessionOverrideCheckoutFlowFacade.class);
    }

    @AfterEach
    void tearDown() {
        sessionOverrides.close();
    }

    @Test
    void shouldForwardTo404WhenModelIsNotFound() {
        final ModelNotFoundException exception = new ModelNotFoundException("no such order");

        final String result = controller.handleModelNotFoundException(exception, request);

        verify(request).setAttribute("message", exception.getMessage());
        assertEquals("forward:/404", result);
    }

    @Test
    void shouldRedirectToCartWhenThereIsNoValidCart() {
        when(checkoutFlowFacade.hasValidCart()).thenReturn(false);

        assertEquals("redirect:/cart", controller.checkout(redirectAttributes));

        verify(checkoutFacade, never()).prepareCartForCheckout();
    }

    @Test
    void shouldRedirectToCartWhenCartValidationFails() {
        when(checkoutFlowFacade.hasValidCart()).thenReturn(true);
        controller.cartInvalid = true;

        assertEquals("redirect:/cart", controller.checkout(redirectAttributes));

        verify(checkoutFacade, never()).prepareCartForCheckout();
    }

    @Test
    void shouldPrepareCartAndRedirectToCheckoutWhenCartIsValid() {
        when(checkoutFlowFacade.hasValidCart()).thenReturn(true);
        controller.cartInvalid = false;

        assertEquals(CHECKOUT_URL, controller.checkout(redirectAttributes));

        verify(checkoutFacade).prepareCartForCheckout();
    }

    @Test
    void shouldShowOrderConfirmationPage() throws Exception {
        when(novalnetCheckoutAddonService.getOrderConfirmationDetails(ORDER_CODE, model, request)).thenReturn(orderData);
        controller.contentPage = contentPage;

        final String result = controller.orderConfirmation(ORDER_CODE, request, model, redirectAttributes);

        assertEquals(NovalnetcheckoutaddonControllerConstants.Views.Pages.Checkout.CheckoutPage, result);
        assertEquals("orderConfirmation", controller.requestedPageLabel);
        assertSame(contentPage, controller.storedPage);
        assertSame(contentPage, controller.metaDataPage);
        assertEquals(ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW, model.asMap().get(ThirdPartyConstants.SeoRobots.META_ROBOTS));
        assertTrue(controller.calls.contains("consent"));
        sessionOverrides.verify(SessionOverrideCheckoutFlowFacade::resetSessionOverrides);
    }

    @Test
    void shouldRedirectToHomeWhenOrderIsUnknown() throws Exception {
        when(novalnetCheckoutAddonService.getOrderConfirmationDetails(ORDER_CODE, model, request))
                .thenThrow(new UnknownIdentifierException("unknown order"));

        final String result = controller.orderConfirmation(ORDER_CODE, request, model, redirectAttributes);

        assertEquals("redirect:/", result);
        assertTrue(controller.calls.isEmpty());
        assertEquals(null, controller.storedPage);
    }

    @Test
    void shouldRedirectToCheckoutWhenOrderDetailsAreMissing() throws Exception {
        when(novalnetCheckoutAddonService.getOrderConfirmationDetails(ORDER_CODE, model, request)).thenReturn(null);

        final String result = controller.orderConfirmation(ORDER_CODE, request, model, redirectAttributes);

        assertEquals(CHECKOUT_URL, result);
        assertEquals(null, controller.storedPage);
    }

    @Test
    void shouldRegisterGuestUserWhenFormIsValid() throws Exception {
        when(bindingResult.hasErrors()).thenReturn(false);
        when(novalnetCheckoutAddonService.registerGuestUser(form, model, request, response, redirectAttributes))
                .thenReturn("redirect:/my-account");

        final String result = controller.orderConfirmation(form, bindingResult, model, request, response, redirectAttributes);

        assertEquals("redirect:/my-account", result);
        verify(guestRegisterValidator).validate(form, bindingResult);
    }

    @Test
    void shouldShowOrderConfirmationAgainWhenGuestFormHasErrors() throws Exception {
        when(bindingResult.hasErrors()).thenReturn(true);
        when(form.getOrderCode()).thenReturn(ORDER_CODE);
        when(novalnetCheckoutAddonService.getOrderConfirmationDetails(ORDER_CODE, model, request)).thenReturn(orderData);
        controller.contentPage = contentPage;

        final String result = controller.orderConfirmation(form, bindingResult, model, request, response, redirectAttributes);

        assertEquals(NovalnetcheckoutaddonControllerConstants.Views.Pages.Checkout.CheckoutPage, result);
        verify(guestRegisterValidator).validate(form, bindingResult);
        verify(form).setTermsCheck(false);
        assertNotNull(model.asMap().get(GlobalMessages.ERROR_MESSAGES_HOLDER));
        verify(novalnetCheckoutAddonService, never()).registerGuestUser(form, model, request, response, redirectAttributes);
    }

    public static class TestableCheckoutController extends NovalnetCheckoutController {
        final List<String> calls = new ArrayList<>();
        CheckoutFlowFacade checkoutFlowFacade;
        ContentPageModel contentPage;
        boolean cartInvalid;
        String requestedPageLabel;
        AbstractPageModel storedPage;
        ContentPageModel metaDataPage;

        @Override
        public CheckoutFlowFacade getCheckoutFlowFacade() {
            return checkoutFlowFacade;
        }

        @Override
        public boolean validateCart(final RedirectAttributes redirectModel) {
            calls.add("validateCart");
            return cartInvalid;
        }

        @Override
        public String getCheckoutRedirectUrl() {
            return CHECKOUT_URL;
        }

        @Override
        public void addRegistrationConsentDataToModel(final Model model) {
            calls.add("consent");
        }

        @Override
        public ContentPageModel getContentPageForLabelOrId(final String labelOrId) throws CMSItemNotFoundException {
            requestedPageLabel = labelOrId;
            return contentPage;
        }

        @Override
        public void storeCmsPageInModel(final Model model, final AbstractPageModel cmsPage) {
            storedPage = cmsPage;
        }

        @Override
        public void setUpMetaDataForContentPage(final Model model, final ContentPageModel page) {
            metaDataPage = page;
        }
    }
}
