package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 */
public class AppointmentFollowupDatasetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405583324151111487L;
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Date endDate;

    public AppointmentFollowupDatasetDefinition() {
        super();
    }

    public AppointmentFollowupDatasetDefinition(String name, String description) {
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
