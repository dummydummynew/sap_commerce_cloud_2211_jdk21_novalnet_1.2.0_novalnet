package com.novalnet.dto.payment;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;


@JsonInclude(JsonInclude.Include.NON_EMPTY)
@SuppressWarnings({"java:S116", "java:S100", "java:S117"})
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    private String payment_type;
    private Long amount;
    private String currency;
    private String order_no;
    private String test_mode;
    private String due_date;
    private String invoice_ref;
    private String return_url;
    private String error_return_url;
    private String system_name;
    private String system_version;
    private String system_ip;
    private PaymentData payment_data;
    private String create_token;
    private Integer enforce_3d;
    private String tid;

    /**
     * @return the amount
     */
    public Long getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(final Long amount) {
        this.amount = amount;
    }

    /**
     * @return the tid
     */
    public String getTid() {
        return tid;
    }

    /**
     * @param tid the tid to set
     */
    public void setTid(final String tid) {
        this.tid = tid;
    }

    /**
     * @return the enforce_3d
     */
    public Integer getEnforce_3d() {
        return enforce_3d;
    }

    /**
     * @param enforce_3d the enforce_3d to set
     */
    public void setEnforce_3d(final Integer enforce_3d) {
        this.enforce_3d = enforce_3d;
    }

    public String getCreate_token() {
        return create_token;
    }

    public void setCreate_token(final String create_token) {
        this.create_token = create_token;
    }

    public String getPayment_type() {
        return payment_type;
    }

    public void setPayment_type(final String payment_type) {
        this.payment_type = payment_type;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(final String currency) {
        this.currency = currency;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(final String order_no) {
        this.order_no = order_no;
    }

    public String getTest_mode() {
        return test_mode;
    }

    public void setTest_mode(final String test_mode) {
        this.test_mode = test_mode;
    }

    public String getDue_date() {
        return due_date;
    }

    public void setDue_date(final String due_date) {
        this.due_date = due_date;
    }

    public String getInvoice_ref() {
        return invoice_ref;
    }

    public void setInvoice_ref(final String invoice_ref) {
        this.invoice_ref = invoice_ref;
    }

    public PaymentData getPayment_data() {
        return payment_data;
    }

    public void setPayment_data(final PaymentData payment_data) {
        this.payment_data = payment_data;
    }

    public String getReturn_url() {
        return return_url;
    }

    public void setReturn_url(final String return_url) {
        this.return_url = return_url;
    }

    public String getError_return_url() {
        return error_return_url;
    }

    public void setError_return_url(final String error_return_url) {
        this.error_return_url = error_return_url;
    }

    public String getSystem_name() {
        return system_name;
    }

    public void setSystem_name(final String system_name) {
        this.system_name = system_name;
    }

    public String getSystem_version() {
        return system_version;
    }

    public void setSystem_version(final String system_version) {
        this.system_version = system_version;
    }

    public String getSystem_ip() {
        return system_ip;
    }

    public void setSystem_ip(final String system_ip) {
        this.system_ip = system_ip;
    }
}
