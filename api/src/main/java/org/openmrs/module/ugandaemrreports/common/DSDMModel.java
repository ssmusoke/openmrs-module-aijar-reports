package org.openmrs.module.ugandaemrreports.common;

import org.openmrs.module.reporting.common.DateUtil;

import java.time.LocalDateTime;
import java.util.Date;

/**
 */
public class DSDMModel {
    private LocalDateTime dateOfEnrollment;
    private String progId;

    public DSDMModel(LocalDateTime dateOfEnrollment) {
        this.dateOfEnrollment = dateOfEnrollment;
    }

    public String toString() {
        return dateOfEnrollment.toLocalDate().atStartOfDay().toString();
    }

    public DSDMModel(LocalDateTime dateOfEnrollment, String progId) {
        this.dateOfEnrollment = dateOfEnrollment;
        this.progId = progId;
    }

    public LocalDateTime getdateOfEnrollment() {
        return dateOfEnrollment;
    }

    public void setdateOfEnrollment(LocalDateTime dateOfEnrollment) {
        this.dateOfEnrollment = dateOfEnrollment;
    }

    public String getprogId() {
        return progId;
    }

    public void setprogId(String programId) {
        this.progId = programId;
    }

}
