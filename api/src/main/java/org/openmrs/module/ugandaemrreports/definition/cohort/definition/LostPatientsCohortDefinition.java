package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

import java.util.Date;

/**
 * Created by carapai on 28/06/2016.
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
public class LostPatientsCohortDefinition extends BaseCohortDefinition {

    private static final long serialVersionUID = 1L;

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private Integer minimumDays;

    @ConfigurationProperty
    private Integer maximumDays;


    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Integer getMinimumDays() {
        return minimumDays;
    }

    public void setMinimumDays(Integer minimumDays) {
        this.minimumDays = minimumDays;
    }

    public Integer getMaximumDays() {
        return maximumDays;
    }

    public void setMaximumDays(Integer maximumDays) {
        this.maximumDays = maximumDays;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
