package org.openmrs.module.ugandaemrreports.common;


public class ViralLoad {
    private Integer patientId;

    private Integer monthsSinceArt;
    private Integer valueCoded;
    private Double valueNumeric;

    public ViralLoad() {
    }

    public ViralLoad(Integer patientId, Integer monthsSinceArt, Integer valueCoded, Double valueNumeric) {
        this.patientId = patientId;
        this.monthsSinceArt = monthsSinceArt;
        this.valueCoded = valueCoded;
        this.valueNumeric = valueNumeric;
    }

    public Integer getPatientId() {
        return this.patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getMonthsSinceArt() {
        return this.monthsSinceArt;
    }

    public void setMonthsSinceArt(Integer monthsSinceArt) {
        this.monthsSinceArt = monthsSinceArt;
    }

    public Integer getValueCoded() {
        return this.valueCoded;
    }

    public void setValueCoded(Integer valueCoded) {
        this.valueCoded = valueCoded;
    }

    public Double getValueNumeric() {
        return this.valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }
}