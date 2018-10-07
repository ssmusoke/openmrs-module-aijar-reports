package org.openmrs.module.ugandaemrreports.common;

public class EWIPatientEncounter {
    private Integer personId;
    private Integer encounterId;
    private String encounterDate;
    private String nextVisitDate;

    public EWIPatientEncounter() {
    }

    public EWIPatientEncounter(Integer personId, Integer encounterId, String encounterDate, String nextVisitDate) {
        this.personId = personId;
        this.encounterId = encounterId;
        this.encounterDate = encounterDate;
        this.nextVisitDate = nextVisitDate;
    }

    public Integer getPersonId() {
        return personId;
    }

    public void setPersonId(Integer personId) {
        this.personId = personId;
    }

    public Integer getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Integer encounterId) {
        this.encounterId = encounterId;
    }

    public String getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(String encounterDate) {
        this.encounterDate = encounterDate;
    }

    public String getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(String nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }
}
