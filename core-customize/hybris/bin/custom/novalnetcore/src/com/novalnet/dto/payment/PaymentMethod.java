package com.novalnet.dto.payment;


import com.novalnet.dto.AddressForm;


@SuppressWarnings({ "java:S116", "java:S100", "java:S1448" })
public class PaymentMethod {

    private String paymentId;

    private String paymentType;

    private Boolean saveInAccount;

    private Boolean newBillingAddress;

    private AddressForm billingAddress;

    private boolean useDeliveryAddress;

    private PaymentData paymentData;

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

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(final String paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(final String paymentType) {
        this.paymentType = paymentType;
    }

    public Boolean getSaveInAccount() {
        return saveInAccount;
    }

    public void setSaveInAccount(final Boolean saveInAccount) {
        this.saveInAccount = saveInAccount;
    }

    public Boolean getNewBillingAddress() {
        return newBillingAddress;
    }

    public void setNewBillingAddress(final Boolean newBillingAddress) {
        this.newBillingAddress = newBillingAddress;
    }

    public AddressForm getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final AddressForm billingAddress) {
        this.billingAddress = billingAddress;
    }

    public boolean isUseDeliveryAddress() {
        return useDeliveryAddress;
    }

    public void setUseDeliveryAddress(final boolean useDeliveryAddress) {
        this.useDeliveryAddress = useDeliveryAddress;
    }

    public String getBillTo_city() {
        return billTo_city;
    }

    public void setBillTo_city(final String billTo_city) {
        this.billTo_city = billTo_city;
    }

    public String getBillTo_country() {
        return billTo_country;
    }

    public void setBillTo_country(final String billTo_country) {
        this.billTo_country = billTo_country;
    }

    public String getBillTo_customerID() {
        return billTo_customerID;
    }

    public void setBillTo_customerID(final String billTo_customerID) {
        this.billTo_customerID = billTo_customerID;
    }

    public String getBillTo_email() {
        return billTo_email;
    }

    public void setBillTo_email(final String billTo_email) {
        this.billTo_email = billTo_email;
    }

    public String getBillTo_firstName() {
        return billTo_firstName;
    }

    public void setBillTo_firstName(final String billTo_firstName) {
        this.billTo_firstName = billTo_firstName;
    }

    public String getBillTo_lastName() {
        return billTo_lastName;
    }

    public void setBillTo_lastName(final String billTo_lastName) {
        this.billTo_lastName = billTo_lastName;
    }

    public String getBillTo_phoneNumber() {
        return billTo_phoneNumber;
    }

    public void setBillTo_phoneNumber(final String billTo_phoneNumber) {
        this.billTo_phoneNumber = billTo_phoneNumber;
    }

    public String getBillTo_postalCode() {
        return billTo_postalCode;
    }

    public void setBillTo_postalCode(final String billTo_postalCode) {
        this.billTo_postalCode = billTo_postalCode;
    }

    public String getBillTo_titleCode() {
        return billTo_titleCode;
    }

    public void setBillTo_titleCode(final String billTo_titleCode) {
        this.billTo_titleCode = billTo_titleCode;
    }

    public String getBillTo_state() {
        return billTo_state;
    }

    public void setBillTo_state(final String billTo_state) {
        this.billTo_state = billTo_state;
    }

    public String getBillTo_street1() {
        return billTo_street1;
    }

    public void setBillTo_street1(final String billTo_street1) {
        this.billTo_street1 = billTo_street1;
    }

    public String getBillTo_street2() {
        return billTo_street2;
    }

    public void setBillTo_street2(final String billTo_street2) {
        this.billTo_street2 = billTo_street2;
    }

    public PaymentData getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(final PaymentData paymentData) {
        this.paymentData = paymentData;
    }
}
