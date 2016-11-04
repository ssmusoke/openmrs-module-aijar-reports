package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 * Created by carapai on 17/10/2016.
 */
public class CBSAdultDatasetDefinition extends BaseDataSetDefinition {
    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    public CBSAdultDatasetDefinition() {
        super();
    }

    public CBSAdultDatasetDefinition(String name, String description) {
        super(name, description);
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
