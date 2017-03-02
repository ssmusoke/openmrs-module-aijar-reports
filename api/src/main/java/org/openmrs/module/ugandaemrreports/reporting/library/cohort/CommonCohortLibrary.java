package org.openmrs.module.ugandaemrreports.reporting.library.cohort;

import org.openmrs.EncounterType;
import org.openmrs.Program;
import org.openmrs.module.reporting.cohort.definition.*;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.openmrs.module.ugandaemrreports.reporting.calculation.IsPregnantCalculation;
import org.openmrs.module.ugandaemrreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

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
     * Patients who are pregnant on ${onDate}
     *
     * @return the cohort definition
     */
    public CohortDefinition pregnant() {
        CalculationCohortDefinition cd = new CalculationCohortDefinition(new IsPregnantCalculation());
        cd.setName("pregnant on date");
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

}

