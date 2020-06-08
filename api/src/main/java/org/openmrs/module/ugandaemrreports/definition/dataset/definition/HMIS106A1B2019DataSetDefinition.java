package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 */
public class HMIS106A1B2019DataSetDefinition extends BaseDataSetDefinition {
    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    public HMIS106A1B2019DataSetDefinition() {
        super();
    }

    public HMIS106A1B2019DataSetDefinition(String name, String description) {
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
