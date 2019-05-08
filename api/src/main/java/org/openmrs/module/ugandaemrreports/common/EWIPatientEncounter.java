package org.openmrs.module.ugandaemrreports.common;

public class EWIPatientEncounter {
    private Integer personId;
    private Integer encounterId;
    private String encounterDate;
    private String nextVisitDate;
    private String baselinePickupDate;
    private Integer numberOfDaysPickedUpAtBaeline;
    private String pickup1Date;

    public EWIPatientEncounter() {
    }

    public EWIPatientEncounter(Integer personId, String baselinePickupDate) {
        this.personId = personId;
        this.baselinePickupDate = baselinePickupDate;
    }

    public EWIPatientEncounter(Integer personId, Integer numberOfDaysPickedUpAtBaeline) {
        this.personId = personId;
        this.numberOfDaysPickedUpAtBaeline = numberOfDaysPickedUpAtBaeline;
    }

    public EWIPatientEncounter(Integer personId, Integer encounterId, String encounterDate, String nextVisitDate) {
        this.personId = personId;
        this.encounterId = encounterId;
        this.encounterDate = encounterDate;
        this.nextVisitDate = nextVisitDate;
    }


    public EWIPatientEncounter(Integer personId, Integer encounterId, String encounterDate, String nextVisitDate,
                               String baselinePickupDate, Integer numberOfDaysPickedUpAtBaeline, String pickup1Date) {
        this.personId = personId;
        this.encounterId = encounterId;
        this.encounterDate = encounterDate;
        this.nextVisitDate = nextVisitDate;
        this.baselinePickupDate = baselinePickupDate;
        this.numberOfDaysPickedUpAtBaeline = numberOfDaysPickedUpAtBaeline;
        this.pickup1Date = pickup1Date;
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

    public String getBaselinePickupDate() {
        return baselinePickupDate;
    }

    public void setBaselinePickupDate(String baselinePickupDate) {
        this.baselinePickupDate = baselinePickupDate;
    }

    public Integer getNumberOfDaysPickedUpAtBaseline() {
        return numberOfDaysPickedUpAtBaeline;
    }

    public void setNumberOfDaysPickedUpAtBaseline(Integer numberOfDaysPickedUpAtBaeline) {
        this.numberOfDaysPickedUpAtBaeline = numberOfDaysPickedUpAtBaeline;
    }

    public String getPickup1Date() {
        return pickup1Date;
    }

    public void setPickup1Date(String pickup1Date) {
        this.pickup1Date = pickup1Date;
    }
}
