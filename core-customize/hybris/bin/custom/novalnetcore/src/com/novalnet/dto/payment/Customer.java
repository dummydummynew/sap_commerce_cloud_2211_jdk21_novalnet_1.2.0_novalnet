package com.novalnet.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@SuppressWarnings({"java:S116", "java:S100"})
public class Customer implements Serializable {
    private static final long serialVersionUID = 1L;
    private String customer_ip;
    private String customer_no;
    private String gender;
    private String first_name;
    private String last_name;
    private String email;
    private Billing billing;
    private Shipping shipping;
    private String birth_date;

    public String getBirth_date() {
        return birth_date;
    }

    public void setBirth_date(final String birth_date) {
        this.birth_date = birth_date;
    }

    public String getCustomer_ip() {
        return customer_ip;
    }

    public void setCustomer_ip(final String customer_ip) {
        this.customer_ip = customer_ip;
    }

    public String getCustomer_no() {
        return customer_no;
    }

    public void setCustomer_no(final String customer_no) {
        this.customer_no = customer_no;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(final String gender) {
        this.gender = gender;
    }

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

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public Billing getBilling() {
        return billing;
    }

    public void setBilling(final Billing billing) {
        this.billing = billing;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public void setShipping(final Shipping shipping) {
        this.shipping = shipping;
    }
}



