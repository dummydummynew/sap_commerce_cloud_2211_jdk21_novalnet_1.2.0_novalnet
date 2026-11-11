<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>

<c:if test="${novalnetDirectDebitAch.active == true}">
    <div class="novalnetDirectDebitAch" style="display:none;">
        <div class="novalnet-select-payment">
            <form:radiobutton path="selectedPaymentMethodId" id="novalnetDirectDebitAch" value="novalnetDirectDebitAch"
                label="${novalnetDirectDebitAch.name}" />
            &nbsp;&nbsp;
            <c:if test="${novalnetBaseStoreConfiguration.novalnetPaymentLogo == true}">
                <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_ach.png" />
            </c:if>
            &nbsp;&nbsp;
        </div>

        <div id="novalnetDirectDebitAchPaymentForm" style="display:none;" class="novalnetPaymentForm">
            <c:if test="${novalnetDirectDebitAch.novalnetTestMode == true}">
                <div id="testModeText">
                    <spring:theme code="novalnet.testModeText" />
                </div><br/>
            </c:if>
            <c:if test="${novalnetDirectDebitAch.novalnetEndUserInfo != null}">
                ${novalnetDirectDebitAch.novalnetEndUserInfo}<br/>
            </c:if>

            <c:choose>
                <c:when test="${novalnetDirectDebitAchOneClick == true}">
                    <div class="novalnetOneClickTokens">
                        <form:radiobutton path="directDebitAchOneClickData1" id="novalnetDirectDebitAchOneClickData1"
                            value="1" label=" Account No  ${novalnetDirectDebitAchAccountNumber}" tabindex="12" />
                        <span class="novalnet-delete-saved glyphicon glyphicon-trash" data-payment="novalnetDirectDebitAch" data-index="1" title="Delete saved payment information" style="cursor:pointer;color:red;margin-left:10px;"></span>
                        <br/>
                        <c:if test="${novalnetDirectDebitAchOneClickToken2 != null}">
                            <form:radiobutton path="directDebitAchOneClickData1"
                                id="novalnetDirectDebitAchOneClickData2" value="2"
                                label=" Account No ${novalnetDirectDebitAchAccountNumber2}" tabindex="12" />
                            <span class="novalnet-delete-saved glyphicon glyphicon-trash" data-payment="novalnetDirectDebitAch" data-index="2" title="Delete saved payment information" style="cursor:pointer;color:red;margin-left:10px;"></span>
                            <br/>
                        </c:if>
                        <form:radiobutton path="directDebitAchOneClickData1"
                            id="novalnetDirectDebitAchOneClickData1AddNew" value="3" label="${achaddnew}"
                            tabindex="12" />
                    </div>
                    <div class="novalnetDirectDebitAchOneClickForm" style="display:none">
                        <div class="form-group">
                            <formElement:formInputBox idKey="novalnetAchAccountHolder"
                                labelKey="novalnet.novalnetAchAccountHolder" path="AchAccountHolder"
                                inputCSS="form-control" placeholder="Account Holder" tabindex="1" mandatory="true" />
                            <formElement:formInputBox idKey="novalnetAchAccountNumber"
                                labelKey="novalnet.novalnetAchAccountNumber" path="AchAccountNumber"
                                inputCSS="form-control" placeholder="123456789" tabindex="2" mandatory="true" />
                            <formElement:formInputBox idKey="novalnetAchRoutingNumber"
                                labelKey="novalnet.novalnetAchRoutingNumber" path="AchRoutingNumber"
                                inputCSS="form-control" placeholder="123456789" tabindex="3" mandatory="true" />
                        </div>
                        <br/>
                        <formElement:formCheckbox path="directDebitAchSaveData" idKey="directDebitAchSaveData"
                            labelKey="novalnet.aftersaveData" tabindex="11" />
                    </div>
                </c:when>
                <c:otherwise>
                    <span id="novalnetDirectDebitAchPaymentFormElements">
                        <div class="form-group">
                            <formElement:formInputBox idKey="novalnetAchAccountHolder" labelKey="novalnet.novalnetAchAccountHolder" path="AchAccountHolder" inputCSS="form-control" placeholder="Account Holder" tabindex="1" mandatory="true" />
                            <formElement:formInputBox idKey="novalnetAchAccountNumber" labelKey="novalnet.novalnetAchAccountNumber" path="AchAccountNumber" inputCSS="form-control" placeholder="123456789" tabindex="2" mandatory="true" />
                            <formElement:formInputBox idKey="novalnetAchRoutingNumber" labelKey="novalnet.novalnetAchRoutingNumber" path="AchRoutingNumber" inputCSS="form-control" placeholder="123456789" tabindex="3" mandatory="true" />
                        </div>
                        <br/>
                    </span>
                    <c:if test="${novalnetDirectDebitAchOneClickEnabled == true}">
                        <formElement:formCheckbox path="directDebitAchSaveData" idKey="directDebitAchSaveData"
                            labelKey="novalnet.beforesaveData" tabindex="11" />
                    </c:if>
                </c:otherwise>
            </c:choose>

            <div class="description novalnet-info-box">
                <ul>
                    <li>${novalnetDirectDebitAch.description}</li>
                    <c:if test="${novalnetDirectDebitAchZeroAmountBooking}">
                        <br/>
                        <li>
                            <spring:theme code="novalnetZeroAmountBooking.description" />
                        </li>
                    </c:if>
                </ul>
            </div>
        </div>
    </div>
</c:if>
