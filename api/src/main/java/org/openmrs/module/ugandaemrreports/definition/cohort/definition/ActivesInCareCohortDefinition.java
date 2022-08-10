package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 */
@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
public class ActivesInCareCohortDefinition extends BaseCohortDefinition {

    private static final long serialVersionUID = 1L;

    @Autowired
    public HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private String lostToFollowupDays;

    public HIVCohortDefinitionLibrary getHivCohortDefinitionLibrary() {
        return hivCohortDefinitionLibrary;
    }

    public void setHivCohortDefinitionLibrary(HIVCohortDefinitionLibrary hivCohortDefinitionLibrary) {
        this.hivCohortDefinitionLibrary = hivCohortDefinitionLibrary;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getLostToFollowupDays() { return lostToFollowupDays; }

    public void setLostToFollowupDays(String lostToFollowupDays) { this.lostToFollowupDays = lostToFollowupDays; }
}
