package org.openmrs.module.ugandaemrreports.reporting.library.cohort;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.Program;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.stereotype.Component;

/**
 * Library of common cohort definitions
 */
@Component
public class CommonCohortLibrary {

    /**
     * Patients who are female
     *
     * @return the cohort definition
     */
    public CohortDefinition females() {
        GenderCohortDefinition cd = new GenderCohortDefinition();
        cd.setName("females");
        cd.setFemaleIncluded(true);
        return cd;
    }

    /**
     * Patients who are male
     *
     * @return the cohort definition
     */
    public CohortDefinition males() {
        GenderCohortDefinition cd = new GenderCohortDefinition();
        cd.setName("males");
        cd.setMaleIncluded(true);
        return cd;
    }

    /**
     * Patients who at most maxAge years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition agedAtMost(int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged at most " + maxAge);
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMaxAge(maxAge);
        return cd;
    }

    /**
     * Patients who are at least minAge years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition agedAtLeast(int minAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged at least " + minAge);
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        return cd;
    }

    /**
     * patients who are at least minAge years old and at most years old on ${effectiveDate}
     *
     * @return CohortDefinition
     */
    public CohortDefinition agedAtLeastAgedAtMost(int minAge, int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged between " + minAge + " and " + maxAge + " years");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        cd.setMaxAge(maxAge);
        return cd;
    }

    /**
     * Patients who are female and at least 18 years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition femalesAgedAtLeast18() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("females aged at least 18");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.addSearch("females", ReportUtils.map(females()));
        cd.addSearch("agedAtLeast18", ReportUtils.map(agedAtLeast(18), "effectiveDate=${effectiveDate}"));
        cd.setCompositionString("females AND agedAtLeast18");
        return cd;
    }

    /**
     * Patients who have an encounter between ${onOrAfter} and ${onOrBefore}
     *
     * @param types the encounter types
     * @return the cohort definition
     */
    public CohortDefinition hasEncounter(EncounterType... types) {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setName("has encounter between dates");
        cd.setTimeQualifier(TimeQualifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        if (types.length > 0) {
            cd.setEncounterTypeList(Arrays.asList(types));
        }
        return cd;
    }


    /**
     * Patients who were enrolled on the given programs between ${enrolledOnOrAfter} and ${enrolledOnOrBefore}
     *
     * @param programs the programs
     * @return the cohort definition
     */
    public CohortDefinition enrolled(Program... programs) {
        ProgramEnrollmentCohortDefinition cd = new ProgramEnrollmentCohortDefinition();
        cd.setName("enrolled in program between dates");
        cd.addParameter(new Parameter("enrolledOnOrAfter", "After Date", Date.class));
        cd.addParameter(new Parameter("enrolledOnOrBefore", "Before Date", Date.class));
        if (programs.length > 0) {
            cd.setPrograms(Arrays.asList(programs));
        }
        return cd;
    }
    
    /**
     * Patients who have an obs between ${onOrAfter} and ${onOrBefore}
     * @param question the question concept
     * @param answers the answers to include
     * @return the cohort definition
     */
    public CohortDefinition hasCodedObs(List<EncounterType> encounterTypeList, Concept question, List<Concept> answers) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setName("has obs between dates");
        cd.setQuestion(question);
        cd.setOperator(SetComparator.IN);
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        if (answers.size() > 0) {
            cd.setValueList(answers);
        }
        return cd;
    }
    
    /**
     * Convenience method to
     * @param question
     * @param answers
     * @return
     */
    public CohortDefinition hasCodedObs(List<EncounterType> encounterTypeList, Concept question, Concept ... answers) {
        return hasCodedObs(encounterTypeList, question, Arrays.asList(answers));
    }
    
    /**
     * Patients who transferred in between ${onOrAfter} and ${onOrBefore}
     *
     * @return the cohort definition
     */
    public CohortDefinition transferredIn() {
        return hasCodedObs(null,Dictionary.getConcept(Metadata.Concept.TRANSFER_IN), Dictionary.getConcepts(Metadata.Concept.YES_WHO));
    }
    
    /**
     * MoH definition of children who is anybody 14 years and below
     * @return
     */
    public CohortDefinition MoHChildren(){
        return agedAtMost(14);
    }
    
    /**
     * MoH definition of adults who are 15 years and older
     * @return
     */
    public CohortDefinition MoHAdult(){
        return agedAtLeast(15);
    }

    /**
     * Patients who are at least minAge Days old and at most maxAge days old on ${effectiveDate}
     *
     * @return the cohort definition
     */
	public CohortDefinition agedAtLeastDaysAgedAtMostDays(int minAge, int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged between " + minAge + " and " + maxAge + " days");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        cd.setMinAgeUnit(DurationUnit.DAYS);
        cd.setMaxAge(maxAge);
        cd.setMaxAgeUnit(DurationUnit.DAYS);
        return cd;
	}

    /**
     * Patients who are at least minAge Days old and at most maxAge years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
	public CohortDefinition agedAtLeastDaysAgedAtMostYears(int minAge, int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged between " + minAge + " days and " + maxAge + " years");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        cd.setMinAgeUnit(DurationUnit.DAYS);
        cd.setMaxAge(maxAge);
        cd.setMaxAgeUnit(DurationUnit.YEARS);
        return cd;
	}

    /**
     * Patients who are at least minAge Months old and at most maxAge years old on ${effectiveDate}
     *
     * @return the cohort definition
     */
	public CohortDefinition agedAtLeastMonthsAgedAtMostYears(int minAge, int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged between " + minAge + " months and " + maxAge + " years");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(minAge);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        cd.setMaxAge(maxAge);
        cd.setMaxAgeUnit(DurationUnit.YEARS);
        return cd;
	}    
}

