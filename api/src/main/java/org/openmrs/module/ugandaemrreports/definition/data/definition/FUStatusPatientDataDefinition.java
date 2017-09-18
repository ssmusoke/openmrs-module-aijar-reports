package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;
import java.util.Map;

/**
 */
public class FUStatusPatientDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
    public FUStatusPatientDataDefinition() {
        super();
    }

    public FUStatusPatientDataDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return Map.class;
    }

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Enums.Period period;

    @ConfigurationProperty
    private int periodToAdd = 0;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Enums.Period getPeriod() {
        return period;
    }

    public void setPeriod(Enums.Period period) {
        this.period = period;
    }

    public int getPeriodToAdd() {
        return periodToAdd;
    }

    public void setPeriodToAdd(int periodToAdd) {
        this.periodToAdd = periodToAdd;
    }
}
