package org.openmrs.module.ugandaemrreports.common;


public class PatientARV {
    private Integer patientId;

    private Integer ageAtEncounter;
    private Integer monthsFromEnrollment;
    private Integer valueCoded;

    public PatientARV() {
    }

    public PatientARV(Integer patientId, Integer ageAtEncounter, Integer monthsFromEnrollment, Integer valueCoded) {
        this.patientId = patientId;
        this.ageAtEncounter = ageAtEncounter;
        this.monthsFromEnrollment = monthsFromEnrollment;
        this.valueCoded = valueCoded;
    }

    public Integer getPatientId() {
        return this.patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getAgeAtEncounter() {
        return this.ageAtEncounter;
    }

    public void setAgeAtEncounter(Integer ageAtEncounter) {
        this.ageAtEncounter = ageAtEncounter;
    }

    public Integer getMonthsFromEnrollment() {
        return this.monthsFromEnrollment;
    }

    public void setMonthsFromEnrollment(Integer monthsFromEnrollment) {
        this.monthsFromEnrollment = monthsFromEnrollment;
    }

    public Integer getValueCoded() {
        return this.valueCoded;
    }

    public void setValueCoded(Integer valueCoded) {
        this.valueCoded = valueCoded;
    }
}