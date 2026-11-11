/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.checkout.impl;

import com.novalnet.core.event.NovalnetOrderCompletedEmailEvent;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetCreditCardPaymentModeModel;
import com.novalnet.model.NovalnetInvoicePaymentModeModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.order.NovalnetOrderService;
import com.novalnet.service.payment.NovalnetPaymentService;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.basecommerce.model.site.BaseSiteModel;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.user.User;
import de.hybris.platform.order.CartService;
import de.hybris.platform.order.InvalidCartException;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.servicelayer.event.EventService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.store.BaseStoreModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class DefaultNovalnetCheckoutServiceTest {
    private static final String ORDER_NO = "100001";
    private static final String TID = "14500000012345";
    private static final String EMAIL = "customer@test.com";
    private static final String CURRENCY = "EUR";
    private static final String BANK_DETAILS = "bank details";
    private static final int AMOUNT = 10000;
    private static final String COMMENTS = "Payment: Invoice<br/>Transaction ID: 1<br>Done<br />End";
    private static final String SANITIZED_COMMENTS = "Payment: Invoice Transaction ID: 1 Done End";
    private final List<String> hookCalls = new ArrayList<>();
    @Mock
    private ModelService modelService;
    @Mock
    private CartService cartService;
    @Mock
    private NovalnetDao novalnetDao;
    @Mock
    private NovalnetPaymentService novalnetPaymentService;
    @Mock
    private EventService eventService;
    @Mock
    private PaymentModeService paymentModeService;
    @Mock
    private NovalnetOrderService novalnetOrderService;
    @Mock
    private Converter<AddressData, AddressModel> addressReverseConverter;
    @Mock
    private Converter<OrderModel, OrderData> orderConverter;
    @Mock
    private CartModel cart;
    @Mock
    private OrderModel order;
    @Mock
    private UserModel user;
    @Mock
    private CustomerModel customer;
    @Mock
    private AddressModel billingAddress;
    @Mock
    private PaymentTransactionEntryModel transactionEntry;
    @Mock
    private OrderHistoryEntryModel historyEntry;
    @Mock
    private OrderData orderData;
    @Mock
    private BaseStoreModel store;
    @Mock
    private BaseSiteModel site;
    @Mock
    private CurrencyModel currencyModel;
    private CustomerModel currentUser;

    @InjectMocks
    private DefaultNovalnetCheckoutService service = new DefaultNovalnetCheckoutService() {
        @Override
        public ModelService getModelService() {
            return modelService;
        }

        @Override
        public CustomerModel getCurrentUserForCheckout() {
            return currentUser;
        }

        @Override
        public Converter<OrderModel, OrderData> getOrderConverter() {
            return orderConverter;
        }

        @Override
        public void beforePlaceOrder(final CartModel cartModel) {
            hookCalls.add("before");
        }

        @Override
        public OrderModel placeOrder(final CartModel cartModel) throws InvalidCartException {
            hookCalls.add("place");
            return order;
        }

        @Override
        public void afterPlaceOrder(final CartModel cartModel, final OrderModel orderModel) {
            hookCalls.add("after");
        }
    };


    private void prepareOrderFlow(final String tid) {
        currentUser = customer;
        when(cartService.getSessionCart()).thenReturn(cart);
        when(modelService.create(AddressModel.class)).thenReturn(billingAddress);
        when(addressReverseConverter.convert(any(AddressData.class), eq(billingAddress))).thenReturn(billingAddress);
        when(novalnetPaymentService.createTransactionEntry(tid, cart, AMOUNT, SANITIZED_COMMENTS, CURRENCY))
                .thenReturn(transactionEntry);
        when(order.getCode()).thenReturn(ORDER_NO);

        lenient().when(order.getStore()).thenReturn(store);
        lenient().when(order.getSite()).thenReturn(site);
        lenient().when(order.getCurrency()).thenReturn(currencyModel);
        lenient().when(modelService.create(OrderHistoryEntryModel.class)).thenReturn(historyEntry);
        lenient().when(orderConverter.convert(order)).thenReturn(orderData);
    }

    private OrderData saveOrder(final String payment, final String status, final String tid) throws InvalidCartException {
        return service.saveOrderData(COMMENTS, payment, status, AMOUNT, CURRENCY, tid, EMAIL, new AddressData(), BANK_DETAILS);
    }

    @Test
    public void shouldPlaceOrderAndReturnConvertedOrderData() throws Exception {
        prepareOrderFlow(TID);
        final PaymentModeModel invoiceMode = mock(NovalnetInvoicePaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode("novalnetInvoice")).thenReturn(invoiceMode);

        final OrderData result = saveOrder("novalnetInvoice", "CONFIRMED", TID);

        assertThat(result).isSameAs(orderData);
        assertThat(hookCalls).containsExactly("before", "place", "after");

        verify(billingAddress).setEmail(EMAIL);
        verify(billingAddress).setOwner(cart);
        verify(novalnetOrderService).updateOrderStatus(eq(ORDER_NO), any(NovalnetPaymentInfoModel.class));
        verify(order).setPaymentMode(invoiceMode);
        verify(order).setStatusInfo(SANITIZED_COMMENTS);
        verify(order).setPaymentInfo(any(NovalnetPaymentInfoModel.class));
        verify(historyEntry).setOrder(order);
        verify(historyEntry).setDescription(SANITIZED_COMMENTS);
        verify(modelService).saveAll(any(NovalnetPaymentInfoModel.class), eq(cart), eq(billingAddress));
        verify(modelService).saveAll(eq(order), eq(historyEntry));
        verify(eventService).publishEvent(any(NovalnetOrderCompletedEmailEvent.class));
    }

    @Test
    public void shouldBuildPaymentInfoFromCheckoutData() throws Exception {
        prepareOrderFlow(TID);

        saveOrder("novalnetCreditCard", "CONFIRMED", TID);

        final ArgumentCaptor<NovalnetPaymentInfoModel> captor = ArgumentCaptor.forClass(NovalnetPaymentInfoModel.class);
        verify(cart).setPaymentInfo(captor.capture());
        final NovalnetPaymentInfoModel info = captor.getValue();

        assertThat(info.getBillingAddress()).isSameAs(billingAddress);
        assertThat(info.getPaymentEmailAddress()).isEqualTo(EMAIL);
        assertThat(info.getDuplicate()).isFalse();
        assertThat(info.isSaved()).isTrue();
        assertThat(info.getUser()).isSameAs(customer);
        assertThat(info.getPaymentInfo()).isEqualTo(COMMENTS);
        assertThat(info.getOrderHistoryNotes()).isEqualTo(BANK_DETAILS);
        assertThat(info.getPaymentProvider()).isEqualTo("novalnetCreditCard");
        assertThat(info.getPaymentGatewayStatus()).isEqualTo("CONFIRMED");
        assertThat(info.getCode()).isEqualTo(ORDER_NO);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldAttachPaymentTransactionToCart() throws Exception {
        prepareOrderFlow(TID);

        saveOrder("novalnetCreditCard", "CONFIRMED", TID);

        final ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(cart).setPaymentTransactions(captor.capture());
        final List<PaymentTransactionModel> transactions = captor.getValue();

        assertThat(transactions).hasSize(1);
        final PaymentTransactionModel transaction = transactions.get(0);
        assertThat(transaction.getPaymentProvider()).isEqualTo("novalnetCreditCard");
        assertThat(transaction.getRequestId()).isEqualTo(TID);
        assertThat(transaction.getEntries()).containsExactly(transactionEntry);
        assertThat(transaction.getOrder()).isSameAs(cart);
    }

    @Test
    public void shouldStoreFullPaidAmountForConfirmedCardPayment() throws Exception {
        prepareOrderFlow(TID);

        saveOrder("novalnetCreditCard", "CONFIRMED", TID);

        verify(order).setPaidAmount(AMOUNT);
        assertThat(capturedCallbackInfo().getPaidAmount()).isEqualTo(AMOUNT);
    }

    @Test
    public void shouldStoreZeroPaidAmountForPendingPayment() throws Exception {
        prepareOrderFlow(TID);

        saveOrder("novalnetCreditCard", "PENDING", TID);

        verify(order).setPaidAmount(0);
        assertThat(capturedCallbackInfo().getPaidAmount()).isZero();
    }

    @Test
    public void shouldStoreZeroPaidAmountForInvoice() throws Exception {
        assertZeroPaidAmountFor("novalnetInvoice");
    }

    @Test
    public void shouldStoreZeroPaidAmountForPrepayment() throws Exception {
        assertZeroPaidAmountFor("novalnetPrepayment");
    }

    @Test
    public void shouldStoreZeroPaidAmountForGuaranteedDirectDebitSepa() throws Exception {
        assertZeroPaidAmountFor("novalnetGuaranteedDirectDebitSepa");
    }

    @Test
    public void shouldStoreZeroPaidAmountForGuaranteedInvoice() throws Exception {
        assertZeroPaidAmountFor("novalnetGuaranteedInvoice");
    }

    private void assertZeroPaidAmountFor(final String payment) throws Exception {
        prepareOrderFlow(TID);

        saveOrder(payment, "CONFIRMED", TID);

        verify(order).setPaidAmount(0);
    }

    @Test
    public void shouldSaveCallbackInfoForTransaction() throws Exception {
        final Long callbackTid = 14500000012345L;
        prepareOrderFlow(TID);

        saveOrder("novalnetCreditCard", "CONFIRMED", TID);

        final NovalnetCallbackInfoModel callbackInfo = capturedCallbackInfo();
        assertThat(callbackInfo.getPaymentType()).isEqualTo("novalnetCreditCard");
        assertThat(callbackInfo.getOrderAmount()).isEqualTo(AMOUNT);
        assertThat(callbackInfo.getCallbackTid()).isEqualTo(callbackTid);
        assertThat(callbackInfo.getOrginalTid()).isEqualTo(callbackTid);
        assertThat(callbackInfo.getOrderNo()).isEqualTo(ORDER_NO);
    }

    @Test
    public void shouldNotAssignPaymentModeForUnknownPaymentCode() throws Exception {
        prepareOrderFlow(TID);
        when(paymentModeService.getPaymentModeForCode("somethingElse")).thenReturn(mock(PaymentModeModel.class));

        saveOrder("somethingElse", "CONFIRMED", TID);

        verify(order, never()).setPaymentMode(any());
    }

    @Test
    public void shouldNotAssignPaymentModeWhenModeIsNotConfigured() throws Exception {
        prepareOrderFlow(TID);
        when(paymentModeService.getPaymentModeForCode("novalnetCreditCard")).thenReturn(null);

        saveOrder("novalnetCreditCard", "CONFIRMED", TID);

        verify(order, never()).setPaymentMode(any());
    }

    @Test
    public void shouldRejectPaymentModeOfUnexpectedType() throws Exception {
        prepareOrderFlow(TID);
        when(paymentModeService.getPaymentModeForCode("novalnetCreditCard"))
                .thenReturn(mock(NovalnetInvoicePaymentModeModel.class));

        assertThatThrownBy(() -> saveOrder("novalnetCreditCard", "CONFIRMED", TID)).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("novalnetCreditCard");
        verify(order, never()).setPaymentMode(any());
    }

    @Test
    public void shouldAcceptPaymentModeOfExpectedType() throws Exception {
        prepareOrderFlow(TID);
        final PaymentModeModel cardMode = mock(NovalnetCreditCardPaymentModeModel.class);
        when(paymentModeService.getPaymentModeForCode("novalnetCreditCard")).thenReturn(cardMode);

        saveOrder("novalnetCreditCard", "CONFIRMED", TID);

        verify(order).setPaymentMode(cardMode);
    }

    @Test
    public void shouldFailOnNonNumericTransactionIdAfterOrderIsPlaced() throws Exception {
        final String badTid = "not-a-number";
        prepareOrderFlow(badTid);

        assertThatThrownBy(() -> saveOrder("novalnetCreditCard", "CONFIRMED", badTid)).isInstanceOf(NumberFormatException.class);
        assertThat(hookCalls).containsExactly("before", "place", "after");
        verify(modelService, never()).save(any(NovalnetCallbackInfoModel.class));
    }

    private NovalnetCallbackInfoModel capturedCallbackInfo() {
        final ArgumentCaptor<NovalnetCallbackInfoModel> captor = ArgumentCaptor.forClass(NovalnetCallbackInfoModel.class);
        verify(modelService).save(captor.capture());
        return captor.getValue();
    }

    @Test
    public void shouldCreateNewBillingAddress() {
        when(modelService.create(AddressModel.class)).thenReturn(billingAddress);

        assertThat(service.getBillingAddress()).isSameAs(billingAddress);
    }

    @Test
    public void shouldReturnCheckoutUserAsCurrentUser() {
        currentUser = customer;

        assertThat(service.getCurrentUser()).isSameAs(customer);
    }

    @Test
    public void shouldUseCartServiceAssignedThroughSetter() {
        final CartService otherCartService = mock(CartService.class);
        final CartModel otherCart = mock(CartModel.class);
        when(otherCartService.getSessionCart()).thenReturn(otherCart);
        when(otherCart.getUser()).thenReturn(user);

        service.setCartService(otherCartService);

        assertThat(service.isGuestUser()).isFalse();
        verify(otherCartService).getSessionCart();
        verify(cartService, never()).getSessionCart();
    }

    @Test
    public void shouldDetectGuestCustomer() {
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.GUEST);

        assertThat(service.isGuestUser()).isTrue();
    }

    @Test
    public void shouldNotTreatRegisteredCustomerAsGuest() {
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.REGISTERED);

        assertThat(service.isGuestUser()).isFalse();
    }

    @Test
    public void shouldNotTreatNonCustomerUserAsGuest() {
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(user);

        assertThat(service.isGuestUser()).isFalse();
    }

    @Test
    public void shouldExtractGuestEmailFromUid() {
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.GUEST);
        when(customer.getUid()).thenReturn("3f2a-guid|guest@test.com");

        assertThat(service.getUserEmail()).isEqualTo("guest@test.com");
    }

    @Test
    public void shouldReturnWholeUidWhenGuestUidHasNoSeparator() {
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.GUEST);
        when(customer.getUid()).thenReturn("guest@test.com");

        assertThat(service.getUserEmail()).isEqualTo("guest@test.com");
    }

    @Test
    public void shouldReadEmailFromJaloSessionForRegisteredUser() {
        final JaloSession session = mock(JaloSession.class);
        final User jaloUser = mock(User.class);
        when(session.getUser()).thenReturn(jaloUser);
        when(jaloUser.getUid()).thenReturn("registered@test.com");
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.REGISTERED);

        try (MockedStatic<JaloSession> jalo = mockStatic(JaloSession.class)) {
            jalo.when(JaloSession::getCurrentSession).thenReturn(session);

            assertThat(service.getUserEmail()).isEqualTo("registered@test.com");
        }
    }

    private void stubRegisteredCartUser() {
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.REGISTERED);
    }

    @Test
    public void shouldReturnNullPaymentModeWhenThereIsNoUser() {
        currentUser = null;

        assertThat(service.getLastOrderPaymentModeCode()).isNull();
        verify(novalnetDao, never()).getLastOrderForUser(any());
    }

    @Test
    public void shouldReturnNullPaymentModeForGuest() {
        currentUser = customer;
        when(cartService.getSessionCart()).thenReturn(cart);
        when(cart.getUser()).thenReturn(customer);
        when(customer.getType()).thenReturn(CustomerType.GUEST);

        assertThat(service.getLastOrderPaymentModeCode()).isNull();
        verify(novalnetDao, never()).getLastOrderForUser(any());
    }

    @Test
    public void shouldReturnNullPaymentModeWhenUserHasNoPreviousOrder() {
        currentUser = customer;
        stubRegisteredCartUser();
        when(novalnetDao.getLastOrderForUser(customer)).thenReturn(null);

        assertThat(service.getLastOrderPaymentModeCode()).isNull();
    }

    @Test
    public void shouldReturnNullPaymentModeWhenLastOrderHasNoPaymentMode() {
        currentUser = customer;
        stubRegisteredCartUser();
        when(novalnetDao.getLastOrderForUser(customer)).thenReturn(order);
        when(order.getPaymentMode()).thenReturn(null);

        assertThat(service.getLastOrderPaymentModeCode()).isNull();
    }

    @Test
    public void shouldReturnPaymentModeCodeOfLastOrder() {
        currentUser = customer;
        stubRegisteredCartUser();
        final PaymentModeModel mode = mock(PaymentModeModel.class);
        when(novalnetDao.getLastOrderForUser(customer)).thenReturn(order);
        when(order.getPaymentMode()).thenReturn(mode);
        when(mode.getCode()).thenReturn("novalnetCreditCard");

        assertThat(service.getLastOrderPaymentModeCode()).isEqualTo("novalnetCreditCard");
    }

    @Test
    public void shouldDetectWalletPaymentFromLastOrder() {
        currentUser = customer;
        stubRegisteredCartUser();
        final PaymentModeModel mode = mock(PaymentModeModel.class);
        when(novalnetDao.getLastOrderForUser(customer)).thenReturn(order);
        when(order.getPaymentMode()).thenReturn(mode);
        when(mode.getCode()).thenReturn("novalnetGooglePay");

        assertThat(service.isWalletPayment()).isTrue();
    }

    @Test
    public void shouldNotTreatCardPaymentAsWallet() {
        currentUser = customer;
        stubRegisteredCartUser();
        final PaymentModeModel mode = mock(PaymentModeModel.class);
        when(novalnetDao.getLastOrderForUser(customer)).thenReturn(order);
        when(order.getPaymentMode()).thenReturn(mode);
        when(mode.getCode()).thenReturn("novalnetCreditCard");

        assertThat(service.isWalletPayment()).isFalse();
    }

    @Test
    public void shouldNotTreatMissingLastOrderAsWallet() {
        currentUser = null;

        assertThat(service.isWalletPayment()).isFalse();
    }
}
