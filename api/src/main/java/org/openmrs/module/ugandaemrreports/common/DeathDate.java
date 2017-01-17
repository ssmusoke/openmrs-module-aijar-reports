package org.openmrs.module.ugandaemrreports.common;

import org.openmrs.module.reporting.common.DateUtil;

import java.util.Date;

/**
 * Created by carapai on 15/09/2016.
 */
public class DeathDate {
    private Date deathDate;
    private String caseOfDeath;

    public DeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String toString() {
        return DateUtil.formatDate(deathDate, "dd/MMM/yyyy", "");
    }

    public DeathDate(Date deathDate, String caseOfDeath) {
        this.deathDate = deathDate;
        this.caseOfDeath = caseOfDeath;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String getCaseOfDeath() {
        return caseOfDeath;
    }

    public void setCaseOfDeath(String caseOfDeath) {
        this.caseOfDeath = caseOfDeath;
    }
}
