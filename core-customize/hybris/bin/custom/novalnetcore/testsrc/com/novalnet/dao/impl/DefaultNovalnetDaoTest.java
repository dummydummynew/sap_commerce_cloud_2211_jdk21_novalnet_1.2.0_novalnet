/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dao.impl;

import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class DefaultNovalnetDaoTest {
    private static final String ORDER_CODE = "100001";
    private static final String CUSTOMER_NO = "12345";
    private static final String PAYMENT_TYPE = "CREDITCARD";
    private static final String TID = "14500000012345";
    private static final String TOKEN = "token-abc";
    private static final String CURRENCY_ISO = "EUR";
    private static final long USER_PK = 987654321L;

    @Mock
    private FlexibleSearchService flexibleSearchService;

    @Mock
    private SearchResult<OrderModel> orderSearchResult;

    @Mock
    private SearchResult<NovalnetPaymentRefInfoModel> paymentRefSearchResult;

    @Mock
    private SearchResult<NovalnetPaymentInfoModel> paymentInfoSearchResult;

    @Mock
    private SearchResult<NovalnetCallbackInfoModel> callbackSearchResult;

    @Mock
    private OrderModel orderModel;

    @Mock
    private UserModel userModel;

    @Mock
    private NovalnetPaymentInfoModel paymentInfoModel;

    @Mock
    private NovalnetPaymentRefInfoModel paymentRefModel;

    @Mock
    private NovalnetCallbackInfoModel callbackInfoModel;

    @InjectMocks
    private DefaultNovalnetDao novalnetDao;


    @Test
    public void shouldReturnOrdersForOrderCode() {
        when(flexibleSearchService.<OrderModel>search(any(FlexibleSearchQuery.class))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(List.of(orderModel));

        assertThat(novalnetDao.getOrderInfoModel(ORDER_CODE)).containsExactly(orderModel);

        final FlexibleSearchQuery query = captureQuery();
        assertThat(query.getQueryParameters()).containsEntry("code", ORDER_CODE);
    }


    @Test
    public void shouldConvertCustomerNoToLongWhenSearchingPaymentRefInfo() {
        when(flexibleSearchService.<NovalnetPaymentRefInfoModel>search(any(FlexibleSearchQuery.class)))
                .thenReturn(paymentRefSearchResult);
        when(paymentRefSearchResult.getResult()).thenReturn(List.of(paymentRefModel));

        assertThat(novalnetDao.getPaymentRefInfo(CUSTOMER_NO, PAYMENT_TYPE)).containsExactly(paymentRefModel);

        final FlexibleSearchQuery query = captureQuery();
        assertThat(query.getQueryParameters()).containsEntry("customerNo", Long.parseLong(CUSTOMER_NO)).containsEntry("paymentType",
                PAYMENT_TYPE);
    }

    @Test
    public void shouldThrowWhenCustomerNoIsNotNumeric() {
        assertThatThrownBy(() -> novalnetDao.getPaymentRefInfo("not-a-number", PAYMENT_TYPE))
                .isInstanceOf(NumberFormatException.class);
        verify(flexibleSearchService, never()).search(any(FlexibleSearchQuery.class));
    }


    @Test
    public void shouldSearchNonDuplicatePaymentInfo() {
        when(flexibleSearchService.<NovalnetPaymentInfoModel>search(any(FlexibleSearchQuery.class)))
                .thenReturn(paymentInfoSearchResult);
        when(paymentInfoSearchResult.getResult()).thenReturn(List.of(paymentInfoModel));

        assertThat(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).containsExactly(paymentInfoModel);

        final FlexibleSearchQuery query = captureQuery();
        assertThat(query.getQueryParameters()).containsEntry("code", ORDER_CODE).containsEntry("duplicate", Boolean.FALSE);
    }

    @Test
    public void shouldReturnCallbackInfoByTransactionId() {
        when(flexibleSearchService.<NovalnetCallbackInfoModel>search(any(FlexibleSearchQuery.class)))
                .thenReturn(callbackSearchResult);
        when(callbackSearchResult.getResult()).thenReturn(List.of(callbackInfoModel));

        assertThat(novalnetDao.getCallbackInfo(TID)).containsExactly(callbackInfoModel);

        assertThat(captureQuery().getQueryParameters()).containsEntry("transactionId", TID);
    }

    @Test
    public void shouldReturnPaymentDetailsInfoByOrderCode() {
        when(flexibleSearchService.<NovalnetCallbackInfoModel>search(any(FlexibleSearchQuery.class)))
                .thenReturn(callbackSearchResult);
        when(callbackSearchResult.getResult()).thenReturn(List.of(callbackInfoModel));

        assertThat(novalnetDao.getPaymentDetailsInfo(ORDER_CODE)).containsExactly(callbackInfoModel);

        assertThat(captureQuery().getQueryParameters()).containsEntry("orderNo", ORDER_CODE);
    }

    @Test
    public void shouldReturnNullWhenNoCallbackFoundForTid() {
        stubCallbackSearch(Collections.emptyList());

        assertThat(novalnetDao.getOrderByTid(TID)).isNull();
    }

    @Test
    public void shouldReturnNullWhenCallbackOrderNoIsNull() {
        when(callbackInfoModel.getOrderNo()).thenReturn(null);
        stubCallbackSearch(List.of(callbackInfoModel));

        assertThat(novalnetDao.getOrderByTid(TID)).isNull();
    }

    @Test
    public void shouldReturnNullWhenCallbackOrderNoIsEmpty() {
        when(callbackInfoModel.getOrderNo()).thenReturn("");
        stubCallbackSearch(List.of(callbackInfoModel));

        assertThat(novalnetDao.getOrderByTid(TID)).isNull();
    }

    @Test
    public void shouldReturnNullWhenOrderNotFoundForCallbackOrderNo() {
        when(callbackInfoModel.getOrderNo()).thenReturn(ORDER_CODE);
        stubCallbackSearch(List.of(callbackInfoModel));
        stubOrderSearch(Collections.emptyList());

        assertThat(novalnetDao.getOrderByTid(TID)).isNull();
    }

    @Test
    public void shouldReturnOrderResolvedFromCallbackTid() {
        when(callbackInfoModel.getOrderNo()).thenReturn(ORDER_CODE);
        stubCallbackSearch(List.of(callbackInfoModel));
        stubOrderSearch(List.of(orderModel));

        assertThat(novalnetDao.getOrderByTid(TID)).isSameAs(orderModel);
    }

    @Test
    public void shouldThrowWhenTidIsNotNumeric() {
        assertThatThrownBy(() -> novalnetDao.getOrderByTid("abc")).isInstanceOf(NumberFormatException.class);
    }

    @Test
    public void shouldReturnNullTokenWhenOrderIsNull() {
        assertThat(novalnetDao.getStoredPaymentToken(null)).isNull();
    }

    @Test
    public void shouldReturnNullTokenWhenUserIsNull() {
        when(orderModel.getUser()).thenReturn(null);

        assertThat(novalnetDao.getStoredPaymentToken(orderModel)).isNull();
    }

    @Test
    public void shouldReturnNullTokenWhenPaymentInfoIsNull() {
        when(orderModel.getUser()).thenReturn(userModel);
        when(orderModel.getPaymentInfo()).thenReturn(null);

        assertThat(novalnetDao.getStoredPaymentToken(orderModel)).isNull();
    }

    @Test
    public void shouldReturnNullTokenWhenNoStoredReferenceFound() {
        stubOrderForToken();
        when(flexibleSearchService.<NovalnetPaymentRefInfoModel>search(any(FlexibleSearchQuery.class)))
                .thenReturn(paymentRefSearchResult);
        when(paymentRefSearchResult.getResult()).thenReturn(Collections.emptyList());

        assertThat(novalnetDao.getStoredPaymentToken(orderModel)).isNull();
    }

    @Test
    public void shouldReturnNullTokenWhenSearchResultIsNull() {
        stubOrderForToken();
        when(flexibleSearchService.<NovalnetPaymentRefInfoModel>search(any(FlexibleSearchQuery.class))).thenReturn(null);

        assertThat(novalnetDao.getStoredPaymentToken(orderModel)).isNull();
    }

    @Test
    public void shouldReturnTokenOfNewestStoredReference() {
        stubOrderForToken();
        when(flexibleSearchService.<NovalnetPaymentRefInfoModel>search(any(FlexibleSearchQuery.class)))
                .thenReturn(paymentRefSearchResult);
        when(paymentRefSearchResult.getResult()).thenReturn(List.of(paymentRefModel));
        when(paymentRefModel.getToken()).thenReturn(TOKEN);

        assertThat(novalnetDao.getStoredPaymentToken(orderModel)).isEqualTo(TOKEN);

        final FlexibleSearchQuery query = captureQuery();
        assertThat(query.getQueryParameters()).containsEntry("customerNo", USER_PK).containsEntry("paymentType", PAYMENT_TYPE);
        assertThat(query.getCount()).isEqualTo(1);
    }

    @Test
    public void shouldLookUpCurrencyByExample() {
        final CurrencyModel found = new CurrencyModel();
        when(flexibleSearchService.getModelByExample(any(CurrencyModel.class))).thenReturn(found);

        assertThat(novalnetDao.getCurrencyForIsoCode(CURRENCY_ISO)).isSameAs(found);

        final ArgumentCaptor<CurrencyModel> captor = ArgumentCaptor.forClass(CurrencyModel.class);
        verify(flexibleSearchService).getModelByExample(captor.capture());
        assertThat(captor.getValue().getIsocode()).isEqualTo(CURRENCY_ISO);
    }

    @Test
    public void shouldReturnLastOrderForUser() {
        when(flexibleSearchService.<OrderModel>search(any(FlexibleSearchQuery.class))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(List.of(orderModel));

        assertThat(novalnetDao.getLastOrderForUser(userModel)).isSameAs(orderModel);

        final FlexibleSearchQuery query = captureQuery();
        assertThat(query.getQueryParameters()).containsEntry("user", userModel);
        assertThat(query.getCount()).isEqualTo(1);
    }

    @Test
    public void shouldReturnNullWhenUserHasNoOrders() {
        when(flexibleSearchService.<OrderModel>search(any(FlexibleSearchQuery.class))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(Collections.emptyList());

        assertThat(novalnetDao.getLastOrderForUser(userModel)).isNull();
    }

    private FlexibleSearchQuery captureQuery() {
        final ArgumentCaptor<FlexibleSearchQuery> captor = ArgumentCaptor.forClass(FlexibleSearchQuery.class);
        verify(flexibleSearchService).search(captor.capture());
        return captor.getValue();
    }

    private void stubCallbackSearch(final List<NovalnetCallbackInfoModel> results) {
        when(flexibleSearchService.<NovalnetCallbackInfoModel>search(argThat(this::isCallbackQuery)))
                .thenReturn(callbackSearchResult);
        when(callbackSearchResult.getResult()).thenReturn(results);
    }

    private void stubOrderSearch(final List<OrderModel> results) {
        when(flexibleSearchService.<OrderModel>search(argThat(this::isOrderQuery))).thenReturn(orderSearchResult);
        when(orderSearchResult.getResult()).thenReturn(results);
    }

    private boolean isCallbackQuery(final FlexibleSearchQuery query) {
        return query != null && query.getQuery().contains("NovalnetCallbackInfo");
    }

    private boolean isOrderQuery(final FlexibleSearchQuery query) {
        return query != null && query.getQuery().contains("Order AS o");
    }

    private void stubOrderForToken() {
        when(orderModel.getUser()).thenReturn(userModel);
        when(userModel.getPk()).thenReturn(PK.fromLong(USER_PK));
        when(orderModel.getPaymentInfo()).thenReturn(paymentInfoModel);
        when(paymentInfoModel.getPaymentProvider()).thenReturn(PAYMENT_TYPE);
    }
}
