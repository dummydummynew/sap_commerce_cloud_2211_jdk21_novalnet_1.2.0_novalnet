package com.novalnet.interceptors;

import com.novalnet.exception.NovalnetInterceptorException;
import com.novalnet.facades.NovalnetTransactionFacade;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.servicelayer.interceptor.InterceptorContext;
import de.hybris.platform.servicelayer.interceptor.InterceptorException;
import de.hybris.platform.servicelayer.interceptor.PrepareInterceptor;
import org.json.JSONException;


/**
 * Interceptor for booking a Novalnet transaction when the book amount of an order is modified.
 * <p>
 * Delegates the transaction booking to the {@link NovalnetTransactionFacade}.
 */
public class TransactionBookingInterceptor implements PrepareInterceptor<OrderModel> {
    private NovalnetTransactionFacade novalnetTransactionFacade;

    /**
     * Sets the Novalnet transaction facade used to book transactions.
     *
     * @param novalnetTransactionFacade the Novalnet transaction facade
     */
    public void setNovalnetTransactionFacade(NovalnetTransactionFacade novalnetTransactionFacade) {
        this.novalnetTransactionFacade = novalnetTransactionFacade;
    }

    /**
     * Prepares the order by booking a Novalnet transaction when the book amount is modified.
     *
     * @param order the order being prepared
     * @param ctx   the interceptor context containing the modification information
     * @throws InterceptorException if an error occurs while preparing the order
     */
    @Override
    public void onPrepare(OrderModel order, InterceptorContext ctx) throws InterceptorException {
        if (!ctx.isModified(order, "bookAmount")) {
            return;
        }

        if (order.getBookAmount() == null) {
            return;
        }

        try {
            novalnetTransactionFacade.bookTransaction(order);
        } catch (final JSONException e) {
            throw new NovalnetInterceptorException("Unable to book Novalnet transaction for Order: " + order.getCode(), e);
        }
    }
}


