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


import com.novalnet.dto.NovalnetPaymentDetailsForm;
import com.novalnet.dto.NovalnetPaymentInfoData;
import com.novalnet.dto.payment.Billing;
import com.novalnet.dto.payment.Customer;
import com.novalnet.dto.payment.Shipping;
import com.novalnet.facades.NovalnetPaymentFacade;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.enums.CountryType;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.servicelayer.session.SessionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collection;


@Controller
@RequestMapping(value = "/checkout/multi/novalnet/select-payment-method")
public class NovalnetPaymentMethodCheckoutStepController extends AbstractCheckoutStepController {
    private static final String PAYMENT_METHOD = "payment-method";
    private static final String BILLING_COUNTRIES = "billingCountries";
    private static final String CART_DATA_ATTR = "cartData";

    @Resource(name = "novalnetPaymentFacade")
    private NovalnetPaymentFacade novalnetPaymentFacade;

    @Resource
    private SessionService sessionService;

    @ModelAttribute("billingCountries")
    public Collection<CountryData> getBillingCountries() {
        return getCheckoutFacade().getCountries(CountryType.BILLING);
    }

    @Override
    @GetMapping(value = "/add")
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getCheckoutFacade().setDeliveryModeIfAvailable();
        model.addAttribute(BILLING_COUNTRIES, getCheckoutFacade().getBillingCountries());
        String errorMessage = getSessionService().getAttribute("novalnetCheckoutError");
        if (errorMessage != null) {
            GlobalMessages.addErrorMessage(model, errorMessage);
        }
        getSessionService().setAttribute("novalnetCheckoutError", null);
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        setCheckoutStepLinksForModel(model, getCheckoutStep());
        setupAddPaymentPage(model);
        NovalnetPaymentDetailsForm paymentDetailsForm = new NovalnetPaymentDetailsForm();
        setupNovalnetPaymentPostPage(paymentDetailsForm, model);
        novalnetPaymentFacade.addPaymentProcess(model, paymentDetailsForm, cartData);
        return NovalnetcheckoutaddonControllerConstants.AddPaymentMethodPage;

    }

    private void setupNovalnetPaymentPostPage(final NovalnetPaymentDetailsForm paymentDetailsForm, final Model model) {
        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute("commonPaymentDetailsForm", new NovalnetPaymentDetailsForm());
        model.addAttribute("hasNoPaymentInfo", Boolean.valueOf(getCheckoutFlowFacade().hasNoPaymentInfo()));
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("paymentDetailsForm", paymentDetailsForm);
        if (StringUtils.isNotBlank(paymentDetailsForm.getBillToCountry())) {
            model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(paymentDetailsForm.getBillToCountry()));
            model.addAttribute("country", paymentDetailsForm.getBillToCountry());
        }
    }

    @PostMapping(value =
            {"/add"})
    @RequireHardLogIn
    public String add(Model model, @Valid
    NovalnetPaymentDetailsForm paymentDetailsForm, BindingResult bindingResult) throws CMSItemNotFoundException {
        setupAddPaymentPage(model);

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute(CART_DATA_ATTR, cartData);
        String selectedPaymentMethod = "";
        selectedPaymentMethod = paymentDetailsForm.getSelectedPaymentMethodId();
        AddressData addressData = new AddressData();

        Customer customer = new Customer();
        String guestEmail = novalnetPaymentFacade.getGuestEmail();
        final String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();
        customer.setEmail(emailAddress);

        getAddressVerificationFacade().verifyAddressData(addressData);
        sessionService.setAttribute("novalnetAddressData", addressData);
        NovalnetPaymentInfoData paymentInfoData = new NovalnetPaymentInfoData();
        paymentInfoData.setBillingAddress(addressData);
        sessionService.setAttribute("selectedPaymentMethodId", selectedPaymentMethod);

        String currentPayment = sessionService.getAttribute("selectedPaymentMethodId");

        if (Boolean.TRUE.equals(paymentDetailsForm.isUseDeliveryAddress())) {
            addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();

            if (addressData == null) {
                GlobalMessages.addErrorMessage(model,
                        "checkout.multi.paymentMethod.createSubscription.billingAddress.noneSelectedMsg");
                return getCheckoutStep().currentStep();
            }
        }
        novalnetPaymentFacade.populateCustomerAddressDetails(model, paymentDetailsForm, cartData, customer, addressData,
                new Billing(), new Shipping());

        boolean success = novalnetPaymentFacade.processOneClickTokenData(currentPayment, paymentDetailsForm, model, cartData,
                addressData);

        if (!success) {
            return getCheckoutStep().currentStep();
        }
        return getCheckoutStep().nextStep();
    }

    @PostMapping(value = "/remove")
    @RequireHardLogIn
    public String remove(@RequestParam(value = "paymentInfoId") final String paymentMethodId, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getUserFacade().unlinkCCPaymentInfo(paymentMethodId);
        GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
                "text.account.profile.paymentCart.removed");
        return getCheckoutStep().currentStep();
    }

    @GetMapping(value = "/choose")
    @RequireHardLogIn
    public String doSelectPaymentMethod(@RequestParam("selectedPaymentMethodId") final String selectedPaymentMethodId) {
        if (StringUtils.isNotBlank(selectedPaymentMethodId)) {
            getCheckoutFacade().setPaymentDetails(selectedPaymentMethodId);
        }
        return getCheckoutStep().nextStep();
    }

    @GetMapping(value = "/back")
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    @GetMapping(value = "/next")
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException {
        model.addAttribute("metaRobots", "noindex,nofollow");
        model.addAttribute("hasNoPaymentInfo", Boolean.valueOf(getCheckoutFlowFacade().hasNoPaymentInfo()));
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.breadcrumb"));
        final ContentPageModel contentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }

    @GetMapping(value = "/billingaddressform")
    public String getCountryAddressForm(@RequestParam("countryIsoCode") final String countryIsoCode, @RequestParam("useDeliveryAddress") final boolean useDeliveryAddress, final Model model) {
        model.addAttribute("supportedCountries", getCountries());
        model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(countryIsoCode));
        model.addAttribute("country", countryIsoCode);

        final NovalnetPaymentDetailsForm novalnetPaymentDetailsForm = new NovalnetPaymentDetailsForm();
        model.addAttribute("novalnetPaymentDetailsForm", novalnetPaymentDetailsForm);
        if (useDeliveryAddress) {
            final AddressData deliveryAddress = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();

            if (deliveryAddress.getRegion() != null && !StringUtils.isEmpty(deliveryAddress.getRegion().getIsocode())) {
                novalnetPaymentDetailsForm.setBillTo_state(deliveryAddress.getRegion().getIsocodeShort());
            }

            novalnetPaymentDetailsForm.setBillTo_titleCode(deliveryAddress.getTitleCode());
            novalnetPaymentDetailsForm.setBillTo_firstName(deliveryAddress.getFirstName());
            novalnetPaymentDetailsForm.setBillTo_lastName(deliveryAddress.getLastName());
            novalnetPaymentDetailsForm.setBillTo_street1(deliveryAddress.getLine1());
            novalnetPaymentDetailsForm.setBillTo_street2(deliveryAddress.getLine2());
            novalnetPaymentDetailsForm.setBillTo_city(deliveryAddress.getTown());
            novalnetPaymentDetailsForm.setBillTo_postalCode(deliveryAddress.getPostalCode());
            novalnetPaymentDetailsForm.setBillTo_country(deliveryAddress.getCountry().getIsocode());
            novalnetPaymentDetailsForm.setBillTo_phoneNumber(deliveryAddress.getPhone());
        }
        return NovalnetcheckoutaddonControllerConstants.BillingAddressForm;
    }

    @PostMapping(value = "/remove-saved-payment")
    @ResponseBody
    @RequireHardLogIn
    public boolean removeSavedPayment(@RequestParam("paymentType") final String paymentType, @RequestParam("cardIndex") final String cardIndex) {
        return novalnetPaymentFacade.removeSavedPaymentData(paymentType, cardIndex);
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(PAYMENT_METHOD);
    }

}
