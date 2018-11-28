package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

public class DueForViralLoadPatientDataDefinition extends BaseCohortDefinition {
    @ConfigurationProperty
    private Date endDate;
    @ConfigurationProperty
    private Date startDate;


    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}