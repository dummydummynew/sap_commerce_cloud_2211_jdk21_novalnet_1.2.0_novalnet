<%@ page trimDirectiveWhitespaces="true" pageEncoding="UTF-8" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags"%>
<%@ taglib prefix="multi-checkout" tagdir="/WEB-INF/tags/responsive/checkout/multi"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%@ taglib prefix='c' uri='http://java.sun.com/jsp/jstl/core' %>
<script src="https://cdn.novalnet.de/js/v3/payment-1.1.2.js"></script>
<spring:htmlEscape defaultHtmlEscape="true" />

<spring:url value="/checkout/multi/novalnet/summary/placeOrder" var="placeOrderUrl" htmlEscape="false"/>
<spring:url value="/checkout/multi/termsAndConditions" var="getTermsAndConditionsUrl" htmlEscape="false"/>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">

    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout" />
            </div>
            <multi-checkout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <ycommerce:testId code="checkoutStepFour">
                    <div class="checkout-review hidden-xs">
                        <div class="checkout-order-summary">
                            <multi-checkout:orderTotals cartData="${cartData}" showTaxEstimate="${showTaxEstimate}"
                                showTax="${showTax}" subtotalsCssClasses="dark" />
                        </div>
                    </div>
                    <div class="place-order-form hidden-xs">
                        <form:form action="${placeOrderUrl}" id="placeOrderForm1" modelAttribute="placeOrderForm">
                            <div class="checkbox">
                                <label> <form:checkbox id="Terms1" path="termsCheck" />
                                <spring:theme var="termsAndConditionsHtml" code="checkout.summary.placeOrder.readTermsAndConditions" arguments="${fn:escapeXml(getTermsAndConditionsUrl)}" htmlEscape="false"/>
                                ${ycommerce:sanitizeHTML(termsAndConditionsHtml)}
                            </label>
                                <spring:message code='checkout.error.terms.not.accepted' var="termsCheckErrorMessage" />
                                <input type="hidden" id="termsCheckErrorMessage" value="${termsCheckErrorMessage}"/>
                            </div>

                            <c:choose>
                                <c:when test="${currentPayment == 'novalnetGooglePay'}">

                                    <input type="hidden" id="lang" value="${lang}"/>
                                    <input type="hidden" id="orderAmount" value="${orderAmount}"/>
                                    <input type="hidden" id="currency" value="${currency}"/>
                                    <input type="hidden" id="countryCode" value="${countryCode}"/>
                                    <input type="hidden" id="currentPayment" value="${currentPayment}"/>
                                    <input type="hidden" id="Clientkey" value="${novalnetBaseStoreConfiguration.novalnetClientKey}"/>
                                    <input type="hidden" id="testMode" value="${novalnetGooglePay.novalnetTestMode}"/>
                                    <input type="hidden" id="enforce3D" value="${novalnetGooglePay.novalnetEnforce3D}"/>
                                    <input type="hidden" id="merchantId" value="${novalnetGooglePay.merchantId}"/>
                                    <input type="hidden" id="novalnetGooglepayButtonType" value="${fn:toLowerCase(novalnetGooglePay.novalnetGooglepayButtonType)}"/>
                                    <input type="hidden" id="novalnetGooglepayButtonHeight" value="${novalnetGooglePay.novalnetGooglepayButtonHeight}"/>
                                    <input type="hidden" id="orderSubtotal" value="${cartData.subTotal.value * 100}"/>
                                    <input type="hidden" id="orderShipping" value="${cartData.deliveryCost.value * 100}"/>
                                    <input type="hidden" id="orderTax"      value="${cartData.totalTax.value * 100}"/>
                                    <input type="hidden" id="productDetails" value='${fn:escapeXml(productDetails)}'/>

                                    <div id="wallet_container"></div>

                                    <button id="placeOrder" type="submit" class="btn btn-primary btn-place-order btn-block" style="display:none">
									<spring:theme code="checkout.summary.placeOrder" text="Place Order"/>
							 </button>
                                </c:when>
                                <c:otherwise>
                                    <button id="placeOrder" type="submit" class="btn btn-primary btn-place-order btn-block">
									<spring:theme code="checkout.summary.placeOrder" text="Place Order"/>
								</button>
                                </c:otherwise>
                            </c:choose>
                        </form:form>
                    </div>
                </ycommerce:testId>
            </multi-checkout:checkoutSteps>
        </div>

        <div class="col-sm-6">
            <multi-checkout:checkoutOrderSummary cartData="${cartData}" showDeliveryAddress="true"
                showPaymentInfo="true" showTaxEstimate="true" showTax="true" />
        </div>

        <div class="col-sm-12 col-lg-12">
            <br class="hidden-lg">
            <cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
                <cms:component component="${feature}" />
            </cms:pageSlot>
        </div>
    </div>
</template:page>