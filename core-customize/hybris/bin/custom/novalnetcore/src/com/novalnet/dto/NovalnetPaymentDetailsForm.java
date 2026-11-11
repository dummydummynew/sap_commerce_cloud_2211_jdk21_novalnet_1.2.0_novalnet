/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dto;


import java.util.Map;


@SuppressWarnings({"java:S116", "java:S2384", "java:S1820", "java:S1448", "java:S100"})
public class NovalnetPaymentDetailsForm {
    private String paymentId;
    private String amount;
    private String billToCountry;
    private String comments;
    private String currency;
    private Map<String, String> parameters;
    private boolean saveInAccount;

    private String selectedPaymentMethodId;
    private String creditCardOneClickData1;
    private boolean creditcardSaveData;
    private boolean paypalSaveData;
    private boolean directDebitSepaSaveData;
    private boolean directDebitAchSaveData;
    private boolean guaranteedDirectDebitSepaSaveData;
    private String creditCardOneClickData2;
    private String guaranteedDirectDebitSepaOneClickData1;
    private String directDebitSepaOneClickData2;
    private String payPalOneClickData1;
    private String paypalOneClickData2;
    private String directDebitSepaOneClickData1;
    private String directDebitAchOneClickData1;
    private String companyName;

    private String achAccountHolder;
    private String achAccountNumber;
    private String achRoutingNumber;
    private String accountIban;
    private String accountBic;
    private String accountHolder;
    private String guaranteeAccountHolder;
    private String guaranteeAccountIban;
    private String guaranteeAccountBic;
    private String novalnetCreditCardPanHash;
    private String novalnetCreditCardUniqueId;
    private String do_redirect;
    private String previousSelectedPayment;
    private boolean novalnetCreditCardOneClickProcess;
    private boolean novalnetDirectDebitSepaOneClickProcess;
    private boolean novalnetPayPalOneClickProcess;

    private String billTo_city;
    private String billTo_country;
    private String billTo_customerID;
    private String billTo_email;
    private String billTo_firstName;
    private String billTo_lastName;
    private String billTo_phoneNumber;
    private String billTo_postalCode;
    private String billTo_titleCode;
    private String billTo_state;
    private String billTo_street1;
    private String billTo_street2;

    private String novalnetCreditCardOneClickCardType;
    private String novalnetCreditCardOneClickCardHolder;
    private String novalnetCreditCardOneClickMaskedCardNumber;
    private String novalnetCreditCardOneClickToken1;
    private String novalnetCreditCardOneClickCardExpiry;
    private String creditCardOneClickNewDeatails;

    private String novalnetDirectDebitSepaOneClickAccountHolder;
    private String novalnetDirectDebitSepaOneClickMaskedAccountIban;

    private String novalnetGuaranteedDirectDebitSepaDateOfBirth;
    private boolean novalnetDirectDebitSepaGuaranteeProcess;
    private String novalnetGuaranteedInvoiceDateOfBirth;
    private boolean novalnetInvoiceGuaranteeProcess;

    private String novalnetPaypalOneClickPpTransactionId;
    private String novalnetPaypalOneClickRefTransactionId;

    private AddressForm billingAddress;
    private boolean newBillingAddress;
    private boolean useDeliveryAddress;
    private boolean savePaymentInfo;


    /**
     * B2b
     */
    private String novalnetAchAccountHolder;
    private String novalnetAchAccountNumber;
    private String novalnetAchRoutingNumber;
    private String novalnetAchOneClickAccountHolder;
    private String novalnetAchOneClickAccountNumber;
    private String novalnetDirectDebitAchOneClickData1;
    private boolean novalnetDirectDebitAchSaveData;
    private String novalnetDirectDebitSepaOneClickAccountIban;


    public String getNovalnetAchAccountHolder() {
        return novalnetAchAccountHolder;
    }

    public void setNovalnetAchAccountHolder(String novalnetAchAccountHolder) {
        this.novalnetAchAccountHolder = novalnetAchAccountHolder;
    }

    public String getNovalnetAchAccountNumber() {
        return novalnetAchAccountNumber;
    }

    public void setNovalnetAchAccountNumber(String novalnetAchAccountNumber) {
        this.novalnetAchAccountNumber = novalnetAchAccountNumber;
    }

    public String getNovalnetAchRoutingNumber() {
        return novalnetAchRoutingNumber;
    }

    public void setNovalnetAchRoutingNumber(String novalnetAchRoutingNumber) {
        this.novalnetAchRoutingNumber = novalnetAchRoutingNumber;
    }

    public String getNovalnetAchOneClickAccountHolder() {
        return novalnetAchOneClickAccountHolder;
    }

    public void setNovalnetAchOneClickAccountHolder(String novalnetAchOneClickAccountHolder) {
        this.novalnetAchOneClickAccountHolder = novalnetAchOneClickAccountHolder;
    }

    public String getNovalnetAchOneClickAccountNumber() {
        return novalnetAchOneClickAccountNumber;
    }

    public void setNovalnetAchOneClickAccountNumber(String novalnetAchOneClickAccountNumber) {
        this.novalnetAchOneClickAccountNumber = novalnetAchOneClickAccountNumber;
    }

    public String getNovalnetDirectDebitAchOneClickData1() {
        return novalnetDirectDebitAchOneClickData1;
    }

    public void setNovalnetDirectDebitAchOneClickData1(String novalnetDirectDebitAchOneClickData1) {
        this.novalnetDirectDebitAchOneClickData1 = novalnetDirectDebitAchOneClickData1;
    }

    public boolean isNovalnetDirectDebitAchSaveData() {
        return novalnetDirectDebitAchSaveData;
    }

    public void setNovalnetDirectDebitAchSaveData(boolean novalnetDirectDebitAchSaveData) {
        this.novalnetDirectDebitAchSaveData = novalnetDirectDebitAchSaveData;
    }

    public String getNovalnetDirectDebitSepaOneClickAccountIban() {
        return novalnetDirectDebitSepaOneClickAccountIban;
    }

    public void setNovalnetDirectDebitSepaOneClickAccountIban(String novalnetDirectDebitSepaOneClickAccountIban) {
        this.novalnetDirectDebitSepaOneClickAccountIban = novalnetDirectDebitSepaOneClickAccountIban;
    }

    /**
     * @return the directDebitSepaOneClickData1
     */
    public String getDirectDebitSepaOneClickData1() {
        return directDebitSepaOneClickData1;
    }

    /**
     * @param directDebitSepaOneClickData1 the directDebitSepaOneClickData1 to set
     */
    public void setDirectDebitSepaOneClickData1(final String directDebitSepaOneClickData1) {
        this.directDebitSepaOneClickData1 = directDebitSepaOneClickData1;
    }

    /**
     * @return the directDebitAchOneClickData1
     */
    public String getDirectDebitAchOneClickData1() {
        return directDebitAchOneClickData1;
    }

    /**
     * @param directDebitAchOneClickData1 the directDebitAchOneClickData1 to set
     */
    public void setDirectDebitAchOneClickData1(final String directDebitAchOneClickData1) {
        this.directDebitAchOneClickData1 = directDebitAchOneClickData1;
    }

    /**
     * @return the achAccountHolder
     */
    public String getAchAccountHolder() {
        return achAccountHolder;
    }

    /**
     * @param achAccountHolder the achAccountHolder to set
     */
    public void setAchAccountHolder(final String achAccountHolder) {
        this.achAccountHolder = achAccountHolder;
    }

    /**
     * @return the achAccountNumber
     */
    public String getAchAccountNumber() {
        return achAccountNumber;
    }

    /**
     * @param achAccountNumber the achAccountNumber to set
     */
    public void setAchAccountNumber(final String achAccountNumber) {
        this.achAccountNumber = achAccountNumber;
    }

    /**
     * @return the achRoutingNumber
     */
    public String getAchRoutingNumber() {
        return achRoutingNumber;
    }

    /**
     * @param achRoutingNumber the achRoutingNumber to set
     */
    public void setAchRoutingNumber(final String achRoutingNumber) {
        this.achRoutingNumber = achRoutingNumber;
    }

    /**
     * @return the guaranteeAccountHolder
     */
    public String getGuaranteeAccountHolder() {
        return guaranteeAccountHolder;
    }

    /**
     * @param guaranteeAccountHolder the guaranteeAccountHolder to set
     */
    public void setGuaranteeAccountHolder(final String guaranteeAccountHolder) {
        this.guaranteeAccountHolder = guaranteeAccountHolder;
    }

    public String getAccountBic() {
        return accountBic;
    }

    /**
     * @param accountBic the accountBic to set
     */
    public void setAccountBic(final String accountBic) {
        this.accountBic = accountBic;
    }

    /**
     * @return the guaranteeAccountBic
     */
    public String getGuaranteeAccountBic() {
        return guaranteeAccountBic;
    }

    /**
     * @param guaranteeAccountBic the guaranteeAccountBic to set
     */
    public void setGuaranteeAccountBic(final String guaranteeAccountBic) {
        this.guaranteeAccountBic = guaranteeAccountBic;
    }

    /**
     * @return the paymentId
     */
    public String getPaymentId() {
        return paymentId;
    }

    /**
     * @param paymentId the paymentId to set
     */
    public void setPaymentId(final String paymentId) {
        this.paymentId = paymentId;
    }

    /**
     * @return the amount
     */
    public String getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(final String amount) {
        this.amount = amount;
    }

    /**
     * @return the billToCountry
     */
    public String getBillToCountry() {
        return billToCountry;
    }

    /**
     * @param billToCountry the billToCountry to set
     */
    public void setBillToCountry(final String billToCountry) {
        this.billToCountry = billToCountry;
    }

    /**
     * @return the comments
     */
    public String getComments() {
        return comments;
    }

    /**
     * @param comments the comments to set
     */
    public void setComments(final String comments) {
        this.comments = comments;
    }

    /**
     * @return the currency
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * @param currency the currency to set
     */
    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    /**
     * @return the parameters
     */
    public Map<String, String> getParameters() {
        return parameters;
    }

    /**
     * @param parameters the parameters to set
     */
    public void setParameters(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    /**
     * @return the saveInAccount
     */
    public boolean isSaveInAccount() {
        return saveInAccount;
    }

    /**
     * @param saveInAccount the saveInAccount to set
     */
    public void setSaveInAccount(final boolean saveInAccount) {
        this.saveInAccount = saveInAccount;
    }

    /**
     * @return the selectedPaymentMethodId
     */
    public String getSelectedPaymentMethodId() {
        return selectedPaymentMethodId;
    }

    /**
     * @param selectedPaymentMethodId the selectedPaymentMethodId to set
     */
    public void setSelectedPaymentMethodId(final String selectedPaymentMethodId) {
        this.selectedPaymentMethodId = selectedPaymentMethodId;
    }

    /**
     * @return the creditCardOneClickData1
     */
    public String getCreditCardOneClickData1() {
        return creditCardOneClickData1;
    }

    /**
     * @param creditCardOneClickData1 the creditCardOneClickData1 to set
     */
    public void setCreditCardOneClickData1(final String creditCardOneClickData1) {
        this.creditCardOneClickData1 = creditCardOneClickData1;
    }

    /**
     * @return the creditcardSaveData
     */
    public boolean isCreditcardSaveData() {
        return creditcardSaveData;
    }

    /**
     * @param creditcardSaveData the creditcardSaveData to set
     */
    public void setCreditcardSaveData(final boolean creditcardSaveData) {
        this.creditcardSaveData = creditcardSaveData;
    }

    /**
     * @return the paypalSaveData
     */
    public boolean isPaypalSaveData() {
        return paypalSaveData;
    }

    /**
     * @param paypalSaveData the paypalSaveData to set
     */
    public void setPaypalSaveData(final boolean paypalSaveData) {
        this.paypalSaveData = paypalSaveData;
    }

    /**
     * @return the directDebitSepaSaveData
     */
    public boolean isDirectDebitSepaSaveData() {
        return directDebitSepaSaveData;
    }

    /**
     * @param directDebitSepaSaveData the directDebitSepaSaveData to set
     */
    public void setDirectDebitSepaSaveData(final boolean directDebitSepaSaveData) {
        this.directDebitSepaSaveData = directDebitSepaSaveData;
    }

    public boolean isDirectDebitAchSaveData() {
        return directDebitAchSaveData;
    }

    public void setDirectDebitAchSaveData(final boolean directDebitAchSaveData) {
        this.directDebitAchSaveData = directDebitAchSaveData;
    }

    /**
     * @return the guaranteedDirectDebitSepaSaveData
     */
    public boolean isGuaranteedDirectDebitSepaSaveData() {
        return guaranteedDirectDebitSepaSaveData;
    }

    /**
     * @param guaranteedDirectDebitSepaSaveData the guaranteedDirectDebitSepaSaveData to set
     */
    public void setGuaranteedDirectDebitSepaSaveData(final boolean guaranteedDirectDebitSepaSaveData) {
        this.guaranteedDirectDebitSepaSaveData = guaranteedDirectDebitSepaSaveData;
    }

    /**
     * @return the creditCardOneClickData2
     */
    public String getCreditCardOneClickData2() {
        return creditCardOneClickData2;
    }

    /**
     * @param creditCardOneClickData2 the creditCardOneClickData2 to set
     */
    public void setCreditCardOneClickData2(final String creditCardOneClickData2) {
        this.creditCardOneClickData2 = creditCardOneClickData2;
    }

    /**
     * @return the guaranteedDirectDebitSepaOneClickData1
     */
    public String getGuaranteedDirectDebitSepaOneClickData1() {
        return guaranteedDirectDebitSepaOneClickData1;
    }

    /**
     * @param guaranteedDirectDebitSepaOneClickData1 the guaranteedDirectDebitSepaOneClickData1 to set
     */
    public void setGuaranteedDirectDebitSepaOneClickData1(final String guaranteedDirectDebitSepaOneClickData1) {
        this.guaranteedDirectDebitSepaOneClickData1 = guaranteedDirectDebitSepaOneClickData1;
    }

    /**
     * @return the directDebitSepaOneClickData2
     */
    public String getDirectDebitSepaOneClickData2() {
        return directDebitSepaOneClickData2;
    }

    /**
     * @param directDebitSepaOneClickData2 the directDebitSepaOneClickData2 to set
     */
    public void setDirectDebitSepaOneClickData2(final String directDebitSepaOneClickData2) {
        this.directDebitSepaOneClickData2 = directDebitSepaOneClickData2;
    }

    /**
     * @return the payPalOneClickData1
     */
    public String getPayPalOneClickData1() {
        return payPalOneClickData1;
    }

    /**
     * @param payPalOneClickData1 the payPalOneClickData1 to set
     */
    public void setPayPalOneClickData1(final String payPalOneClickData1) {
        this.payPalOneClickData1 = payPalOneClickData1;
    }

    /**
     * @return the paypalOneClickData2
     */
    public String getPaypalOneClickData2() {
        return paypalOneClickData2;
    }

    /**
     * @param paypalOneClickData2 the paypalOneClickData2 to set
     */
    public void setPaypalOneClickData2(final String paypalOneClickData2) {
        this.paypalOneClickData2 = paypalOneClickData2;
    }

    /**
     * @return the accountIban
     */
    public String getAccountIban() {
        return accountIban;
    }

    /**
     * @param accountIban the accountIban to set
     */
    public void setAccountIban(final String accountIban) {
        this.accountIban = accountIban;
    }

    /**
     * @return the accountHolder
     */
    public String getAccountHolder() {
        return accountHolder;
    }

    /**
     * @param accountHolder the accountHolder to set
     */
    public void setAccountHolder(final String accountHolder) {
        this.accountHolder = accountHolder;
    }

    /**
     * @return the guaranteeAccountIban
     */
    public String getGuaranteeAccountIban() {
        return guaranteeAccountIban;
    }

    /**
     * @param guaranteeAccountIban the guaranteeAccountIban to set
     */
    public void setGuaranteeAccountIban(final String guaranteeAccountIban) {
        this.guaranteeAccountIban = guaranteeAccountIban;
    }

    /**
     * @return the novalnetCreditCardPanHash
     */
    public String getNovalnetCreditCardPanHash() {
        return novalnetCreditCardPanHash;
    }

    /**
     * @param novalnetCreditCardPanHash the novalnetCreditCardPanHash to set
     */
    public void setNovalnetCreditCardPanHash(final String novalnetCreditCardPanHash) {
        this.novalnetCreditCardPanHash = novalnetCreditCardPanHash;
    }

    /**
     * @return the novalnetCreditCardUniqueId
     */
    public String getNovalnetCreditCardUniqueId() {
        return novalnetCreditCardUniqueId;
    }

    /**
     * @param novalnetCreditCardUniqueId the novalnetCreditCardUniqueId to set
     */
    public void setNovalnetCreditCardUniqueId(final String novalnetCreditCardUniqueId) {
        this.novalnetCreditCardUniqueId = novalnetCreditCardUniqueId;
    }

    /**
     * @return the do_redirect
     */
    public String getDo_redirect() {
        return do_redirect;
    }

    /**
     * @param do_redirect the do_redirect to set
     */
    public void setDo_redirect(final String do_redirect) {
        this.do_redirect = do_redirect;
    }

    /**
     * @return the previousSelectedPayment
     */
    public String getPreviousSelectedPayment() {
        return previousSelectedPayment;
    }

    /**
     * @param previousSelectedPayment the previousSelectedPayment to set
     */
    public void setPreviousSelectedPayment(final String previousSelectedPayment) {
        this.previousSelectedPayment = previousSelectedPayment;
    }

    /**
     * @return the novalnetCreditCardOneClickProcess
     */
    public boolean isNovalnetCreditCardOneClickProcess() {
        return novalnetCreditCardOneClickProcess;
    }

    /**
     * @param novalnetCreditCardOneClickProcess the novalnetCreditCardOneClickProcess to set
     */
    public void setNovalnetCreditCardOneClickProcess(final boolean novalnetCreditCardOneClickProcess) {
        this.novalnetCreditCardOneClickProcess = novalnetCreditCardOneClickProcess;
    }

    /**
     * @return the novalnetDirectDebitSepaOneClickProcess
     */
    public boolean isNovalnetDirectDebitSepaOneClickProcess() {
        return novalnetDirectDebitSepaOneClickProcess;
    }

    /**
     * @param novalnetDirectDebitSepaOneClickProcess the novalnetDirectDebitSepaOneClickProcess to set
     */
    public void setNovalnetDirectDebitSepaOneClickProcess(final boolean novalnetDirectDebitSepaOneClickProcess) {
        this.novalnetDirectDebitSepaOneClickProcess = novalnetDirectDebitSepaOneClickProcess;
    }

    /**
     * @return the novalnetPayPalOneClickProcess
     */
    public boolean isNovalnetPayPalOneClickProcess() {
        return novalnetPayPalOneClickProcess;
    }

    /**
     * @param novalnetPayPalOneClickProcess the novalnetPayPalOneClickProcess to set
     */
    public void setNovalnetPayPalOneClickProcess(final boolean novalnetPayPalOneClickProcess) {
        this.novalnetPayPalOneClickProcess = novalnetPayPalOneClickProcess;
    }

    /**
     * @return the billTo_city
     */
    public String getBillTo_city() {
        return billTo_city;
    }

    /**
     * @param billTo_city the billTo_city to set
     */
    public void setBillTo_city(final String billTo_city) {
        this.billTo_city = billTo_city;
    }

    /**
     * @return the billTo_country
     */
    public String getBillTo_country() {
        return billTo_country;
    }

    /**
     * @param billTo_country the billTo_country to set
     */
    public void setBillTo_country(final String billTo_country) {
        this.billTo_country = billTo_country;
    }

    /**
     * @return the billTo_customerID
     */
    public String getBillTo_customerID() {
        return billTo_customerID;
    }

    /**
     * @param billTo_customerID the billTo_customerID to set
     */
    public void setBillTo_customerID(final String billTo_customerID) {
        this.billTo_customerID = billTo_customerID;
    }

    /**
     * @return the billTo_email
     */
    public String getBillTo_email() {
        return billTo_email;
    }

    /**
     * @param billTo_email the billTo_email to set
     */
    public void setBillTo_email(final String billTo_email) {
        this.billTo_email = billTo_email;
    }

    /**
     * @return the billTo_firstName
     */
    public String getBillTo_firstName() {
        return billTo_firstName;
    }

    /**
     * @param billTo_firstName the billTo_firstName to set
     */
    public void setBillTo_firstName(final String billTo_firstName) {
        this.billTo_firstName = billTo_firstName;
    }

    /**
     * @return the billTo_lastName
     */
    public String getBillTo_lastName() {
        return billTo_lastName;
    }

    /**
     * @param billTo_lastName the billTo_lastName to set
     */
    public void setBillTo_lastName(final String billTo_lastName) {
        this.billTo_lastName = billTo_lastName;
    }

    /**
     * @return the billTo_phoneNumber
     */
    public String getBillTo_phoneNumber() {
        return billTo_phoneNumber;
    }

    /**
     * @param billTo_phoneNumber the billTo_phoneNumber to set
     */
    public void setBillTo_phoneNumber(final String billTo_phoneNumber) {
        this.billTo_phoneNumber = billTo_phoneNumber;
    }

    /**
     * @return the billTo_postalCode
     */
    public String getBillTo_postalCode() {
        return billTo_postalCode;
    }

    /**
     * @param billTo_postalCode the billTo_postalCode to set
     */
    public void setBillTo_postalCode(final String billTo_postalCode) {
        this.billTo_postalCode = billTo_postalCode;
    }

    /**
     * @return the billTo_titleCode
     */
    public String getBillTo_titleCode() {
        return billTo_titleCode;
    }

    /**
     * @param billTo_titleCode the billTo_titleCode to set
     */
    public void setBillTo_titleCode(final String billTo_titleCode) {
        this.billTo_titleCode = billTo_titleCode;
    }

    /**
     * @return the billTo_state
     */
    public String getBillTo_state() {
        return billTo_state;
    }

    /**
     * @param billTo_state the billTo_state to set
     */
    public void setBillTo_state(final String billTo_state) {
        this.billTo_state = billTo_state;
    }

    /**
     * @return the billTo_street1
     */
    public String getBillTo_street1() {
        return billTo_street1;
    }

    /**
     * @param billTo_street1 the billTo_street1 to set
     */
    public void setBillTo_street1(final String billTo_street1) {
        this.billTo_street1 = billTo_street1;
    }

    /**
     * @return the billTo_street2
     */
    public String getBillTo_street2() {
        return billTo_street2;
    }

    /**
     * @param billTo_street2 the billTo_street2 to set
     */
    public void setBillTo_street2(final String billTo_street2) {
        this.billTo_street2 = billTo_street2;
    }

    /**
     * @return the novalnetCreditCardOneClickCardType
     */
    public String getNovalnetCreditCardOneClickCardType() {
        return novalnetCreditCardOneClickCardType;
    }

    /**
     * @param novalnetCreditCardOneClickCardType the novalnetCreditCardOneClickCardType to set
     */
    public void setNovalnetCreditCardOneClickCardType(final String novalnetCreditCardOneClickCardType) {
        this.novalnetCreditCardOneClickCardType = novalnetCreditCardOneClickCardType;
    }

    /**
     * @return the novalnetCreditCardOneClickCardHolder
     */
    public String getNovalnetCreditCardOneClickCardHolder() {
        return novalnetCreditCardOneClickCardHolder;
    }

    /**
     * @param novalnetCreditCardOneClickCardHolder the novalnetCreditCardOneClickCardHolder to set
     */
    public void setNovalnetCreditCardOneClickCardHolder(final String novalnetCreditCardOneClickCardHolder) {
        this.novalnetCreditCardOneClickCardHolder = novalnetCreditCardOneClickCardHolder;
    }

    /**
     * @return the novalnetCreditCardOneClickMaskedCardNumber
     */
    public String getNovalnetCreditCardOneClickMaskedCardNumber() {
        return novalnetCreditCardOneClickMaskedCardNumber;
    }

    /**
     * @param novalnetCreditCardOneClickMaskedCardNumber the novalnetCreditCardOneClickMaskedCardNumber to set
     */
    public void setNovalnetCreditCardOneClickMaskedCardNumber(final String novalnetCreditCardOneClickMaskedCardNumber) {
        this.novalnetCreditCardOneClickMaskedCardNumber = novalnetCreditCardOneClickMaskedCardNumber;
    }

    /**
     * @return the novalnetCreditCardOneClickToken1
     */
    public String getNovalnetCreditCardOneClickToken1() {
        return novalnetCreditCardOneClickToken1;
    }

    /**
     * @param novalnetCreditCardOneClickToken1 the novalnetCreditCardOneClickToken1 to set
     */
    public void setNovalnetCreditCardOneClickToken1(final String novalnetCreditCardOneClickToken1) {
        this.novalnetCreditCardOneClickToken1 = novalnetCreditCardOneClickToken1;
    }

    /**
     * @return the novalnetCreditCardOneClickCardExpiry
     */
    public String getNovalnetCreditCardOneClickCardExpiry() {
        return novalnetCreditCardOneClickCardExpiry;
    }

    /**
     * @param novalnetCreditCardOneClickCardExpiry the novalnetCreditCardOneClickCardExpiry to set
     */
    public void setNovalnetCreditCardOneClickCardExpiry(final String novalnetCreditCardOneClickCardExpiry) {
        this.novalnetCreditCardOneClickCardExpiry = novalnetCreditCardOneClickCardExpiry;
    }

    /**
     * @return the creditCardOneClickNewDeatails
     */
    public String getCreditCardOneClickNewDeatails() {
        return creditCardOneClickNewDeatails;
    }

    /**
     * @param creditCardOneClickNewDeatails the creditCardOneClickNewDeatails to set
     */
    public void setCreditCardOneClickNewDeatails(final String creditCardOneClickNewDeatails) {
        this.creditCardOneClickNewDeatails = creditCardOneClickNewDeatails;
    }

    /**
     * @return the novalnetDirectDebitSepaOneClickAccountHolder
     */
    public String getNovalnetDirectDebitSepaOneClickAccountHolder() {
        return novalnetDirectDebitSepaOneClickAccountHolder;
    }

    /**
     * @param novalnetDirectDebitSepaOneClickAccountHolder the novalnetDirectDebitSepaOneClickAccountHolder to set
     */
    public void setNovalnetDirectDebitSepaOneClickAccountHolder(final String novalnetDirectDebitSepaOneClickAccountHolder) {
        this.novalnetDirectDebitSepaOneClickAccountHolder = novalnetDirectDebitSepaOneClickAccountHolder;
    }

    /**
     * @return the novalnetDirectDebitSepaOneClickMaskedAccountIban
     */
    public String getNovalnetDirectDebitSepaOneClickMaskedAccountIban() {
        return novalnetDirectDebitSepaOneClickMaskedAccountIban;
    }

    /**
     * @param novalnetDirectDebitSepaOneClickMaskedAccountIban the novalnetDirectDebitSepaOneClickMaskedAccountIban to set
     */
    public void setNovalnetDirectDebitSepaOneClickMaskedAccountIban(final String novalnetDirectDebitSepaOneClickMaskedAccountIban) {
        this.novalnetDirectDebitSepaOneClickMaskedAccountIban = novalnetDirectDebitSepaOneClickMaskedAccountIban;
    }

    /**
     * @return the novalnetGuaranteedDirectDebitSepaDateOfBirth
     */
    public String getNovalnetGuaranteedDirectDebitSepaDateOfBirth() {
        return novalnetGuaranteedDirectDebitSepaDateOfBirth;
    }

    /**
     * @param novalnetGuaranteedDirectDebitSepaDateOfBirth the novalnetGuaranteedDirectDebitSepaDateOfBirth to set
     */
    public void setNovalnetGuaranteedDirectDebitSepaDateOfBirth(final String novalnetGuaranteedDirectDebitSepaDateOfBirth) {
        this.novalnetGuaranteedDirectDebitSepaDateOfBirth = novalnetGuaranteedDirectDebitSepaDateOfBirth;
    }

    /**
     * @return the novalnetDirectDebitSepaGuaranteeProcess
     */
    public boolean isNovalnetDirectDebitSepaGuaranteeProcess() {
        return novalnetDirectDebitSepaGuaranteeProcess;
    }

    /**
     * @param novalnetDirectDebitSepaGuaranteeProcess the novalnetDirectDebitSepaGuaranteeProcess to set
     */
    public void setNovalnetDirectDebitSepaGuaranteeProcess(final boolean novalnetDirectDebitSepaGuaranteeProcess) {
        this.novalnetDirectDebitSepaGuaranteeProcess = novalnetDirectDebitSepaGuaranteeProcess;
    }

    /**
     * @return the novalnetGuaranteedInvoiceDateOfBirth
     */
    public String getNovalnetGuaranteedInvoiceDateOfBirth() {
        return novalnetGuaranteedInvoiceDateOfBirth;
    }

    /**
     * @param novalnetGuaranteedInvoiceDateOfBirth the novalnetGuaranteedInvoiceDateOfBirth to set
     */
    public void setNovalnetGuaranteedInvoiceDateOfBirth(final String novalnetGuaranteedInvoiceDateOfBirth) {
        this.novalnetGuaranteedInvoiceDateOfBirth = novalnetGuaranteedInvoiceDateOfBirth;
    }

    /**
     * @return the novalnetInvoiceGuaranteeProcess
     */
    public boolean isNovalnetInvoiceGuaranteeProcess() {
        return novalnetInvoiceGuaranteeProcess;
    }

    /**
     * @param novalnetInvoiceGuaranteeProcess the novalnetInvoiceGuaranteeProcess to set
     */
    public void setNovalnetInvoiceGuaranteeProcess(final boolean novalnetInvoiceGuaranteeProcess) {
        this.novalnetInvoiceGuaranteeProcess = novalnetInvoiceGuaranteeProcess;
    }

    /**
     * @return the novalnetPaypalOneClickPpTransactionId
     */
    public String getNovalnetPaypalOneClickPpTransactionId() {
        return novalnetPaypalOneClickPpTransactionId;
    }

    /**
     * @param novalnetPaypalOneClickPpTransactionId the novalnetPaypalOneClickPpTransactionId to set
     */
    public void setNovalnetPaypalOneClickPpTransactionId(final String novalnetPaypalOneClickPpTransactionId) {
        this.novalnetPaypalOneClickPpTransactionId = novalnetPaypalOneClickPpTransactionId;
    }

    /**
     * @return the novalnetPaypalOneClickRefTransactionId
     */
    public String getNovalnetPaypalOneClickRefTransactionId() {
        return novalnetPaypalOneClickRefTransactionId;
    }

    /**
     * @param novalnetPaypalOneClickRefTransactionId the novalnetPaypalOneClickRefTransactionId to set
     */
    public void setNovalnetPaypalOneClickRefTransactionId(final String novalnetPaypalOneClickRefTransactionId) {
        this.novalnetPaypalOneClickRefTransactionId = novalnetPaypalOneClickRefTransactionId;
    }

    /**
     * @return the billingAddress
     */
    public AddressForm getBillingAddress() {
        return billingAddress;
    }

    /**
     * @param billingAddress the billingAddress to set
     */
    public void setBillingAddress(final AddressForm billingAddress) {
        this.billingAddress = billingAddress;
    }

    /**
     * @return the newBillingAddress
     */
    public boolean isNewBillingAddress() {
        return newBillingAddress;
    }

    /**
     * @param newBillingAddress the newBillingAddress to set
     */
    public void setNewBillingAddress(final boolean newBillingAddress) {
        this.newBillingAddress = newBillingAddress;
    }

    /**
     * @return the useDeliveryAddress
     */
    public boolean isUseDeliveryAddress() {
        return useDeliveryAddress;
    }

    /**
     * @param useDeliveryAddress the useDeliveryAddress to set
     */
    public void setUseDeliveryAddress(final boolean useDeliveryAddress) {
        this.useDeliveryAddress = useDeliveryAddress;
    }

    /**
     * @return the savePaymentInfo
     */
    public boolean isSavePaymentInfo() {
        return savePaymentInfo;
    }

    /**
     * @param savePaymentInfo the savePaymentInfo to set
     */
    public void setSavePaymentInfo(final boolean savePaymentInfo) {
        this.savePaymentInfo = savePaymentInfo;
    }

    /**
     * @return the companyName
     */
    public String getCompanyName() {
        return companyName;
    }

    /**
     * @param companyName the companyName to set
     */
    public void setCompanyName(final String companyName) {
        this.companyName = companyName;
    }
}
