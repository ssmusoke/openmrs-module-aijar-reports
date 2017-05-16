package org.openmrs.module.ugandaemrreports.common;


import java.util.Date;

public class PatientNonSuppressingData {
    private Integer patientId;
    private Date encounterDate;
    private Integer encounterId;
    private Integer concept;
    private Integer valueCoded;
    private Date valueDatetime;
    private Double valueNumeric;
    private String valueText;
    private Date birthDate;

    public PatientNonSuppressingData() {
    }

    public PatientNonSuppressingData(Integer patientId) {
        this.patientId = patientId;

    }

    public PatientNonSuppressingData(Integer patientId, Integer encounterId, Date encounterDate, Integer concept, Integer valueCoded, Date valueDatetime, Double valueNumeric, String valueText, Date birthDate) {
        this.patientId = patientId;
        this.encounterId = encounterId;
        this.encounterDate = encounterDate;
        this.concept = concept;
        this.valueCoded = valueCoded;
        this.valueDatetime = valueDatetime;
        this.valueNumeric = valueNumeric;
        this.valueText = valueText;
        this.birthDate = birthDate;
    }

    public Integer getPatientId() {
        return this.patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Date getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(Date encounterDate) {
        this.encounterDate = encounterDate;
    }

    public Integer getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(Integer encounterId) {
        this.encounterId = encounterId;
    }

    public Integer getConcept() {
        return concept;
    }

    public void setConcept(Integer concept) {
        this.concept = concept;
    }

    public Integer getValueCoded() {
        return valueCoded;
    }

    public void setValueCoded(Integer valueCoded) {
        this.valueCoded = valueCoded;
    }

    public Date getValueDatetime() {
        return valueDatetime;
    }

    public void setValueDatetime(Date valueDatetime) {
        this.valueDatetime = valueDatetime;
    }

    public Double getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public String getValueText() {
        return valueText;
    }

    public void setValueText(String valueText) {
        this.valueText = valueText;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }
}