package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;

/**
 */
public class LongRefillsCohortDefinition extends BaseCohortDefinition {

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private Integer minimumMonths;

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

    public Integer getMinimumMonths() {
        return minimumMonths;
    }

    public void setMinimumMonths(Integer minimumMonths) {
        this.minimumMonths = minimumMonths;
    }
}


