/*
 *
 * @author    Novalnet AG
 * @copyright Copyright by Novalnet
 * @license   https://www.novalnet.de/payment-plugins/kostenlos/lizenz
 *
 * If you have found this script useful a small
 * recommendation as well as a comment on merchant form
 * would be greatly appreciated.
 *
 */
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;


import com.novalnet.facades.NovalnetPaymentFacade;
import com.novalnet.util.NovalnetUtils;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.commercefacades.order.data.OrderData;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;


@Controller
@RequestMapping(value = "/")
public class NovalnetHopPaymentResponseController extends NovalnetPaymentMethodCheckoutStepController {

    protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/novalnet/orderConfirmation/";

    @Resource(name = "novalnetPaymentFacade")
    private NovalnetPaymentFacade novalnetPaymentFacade;

    @GetMapping(value = "checkout/multi/novalnet/hop-response")
    @RequireHardLogIn
    public String handleHopResponse(final RedirectAttributes redirectAttributes, final HttpServletRequest request) {

        final Map<String, String> resultMap = getRequestParameterMap(request);

        String transactionSecret = getSessionService().getAttribute("txn_secret");

        if (!"".equals(resultMap.get("checksum")) && !"".equals(resultMap.get("tid")) && !"".equals(transactionSecret)
                && !"".equals(resultMap.get("status"))) {
            String tokenString = resultMap.get("tid") + transactionSecret + resultMap.get("status")
                    + new StringBuilder(getSessionService().getAttribute("txn_check").toString()).reverse().toString();


            String generatedChecksum = NovalnetUtils.generateChecksum(tokenString);

            if (!generatedChecksum.equals(resultMap.get("checksum"))) {
                final String statusMessage = "While redirecting some data has been changed. The hash check failed";
                getSessionService().setAttribute("novalnetCheckoutError", statusMessage);
                return getCheckoutStep().currentStep();
            } else {
                boolean success = novalnetPaymentFacade.processTransaction(resultMap);

                if (!success) {
                    GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.ERROR_MESSAGES_HOLDER,
                            "novalnet.payment.processing.error");
                    return getCheckoutStep().currentStep();
                }

                OrderData orderData = getSessionService().getAttribute("novalnetOrderData");
                return confirmationPageURL(orderData);
            }
        } else {
            final String statusMessage = "While redirecting some data has been changed. The hash check failed";
            getSessionService().setAttribute("novalnetCheckoutError", statusMessage);
            return getCheckoutStep().currentStep();
        }

    }

    protected String confirmationPageURL(final OrderData orderData) {
        return REDIRECT_URL_ORDER_CONFIRMATION
                + (getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode());
    }

}
