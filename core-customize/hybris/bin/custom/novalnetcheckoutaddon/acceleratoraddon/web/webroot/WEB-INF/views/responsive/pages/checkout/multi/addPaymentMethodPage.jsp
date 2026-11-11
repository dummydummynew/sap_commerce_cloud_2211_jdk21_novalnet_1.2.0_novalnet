<%@ page trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="multiCheckout" tagdir="/WEB-INF/tags/responsive/checkout/multi" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="address" tagdir="/WEB-INF/tags/responsive/address" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="multiCheckoutNovalnet" tagdir="/WEB-INF/tags/addons/novalnetcheckoutaddon/responsive/checkout/multi" %>
<%@ taglib prefix="novalnetPayment" tagdir="/WEB-INF/tags/addons/novalnetcheckoutaddon/responsive/checkout/multi/paymentmethods" %>
<script src="https://cdn.novalnet.de/js/v2/NovalnetUtility.js"></script>
<spring:htmlEscape defaultHtmlEscape="true"/>

<<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">
    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout" />
            </div>
            <multiCheckout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepThree">
                        <div class="checkout-paymentmethod">
                            <div class="checkout-indent">

                                <div class="headline">
                                    <spring:theme code="checkout.multi.paymentMethod" />
                                </div>

                                <ycommerce:testId code="paymentDetailsForm">
                                    <form:form id="paymentDetailsForm" name="paymentDetailsForm"
                                        modelAttribute="paymentDetailsForm" method="POST">

                                        <div id="billingAdrressInfo" style="display:block">
                                            <h1 class="headline">
                                                <spring:theme
                                                    code="checkout.multi.paymentMethod.addPaymentDetails.billingAddress" />
                                            </h1>

                                            <c:if test="${cartData.deliveryItemsQuantity > 0}">
                                                <div id="useDeliveryAddressData"
                                                    data-title="${fn:escapeXml(deliveryAddress.title)}"
                                                    data-firstname="${fn:escapeXml(deliveryAddress.firstName)}"
                                                    data-lastname="${fn:escapeXml(deliveryAddress.lastName)}"
                                                    data-line1="${fn:escapeXml(deliveryAddress.line1)}"
                                                    data-line2="${fn:escapeXml(deliveryAddress.line2)}"
                                                    data-countryisocode="${fn:escapeXml(deliveryAddress.country.isocode)}"
                                                    data-regionisocode="${fn:escapeXml(deliveryAddress.region.isocodeShort)}"
                                                    data-address-id="${fn:escapeXml(deliveryAddress.id)}"></div>

                                                <formElement:formCheckbox path="useDeliveryAddress"
                                                    idKey="useDeliveryAddress"
                                                    labelKey="checkout.multi.sop.useMyDeliveryAddress" tabindex="11" />
                                            </c:if>

                                            <div id="novalnetBillAddressForm">
                                                <address:billAddressFormSelector supportedCountries="${countries}"
                                                    regions="${regions}" tabindex="12" />
                                            </div>

                                            <h1 class="headline">
                                                <spring:theme code="checkout.summary.select.payment.method" />
                                            </h1>

                                            <input type="hidden" id="customerFirstName" value="${fn:escapeXml(customerFirstName)}"/>
                                            <input type="hidden" id="customerLastName" value="${fn:escapeXml(customerLastName)}"/>
                                            <input type="hidden" id="ship_zip" value="${fn:escapeXml(deliveryAddress.postalCode)}"/>
                                            <input type="hidden" id="ship_country" value="${fn:escapeXml(deliveryAddress.country.isocode)}"/>
                                            <input type="hidden" id="ship_city" value="${fn:escapeXml(deliveryAddress.town)}"/>
                                            <input type="hidden" id="ship_street" value="${fn:escapeXml(deliveryAddress.line1)} ${fn:escapeXml(deliveryAddress.line2)}"/>
                                            <input type="hidden" id="email" value="${email} "/>

                                            <c:if
                                                test="${novalnetBaseStoreConfiguration.novalnetTariffId != null && novalnetBaseStoreConfiguration.novalnetPaymentAccessKey != null}">
                                                <div class="paymentMethods">

                                                    <novalnetPayment:creditCard />
                                                    <novalnetPayment:guaranteedDirectDebitSepa />
                                                    <novalnetPayment:directDebitSepa />
                                                    <novalnetPayment:directDebitAch />
                                                    <novalnetPayment:paymentOption config="${novalnetPayPal}"
                                                        id="novalnetPayPal" value="novalnetPayPal"
                                                        logoImage="novalnetPayPal.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetTwint}"
                                                        id="novalnetTwint" value="novalnetTwint"
                                                        logoImage="novalnetTwint.png" logoWidth="5em" logoHeight="2em"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetMbWay}"
                                                        id="novalnetMbWay" value="novalnetMbWay"
                                                        logoImage="novalnet_mbway.png" logoWidth="5em" logoHeight="2em"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetTrustly}"
                                                        id="novalnetTrustly" value="novalnetTrustly"
                                                        logoImage="novalnet_trustly.png" logoWidth="5em"
                                                        logoHeight="2em"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetBlik}"
                                                        id="novalnetBlik" value="novalnetBlik"
                                                        logoImage="novalnet_blik.png" logoWidth="5em" logoHeight="2em"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetWeChatPay}"
                                                        id="novalnetWeChatPay" value="novalnetWeChatPay"
                                                        logoImage="novalnet_wechatpay.png" logoWidth="5em"
                                                        logoHeight="2em"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetAlipay}"
                                                        id="novalnetAlipay" value="novalnetAlipay"
                                                        logoImage="novalnet_alipay.png" logoWidth="5em" logoHeight="2em"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetInvoice}"
                                                        id="novalnetInvoice" value="novalnetInvoice"
                                                        logoImage="novalnetInvoice.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:guaranteedInvoice />

                                                    <novalnetPayment:paymentOption config="${novalnetPrepayment}"
                                                        id="novalnetPrepayment" value="novalnetPrepayment"
                                                        logoImage="novalnetPrepayment.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetMultibanco}"
                                                        id="novalnetMultibanco" value="novalnetMultibanco"
                                                        logoImage="novalnetMultibanco.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetIdeal}"
                                                        id="novalnetIdeal" value="novalnetIdeal"
                                                        logoImage="novalnetIdeal.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetPrzelewy24}"
                                                        id="novalnetPrzelewy24" value="novalnetPrzelewy24"
                                                        logoImage="novalnetPrzelewy24.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetEps}"
                                                        id="novalnetEps" value="novalnetEps" logoImage="novalnetEps.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption
                                                        config="${novalnetOnlineBankTransfer}"
                                                        id="novalnetOnlineBankTransfer"
                                                        value="novalnetOnlineBankTransfer"
                                                        logoImage="novalnetOnlineBankTransfer.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetBancontact}"
                                                        id="novalnetBancontact" value="novalnetBancontact"
                                                        logoImage="novalnetBancontact.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetPostFinance}"
                                                        id="novalnetPostFinance" value="novalnetPostFinance"
                                                        logoImage="novalnetPostFinance.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetPostFinanceCard}"
                                                        id="novalnetPostFinanceCard" value="novalnetPostFinanceCard"
                                                        logoImage="novalnetPostFinanceCard.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}" />

                                                    <novalnetPayment:paymentOption config="${novalnetGooglePay}"
                                                        id="novalnetGooglePay" value="novalnetGooglePay"
                                                        logoImage="novalnetGooglePay.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}"
                                                        zeroAmountBooking="${novalnetGooglePayZeroAmountBooking}" />

                                                    <novalnetPayment:paymentOption config="${novalnetApplePay}"
                                                        id="novalnetApplePay" value="novalnetApplePay"
                                                        logoImage="novalnetApplePay.png"
                                                        showLogo="${novalnetBaseStoreConfiguration.novalnetPaymentLogo}"
                                                        zeroAmountBooking="${novalnetApplePayZeroAmountBooking}" />
                                                </div>
                                            </c:if>
                                            <p>
                                                <spring:theme
                                                    code="checkout.multi.paymentMethod.seeOrderSummaryForMoreInformation" />
                                            </p>
                                        </div>
                                    </form:form>
                                    <button type="button"
                                            class="btn btn-primary btn-block submit_novalnetPaymentDetailsForm checkout-next" id="submit_novalnetPaymentDetailsForm">
                                        <spring:theme code="checkout.multi.paymentMethod.continue"/>
                                    </button>
                                </ycommerce:testId>
                            </div>
                        </div>

                        <c:if test="${not empty paymentInfos}">
                            <div id="savedpayments">
                                <div id="savedpaymentstitle">
                                    <div class="headline">
                                        <span class="headline-text"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.useSavedCard"/></span>
                                    </div>
                                </div>
                                <div id="savedpaymentsbody">
                                    <spring:url var="choosePaymentMethod"
                                        value="{contextPath}/checkout/multi/payment-method/choose" htmlEscape="false">
                                        <spring:param name="contextPath" value="${request.contextPath}" />
                                    </spring:url>
                                    <c:forEach items="${paymentInfos}" var="paymentInfo" varStatus="status">
                                        <form action="${fn:escapeXml(choosePaymentMethod)}" method="GET">
                                            <input type="hidden" name="selectedPaymentMethodId" value="${fn:escapeXml(paymentInfo.id)}"/>
                                            <strong>${fn:escapeXml(paymentInfo.billingAddress.firstName)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.lastName)}</strong><br/>
                                            ${fn:escapeXml(paymentInfo.cardType)}<br/>
                                            ${fn:escapeXml(paymentInfo.accountHolderName)}<br/>
                                            ${fn:escapeXml(paymentInfo.cardNumber)}<br/>
                                            <spring:theme code="checkout.multi.paymentMethod.paymentDetails.expires"
                                                arguments="${paymentInfo.expiryMonth},${paymentInfo.expiryYear}" /><br/>
                                            ${fn:escapeXml(paymentInfo.billingAddress.line1)}<br/>
                                            ${fn:escapeXml(paymentInfo.billingAddress.town)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.region.isocodeShort)}<br/>
                                            ${fn:escapeXml(paymentInfo.billingAddress.postalCode)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.country.isocode)}<br/>
                                            <button type="submit" class="btn btn-primary btn-block" tabindex="${(status.count * 2) - 1}"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.useThesePaymentDetails"/></button>

                                            <button type="button" class="btn btn-primary btn-block submit_silentOrderPostForm checkout-next">
                                                <spring:theme code="checkout.multi.paymentMethod.continue"/>
                                            </button>
                                        </form>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
                    </ycommerce:testId>
                </jsp:body>
            </multiCheckout:checkoutSteps>
        </div>

        <div class="col-sm-6 hidden-xs">
            <multiCheckout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true"
                showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
        </div>

        <div class="col-sm-12 col-lg-12">
            <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
                <cms:component component="${feature}" />
            </cms:pageSlot>
        </div>
    </div>
</template:page>
