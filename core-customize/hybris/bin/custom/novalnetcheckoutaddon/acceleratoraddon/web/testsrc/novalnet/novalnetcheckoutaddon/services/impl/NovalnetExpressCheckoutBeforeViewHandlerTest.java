/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package novalnet.novalnetcheckoutaddon.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetExpressCheckoutBeforeViewHandlerTest {
    @Mock
    private CheckoutFlowFacade checkoutFlowFacade;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ModelMap model;

    @InjectMocks
    private NovalnetExpressCheckoutBeforeViewHandler beforeViewHandler;

    @Test
    void testBeforeViewWithCartPageAndExpressCheckoutEnabled() {
        String viewName = "cartPage";

        when(checkoutFlowFacade.isExpressCheckoutEnabledForStore()).thenReturn(true);

        String result = beforeViewHandler.beforeView(request, response, model, viewName);

        assertEquals(viewName, result);
        verify(checkoutFlowFacade).isExpressCheckoutEnabledForStore();
        verify(model).addAttribute("expressCheckoutAllowed", true);
    }

    @Test
    void testBeforeViewWithCartPageAndExpressCheckoutDisabled() {
        String viewName = "cartPage";

        when(checkoutFlowFacade.isExpressCheckoutEnabledForStore()).thenReturn(false);

        String result = beforeViewHandler.beforeView(request, response, model, viewName);

        assertEquals(viewName, result);
        verify(checkoutFlowFacade).isExpressCheckoutEnabledForStore();
        verify(model).addAttribute("expressCheckoutAllowed", false);
    }

    @Test
    void testBeforeViewWithViewNameContainingCartPage() {
        String viewName = "pages/cart/cartPage";

        when(checkoutFlowFacade.isExpressCheckoutEnabledForStore()).thenReturn(true);

        String result = beforeViewHandler.beforeView(request, response, model, viewName);

        assertEquals(viewName, result);
        verify(checkoutFlowFacade).isExpressCheckoutEnabledForStore();
        verify(model).addAttribute("expressCheckoutAllowed", true);
    }

    @Test
    void testBeforeViewWithDifferentViewName() {
        String viewName = "checkoutPage";

        String result = beforeViewHandler.beforeView(request, response, model, viewName);

        assertEquals(viewName, result);
        verifyNoInteractions(checkoutFlowFacade, model);
    }

    @Test
    void testBeforeViewWithNullViewName() {
        String result = beforeViewHandler.beforeView(request, response, model, null);

        assertEquals(null, result);
        verifyNoInteractions(checkoutFlowFacade, model);
    }

    @Test
    void testSetCheckoutFlowFacade() {
        NovalnetExpressCheckoutBeforeViewHandler handler = new NovalnetExpressCheckoutBeforeViewHandler();

        handler.setCheckoutFlowFacade(checkoutFlowFacade);

        when(checkoutFlowFacade.isExpressCheckoutEnabledForStore()).thenReturn(true);

        String result = handler.beforeView(request, response, model, "cartPage");

        assertEquals("cartPage", result);
        verify(model).addAttribute("expressCheckoutAllowed", true);
    }
}
