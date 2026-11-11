<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ attribute name="config" required="true" type="java.lang.Object" %>
<%@ attribute name="id" required="true" type="java.lang.String" %>
<%@ attribute name="value" required="true" type="java.lang.String" %>
<%@ attribute name="logoImage" required="false" type="java.lang.String" %>
<%@ attribute name="logoWidth" required="false" type="java.lang.String" %>
<%@ attribute name="logoHeight" required="false" type="java.lang.String" %>
<%@ attribute name="showLogo" required="true" type="java.lang.Boolean" %>
<%@ attribute name="zeroAmountBooking" required="false" type="java.lang.Boolean" %>
<%@ attribute name="useBulletDescription" required="false" type="java.lang.Boolean" %>

<c:if test="${config.active == true}">
    <div class="novalnet${id}">
        <div class="novalnet-select-payment">
            <form:radiobutton path="selectedPaymentMethodId" id="${id}" value="${value}" label="${config.name}"/>
            &nbsp;&nbsp;
            <c:if test="${showLogo == true && not empty logoImage}">
                <img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/${logoImage}"
                     <c:if test="${not empty logoWidth}">style="width:${logoWidth};height:${logoHeight};"</c:if>/>
            </c:if>
            &nbsp;&nbsp;
        </div>

        <div id="${id}PaymentForm" style="display:none;" class="novalnetPaymentForm">
            <c:if test="${config.novalnetTestMode == true}">
                <div id="testModeText">
                    <spring:theme code="novalnet.testModeText"/><br/>
                </div><br/>
            </c:if>
            <c:if test="${config.novalnetEndUserInfo != null}">
                ${config.novalnetEndUserInfo}<br/>
            </c:if>

			<div class="description novalnet-info-box">
			    <ul>
			        <li>${config.description}</li>
			        <c:if test="${zeroAmountBooking == true}">
			            <br/>
			            <li><spring:theme code="novalnetZeroAmountBooking.description"/></li>
			        </c:if>
			    </ul>
			</div>
        </div>
    </div>
</c:if>