package com.novalnet.dto.payment;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;


@SuppressWarnings({"java:S116", "java:S100"})
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Shipping implements Serializable {
    private static final long serialVersionUID = 1L;

    private String first_name;
    private String last_name;
    private String street;
    private String city;
    private String zip;
    private String country_code;
    private String same_as_billing;

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(final String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(final String last_name) {
        this.last_name = last_name;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(final String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public String getCountry_code() {
        return country_code;
    }

    public void setCountry_code(final String country_code) {
        this.country_code = country_code;
    }

    public String getSame_as_billing() {
        return same_as_billing;
    }

    public void setSame_as_billing(final String same_as_billing) {
        this.same_as_billing = same_as_billing;
    }
}
