package com.novalnet.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class Custom implements Serializable {
    private static final long serialVersionUID = 1L;

    private String lang;
    private String input1;
    private String inputval1;
    private String shop_invoked;

    public String getShop_invoked() {
        return shop_invoked;
    }

    public void setShop_invoked(final String shop_invoked) {
        this.shop_invoked = shop_invoked;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(final String lang) {
        this.lang = lang;
    }

    public String getInput1() {
        return input1;
    }

    public void setInput1(final String input1) {
        this.input1 = input1;
    }

    public String getInputval1() {
        return inputval1;
    }

    public void setInputval1(final String inputval1) {
        this.inputval1 = inputval1;
    }
}
