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

import de.hybris.platform.acceleratorfacades.flow.impl.SessionOverrideCheckoutFlowFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestRegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.GuestRegisterValidator;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;
import novalnet.novalnetcheckoutaddon.services.NovalnetCheckoutAddonService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping(value = "/checkout/")
public class NovalnetCheckoutController extends AbstractCheckoutController {
    private static final String ORDER_CODE_PATH_VARIABLE_PATTERN = "{orderCode:.*}";
    private static final String CHECKOUT_ORDER_CONFIRMATION_CMS_PAGE_LABEL = "orderConfirmation";

    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;

    @Resource(name = "guestRegisterValidator")
    private GuestRegisterValidator guestRegisterValidator;

    @Resource(name = "novalnetCheckoutAddonService")
    private NovalnetCheckoutAddonService novalnetCheckoutAddonService;

    @ExceptionHandler(ModelNotFoundException.class)
    public String handleModelNotFoundException(final ModelNotFoundException exception, final HttpServletRequest request) {
        request.setAttribute("message", exception.getMessage());
        return FORWARD_PREFIX + "/404";
    }

    @GetMapping
    public String checkout(final RedirectAttributes redirectModel) {
        if (getCheckoutFlowFacade().hasValidCart()) {
            if (validateCart(redirectModel)) {
                return REDIRECT_PREFIX + "/cart";
            } else {
                checkoutFacade.prepareCartForCheckout();
                return getCheckoutRedirectUrl();
            }
        }
        return REDIRECT_PREFIX + "/cart";
    }


    @GetMapping(value = "novalnet/orderConfirmation/" + ORDER_CODE_PATH_VARIABLE_PATTERN)
    @RequireHardLogIn
    public String orderConfirmation(@PathVariable("orderCode") final String orderCode, final HttpServletRequest request, final Model model, final RedirectAttributes redirectModel)
            throws CMSItemNotFoundException {
        SessionOverrideCheckoutFlowFacade.resetSessionOverrides();
        return processOrderCode(orderCode, model, request, redirectModel);
    }

    @PostMapping(value = "novalnet/orderConfirmation/" + ORDER_CODE_PATH_VARIABLE_PATTERN)
    public String orderConfirmation(final GuestRegisterForm form, final BindingResult bindingResult, final Model model,
                                    final HttpServletRequest request, final HttpServletResponse response, final RedirectAttributes redirectModel)
            throws CMSItemNotFoundException {
        guestRegisterValidator.validate(form, bindingResult);
        return processRegisterGuestUserRequest(form, bindingResult, model, request, response, redirectModel);
    }

    protected String processRegisterGuestUserRequest(final GuestRegisterForm form, final BindingResult bindingResult,
                                                     final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                                     final RedirectAttributes redirectModel) throws CMSItemNotFoundException {
        if (bindingResult.hasErrors()) {
            form.setTermsCheck(false);
            GlobalMessages.addErrorMessage(model, "form.global.error");
            return processOrderCode(form.getOrderCode(), model, request, redirectModel);
        }

        return novalnetCheckoutAddonService.registerGuestUser(form, model, request, response, redirectModel);
    }

    protected String processOrderCode(final String orderCode, final Model model, final HttpServletRequest request,
                                      final RedirectAttributes redirectModel) throws CMSItemNotFoundException {

        final OrderData orderDetails;

        try {
            orderDetails = novalnetCheckoutAddonService.getOrderConfirmationDetails(orderCode, model, request);
        } catch (final UnknownIdentifierException e) {
            return REDIRECT_PREFIX + ROOT;
        }

        addRegistrationConsentDataToModel(model);

        if (orderDetails == null) {
            return getCheckoutRedirectUrl();
        }

        final ContentPageModel checkoutOrderConfirmationPage = getContentPageForLabelOrId(
                CHECKOUT_ORDER_CONFIRMATION_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, checkoutOrderConfirmationPage);
        setUpMetaDataForContentPage(model, checkoutOrderConfirmationPage);
        model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);

        return NovalnetcheckoutaddonControllerConstants.Views.Pages.Checkout.CheckoutPage;
    }
}
