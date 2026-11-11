/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.util;

import com.novalnet.dto.payment.NovalnetPaymentRequest;
import de.hybris.bootstrap.annotations.UnitTest;
import de.hybris.platform.commercefacades.user.data.TitleData;
import de.hybris.platform.core.model.c2l.LanguageModel;
import de.hybris.platform.core.model.order.OrderModel;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.jalo.SessionContext;
import de.hybris.platform.servicelayer.exceptions.SystemException;
import de.hybris.platform.store.BaseStoreModel;
import de.novalnet.beans.NnCallbackEventData;
import de.novalnet.beans.NnCallbackRequestData;
import de.novalnet.beans.NnCallbackResultData;
import de.novalnet.beans.NnCallbackTransactionData;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


@UnitTest
@ExtendWith(MockitoExtension.class)
public class NovalnetUtilsTest {
    private static final String TID = "14500000012345";
    private static final String EVENT_TYPE = "TRANSACTION_CAPTURE";
    private static final String STATUS = "SUCCESS";
    private static final String AMOUNT = "10000";
    private static final String CURRENCY = "EUR";
    private static final String ACCESS_KEY = "  AbC123 ";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final String CHECKSUM_FULL = "2435015d89d431e56fcade2cd0610000f99750801f752bc3aac09c1065103b34";
    private static final String CHECKSUM_NO_AMOUNT_CURRENCY = "6593d6e9c5ad54b40f0c8a02406acb690e17259e4acf0c2afb102d68f1c6dec4";

    private static final String SHA256_TEST = "9f86d081884c7d659a2feaa0c55ad015a3bf4f1b2b0b822cd15d6c15b0f00a08";
    private static final String SHA256_EMPTY = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";

    @Mock
    private OrderModel order;
    @Mock
    private LanguageModel language;
    @Mock
    private HttpServletRequest request;
    @Mock
    private BaseStoreModel baseStore;

    private static NovalnetUtils newInstance()
            throws NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException {
        final Constructor<NovalnetUtils> constructor =
                NovalnetUtils.class.getDeclaredConstructor();

        if (!constructor.trySetAccessible()) {
            throw new IllegalAccessException("Unable to access NovalnetUtils constructor");
        }

        return constructor.newInstance();
    }

    private static TitleData title(final String code) {
        final TitleData title = new TitleData();
        title.setCode(code);
        return title;
    }

    private static MockedStatic<JaloSession> mockSessionLocale(final Locale locale) {
        final JaloSession session = mock(JaloSession.class);
        final SessionContext context = mock(SessionContext.class);
        when(session.getSessionContext()).thenReturn(context);
        when(context.getLocale()).thenReturn(locale);

        final MockedStatic<JaloSession> jalo = mockStatic(JaloSession.class);
        jalo.when(JaloSession::getCurrentSession).thenReturn(session);
        return jalo;
    }

    private static NnCallbackRequestData callbackRequest(final String amount, final String currency) {
        final NnCallbackEventData event = new NnCallbackEventData();
        event.setTid(TID);
        event.setType(EVENT_TYPE);

        final NnCallbackResultData result = new NnCallbackResultData();
        result.setStatus(STATUS);

        final NnCallbackTransactionData transaction = new NnCallbackTransactionData();
        transaction.setAmount(amount);
        transaction.setCurrency(currency);

        final NnCallbackRequestData callback = new NnCallbackRequestData();
        callback.setEvent(event);
        callback.setResult(result);
        callback.setTransaction(transaction);
        return callback;
    }

    @Test
    public void shouldReturnEnWhenOrderIsNull() {
        assertThat(NovalnetUtils.getLanguageCode(null)).isEqualTo("EN");
    }

    @Test
    public void shouldReturnEnWhenOrderHasNoLanguage() {
        when(order.getLanguage()).thenReturn(null);

        assertThat(NovalnetUtils.getLanguageCode(order)).isEqualTo("EN");
    }

    @Test
    public void shouldReturnUppercaseOrderLanguage() {
        when(order.getLanguage()).thenReturn(language);
        when(language.getIsocode()).thenReturn("de");

        assertThat(NovalnetUtils.getLanguageCode(order)).isEqualTo("DE");
    }

    @Test
    public void shouldDetectPopulatedStrings() {
        assertThat(NovalnetUtils.isPopulated("abc")).isTrue();
        assertThat(NovalnetUtils.isPopulated(" a ")).isTrue();
        assertThat(NovalnetUtils.isPopulated(null)).isFalse();
        assertThat(NovalnetUtils.isPopulated("")).isFalse();
        assertThat(NovalnetUtils.isPopulated("   ")).isFalse();
    }

    @Test
    public void shouldLeaveAmountWithoutCommaUnchanged() {
        assertThat(NovalnetUtils.formatAmount("100")).isEqualTo("100");
        assertThat(NovalnetUtils.formatAmount("10.5")).isEqualTo("10.5");
    }

    @Test
    public void shouldConvertGermanDecimalCommaToDot() {
        assertThat(NovalnetUtils.formatAmount("10,50")).isEqualTo("10.5");
    }

    @Test
    public void shouldHandleGermanThousandsSeparator() {
        assertThat(NovalnetUtils.formatAmount("1.234,56")).isEqualTo("1234.56");
    }

    @Test
    public void shouldReplaceCommaWhenAmountCannotBeParsed() {
        assertThat(NovalnetUtils.formatAmount("abc,def")).isEqualTo("abc.def");
    }

    @Test
    public void shouldEncodeValueAsBase64() throws Exception {
        assertThat(newInstance().getEncodedValue("test")).isEqualTo("dGVzdA==");
    }

    @Test
    public void shouldReturnIpv4OrLocalhostForServerAddress() throws Exception {
        final String ip = newInstance().getServerIpAddr();

        assertThat(ip).isNotBlank().matches("\\d{1,3}(\\.\\d{1,3}){3}");
    }

    @Test
    public void shouldAcceptAdultBirthDate() {
        final int age = 30;
        assertThat(NovalnetUtils.hasAgeRequirement(LocalDate.now().minusYears(age).toString())).isTrue();
    }

    @Test
    public void shouldAcceptExactlyEighteenYears() {
        final int age = 18;
        assertThat(NovalnetUtils.hasAgeRequirement(LocalDate.now().minusYears(age).toString())).isTrue();
    }

    @Test
    public void shouldRejectOneDayBeforeEighteenthBirthday() {
        final int age = 18;
        assertThat(NovalnetUtils.hasAgeRequirement(LocalDate.now().minusYears(age).plusDays(1).toString())).isFalse();
    }

    @Test
    public void shouldRejectMinor() {
        final int age = 10;
        assertThat(NovalnetUtils.hasAgeRequirement(LocalDate.now().minusYears(age).toString())).isFalse();
    }

    @Test
    public void shouldRejectUnparseableBirthDate() {
        assertThat(NovalnetUtils.hasAgeRequirement("31-12-1990")).isFalse();
        assertThat(NovalnetUtils.hasAgeRequirement("not a date")).isFalse();
    }

    @Test
    public void shouldDetectZeroAmountBooking() {
        assertThat(NovalnetUtils.isZeroAmountBooking("AUTHORIZE_WITH_ZERO_AMOUNT")).isTrue();
        assertThat(NovalnetUtils.isZeroAmountBooking(new Object() {
            @Override
            public String toString() {
                return "AUTHORIZE_WITH_ZERO_AMOUNT";
            }
        })).isTrue();
    }

    @Test
    public void shouldNotTreatOtherActionsAsZeroAmountBooking() {
        assertThat(NovalnetUtils.isZeroAmountBooking(null)).isFalse();
        assertThat(NovalnetUtils.isZeroAmountBooking("AUTHORIZE")).isFalse();
        assertThat(NovalnetUtils.isZeroAmountBooking("")).isFalse();
    }

    @Test
    public void shouldMapEveryNovalnetPaymentModeToGatewayType() {
        final Map<String, String> expected = Map.ofEntries(Map.entry("novalnetCreditCard", "CREDITCARD"), //
                Map.entry("novalnetDirectDebitSepa", "DIRECT_DEBIT_SEPA"), //
                Map.entry("novalnetDirectDebitAch", "DIRECT_DEBIT_ACH"), //
                Map.entry("novalnetGuaranteedDirectDebitSepa", "GUARANTEED_DIRECT_DEBIT_SEPA"), //
                Map.entry("novalnetInvoice", "INVOICE"), //
                Map.entry("novalnetGuaranteedInvoice", "GUARANTEED_INVOICE"), //
                Map.entry("novalnetPrepayment", "PREPAYMENT"), //
                Map.entry("novalnetPayPal", "PAYPAL"), //
                Map.entry("novalnetOnlineBankTransfer", "ONLINE_BANK_TRANSFER"), //
                Map.entry("novalnetBancontact", "BANCONTACT"), //
                Map.entry("novalnetMultibanco", "MULTIBANCO"), //
                Map.entry("novalnetIdeal", "IDEAL"), //
                Map.entry("novalnetGooglePay", "GOOGLEPAY"), //
                Map.entry("novalnetApplePay", "APPLEPAY"), //
                Map.entry("novalnetTwint", "TWINT"), //
                Map.entry("novalnetMbWay", "MBWAY"), //
                Map.entry("novalnetAlipay", "ALIPAY"), //
                Map.entry("novalnetTrustly", "TRUSTLY"), //
                Map.entry("novalnetBlik", "BLIK"), //
                Map.entry("novalnetWeChatPay", "WECHATPAY"), //
                Map.entry("novalnetEps", "EPS"), //
                Map.entry("novalnetPrzelewy24", "PRZELEWY24"), //
                Map.entry("novalnetPostFinanceCard", "POSTFINANCE_CARD"), //
                Map.entry("novalnetPostFinance", "POSTFINANCE"));

        expected.forEach((mode, type) -> assertThat(NovalnetUtils.getPaymentType(mode)).as(mode).isEqualTo(type));
    }

    @Test
    public void shouldReturnNullForUnknownOrNullPaymentName() {
        assertThat(NovalnetUtils.getPaymentType("unknownMode")).isNull();
        assertThat(NovalnetUtils.getPaymentType(null)).isNull();
    }

    @Test
    public void shouldFormatTodayWhenZeroDaysAdded() {
        assertThat(NovalnetUtils.formatDate(0)).isEqualTo(LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    @Test
    public void shouldFormatDateWithDaysAdded() {
        final int date = 7;
        assertThat(NovalnetUtils.formatDate(date))
                .isEqualTo(LocalDate.now().plusDays(date).format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    }

    @Test
    public void shouldReturnIpv4RemoteAddressAsIs() {
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        assertThat(NovalnetUtils.getRemoteIpAddr(request)).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void shouldReturnLocalhostForIpv6RemoteAddress() {
        when(request.getRemoteAddr()).thenReturn("::1");

        assertThat(NovalnetUtils.getRemoteIpAddr(request)).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void shouldUseFirstEntryOfForwardedForHeader() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(IP_ADDRESS);

        assertThat(NovalnetUtils.extractClientIp(request)).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void shouldFallBackToRemoteAddressWhenForwardedForIsMissing() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        assertThat(NovalnetUtils.extractClientIp(request)).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void shouldFallBackToRemoteAddressWhenForwardedForIsEmpty() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("");
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        assertThat(NovalnetUtils.extractClientIp(request)).isEqualTo(IP_ADDRESS);
    }

    @Test
    public void shouldGenerateSha256Hex() {
        assertThat(NovalnetUtils.generateChecksum("test")).isEqualTo(SHA256_TEST);
        assertThat(NovalnetUtils.generateChecksum("")).isEqualTo(SHA256_EMPTY);
    }

    @Test
    public void shouldReturnEmptyChecksumWhenInputIsNull() {
        assertThat(NovalnetUtils.generateChecksum(null)).isEmpty();
    }

    @Test
    public void shouldFindTitleByCode() {
        final TitleData mr = title("mr");
        final TitleData mrs = title("mrs");

        assertThat(NovalnetUtils.findTitleForCode(List.of(mr, mrs), "mrs")).isSameAs(mrs);
    }

    @Test
    public void shouldReturnNullWhenTitleCodeNotInList() {
        assertThat(NovalnetUtils.findTitleForCode(List.of(title("mr")), "dr")).isNull();
    }

    @Test
    public void shouldReturnNullForInvalidTitleLookupInput() {
        final List<TitleData> titles = List.of(title("mr"));

        assertThat(NovalnetUtils.findTitleForCode(titles, null)).isNull();
        assertThat(NovalnetUtils.findTitleForCode(titles, "")).isNull();
        assertThat(NovalnetUtils.findTitleForCode(null, "mr")).isNull();
        assertThat(NovalnetUtils.findTitleForCode(List.of(), "mr")).isNull();
    }

    @Test
    public void shouldResolveSessionLanguageInUppercase() {
        try (MockedStatic<JaloSession> jalo = mockSessionLocale(Locale.GERMAN)) {
            assertThat(NovalnetUtils.resolveLanguageCode()).isEqualTo("DE");
        }
    }

    @Test
    public void shouldKeepCountryInResolvedLanguage() {
        try (MockedStatic<JaloSession> jalo = mockSessionLocale(new Locale.Builder().setLanguage("de").setRegion("DE").build())) {
            assertThat(NovalnetUtils.resolveLanguageCode()).isEqualTo("DE_DE");
        }
    }

    @Test
    public void shouldDefaultToEnWhenSessionHasNoLocale() {
        try (MockedStatic<JaloSession> jalo = mockSessionLocale(null)) {
            assertThat(NovalnetUtils.resolveLanguageCode()).isEqualTo("EN");
        }
    }

    @Test
    public void shouldFormatCurrentDateWithGermanPattern() {
        try (MockedStatic<JaloSession> jalo = mockSessionLocale(Locale.GERMAN)) {
            assertThat(NovalnetUtils.getCurrentDate()).matches("\\d{2}-\\d{2}-\\d{4} um \\d{2}:\\d{2}:\\d{2}");
        }
    }

    @Test
    public void shouldFormatCurrentDateWithDefaultPattern() {
        try (MockedStatic<JaloSession> jalo = mockSessionLocale(Locale.ENGLISH)) {
            assertThat(NovalnetUtils.getCurrentDate()).matches("\\d{2}-\\d{2}-\\d{4}, \\d{2}:\\d{2}:\\d{2}");
        }
    }

    @Test
    public void shouldAcceptCallbackFromNovalnetIp() throws Exception {
        final InetAddress novalnet = InetAddress.getByAddress(new byte[]{1, 2, 3, 4});
        final String remoteAddr = "1.2.3.4";
        when(baseStore.getNovalnetVendorscriptTestMode()).thenReturn(Boolean.FALSE);
        when(request.getRemoteAddr()).thenReturn(remoteAddr);

        try (MockedStatic<InetAddress> dns = mockStatic(InetAddress.class)) {
            dns.when(() -> InetAddress.getByName("pay-nn.de")).thenReturn(novalnet);

            assertThatCode(() -> NovalnetUtils.validateCallbackIp(request, baseStore)).doesNotThrowAnyException();
        }
    }

    @Test
    public void shouldRejectCallbackFromForeignIpOutsideTestMode() throws Exception {
        final InetAddress novalnet = InetAddress.getByAddress(new byte[]
                {1, 2, 3, 4});
        when(baseStore.getNovalnetVendorscriptTestMode()).thenReturn(Boolean.FALSE);
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        try (MockedStatic<InetAddress> dns = mockStatic(InetAddress.class)) {
            dns.when(() -> InetAddress.getByName("pay-nn.de")).thenReturn(novalnet);

            assertThatThrownBy(() -> NovalnetUtils.validateCallbackIp(request, baseStore)).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining(IP_ADDRESS);
        }
    }

    @Test
    public void shouldTreatMissingTestModeFlagAsProduction() throws Exception {
        final InetAddress novalnet = InetAddress.getByAddress(new byte[]
                {1, 2, 3, 4});
        when(baseStore.getNovalnetVendorscriptTestMode()).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        try (MockedStatic<InetAddress> dns = mockStatic(InetAddress.class)) {
            dns.when(() -> InetAddress.getByName("pay-nn.de")).thenReturn(novalnet);

            assertThatThrownBy(() -> NovalnetUtils.validateCallbackIp(request, baseStore)).isInstanceOf(IllegalStateException.class);
        }
    }

    @Test
    public void shouldAllowAnyIpInTestMode() throws Exception {
        final InetAddress novalnet = InetAddress.getByAddress(new byte[]
                {1, 2, 3, 4});
        when(baseStore.getNovalnetVendorscriptTestMode()).thenReturn(Boolean.TRUE);
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        try (MockedStatic<InetAddress> dns = mockStatic(InetAddress.class)) {
            dns.when(() -> InetAddress.getByName("pay-nn.de")).thenReturn(novalnet);

            assertThatCode(() -> NovalnetUtils.validateCallbackIp(request, baseStore)).doesNotThrowAnyException();
        }
    }

    @Test
    public void shouldFailWhenNovalnetHostCannotBeResolved() {
        when(baseStore.getNovalnetVendorscriptTestMode()).thenReturn(Boolean.FALSE);
        when(request.getRemoteAddr()).thenReturn(IP_ADDRESS);

        try (MockedStatic<InetAddress> dns = mockStatic(InetAddress.class)) {
            dns.when(() -> InetAddress.getByName("pay-nn.de")).thenThrow(new UnknownHostException("pay-nn.de"));

            assertThatThrownBy(() -> NovalnetUtils.validateCallbackIp(request, baseStore)).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("resolve");
        }
    }

    @Test
    public void shouldAcceptCallbackWithAllMandatoryParts() {
        assertThatCode(() -> NovalnetUtils.validateMandatoryParams(callbackRequest(AMOUNT, CURRENCY))).doesNotThrowAnyException();
    }

    @Test
    public void shouldRejectCallbackWithoutEvent() {
        final NnCallbackRequestData callback = callbackRequest(AMOUNT, CURRENCY);
        callback.setEvent(null);

        assertThatThrownBy(() -> NovalnetUtils.validateMandatoryParams(callback)).isInstanceOf(SystemException.class);
    }

    @Test
    public void shouldRejectCallbackWithoutTransaction() {
        final NnCallbackRequestData callback = callbackRequest(AMOUNT, CURRENCY);
        callback.setTransaction(null);

        assertThatThrownBy(() -> NovalnetUtils.validateMandatoryParams(callback)).isInstanceOf(SystemException.class);
    }

    @Test
    public void shouldRejectCallbackWithoutResult() {
        final NnCallbackRequestData callback = callbackRequest(AMOUNT, CURRENCY);
        callback.setResult(null);

        assertThatThrownBy(() -> NovalnetUtils.validateMandatoryParams(callback)).isInstanceOf(SystemException.class);
    }

    @Test
    public void shouldAcceptValidChecksum() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(ACCESS_KEY);
        final NnCallbackRequestData callback = callbackRequest(AMOUNT, CURRENCY);
        callback.getEvent().setChecksum(CHECKSUM_FULL);

        assertThatCode(() -> NovalnetUtils.validateCallbackChecksum(callback, baseStore)).doesNotThrowAnyException();
    }

    @Test
    public void shouldAcceptValidChecksumWhenAmountAndCurrencyAreMissing() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(ACCESS_KEY);
        final NnCallbackRequestData callback = callbackRequest(null, null);
        callback.getEvent().setChecksum(CHECKSUM_NO_AMOUNT_CURRENCY);

        assertThatCode(() -> NovalnetUtils.validateCallbackChecksum(callback, baseStore)).doesNotThrowAnyException();
    }

    @Test
    public void shouldRejectTamperedChecksum() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(ACCESS_KEY);
        final NnCallbackRequestData callback = callbackRequest(AMOUNT, CURRENCY);
        callback.getEvent().setChecksum("0000");

        assertThatThrownBy(() -> NovalnetUtils.validateCallbackChecksum(callback, baseStore))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Checksum validation failed");
    }

    @Test
    public void shouldRejectWhenPayloadAmountWasChanged() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(ACCESS_KEY);
        final NnCallbackRequestData callback = callbackRequest("99999", CURRENCY);
        callback.getEvent().setChecksum(CHECKSUM_FULL);

        assertThatThrownBy(() -> NovalnetUtils.validateCallbackChecksum(callback, baseStore))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void shouldRejectWhenAccessKeyIsNull() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn(null);

        assertThatThrownBy(() -> NovalnetUtils.validateCallbackChecksum(callbackRequest(AMOUNT, CURRENCY), baseStore))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not configured");
    }

    @Test
    public void shouldRejectWhenAccessKeyIsEmpty() {
        when(baseStore.getNovalnetPaymentAccessKey()).thenReturn("");

        assertThatThrownBy(() -> NovalnetUtils.validateCallbackChecksum(callbackRequest(AMOUNT, CURRENCY), baseStore))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("not configured");
    }

    @Test
    public void shouldRecognizeWalletPayments() {
        assertThat(NovalnetUtils.isWalletPayment("novalnetGooglePay")).isTrue();
        assertThat(NovalnetUtils.isWalletPayment("novalnetApplePay")).isTrue();
        assertThat(NovalnetUtils.isWalletPayment("novalnetCreditCard")).isFalse();
        assertThat(NovalnetUtils.isWalletPayment(null)).isFalse();
    }

    @Test
    public void shouldSerializeEmptyRequestToEmptyJsonObject() {
        assertThat(NovalnetUtils.convetToString(new NovalnetPaymentRequest())).isEqualTo("{}");
    }
}
