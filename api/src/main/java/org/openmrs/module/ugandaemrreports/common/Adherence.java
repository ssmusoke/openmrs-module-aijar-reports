package org.openmrs.module.ugandaemrreports.common;

/**
 */
public class Adherence {
    private String patientId;
    private String name;
    private String obsDatetime;

    public Adherence() {
    }

    public Adherence(String patientId, String valueCoded, String obsDatetime) {
        this.patientId = patientId;
        this.name = valueCoded;
        this.obsDatetime = obsDatetime;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getObsDatetime() {
        return obsDatetime;
    }

    public void setObsDatetime(String obsDatetime) {
        this.obsDatetime = obsDatetime;
    }
}
