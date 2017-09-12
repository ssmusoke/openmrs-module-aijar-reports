package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.common.PatientMonthData;

import java.util.Date;


public class CBSPatientDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Cohort cohort;
    @ConfigurationProperty
    private Enums.DataFor dataFor;

    public Class<?> getDataType() {
        return PatientMonthData.class;
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

    public Enums.DataFor getDataFor() {
        return this.dataFor;
    }

    public void setDataFor(Enums.DataFor dataFor) {
        this.dataFor = dataFor;
    }
}