package org.openmrs.module.aijarreports.common;

import java.util.Date;

/**
 * Created by carapai on 12/07/2016.
 */
public class PatientData {
    private Integer patientId;
    private Date encounterDate;
    private Integer numberOfSinceLastVisit = 0;
    private Date deathDate;
    private boolean transferredOut;
    private Date nextVisitDate;

    public PatientData() {
    }

    public PatientData(Integer patientId, Date encounterDate, Integer numberOfSinceLastVisit, Date deathDate, boolean transferredOut, Date nextVisitDate) {
        this.patientId = patientId;
        this.encounterDate = encounterDate;
        this.numberOfSinceLastVisit = numberOfSinceLastVisit;
        this.deathDate = deathDate;
        this.transferredOut = transferredOut;
        this.nextVisitDate = nextVisitDate;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public Integer getNumberOfSinceLastVisit() {
        return numberOfSinceLastVisit;
    }

    public void setNumberOfSinceLastVisit(Integer numberOfSinceLastVisit) {
        this.numberOfSinceLastVisit = numberOfSinceLastVisit;
    }

    public Date getDeathDate() {
        return deathDate;
    }

    public void setDeathDate(Date deathDate) {
        this.deathDate = deathDate;
    }

    public boolean isTransferredOut() {
        return transferredOut;
    }

    public void setTransferredOut(boolean transferredOut) {
        this.transferredOut = transferredOut;
    }

    public Date getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(Date nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }
}
