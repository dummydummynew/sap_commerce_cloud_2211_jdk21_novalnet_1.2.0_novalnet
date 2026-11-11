package com.novalnet.process.actions;

import com.novalnet.model.NovalnetPaymentInfoModel;
import com.novalnet.service.order.NovalnetOrderService;
import de.hybris.platform.b2bacceleratorservices.model.process.ReplenishmentProcessModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.processengine.action.AbstractProceduralAction;
import de.hybris.platform.processengine.model.BusinessProcessParameterModel;
import jakarta.annotation.Resource;

import java.util.List;

/**
 * Applies the configured Novalnet payment/order status after SAP Commerce has created the order.
 */
public class NovalnetReplenishmentStatusAction extends AbstractProceduralAction<ReplenishmentProcessModel> {
    @Resource
    private NovalnetOrderService novalnetOrderService;

    @Override
    public void executeAction(final ReplenishmentProcessModel process) {
        final BusinessProcessParameterModel orderParameter = getProcessParameterHelper()
                .getProcessParameterByName(process, "order");
        final OrderModel order = (OrderModel) orderParameter.getValue();
        final BusinessProcessParameterModel cartParameter = getProcessParameterHelper()
                .getProcessParameterByName(process, "cart");
        final CartModel cart = (CartModel) cartParameter.getValue();
        modelService.refresh(order);
        modelService.refresh(cart);

        if (!(order.getPaymentInfo() instanceof NovalnetPaymentInfoModel)) {
            throw new IllegalStateException("Novalnet payment information was not copied to replenishment order " + order.getCode());
        }

        final NovalnetPaymentInfoModel paymentInfo = (NovalnetPaymentInfoModel) order.getPaymentInfo();
        final AddressModel billingAddress = order.getPaymentAddress();
        if (billingAddress == null) {
            throw new IllegalStateException("Billing address was not copied to replenishment order " + order.getCode());
        }

        final List<PaymentTransactionModel> paymentTransactions = order.getPaymentTransactions();
        if (paymentTransactions == null || paymentTransactions.isEmpty()) {
            throw new IllegalStateException("Novalnet payment transaction was not copied to replenishment order " + order.getCode());
        }

        paymentInfo.setBillingAddress(billingAddress);
        paymentInfo.setCode(order.getCode());
        for (final PaymentTransactionModel paymentTransaction : paymentTransactions) {
            paymentTransaction.setOrder(order);
            paymentTransaction.setInfo(paymentInfo);
            paymentTransaction.setPaymentProvider(paymentInfo.getPaymentProvider());
        }

        order.setPaymentInfo(paymentInfo);
        order.setPaymentAddress(billingAddress);
        order.setPaymentMode(cart.getPaymentMode());
        order.setStatusInfo(paymentInfo.getOrderHistoryNotes());
        modelService.saveAll(paymentInfo, billingAddress, order);
        modelService.saveAll(paymentTransactions);
        novalnetOrderService.updateOrderStatus(order.getCode(), paymentInfo);
    }
}
