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
import org.openmrs.module.reporting.common.SetComparator;
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
    public CohortDefinition enrolledInCareAtEndOfPeriod() {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.ART_SUMMARY_PAGE)));
        cd.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        return cd;
    }
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
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, activeClientOnART(), commonCohortLibrary.MoHChildren());
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
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, activeClientOnART(), commonCohortLibrary.MoHAdult());
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
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, activeClientOnART(), commonCohortLibrary.MoHChildren());
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
        ccd.initializeFromQueries(BooleanOperator.AND, cobs, activeClientOnART(), commonCohortLibrary.MoHAdult());
        return ccd;
    }
    
    /**
     * Active client on Third Line
     * @return
     */
    public CohortDefinition clientonThirdLineRegimen() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnART(), commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN), Dictionary.getConceptList(Metadata.Concept.THIRD_LINE_REGIMEN)));
        return ccd;
    }
    
    /**
     * Clients starting ART
     *
     * @return
     *
     */
    public CohortDefinition clientStartingART() {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setName("Has an ART Start date");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.ART_START_DATE));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        return cd;
    }
    
    /**
     * Active clients on ART
     * - has an ART start date in the perriod
     * - has picked up a regimen in the period
     *
     *
     * TODO: Add additional cohort filters for active clients who did not come to the facility during the quarter
     *
     * @return
     *
     */
    public CohortDefinition activeClientOnART() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.OR, clientStartingART(), commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN)));
        return ccd;
    }
    
    /**
     * Patients ever taken ART
     *
     * @return
     */
    public CohortDefinition hasEverTakenARTRegimenAtEndOfPeriod() {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setName("Has taken ART drugs to end of period");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.CURRENT_REGIMEN));
        cd.setOperator(SetComparator.IN);
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        return cd;
    }
    
    /**
     * Active clients on PreARt are clients who are enrolled in care but are not in ART at the end of the quarter
     *
     * TODO: Add additional cohort filters for active preART clients who did not come to the facility during the quarter
     * @return
     */
    public CohortDefinition activeClientOnPreART() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.NOT, enrolledInCareAtEndOfPeriod(), hasEverTakenARTRegimenAtEndOfPeriod());
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
    
    /**
     * Patients who started on INH Prophylaxis
     * @return
     */
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
    
    /**
     * PreART clients who got CPT on the last visit
     * @return
     */
    public CohortDefinition preARTClientsWhoGotCPTOnLastVisit() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnPreART(), clientsWhoGotCPTOnLastVisit());
        return ccd;
    }
    /**
     * PreART clients who got CPT on the last visit
     * @return
     */
    public CohortDefinition ARTClientsWhoGotCPTOnLastVisit() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnART(), clientsWhoGotCPTOnLastVisit());
        return ccd;
    }
    
    /**
     * Cleints who got CPT/Dapsone on last visit
     * @return
     */
    public CohortDefinition clientsWhoGotCPTOnLastVisit() {
        NumericObsCohortDefinition nocd = new NumericObsCohortDefinition();
        nocd.setQuestion(Dictionary.getConcept(Metadata.Concept.CPT_DAPSONE_PILLS_DISPENSED));
        nocd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        nocd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        nocd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.MAX);
        nocd.setOperator1(RangeComparator.GREATER_THAN);
        nocd.setValue1(0.0);
    
        return nocd;
    }
    
    /**
     * PreART clients assessed for TB
     */
    public CohortDefinition preARTClientsAssessedForTBAtLastVisit() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnPreART(), patientAssessedForTBAtLastVisit());
        return ccd;
    }
    /**
     * ART clients assessed for TB
     */
    public CohortDefinition ARTClientsAssessedForTBAtLastVisit() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnART(), patientAssessedForTBAtLastVisit());
        return ccd;
    }
    /**
     * Patients assessed for TB in the last visit in the quarter
     * @return
     */
    public CohortDefinition patientAssessedForTBAtLastVisit() {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setName("Assessed for TB in the last visit in the quarter");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.ASSESSED_FOR_TB));
        cd.setOperator(SetComparator.IN);
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.MAX);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        return cd;
    }
    /**
     * PreART Patients diagnosed with TB
     * @return
     */
    public CohortDefinition preARTClientsDiagnosedWithTBInQuarter() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnPreART(), commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.ASSESSED_FOR_TB), Dictionary.getConcept(Metadata.Concept.DIAGNOSED_WITH_TB)));
        return ccd;
    }
    /**
     * ART Patients diagnosed with TB
     * @return
     */
    public CohortDefinition ARTClientsDiagnosedWithTBInQuarter() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnART(), commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.ASSESSED_FOR_TB), Dictionary.getConcept(Metadata.Concept.DIAGNOSED_WITH_TB)));
        return ccd;
    }
    /**
     * preART Patients started on TB treatment
     * @return
     */
    public CohortDefinition preARTClientsStartedOnTBTreatmentInQuarter() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnPreART(), patientStartedOnTBTreatmentInQuarter());
        return ccd;
    }
    /**
     * ART Patients started on TB treatment
     * @return
     */
    public CohortDefinition ARTClientsStartedOnTBTreatmentInQuarter() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnART(), patientStartedOnTBTreatmentInQuarter());
        return ccd;
    }
    
    /**
     * Patients started on TB treatment
     * @return
     */
    public CohortDefinition patientStartedOnTBTreatmentInQuarter() {
        DateObsCohortDefinition cd = new DateObsCohortDefinition();
        cd.setName("Started TB Treatment");
        cd.setQuestion(Dictionary.getConcept(Metadata.Concept.TB_TREATMENT_START_DATE));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.addParameter(new Parameter("onOrBefore", "Before Date", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "After Date", Date.class));
        
        return cd;
    }
    
    /**
     * PreART clients assessed for Malnutrition
     * @return
     */
    public CohortDefinition preARTClientsAssessedForMalnutrition() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnPreART(), patientAssessedForMalnutrition());
        return ccd;
    }
    /**
     * ART clients assessed for Malnutrition
     * @return
     */
    public CohortDefinition ARTClientsAssessedForMalnutrition() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, clientStartingART(), patientAssessedForMalnutrition());
        return ccd;
    }
    /**
     * Clients assessed for malnutrition in the quarter
     * @return
     */
    public CohortDefinition patientAssessedForMalnutrition() {
        return commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.ASSESSED_FOR_MALNUTRITION));
    }
    
    /**
     * Patients receiving TB Treatment
     * @return
     */
    public CohortDefinition patientReceivingTBTreatment() {
        return commonCohortLibrary.hasCodedObs(Dictionary.getConcept(Metadata.Concept.ASSESSED_FOR_TB), Dictionary.getConcept(Metadata.Concept.ON_TB_TREATMENT));
    }
    
    /**
     * Patients on TB treatment - patients who have started TB treatment and those who re receiving TB treatment
     * @return
     */
    public CohortDefinition patientsOnTBTreatment() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.OR, patientStartedOnTBTreatmentInQuarter(), patientReceivingTBTreatment());
        
        return ccd;
    }
    
    /**
     * Patients on ART who have either started TB treatment or are receiving TB treatment
     * @return
     */
    
    public CohortDefinition ARTClientsOnTBTreatmentInQuarter() {
        CompositionCohortDefinition ccd = new CompositionCohortDefinition();
        ccd.initializeFromQueries(BooleanOperator.AND, activeClientOnART(), patientsOnTBTreatment());
        return ccd;
    }
}
