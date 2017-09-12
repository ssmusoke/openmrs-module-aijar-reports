package org.openmrs.module.ugandaemrreports.definition.data.definition;

import java.util.Date;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.PatientARV;

public class CBSPatientARVDataDefinition
        extends BaseDataDefinition
        implements PatientDataDefinition {
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Cohort cohort;

    public Class<?> getDataType() {
        return PatientARV.class;
    }

    public Date getStartDate() {
        return this.startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Cohort getCohort() {
        return this.cohort;
    }

    public void setCohort(Cohort cohort) {
        this.cohort = cohort;
    }
}