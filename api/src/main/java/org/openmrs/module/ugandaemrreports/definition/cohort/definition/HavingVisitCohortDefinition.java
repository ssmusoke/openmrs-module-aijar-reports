package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reportingcompatibility.service.ReportService.TimeModifier;

/**
 * Created by carapai on 16/06/2016.
 */
public class HavingVisitCohortDefinition extends BaseCohortDefinition {

    @ConfigurationProperty
    private TimeModifier timeModifier;

    @ConfigurationProperty
    private Date startDate;

    @ConfigurationProperty
    private Date endDate;

    @ConfigurationProperty
    private List<EncounterType> encounterTypes;

    @ConfigurationProperty
    private Concept question;

    public TimeModifier getTimeModifier() {
        return timeModifier;
    }

    public void setTimeModifier(TimeModifier timeModifier) {
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
