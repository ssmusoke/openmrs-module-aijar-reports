package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

public class TXMLCohortDefinition extends BaseCohortDefinition {

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private Integer lostToFollowupDays;

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

    public Integer getLostToFollowupDays() {
        return lostToFollowupDays;
    }

    public void setLostToFollowupDays(Integer lostToFollowupDays) {
        this.lostToFollowupDays = lostToFollowupDays;
    }
}
