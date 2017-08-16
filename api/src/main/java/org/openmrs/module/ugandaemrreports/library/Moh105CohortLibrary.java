/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.library;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.CoreUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Nicholas Ingosi on 5/23/17.
 */
@Component
public class Moh105CohortLibrary {

    @Autowired
    CommonCohortDefinitionLibrary definitionLibrary;
    
    @Autowired
    HIVMetadata hivMetadata;
    
    @Autowired
    private DataFactory df;

    public CohortDefinition femaleAndHasAncVisit(double lower, double upper){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Female and has ANC Visit");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("female", ReportUtils.map(definitionLibrary.females(), ""));
        cd.addSearch("ancVist", ReportUtils.map(totalAncVisits(lower, upper), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("female AND ancVist AND ancEncounter");
        return cd;
    }

    /**
     * Total ANC visits - including new clients and re-attendances
     * @return CohortDefinition
     */
    public CohortDefinition totalAncVisits(double lower, double upper) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setName("Anc visit between "+lower+" and "+upper);
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setQuestion(Dictionary.getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.setOperator1(RangeComparator.GREATER_THAN);
        cd.setValue1(lower);
        cd.setOperator2(RangeComparator.LESS_EQUAL);
        cd.setValue2(upper);
        cd.setEncounterTypeList(Arrays.asList(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)));
        return cd;
    }

    /**
     * Pregnant women receiving iron/folic acid on ANC 1st Visit
     * @return CohortDefinition
     */
    public CohortDefinition pregnantAndReceivingIronOrFolicAcidAnc1stVisit(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Pregnant women receiving iron/folic acid on ANC 1st Visit");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("femaleAndHasAncVisit", ReportUtils.map(femaleAndHasAncVisit(0.0, 1.0), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("takingIron", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("315825e8-8ba4-4551-bdd1-aa4e02a36639"), Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("takingFolic", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("8c346216-c444-4528-a174-5139922218ed"), Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("(ancEncounter AND femaleAndHasAncVisit) AND (takingIron OR takingFolic)");
        return cd;
    }
    
    /**
     * HIV Positive before first ANC
     * @return
     */
    public CohortDefinition hivPostiveBeforeFirstANCVisit() {
        return definitionLibrary.hasANCObs(Dictionary.getConcept(org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata.Concept.HIV_STATUS), Dictionary.getConcept(org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata.Concept.HIV_POSITIVE));
    }

    /**
     * Assessed by
     * @return CohortDefinition
     */
    public CohortDefinition assessedByNumericValues(Concept question) {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setName("Numeric obs based on question");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setQuestion(question);
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        return cd;
    }

    /**
     * HIV+ pregnant women assessed by CD4 or WHO clinical stage for the 1st time
     * @return CohortDefinition
     */
    public CohortDefinition hivPositiveAndAccessedWithCd4WhoStage(Concept question) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ assed by "+question.getName().getName());
        cd.addSearch("assessedBy", ReportUtils.map(assessedByNumericValues(question), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("assessedBy AND ancEncounter");
        return cd;
    }

    /**
     * Total HIV+ mothers attending postnatal
     * Those who are hiv postive
     * Counselled tested and results given - Client tested HIV+ in PNC,
     *Client tested HIV+ on a re-test
     * Client tested on previous visit with known HIV+ status
     */
    public CohortDefinition hivPositiveMothersInAnc() {
        Concept emtctQ = Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74");
        Concept trr = Dictionary.getConcept("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a");
        Concept trrPlus = Dictionary.getConcept("60155e4d-1d49-4e97-9689-758315967e0f");
        Concept trrTick = Dictionary.getConcept("1f177240-85f6-4f10-964a-cfc7722408b3");

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("emctCodes", ReportUtils.map(definitionLibrary.hasObs(emtctQ, trr, trrPlus, trrTick), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.PNC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("emctCodes AND ancEncounter");
        return cd;
    }

    /**ancEncounter
     * Mother-baby pairs enrolled at Mother-Baby care point
     * @return CohortDefinition
     */
    public CohortDefinition motherBabyEnrolled() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("hasEncounter", ReportUtils.map(definitionLibrary.hasEncounter(Context.getEncounterService().getEncounterTypeByUuid("fa6f3ff5-b784-43fb-ab35-a08ab7dbf074")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("babyAl", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("dd8a2ad9-16f6-44db-82d7-87d6eef14886"), Dictionary.getConcept("9d9e6b5a-8b5d-4b8c-8ab7-9fdabb279493")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hasEncounter AND babyAl");
        return cd;
    }

    
    public CohortDefinition missedANCAppointment() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("hasAppointment", ReportUtils.map(
                df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getReturnVisitDate(), Arrays.asList(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)), BaseObsCohortDefinition.TimeModifier.ANY), "startDate=${onOrAfter},endDate=${onOrBefore}"));
        
        cd.addSearch("hasVisit", ReportUtils.map(femaleAndHasAncVisit(0.0, 10.0), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
    
        cd.setCompositionString("hasAppointment NOT hasVisit");
        return cd;
    }

    /**
     * pregnantWomenNewlyTestedForHivThisPregnancyTRR at any visit
     * @return CohortDefinition
     */
    public CohortDefinition pregnantWomenNewlyTestedForHivThisPregnancyTRRAnyVisit(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("trr", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"), Dictionary.getConcept("25c448ff-5fe4-4a3a-8c0a-b5aaea9d5465")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.addSearch("anyVisit", ReportUtils.map(femaleAndHasAncVisit(0.0, 10.0), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("trr AND anyVisit AND ancEncounter");
        return cd;
    }

    /**
     * combine the has obs cohort definiton with the encounter provided
     * @return CohortDefinition
     */
    public CohortDefinition hasObsAndEncounter(String encounterType, Concept q, Concept ... a){
    	return hasObsAndEncounter(encounterType, q, Arrays.asList(a));
    }

	public CohortDefinition hasObsAndEncounter(String encounterType, Concept q, List<Concept> a) {
    	CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("hasEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, encounterType)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("hasObs", ReportUtils.map(definitionLibrary.hasObs(q, a), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hasEncounter AND hasObs");
        return cd;
	}    
    
    
    /**
     * Mothers admitted to Maternity Clinic
     *
     * @return the cohort definition
     */
    public CohortDefinition maternityAdmissions() {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.MATERNITY_ENCOUNTER)));
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        return cd;
    }    

	/**
	 * HIV Positive Persons
	 * @return CohortDefinition 
	 */
    public CohortDefinition hivPositivePersons() {
    	return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.HIV_STATUS), Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE));
    }    
    
    /**
     * HIV+ women
     * @return CohortDefinition
     */
    public CohortDefinition hivPositiveWomen() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ Women");
        cd.addSearch("female", ReportUtils.map(definitionLibrary.females(), ""));
        cd.addSearch("hivPositive", ReportUtils.map(hivPositivePersons(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("female AND hivPositive");
        return cd;
    }

    /**
     * Deliveries in unit
     * @return CohortDefinition
     */
    public CohortDefinition deliveriesInUnit() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.PREGNANCY_OUTCOME));
    }    
    
    /**
     * HIV+ women initiating ARV in maternity
     * @return CohortDefinition
     */
    public CohortDefinition hivPositiveWomenInitiatingArvInMaternity() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ Initiating Maternity");        
        cd.addSearch("maternityAdmissions", ReportUtils.map(maternityAdmissions(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("hivPositive", ReportUtils.map(hivPositiveWomen(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("InitiatingARV", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("35ae2043-a3b0-48de-8e22-05f377ac39a2")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hivPositive AND InitiatingARV AND maternityAdmissions");
        return cd;
    }
    
    /**
     * Deliveries to HIV+ women
     * @return CohortDefinition
     */
    public CohortDefinition deliveriesToHIVPositiveWomen() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ Women Deliveries");
        cd.addSearch("hivPositive", ReportUtils.map(hivPositiveWomen(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("deliveries", ReportUtils.map(deliveriesInUnit(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hivPositive AND deliveries");
        return cd;
    }

    /**
     * Live Births 
     * @return Cohort Definition
     */
	public CohortDefinition liveBirths() {
		return definitionLibrary.hasObs(Dictionary.getConcept("a5638850-0cb4-4ce8-8e87-96fc073de25d"),Dictionary.getConceptList("eb7041a0-02e6-4e9a-9b96-ff65dd09a416,23ac7575-f0ea-49a5-855e-b3348ad1da01"));
	}    
    
    /**
     * Live Birth Deliveries to HIV+ women
     * @return CohortDefinition
     */
    public CohortDefinition liveBirthDeliveriesToHIVPositiveWomen() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ Women Deliveries");
        cd.addSearch("hivPositive", ReportUtils.map(hivPositiveWomen(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("deliveries", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept(org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata.Concept.PREGNANCY_OUTCOME)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("liveBirths", ReportUtils.map(liveBirths(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hivPositive AND deliveries AND liveBirths");
        return cd;
    }

    /**
     * No. of mothers who initiated breastfeeding within the 1st hour after delivery - Total
     * @return CohortDefinition
     */
    public CohortDefinition initiatedBreastfeedingWithinFirstHourAfterDelivery() {
        return definitionLibrary.hasObs(Dictionary.getConcept("a4063d62-a936-4a26-9c1c-a0fb279a71b1"),Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
    }

    /**
     * No. of mothers who initiated breastfeeding within the 1st hour after delivery - HIV+
     * @return CohortDefinition
     */
    public CohortDefinition initiatedBreastfeedingWithinFirstHourAfterDeliveryAndHIVPositive() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Initiated breastfeeding within 1 hour after delivery and HIV+");
        cd.addSearch("hivPositive", ReportUtils.map(hivPositiveWomen(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("initiatedBreastFeedingWithin1HourDelivery", ReportUtils.map(initiatedBreastfeedingWithinFirstHourAfterDelivery(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("hivPositive AND initiatedBreastFeedingWithin1HourDelivery");
        return cd;
    }

    /**
     * Babies born with low birth weight
     * @return CohortDefinition
     */
	public CohortDefinition babiesBornWithLowBirthWeight() {
        NumericObsCohortDefinition cd = new NumericObsCohortDefinition();
        cd.setName("Babies born with low birth weight");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setQuestion(Dictionary.getConcept("dcce847a-30ab-102d-86b0-7a5022ba4115"));
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        cd.setOperator1(RangeComparator.LESS_THAN);
        cd.setValue1(2.5);
        return cd;
	}
        
    /**
     * Maternal Deaths
     * @return CohortDefinition
     */
    public CohortDefinition maternalDeaths() {
        return definitionLibrary.hasObs(Dictionary.getConcept("e87431db-b49e-4ab6-93ee-a3bd6c616a94"),Dictionary.getConcept("17fcfd67-a1a2-4361-9915-ad4e81a7a61d"));
    }
        
	/**
	 * Number of HIV+ infants from EID Enrolled in care
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition hivPositiveInfantsFromEidEnrolledInCare() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
		cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
		cd.setName("Number of HIV+ infants from EID enrolled in care");
		cd.addSearch("hivPositiveInfantsFromEid",
		    ReportUtils.map(hivPositiveInfantsFromEid(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		cd.addSearch("infantsFromEidEnrolledInCare",
		    ReportUtils.map(infantsFromEidEnrolledInCare(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		cd.setCompositionString("hivPositiveInfantsFromEid AND infantsFromEidEnrolledInCare");
		return cd;
	}
	
	/**
	 * Number of infants from EID Enrolled in care
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition infantsFromEidEnrolledInCare() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.ENROLLED_IN_HIV_CARE_PROGRAM),
		    Dictionary.getConcept(Metadata.Concept.YES_CIEL));
	}
	
	/**
	 * Number of HIV+ infants from EID
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition hivPositiveInfantsFromEid() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.FINAL_EID_PCR_TEST_RESULT),
		    Dictionary.getConcept(Metadata.Concept.POSITIVE));
	}
	
	/**
	 * Number of DNA PCR results returned from the lab
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition dnaPcrResultsReturnedFromTheLab() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
		cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
		cd.setName("Number of DNA PCR results returned from the lab");
		cd.addSearch("firstDnaPCRResultsReturnedFromTheLab",
		    ReportUtils.map(firstDnaPCRResultsReturnedFromTheLab(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		cd.addSearch("secondDnaPCRResultsReturnedFromTheLab",
		    ReportUtils.map(secondDnaPCRResultsReturnedFromTheLab(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		cd.setCompositionString("firstDnaPCRResultsReturnedFromTheLab AND secondDnaPCRResultsReturnedFromTheLab");
		return cd;
	}
	
	/**
	 * Number of DNA PCR results returned from the lab - given to care giver
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition dnaPcrResultsReturnedFromTheLabGivenToCareGiver() {
		CompositionCohortDefinition cd = new CompositionCohortDefinition();
		cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
		cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
		cd.setName("Number of DNA PCR results returned from the lab - given to care giver");
		cd.addSearch("firstDnaPCRResultsReturnedFromTheLabGivenToCareGiver", ReportUtils.map(
		    firstDnaPCRResultsReturnedFromTheLabGivenToCareGiver(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		cd.addSearch("secondDnaPCRResultsReturnedFromTheLabGivenToCareGiver", ReportUtils.map(
		    secondDnaPCRResultsReturnedFromTheLabGivenToCareGiver(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
		cd.setCompositionString(
		    "firstDnaPCRResultsReturnedFromTheLabGivenToCareGiver AND secondDnaPCRResultsReturnedFromTheLabGivenToCareGiver");
		return cd;
	}
	
	/**
	 * Number of 1st DNA PCR results returned from the lab given to care giver
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition firstDnaPCRResultsReturnedFromTheLabGivenToCareGiver() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.DATE_FIRST_EID_PCR_TEST_RESULT_GIVEN_TO_CARE_PROVIDER));
	}
	
	/**
	 * Number of 2nd DNA PCR results returned from the lab given to care giver
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition secondDnaPCRResultsReturnedFromTheLabGivenToCareGiver() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.DATE_SECOND_EID_PCR_TEST_RESULT_GIVEN_TO_CARE_PROVIDER));
	}
	
	/**
	 * Number of 1st DNA PCR results returned from the lab
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition firstDnaPCRResultsReturnedFromTheLab() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.FIRST_EID_PCR_TEST_RESULT));
	}
	
	/**
	 * Number of 2nd DNA PCR results returned from the lab
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition secondDnaPCRResultsReturnedFromTheLab() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.SECOND_EID_PCR_TEST_RESULT));
	}
	
	/**
	 * Number of HIV+ 1st DNA PCR results returned from the lab
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition firstDnaPCRResultsReturnedFromTheLabHivPositive() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.FIRST_EID_PCR_TEST_RESULT),
		    Dictionary.getConcept(Metadata.Concept.POSITIVE));
	}
	
	/**
	 * Number of HIV+ 2nd DNA PCR results returned from the lab
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition secondDnaPCRResultsReturnedFromTheLabHivPositive() {
		return hasObsAndEncounter(Metadata.EncounterType.EID_ENCOUNTER_PAGE,
		    Dictionary.getConcept(Metadata.Concept.SECOND_EID_PCR_TEST_RESULT),
		    Dictionary.getConcept(Metadata.Concept.POSITIVE));
	}

	public CohortDefinition referralsToMaternityUnit() {
    	CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("maternityAdmissions", ReportUtils.map(maternityAdmissions(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("hasObs", ReportUtils.map(definitionLibrary.hasTextObs(Dictionary.getConcept(Metadata.Concept.REFERRAL_NUMBER), "REF"), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("maternityAdmissions AND hasObs");
        return cd;

	}

	public CohortDefinition maternityReferralsOut() {
    	CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("maternityAdmissions", ReportUtils.map(maternityAdmissions(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("hasObs", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("e87431db-b49e-4ab6-93ee-a3bd6c616a94"), Dictionary.getConcept("6e4f1db1-1534-43ca-b2a8-5c01bc62e7ef")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("maternityAdmissions AND hasObs");
        return cd;

	}

  /**
	 * Number of Tetanus Immunizations done
	 * 
	 * @return CohortIndicator
	 */
	public CohortDefinition tetanusImmunizationsDone(int doseNumber, Boolean pregnant ) {
		Concept doseNumberConcept = null;
		switch (doseNumber) {
			case 1:
				doseNumberConcept = Dictionary.getConcept(Metadata.Concept.FIRST_DOSE);
				break;
			case 2:
				doseNumberConcept = Dictionary.getConcept(Metadata.Concept.SECOND_DOSE);
				break;
			case 3:
				doseNumberConcept = Dictionary.getConcept(Metadata.Concept.THIRD_DOSE);
				break;
			case 4:
				doseNumberConcept = Dictionary.getConcept(Metadata.Concept.FOURTH_DOSE);
				break;
			case 5:
				doseNumberConcept = Dictionary.getConcept(Metadata.Concept.FIFTH_DOSE);
				break;
			
			default:
				break;
		}
		if (pregnant) {
			//Fetch a cohort of pregnant persons given the tetanus dose i.e those with ANC encounters
			CohortDefinition cd = null;
			if (doseNumberConcept != null) {
				cd = hasObsAndEncounter(Metadata.EncounterType.ANC_ENCOUNTER,
				    Dictionary.getConcept(Metadata.Concept.TETANUS_DOSE_GIVEN), doseNumberConcept);
			} else {
				cd = hasObsAndEncounter(Metadata.EncounterType.ANC_ENCOUNTER,
				    Dictionary.getConcept(Metadata.Concept.TETANUS_DOSE_GIVEN));
				
			}
			return cd;
			
		} else {
			//Fetch a cohort of non-pregnant persons given the tetanus dose i.e those without ANC encounters
			CompositionCohortDefinition cd = new CompositionCohortDefinition();
	        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
	        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
	        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.ANC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
	        if (doseNumberConcept != null) {
	        	cd.addSearch("hasObs", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.TETANUS_DOSE_GIVEN), doseNumberConcept), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
	        } else {
	        	cd.addSearch("hasObs", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.TETANUS_DOSE_GIVEN)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
	        }
	        cd.setCompositionString("hasObs AND NOT ancEncounter");

	        return cd;
		}
	}    

}