<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="multiCheckoutNovalnet" tagdir="/WEB-INF/tags/addons/novalnetcheckoutaddon/responsive/checkout/multi" %>

<c:if test="${novalnetGuaranteedInvoice.active == true}">
    <input type="hidden" name="invoiceforceGuaranteeCheck" id="invoiceforceGuaranteeCheck" value="${novalnetGuaranteedInvoice.novalnetForceGuarantee}">
</c:if>

<c:if test="${novalnetGuaranteedInvoice.active == true && orderAmountCent >= novalnetGuaranteedInvoiceMinAmount}">
    <input type="hidden" name="invoiceGuaranteeCheck" id="invoiceGuaranteeCheck" value="1">
    <div class="novalnetGuaranteedInvoice">
        <div class="novalnet-select-payment">
            <form:radiobutton path="selectedPaymentMethodId" id="novalnetGuaranteedInvoice"
                value="novalnetGuaranteedInvoice" label="${novalnetGuaranteedInvoice.name}" />
            &nbsp;&nbsp;
            <c:if test="${novalnetBaseStoreConfiguration.novalnetPaymentLogo == true}">
                <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGuaranteedInvoice.png" />
            </c:if>
            &nbsp;&nbsp;
        </div>

        <div id="novalnetGuaranteedInvoicePaymentForm" style="display:none;" class="novalnetPaymentForm">
            <c:if test="${novalnetGuaranteedInvoice.novalnetTestMode == true}">
                <div id="testModeText">
                    <spring:theme code="novalnet.testModeText" /><br/>
                </div><br/>
            </c:if>
            <c:if test="${novalnetGuaranteedInvoice.novalnetEndUserInfo != null}">
                ${novalnetGuaranteedInvoice.novalnetEndUserInfo}<br/>
            </c:if>

            <div class="form-group">
                <multiCheckoutNovalnet:formDate path="novalnetGuaranteedInvoiceDateOfBirth"
                    idKey="novalnetGuaranteedInvoiceDateOfBirth" labelKey="novalnet.dob" inputCSS="" labelCSS="" />
            </div>
            <div class="description novalnet-info-box">${novalnetGuaranteedInvoice.description}</div>
        </div>
    </div>
</c:if>