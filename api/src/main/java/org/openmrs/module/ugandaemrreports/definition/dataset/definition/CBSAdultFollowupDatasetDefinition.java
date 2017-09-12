package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

public class CBSAdultFollowupDatasetDefinition
        extends BaseDataSetDefinition {
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Date endDate;

    public CBSAdultFollowupDatasetDefinition() {
    }

    public CBSAdultFollowupDatasetDefinition(String name, String description) {
        super(name, description);
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return this.endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
