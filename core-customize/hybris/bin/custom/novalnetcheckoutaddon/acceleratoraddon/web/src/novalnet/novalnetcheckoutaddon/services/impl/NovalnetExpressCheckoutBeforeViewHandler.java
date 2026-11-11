package novalnet.novalnetcheckoutaddon.services.impl;

import de.hybris.platform.acceleratorfacades.flow.CheckoutFlowFacade;
import de.hybris.platform.addonsupport.interceptors.BeforeViewHandlerAdaptee;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.ui.ModelMap;


public class NovalnetExpressCheckoutBeforeViewHandler implements BeforeViewHandlerAdaptee {

    private CheckoutFlowFacade checkoutFlowFacade;

    public void setCheckoutFlowFacade(final CheckoutFlowFacade checkoutFlowFacade) {
        this.checkoutFlowFacade = checkoutFlowFacade;
    }

    public String beforeView(HttpServletRequest request, HttpServletResponse response, ModelMap model, String viewName) {
        if (viewName != null && viewName.contains("cartPage")) {
            model.addAttribute("expressCheckoutAllowed", checkoutFlowFacade.isExpressCheckoutEnabledForStore());
        }

        return viewName;
    }

}
