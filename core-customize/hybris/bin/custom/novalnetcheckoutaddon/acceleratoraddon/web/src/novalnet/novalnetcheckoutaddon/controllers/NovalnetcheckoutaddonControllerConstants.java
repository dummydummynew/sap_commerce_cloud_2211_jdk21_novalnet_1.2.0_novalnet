/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.controllers;

/**
 *
 */
public interface NovalnetcheckoutaddonControllerConstants {
    public static final String ADDON_PREFIX = "addon:/novalnetcheckoutaddon";
    public static String AddPaymentMethodPage = ADDON_PREFIX + "/pages/checkout/multi/addPaymentMethodPage"; // NOSONAR
    public static String CheckoutSummaryPage = ADDON_PREFIX + "/pages/checkout/multi/checkoutSummaryPage"; // NOSONAR
    String AddEditDeliveryAddressPage = ADDON_PREFIX + "pages/checkout/multi/addEditDeliveryAddressPage"; // NOSONAR
    public static String ChooseDeliveryMethodPage = ADDON_PREFIX + "pages/checkout/multi/chooseDeliveryMethodPage"; // NOSONAR
    public static String ChoosePickupLocationPage = ADDON_PREFIX + "pages/checkout/multi/choosePickupLocationPage"; // NOSONAR
    public static String HostedOrderPageErrorPage = ADDON_PREFIX + "pages/checkout/multi/hostedOrderPageErrorPage"; // NOSONAR
    public static String SilentOrderPostPage = ADDON_PREFIX + "pages/checkout/multi/silentOrderPostPage"; // NOSONAR
    public static String GiftWrapPage = ADDON_PREFIX + "pages/checkout/multi/giftWrapPage"; // NOSONAR
    public static String BillingAddressForm = "fragments/checkout/billingAddressForm";

    interface Views {

        interface Pages {

            interface Checkout // NOSONAR
            {
                public static String CheckoutPage = ADDON_PREFIX + "/pages/checkout/multi/checkoutConfirmationPage"; // NOSONAR
            }

            interface Account {
                public static String AccountPage = ADDON_PREFIX + "/pages/account/accountLayoutPage";
            }
        }
    }
}
