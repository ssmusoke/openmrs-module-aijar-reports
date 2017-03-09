package org.openmrs.module.ugandaemrreports.reporting.library.cohort;

import java.util.Arrays;
import java.util.Date;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.common.BooleanOperator;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.CoreUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * ART Library
 */
@Component
public class ARTCohortLibrary {
    @Autowired
    CommonCohortLibrary commonCohortLibrary;
    /**
     * Patients who on in Care
     *
     * @return the cohort definition
     */
    public CohortDefinition enrolledInCare() {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_SUMMARY_PAGE)));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return cd;
    }
    
    /**
     * Patients who were enrolled into care for the period less those who transferred in
     */
    public CohortDefinition enrolledInCareForPeriodWithoutTransferIn() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.NOT, enrolledInCare(), commonCohortLibrary.transferredIn());
        
        return ccd;
    }
    
    /**
     * Pregnant and lactating women enrolled into care for the period
     * @return
     */
    public CohortDefinition pregnantAndLactatingWomenEnrolledIntoCare() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, enrolledInCareForPeriodWithoutTransferIn(), pregnantAtHIVEnrollment(), commonCohortLibrary.females());
        return ccd;
    }
    
    /**
     * Enrolled through EMTCT
     * @return
     */
    public CohortDefinition pregnantAtHIVEnrollment() {
        return commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CARE_ENTRY_POINT), Dictionary.getConcept(Metadata.Concept.CARE_ENTRY_POINT_EMTCT));
    }
    
    /**
     * Active clients are a combination of any of the following:
     *
     * <ul>
     *     <li>Have an ART encounter in the period</li>
     *     <li>Have missed their last appointment, which is less than 90 days from the start of the period</li>
     * </ul>
     * @return
     */
    public CohortDefinition activeClients(){
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        //ccd.initializeFromQueries(BooleanOperator.OR, clientWithARTEncounter());
        
        return ccd;
    }
    
    /**
     * Clients on first line regimen - combine the cohorts for adults and children since each has different first line regimens
     * @return
     */
    public CohortDefinition clientOnFirstLineRegimen() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.OR, childrenOnFirstLineRegimen(), adultsOnFirstLineRegimen());
    
        return ccd;
    }
    
    /**
     * Children who are on first line regimen
     * @return
     */
    public CohortDefinition childrenOnFirstLineRegimen() {
        CodedObsCohortDefinition cobs = (CodedObsCohortDefinition) commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN), Dictionary.getConceptList(Metadata.Concept.CHILDREN_FIRST_LINE_REGIMEN));
        cobs.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_ENCOUNTER_PAGE)));
    
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, commonCohortLibrary.MoHChildren());
        return ccd;
    }
    /**
     * Adults who are on first line regimen
     * @return
     */
    public CohortDefinition adultsOnFirstLineRegimen() {
        CodedObsCohortDefinition cobs = (CodedObsCohortDefinition) commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN), Dictionary.getConceptList(Metadata.Concept.ADULT_FIRST_LINE_REGIMEN));
        cobs.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_ENCOUNTER_PAGE)));
        
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, commonCohortLibrary.MoHAdult());
        return ccd;
    }
    /**
     * Clients on second line regimen - combine the cohorts for adults and children since each has different first line regimens
     * @return
     */
    public CohortDefinition clientOnSecondLineRegimen() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.OR, childrenOnSecondLineRegimen(), adultsOnSecondLineRegimen());
    
        return ccd;
    }
    
    /**
     * Children who are on second line regimen
     * @return
     */
    public CohortDefinition childrenOnSecondLineRegimen() {
        CodedObsCohortDefinition cobs = (CodedObsCohortDefinition) commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN), Dictionary.getConceptList(Metadata.Concept.CHILDREN_SECOND_LINE_REGIMEN));
        cobs.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_ENCOUNTER_PAGE)));
        
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, commonCohortLibrary.MoHChildren());
        return ccd;
    }
    /**
     * Adults who are on second line regimen
     * @return
     */
    public CohortDefinition adultsOnSecondLineRegimen() {
        CodedObsCohortDefinition cobs = (CodedObsCohortDefinition) commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN), Dictionary.getConceptList(Metadata.Concept.ADULT_SECOND_LINE_REGIMEN));
        cobs.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_ENCOUNTER_PAGE)));
        
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, commonCohortLibrary.MoHAdult());
        return ccd;
    }
    
    public CohortDefinition clientonThirdLineRegimen() {
        return commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN), Dictionary.getConceptList(Metadata.Concept.THIRD_LINE_REGIMEN));
    }
    
    /**
     * Clients on ART
     *
     * @return
     *
     */
    public CohortDefinition clientOnART() {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setName("Has an ART Start date");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.ART_START_DATE));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        return cd;
    }
    /**
     * Clients on ART at the end of the period
     *
     * @return
     *
     */
    public CohortDefinition clientOnARTAtPeriodEnd() {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setName("Has an ART Start date");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.ART_START_DATE));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        return cd;
    }
    
    /**
     * Active clients on PreARt are clients who are enrolled in care but are not in ART at the end of the quarter
     * @return
     */
    public CohortDefinition activeClientOnPreART() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.NOT, enrolledInCare(), clientOnARTAtPeriodEnd());
        return ccd;
    }
    
    /**
     * Patients who are either pregnant or lactating at ART start
     * @return
     */
    public CohortDefinition pregnantOrLactatingAtARTStart() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.OR, pregnantAtARTStart(), lactatingAtARTStart());
        return ccd;
    }
    
    /**
     * Patients who are pregnant at the time of starting ART
     * @return
     */
    public CohortDefinition pregnantAtARTStart() {
        return commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.PREGNANT_AT_ART_START), Dictionary.getConcept(Metadata.Concept.YES_WHO));
    }
    
    /**
     * Patients who are lactating at the time of starting ART
     * @return
     */
    public CohortDefinition lactatingAtARTStart() {
        return commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.LACTATING_AT_ART_START), Dictionary.getConcept(Metadata.Concept.YES_WHO));
    }
    
    public CohortDefinition startedINHProphylaxis() {
        NumericObsCohortDefinition nocd = new NumericObsCohortDefinition();
        nocd.setQuestion(Dictionary.getConcept(Metadata.Concept.INH_DOSAGE));
        nocd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        nocd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        nocd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        nocd.setOperator1(RangeComparator.GREATER_THAN);
        nocd.setValue1(0.0);
        
        return nocd;
    }
    
    
}
