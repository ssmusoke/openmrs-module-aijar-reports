package org.openmrs.module.ugandaemrreports.common;


public class ViralLoad {
    private Integer patientId;
    private Integer monthsBetweenReportDateAndArt;
    private Integer monthsBetweenArtAndViralLoad;
    private Integer valueCoded;
    private Double valueNumeric;

    public ViralLoad() {
    }

    public ViralLoad(Integer patientId, Integer monthsBetweenReportDateAndArt, Integer monthsBetweenArtAndViralLoad, Integer valueCoded, Double valueNumeric) {
        this.patientId = patientId;
        this.monthsBetweenReportDateAndArt = monthsBetweenReportDateAndArt;
        this.monthsBetweenArtAndViralLoad = monthsBetweenArtAndViralLoad;
        this.valueCoded = valueCoded;
        this.valueNumeric = valueNumeric;
    }

    public Integer getPatientId() {
        return this.patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getMonthsBetweenReportDateAndArt() {
        return monthsBetweenReportDateAndArt;
    }

    public void setMonthsBetweenReportDateAndArt(Integer monthsBetweenReportDateAndArt) {
        this.monthsBetweenReportDateAndArt = monthsBetweenReportDateAndArt;
    }

    public Integer getMonthsBetweenArtAndViralLoad() {
        return monthsBetweenArtAndViralLoad;
    }

    public void setMonthsBetweenArtAndViralLoad(Integer monthsBetweenArtAndViralLoad) {
        this.monthsBetweenArtAndViralLoad = monthsBetweenArtAndViralLoad;
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