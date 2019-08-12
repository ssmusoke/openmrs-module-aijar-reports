package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.DSDMModel;

import java.util.Date;

/**
 */
public class DSDMModelDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
    public DSDMModelDataDefinition() {
        super();
    }

    public DSDMModelDataDefinition(String name) {
        super(name);
    }
    @Override
    public Class<?> getDataType() {
        return DSDMModel.class;
    }
    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }



}
