/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.process.actions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novalnet.dao.NovalnetReplenishmentDao;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import com.novalnet.service.http.NovalnetEndpointConfigService;
import com.novalnet.service.payment.NovalnetB2BPaymentMethodService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.payment.dto.TransactionStatus;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.user.AddressService;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
class NovalnetTokenizedPaymentActionTest {
    private static final String STATUS_CODE_SUCCESS = "100";
    private static final String STATUS_CODE_FAILURE = "200";

    private static final String STATUS_CONFIRMED = "CONFIRMED";
    private static final String STATUS_ON_HOLD = "ON_HOLD";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_DECLINED = "DECLINED";

    private static final String PAYMENT_TYPE = "novalnetCreditCard";
    private static final String TRANSACTION_ID = "123456789";
    private static final String CART_CODE = "CART001";
    private static final String EMAIL = "customer@example.com";
    private static final String GUEST_USER_ID = "guest|customer@example.com";
    private static final String API_KEY = "api-key";
    private static final String NOTES = "Payment transaction details";

    private static final BigDecimal EXPECTED_AMOUNT = new BigDecimal("123.45");

    private static final String SUCCESS_RESPONSE = "{\"result\":{\"status_code\":\"100\",\"status_text\":\"SUCCESS\"},\"transaction\":{\"tid\":\"123456789\",\"status\":\"CONFIRMED\",\"amount\":12345,\"test_mode\":\"0\"},\"customer\":{\"email\":\"customer@example.com\"}}";

    private static final String EMPTY_CUSTOMER_EMAIL_RESPONSE = "{\"customer\":{\"email\":\"\"}}";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private NovalnetTokenizedPaymentAction novalnetTokenizedPaymentAction;

    @Mock
    private ModelService modelService;

    @Mock
    private NovalnetB2BPaymentMethodService novalnetB2BPaymentMethodService;

    @Mock
    private AddressService addressService;

    @Mock
    private PaymentModeService paymentModeService;

    @Mock
    private NovalnetReplenishmentDao novalnetReplenishmentDao;

    @Mock
    private NovalnetEndpointConfigService novalnetEndpointConfigService;

    @Mock
    private CartModel cart;

    @Mock
    private BaseStoreModel baseStore;

    @Mock
    private UserModel user;

    @Mock
    private AddressModel sourceBillingAddress;

    @Mock
    private AddressModel billingAddress;

    @Mock
    private NovalnetReplPayRefInfoModel paymentReference;

    @Mock
    private NovalnetPaymentInfoModel paymentInfo;

    @Mock
    private PaymentTransactionEntryModel transactionEntry;

    @Mock
    private PaymentTransactionModel paymentTransaction;

    @Mock
    private PaymentModeModel paymentMode;

    @BeforeEach
    void setUp() {
        novalnetTokenizedPaymentAction = spy(new NovalnetTokenizedPaymentAction());

        ReflectionTestUtils.setField(novalnetTokenizedPaymentAction, "modelService", modelService);
        ReflectionTestUtils.setField(novalnetTokenizedPaymentAction, "novalnetB2BPaymentMethodService",
                novalnetB2BPaymentMethodService);
        ReflectionTestUtils.setField(novalnetTokenizedPaymentAction, "addressService", addressService);
        ReflectionTestUtils.setField(novalnetTokenizedPaymentAction, "paymentModeService", paymentModeService);
        ReflectionTestUtils.setField(novalnetTokenizedPaymentAction, "novalnetReplenishmentDao", novalnetReplenishmentDao);
        ReflectionTestUtils.setField(novalnetTokenizedPaymentAction, "novalnetEndpointConfigService",
                novalnetEndpointConfigService);
    }

    @Test
    void isSuccessfulShouldReturnTrueForConfirmedTransaction() throws Exception {
        final JsonNode response = createResponse(STATUS_CODE_SUCCESS, STATUS_CONFIRMED);

        assertEquals(true, novalnetTokenizedPaymentAction.isSuccessful(response));
    }

    @Test
    void isSuccessfulShouldReturnTrueForOnHoldTransaction() throws Exception {
        final JsonNode response = createResponse(STATUS_CODE_SUCCESS, STATUS_ON_HOLD);

        assertEquals(true, novalnetTokenizedPaymentAction.isSuccessful(response));
    }

    @Test
    void isSuccessfulShouldReturnTrueForPendingTransaction() throws Exception {
        final JsonNode response = createResponse(STATUS_CODE_SUCCESS, STATUS_PENDING);

        assertEquals(true, novalnetTokenizedPaymentAction.isSuccessful(response));
    }

    @Test
    void isSuccessfulShouldReturnFalseForDeclinedTransaction() throws Exception {
        final JsonNode response = createResponse(STATUS_CODE_SUCCESS, STATUS_DECLINED);

        assertEquals(false, novalnetTokenizedPaymentAction.isSuccessful(response));
    }

    @Test
    void isSuccessfulShouldReturnFalseForFailureStatusCode() throws Exception {
        final JsonNode response = createResponse(STATUS_CODE_FAILURE, STATUS_CONFIRMED);

        assertEquals(false, novalnetTokenizedPaymentAction.isSuccessful(response));
    }

    @Test
    void resolvePaymentEmailAddressShouldReturnResponseEmail() throws Exception {
        final JsonNode response = OBJECT_MAPPER.readTree(SUCCESS_RESPONSE);

        final String result = novalnetTokenizedPaymentAction.resolvePaymentEmailAddress(cart, response);

        assertEquals(EMAIL, result);
    }

    @Test
    void resolvePaymentEmailAddressShouldReturnBillingEmail() throws Exception {
        final JsonNode response = OBJECT_MAPPER.readTree(EMPTY_CUSTOMER_EMAIL_RESPONSE);

        when(cart.getPaymentAddress()).thenReturn(sourceBillingAddress);
        when(sourceBillingAddress.getEmail()).thenReturn(EMAIL);

        final String result = novalnetTokenizedPaymentAction.resolvePaymentEmailAddress(cart, response);

        assertEquals(EMAIL, result);
    }

    @Test
    void resolvePaymentEmailAddressShouldReturnGuestEmail() throws Exception {
        final JsonNode response = OBJECT_MAPPER.readTree(EMPTY_CUSTOMER_EMAIL_RESPONSE);

        when(cart.getPaymentAddress()).thenReturn(null);
        when(cart.getUser()).thenReturn(user);
        when(user.getUid()).thenReturn(GUEST_USER_ID);

        final String result = novalnetTokenizedPaymentAction.resolvePaymentEmailAddress(cart, response);

        assertEquals(EMAIL, result);
    }

    @Test
    void resolvePaymentEmailAddressShouldReturnUserIdWhenNoGuestSeparator() throws Exception {
        final JsonNode response = OBJECT_MAPPER.readTree(EMPTY_CUSTOMER_EMAIL_RESPONSE);

        when(cart.getPaymentAddress()).thenReturn(null);
        when(cart.getUser()).thenReturn(user);
        when(user.getUid()).thenReturn(EMAIL);

        final String result = novalnetTokenizedPaymentAction.resolvePaymentEmailAddress(cart, response);

        assertEquals(EMAIL, result);
    }

    @Test
    void executePaymentShouldThrowExceptionWhenBaseStoreIsMissing() {
        when(cart.getStore()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> novalnetTokenizedPaymentAction.executePayment(cart, paymentReference));
    }

    @Test
    void executePaymentShouldThrowExceptionWhenApiKeyIsMissing() {
        when(cart.getStore()).thenReturn(baseStore);
        when(baseStore.getNovalnetAPIKey()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> novalnetTokenizedPaymentAction.executePayment(cart, paymentReference));
    }

    @Test
    void executePaymentShouldThrowExceptionWhenAccessKeyIsMissing() {
        when(cart.getStore()).thenReturn(baseStore);
        when(baseStore.getNovalnetAPIKey()).thenReturn(API_KEY);
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(null);

        assertThrows(IllegalStateException.class, () -> novalnetTokenizedPaymentAction.executePayment(cart, paymentReference));
    }

    @Test
    void storeSuccessfulPaymentShouldStoreTransactionSuccessfully() throws Exception {
        final JsonNode response = OBJECT_MAPPER.readTree(SUCCESS_RESPONSE);

        when(paymentReference.getPaymentType()).thenReturn(PAYMENT_TYPE);
        when(cart.getPaymentAddress()).thenReturn(sourceBillingAddress);
        when(cart.getUser()).thenReturn(user);
        when(cart.getCode()).thenReturn(CART_CODE);

        when(addressService.cloneAddressForOwner(sourceBillingAddress, cart)).thenReturn(billingAddress);

        when(modelService.create(NovalnetPaymentInfoModel.class)).thenReturn(paymentInfo);
        when(modelService.create(PaymentTransactionEntryModel.class)).thenReturn(transactionEntry);
        when(modelService.create(PaymentTransactionModel.class)).thenReturn(paymentTransaction);

        when(paymentModeService.getPaymentModeForCode(PAYMENT_TYPE)).thenReturn(paymentMode);

        doReturn(NOTES).when(novalnetTokenizedPaymentAction).buildOrderHistoryNotes(PAYMENT_TYPE, response);
        doReturn(EMAIL).when(novalnetTokenizedPaymentAction).resolvePaymentEmailAddress(cart, response);

        novalnetTokenizedPaymentAction.storeSuccessfulPayment(cart, paymentReference, response);

        verify(billingAddress).setBillingAddress(Boolean.TRUE);

        verify(paymentInfo).setUser(user);
        verify(paymentInfo).setBillingAddress(billingAddress);
        verify(paymentInfo).setPaymentEmailAddress(EMAIL);
        verify(paymentInfo).setPaymentProvider(PAYMENT_TYPE);
        verify(paymentInfo).setPaymentGatewayStatus(STATUS_CONFIRMED);
        verify(paymentInfo).setPaymentInfo(NOTES);
        verify(paymentInfo).setOrderHistoryNotes(NOTES);
        verify(paymentInfo).setCode(CART_CODE);
        verify(paymentInfo).setSaved(Boolean.TRUE);
        verify(paymentInfo).setDuplicate(Boolean.FALSE);

        verify(transactionEntry).setRequestId(TRANSACTION_ID);
        verify(transactionEntry).setCode(CART_CODE);
        verify(transactionEntry).setType(PaymentTransactionType.AUTHORIZATION);
        verify(transactionEntry).setTransactionStatus(TransactionStatus.ACCEPTED.name());
        verify(transactionEntry).setAmount(EXPECTED_AMOUNT);

        verify(paymentTransaction).setRequestId(TRANSACTION_ID);
        verify(paymentTransaction).setPaymentProvider(PAYMENT_TYPE);
        verify(paymentTransaction).setInfo(paymentInfo);
        verify(paymentTransaction).setOrder(cart);
        verify(paymentTransaction).setEntries(Collections.singletonList(transactionEntry));

        verify(cart).setPaymentMode(paymentMode);
        verify(cart).setPaymentAddress(billingAddress);
        verify(cart).setPaymentInfo(paymentInfo);
        verify(cart).setPaymentTransactions(Collections.singletonList(paymentTransaction));

        verify(modelService).saveAll(billingAddress, paymentInfo, transactionEntry, paymentTransaction, cart);
    }

    @Test
    void storeSuccessfulPaymentShouldThrowExceptionWhenBillingAddressIsMissing() throws Exception {
        final JsonNode response = OBJECT_MAPPER.readTree(SUCCESS_RESPONSE);

        when(paymentReference.getPaymentType()).thenReturn(PAYMENT_TYPE);
        when(cart.getPaymentAddress()).thenReturn(null);
        when(cart.getCode()).thenReturn(CART_CODE);

        doReturn(NOTES).when(novalnetTokenizedPaymentAction).buildOrderHistoryNotes(PAYMENT_TYPE, response);

        assertThrows(IllegalStateException.class,
                () -> novalnetTokenizedPaymentAction.storeSuccessfulPayment(cart, paymentReference, response));
    }

    private JsonNode createResponse(final String statusCode, final String transactionStatus) throws Exception {
        final String response = "{\"result\":{\"status_code\":\"" + statusCode
                + "\",\"status_text\":\"Test status\"},\"transaction\":{\"status\":\"" + transactionStatus + "\"}}";

        return OBJECT_MAPPER.readTree(response);
    }
}
