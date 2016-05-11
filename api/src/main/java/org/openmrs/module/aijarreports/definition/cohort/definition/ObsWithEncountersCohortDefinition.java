package org.openmrs.module.aijarreports.definition.cohort.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 20/04/2016.
 */
public class ObsWithEncountersCohortDefinition extends BaseCohortDefinition {
    private static final long serialVersionUID = 1L;
    @ConfigurationProperty
    private TimeQualifier whichEncounter;
    /*@ConfigurationProperty
    private Integer startYear;
    @ConfigurationProperty
    private Integer startMonth;
    @ConfigurationProperty
    private Integer monthsBefore;*/
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private Date endDate;
    @ConfigurationProperty
    private List<EncounterType> encounterTypes;
    @ConfigurationProperty
    private Concept question;
    @ConfigurationProperty
    private List<Concept> answers;

    public ObsWithEncountersCohortDefinition() {
        super();
    }

    /*public Integer getStartYear() {
        return startYear;
    }

    public void setStartYear(Integer startYear) {
        this.startYear = startYear;
    }

    public Integer getStartMonth() {
        return startMonth;
    }

    public void setStartMonth(Integer startMonth) {
        this.startMonth = startMonth;
    }

    public Integer getMonthsBefore() {
        return monthsBefore;
    }

    public void setMonthsBefore(Integer monthsBefore) {
        this.monthsBefore = monthsBefore;
    }*/

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

    public TimeQualifier getWhichEncounter() {
        return whichEncounter;
    }

    public void setWhichEncounter(TimeQualifier whichEncounter) {
        this.whichEncounter = whichEncounter;
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
}
