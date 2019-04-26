package org.openmrs.module.ugandaemrreports.common;

import org.openmrs.module.reporting.common.DateUtil;

import java.util.Date;

/**
 */
public class DeathDate {
    private Date deathDate;
    private String caseOfDeath;
    private Integer ageAtDeath;

    public DeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public String toString() {
        return DateUtil.formatDate(deathDate, "MMM dd, yyyy", "");
    }

    public DeathDate(Date deathDate, String caseOfDeath, Integer ageAtDeath) {
        this.deathDate = deathDate;
        this.caseOfDeath = caseOfDeath;
        this.ageAtDeath = ageAtDeath;
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

    public Integer getAgeAtDeath() {
        return ageAtDeath;
    }

    public void setAgeAtDeath(Integer ageAtDeath) {
        this.ageAtDeath = ageAtDeath;
    }
}
