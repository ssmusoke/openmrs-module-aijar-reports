package org.openmrs.module.ugandaemrreports.definition.dataset.definition.rest;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 */
public class AppointmentDatasetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405584424151111487L;
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Date endDate;

    public AppointmentDatasetDefinition() {
        super();
    }

    public AppointmentDatasetDefinition(String name, String description) {
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
