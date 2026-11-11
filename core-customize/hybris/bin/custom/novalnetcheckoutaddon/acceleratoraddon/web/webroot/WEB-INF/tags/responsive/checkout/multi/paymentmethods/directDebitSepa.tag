<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>

<c:if test="${novalnetDirectDebitSepa.active == true}">
    <div class="novalnetSepa" style="display:none;">
        <div class="novalnet-select-payment">
            <form:radiobutton path="selectedPaymentMethodId" id="novalnetDirectDebitSepa"
                value="novalnetDirectDebitSepa" label="${novalnetDirectDebitSepa.name}" />
            &nbsp;&nbsp;
            <c:if test="${novalnetBaseStoreConfiguration.novalnetPaymentLogo == true}">
                <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetDirectDebitSepa.png" />
            </c:if>
            &nbsp;&nbsp;
        </div>

        <div id="novalnetDirectDebitSepaPaymentForm" style="display:none;" class="novalnetPaymentForm">
            <c:if test="${novalnetDirectDebitSepa.novalnetTestMode == true}">
                <div id="testModeText">
                    <spring:theme code="novalnet.testModeText" />
                </div><br/>
            </c:if>
            <c:if test="${novalnetDirectDebitSepa.novalnetEndUserInfo != null}">
                ${novalnetDirectDebitSepa.novalnetEndUserInfo}<br/>
            </c:if>

            <c:choose>
                <c:when test="${novalnetDirectDebitSepaOneClick == true}">
                    <div class="novalnetOneClickTokens">
                        <form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1"
                            value="1" label="IBAN  ${novalnetDirectDebitSepaAccountIban}" tabindex="12" />
                        <span class="novalnet-delete-saved glyphicon glyphicon-trash" data-payment="novalnetDirectDebitSepa" data-index="1" title="Delete saved payment information" style="cursor:pointer;color:red;margin-left:10px;"></span>
                        <br/>
                        <c:if test="${novalnetDirectDebitSepaOneClickToken2 != null}">
                            <form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData2"
                                value="2" label="IBAN ${novalnetDirectDebitSepaAccountIban2}" tabindex="12" />
                            <span class="novalnet-delete-saved glyphicon glyphicon-trash" data-payment="novalnetDirectDebitSepa" data-index="2" title="Delete saved payment information" style="cursor:pointer;color:red;margin-left:10px;"></span>
                            <br/>
                        </c:if>
                        <form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1AddNew"
                            value="3" label="${sepaAddNew}" tabindex="12" />
                    </div>
                    <div class="novalnetDirectDebitSepaOneClickForm" style="display:none">
                        <div class="form-group">
                            <formElement:formInputBox idKey="accountHolder" labelKey="novalnet.account.holder"
                                path="accountHolder" inputCSS="form-control" placeholder="Account Holder" tabindex="1"
                                mandatory="true" />
                            <formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban"
                                inputCSS="form-control" placeholder="DE00 0000 0000 0000 0000 00" tabindex="2"
                                mandatory="true" />
                        </div>
                        <div id="accountBic" class="form-group">
                            <formElement:formInputBox idKey="accountBic" labelKey="novalnet.bic" path="accountBic"
                                inputCSS="form-control" placeholder="XXXX XX XX XXX" tabindex="2" mandatory="true" />
                        </div>
                        <br/>
                        <formElement:formCheckbox path="directDebitSepaSaveData" idKey="directDebitSepaSaveData"
                            labelKey="novalnet.aftersaveData" tabindex="11" />
                    </div>
                </c:when>
                <c:otherwise>
                    <span id="novalnetDirectDebitSepaPaymentFormElements">
                        <div class="form-group">
                            <formElement:formInputBox idKey="accountHolder" labelKey="novalnet.account.holder" path="accountHolder" inputCSS="form-control" placeholder="Account Holder" tabindex="1" mandatory="true" />
                            <formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban" inputCSS="form-control" placeholder="DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                        </div>
                        <div id="accountBic" class="form-group">
                            <formElement:formInputBox idKey="accountBic" labelKey="novalnet.bic" path="accountBic" inputCSS="form-control" placeholder="XXXX XX XX XXX" tabindex="2" mandatory="true" />
                        </div>
                        <br/>
                    </span>
                    <c:if test="${novalnetDirectDebitSepaOneClickEnabled == true}">
                        <formElement:formCheckbox path="directDebitSepaSaveData" idKey="directDebitSepaSaveData"
                            labelKey="novalnet.beforesaveData" tabindex="11" />
                    </c:if>
                </c:otherwise>
            </c:choose>

            <div class="description novalnet-info-box">
                <ul>
                    <li>${novalnetDirectDebitSepa.description}</li>
                    <c:if test="${novalnetDirectDebitSepaZeroAmountBooking}">
                        <br/>
                        <li>
                            <spring:theme code="novalnetZeroAmountBooking.description" />
                        </li>
                        <br/>
                    </c:if>
                    <li>
                        <div class="form-group">
                            <a id="novalnet-sepa-mandate" style="cursor:pointer;"
                                onclick="jQuery('#novalnet-about-mandate').toggle();">
                                <spring:theme code="novalnet.sepaNotificationText" />
                            </a>
                        </div>
                        <div class="form-group" id="novalnet-about-mandate" style="display:none;">
                            <spring:theme code="novalnet.sepaAboutMandateDescOne" /><br/><br/>
                            <strong><spring:theme code="novalnet.sepaAboutMandateDescTwo"/></strong><br/><br/>
                            <spring:theme code="novalnet.sepaAboutMandateDescThree" />
                        </div>
                    </li>
                </ul>
            </div>
        </div>
    </div>
</c:if>
