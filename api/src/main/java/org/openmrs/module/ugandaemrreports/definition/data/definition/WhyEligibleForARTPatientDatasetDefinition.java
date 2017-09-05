package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;
import java.util.Map;

/**
 */
public class WhyEligibleForARTPatientDatasetDefinition extends BaseDataDefinition implements PatientDataDefinition {

    @ConfigurationProperty
    private Date onDate;

    public WhyEligibleForARTPatientDatasetDefinition() {
        super();
    }

    public WhyEligibleForARTPatientDatasetDefinition(String name) {
        super(name);
    }

    public Date getOnDate() {
        return onDate;
    }

    public void setOnDate(Date onDate) {
        this.onDate = onDate;
    }

    @Override
    public Class<?> getDataType() {
        return Map.class;
    }
}
