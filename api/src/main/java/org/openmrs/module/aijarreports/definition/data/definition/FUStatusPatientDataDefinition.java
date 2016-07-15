package org.openmrs.module.aijarreports.definition.data.definition;

import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;
import java.util.Map;

/**
 * Created by carapai on 05/07/2016.
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
    private Date onDate;

    @ConfigurationProperty
    private Period period;

    @ConfigurationProperty
    private int periodToAdd = 0;

    public Date getOnDate() {
        return onDate;
    }

    public void setOnDate(Date onDate) {
        this.onDate = onDate;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
        this.period = period;
    }

    public int getPeriodToAdd() {
        return periodToAdd;
    }

    public void setPeriodToAdd(int periodToAdd) {
        this.periodToAdd = periodToAdd;
    }
}
