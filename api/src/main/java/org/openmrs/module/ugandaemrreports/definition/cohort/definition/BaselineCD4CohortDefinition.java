package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

public class BaselineCD4CohortDefinition
        extends BaseCohortDefinition {
    @ConfigurationProperty
    private Double valueNumericFrom;
    @ConfigurationProperty
    private Double valueNumericTo;

    public Double getValueNumericFrom() {
        return this.valueNumericFrom;
    }

    public void setValueNumericFrom(Double valueNumericFrom) {
        this.valueNumericFrom = valueNumericFrom;
    }

    public Double getValueNumericTo() {
        return this.valueNumericTo;
    }

    public void setValueNumericTo(Double valueNumericTo) {
        this.valueNumericTo = valueNumericTo;
    }
}