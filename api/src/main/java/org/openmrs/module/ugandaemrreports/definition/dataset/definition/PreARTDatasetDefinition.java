package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 */
public class PreARTDatasetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405583377151111487L;

    @ConfigurationProperty
    private Date startDate;

    public PreARTDatasetDefinition() {
        super();
    }

    public PreARTDatasetDefinition(String name, String description) {
        super(name, description);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
