<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="multiCheckoutNovalnet" tagdir="/WEB-INF/tags/addons/novalnetcheckoutaddon/responsive/checkout/multi" %>

<c:if test="${novalnetGuaranteedDirectDebitSepa.active == true}">
<input type="hidden" name="sepaforceGuaranteeCheck" id="sepaforceGuaranteeCheck" value="${novalnetGuaranteedDirectDebitSepa.novalnetForceGuarantee}">
</c:if>

<c:if
    test="${novalnetGuaranteedDirectDebitSepa.active == true && orderAmountCent > novalnetGuaranteedDirectDebitSepaMinAmount}">
    <input type="hidden" name="sepaGuaranteeCheck" id="sepaGuaranteeCheck" value="1">
    <div class="novalnetGuaranteedDirectDebitSepa">
        <div class="novalnet-select-payment">
            <form:radiobutton path="selectedPaymentMethodId" id="novalnetGuaranteedDirectDebitSepa"
                value="novalnetGuaranteedDirectDebitSepa" label="${novalnetGuaranteedDirectDebitSepa.name}" />
            &nbsp;&nbsp;
            <c:if test="${novalnetBaseStoreConfiguration.novalnetPaymentLogo == true}">
                <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGuaranteedDirectDebitSepa.png" />
            </c:if>
            &nbsp;&nbsp;
        </div>

        <div id="novalnetGuaranteedDirectDebitSepaPaymentForm" style="display:none;" class="novalnetPaymentForm">
            <c:if test="${novalnetGuaranteedDirectDebitSepa.novalnetTestMode == true}">
                <div id="testModeText">
                    <spring:theme code="novalnet.testModeText" /><br/>
                </div>
            </c:if>
            <c:if test="${novalnetGuaranteedDirectDebitSepa.novalnetEndUserInfo != null}">
                ${novalnetGuaranteedDirectDebitSepa.novalnetEndUserInfo}<br/>
            </c:if>

            <c:choose>
                <c:when test="${novalnetGuaranteedDirectDebitSepaOneClick == true}">
                    <div class="novalnetOneClickTokens">
                        <form:radiobutton path="guaranteedDirectDebitSepaOneClickData1"
                            id="GuaranteedDirectDebitSepaOneClickData1" value="1"
                            label="IBAN ${novalnetDirectDebitSepaAccountIban}" tabindex="12" />
                        <span class="novalnet-delete-saved glyphicon glyphicon-trash" data-payment="novalnetGuaranteedDirectDebitSepa" data-index="1" title="Delete saved payment information" style="cursor:pointer;color:red;margin-left:10px;"></span>
                        <br/>
                        <c:if test="${novalnetDirectDebitSepaOneClickToken2 != null}">
                            <form:radiobutton path="guaranteedDirectDebitSepaOneClickData1"
                                id="guaranteedDirectDebitSepaOneClickData2" value="2"
                                label="IBAN ${novalnetDirectDebitSepaAccountIban2}" tabindex="12" />
                            <span class="novalnet-delete-saved glyphicon glyphicon-trash" data-payment="novalnetGuaranteedDirectDebitSepa" data-index="2" title="Delete saved payment information" style="cursor:pointer;color:red;margin-left:10px;"></span>
                            <br/>
                        </c:if>
                        <form:radiobutton path="guaranteedDirectDebitSepaOneClickData1"
                            id="guaranteedDirectDebitSepaOneClickData1AddNew" value="3" label="${sepaAddNew}"
                            tabindex="12" />
                    </div>
                    <div class="novalnetGuaranteedDirectDebitSepaOneClickForm" style="display:none">
                        <div class="form-group">
                            <formElement:formInputBox idKey="guaranteeAccountHolder" labelKey="novalnet.account.holder"
                                path="guaranteeAccountHolder" inputCSS="form-control" placeholder="Account Holder"
                                tabindex="1" mandatory="true" />
                            <formElement:formInputBox idKey="guaranteeAccountIban" labelKey="novalnet.iban"
                                path="guaranteeAccountIban" inputCSS="form-control"
                                placeholder="DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                        </div>
                        <div id="guaranteeAccountBic" class="form-group">
                            <formElement:formInputBox idKey="guaranteeAccountBic" labelKey="novalnet.bic"
                                path="guaranteeAccountBic" inputCSS="form-control" placeholder="XXXX XX XX XXX"
                                tabindex="2" mandatory="true" />
                        </div>
                        <br/>
                        <formElement:formCheckbox path="guaranteedDirectDebitSepaSaveData"
                            idKey="guaranteedDirectDebitSepaSaveData" labelKey="novalnet.aftersaveData" tabindex="11" />
                    </div>
                </c:when>
                <c:otherwise>
                    <span id="novalnetDirectDebitSepaPaymentFormElements">
                        <div class="form-group">
                            <formElement:formInputBox idKey="guaranteeAccountHolder" labelKey="novalnet.account.holder" path="guaranteeAccountHolder" inputCSS="form-control" placeholder="Account Holder" tabindex="1" mandatory="true"/>
                            <formElement:formInputBox idKey="guaranteeAccountIban" labelKey="novalnet.iban" path="guaranteeAccountIban" inputCSS="form-control" placeholder="DE00 0000 0000 0000 0000 00" tabindex="2" mandatory="true" />
                        </div>
                        <div id="guaranteeAccountBic" class="form-group">
                            <formElement:formInputBox idKey="guaranteeAccountBic" labelKey="novalnet.bic" path="guaranteeAccountBic" inputCSS="form-control" placeholder="XXXX XX XX XXX" tabindex="2" mandatory="true" />
                        </div>
                        <br/>
                    </span>
                    <c:if test="${novalnetGuaranteedDirectDebitSepaOneClickEnabled == true}">
                        <formElement:formCheckbox path="guaranteedDirectDebitSepaSaveData"
                            idKey="guaranteedDirectDebitSepaSaveData" labelKey="novalnet.beforesaveData"
                            tabindex="11" />
                    </c:if>
                </c:otherwise>
            </c:choose>

            <div class="form-group">
                <multiCheckoutNovalnet:formDate path="novalnetGuaranteedDirectDebitSepaDateOfBirth"
                    idKey="novalnetGuaranteedDirectDebitSepaDateOfBirth" labelKey="novalnet.dob" inputCSS=""
                    labelCSS="" />
            </div>

            <div class="description novalnet-info-box">
                <ul>
                    <li>${novalnetGuaranteedDirectDebitSepa.description}</li> <br/>
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
            </div><br/>
        </div>
    </div>
</c:if>
