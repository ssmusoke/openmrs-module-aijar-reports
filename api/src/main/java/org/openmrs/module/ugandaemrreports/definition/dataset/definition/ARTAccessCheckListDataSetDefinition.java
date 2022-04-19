package org.openmrs.module.ugandaemrreports.definition.dataset.definition;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 * Created by carapai on 17/10/2016.
 */
public class ARTAccessCheckListDataSetDefinition extends PatientDataSetDefinition {
    @ConfigurationProperty
    private Date endDate;

    private Cohort baseCohort;

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Cohort getBaseCohort() {return baseCohort; }

    public void setBaseCohort(Cohort baseCohort) { this.baseCohort = baseCohort; }

}
