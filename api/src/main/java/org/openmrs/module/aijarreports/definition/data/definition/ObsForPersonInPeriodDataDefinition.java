package org.openmrs.module.aijarreports.definition.data.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

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
    private Period obsPeriod;
    @ConfigurationProperty
    private Period encounterPeriod;
    @ConfigurationProperty
    private Date onDate;
    @ConfigurationProperty
    private List<EncounterType> encounterTypes;
    @ConfigurationProperty
    private Concept question;
    @ConfigurationProperty
    private TimeQualifier whichEncounter;
    @ConfigurationProperty
    private TimeQualifier whichObs;
    @ConfigurationProperty
    private List<Concept> answers;
    @ConfigurationProperty
    private int periodToAdd = 0;
    @ConfigurationProperty
    private boolean encountersInclusive = false;
    @ConfigurationProperty
    private boolean valueDatetime = false;
    @ConfigurationProperty
    private String whichReport;


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

    public Period getObsPeriod() {
        return obsPeriod;
    }

    public void setObsPeriod(Period obsPeriod) {
        this.obsPeriod = obsPeriod;
    }

    public Period getEncounterPeriod() {
        return encounterPeriod;
    }

    public void setEncounterPeriod(Period encounterPeriod) {
        this.encounterPeriod = encounterPeriod;
    }

    public Date getOnDate() {
        return onDate;
    }

    public void setOnDate(Date onDate) {
        this.onDate = onDate;
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

    public TimeQualifier getWhichObs() {
        return whichObs;
    }

    public void setWhichObs(TimeQualifier whichObs) {
        this.whichObs = whichObs;
    }

    public boolean isEncountersInclusive() {
        return encountersInclusive;
    }

    public void setEncountersInclusive(boolean encountersInclusive) {
        this.encountersInclusive = encountersInclusive;
    }

    public boolean isValueDatetime() {
        return valueDatetime;
    }

    public void setValueDatetime(boolean valueDatetime) {
        this.valueDatetime = valueDatetime;
    }

    public String getWhichReport() {
        return whichReport;
    }

    public void setWhichReport(String whichReport) {
        this.whichReport = whichReport;
    }
}
