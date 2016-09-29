package org.openmrs.module.ugandaemrreports.common;

import org.openmrs.module.reporting.common.DateUtil;

import java.util.Date;

/**
 * Created by carapai on 15/09/2016.
 */
public class DeathDate {
    private Date deathDate;

    public DeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String toString() {
        return DateUtil.formatDate(deathDate, "dd/MMM/yyyy", "");
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }
}
