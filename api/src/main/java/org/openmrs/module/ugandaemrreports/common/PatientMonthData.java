package org.openmrs.module.ugandaemrreports.common;


public class PatientMonthData {
    private Integer patientId;
    private Integer month;
    private Integer dataType;

    public PatientMonthData(Integer patientId, Integer month) {
        this.patientId = patientId;
        this.month = month;
    }

    public PatientMonthData(Integer patientId, Integer month, Integer dataType) {
        this.patientId = patientId;
        this.month = month;
        this.dataType = dataType;
    }

    public Integer getPatientId() {
        return this.patientId;
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
    }

    public Integer getMonth() {
        return this.month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getDataType() {
        return this.dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }
}