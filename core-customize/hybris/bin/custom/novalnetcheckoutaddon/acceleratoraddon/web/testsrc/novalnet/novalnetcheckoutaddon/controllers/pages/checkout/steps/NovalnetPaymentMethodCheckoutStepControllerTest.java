/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.novalnet.facades.NovalnetPaymentFacade;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.commercefacades.user.UserFacade;

@UnitTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class NovalnetPaymentMethodCheckoutStepControllerTest {
    private static final String PAYMENT_TYPE = "CreditCard";
    private static final String CARD_INDEX = "0";
    private static final String PAYMENT_INFO_ID = "paymentInfo123";
    private static final String PAYMENT_METHOD_ID = "novalnetCreditCard";

    private static final String CURRENT_STEP = "payment-method";
    private static final String NEXT_STEP = "summary";
    private static final String PREVIOUS_STEP = "delivery-method";

    private TestNovalnetPaymentMethodCheckoutStepController controller;

    @Mock
    private NovalnetPaymentFacade novalnetPaymentFacade;

    @Mock
    private AcceleratorCheckoutFacade checkoutFacade;

    @Mock
    private UserFacade userFacade;

    @Mock
    private CheckoutStep checkoutStep;

    @BeforeEach
    void setUp() {
        controller = new TestNovalnetPaymentMethodCheckoutStepController(checkoutStep, checkoutFacade);

        ReflectionTestUtils.setField(controller, "novalnetPaymentFacade", novalnetPaymentFacade);
        ReflectionTestUtils.setField(controller, "userFacade", userFacade);
    }

    @Test
    void shouldRemoveSavedPaymentSuccessfully() {
        when(novalnetPaymentFacade.removeSavedPaymentData(PAYMENT_TYPE, CARD_INDEX)).thenReturn(true);

        final boolean result = controller.removeSavedPayment(PAYMENT_TYPE, CARD_INDEX);

        assertTrue(result);

        verify(novalnetPaymentFacade).removeSavedPaymentData(PAYMENT_TYPE, CARD_INDEX);
    }

    @Test
    void shouldReturnFalseWhenSavedPaymentRemovalFails() {
        when(novalnetPaymentFacade.removeSavedPaymentData(PAYMENT_TYPE, CARD_INDEX)).thenReturn(false);

        final boolean result = controller.removeSavedPayment(PAYMENT_TYPE, CARD_INDEX);

        assertFalse(result);

        verify(novalnetPaymentFacade).removeSavedPaymentData(PAYMENT_TYPE, CARD_INDEX);
    }

    @Test
    void shouldRemovePaymentMethodSuccessfully() throws Exception {
        when(checkoutStep.currentStep()).thenReturn(CURRENT_STEP);

        final String result = controller.remove(PAYMENT_INFO_ID, new RedirectAttributesModelMap());

        verify(userFacade).unlinkCCPaymentInfo(PAYMENT_INFO_ID);
        verify(checkoutStep).currentStep();

        assertEquals(CURRENT_STEP, result);
    }

    @Test
    void shouldSelectPaymentMethodSuccessfully() {
        when(checkoutStep.nextStep()).thenReturn(NEXT_STEP);

        final String result = controller.doSelectPaymentMethod(PAYMENT_METHOD_ID);

        verify(checkoutFacade).setPaymentDetails(PAYMENT_METHOD_ID);
        verify(checkoutStep).nextStep();

        assertEquals(NEXT_STEP, result);
    }

    @Test
    void shouldNotSetPaymentDetailsWhenPaymentMethodIdIsEmpty() {
        when(checkoutStep.nextStep()).thenReturn(NEXT_STEP);

        final String result = controller.doSelectPaymentMethod("");

        verifyNoInteractions(checkoutFacade);
        verify(checkoutStep).nextStep();

        assertEquals(NEXT_STEP, result);
    }

    @Test
    void shouldNotSetPaymentDetailsWhenPaymentMethodIdIsNull() {
        when(checkoutStep.nextStep()).thenReturn(NEXT_STEP);

        final String result = controller.doSelectPaymentMethod(null);

        verifyNoInteractions(checkoutFacade);
        verify(checkoutStep).nextStep();

        assertEquals(NEXT_STEP, result);
    }

    @Test
    void shouldNavigateToPreviousCheckoutStep() {
        when(checkoutStep.previousStep()).thenReturn(PREVIOUS_STEP);

        final String result = controller.back(null);

        verify(checkoutStep).previousStep();

        assertEquals(PREVIOUS_STEP, result);
    }

    @Test
    void shouldNavigateToNextCheckoutStep() {
        when(checkoutStep.nextStep()).thenReturn(NEXT_STEP);

        final String result = controller.next(null);

        verify(checkoutStep).nextStep();

        assertEquals(NEXT_STEP, result);
    }

    private static class TestNovalnetPaymentMethodCheckoutStepController extends NovalnetPaymentMethodCheckoutStepController {
        private final CheckoutStep checkoutStep;
        private final AcceleratorCheckoutFacade checkoutFacade;

        TestNovalnetPaymentMethodCheckoutStepController(final CheckoutStep checkoutStep,
                                                        final AcceleratorCheckoutFacade checkoutFacade) {
            this.checkoutStep = checkoutStep;
            this.checkoutFacade = checkoutFacade;
        }

        @Override
        protected CheckoutStep getCheckoutStep() {
            return checkoutStep;
        }

        @Override
        public AcceleratorCheckoutFacade getCheckoutFacade() {
            return checkoutFacade;
        }
    }
}
