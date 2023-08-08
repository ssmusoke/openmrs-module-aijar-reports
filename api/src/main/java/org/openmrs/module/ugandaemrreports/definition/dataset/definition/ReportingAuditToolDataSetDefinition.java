package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

public class ReportingAuditToolDataSetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405483324151390487L;
    @ConfigurationProperty
    private String cohortList;

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;


    public ReportingAuditToolDataSetDefinition() { super(); }

    public ReportingAuditToolDataSetDefinition(String name, String description) {
        super(name, description);
    }

    public String getCohortList() {
        return cohortList;
    }

    public void setCohortList(String cohortList) {
        this.cohortList = cohortList;
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
