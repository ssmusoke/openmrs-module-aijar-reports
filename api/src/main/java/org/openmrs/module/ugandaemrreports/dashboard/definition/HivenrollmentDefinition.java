package org.openmrs.module.ugandaemrreports.dashboard.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;

import java.util.Date;

public class HivenrollmentDefinition extends BaseDataSetDefinition {

    public HivenrollmentDefinition(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    private Date startDate, endDate;

    public HivenrollmentDefinition() {

    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
