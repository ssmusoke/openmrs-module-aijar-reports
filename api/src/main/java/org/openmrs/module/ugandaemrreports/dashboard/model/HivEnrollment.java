package org.openmrs.module.ugandaemrreports.dashboard.model;



public class HivEnrollment {

    public HivEnrollment(String month, Integer numberEnrolled) {
        this.month = month;
        this.numberEnrolled = numberEnrolled;
    }

    private String month;
    private Integer numberEnrolled;

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public Integer getNumberEnrolled() {
        return numberEnrolled;
    }

    public void setNumberEnrolled(Integer endDate) {
        this.numberEnrolled = numberEnrolled;
    }
}
