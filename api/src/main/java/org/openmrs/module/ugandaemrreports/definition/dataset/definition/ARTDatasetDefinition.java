package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 * Created by carapai on 06/04/2016.
 */
public class ARTDatasetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405583324151111487L;
    @ConfigurationProperty
    private Date startDate;

    public ARTDatasetDefinition() {
        super();
    }

    public ARTDatasetDefinition(String name, String description) {
        super(name, description);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
