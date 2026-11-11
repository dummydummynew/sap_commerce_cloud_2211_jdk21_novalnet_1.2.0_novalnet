/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.service.order.impl;

import com.novalnet.dao.NovalnetCallbackDao;
import com.novalnet.dao.NovalnetDao;
import com.novalnet.model.NovalnetCallbackInfoModel;
import com.novalnet.model.NovalnetPaymentInfoModel;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercewebservicescommons.errors.exceptions.RequestParameterException;
import de.hybris.platform.core.PK;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.core.enums.PaymentStatus;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.orderhistory.model.OrderHistoryEntryModel;
import de.hybris.platform.servicelayer.i18n.CommonI18NService;
import de.hybris.platform.servicelayer.model.ModelService;
import de.hybris.platform.util.localization.Localization;
import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class DefaultNovalnetCallbackOrderServiceTest {
    private static final String ORDER_CODE = "100001";
    private static final String BANK_DETAILS_COMMENTS1_KEY = "novalnet.bankDetailsComments1";
    private static final String BANK_DETAILS_COMMENTS1_TEMPLATE = "Please transfer %s";
    private static final String BANK_DETAILS_COMMENTS2_KEY = "novalnet.bankDetailsComments2";
    private static final String BANK_DETAILS_COMMENTS2_TEMPLATE = "before %s";
    private static final String EMPTY_STRING = "";
    private static final String TWO_SPACES = "  ";
    private static final String PAYMENT_INFO_FIELD = "paymentInfo";
    private static final String UNKNOWN_GATEWAY_STATUS = "SOMETHING_ELSE";
    private static final String INVALID_GATEWAY_STATUS_MESSAGE = "Invalid payment gateway status";
    private static final String GATEWAY_STATUS_PENDING = "PENDING";
    private static final String GATEWAY_STATUS_ON_HOLD = "ON_HOLD";
    private static final String GATEWAY_STATUS_CONFIRMED = "CONFIRMED";
    private static final String GATEWAY_STATUS_FAILURE = "FAILURE";
    private static final String GATEWAY_STATUS_DEACTIVATED = "DEACTIVATED";
    private static final String GATEWAY_STATUS_CONFIRMED_LOWERCASE = "confirmed";
    private static final String WHITESPACE_STRING = " ";
    private static final String EXISTING_NOTES = "Existing notes";
    private static final String NEW_CALLBACK_COMMENT = "New callback";
    private static final String HISTORY_ENTRY_TEXT = "Entry text";
    private static final String EXPECTED_EXISTING_NOTES_WITH_CALLBACK = "Existing notes<br/><br/>New callback";
    private static final String EXPECTED_EMPTY_NOTES_WITH_CALLBACK = "<br/><br/>New callback";
    private static final String SHORT_COMMENT = "c";
    private static final String SHORT_ENTRY = "e";
    private static final String PREVIOUS_NOTES = "Previous";
    private static final String NEW_COMMENT = "New comment";
    private static final String EXPECTED_PREVIOUS_NOTES_WITH_COMMENT = "Previous<br><br>New comment";
    private static final String CURRENCY_ISO_EUR = "EUR";
    private static final String PAYMENT_NAME_LABEL = "novalnet.paymentname";
    private static final String JSON_TID_PREFIX = "{\"tid\":\"";
    private static final String JSON_AMOUNT_10000_SUFFIX = "\",\"amount\":\"10000\"}";
    private static final String AMOUNT_100_EUR = "100.00 EUR";
    private static final String PAYMENT_METHOD_INVOICE = "Invoice";
    private static final String EXPECTED_PAYMENT_HEADER_PREFIX = "novalnet.paymentname : Invoice<br/>novalnet.transactionID ";
    private static final String HTML_LINE_BREAK = "<br/>";
    private static final String JSON_AMOUNT_ZERO_SUFFIX = "\",\"amount\":\"0\"}";
    private static final String ZERO_AMOUNT_EUR = "0.00 EUR";
    private static final String PAYMENT_METHOD_CREDIT_CARD = "Credit Card";
    private static final String ZERO_AMOUNT_NOTE = "novalnet.zeroAmountTransactionText<br/>";
    private static final String JSON_AMOUNT_500_SUFFIX = "\",\"amount\":\"500\"}";
    private static final String AMOUNT_5_EUR = "5.00 EUR";
    private static final String ZERO_AMOUNT_NOTE_KEY = "zeroAmountTransactionText";
    private static final String JSON_TEST_MODE_ENABLED = "\",\"test_mode\":\"1\"}";
    private static final String AMOUNT_1_EUR = "1.00 EUR";
    private static final String TEST_ORDER_NOTE = "novalnet.testOrderText";
    private static final String JSON_TEST_MODE_DISABLED = "\",\"test_mode\":\"0\"}";
    private static final String TEST_ORDER_NOTE_KEY = "testOrderText";
    private static final String JSON_PENDING_WITH_DUE_DATE_PREFIX = "\",\"status\":\"PENDING\",\"status_code\":\"100\",\"due_date\":\"2026-12-31\",";
    private static final String JSON_BANK_DETAILS_PREFIX = "\"bank_details\":{\"account_holder\":\"Novalnet AG\",\"iban\":\"DE89370400440532013000\",\"bic\":\"COBADEFFXXX\",";
    private static final String JSON_BANK_DETAILS_SUFFIX = "\"bank_name\":\"Test Bank\",\"bank_place\":\"Berlin\"}}";
    private static final String EXPECTED_BANK_TRANSFER_DUE_DATE = "<br/><br/>Please transfer 100.00 EUR before 2026-12-31";
    private static final String EXPECTED_ACCOUNT_HOLDER = "<br/>novalnet.bankDetailsAccountHolder Novalnet AG";
    private static final String EXPECTED_IBAN = "<br/>novalnet.bankDetailsIban DE89370400440532013000";
    private static final String EXPECTED_BIC = "<br/>novalnet.bankDetailsBic COBADEFFXXX";
    private static final String EXPECTED_BANK_NAME = "<br/>novalnet.bankDetailsBank Test Bank";
    private static final String EXPECTED_BANK_PLACE = "<br/>novalnet.bankPlace Berlin";
    private static final String BANK_PAYMENT_REFERENCE_TID_PREFIX = "novalnet.bankDetailsPaymentReference : TID ";
    private static final String JSON_ON_HOLD_WITH_DUE_DATE_PREFIX = "\",\"status\":\"ON_HOLD\",\"status_code\":\"100\",\"due_date\":\"2026-12-31\",";
    private static final String JSON_BANK_DETAILS_IBAN_SUFFIX = "\"bank_details\":{\"iban\":\"DE89370400440532013000\"}}";
    private static final String BANK_TRANSFER_INSTRUCTION = "Please transfer 100.00 EUR";
    private static final String DUE_DATE_TEXT = "before 2026-12-31";
    private static final String JSON_PENDING_STATUS_PREFIX = "\",\"status\":\"PENDING\",\"status_code\":\"100\",";
    private static final String JSON_BANK_DETAILS_WITH_BLANK_BIC_SUFFIX = "\"bank_details\":{\"iban\":\"DE89370400440532013000\",\"bic\":\"  \"}}";
    private static final String BANK_DETAILS_IBAN_KEY = "bankDetailsIban";
    private static final String BANK_DETAILS_BIC_KEY = "bankDetailsBic";
    private static final String BANK_DETAILS_ACCOUNT_HOLDER_KEY = "bankDetailsAccountHolder";
    private static final String BANK_PLACE_KEY = "bankPlace";
    private static final String JSON_BANK_DETAILS_WITH_QR_SUFFIX = "\"bank_details\":{\"iban\":\"DE89\",\"qr_image\":\"https://qr.test/code.png\"}}";
    private static final String EXPECTED_QR_CODE_NOTE = "novalnet.qrCodeComments<br/><br/><img alt='nn_qr_code' src='https://qr.test/code.png'><br/>";
    private static final String JSON_BANK_DETAILS_SHORT_IBAN_SUFFIX = "\"bank_details\":{\"iban\":\"DE89\"}}";
    private static final String QR_CODE_ALT_TEXT = "nn_qr_code";
    private static final String JSON_STATUS_CODE_75_PREFIX = "\",\"status_code\":\"75\",";
    private static final String PAYMENT_METHOD_GUARANTEED_INVOICE = "Guaranteed Invoice";
    private static final String GUARANTEE_PENDING_NOTE = "novalnet.guaranteePendingNote";
    private static final String JSON_STATUS_CODE_100_SUFFIX = "\",\"status_code\":\"100\"}";
    private static final String BANK_DETAILS_KEY = "bankDetails";
    private static final String TID = "14500000012345";
    private static final String GUARANTEE_PENDING_NOTE_KEY = "guaranteePendingNote";
    private static final String JSON_PARTNER_PAYMENT_REFERENCE_SUFFIX = "\",\"status_code\":\"100\",\"partner_payment_reference\":\"REF-77\"}";
    private static final String PAYMENT_METHOD_MULTIBANCO = "Multibanco";
    private static final String EXPECTED_MULTIBANCO_NOTE = "<br/><br/>novalnet.multibancocomments1 100.00 EUR novalnet.multibancocomments2<br/>";
    private static final String EXPECTED_PARTNER_PAYMENT_REFERENCE_NOTE = "novalnet.bankDetailsPaymentReference : REF-77";


    private static final Map<String, String> LABEL_TEMPLATES = Map.of( //
            BANK_DETAILS_COMMENTS1_KEY, BANK_DETAILS_COMMENTS1_TEMPLATE, //
            BANK_DETAILS_COMMENTS2_KEY, BANK_DETAILS_COMMENTS2_TEMPLATE);

    @Mock
    private ModelService modelService;
    @Mock
    private NovalnetCallbackDao novalnetCallbackDao;
    @Mock
    private CommonI18NService commonI18NService;
    @Mock
    private NovalnetDao novalnetDao;

    @Mock
    private OrderModel order;
    @Mock
    private NovalnetPaymentInfoModel paymentInfo;
    @Mock
    private NovalnetCallbackInfoModel callbackInfo;
    @Mock
    private OrderHistoryEntryModel historyEntry;
    @Mock
    private LanguageModel language;

    @InjectMocks
    private DefaultNovalnetCallbackOrderService service;

    private MockedStatic<Localization> localization;

    private static JSONObject json(final String content) {
        return new JSONObject(content);
    }

    @BeforeEach
    public void setUp() {
        localization = mockStatic(Localization.class, withSettings().lenient().defaultAnswer(invocation -> {
            final String key = invocation.getArgument(0);
            return LABEL_TEMPLATES.getOrDefault(key, key);
        }));
    }

    @AfterEach
    public void tearDown() {
        localization.close();
    }

    @Test
    public void shouldRejectBlankOrderCodeWhenUpdatingOrderStatus() {
        assertThatThrownBy(() -> service.updateOrderStatus(EMPTY_STRING, paymentInfo, order))
                .isInstanceOf(RequestParameterException.class);
        assertThatThrownBy(() -> service.updateOrderStatus(TWO_SPACES, paymentInfo, order))
                .isInstanceOf(RequestParameterException.class);
        assertThatThrownBy(() -> service.updateOrderStatus(null, paymentInfo, order)).isInstanceOf(RequestParameterException.class);
        verifyNoInteractions(modelService);
    }

    @Test
    public void shouldRejectNullPaymentInfoWhenUpdatingOrderStatus() {
        assertThatThrownBy(() -> service.updateOrderStatus(ORDER_CODE, null, order)).isInstanceOf(RequestParameterException.class)
                .hasMessageContaining(PAYMENT_INFO_FIELD);
        verifyNoInteractions(modelService);
    }

    @Test
    public void shouldRejectUnknownGatewayStatus() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(UNKNOWN_GATEWAY_STATUS);

        assertThatThrownBy(() -> service.updateOrderStatus(ORDER_CODE, paymentInfo, order))
                .isInstanceOf(RequestParameterException.class).hasMessageContaining(INVALID_GATEWAY_STATUS_MESSAGE);
        verifyNoInteractions(modelService);
    }

    @Test
    public void shouldRejectMissingGatewayStatus() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(null);

        assertThatThrownBy(() -> service.updateOrderStatus(ORDER_CODE, paymentInfo, order))
                .isInstanceOf(RequestParameterException.class);
    }

    @Test
    public void shouldMarkPendingOrderAsPaymentNotCaptured() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_PENDING);

        service.updateOrderStatus(ORDER_CODE, paymentInfo, order);

        verify(order).setStatus(OrderStatus.PAYMENT_NOT_CAPTURED);
        verify(order).setPaymentStatus(PaymentStatus.NOTPAID);
        verify(modelService).save(order);
    }

    @Test
    public void shouldMarkOnHoldOrderAsPaymentAuthorized() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_ON_HOLD);

        service.updateOrderStatus(ORDER_CODE, paymentInfo, order);

        verify(order).setStatus(OrderStatus.PAYMENT_AUTHORIZED);
        verify(order).setPaymentStatus(PaymentStatus.NOTPAID);
        verify(modelService).save(order);
    }

    @Test
    public void shouldCompleteConfirmedOrder() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_CONFIRMED);

        service.updateOrderStatus(ORDER_CODE, paymentInfo, order);

        verify(order).setStatus(OrderStatus.COMPLETED);
        verify(order).setPaymentStatus(PaymentStatus.PAID);
        verify(modelService).save(order);
    }

    @Test
    public void shouldCancelFailedOrder() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_FAILURE);

        service.updateOrderStatus(ORDER_CODE, paymentInfo, order);

        verify(order).setStatus(OrderStatus.CANCELLED);
        verify(order).setPaymentStatus(PaymentStatus.NOTPAID);
        verify(modelService).save(order);
    }

    @Test
    public void shouldCancelDeactivatedOrder() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_DEACTIVATED);

        service.updateOrderStatus(ORDER_CODE, paymentInfo, order);

        verify(order).setStatus(OrderStatus.CANCELLED);
        verify(order).setPaymentStatus(PaymentStatus.NOTPAID);
    }

    @Test
    public void shouldIgnoreCaseOfGatewayStatus() {
        when(paymentInfo.getPaymentGatewayStatus()).thenReturn(GATEWAY_STATUS_CONFIRMED_LOWERCASE);

        service.updateOrderStatus(ORDER_CODE, paymentInfo, order);

        verify(order).setStatus(OrderStatus.COMPLETED);
    }

    @Test
    public void shouldMarkOrderAsPartPaid() {
        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);

        service.updatePartPaidStatus(ORDER_CODE);

        verify(order).setPaymentStatus(PaymentStatus.PARTPAID);
        verify(modelService).save(order);
    }

    @Test
    public void shouldRejectBlankOrderCodeWhenMarkingPartPaid() {
        assertThatThrownBy(() -> service.updatePartPaidStatus(WHITESPACE_STRING)).isInstanceOf(RequestParameterException.class);
        verifyNoInteractions(novalnetCallbackDao, modelService);
    }

    @Test
    public void shouldUpdateGatewayStatusOnLatestPaymentInfo() {
        when(novalnetCallbackDao.getLatestNovalnetPaymentInfo(ORDER_CODE)).thenReturn(paymentInfo);

        service.updatePaymentInfo(ORDER_CODE, GATEWAY_STATUS_CONFIRMED);

        verify(paymentInfo).setPaymentGatewayStatus(GATEWAY_STATUS_CONFIRMED);
        verify(modelService).save(paymentInfo);
    }

    @Test
    public void shouldRejectBlankInputsWhenUpdatingPaymentInfo() {
        assertThatThrownBy(() -> service.updatePaymentInfo(EMPTY_STRING, GATEWAY_STATUS_CONFIRMED))
                .isInstanceOf(RequestParameterException.class);
        assertThatThrownBy(() -> service.updatePaymentInfo(ORDER_CODE, WHITESPACE_STRING))
                .isInstanceOf(RequestParameterException.class);
        verifyNoInteractions(modelService);
    }

    @Test
    public void shouldUpdateCallbackInfoWithTidAndPaidAmount() {
        final Long callBackTid = 987654321L;
        final int amount = 5000;
        when(novalnetCallbackDao.findCallbackInfoByOriginalTid(TID)).thenReturn(callbackInfo);

        service.updateCallbackInfo(callBackTid, TID, amount);

        verify(callbackInfo).setCallbackTid(callBackTid);
        verify(callbackInfo).setPaidAmount(amount);
        verify(modelService).save(callbackInfo);
    }

    @Test
    public void shouldRejectBlankOriginalTid() {
        final int paidAmount = 100;
        assertThatThrownBy(() -> service.updateCallbackInfo(1L, EMPTY_STRING, paidAmount)).isInstanceOf(RequestParameterException.class);
        verifyNoInteractions(novalnetCallbackDao, modelService);
    }

    @Test
    public void shouldAppendCallbackCommentsToExistingNotes() {
        when(novalnetCallbackDao.getLatestNovalnetPaymentInfo(ORDER_CODE)).thenReturn(paymentInfo);
        when(paymentInfo.getOrderHistoryNotes()).thenReturn(EXISTING_NOTES);
        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);
        when(modelService.create(OrderHistoryEntryModel.class)).thenReturn(historyEntry);

        service.updateCallbackComments(NEW_CALLBACK_COMMENT, ORDER_CODE, GATEWAY_STATUS_CONFIRMED, HISTORY_ENTRY_TEXT);

        verify(paymentInfo).setOrderHistoryNotes(EXPECTED_EXISTING_NOTES_WITH_CALLBACK);
        verify(paymentInfo).setPaymentGatewayStatus(GATEWAY_STATUS_CONFIRMED);
        verify(historyEntry).setOrder(order);
        verify(historyEntry).setDescription(HISTORY_ENTRY_TEXT);
        verify(historyEntry).setTimestamp(any(Timestamp.class));
        verify(modelService).saveAll(paymentInfo, historyEntry);
    }

    @Test
    public void shouldStartNotesWhenNoneExist() {
        when(novalnetCallbackDao.getLatestNovalnetPaymentInfo(ORDER_CODE)).thenReturn(paymentInfo);
        when(paymentInfo.getOrderHistoryNotes()).thenReturn(null);
        when(novalnetCallbackDao.findOrderByCode(ORDER_CODE)).thenReturn(order);
        when(modelService.create(OrderHistoryEntryModel.class)).thenReturn(historyEntry);

        service.updateCallbackComments(NEW_CALLBACK_COMMENT, ORDER_CODE, GATEWAY_STATUS_CONFIRMED, HISTORY_ENTRY_TEXT);

        verify(paymentInfo).setOrderHistoryNotes(EXPECTED_EMPTY_NOTES_WITH_CALLBACK);
    }

    @Test
    public void shouldRejectBlankOrderCodeOrStatusWhenUpdatingCallbackComments() {
        assertThatThrownBy(() -> service.updateCallbackComments(SHORT_COMMENT, EMPTY_STRING, GATEWAY_STATUS_CONFIRMED, SHORT_ENTRY))
                .isInstanceOf(RequestParameterException.class);
        assertThatThrownBy(() -> service.updateCallbackComments(SHORT_COMMENT, ORDER_CODE, WHITESPACE_STRING, SHORT_ENTRY))
                .isInstanceOf(RequestParameterException.class);
        verifyNoInteractions(novalnetCallbackDao, modelService);
    }

    @Test
    public void shouldReloadModelsAndAppendCommentsWithSingleLineBreaks() {
        final PK paymentInfoPk = PK.fromLong(11L);
        final PK orderPk = PK.fromLong(22L);
        final NovalnetPaymentInfoModel listedPaymentInfo = org.mockito.Mockito.mock(NovalnetPaymentInfoModel.class);
        final OrderModel listedOrder = org.mockito.Mockito.mock(OrderModel.class);

        when(novalnetDao.getNovalnetPaymentInfo(ORDER_CODE)).thenReturn(List.of(listedPaymentInfo));
        when(listedPaymentInfo.getPk()).thenReturn(paymentInfoPk);
        when(modelService.<NovalnetPaymentInfoModel>get(paymentInfoPk)).thenReturn(paymentInfo);
        when(paymentInfo.getOrderHistoryNotes()).thenReturn(PREVIOUS_NOTES);
        when(novalnetDao.getOrderInfoModel(ORDER_CODE)).thenReturn(List.of(listedOrder));
        when(listedOrder.getPk()).thenReturn(orderPk);
        when(modelService.<OrderModel>get(orderPk)).thenReturn(order);
        when(modelService.create(OrderHistoryEntryModel.class)).thenReturn(historyEntry);

        service.updateCallbackComments(NEW_COMMENT, ORDER_CODE, GATEWAY_STATUS_ON_HOLD);

        verify(paymentInfo).setOrderHistoryNotes(EXPECTED_PREVIOUS_NOTES_WITH_COMMENT);
        verify(paymentInfo).setPaymentGatewayStatus(GATEWAY_STATUS_ON_HOLD);
        verify(historyEntry).setOrder(order);
        verify(historyEntry).setDescription(NEW_COMMENT);
        verify(historyEntry).setTimestamp(any(Timestamp.class));
        verify(modelService).saveAll(paymentInfo, historyEntry);
    }

    @Test
    public void shouldSetSessionLanguageFromOrder() {
        when(order.getLanguage()).thenReturn(language);

        service.setSessionLanguage(order);

        verify(commonI18NService).setCurrentLanguage(language);
    }

    @Test
    public void shouldResolveLocaleFromOrderLanguage() {
        when(order.getLanguage()).thenReturn(language);
        when(commonI18NService.getLocaleForLanguage(language)).thenReturn(Locale.GERMAN);

        assertThat(service.getLocale(order)).isEqualTo(Locale.GERMAN);
    }

    @Test
    public void shouldResolveCurrencyByIsoCode() {
        final CurrencyModel euro = org.mockito.Mockito.mock(CurrencyModel.class);
        when(commonI18NService.getCurrency(CURRENCY_ISO_EUR)).thenReturn(euro);

        assertThat(service.getCurrency(CURRENCY_ISO_EUR)).isSameAs(euro);
    }

    @Test
    public void shouldReturnLocalizedLabel() {
        assertThat(service.getLabel(PAYMENT_NAME_LABEL)).isEqualTo(PAYMENT_NAME_LABEL);
    }

    @Test
    public void shouldBuildBasicPaymentHeader() {
        final String notes = service.buildOrderHistoryNotes(json(JSON_TID_PREFIX + TID + JSON_AMOUNT_10000_SUFFIX), AMOUNT_100_EUR,
                PAYMENT_METHOD_INVOICE);

        assertThat(notes).isEqualTo(EXPECTED_PAYMENT_HEADER_PREFIX + TID + HTML_LINE_BREAK);
    }

    @Test
    public void shouldAddZeroAmountNote() {
        final String notes = service.buildOrderHistoryNotes(json(JSON_TID_PREFIX + TID + JSON_AMOUNT_ZERO_SUFFIX), ZERO_AMOUNT_EUR,
                PAYMENT_METHOD_CREDIT_CARD);

        assertThat(notes).contains(ZERO_AMOUNT_NOTE);
    }

    @Test
    public void shouldNotAddZeroAmountNoteForPositiveAmount() {
        final String notes = service.buildOrderHistoryNotes(json(JSON_TID_PREFIX + TID + JSON_AMOUNT_500_SUFFIX), AMOUNT_5_EUR,
                PAYMENT_METHOD_CREDIT_CARD);

        assertThat(notes).doesNotContain(ZERO_AMOUNT_NOTE_KEY);
    }

    @Test
    public void shouldAddTestModeNote() {
        final String notes = service.buildOrderHistoryNotes(json(JSON_TID_PREFIX + TID + JSON_TEST_MODE_ENABLED), AMOUNT_1_EUR,
                PAYMENT_METHOD_CREDIT_CARD);

        assertThat(notes).endsWith(TEST_ORDER_NOTE);
    }

    @Test
    public void shouldNotAddTestModeNoteWhenTestModeIsOff() {
        final String notes = service.buildOrderHistoryNotes(json(JSON_TID_PREFIX + TID + JSON_TEST_MODE_DISABLED), AMOUNT_1_EUR,
                PAYMENT_METHOD_CREDIT_CARD);

        assertThat(notes).doesNotContain(TEST_ORDER_NOTE_KEY);
    }

    @Test
    public void shouldAddBankDetailsWithDueDate() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_PENDING_WITH_DUE_DATE_PREFIX + JSON_BANK_DETAILS_PREFIX
                + JSON_BANK_DETAILS_SUFFIX;

        final String notes = service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_INVOICE);

        assertThat(notes).contains(EXPECTED_BANK_TRANSFER_DUE_DATE).contains(EXPECTED_ACCOUNT_HOLDER).contains(EXPECTED_IBAN)
                .contains(EXPECTED_BIC).contains(EXPECTED_BANK_NAME).contains(EXPECTED_BANK_PLACE)
                .contains(BANK_PAYMENT_REFERENCE_TID_PREFIX + TID + HTML_LINE_BREAK);
    }

    @Test
    public void shouldLeaveOutDueDateForOnHoldTransaction() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_ON_HOLD_WITH_DUE_DATE_PREFIX + JSON_BANK_DETAILS_IBAN_SUFFIX;

        final String notes = service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_INVOICE);

        assertThat(notes).contains(BANK_TRANSFER_INSTRUCTION).doesNotContain(DUE_DATE_TEXT);
    }

    @Test
    public void shouldSkipBlankBankDetailFields() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_PENDING_STATUS_PREFIX + JSON_BANK_DETAILS_WITH_BLANK_BIC_SUFFIX;

        final String notes = service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_INVOICE);

        assertThat(notes).contains(BANK_DETAILS_IBAN_KEY).doesNotContain(BANK_DETAILS_BIC_KEY)
                .doesNotContain(BANK_DETAILS_ACCOUNT_HOLDER_KEY).doesNotContain(BANK_PLACE_KEY);
    }

    @Test
    public void shouldAddQrCodeImageWhenProvided() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_PENDING_STATUS_PREFIX + JSON_BANK_DETAILS_WITH_QR_SUFFIX;

        final String notes = service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_INVOICE);

        assertThat(notes).contains(EXPECTED_QR_CODE_NOTE);
    }

    @Test
    public void shouldNotAddQrCodeWhenImageIsMissing() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_PENDING_STATUS_PREFIX + JSON_BANK_DETAILS_SHORT_IBAN_SUFFIX;

        assertThat(service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_INVOICE))
                .doesNotContain(QR_CODE_ALT_TEXT);
    }

    @Test
    public void shouldAddGuaranteePendingNoteInsteadOfBankDetailsForStatusCode75() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_STATUS_CODE_75_PREFIX + JSON_BANK_DETAILS_IBAN_SUFFIX;

        final String notes = service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_GUARANTEED_INVOICE);

        assertThat(notes).endsWith(GUARANTEE_PENDING_NOTE).doesNotContain(BANK_DETAILS_IBAN_KEY);
    }

    @Test
    public void shouldAddNothingWhenThereAreNoBankDetails() {
        final String notes = service.buildOrderHistoryNotes(json(JSON_TID_PREFIX + TID + JSON_STATUS_CODE_100_SUFFIX),
                AMOUNT_100_EUR, PAYMENT_METHOD_CREDIT_CARD);

        assertThat(notes).doesNotContain(BANK_DETAILS_KEY).doesNotContain(GUARANTEE_PENDING_NOTE_KEY);
    }

    @Test
    public void shouldAddPartnerPaymentReference() {
        final String transaction = JSON_TID_PREFIX + TID + JSON_PARTNER_PAYMENT_REFERENCE_SUFFIX;

        final String notes = service.buildOrderHistoryNotes(json(transaction), AMOUNT_100_EUR, PAYMENT_METHOD_MULTIBANCO);

        assertThat(notes).endsWith(EXPECTED_MULTIBANCO_NOTE + EXPECTED_PARTNER_PAYMENT_REFERENCE_NOTE);
    }
}
