package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.CD4;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;

/**
 */
public class CD4PatientDataDefinition extends BaseDataDefinition implements PatientDataDefinition {

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Integer periodDifference;

    @ConfigurationProperty
    private Enums.Period period;

    @ConfigurationProperty
    private Enums.PeriodInterval periodInterval;

    @Override
    public Class<?> getDataType() {
        return CD4.class;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Integer getPeriodDifference() {
        return periodDifference;
    }

    public void setPeriodDifference(Integer periodDifference) {
        this.periodDifference = periodDifference;
    }

    public Enums.Period getPeriod() {
        return period;
    }

    public void setPeriod(Enums.Period period) {
        this.period = period;
    }

    public Enums.PeriodInterval getPeriodInterval() {
        return periodInterval;
    }

    public void setPeriodInterval(Enums.PeriodInterval periodInterval) {
        this.periodInterval = periodInterval;
    }
}
