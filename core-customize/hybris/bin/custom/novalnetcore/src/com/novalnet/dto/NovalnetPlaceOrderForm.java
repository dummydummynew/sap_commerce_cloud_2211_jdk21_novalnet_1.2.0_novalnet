/*
 * Copyright (c) 2026 SAP SE or an SAP affiliate company. All rights reserved.
 */

package com.novalnet.dto;

import de.hybris.platform.b2bacceleratorfacades.order.data.B2BReplenishmentRecurrenceEnum;
import de.hybris.platform.cronjob.enums.DayOfWeek;


import java.util.Date;
import java.util.List;

public class NovalnetPlaceOrderForm {

    private String securityCode;
    private boolean termsCheck;
    private boolean replenishmentOrder;
    private Date replenishmentStartDate;
    private String nDays;
    private String nWeeks;
    private String nthDayOfMonth;
    private B2BReplenishmentRecurrenceEnum replenishmentRecurrence;
    private List<DayOfWeek> nDaysOfWeek;

    public String getSecurityCode() {
        return securityCode;
    }

    public void setSecurityCode(final String securityCode) {
        this.securityCode = securityCode;
    }

    public boolean isTermsCheck() {
        return termsCheck;
    }

    public void setTermsCheck(final boolean termsCheck) {
        this.termsCheck = termsCheck;
    }

    public boolean isReplenishmentOrder() {
        return replenishmentOrder;
    }

    public void setReplenishmentOrder(boolean replenishmentOrder) {
        this.replenishmentOrder = replenishmentOrder;
    }

    public Date getReplenishmentStartDate() {
        return replenishmentStartDate;
    }

    public void setReplenishmentStartDate(Date replenishmentStartDate) {
        this.replenishmentStartDate = replenishmentStartDate;
    }

    public String getnDays() {
        return nDays;
    }

    public void setnDays(String nDays) {
        this.nDays = nDays;
    }

    public String getnWeeks() {
        return nWeeks;
    }

    public void setnWeeks(String nWeeks) {
        this.nWeeks = nWeeks;
    }

    public String getNthDayOfMonth() {
        return nthDayOfMonth;
    }

    public void setNthDayOfMonth(String nthDayOfMonth) {
        this.nthDayOfMonth = nthDayOfMonth;
    }

    public B2BReplenishmentRecurrenceEnum getReplenishmentRecurrence() {
        return replenishmentRecurrence;
    }

    public void setReplenishmentRecurrence(B2BReplenishmentRecurrenceEnum replenishmentRecurrence) {
        this.replenishmentRecurrence = replenishmentRecurrence;
    }

    public List<DayOfWeek> getnDaysOfWeek() {
        return nDaysOfWeek;
    }

    public void setnDaysOfWeek(List<DayOfWeek> nDaysOfWeek) {
        this.nDaysOfWeek = nDaysOfWeek;
    }
}
