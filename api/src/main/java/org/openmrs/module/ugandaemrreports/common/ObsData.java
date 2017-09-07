package org.openmrs.module.ugandaemrreports.common;

import java.util.Date;

/**
 * Created by carapai on 06/09/2017.
 */
public class ObsData {
    private Integer patientId;
    private String conceptId;
    private Integer encounterId;
    private Date encounterDate;
    private String val;

    public ObsData(Integer patientId, String conceptId, Integer encounterId, Date encounterDate, String val) {
        this.patientId = patientId;
        this.conceptId = conceptId;
        this.encounterId = encounterId;
        this.encounterDate = encounterDate;
        this.val = val;
    }

    public Integer getPatientId() {
        return patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public String getConceptId() {
        return conceptId;
    }

    public void setConceptId(String conceptId) {
        this.conceptId = conceptId;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Integer getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Integer encounterId) {
        this.encounterId = encounterId;
    }
}
