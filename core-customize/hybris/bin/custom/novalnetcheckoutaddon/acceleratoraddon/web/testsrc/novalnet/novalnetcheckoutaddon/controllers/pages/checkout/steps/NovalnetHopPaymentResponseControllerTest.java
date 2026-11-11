package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.novalnet.facades.NovalnetPaymentFacade;
import com.novalnet.util.NovalnetUtils;

import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commerceservices.strategies.CheckoutCustomerStrategy;
import de.hybris.platform.servicelayer.session.SessionService;

import jakarta.servlet.http.HttpServletRequest;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetHopPaymentResponseControllerTest {
    private static final String TRANSACTION_SECRET = "secret123";
    private static final String TRANSACTION_CHECK = "check123";
    private static final String TRANSACTION_ID = "123456";
    private static final String STATUS = "100";
    private static final String CHECKSUM = "validChecksum";

    private static final String ORDER_GUID = "anonymous-guid";
    private static final String ORDER_CODE = "ORDER123";

    private static final String SESSION_TXN_SECRET = "txn_secret";
    private static final String SESSION_TXN_CHECK = "txn_check";
    private static final String SESSION_ORDER_DATA = "novalnetOrderData";

    private static final String PARAM_TRANSACTION_ID = "tid";
    private static final String PARAM_STATUS = "status";
    private static final String PARAM_CHECKSUM = "checksum";

    @Spy
    @InjectMocks
    private TestableNovalnetHopPaymentResponseController controller;

    @Mock
    private NovalnetPaymentFacade novalnetPaymentFacade;

    @Mock
    private SessionService sessionService;

    @Mock
    private CheckoutCustomerStrategy checkoutCustomerStrategy;

    @Mock
    private HttpServletRequest request;

    private Map<String, String> resultMap;

    @BeforeEach
    void setUp() {
        resultMap = new HashMap<>();
        resultMap.put(PARAM_TRANSACTION_ID, TRANSACTION_ID);
        resultMap.put(PARAM_STATUS, STATUS);
        resultMap.put(PARAM_CHECKSUM, CHECKSUM);
    }

    @Test
    void shouldRedirectToAnonymousOrderConfirmationWhenPaymentIsSuccessful() {
        final OrderData orderData = new OrderData();
        orderData.setGuid(ORDER_GUID);

        mockRequestParameters();
        mockSessionValues();

        when(novalnetPaymentFacade.processTransaction(resultMap)).thenReturn(true);
        when(sessionService.getAttribute(SESSION_ORDER_DATA)).thenReturn(orderData);
        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(true);

        try (MockedStatic<NovalnetUtils> mockedStatic = mockStatic(NovalnetUtils.class)) {
            mockChecksum(mockedStatic);

            final String result = controller.handleHopResponse(null, request);

            assertEquals(NovalnetHopPaymentResponseController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_GUID, result);
        }
    }

    @Test
    void shouldRedirectToRegisteredOrderConfirmationWhenPaymentIsSuccessful() {
        final OrderData orderData = new OrderData();
        orderData.setCode(ORDER_CODE);

        mockRequestParameters();
        mockSessionValues();

        when(novalnetPaymentFacade.processTransaction(resultMap)).thenReturn(true);
        when(sessionService.getAttribute(SESSION_ORDER_DATA)).thenReturn(orderData);
        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(false);

        try (MockedStatic<NovalnetUtils> mockedStatic = mockStatic(NovalnetUtils.class)) {
            mockChecksum(mockedStatic);

            final String result = controller.handleHopResponse(null, request);

            assertEquals(NovalnetHopPaymentResponseController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_CODE, result);
        }
    }

    @Test
    void shouldReturnAnonymousOrderConfirmationUrl() {
        final OrderData orderData = new OrderData();
        orderData.setGuid(ORDER_GUID);

        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(true);

        assertEquals(NovalnetHopPaymentResponseController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_GUID,
                controller.confirmationPageURL(orderData));
    }

    @Test
    void shouldReturnRegisteredOrderConfirmationUrl() {
        final OrderData orderData = new OrderData();
        orderData.setCode(ORDER_CODE);

        when(checkoutCustomerStrategy.isAnonymousCheckout()).thenReturn(false);

        assertEquals(NovalnetHopPaymentResponseController.REDIRECT_URL_ORDER_CONFIRMATION + ORDER_CODE,
                controller.confirmationPageURL(orderData));
    }

    private void mockRequestParameters() {
        doReturn(resultMap).when(controller).getRequestParameterMap(request);
    }

    private void mockSessionValues() {
        when(sessionService.getAttribute(SESSION_TXN_SECRET)).thenReturn(TRANSACTION_SECRET);
        when(sessionService.getAttribute(SESSION_TXN_CHECK)).thenReturn(TRANSACTION_CHECK);
    }

    private void mockChecksum(final MockedStatic<NovalnetUtils> mockedStatic) {
        final String tokenString = TRANSACTION_ID + TRANSACTION_SECRET + STATUS
                + new StringBuilder(TRANSACTION_CHECK).reverse().toString();

        mockedStatic.when(() -> NovalnetUtils.generateChecksum(tokenString)).thenReturn(CHECKSUM);
    }

    private static class TestableNovalnetHopPaymentResponseController extends NovalnetHopPaymentResponseController {
        @Override
        public SessionService getSessionService() {
            return super.getSessionService();
        }

        @Override
        public Map<String, String> getRequestParameterMap(final HttpServletRequest request) {
            return super.getRequestParameterMap(request);
        }
    }
}
