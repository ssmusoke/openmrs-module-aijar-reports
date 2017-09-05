package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;

/**
 * Created by carapai on 16/07/2017.
 */
public class SummarizedCohortDefinition extends BaseCohortDefinition {

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private String query;

    @ConfigurationProperty
    private Enums.Period period;

    @ConfigurationProperty
    private String periodComparator;

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Enums.Period getPeriod() {
        return period;
    }

    public void setPeriod(Enums.Period period) {
        this.period = period;
    }

    public String getPeriodComparator() {
        return periodComparator;
    }

    public void setPeriodComparator(String periodComparator) {
        this.periodComparator = periodComparator;
    }
}
