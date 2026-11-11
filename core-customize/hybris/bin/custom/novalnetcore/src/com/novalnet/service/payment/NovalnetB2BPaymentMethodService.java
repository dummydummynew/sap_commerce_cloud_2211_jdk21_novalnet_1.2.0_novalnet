package com.novalnet.service.payment;

import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.model.NovalnetReplPayRefInfoModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.core.model.order.CartModel;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;

/**
 * The interface Novalnet b 2 b payment method service.
 */
public interface NovalnetB2BPaymentMethodService {

    /**
     * Add payment process novalnet payment details form.
     *
     * @param model                      the model
     * @param cartData                   the cart data
     * @param novalnetPaymentDetailsForm the novalnet payment details form
     * @return the novalnet payment details form
     */
    NovalnetPaymentDetailsForm addPaymentProcess(Model model, CartData cartData, NovalnetPaymentDetailsForm novalnetPaymentDetailsForm);

    /**
     * Create payment request string.
     *
     * @param paymentDetailsForm the payment details form
     * @param addressData        the address data
     * @param cartData           the cart data
     * @param request            the request
     * @return the string
     */
    String createPaymentRequest(NovalnetPaymentDetailsForm paymentDetailsForm, AddressData addressData, CartData cartData, HttpServletRequest request);

    /**
     * Creates the Novalnet request used to charge a saved token for a replenishment order (B2B).
     *
     * @param cart             the cart
     * @param paymentReference the payment reference
     * @return the string
     */
    String createPaymentRequest(CartModel cart, NovalnetReplPayRefInfoModel paymentReference);
}
