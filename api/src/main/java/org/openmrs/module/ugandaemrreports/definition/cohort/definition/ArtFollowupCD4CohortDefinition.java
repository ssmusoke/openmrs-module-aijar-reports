package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;

/**
 * Created by carapai on 10/01/2017.
 */
public class ArtFollowupCD4CohortDefinition extends BaseCohortDefinition {

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Integer periodDifference;

    @ConfigurationProperty
    private Enums.Period period;

    @ConfigurationProperty
    private Enums.PeriodInterval periodInterval;

    @ConfigurationProperty
    private Boolean allBaseCD4 = Boolean.TRUE;

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

    public Boolean getAllBaseCD4() {
        return allBaseCD4;
    }

    public void setAllBaseCD4(Boolean allBaseCD4) {
        this.allBaseCD4 = allBaseCD4;
    }
}
