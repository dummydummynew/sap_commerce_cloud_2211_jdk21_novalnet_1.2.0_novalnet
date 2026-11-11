/*

 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.

 */


package com.novalnet.service.payment.impl;


import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.payment.NovalnetCoreService;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.CustomerModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.servicelayer.search.FlexibleSearchQuery;
import de.hybris.platform.servicelayer.search.FlexibleSearchService;
import de.hybris.platform.servicelayer.search.SearchResult;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import jakarta.annotation.Resource;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.List;


/**
 *
 * Handle the core Novalnet payment methods
 *
 */

@Service
public class DefaultNovalnetCoreService implements NovalnetCoreService {

    private static final Integer SECONDS = 60;

    private static final Logger LOGGER = Logger.getLogger(DefaultNovalnetCoreService.class);

    @Resource
    private CartService cartService;


    @Resource
    private FlexibleSearchService flexibleSearchService;

    @Resource
    private ModelService modelService;

    @Resource
    private BaseStoreService baseStoreService;


    @Resource
    private NovalnetDao novalnetDao;

    /**
     *
     * @return the current base store details
     *
     */
    @Override
    public BaseStoreModel getCurrentBaseStore() {
        return baseStoreService.getCurrentBaseStore();
    }


    /**
     *
     * Get order model
     *
     * @param orderCode Order code of the order
     * @return SearchResult
     *
     */
    public List<OrderModel> getOrderInfoModel(String orderCode) {
        return novalnetDao.getOrderInfoModel(orderCode);
    }

    /**
     *
     * Get Payment model
     *
     * @param paymentInfo info of the payment
     * @return paymentModel
     *
     */
    public NovalnetPaymentInfoModel getPaymentModel(final List<NovalnetPaymentInfoModel> paymentInfo) {
        return modelService.get(paymentInfo.get(0).getPk());
    }


    /**
     *
     * Save data in database
     *
     * @param orderCode Order code of the order
     * @return orderModel
     *
     */
    public OrderModel getOrder(String orderCode) {
        List<OrderModel> orderInfoModel = getOrderInfoModel(orderCode);
        return modelService.get(orderInfoModel.get(0).getPk());
    }


    /**
     *
     * Get BaseStore Model
     *
     * @return BaseStore configuration
     *
     */
    public BaseStoreModel getBaseStoreModel() {
        return baseStoreService.getCurrentBaseStore();
    }


    /**
     *
     * @param flexibleSearchService the flexibleSearchService to set
     *
     */
    public void setFlexibleSearchService(final FlexibleSearchService flexibleSearchService) {
        this.flexibleSearchService = flexibleSearchService;
    }


    /**
     *
     * Get Novalnet payment info model
     *
     * @param orderCode Order code of the order
     * @return SearchResult
     *
     */
    public List<NovalnetPaymentInfoModel> getNovalnetPaymentInfo(String orderCode) {
        String query = "SELECT {pk} from {PaymentInfo} where {" + PaymentInfoModel.CODE + "} = ?code AND {" + PaymentInfoModel.DUPLICATE + "} = ?duplicate";
        FlexibleSearchQuery executeQuery = new FlexibleSearchQuery(query);
        executeQuery.addQueryParameter("code", orderCode);
        executeQuery.addQueryParameter("duplicate", Boolean.TRUE);
        SearchResult<NovalnetPaymentInfoModel> result = flexibleSearchService.search(executeQuery);
        return result.getResult();
    }


    /**
     *
     * Is Guest User.
     *
     * @return Boolean
     *
     */
    public Boolean isGuestUser() {
        final CartModel cart = cartService.getSessionCart();
        final UserModel user = cart.getUser();
        return user instanceof CustomerModel customerModel && customerModel.getType() == CustomerType.GUEST;
    }

    /**
     *
     * Get callback info model
     *
     * @param transactionId Transaction ID of the order
     * @return SearchResult
     *
     */

    public List<NovalnetCallbackInfoModel> getCallbackInfo(String transactionId) {
        return novalnetDao.getCallbackInfo(transactionId);
    }

    /**
     *
     * @param url
     * @param jsonBody
     * @param baseStore
     * @return
     *
     */
    public String getOrderDetails(String url, String jsonBody, BaseStoreModel baseStore) {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(SECONDS)).build();
        HttpResponse<String> response;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(SECONDS))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Charset", "utf-8")
                .header("X-NN-Access-Key", Base64.getEncoder().encodeToString(baseStore.getNovalnetPaymentAccessKey().getBytes()))
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            LOGGER.log(Level.WARN, "Interrupted!", e);
            Thread.currentThread().interrupt();
            throw new RuntimeException(e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return response.body();
    }

    public List<NovalnetCallbackInfoModel> getPaymentDetailsInfo(String orderCode) {
        return novalnetDao.getPaymentDetailsInfo(orderCode);
    }


    public OrderModel getOrderByTid(String tid) {
        List<NovalnetCallbackInfoModel> callbackInfos = novalnetDao.getCallbackInfo(tid);
        if (callbackInfos.isEmpty()) {
            return null;
        }
        String orderNo = callbackInfos.get(0).getOrderNo();
        if (orderNo == null || orderNo.isEmpty()) {
            return null;
        }
        List<OrderModel> orders = novalnetDao.getOrderInfoModel(orderNo);
        if (orders.isEmpty()) {
            return null;
        }
        return orders.get(0);
    }

    public void setModelService(final ModelService modelService) {
        this.modelService = modelService;
    }

}
