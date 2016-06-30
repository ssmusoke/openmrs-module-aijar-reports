package org.openmrs.module.aijarreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 * Created by carapai on 28/06/2016.
 */
public class LostPatientsCohortDefinition extends BaseCohortDefinition {

    private static final long serialVersionUID = 1L;


    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private Integer minimumDays;

    @ConfigurationProperty
    private Integer maximumDays;

    @ConfigurationProperty
    private CohortDefinition cohort;


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


    public CohortDefinition getCohort() {
        return cohort;
    }

    public void setCohort(CohortDefinition cohort) {
        this.cohort = cohort;
    }

}
