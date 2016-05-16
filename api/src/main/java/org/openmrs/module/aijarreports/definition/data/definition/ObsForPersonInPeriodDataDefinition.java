package org.openmrs.module.aijarreports.definition.data.definition;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Obs;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
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
public class ObsForPersonInPeriodDataDefinition extends BaseDataDefinition implements PersonDataDefinition {

    @ConfigurationProperty
    private Period period;
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
    private Date valueDatetime;
    @ConfigurationProperty
    private Double valueNumeric;
    @ConfigurationProperty
    private RangeComparator rangeComparator;


    public ObsForPersonInPeriodDataDefinition() {
        super();
    }

    public ObsForPersonInPeriodDataDefinition(String name) {
        super(name);
    }

    public ObsForPersonInPeriodDataDefinition(String name, TimeQualifier whichEncounter, Concept question, Date startDate) {
        this(name);
        this.whichEncounter = whichEncounter;
        this.question = question;
        this.startDate = startDate;
    }

    @Override
    public Class<?> getDataType() {
        if (whichEncounter == TimeQualifier.LAST || whichEncounter == TimeQualifier.FIRST) {
            return Obs.class;
        }
        return List.class;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period period) {
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

    public Double getValueNumeric() {
        return valueNumeric;
    }

    public void setValueNumeric(Double valueNumeric) {
        this.valueNumeric = valueNumeric;
    }

    public List<Concept> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Concept> answers) {
        this.answers = answers;
    }

    public Date getValueDatetime() {
        return valueDatetime;
    }

    public void setValueDatetime(Date valueDatetime) {
        this.valueDatetime = valueDatetime;
    }

    public RangeComparator getRangeComparator() {
        return rangeComparator;
    }

    public void setRangeComparator(RangeComparator rangeComparator) {
        this.rangeComparator = rangeComparator;
    }
}
