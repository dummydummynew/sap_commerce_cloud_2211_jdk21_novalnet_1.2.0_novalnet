<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>

<c:if test="${novalnetCreditCard.active == true}">
    <div class="novalnet-select-payment">
        <form:radiobutton path="selectedPaymentMethodId" id="novalnetCreditCard" value="novalnetCreditCard"
            label="${novalnetCreditCard.name}" />
        &nbsp;&nbsp;<a href=<spring:theme code="http://www.novalnet.com" /> target="_new">
        <c:if test="${novalnetBaseStoreConfiguration.novalnetPaymentLogo == true}">
            <c:choose>
                <c:when test="${novalnetCreditCard.novalnetAmexLogo == true}">
                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_cc_visa_master_amex.png" />
                </c:when>
                <c:otherwise>
                    <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_cc_visa_master.png" />
                </c:otherwise>
            </c:choose>
        </c:if>
        </a>&nbsp;&nbsp;

        <div id="novalnetCreditCardPaymentForm" style="display:none;" class="novalnetPaymentForm">
            <c:if test="${novalnetCreditCard.novalnetTestMode == true}">
                <input type="hidden" id="novalnetTestMode" value="1"/>
                <div id="testModeText">
                    <spring:theme code="novalnet.testModeText" /><br/>
                </div>
            </c:if>
            <c:if test="${novalnetCreditCard.novalnetEndUserInfo != null}">
                ${novalnetCreditCard.novalnetEndUserInfo} <br/>
            </c:if>

            <form:hidden path="novalnetCreditCardPanHash" id="novalnetCreditCardPanHash" />
            <form:hidden path="novalnetCreditCardUniqueId" id="novalnetCreditCardUniqueId" />
            <form:hidden path="do_redirect" id="do_redirect" />

            <input type="hidden" id="novalnetStandardLabelCss" value="${novalnetCreditCard.novalnetStandardLabelCss}"/>
            <input type="hidden" id="novalnetStandardInputCss" value="${novalnetCreditCard.novalnetStandardInputCss}"/>
            <input type="hidden" id="novalnetStandardCss" value="${novalnetCreditCard.novalnetStandardCss}"/>
            <c:if test="${novalnetCreditCard.novalnetInlineCC == true}">
                <input type="hidden" id="novalnetInlineCC" value="1"/>
            </c:if>
            <input type="hidden" id="lang" value="${lang}"/>
            <input type="hidden" id="orderAmount" value="${orderAmountCent}"/>
            <input type="hidden" id="currency" value="${currency}"/>
            <input type="hidden" id="Clientkey" value="${novalnetBaseStoreConfiguration.novalnetClientKey}"/>

            <c:choose>
                <c:when test="${novalnetCreditCardOneClick == true}">
                    <div class="novalnetOneClickTokens">
                        <form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickData1" value="1"
                            label="${novalnetCreditCardOneClickCardType} ${endswith} ${novalnetCreditCardOneClickMaskedCardNumber} ( ${expires} ${novalnetCreditCardOneClickCardExpiry}) "
                            tabindex="12" />
                            <span class="novalnet-delete-saved glyphicon glyphicon-trash"
      data-payment="novalnetCreditCard"
      data-index="1"
      title="Delete saved payment information"
      style="cursor:pointer;color:red;margin-left:10px;">
</span>
                            
                        <br/>
                        <c:if test="${novalnetCreditCardOneClickToken2 != null}">
                            <form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickData2" value="2"
                                label="${novalnetCreditCardOneClickCardType2} ${endswith} ${novalnetCreditCardOneClickMaskedCardNumber2}  ( ${expires} ${novalnetCreditCardOneClickCardExpiry2}) "
                                tabindex="12" />
                                
                                <span class="novalnet-delete-saved glyphicon glyphicon-trash"
      data-payment="novalnetCreditCard"
      data-index="2"
      title="Delete saved payment information"
      style="cursor:pointer;color:red;margin-left:10px;">
</span>
                            <br/>
                        </c:if>
                        <form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickNewDeatails" value="3"
                            label="${creditcardAddNew}" tabindex="12" />
                    </div>
                    <div class="novalnetCreditCardOneClickForm" style="display:none">
                        <a href="" class="help js-cart-help" data-help="">
                            <spring:theme code="novalnet.creditcardOneClickInfo.text" />
                            <span class="glyphicon glyphicon-question-sign"></span>
                        </a>
                        <div class="help-popup-content-holder js-help-popup-content novalnet-hide">
                            <div class="help-popup-content">
                                <spring:theme code="novalnet.creditcardOneClickInfo.description" />
                            </div>
                        </div>
                        <br/><br/>
                        <div id="nn_overlay"></div>
                        <iframe id="novalnetCreditCardIframe" frameborder="0" scrolling="no"
                            style="min-width:40%;max-height:200px;"></iframe><br/>
                        <formElement:formCheckbox path="creditcardSaveData" idKey="creditcardSaveData"
                            labelKey="novalnet.creditcard.saveData" tabindex="11" />
                    </div>
                </c:when>
                <c:otherwise>
                    <span id="novalnetCreditCardPaymentFormElements">
                        <a href="" class="help js-cart-help" data-help=""><spring:theme code="novalnet.creditcardOneClickInfo.text"/><span class="glyphicon glyphicon-question-sign"></span></a>
                    <div class="help-popup-content-holder js-help-popup-content novalnet-hide">
                        <div class="help-popup-content">
                            <spring:theme code="novalnet.creditcardOneClickInfo.description" />
                        </div>
                    </div>
                    <br/><br/>
                    <div id="nn_overlay"></div>
                    <iframe id="novalnetCreditCardIframe" frameborder="0" scrolling="no"
                        style="min-width:40%;"></iframe>
                    <c:if test="${novalnetCreditCardOneClickEnabled == true}">
                        <formElement:formCheckbox path="creditcardSaveData" idKey="creditcardSaveData"
                            labelKey="novalnet.creditcard.saveData" tabindex="11" />
                    </c:if>
                    </span>
                </c:otherwise>
            </c:choose>

            <div class="description novalnet-info-box">
                <ul>
                    <li>${novalnetCreditCard.description}</li>
                    <c:if test="${novalnetCreditCardZeroAmountBooking}">
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
