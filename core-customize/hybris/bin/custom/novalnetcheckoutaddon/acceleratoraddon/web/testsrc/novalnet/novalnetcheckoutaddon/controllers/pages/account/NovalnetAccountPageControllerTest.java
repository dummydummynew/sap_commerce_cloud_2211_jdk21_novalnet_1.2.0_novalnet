/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.UserFacade;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;

import com.novalnet.service.order.NovalnetOrderService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetAccountPageControllerTest {
    private static final String PAYMENT_INFO_ID = "paymentInfo123";
    private static final String ORDER_CODE = "order123";
    private static final String REDIRECT_PAYMENT_DETAILS = "redirect:/my-account/payment-details";
    private static final String REDIRECT_MY_ACCOUNT = "redirect:/my-account";

    @InjectMocks
    private NovalnetAccountPageController controller;

    @Mock
    private UserFacade userFacade;

    @Mock
    private NovalnetOrderService novalnetOrderService;

    @Mock
    private RedirectAttributes redirectAttributes;


    @Test
    void shouldRemovePaymentMethodSuccessfully() throws Exception {
        final String result = controller.removePaymentMethod(PAYMENT_INFO_ID, redirectAttributes);

        verify(userFacade).unlinkCCPaymentInfo(PAYMENT_INFO_ID);

        assertEquals(REDIRECT_PAYMENT_DETAILS, result);
    }

    @Test
    void shouldRemovePaymentMethodWhenPaymentInfoIdIsEmpty() throws Exception {
        final String result = controller.removePaymentMethod("", redirectAttributes);

        verify(userFacade).unlinkCCPaymentInfo("");

        assertEquals(REDIRECT_PAYMENT_DETAILS, result);
    }

    @Test
    void shouldRedirectToMyAccountWhenOrderDoesNotExist() throws Exception {
        doThrow(new UnknownIdentifierException("Order not found"))
                .when(novalnetOrderService).getOrder(ORDER_CODE);

        final String result = controller.order(ORDER_CODE, null, null);

        assertEquals(REDIRECT_MY_ACCOUNT, result);
    }
}
