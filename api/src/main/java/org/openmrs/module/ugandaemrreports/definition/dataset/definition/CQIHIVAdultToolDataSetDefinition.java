package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

public class CQIHIVAdultToolDataSetDefinition extends BaseDataSetDefinition {

    private static final long serialVersionUID = 6405483324151390487L;

    @ConfigurationProperty
    private  Date endDate;

    public CQIHIVAdultToolDataSetDefinition() { super(); }

    public CQIHIVAdultToolDataSetDefinition(String name, String description, Date endDate) {
        super(name, description);
        this.endDate = endDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
