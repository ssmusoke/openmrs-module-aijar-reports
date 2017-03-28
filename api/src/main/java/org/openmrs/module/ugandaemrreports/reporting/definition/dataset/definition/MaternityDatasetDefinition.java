package org.openmrs.module.ugandaemrreports.reporting.definition.dataset.definition;

import java.util.Date;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

public class MaternityDatasetDefinition extends BaseDataSetDefinition {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@ConfigurationProperty
    private Date startDate;

    public MaternityDatasetDefinition() {
        super();
    }

    public MaternityDatasetDefinition(String name, String description) {
        super(name, description);
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }	
}
