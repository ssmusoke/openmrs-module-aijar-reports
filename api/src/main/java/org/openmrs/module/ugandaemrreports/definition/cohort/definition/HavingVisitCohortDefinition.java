package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.PatientSetService;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 16/06/2016.
 */
public class HavingVisitCohortDefinition extends BaseCohortDefinition {

    @ConfigurationProperty
    private PatientSetService.TimeModifier timeModifier;

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private List<EncounterType> encounterTypes;

    @ConfigurationProperty
    private Concept question;

    public PatientSetService.TimeModifier getTimeModifier() {
        return timeModifier;
    }

    public void setTimeModifier(PatientSetService.TimeModifier timeModifier) {
        this.timeModifier = timeModifier;
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

    public List<EncounterType> getEncounterTypes() {
        return encounterTypes;
    }

    public void setEncounterTypes(List<EncounterType> encounterTypes) {
        this.encounterTypes = encounterTypes;
    }

    public Concept getQuestion() {
        return question;
    }

    public void setQuestion(Concept question) {
        this.question = question;
    }
}
