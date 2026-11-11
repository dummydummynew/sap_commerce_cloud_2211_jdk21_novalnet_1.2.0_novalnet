/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.novalnet.dto;

public class AddressForm {
    private String addressId;
    private String titleCode;
    private String firstName;
    private String lastName;
    private String line1;
    private String line2;
    private String townCity;
    private String regionIso;
    private String postcode;
    private String countryIso;
    private Boolean saveInAddressBook;
    private Boolean defaultAddress;
    private Boolean shippingAddress;
    private Boolean billingAddress;
    private Boolean editAddress;
    private String phone;

    public String getAddressId() {
        return addressId;
    }

    public void setAddressId(final String addressId) {
        this.addressId = addressId;
    }

    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(final String titleCode) {
        this.titleCode = titleCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getLine1() {
        return line1;
    }

    public void setLine1(final String line1) {
        this.line1 = line1;
    }

    public String getLine2() {
        return line2;
    }

    public void setLine2(final String line2) {
        this.line2 = line2;
    }

    public String getTownCity() {
        return townCity;
    }

    public void setTownCity(final String townCity) {
        this.townCity = townCity;
    }

    public String getRegionIso() {
        return regionIso;
    }

    public void setRegionIso(final String regionIso) {
        this.regionIso = regionIso;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(final String postcode) {
        this.postcode = postcode;
    }

    public String getCountryIso() {
        return countryIso;
    }

    public void setCountryIso(final String countryIso) {
        this.countryIso = countryIso;
    }

    public Boolean getSaveInAddressBook() {
        return saveInAddressBook;
    }

    public void setSaveInAddressBook(final Boolean saveInAddressBook) {
        this.saveInAddressBook = saveInAddressBook;
    }

    public Boolean getDefaultAddress() {
        return defaultAddress;
    }

    public void setDefaultAddress(final Boolean defaultAddress) {
        this.defaultAddress = defaultAddress;
    }

    public Boolean getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(final Boolean shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public Boolean getBillingAddress() {
        return billingAddress;
    }

    public void setBillingAddress(final Boolean billingAddress) {
        this.billingAddress = billingAddress;
    }

    public Boolean getEditAddress() {
        return editAddress;
    }

    public void setEditAddress(final Boolean editAddress) {
        this.editAddress = editAddress;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(final String phone) {
        this.phone = phone;
    }
}

