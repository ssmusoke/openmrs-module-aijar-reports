package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;

/**
 */
public class ViralLoadCohortDefinition extends BaseCohortDefinition {
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Date endDate;
    @ConfigurationProperty
    private Double copiesFrom;
    @ConfigurationProperty
    private Double copiesTo;
    @ConfigurationProperty
    private Boolean detected = Boolean.FALSE;
    @ConfigurationProperty
    private Boolean notDetected = Boolean.FALSE;

    public Double getCopiesFrom() {
        return copiesFrom;
    }

    public void setCopiesFrom(Double copiesFrom) {
        this.copiesFrom = copiesFrom;
    }

    public Double getCopiesTo() {
        return copiesTo;
    }

    public void setCopiesTo(Double copiesTo) {
        this.copiesTo = copiesTo;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Boolean getDetected() {
        return detected;
    }

    public void setDetected(Boolean detected) {
        this.detected = detected;
    }

    public Boolean getNotDetected() {
        return notDetected;
    }

    public void setNotDetected(Boolean notDetected) {
        this.notDetected = notDetected;
    }
}
