/**
 *
 */
package novalnet.novalnetcheckoutaddon.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ConsentForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestRegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.security.AutoLoginStrategy;
import de.hybris.platform.commercefacades.consent.ConsentFacade;
import de.hybris.platform.commercefacades.consent.CustomerConsentDataStrategy;
import de.hybris.platform.commercefacades.consent.data.AnonymousConsentData;
import de.hybris.platform.commercefacades.coupon.data.CouponData;
import de.hybris.platform.commercefacades.customer.CustomerFacade;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.servicelayer.session.SessionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import novalnet.novalnetcheckoutaddon.services.NovalnetCheckoutAddonService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type Default novalnet checkout addon service.
 */
@Service("novalnetCheckoutAddonService")
public class DefaultNovalnetCheckoutAddonService implements NovalnetCheckoutAddonService {
    public static final String REDIRECT_PREFIX = "redirect:";
    public static final String ROOT = "/";
    private static final Logger LOGGER = Logger.getLogger(DefaultNovalnetCheckoutAddonService.class);
    private static final String CONSENT_FORM_GLOBAL_ERROR = "consent.form.global.error";
    private static final String CONTINUE_URL_KEY = "continueUrl";
    private static final String CONSENT_GIVEN = "GIVEN";
    @Resource
    private SessionService sessionService;

    @Resource(name = "customerFacade")
    private CustomerFacade customerFacade;

    @Resource(name = "autoLoginStrategy")
    private AutoLoginStrategy autoLoginStrategy;

    @Resource(name = "consentFacade")
    private ConsentFacade consentFacade;

    @Resource(name = "orderFacade")
    private OrderFacade orderFacade;

    @Resource(name = "productFacade")
    private ProductFacade productFacade;

    @Resource(name = "customerConsentDataStrategy")
    private CustomerConsentDataStrategy customerConsentDataStrategy;

    @Override
    public String registerGuestUser(final GuestRegisterForm form, final Model model, final HttpServletRequest request,
                                    final HttpServletResponse response, final RedirectAttributes redirectModel) {
        try {
            customerFacade.changeGuestToCustomer(form.getPwd(), form.getOrderCode());
            autoLoginStrategy.login(customerFacade.getCurrentCustomer().getUid(), form.getPwd(), request, response);
            sessionService.removeAttribute(WebConstants.ANONYMOUS_CHECKOUT);
        } catch (final DuplicateUidException e) {
            form.setTermsCheck(false);
            model.addAttribute(new GuestRegisterForm());
            GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
                    "guest.checkout.existingaccount.register.error", new Object[]
                            {form.getUid()});
            return REDIRECT_PREFIX + request.getHeader("Referer");
        }

        try {
            final ConsentForm consentForm = form.getConsentForm();
            if (consentForm != null && consentForm.getConsentGiven()) {
                consentFacade.giveConsent(consentForm.getConsentTemplateId(), consentForm.getConsentTemplateVersion());
            }
        } catch (final Exception e) {
            LOGGER.error("Error occurred while creating consents during registration", e);
            GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER, CONSENT_FORM_GLOBAL_ERROR);
        }

        final Cookie cookie = WebUtils.getCookie(request, WebConstants.ANONYMOUS_CONSENT_COOKIE);
        if (cookie != null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                final List<AnonymousConsentData> anonymousConsentDataList = Arrays.asList(mapper.readValue(
                        URLDecoder.decode(cookie.getValue(), StandardCharsets.UTF_8.displayName()), AnonymousConsentData[].class));
                anonymousConsentDataList.stream().filter(consentData -> CONSENT_GIVEN.equals(consentData.getConsentState()))
                        .forEach(consentData -> consentFacade.giveConsent(consentData.getTemplateCode(),
                                Integer.valueOf(consentData.getTemplateVersion())));
            } catch (final UnsupportedEncodingException e) {
                LOGGER.error(String.format("Cookie Data could not be decoded : %s", cookie.getValue()), e);
            } catch (final IOException e) {
                LOGGER.error("Cookie Data could not be mapped into the Object", e);
            } catch (final Exception e) {
                LOGGER.error("Error occurred while creating Anonymous cookie consents", e);
            }
        }

        customerConsentDataStrategy.populateCustomerConsentDataInSession();

        return REDIRECT_PREFIX + "/";
    }

    @Override
    public OrderData getOrderConfirmationDetails(final String orderCode, final Model model, final HttpServletRequest request) {
        final OrderData orderDetails = orderFacade.getOrderDetailsForCode(orderCode);

        if (orderDetails.isGuestCustomer() && !StringUtils.substringBefore(orderDetails.getUser().getUid(), "|")
                .equals(sessionService.getAttribute(WebConstants.ANONYMOUS_CHECKOUT_GUID))) {
            return null;
        }

        if (orderDetails.getEntries() != null && !orderDetails.getEntries().isEmpty()) {
            for (final OrderEntryData entry : orderDetails.getEntries()) {
                final String productCode = entry.getProduct().getCode();
                final ProductData product = productFacade.getProductForCodeAndOptions(productCode,
                        Arrays.asList(ProductOption.BASIC, ProductOption.PRICE, ProductOption.CATEGORIES));
                entry.setProduct(product);
            }
        }

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("orderData", orderDetails);
        model.addAttribute("allItems", orderDetails.getEntries());
        model.addAttribute("deliveryAddress", orderDetails.getDeliveryAddress());
        model.addAttribute("deliveryMode", orderDetails.getDeliveryMode());
        model.addAttribute("paymentInfo", orderDetails.getPaymentInfo());
        model.addAttribute("pageType", PageType.ORDERCONFIRMATION.name());
        model.addAttribute("tid", sessionService.getAttribute("tid"));
        final AddressData addressData = sessionService.getAttribute("novalnetAddressData");
        model.addAttribute("addressDetails", addressData);

        final List<CouponData> giftCoupons = orderDetails.getAppliedOrderPromotions().stream()
                .filter(x -> CollectionUtils.isNotEmpty(x.getGiveAwayCouponCodes())).flatMap(p -> p.getGiveAwayCouponCodes().stream())
                .collect(Collectors.toList());
        model.addAttribute("giftCoupons", giftCoupons);

        processEmailAddress(model, orderDetails);

        final String continueUrl = (String) sessionService.getAttribute(WebConstants.CONTINUE_URL);
        model.addAttribute(CONTINUE_URL_KEY, (continueUrl != null && !continueUrl.isEmpty()) ? continueUrl : ROOT);

        return orderDetails;
    }

    @Override
    public void processEmailAddress(final Model model, final OrderData orderDetails) {
        final String uid;

        if (orderDetails.isGuestCustomer() && !model.containsAttribute("guestRegisterForm")) {
            final GuestRegisterForm guestRegisterForm = new GuestRegisterForm();
            guestRegisterForm.setOrderCode(orderDetails.getGuid());
            uid = sessionService.getAttribute("email");
            guestRegisterForm.setUid(uid);
            model.addAttribute(guestRegisterForm);
        } else {
            uid = orderDetails.getUser().getUid();
        }
        model.addAttribute("email", uid);
    }

}
