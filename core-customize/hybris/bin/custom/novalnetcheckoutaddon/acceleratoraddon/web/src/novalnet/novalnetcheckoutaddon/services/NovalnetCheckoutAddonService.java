/**
 *
 */
package novalnet.novalnetcheckoutaddon.services;

import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestRegisterForm;
import de.hybris.platform.commercefacades.order.data.OrderData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


/**
 * The interface Novalnet checkout addon service.
 */
public interface NovalnetCheckoutAddonService {
    /**
     * Register guest user string.
     *
     * @param form          the form
     * @param model         the model
     * @param request       the request
     * @param response      the response
     * @param redirectModel the redirect model
     * @return the string
     */
    String registerGuestUser(GuestRegisterForm form, Model model, HttpServletRequest request, HttpServletResponse response,
                             RedirectAttributes redirectModel);

    /**
     * Gets order confirmation details.
     *
     * @param orderCode the order code
     * @param model     the model
     * @param request   the request
     * @return the order confirmation details
     */
    OrderData getOrderConfirmationDetails(String orderCode, Model model, HttpServletRequest request);

    /**
     * Process email address.
     *
     * @param model        the model
     * @param orderDetails the order details
     */
    void processEmailAddress(Model model, OrderData orderDetails);

}
