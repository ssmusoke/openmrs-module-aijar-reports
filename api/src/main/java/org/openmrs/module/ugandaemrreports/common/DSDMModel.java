package org.openmrs.module.ugandaemrreports.common;

import org.openmrs.module.reporting.common.DateUtil;

import java.time.LocalDateTime;
import java.util.Date;

/**
 */
public class DSDMModel {
    private Date dateOfEnrollment;
    private String progId;

    public DSDMModel(Date dateOfEnrollment) {
        this.dateOfEnrollment = dateOfEnrollment;
    }

    public String toString() {
        return dateOfEnrollment.toString();
    }

    public DSDMModel(Date dateOfEnrollment, String progId) {
        this.dateOfEnrollment = dateOfEnrollment;
        this.progId = progId;
    }

    public Date getdateOfEnrollment() {
        return dateOfEnrollment;
    }

    public void setdateOfEnrollment(Date dateOfEnrollment) {
        this.dateOfEnrollment = dateOfEnrollment;
    }

    public String getprogId() {
        return progId;
    }

    public void setprogId(String programId) {
        this.progId = programId;
    }

}
