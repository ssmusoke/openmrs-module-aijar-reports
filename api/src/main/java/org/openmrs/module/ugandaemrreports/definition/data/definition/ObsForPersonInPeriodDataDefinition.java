package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;
import org.openmrs.module.ugandaemrreports.common.Enums;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 13/05/2016.
 */
@Caching(
        strategy = ConfigurationPropertyCachingStrategy.class
)
public class ObsForPersonInPeriodDataDefinition extends BaseDataDefinition implements PatientDataDefinition {

    @ConfigurationProperty
    private Enums.Period period;
    @ConfigurationProperty
    private Date startDate;
    @ConfigurationProperty
    private List<EncounterType> encounterTypes;
    @ConfigurationProperty
    private Concept question;
    @ConfigurationProperty
    private TimeQualifier whichEncounter;
    @ConfigurationProperty
    private List<Concept> answers;
    @ConfigurationProperty
    private int periodToAdd = 0;


    public ObsForPersonInPeriodDataDefinition() {
        super();
    }

    public ObsForPersonInPeriodDataDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return Obs.class;
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

    public TimeQualifier getWhichEncounter() {
        return whichEncounter;
    }

    public void setWhichEncounter(TimeQualifier whichEncounter) {
        this.whichEncounter = whichEncounter;
    }

    public List<Concept> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Concept> answers) {
        this.answers = answers;
    }

    public int getPeriodToAdd() {
        return periodToAdd;
    }

    public void setPeriodToAdd(int periodToAdd) {
        this.periodToAdd = periodToAdd;
    }
}
