package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.ArtPatientData;

import java.util.Date;

/**
 */
public class ArtPatientDataDefinition extends BaseDataDefinition implements PatientDataDefinition {

    @ConfigurationProperty
    private Date startDate;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    @Override
    public Class<?> getDataType() {
        return ArtPatientData.class;
    }
}
