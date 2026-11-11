package com.novalnet.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NovalnetPaymentRequest implements Serializable {
    private Merchant merchant;
    private Customer customer;
    private Transaction transaction;
    private Custom custom;
    private HostedPage hosted_page;

    public HostedPage getHosted_page() {
        return hosted_page;
    }

    public void setHosted_page(final HostedPage hosted_page) {
        this.hosted_page = hosted_page;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(final Merchant merchant) {
        this.merchant = merchant;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(final Customer customer) {
        this.customer = customer;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(final Transaction transaction) {
        this.transaction = transaction;
    }

    public Custom getCustom() {
        return custom;
    }

    public void setCustom(final Custom custom) {
        this.custom = custom;
    }
}
