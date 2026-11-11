/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

ACC.novalnetcheckoutaddon = {

    spinner: $("<img id='novalnetLoading' src='" + ACC.config.commonResourcePath + "/images/spinner.gif' />"),
    WALLET_CONTAINER: '#wallet_container',
    PREVIOUS_SELECTED_PAYMENT: '#previousSelectedPayment',
    ORDER_BILLING_ADDRESS: '.order-billing-address',
    NOVALNET_ADDRESS_DATA: '.novalnetaddressdata',
    VALUE_ORDER: '.value-order',
    GUARANTEE_ACCOUNT_BIC: '#guaranteeAccountBic',
    ACCOUNT_BIC: '#accountBic',
    ACCOUNT_IBAN: '#accountIban',
    CSS_TEXT_TRANSFORM: 'text-transform',
    GUARANTEE_ACCOUNT_IBAN: '#guaranteeAccountIban',
    SEPA_GUARANTEE_CHECK: '#sepaGuaranteeCheck',
    USE_DELIVERY_ADDRESS: '#useDeliveryAddress',
    GUARANTEED_DIRECT_DEBIT_SEPA: '.novalnetGuaranteedDirectDebitSepa',
    NOT_CHECKED: ':not(:checked)',
    SHIP_COUNTRY: '#ship_country',
    SEPA_FORCE_GUARANTEE_CHECK: '#sepaforceGuaranteeCheck',
    NOVALNET_SEPA: '.novalnetSepa',
    NOVALNET_DIRECT_DEBIT_SEPA_ID: '#novalnetDirectDebitSepa',
    GUARANTEED_DIRECT_DEBIT_SEPA_ID: '#novalnetGuaranteedDirectDebitSepa',
    INVOICE_GUARANTEE_CHECK: '#invoiceGuaranteeCheck',
    GUARANTEED_INVOICE: '.novalnetGuaranteedInvoice',
    INVOICE_FORCE_GUARANTEE_CHECK: '#invoiceforceGuaranteeCheck',
    NOVALNET_INVOICE: '.novalnetInvoice',
    NOVALNET_INVOICE_ID: '#novalnetInvoice',
    NOVALNET_APPLE_PAY: '#novalnetApplePay',
    NOVALNET_DIRECT_DEBIT_ACH: '.novalnetDirectDebitAch',
    NOVALNET_CREDIT_CARD: '#novalnetCreditCard',
    NOVALNET_CREDIT_CARD_PAN_HASH: '#novalnetCreditCardPanHash',
    CREDIT_CARD_IFRAME_ID: '#novalnetCreditCardIframe',
    CREDIT_CARD_IFRAME: '.novalnetCreditCardIframe',
    PAYMENT_DETAILS_FORM: '#paymentDetailsForm',
    NOVALNET_ACH_ACCOUNT_HOLDER: '#novalnetAchAccountHolder',
    NOVALNET_ACH_ACCOUNT_NUMBER: '#novalnetAchAccountNumber',
    NOVALNET_ACH_ROUTING_NUMBER: '#novalnetAchRoutingNumber',
    DIRECT_DEBIT_SEPA_DATE_OF_BIRTH: '#novalnetDirectDebitSepaDateOfBirth',
    NOVALNET_DIRECT_DEBIT_ACH_ID: '#novalnetDirectDebitAch',
    CLIENT_KEY: '#Clientkey',
    NOVALNET_TEST_MODE: '#novalnetTestMode',
    CHALLENGE_WINDOW_OVERLAY: 'novalnet-challenge-window-overlay',
    NOVALNET_COMMENTS: '#novalnetComments',
    ORDER_PAYMENT_BOX: '.orderBox.payment',
    CANCEL_ORDER_FORM: '#cancelorderForm',
    ORDER_PAYMENT_DATA: '.order-payment-data',
    FLAG_TRUE: 'true',
    FLAG_FALSE: 'false',
    STATUS_SUCCESS: 'SUCCESS',
    STATUS_FAILURE: 'FAILURE',

    getGooglePayLocale: function () {
        var lang = ($('#lang').val() || 'en').toLowerCase();
        var country = ($("#countryCode").val() || 'US').toUpperCase();
        return lang + '-' + country;
    },

    initialProcess: function () { // NOSONAR
        var self = ACC.novalnetcheckoutaddon;
        var WALLET_CONTAINER = self.WALLET_CONTAINER;
        var PREVIOUS_SELECTED_PAYMENT = self.PREVIOUS_SELECTED_PAYMENT;
        var ORDER_BILLING_ADDRESS = self.ORDER_BILLING_ADDRESS;
        var NOVALNET_ADDRESS_DATA = self.NOVALNET_ADDRESS_DATA;
        var VALUE_ORDER = self.VALUE_ORDER;
        var GUARANTEE_ACCOUNT_BIC = self.GUARANTEE_ACCOUNT_BIC;
        var ACCOUNT_BIC = self.ACCOUNT_BIC;
        var ACCOUNT_IBAN = self.ACCOUNT_IBAN;
        var CSS_TEXT_TRANSFORM = self.CSS_TEXT_TRANSFORM;
        var GUARANTEE_ACCOUNT_IBAN = self.GUARANTEE_ACCOUNT_IBAN;
        var SEPA_GUARANTEE_CHECK = self.SEPA_GUARANTEE_CHECK;
        var USE_DELIVERY_ADDRESS = self.USE_DELIVERY_ADDRESS;
        var GUARANTEED_DIRECT_DEBIT_SEPA = self.GUARANTEED_DIRECT_DEBIT_SEPA;
        var NOT_CHECKED = self.NOT_CHECKED;
        var SHIP_COUNTRY = self.SHIP_COUNTRY;
        var SEPA_FORCE_GUARANTEE_CHECK = self.SEPA_FORCE_GUARANTEE_CHECK;
        var NOVALNET_SEPA = self.NOVALNET_SEPA;
        var NOVALNET_DIRECT_DEBIT_SEPA_ID = self.NOVALNET_DIRECT_DEBIT_SEPA_ID;
        var GUARANTEED_DIRECT_DEBIT_SEPA_ID = self.GUARANTEED_DIRECT_DEBIT_SEPA_ID;
        var INVOICE_GUARANTEE_CHECK = self.INVOICE_GUARANTEE_CHECK;
        var GUARANTEED_INVOICE = self.GUARANTEED_INVOICE;
        var INVOICE_FORCE_GUARANTEE_CHECK = self.INVOICE_FORCE_GUARANTEE_CHECK;
        var NOVALNET_INVOICE = self.NOVALNET_INVOICE;
        var NOVALNET_INVOICE_ID = self.NOVALNET_INVOICE_ID;
        var NOVALNET_APPLE_PAY = self.NOVALNET_APPLE_PAY;
        var NOVALNET_DIRECT_DEBIT_ACH = self.NOVALNET_DIRECT_DEBIT_ACH;
        var NOVALNET_CREDIT_CARD = self.NOVALNET_CREDIT_CARD;
        var NOVALNET_CREDIT_CARD_PAN_HASH = self.NOVALNET_CREDIT_CARD_PAN_HASH;
        var CREDIT_CARD_IFRAME_ID = self.CREDIT_CARD_IFRAME_ID;
        var CREDIT_CARD_IFRAME = self.CREDIT_CARD_IFRAME;
        var PAYMENT_DETAILS_FORM = self.PAYMENT_DETAILS_FORM;
        var STATUS_SUCCESS = self.STATUS_SUCCESS;
        var STATUS_FAILURE = self.STATUS_FAILURE;
        var FLAG_TRUE = self.FLAG_TRUE;

        if ($(WALLET_CONTAINER).length) {
            var merchantInformation = {
                countryCode: $("#countryCode").val(),
                partnerId: $("#merchantId").val()
            };

            var transactionInformation = {
                amount: $("#orderAmount").val(),
                enforce3d: ($("#enforce3D").val() === FLAG_TRUE) ? true : false,
                currency: $("#currency").val(),
                paymentMethod: ($("#currentPayment").val() === 'novalnetGooglePay') ? 'GOOGLEPAY' : 'APPLEPAY',
                environment: ($("#testMode").val() === FLAG_TRUE) ? "SANDBOX" : "PRODUCTION",
            };

            var buttonInformation = {
                type: $('#novalnetGooglepayButtonType').val(),
                style: "black",
                locale: ACC.novalnetcheckoutaddon.getGooglePayLocale(),
                boxSizing: "fill",
                dimensions: {
                    width: 0,
                    height: parseInt($('#novalnetGooglepayButtonHeight').val())
                }
            };

            var paymentIntent = {
                clientKey: $(self.CLIENT_KEY).val(),
                paymentIntent: {
                    merchant: merchantInformation,
                    transaction: transactionInformation,
                    button: buttonInformation,
                    callbacks: {

                        onPaymentButtonClicked: function (bookingResult) {//Nosonar
                            bookingResult({status: $("#Terms1").prop("checked") ? STATUS_SUCCESS : STATUS_FAILURE});
                            if ($("#Terms1").prop("checked") === false) {
                                if (!$('#termserror').length) {
                                    var errorMessage = $("#termsCheckErrorMessage").val();
                                    var div = $("<div>").attr("id", "termserror").attr("class", "alert alert-danger alert-dismissable getAccAlert").text(errorMessage);
                                    $('#placeOrderForm1').prepend($(div));
                                }
                                return false;
                            }
                        },
                        "onProcessCompletion": function (payLoad, bookingResult) { // NOSONAR
                            if (payLoad.result && payLoad.result.status === STATUS_SUCCESS) {
                                $.post('bookWalletTransaction', {
                                    token: payLoad.transaction.token,
                                    doRedirect: payLoad.transaction.doRedirect,
                                    amount: payLoad.transaction.amount
                                }).done(function (processedResult) {

                                    processedResult = $.parseJSON(processedResult);
                                    const validStatus = ['CONFIRMED', 'ON_HOLD'];
                                    if (processedResult.result.status === STATUS_SUCCESS && processedResult.result.redirect_url !== undefined) {
                                        window.location = processedResult.result.redirect_url;
                                    } else if (processedResult.hasOwnProperty('transaction') && validStatus.includes(processedResult.transaction.status)) {
                                        bookingResult({status: STATUS_SUCCESS, statusText: ""});
                                        document.getElementById('placeOrder').click();
                                    } else {
                                        bookingResult({
                                            status: STATUS_FAILURE,
                                            statusText: processedResult.result.status_text
                                        });
                                    }

                                });

                            } else {
                                if (payLoad.result.status_text) {
                                    alert(payLoad.result.status_text);
                                }
                            }

                        }
                    }
                }
            };
            try {
                var NovalnetPaymentInstance = NovalnetPayment();
                var walletNovalnetPaymentObj = NovalnetPaymentInstance.createPaymentObject();
                walletNovalnetPaymentObj.setPaymentIntent(paymentIntent);
                walletNovalnetPaymentObj.isPaymentMethodAvailable(function (displayGooglePayButton) {
                    walletNovalnetPaymentObj.addPaymentButton(WALLET_CONTAINER);
                });

            } catch (e) {
                // Handling the errors from the payment intent setup
                console.log(e.message);
            }
        }
        if ($(PREVIOUS_SELECTED_PAYMENT) !== undefined && $(PREVIOUS_SELECTED_PAYMENT).val() !== null && $(PREVIOUS_SELECTED_PAYMENT).val() !== '') {
            $(document).find('input:radio[name=selectedPaymentMethodId][value=' + $(PREVIOUS_SELECTED_PAYMENT).val() + ']').attr('checked', true);
        } else {
            $(document).find('input:radio[name=selectedPaymentMethodId]:first').attr('checked', true);
        }

        $('input:radio[name=selectedPaymentMethodId]').click(function () {
            ACC.novalnetcheckoutaddon.paymentSelectionProcess();

            if ($(NOVALNET_CREDIT_CARD).is(":checked")) {
                ACC.novalnetcheckoutaddon.loadIframe();
            }
        });

        if ($(ORDER_BILLING_ADDRESS).length && $(NOVALNET_ADDRESS_DATA).length) {
            if ($(ORDER_BILLING_ADDRESS).children(VALUE_ORDER).length) {
                $(ORDER_BILLING_ADDRESS).children(VALUE_ORDER).replaceWith($(NOVALNET_ADDRESS_DATA).html());
            } else {
                $(ORDER_BILLING_ADDRESS).append("<div class = value-order>" + $(NOVALNET_ADDRESS_DATA).html() + "</div>")
            }
        }

        if ($(GUARANTEE_ACCOUNT_BIC).length) {
            $(GUARANTEE_ACCOUNT_BIC).css('display', 'none');
        }

        if ($(ACCOUNT_BIC).length) {
            $(ACCOUNT_BIC).css('display', 'none');
        }

        if ($(ACCOUNT_IBAN).length) {
            $(ACCOUNT_IBAN).css(CSS_TEXT_TRANSFORM, 'uppercase');
        }
        if ($(ACCOUNT_BIC).length) {
            $(ACCOUNT_BIC).css(CSS_TEXT_TRANSFORM, 'uppercase');
        }

        if ($('#creditcardSaveData').length) {
            $('#creditcardSaveData').prop('checked', true);
        }

        if ($('#paypalSaveData').length) {
            $('#paypalSaveData').prop('checked', true);
        }

        if ($('#directDebitSepaSaveData').length) {
            $('#directDebitSepaSaveData').prop('checked', true);
        }

        if ($('#directDebitAchSaveData').length) {
            $('#directDebitAchSaveData').prop('checked', true);
        }

        if ($('#guaranteedDirectDebitSepaSaveData').length) {
            $('#guaranteedDirectDebitSepaSaveData').prop('checked', true);
        }

        $(ACCOUNT_BIC).keypress(function (event) {
            return NovalnetUtility.formatBic(event);
        });

        $(ACCOUNT_IBAN).keypress(function (event) {
            return NovalnetUtility.formatIban(event, 'accountBic');
        });

        if ($(GUARANTEE_ACCOUNT_IBAN).length) {
            $(ACCOUNT_IBAN).css(CSS_TEXT_TRANSFORM, 'uppercase');
        }

        if ($(GUARANTEE_ACCOUNT_BIC).length) {
            $(ACCOUNT_BIC).css(CSS_TEXT_TRANSFORM, 'uppercase');
        }

        $(GUARANTEE_ACCOUNT_IBAN).keypress(function (event) {
            return NovalnetUtility.formatIban(event, 'guaranteeAccountBic');
        });

        $(GUARANTEE_ACCOUNT_IBAN).on("input", function (event) {
            NovalnetUtility.formatIban(event, "guaranteeAccountBic");
        });

        $(GUARANTEE_ACCOUNT_BIC).keypress(function (event) {
            return NovalnetUtility.formatBic(event);
        });

        $(ACCOUNT_IBAN).on("input", function (event) {
            NovalnetUtility.formatIban(event, 'accountBic');
        });

        $(ACCOUNT_BIC).change(function (event) {
            return NovalnetUtility.formatBic(event);
        });

        if ($('#creditCardOneClickData1').length) {
            $('#creditCardOneClickData1').prop('checked', true);
        }

        if ($('#paypalOneClickData1').length) {
            $('#paypalOneClickData1').prop('checked', true);
        }

        if ($('#directDebitSepaOneClickData1').length) {
            $('#directDebitSepaOneClickData1').prop('checked', true);
        }

        if ($('#novalnetDirectDebitAchOneClickData1').length) {
            $('#novalnetDirectDebitAchOneClickData1').prop('checked', true);
        }

        if ($('#GuaranteedDirectDebitSepaOneClickData1').length) {
            $('#GuaranteedDirectDebitSepaOneClickData1').prop('checked', true);
        }

        const acceptedCountry = ["DE", "AU", "AT", "CH"];
        var sepaError = 0;
        var invoiceError = 0;

        if ($(SEPA_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(NOT_CHECKED)) {
            $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'none');
            sepaError = 1;
        }
        if ($(SEPA_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(":checked") && $.inArray($(SHIP_COUNTRY).val(), acceptedCountry) === -1) {
            $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'none');
            sepaError = 1;
        }
        if ($(INVOICE_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(NOT_CHECKED)) {
            $(GUARANTEED_INVOICE).css('display', 'none');
            invoiceError = 1;
        }
        if ($(INVOICE_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(":checked") && $.inArray($(SHIP_COUNTRY).val(), acceptedCountry) === -1) {
            $(GUARANTEED_INVOICE).css('display', 'none');
            invoiceError = 1;
        }

        $(USE_DELIVERY_ADDRESS).click(function () {//NOSONAR
            if ($(SEPA_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(NOT_CHECKED)) {
                $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'none');
                $(GUARANTEED_DIRECT_DEBIT_SEPA_ID).prop('checked', false);
                if ($(SEPA_FORCE_GUARANTEE_CHECK).length && $(SEPA_FORCE_GUARANTEE_CHECK).val() === FLAG_TRUE) {
                    $(NOVALNET_SEPA).css('display', 'block');
                    $(NOVALNET_DIRECT_DEBIT_SEPA_ID).prop('checked', true);
                }
                sepaError = 1;
            } else if ($(SEPA_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(":checked") && $.inArray($(SHIP_COUNTRY).val(), acceptedCountry) === -1) {
                $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'none');
                sepaError = 1;
            } else if ($(SEPA_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(":checked") && $.inArray($(SHIP_COUNTRY).val(), acceptedCountry) !== -1) {
                $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'block');
                $(NOVALNET_SEPA).css('display', 'none');
                $(GUARANTEED_DIRECT_DEBIT_SEPA_ID).prop('checked', true);
            }

            if ($(INVOICE_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(NOT_CHECKED)) {
                $(GUARANTEED_INVOICE).css('display', 'none');
                $(NOVALNET_INVOICE_ID).prop('checked', false);
                if ($(INVOICE_FORCE_GUARANTEE_CHECK).length && $(INVOICE_FORCE_GUARANTEE_CHECK).val() === FLAG_TRUE) {
                    $(NOVALNET_INVOICE).css('display', 'block');
                    $(NOVALNET_INVOICE_ID).prop('checked', true);
                } else {
                    $('input:radio[name="selectedPaymentMethodId"]').each(function () {
                        if ($(this).attr('id') === 'novalnetGuaranteedInvoice' || $(this).attr('id') === 'novalnetInvoice') {
                            return true;
                        }
                        $(this).prop('checked', true);
                        return false;
                    });
                }
                invoiceError = 1;
            } else if ($(INVOICE_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(":checked") && $.inArray($(SHIP_COUNTRY).val(), acceptedCountry) === -1) {
                $(GUARANTEED_INVOICE).css('display', 'none');
                invoiceError = 1;
            } else if ($(INVOICE_GUARANTEE_CHECK).length && $(USE_DELIVERY_ADDRESS).is(":checked") && $.inArray($(SHIP_COUNTRY).val(), acceptedCountry) !== -1) {
                $(GUARANTEED_INVOICE).css('display', 'block');
                $(NOVALNET_INVOICE).css('display', 'none');
                $(NOVALNET_INVOICE_ID).prop('checked', true);
            }
        });


        var hideInvoice = 0;
        var hidesepa = 0;

        if ($(NOVALNET_APPLE_PAY).length && !NovalnetUtility.isApplePayAllowed()) {
            $(NOVALNET_APPLE_PAY).parent('div').css('display', 'none');
        }
        if ((invoiceError === 1 || !$(INVOICE_GUARANTEE_CHECK).length) && ($(INVOICE_FORCE_GUARANTEE_CHECK).length && $(INVOICE_FORCE_GUARANTEE_CHECK).val() === self.FLAG_FALSE)) {
            $(NOVALNET_INVOICE).css('display', 'block');
            $(GUARANTEED_INVOICE).css('display', 'none');
        }

        if ((invoiceError === 1 || !$(INVOICE_GUARANTEE_CHECK).length) && ($(INVOICE_FORCE_GUARANTEE_CHECK).length && $(INVOICE_FORCE_GUARANTEE_CHECK).val() === self.FLAG_FALSE)) {
            $(NOVALNET_INVOICE).css('display', 'none');
            $(GUARANTEED_INVOICE).css('display', 'none');
            hideInvoice = 1;
        }

        if ((sepaError === 1 || !$(SEPA_GUARANTEE_CHECK).length) && ($(SEPA_FORCE_GUARANTEE_CHECK).length && $(SEPA_FORCE_GUARANTEE_CHECK).val() === FLAG_TRUE)) {
            $(NOVALNET_SEPA).css('display', 'block');
            $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'none');
        }

        if ((sepaError === 1 || !$(SEPA_GUARANTEE_CHECK).length) && ($(SEPA_FORCE_GUARANTEE_CHECK).length && $(SEPA_FORCE_GUARANTEE_CHECK).val() === self.FLAG_FALSE)) {
            $(NOVALNET_SEPA).css('display', 'none');
            $(GUARANTEED_DIRECT_DEBIT_SEPA).css('display', 'none');
            hidesepa = 1;
        }

        if (hidesepa === 0 && ($(GUARANTEED_DIRECT_DEBIT_SEPA).css('display') === 'none' || !$(GUARANTEED_DIRECT_DEBIT_SEPA).length)) {
            $(NOVALNET_SEPA).css('display', 'block');
        }

        if ($(NOVALNET_DIRECT_DEBIT_ACH).length) {
            $(NOVALNET_DIRECT_DEBIT_ACH).css('display', 'block');
        }

        if (hideInvoice === 0 && ($(GUARANTEED_INVOICE).css('display') === 'none' || !$(GUARANTEED_INVOICE).length)) {
            $(NOVALNET_INVOICE).css('display', 'block');
        }

        // Process hash call.
        if ($(NOVALNET_CREDIT_CARD).is(":checked")) {
            ACC.novalnetcheckoutaddon.loadIframe();
        }

        $(".submit_novalnetPaymentDetailsForm").click(function (event) {
            if ($(NOVALNET_CREDIT_CARD).is(":checked") && $(NOVALNET_CREDIT_CARD_PAN_HASH).val() === '') {
                event.preventDefault();
                event.stopImmediatePropagation();
                if ($(CREDIT_CARD_IFRAME_ID).css('display') !== 'none' && $(CREDIT_CARD_IFRAME_ID).parent('div').css('display') !== 'none') {
                    NovalnetUtility.getPanHash();
                } else {
                    $(PAYMENT_DETAILS_FORM).submit();
                }

            } else {
                $(PAYMENT_DETAILS_FORM).submit();
            }
        });

        $('input:radio[name=directDebitSepaOneClickData1]').click(function () {
            if ($('#directDebitSepaOneClickData1AddNew').is(":checked")) {
                $('.novalnetDirectDebitSepaOneClickForm').css('display', 'block');
                $(CREDIT_CARD_IFRAME).css('display', 'block');

            } else {
                $('.novalnetDirectDebitSepaOneClickForm').css('display', 'none');
                $(CREDIT_CARD_IFRAME).css('display', 'none');
            }
        });

        $('input:radio[name=directDebitAchOneClickData1]').click(function () {

            if ($('#novalnetDirectDebitAchOneClickData1AddNew').is(":checked")) {
                $('.novalnetDirectDebitAchOneClickForm').css('display', 'block');
            } else {
                $('.novalnetDirectDebitAchOneClickForm').css('display', 'none');
            }
        });


        $('input:radio[name=guaranteedDirectDebitSepaOneClickData1]').click(function () {
            if ($('#guaranteedDirectDebitSepaOneClickData1AddNew').is(":checked")) {
                $('.novalnetGuaranteedDirectDebitSepaOneClickForm').css('display', 'block');
                $(CREDIT_CARD_IFRAME).css('display', 'block');

            } else {
                $('.novalnetGuaranteedDirectDebitSepaOneClickForm').css('display', 'none');
                $(CREDIT_CARD_IFRAME).css('display', 'none');
            }
        });
        $('input:radio[name=creditCardOneClickData1]').click(function () {
            if ($('#creditCardOneClickNewDeatails').is(":checked")) {
                $('.novalnetCreditCardOneClickForm').css('display', 'block');

            } else {
                $('.novalnetCreditCardOneClickForm').css('display', 'none');
            }
        });
        $('input:radio[name=payPalOneClickData1]').click(function () {
            if ($('#payPalOneClickData1AddNew').is(":checked")) {
                $('.novalnetPayPalOneClickForm').css('display', 'block');

            } else {
                $('.novalnetPayPalOneClickForm').css('display', 'none');
            }
        });

    },
    paymentSelectionProcess: function () {
        var self = ACC.novalnetcheckoutaddon;
        var ACCOUNT_IBAN = self.ACCOUNT_IBAN;
        var GUARANTEED_DIRECT_DEBIT_SEPA_ID = self.GUARANTEED_DIRECT_DEBIT_SEPA_ID;
        var CREDIT_CARD_IFRAME_ID = self.CREDIT_CARD_IFRAME_ID;
        var DIRECT_DEBIT_SEPA_DATE_OF_BIRTH = self.DIRECT_DEBIT_SEPA_DATE_OF_BIRTH;
        var NOVALNET_DIRECT_DEBIT_ACH_ID = self.NOVALNET_DIRECT_DEBIT_ACH_ID;
        var NOVALNET_ACH_ACCOUNT_HOLDER = self.NOVALNET_ACH_ACCOUNT_HOLDER;
        var NOVALNET_ACH_ACCOUNT_NUMBER = self.NOVALNET_ACH_ACCOUNT_NUMBER;
        var NOVALNET_ACH_ROUTING_NUMBER = self.NOVALNET_ACH_ROUTING_NUMBER;

        if ($('input:radio[name=selectedPaymentMethodId]') !== undefined) {
            $.each([
                'novalnetCreditCard',
                'novalnetDirectDebitSepa',
                'novalnetDirectDebitAch',
                'novalnetInvoice',
                'novalnetPrepayment',
                'novalnetPayPal',
                'novalnetOnlineBankTransfer',
                'novalnetIdeal',
                'novalnetApplePay',
                'novalnetGooglePay',
                'novalnetTwint',
                'novalnetMbWay',
                'novalnetAlipay',
                'novalnetTrustly',
                'novalnetBlik',
                'novalnetWeChatPay',
                'novalnetEps',
                'novalnetPrzelewy24',
                'novalnetGuaranteedDirectDebitSepa',
                'novalnetGuaranteedInvoice',
                'novalnetPostFinance',
                'novalnetPostFinanceCard',
                'novalnetMultibanco',
                'novalnetBancontact'
            ], function (index, value) {
                $('input:radio[name=selectedPaymentMethodId]:checked').val() === value ? $('#' + value + 'PaymentForm').show() : $('#' + value + 'PaymentForm').hide();
            });

            if ($(self.NOVALNET_DIRECT_DEBIT_SEPA_ID).is(":checked") || $(GUARANTEED_DIRECT_DEBIT_SEPA_ID).is(":checked")) {

                $(ACCOUNT_IBAN).keypress(
                    function (event) {
                        return ACC.novalnetcheckoutaddon.allowAlphanumericKey(event);
                    }
                );
            }

            if ($(NOVALNET_DIRECT_DEBIT_ACH_ID).is(":checked")) {

                $(NOVALNET_ACH_ACCOUNT_HOLDER).keypress(function (event) {
                    return ACC.novalnetcheckoutaddon.allowAchNameKey(event);
                });

                $(NOVALNET_ACH_ACCOUNT_NUMBER).keypress(function (event) {
                    return ACC.novalnetcheckoutaddon.allowAchNumberKey(event);
                });

                $(NOVALNET_ACH_ROUTING_NUMBER).keypress(function (event) {
                    return ACC.novalnetcheckoutaddon.allowAchNumberKey(event);
                });
            }

            $(NOVALNET_ACH_ACCOUNT_HOLDER).on('input', function () {
                this.value = this.value.replace(/[^a-zA-Z\s]/g, '');
            });

            $(NOVALNET_ACH_ACCOUNT_NUMBER + ', ' + NOVALNET_ACH_ROUTING_NUMBER).on('input', function () {
                this.value = this.value.replace(/\D/g, '');
            });

            if ($(CREDIT_CARD_IFRAME_ID).attr("id") !== undefined) {

                $('#novalnetCreditCardPaymentOption').on(
                    'click', function (event) {

                        if ($('#novalnetCreditCardOneClickElements').css('display') === 'none') {
                            ACC.novalnetcheckoutaddon.showOneClickForm('novalnetCreditCard');
                        } else {
                            ACC.novalnetcheckoutaddon.showPaymentForm('novalnetCreditCard');
                        }
                    }
                );

                if ($('#novalnetCreditCardOneClickProcess').val() === '1') {
                    ACC.novalnetcheckoutaddon.showOneClickForm('novalnetCreditCard');
                } else {
                    ACC.novalnetcheckoutaddon.showPaymentForm('novalnetCreditCard');
                }
            }
            $('#novalnetDirectDebitSepaPaymentOption').on(
                'click', function (event) {
                    if ($('#novalnetDirectDebitSepaOneClickElements').css('display') === 'none') {
                        ACC.novalnetcheckoutaddon.showOneClickForm('novalnetDirectDebitSepa');
                        $(DIRECT_DEBIT_SEPA_DATE_OF_BIRTH).parent('div').css('display', 'none');
                    } else {
                        ACC.novalnetcheckoutaddon.showPaymentForm('novalnetDirectDebitSepa');
                        $(DIRECT_DEBIT_SEPA_DATE_OF_BIRTH).parent('div').css('display', 'block');
                    }
                }
            );
            $('#novalnetDirectDebitAchPaymentOption').on(
                'click',
                function (event) {

                    if ($('#novalnetDirectDebitAchOneClickElements').css('display') === 'none') {
                        ACC.novalnetcheckoutaddon.showOneClickForm('novalnetDirectDebitAch');
                    } else {
                        ACC.novalnetcheckoutaddon.showPaymentForm('novalnetDirectDebitAch');
                    }
                }
            );
        }

        if ($('#novalnetDirectDebitSepaOneClickProcess').val() === '1') {
            ACC.novalnetcheckoutaddon.showOneClickForm('novalnetDirectDebitSepa');
            $(DIRECT_DEBIT_SEPA_DATE_OF_BIRTH).parent('div').css('display', 'none');
        } else {
            ACC.novalnetcheckoutaddon.showPaymentForm('novalnetDirectDebitSepa');
            $(DIRECT_DEBIT_SEPA_DATE_OF_BIRTH).parent('div').css('display', 'block');
        }

        if ($('#novalnetDirectDebitAchOneClickProcess').val() === '1') {
            ACC.novalnetcheckoutaddon.showOneClickForm('novalnetDirectDebitAch');
        } else {
            ACC.novalnetcheckoutaddon.showPaymentForm('novalnetDirectDebitAch');
        }
    },


    loadIframe: function () {
        var self = ACC.novalnetcheckoutaddon;
        var USE_DELIVERY_ADDRESS = self.USE_DELIVERY_ADDRESS;
        var CLIENT_KEY = self.CLIENT_KEY;
        var NOVALNET_TEST_MODE = self.NOVALNET_TEST_MODE;
        var SHIP_COUNTRY = self.SHIP_COUNTRY;
        var PAYMENT_DETAILS_FORM = self.PAYMENT_DETAILS_FORM;
        var CHALLENGE_WINDOW_OVERLAY = self.CHALLENGE_WINDOW_OVERLAY;

        var inline = 0;
        var testMode;
        var sameAsBilling = 0;

        if ($('#novalnetInlineCC').length) {
            inline = 1;
        }

        if ($(USE_DELIVERY_ADDRESS).is(":checked")) {
            sameAsBilling = 1;
        }

        if (sameAsBilling === 1) {
            var bill_to_zip = $("#address.postcode").val();// NOSONAR
            var bill_to_city = $("#address.townCity").val();// NOSONAR
            var bill_to_street = $("#address.line1").val() + ' ' + $("#address.line2").val();// NOSONAR
        }

        NovalnetUtility.setClientKey($(CLIENT_KEY).val());

        if ($(NOVALNET_TEST_MODE).val() !== undefined && $(NOVALNET_TEST_MODE).val() === 1) {
            testMode = 1;
        } else {
            testMode = 0;
        }
        var configurationObject = {

            // You can handle the process here, when specific events occur.
            callback: {

                // Called once the pan_hash (temp. token) created successfully.
                on_success: function (data) {// NOSONAR
                    document.getElementById('novalnetCreditCardPanHash').value = data['hash'];
                    document.getElementById('novalnetCreditCardUniqueId').value = data['unique_id'];
                    document.getElementById('do_redirect').value = data['do_redirect'];
                    $(PAYMENT_DETAILS_FORM).submit();
                    return true;
                },

                // Called in case of an invalid payment data or incomplete input.
                on_error: function (data) {// NOSONAR
                    document.getElementById("nn_overlay").classList.remove(CHALLENGE_WINDOW_OVERLAY);
                    document.getElementById("novalnetCreditCardIframe").classList.remove(CHALLENGE_WINDOW_OVERLAY);
                    if (undefined !== data['error_message']) {
                        alert(data['error_message']);
                        return false;
                    }
                },

                // Called in case the challenge window Overlay (for 3ds2.0) displays
                on_show_overlay: function (data) {// NOSONAR
                    document.getElementById('novalnetCreditCardIframe').classList.add(CHALLENGE_WINDOW_OVERLAY);
                },

                // Called in case the Challenge window Overlay (for 3ds2.0) hided
                on_hide_overlay: function (data) {// NOSONAR
                    document.getElementById("novalnetCreditCardIframe").classList.remove(CHALLENGE_WINDOW_OVERLAY);
                    document.getElementById("nn_overlay").classList.add(CHALLENGE_WINDOW_OVERLAY);
                }
            },

            iframe: {

                // It is mandatory to pass the Iframe ID here.  Based on which the entire process will took place.
                id: "novalnetCreditCardIframe",

                // Set to 1 to make you Iframe input container more compact (default - 0)
                inline: inline,

                // Add the style (css) here for either the whole Iframe contanier or for particular label/input field
                style: {
                    // The css for the Iframe container
                    container: $('#novalnetStandardCss').val(),

                    // The css for the input field of the Iframe container
                    input: $('#novalnetStandardInputCss').val(),

                    // The css for the label of the Iframe container
                    label: $('#novalnetStandardLabelCss').val()
                },

                // You can customize the text of the Iframe container here
                text: {

                    // The End-customers selected language. The Iframe container will be rendered in this Language.
                    lang: $('#lang').val(),

                    // Basic Error Message
                    error: "Your credit card details are invalid",

                    // You can customize the text for the Card Holder here
                    card_holder: {

                        // You have to give the Customized label text for the Card Holder Container here
                        label: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.holder.label.text'],

                        // You have to give the Customized placeholder text for the Card Holder Container here
                        place_holder: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.holder.placeholder.text'],

                        // You have to give the Customized error text for the Card Holder Container here
                        error: "Please enter the valid card holder name"
                    },

                    card_number: {

                        // You have to give the Customized label text for the Card Number Container here
                        label: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.number.label.text'],

                        // You have to give the Customized placeholder text for the Card Number Container here
                        place_holder: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.number.placeholder.text'],

                        // You have to give the Customized error text for the Card Number Container here
                        error: "Please enter the valid card number"
                    },
                    expiry_date: {

                        // You have to give the Customized label text for the Expiry Date Container here
                        label: "Expiry date",

                        // You have to give the Customized error text for the Expiry Date Container here
                        error: "Please enter the valid expiry month / year in the given format"
                    },
                    cvc: {

                        // You have to give the Customized label text for the CVC/CVV/CID Container here
                        label: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.cvc.label.text'],

                        // You have to give the Customized placeholder text for the CVC/CVV/CID Container here
                        place_holder: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.cvc.placeholder.text'],

                        // You have to give the Customized error text for the CVC/CVV/CID Container here
                        error: "Please enter the valid CVC/CVV/CID"
                    }
                }
            },

            // Add Customer data
            customer: {

                // Your End-customer's First name which will be prefilled in the Card Holder field
                first_name: $('#customerFirstName').val(),

                // Your End-customer's Last name which will be prefilled in the Card Holder field
                last_name: $('#customerLastName').val(),

                // Your End-customer's Email ID.
                email: $('#email').val(),

                // Your End-customer's billing address.
                billing: {

                    // Your End-customer's billing street (incl. House no).
                    street: $('#ship_street').val(),

                    // Your End-customer's billing city.
                    city: $('#ship_city').val(),

                    // Your End-customer's billing zip.
                    zip: $('#ship_zip').val(),

                    // Your End-customer's billing country ISO code.
                    country_code: $(SHIP_COUNTRY).val(),
                },
                shipping: {

                    // Set to 1 if the billing and shipping address are same and no need to specify shipping details again here.
                    "same_as_billing": 1,
                },
            },

            // Add transaction data
            transaction: {

                // The payable amount that can be charged for the transaction (in minor units), for eg:- Euro in Eurocents (5,22 EUR = 522).
                amount: $("#orderAmount").val(),

                // The three-character currency code as defined in ISO-4217.
                currency: $("#currency").val(),

                // Set to 1 for the TEST transaction (default - 0).
                test_mode: testMode
            }
        }
        NovalnetUtility.createCreditCardForm(configurationObject);
    },

    showPaymentForm: function (paymentName) {
        // Assigning payment form functionality.
        $('#' + paymentName + 'PaymentFormElements').css('display', 'block');
        $('#' + paymentName + 'OneClickElements').css('display', 'none');
        $('#' + paymentName + 'OneClickProcess').val('0');
        $('#' + paymentName + 'PaymentOption').html(ACC.addons.novalnetcheckoutaddon['novalnet.' + paymentName + 'GivenDetails.text']);
    },

    showOneClickForm: function (paymentName) {
        // Assigning one click shop functionality.
        $('#' + paymentName + 'PaymentFormElements').css('display', 'none');
        $('#' + paymentName + 'OneClickElements').css('display', 'block');
        $('#' + paymentName + 'OneClickProcess').val('1');
        $('#' + paymentName + 'PaymentOption').html(ACC.addons.novalnetcheckoutaddon['novalnet.' + paymentName + 'NewDetails.text']);
        $('#' + paymentName + 'OneClickElements input[type="text"]').prop('readonly', true);
        $('#' + paymentName + 'PaymentOption').removeAttr('class');
    },
    allowAlphanumericKey: function (event) {

        var keycode = ('which' in event) ? event.which : event.keyCode,
            reg = /^(?:[0-9a-zA-Z]+$)/;
        return (reg.test(String.fromCharCode(keycode)) || 0 === keycode || 8 === keycode);
    },
    allowNameKey: function (event) {

        var keycode = ('which' in event) ? event.which : event.keyCode,
            reg = /[^0-9\[\]\/\\#,+@!^()$~%'"=:;<>{}\_\|*?`]/g;
        return (reg.test(String.fromCharCode(keycode)) || 0 === keycode || 8 === keycode);
    },
    allowAchNameKey: function (event) {

        var keycode = ('which' in event) ? event.which : event.keyCode,
            reg = /^[a-zA-Z\s]$/;

        return (reg.test(String.fromCharCode(keycode)) || keycode === 0 || keycode === 8);
    }, allowAchNumberKey: function (event) {

        var keycode = ('which' in event) ? event.which : event.keyCode,
            reg = /^\d$/;

        return (reg.test(String.fromCharCode(keycode)) || keycode === 0 || keycode === 8);
    },
}

$(document).ready(function () {
    var self = ACC.novalnetcheckoutaddon;
    var NOVALNET_COMMENTS = self.NOVALNET_COMMENTS;
    var ORDER_PAYMENT_BOX = self.ORDER_PAYMENT_BOX;
    var CANCEL_ORDER_FORM = self.CANCEL_ORDER_FORM;
    var ORDER_PAYMENT_DATA = self.ORDER_PAYMENT_DATA;
    var VALUE_ORDER = self.VALUE_ORDER;

    if ($(NOVALNET_COMMENTS) !== undefined && $(ORDER_PAYMENT_BOX) !== undefined && (parseFloat($(NOVALNET_COMMENTS).css("height")) > parseFloat($(ORDER_PAYMENT_BOX).css("height")))) {
        $(ORDER_PAYMENT_BOX).css("height",
            parseFloat($(ORDER_PAYMENT_BOX).css("height")) + parseFloat($(NOVALNET_COMMENTS).css("height"))
        );
    }
    if ($(CANCEL_ORDER_FORM) !== undefined) {
        $(CANCEL_ORDER_FORM).hide();
    }
    if ($(ORDER_PAYMENT_DATA).find(VALUE_ORDER).html() !== undefined) {
        var orderCommernts = $(ORDER_PAYMENT_DATA).find(VALUE_ORDER).html();

        $(ORDER_PAYMENT_DATA).find(VALUE_ORDER).html(
            "<span class='novalnet-transaction-comments'>" +
            orderCommernts.replace(
                /&lt;br&gt;/g,
                "</span>&nbsp;<span class='novalnet-transaction-comments'>"
            ) +
            "</span>"
        );
    }

    ACC.novalnetcheckoutaddon.initialProcess();

    const PAYGATE_URL_SELECTOR = '#paygateurl';

    const $paygateUrl = $(PAYGATE_URL_SELECTOR);

    if ($paygateUrl.length) {
        window.open($paygateUrl.val(), '_self');
    }

    $(document).on('click', '.novalnet-delete-saved', function (e) {
        e.preventDefault();
        e.stopPropagation();

        var deleteIcon = $(this);
        var paymentType = deleteIcon.data('payment');
        var cardIndex = deleteIcon.data('index');
        $.ajax({
            url: ACC.config.encodedContextPath +
                '/checkout/multi/novalnet/select-payment-method/remove-saved-payment',
            type: 'POST',
            data: {
                paymentType: paymentType,
                cardIndex: cardIndex
            },
            success: function (result) {
                if (result === true || result === 'true') {
                    window.location.reload();
                } else {
                    console.error('Unable to delete saved payment information.');
                }
            },
            error: function (xhr) {
                console.error('Delete payment failed:', xhr.status);
            }
        });
    });

});
