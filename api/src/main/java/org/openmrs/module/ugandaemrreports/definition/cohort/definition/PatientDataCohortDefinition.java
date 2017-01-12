package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;

/**
 * Created by carapai on 09/01/2017.
 */
public class PatientDataCohortDefinition extends BaseCohortDefinition {
    @ConfigurationProperty
    private Concept concept;

    @ConfigurationProperty
    private Boolean firstEncounter = Boolean.FALSE;

    @ConfigurationProperty
    private Boolean firstAppearance = Boolean.FALSE;

    @ConfigurationProperty
    private Enums.ValueType valueType;

    @ConfigurationProperty
    private EncounterType encounterType;

    @ConfigurationProperty
    private Object value;

    public Boolean getFirstEncounter() {
        return firstEncounter;
    }

    public void setFirstEncounter(Boolean firstEncounter) {
        this.firstEncounter = firstEncounter;
    }

    public Boolean getFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(Boolean firstAppearance) {
        this.firstAppearance = firstAppearance;
    }

    public Enums.ValueType getValueType() {
        return valueType;
    }

    public void setValueType(Enums.ValueType valueType) {
        this.valueType = valueType;
    }

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
