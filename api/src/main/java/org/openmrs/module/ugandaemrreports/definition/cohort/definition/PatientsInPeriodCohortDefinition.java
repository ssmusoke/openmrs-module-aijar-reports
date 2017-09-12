package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;
import java.util.List;

/**
 */
public class PatientsInPeriodCohortDefinition extends BaseCohortDefinition {
    private static final long serialVersionUID = 1L;
    @ConfigurationProperty
    private Enums.Period period;
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private List<EncounterType> encounterTypes;
    @ConfigurationProperty
    private Concept question;
    @ConfigurationProperty
    private List<Concept> answers;
    @ConfigurationProperty
    private TimeQualifier whichEncounter;
    @ConfigurationProperty
    private boolean includeObs = false;

    public PatientsInPeriodCohortDefinition() {
        super();
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

    public List<Concept> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Concept> answers) {
        this.answers = answers;
    }

    public Enums.Period getPeriod() {
        return period;
    }

    public void setPeriod(Enums.Period period) {
        this.period = period;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public TimeQualifier getWhichEncounter() {
        return whichEncounter;
    }

    public void setWhichEncounter(TimeQualifier whichEncounter) {
        this.whichEncounter = whichEncounter;
    }

    public boolean isIncludeObs() {
        return includeObs;
    }

    public void setIncludeObs(boolean includeObs) {
        this.includeObs = includeObs;
    }
}
