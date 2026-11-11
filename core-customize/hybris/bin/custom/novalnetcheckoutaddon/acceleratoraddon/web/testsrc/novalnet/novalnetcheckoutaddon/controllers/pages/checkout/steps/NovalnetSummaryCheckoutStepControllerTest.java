/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import com.novalnet.dto.OrderPaymentCommentsData;
import com.novalnet.dto.payment.NovalnetPaymentRequest;
import com.novalnet.facades.NovalnetPaymentFacade;
import com.novalnet.service.payment.NovalnetPaymentRequestBuilder;
import com.novalnet.util.NovalnetUtils;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.acceleratorfacades.order.AcceleratorCheckoutFacade;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PlaceOrderForm;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@UnitTest
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NovalnetSummaryCheckoutStepControllerTest {
    private static final String PREVIOUS_STEP = "previousStep";
    private static final String NEXT_STEP = "nextStep";
    private static final String CURRENT_PAYMENT = "novalnetCreditCard";
    private static final String DECLINED_STATUS_TEXT = "Payment declined";
    private static final String REDIRECT_URL = "https://paygate.novalnet.de/redirect/abc";
    private static final String TID = "14000000001";
    private static final String EMAIL = "buyer@example.com";
    private static final String ORDER_CODE = "ORDER-001";
    private static final String ORDER_GUID = "GUID-001";
    private static final String ACCESS_KEY = " accessKey ";
    private static final int ORDER_AMOUNT_CENT = 10000;

    private static final String JSON_DECLINED = "{\"result\":{\"status_code\":\"103\",\"status_text\":\"Payment declined\",\"status_desc\":\"\"},"
            + "\"transaction\":{\"status\":\"FAILURE\",\"tid\":\"14000000001\"},"
            + "\"customer\":{\"email\":\"buyer@example.com\"}}";

    private static final String JSON_REDIRECT = "{\"result\":{\"status_code\":\"100\",\"status_text\":\"\",\"status_desc\":\"\","
            + "\"redirect_url\":\"https://paygate.novalnet.de/redirect/abc\"},"
            + "\"transaction\":{\"status\":\"PENDING\",\"tid\":\"14000000001\",\"txn_secret\":\"secret123\"},"
            + "\"customer\":{\"email\":\"buyer@example.com\"}}";

    private static final String JSON_CONFIRMED = "{\"result\":{\"status_code\":\"100\",\"status_text\":\"\",\"status_desc\":\"\"},"
            + "\"transaction\":{\"status\":\"CONFIRMED\",\"tid\":\"14000000001\",\"amount\":10000,\"currency\":\"EUR\"},"
            + "\"customer\":{\"email\":\"buyer@example.com\"}}";
    private final Model model = new ExtendedModelMap();
    @Mock
    private AcceleratorCheckoutFacade checkoutFacade;
    @Mock
    private CheckoutStep checkoutStep;
    @Mock
    private CheckoutCustomerStrategy checkoutCustomerStrategy;
    @Mock
    private NovalnetPaymentFacade novalnetPaymentFacade;
    @Mock
    private NovalnetPaymentRequestBuilder novalnetPaymentRequestBuilder;
    @Mock
    private PaymentModeService paymentModeService;
    @Mock
    private SessionService sessionService;
    @Mock
    private BaseStoreService baseStoreService;
    @Mock
    private BaseStoreModel baseStore;
    @Mock
    private CartData cartData;
    @Mock
    private OrderData orderData;
    @Mock
    private OrderPaymentCommentsData commentsData;
    @Mock
    private NovalnetPaymentRequest updateRequest;
    @Mock
    private HttpServletRequest request;
    @Mock
    private RedirectAttributes redirectAttributes;
    private TestableController controller;
    private MockedStatic<JaloSession> jaloSessionStatic;
    private MockedStatic<NovalnetUtils> novalnetUtilsStatic;

    @BeforeEach
    void setUp() {
        controller = new TestableController();
        controller.stepMock = checkoutStep;
        controller.facadeMock = checkoutFacade;
        controller.strategyMock = checkoutCustomerStrategy;

        controller.setBaseStoreService(baseStoreService);
        ReflectionTestUtils.setField(controller, "novalnetPaymentFacade", novalnetPaymentFacade);
        ReflectionTestUtils.setField(controller, "novalnetPaymentRequestBuilder", novalnetPaymentRequestBuilder);
        ReflectionTestUtils.setField(controller, "paymentModeService", paymentModeService);
        ReflectionTestUtils.setField(controller, "sessionService", sessionService);

        final JaloSession jaloSession = Mockito.mock(JaloSession.class, Answers.RETURNS_DEEP_STUBS);
        when(jaloSession.getSessionContext().getLocale()).thenReturn(Locale.ENGLISH);
        jaloSessionStatic = Mockito.mockStatic(JaloSession.class);
        jaloSessionStatic.when(JaloSession::getCurrentSession).thenReturn(jaloSession);

        novalnetUtilsStatic = Mockito.mockStatic(NovalnetUtils.class);
        novalnetUtilsStatic.when(NovalnetUtils::resolveLanguageCode).thenReturn("EN");

        when(baseStoreService.getCurrentBaseStore()).thenReturn(baseStore);
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(ACCESS_KEY);
        when(checkoutFacade.getCheckoutCart()).thenReturn(cartData);
        when(checkoutStep.previousStep()).thenReturn(PREVIOUS_STEP);
        when(checkoutStep.nextStep()).thenReturn(NEXT_STEP);

        doReturn(CURRENT_PAYMENT).when(sessionService).getAttribute("selectedPaymentMethodId");
        doReturn(ORDER_AMOUNT_CENT).when(sessionService).getAttribute("novalnetOrderAmount");
    }

    @AfterEach
    void tearDown() {
        jaloSessionStatic.close();
        novalnetUtilsStatic.close();
    }

    @Test
    void testBack() {
        assertEquals(PREVIOUS_STEP, controller.back(redirectAttributes));
    }

    @Test
    void testNext() {
        assertEquals(NEXT_STEP, controller.next(redirectAttributes));
    }

    @Test
    void testConfirmationPageUrlForRegisteredCustomer() {
        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(false);
        when(orderData.getCode()).thenReturn(ORDER_CODE);

        assertEquals(NovalnetSummaryCheckoutStepController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_CODE,
                controller.confirmationPageURL(orderData));
    }

    @Test
    void testConfirmationPageUrlForGuestCustomer() {
        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(true);
        when(orderData.getGuid()).thenReturn(ORDER_GUID);

        assertEquals(NovalnetSummaryCheckoutStepController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_GUID,
                controller.confirmationPageURL(orderData));
    }

    @Test
    void testBookWalletTransactionDelegatesToFacade() throws Exception {
        when(novalnetPaymentFacade.bookWalletTransaction(any(), any(), any(), anyInt(), any())).thenReturn("booked");

        final String result = controller.bookWalletTransaction(model, request, redirectAttributes);

        assertEquals("booked", result);
        verify(novalnetPaymentFacade).bookWalletTransaction(any(), any(), any(), anyInt(), any());
    }

    @Test
    void testPlaceOrderWalletPaymentWithoutWalletTidReturnsPreviousStep() throws Exception {
        doReturn("novalnetGooglePay").when(sessionService).getAttribute("selectedPaymentMethodId");
        doReturn("").when(sessionService).getAttribute("wallet_tid");

        final String result = controller.placeOrder(new PlaceOrderForm(), model, request, redirectAttributes);

        assertEquals(PREVIOUS_STEP, result);
        verify(sessionService).setAttribute("novalnetCheckoutError", "novalnet.payment.processing.error");
        verify(novalnetPaymentFacade, never()).createTransaction(any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    void testPlaceOrderWhenGatewayDeclines() throws Exception {
        when(novalnetPaymentFacade.createTransaction(any(), any(), any(), any(), anyInt(), any())).thenReturn(JSON_DECLINED);

        final String result = controller.placeOrder(new PlaceOrderForm(), model, request, redirectAttributes);

        assertEquals(PREVIOUS_STEP, result);
        verify(sessionService).setAttribute("novalnetCheckoutError", DECLINED_STATUS_TEXT);
        verify(novalnetPaymentFacade, never()).saveOrderData(any(), any(), any(), anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    void testPlaceOrderWhenGatewayReturnsRedirectUrl() throws Exception {
        when(novalnetPaymentFacade.createTransaction(any(), any(), any(), any(), anyInt(), any())).thenReturn(JSON_REDIRECT);

        final String result = controller.placeOrder(new PlaceOrderForm(), model, request, redirectAttributes);

        assertEquals("redirect:" + REDIRECT_URL, result);
        verify(sessionService).setAttribute("txn_secret", "secret123");
        verify(sessionService).setAttribute("txn_check", ACCESS_KEY.trim());
        verify(novalnetPaymentFacade, never()).saveOrderData(any(), any(), any(), anyInt(), any(), any(), any(), any(), any());
    }

    @Test
    void testPlaceOrderWithConfirmedTransactionPlacesOrder() throws Exception {
        when(novalnetPaymentFacade.createTransaction(any(), any(), any(), any(), anyInt(), any())).thenReturn(JSON_CONFIRMED);
        when(novalnetPaymentFacade.buildOrderAndPaymentComments(any(), any(), any())).thenReturn(commentsData);
        when(commentsData.getOrderComments()).thenReturn("comments");
        when(commentsData.getBankDetails()).thenReturn("bank");
        when(novalnetPaymentFacade.saveOrderData(any(), any(), any(), anyInt(), any(), any(), any(), any(), any()))
                .thenReturn(orderData);
        when(orderData.getCode()).thenReturn(ORDER_CODE);
        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(false);
        when(novalnetPaymentRequestBuilder.createTransactionUpdate(any(), any(), any())).thenReturn(updateRequest);
        when(novalnetPaymentRequestBuilder.serializePaymentRequest(updateRequest)).thenReturn("{}");

        final String result = controller.placeOrder(new PlaceOrderForm(), model, request, redirectAttributes);

        assertEquals(NovalnetSummaryCheckoutStepController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_CODE, result);
        verify(sessionService).setAttribute("tid", "commentsbank");
        verify(sessionService).setAttribute("email", EMAIL);
        verify(novalnetPaymentRequestBuilder).createTransactionUpdate(TID, ORDER_CODE, "EN");
        verify(novalnetPaymentFacade).sendRequest(any(), any());
    }

    static class TestableController extends NovalnetSummaryCheckoutStepController {
        CheckoutStep stepMock;
        AcceleratorCheckoutFacade facadeMock;
        CheckoutCustomerStrategy strategyMock;

        @Override
        protected CheckoutStep getCheckoutStep() {
            return stepMock;
        }

        @Override
        public AcceleratorCheckoutFacade getCheckoutFacade() {
            return facadeMock;
        }

        @Override
        protected CheckoutCustomerStrategy getCheckoutCustomerStrategy() {
            return strategyMock;
        }

        @Override
        protected boolean validateCart(final RedirectAttributes redirectModel) {
            return false;
        }

        @Override
        protected boolean validateOrderForm(final PlaceOrderForm placeOrderForm, final Model model) {
            return false;
        }

        @Override
        protected void setupPageModel(final Model model) {
            // CMS / breadcrumb setup is not under test
        }
    }
}
