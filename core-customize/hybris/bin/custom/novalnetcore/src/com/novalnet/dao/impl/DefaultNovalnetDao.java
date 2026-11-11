/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dao.impl;

import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.order.AbstractOrderModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import jakarta.annotation.Resource;

import java.util.List;


/**
 *
 * Default implementation of {@link NovalnetDao} for accessing Novalnet data.
 */
public class DefaultNovalnetDao implements NovalnetDao {
    private static final int SINGLE_RESULT_COUNT = 1;

    private static final String SELECT_PK_FROM = "SELECT {pk} FROM {";

    private static final String WHERE = "} WHERE {";

    private static final String GET_LAST_ORDER_FOR_USER_QUERY = SELECT_PK_FROM + OrderModel._TYPECODE + WHERE
            + AbstractOrderModel.USER + "} = ?user AND {" + OrderModel.VERSIONID + "} IS NULL ORDER BY {creationtime} DESC";

    private static final String GET_ORDER_INFO_QUERY = SELECT_PK_FROM + OrderModel._TYPECODE + WHERE + AbstractOrderModel.CODE
            + "} = ?code";

    private static final String GET_PAYMENT_REF_INFO_QUERY = SELECT_PK_FROM + NovalnetPaymentRefInfoModel._TYPECODE + WHERE
            + NovalnetPaymentRefInfoModel.CUSTOMERNO + "} = ?customerNo AND {" + NovalnetPaymentRefInfoModel.PAYMENTTYPE
            + "} = ?paymentType ORDER BY {creationtime} DESC";

    private static final String GET_NOVALNET_PAYMENT_INFO_QUERY = SELECT_PK_FROM + "PaymentInfo" + WHERE + PaymentInfoModel.CODE
            + "} = ?code AND {" + PaymentInfoModel.DUPLICATE + "} = ?duplicate";

    private static final String GET_CALLBACK_INFO_QUERY = SELECT_PK_FROM + NovalnetCallbackInfoModel._TYPECODE + WHERE
            + NovalnetCallbackInfoModel.ORGINALTID + "} = ?transactionId";

    private static final String GET_PAYMENT_DETAILS_INFO_QUERY = SELECT_PK_FROM + NovalnetCallbackInfoModel._TYPECODE + WHERE
            + NovalnetCallbackInfoModel.ORDERNO + "} = ?orderNo";

    private static final String GET_ORDER_BY_TID_CALLBACK_QUERY = SELECT_PK_FROM + "NovalnetCallbackInfo AS c" + WHERE
            + "callbackTid} = ?tid OR {orginalTid} = ?tid";

    private static final String GET_ORDER_BY_TID_ORDER_QUERY = SELECT_PK_FROM + "Order AS o" + WHERE + "o.code} = ?orderNo";

    private static final String GET_STORED_PAYMENT_TOKEN_QUERY = SELECT_PK_FROM + "NovalnetPaymentRefInfo" + WHERE
            + "customerNo} = ?customerNo AND {paymentType} = ?paymentType " + "ORDER BY {modifiedtime} DESC";

    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Override
    public List<OrderModel> getOrderInfoModel(String orderCode) {
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(GET_ORDER_INFO_QUERY);

        executeQuery.addQueryParameter("code", orderCode);

        SearchResult<OrderModel> result = flexibleSearchService.search(executeQuery);

        return result.getResult();
    }

    @Override
    public List<NovalnetPaymentRefInfoModel> getPaymentRefInfo(String customerNo, String paymentType) {
        long customerId = Long.parseLong(customerNo);

        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(GET_PAYMENT_REF_INFO_QUERY);

        executeQuery.addQueryParameter("customerNo", customerId);
        executeQuery.addQueryParameter("paymentType", paymentType);

        SearchResult<NovalnetPaymentRefInfoModel> result = flexibleSearchService.search(executeQuery);

        return result.getResult();
    }

    @Override
    public List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode) {
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(GET_NOVALNET_PAYMENT_INFO_QUERY);

        executeQuery.addQueryParameter("code", orderCode);
        executeQuery.addQueryParameter("duplicate", Boolean.FALSE);

        SearchResult<NovalnetPaymentInfoModel> result = flexibleSearchService.search(executeQuery);

        return result.getResult();
    }

    @Override
    public List<NovalnetCallbackInfoModel> getCallbackInfo(String transactionId) {
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(GET_CALLBACK_INFO_QUERY);

        executeQuery.addQueryParameter("transactionId", transactionId);

        SearchResult<NovalnetCallbackInfoModel> result = flexibleSearchService.search(executeQuery);

        return result.getResult();
    }

    @Override
    public List<NovalnetCallbackInfoModel> getPaymentDetailsInfo(String orderCode) {
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(GET_PAYMENT_DETAILS_INFO_QUERY);

        executeQuery.addQueryParameter("orderNo", orderCode);

        SearchResult<NovalnetCallbackInfoModel> result = flexibleSearchService.search(executeQuery);

        return result.getResult();
    }

    @Override
    public OrderModel getOrderByTid(String tid) {
        FlexibleSearchQuery fsCallback = new FlexibleSearchQuery(GET_ORDER_BY_TID_CALLBACK_QUERY);

        fsCallback.addQueryParameter("tid", Long.parseLong(tid));

        SearchResult<NovalnetCallbackInfoModel> callbackResult = flexibleSearchService.search(fsCallback);

        List<NovalnetCallbackInfoModel> callbackInfos = callbackResult.getResult();

        if (callbackInfos.isEmpty()) {
            return null;
        }

        String orderNo = callbackInfos.get(0).getOrderNo();

        if (orderNo == null || orderNo.isEmpty()) {
            return null;
        }

        FlexibleSearchQuery fsOrder = new FlexibleSearchQuery(GET_ORDER_BY_TID_ORDER_QUERY);

        fsOrder.addQueryParameter("orderNo", orderNo);

        SearchResult<OrderModel> orderResult = flexibleSearchService.search(fsOrder);

        List<OrderModel> orders = orderResult.getResult();

        if (orders.isEmpty()) {
            return null;
        }

        return orders.get(0);
    }

    @Override
    public String getStoredPaymentToken(OrderModel order) {
        if (order == null || order.getUser() == null || order.getPaymentInfo() == null) {
            return null;
        }

        long customerNo = order.getUser().getPk().getLongValue();

        NovalnetPaymentInfoModel paymentInfo = (NovalnetPaymentInfoModel) order.getPaymentInfo();

        String paymentType = paymentInfo.getPaymentProvider();

        FlexibleSearchQuery flexibleSearchQuery = new FlexibleSearchQuery(GET_STORED_PAYMENT_TOKEN_QUERY);

        flexibleSearchQuery.addQueryParameter("customerNo", customerNo);
        flexibleSearchQuery.addQueryParameter("paymentType", paymentType);
        flexibleSearchQuery.setCount(1);

        SearchResult<NovalnetPaymentRefInfoModel> searchResult = flexibleSearchService.search(flexibleSearchQuery);

        if (searchResult == null || searchResult.getResult().isEmpty()) {
            return null;
        }

        return searchResult.getResult().get(0).getToken();
    }

    @Override
    public CurrencyModel getCurrencyForIsoCode(String currencyIsoCode) {
        CurrencyModel currencyModel = new CurrencyModel();
        currencyModel.setIsocode(currencyIsoCode);

        currencyModel = flexibleSearchService.getModelByExample(currencyModel);

        return currencyModel;
    }

    @Override
    public OrderModel getLastOrderForUser(UserModel user) {
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(GET_LAST_ORDER_FOR_USER_QUERY);

        executeQuery.addQueryParameter("user", user);
        executeQuery.setCount(SINGLE_RESULT_COUNT);

        SearchResult<OrderModel> result = flexibleSearchService.search(executeQuery);

        List<OrderModel> orders = result.getResult();

        if (orders.isEmpty()) {
            return null;
        }

        return orders.get(0);
    }
}
