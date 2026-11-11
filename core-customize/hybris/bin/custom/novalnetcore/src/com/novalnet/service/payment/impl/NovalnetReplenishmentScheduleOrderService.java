package com.novalnet.service.payment.impl;

import com.novalnet.dao.NovalnetReplenishmentDao;
import com.novalnet.model.NovalnetPaymentRefInfoModel;
import de.hybris.platform.b2bacceleratorservices.orderscheduling.impl.B2BAcceleratorScheduleOrderService;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.order.payment.PaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.cronjob.model.TriggerModel;
import de.hybris.platform.orderscheduling.model.CartToOrderCronJobModel;
import de.hybris.platform.servicelayer.session.SessionService;
import de.hybris.platform.servicelayer.user.AddressService;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Set;

/**
 * Creates the standard schedule and snapshots the selected Novalnet token against the
 * generated replenishment CronJob code.
 */
public class NovalnetReplenishmentScheduleOrderService extends B2BAcceleratorScheduleOrderService {
    private static final String SELECTED_PAYMENT_REFERENCE_PK = "novalnetSelectedPaymentReferencePkForReplenishment";
    private static final Set<String> TOKENIZED_PAYMENT_TYPES = Set.of("novalnetCreditCard", "novalnetDirectDebitSepa", "novalnetGuaranteedDirectDebitSepa", "novalnetDirectDebitAch");

    @Resource
    private SessionService sessionService;
    @Resource
    private NovalnetReplenishmentPaymentReferenceService novalnetReplenishmentPaymentReferenceService;
    @Resource
    private NovalnetReplenishmentDao novalnetReplenishmentDao;
    @Resource
    private AddressService addressService;

    @Override
    public CartToOrderCronJobModel createOrderFromCartCronJob(final CartModel cart, final AddressModel deliveryAddress,
                                                              final AddressModel paymentAddress, final PaymentInfoModel paymentInfo, final List<TriggerModel> triggers) {

        final String paymentReferencePk = getSessionService().getAttribute(SELECTED_PAYMENT_REFERENCE_PK);
        if (paymentReferencePk == null || paymentReferencePk.isBlank()) {
            final CartToOrderCronJobModel cronJob = super.createOrderFromCartCronJob(cart, deliveryAddress, paymentAddress,
                    paymentInfo, triggers);
            snapshotAddresses(cronJob, deliveryAddress, paymentAddress);
            return cronJob;
        }

        final NovalnetPaymentRefInfoModel paymentReference = findPaymentReference(cart, paymentReferencePk);
        if (paymentReference == null || paymentReference.getToken() == null || paymentReference.getToken().isBlank()
                || !TOKENIZED_PAYMENT_TYPES.contains(paymentReference.getPaymentType())) {
            throw new IllegalArgumentException("The selected Novalnet payment reference is invalid or does not belong to the scheduled cart customer");
        }

        final CartToOrderCronJobModel cronJob = super.createOrderFromCartCronJob(cart, deliveryAddress, paymentAddress,
                paymentInfo, triggers);
        snapshotAddresses(cronJob, deliveryAddress, paymentAddress);
        getNovalnetReplenishmentPaymentReferenceService().createPaymentReference(cronJob, paymentReference);
        getSessionService().removeAttribute(SELECTED_PAYMENT_REFERENCE_PK);
        return cronJob;
    }

    protected void snapshotAddresses(final CartToOrderCronJobModel cronJob, final AddressModel deliveryAddress,
                                     final AddressModel paymentAddress) {
        final CartModel scheduledCart = cronJob.getCart();
        if (deliveryAddress != null) {
            final AddressModel scheduledDeliveryAddress = addressService.cloneAddressForOwner(deliveryAddress, scheduledCart);
            scheduledCart.setDeliveryAddress(scheduledDeliveryAddress);
            cronJob.setDeliveryAddress(scheduledDeliveryAddress);
        }
        if (paymentAddress != null) {
            final AddressModel scheduledPaymentAddress = addressService.cloneAddressForOwner(paymentAddress, scheduledCart);
            scheduledPaymentAddress.setBillingAddress(Boolean.TRUE);
            scheduledCart.setPaymentAddress(scheduledPaymentAddress);
            cronJob.setPaymentAddress(scheduledPaymentAddress);
        }
        getModelService().saveAll(scheduledCart, cronJob);
    }

    protected NovalnetPaymentRefInfoModel findPaymentReference(final CartModel cart, final String paymentReferencePk) {
        final PK referencePk;
        try {
            referencePk = PK.fromLong(Long.parseLong(paymentReferencePk));
        } catch (final NumberFormatException e) {
            throw new IllegalArgumentException("The selected Novalnet payment reference PK is invalid", e);
        }
        return novalnetReplenishmentDao.findPaymentReference(cart, referencePk);
    }

    protected SessionService getSessionService() {
        return sessionService;
    }

    public void setSessionService(final SessionService sessionService) {
        this.sessionService = sessionService;
    }

    protected NovalnetReplenishmentPaymentReferenceService getNovalnetReplenishmentPaymentReferenceService() {
        return novalnetReplenishmentPaymentReferenceService;
    }

    public void setNovalnetReplenishmentPaymentReferenceService(
            final NovalnetReplenishmentPaymentReferenceService novalnetReplenishmentPaymentReferenceService) {
        this.novalnetReplenishmentPaymentReferenceService = novalnetReplenishmentPaymentReferenceService;
    }
}
