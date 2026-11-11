package com.novalnet.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@SuppressWarnings({"java:S116", "java:S100"})
public class PaymentData implements Serializable {

    private static final long serialVersionUID = 1L;

    private String account_holder;
    private String iban;
    private String bic;
    private String pan_hash;
    private String unique_id;
    private String do_redirect;
    private Boolean create_token;
    private String token;
    private String routing_number;
    private String account_number;
    private String wallet_token;

    public String getWallet_token() {
        return wallet_token;
    }

    public void setWallet_token(final String wallet_token) {
        this.wallet_token = wallet_token;
    }

    public String getAccount_holder() {
        return account_holder;
    }

    public void setAccount_holder(final String account_holder) {
        this.account_holder = account_holder;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(final String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(final String bic) {
        this.bic = bic;
    }

    public String getPan_hash() {
        return pan_hash;
    }

    public void setPan_hash(final String pan_hash) {
        this.pan_hash = pan_hash;
    }

    public String getUnique_id() {
        return unique_id;
    }

    public void setUnique_id(final String unique_id) {
        this.unique_id = unique_id;
    }

    public String getDo_redirect() {
        return do_redirect;
    }

    public void setDo_redirect(final String do_redirect) {
        this.do_redirect = do_redirect;
    }

    public Boolean getCreate_token() {
        return create_token;
    }

    public void setCreate_token(final Boolean create_token) {
        this.create_token = create_token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

    public String getRouting_number() {
        return routing_number;
    }

    public void setRouting_number(final String routing_number) {
        this.routing_number = routing_number;
    }

    public String getAccount_number() {
        return account_number;
    }

    public void setAccount_number(final String account_number) {
        this.account_number = account_number;
    }
}
