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
        return definitionLibrary.hasANCObs(Dictionary.getConcept("dce0e886-30ab-102d-86b0-7a5022ba4115"), Dictionary.getConcept("dcdf4241-30ab-102d-86b0-7a5022ba4115"));
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
        cd.setCompositionString("femaleAndHasAncVisit AND (takingIron OR takingFolic)");
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
    	return definitionLibrary.hasObs(Dictionary.getConcept("dce0e886-30ab-102d-86b0-7a5022ba4115"), Dictionary.getConcept("dcdf4241-30ab-102d-86b0-7a5022ba4115"));
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
        return definitionLibrary.hasObs(Dictionary.getConcept("161033AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
    }    
    
    /**
     * Deliveries in unit 10-19
     * @return CohortDefinition
     */
    public CohortDefinition deliveriesInUnit10To19() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Deliveries in unit 10-19");
        cd.addSearch("deliveriesInUnit", ReportUtils.map(deliveriesInUnit(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age10To19", ReportUtils.map(definitionLibrary.agedBetween(10,19), "endDate=${onOrBefore}"));
        cd.setCompositionString("deliveriesInUnit AND Age10To19");
        return cd;
    }    

    /**
     * Deliveries in unit 20-24
     * @return CohortDefinition
     */
    public CohortDefinition deliveriesInUnit20To24() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Deliveries in unit 20-24");
        cd.addSearch("deliveriesInUnit", ReportUtils.map(deliveriesInUnit(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age20To24", ReportUtils.map(definitionLibrary.agedBetween(20,24), "endDate=${onOrBefore}"));
        cd.setCompositionString("deliveriesInUnit AND Age20To24");
        return cd;
    }    

    /**
     * Deliveries in unit 25 and above
     * @return CohortDefinition
     */
    public CohortDefinition deliveriesInUnit25AndAbove() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Deliveries in unit 25 and above");
        cd.addSearch("deliveriesInUnit", ReportUtils.map(deliveriesInUnit(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age25AndABove", ReportUtils.map(definitionLibrary.agedAtLeast(25), "endDate=${onOrBefore}"));
        cd.setCompositionString("deliveriesInUnit AND Age25AndABove");
        return cd;
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
        cd.addSearch("hivPositive", ReportUtils.map(hivPositiveWomen(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("InitiatingARV", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("35ae2043-a3b0-48de-8e22-05f377ac39a2")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hivPositive AND InitiatingARV");
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
        cd.addSearch("deliveries", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("161033AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
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
        cd.setOperator2(RangeComparator.LESS_THAN);
        cd.setValue2(2.5);
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
     * Maternal deaths - Age 10-19
     * @return CohortDefinition
     */
    public CohortDefinition maternalDeathsAge10To19() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Maternal deaths 10-19");
        cd.addSearch("maternalDeaths", ReportUtils.map(maternalDeaths(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age10To19", ReportUtils.map(definitionLibrary.agedBetween(10,19), "endDate=${onOrBefore}"));
        cd.setCompositionString("maternalDeaths AND Age10To19");
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
        
    /** Maternal deaths - Age 20-24
     * @return CohortDefinition
     */
    public CohortDefinition maternalDeathsAge20To24() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Maternal deaths 20-24");
        cd.addSearch("maternalDeaths", ReportUtils.map(maternalDeaths(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age20To24", ReportUtils.map(definitionLibrary.agedBetween(20,24), "endDate=${onOrBefore}"));
        cd.setCompositionString("maternalDeaths AND Age20To24");
        return cd;
    }

    /**
     * Maternal deaths - Age 25 and above
     * @return CohortDefinition
     */
    public CohortDefinition maternalDeathsAge25AndAbove() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Maternal deaths 25 and above");
        cd.addSearch("maternalDeaths", ReportUtils.map(maternalDeaths(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age25AndAbove", ReportUtils.map(definitionLibrary.agedAtLeast(25), "endDate=${onOrBefore}"));
        cd.setCompositionString("maternalDeaths AND Age25AndAbove");
        return cd;
    }

    /**
     * All Family Planning Users
     * @return CohortDefinition
     */    
	public CohortDefinition allFamilyPlanningUsers() {
		return definitionLibrary.hasObs(
			Dictionary.getConcept(Metadata.Concept.FAMILY_PLANNING_METHOD),
			Dictionary.getConceptList("38aa1dc0-1aaa-4bdd-b26f-28f960dfb16c,"
				+ "4b0899f2-395e-4e0f-8b58-d304b214615e,"
				+ "82624AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,"
				+ "670b7048-d71e-483a-b2ec-f10d2326dd84,"
				+ "dc882c84-30ab-102d-86b0-7a5022ba4115,"
				+ "aeee4ccf-cbf8-473c-9d9f-846643afbf11,"
				+ "dcb2f595-30ab-102d-86b0-7a5022ba4115,"
				+ "fed07c37-7bb6-4baa-adf9-596ce4c4e93c,"
				+ "dd4c3016-13cf-458a-8e93-fe54460be667,"
				+ "dcb30ba3-30ab-102d-86b0-7a5022ba4115,"
				+ "dcb30381-30ab-102d-86b0-7a5022ba4115,"
				+ "5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA,"
				+ "dcb2fba9-30ab-102d-86b0-7a5022ba4115,"
				+ "dcdd8d8d-30ab-102d-86b0-7a5022ba4115,"
				+ "bb83fd9d-24c5-4d49-89c0-97e13c792aaf,"
				+ "dcdd91a7-30ab-102d-86b0-7a5022ba4115,"
				+ "efbe5bf3-3411-4949-855b-636ada05f5e7,"
				+ "336650b2-65f7-4202-80eb-3c6437878262,"
				+ "3e18cafc-8edc-4648-94b3-835de371a2f2,"
				+ "aa14bbbb-cbbe-445d-8958-9f521220b0fd,"
				+ "dc692ad3-30ab-102d-86b0-7a5022ba4115"));
	}
	
    /**
     * HIV+ Family Planning Users 
     * @return CohortDefinition
     */
    public CohortDefinition hivPositiveFamilyPlanningUsers() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ And Family Planning Users");
        cd.addSearch("fpUsers", ReportUtils.map(allFamilyPlanningUsers(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("hivPositive", ReportUtils.map(hivPositivePersons(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("fpUsers AND hivPositive");
        return cd;
    }
    
    //Begin HCT Section
    
    /**
     * Pre-test Counseling done
     * @return CohortDefinition
     */
    public CohortDefinition pretestCounselingDone() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.PRETEST_COUNSELING_DONE),Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"));
    }

    /**
     * Counseled as individuals
     * @return CohortDefinition
     */
    public CohortDefinition counseledAsIndividuals() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.COUNSELING_SESSION_TYPE),Dictionary.getConcept("c61ea879-2a23-484d-bec1-ab177a926265"));
    }

    /**
     * Counseled as couples
     * @return CohortDefinition
     */
    public CohortDefinition counseledAsACouple() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.COUNSELING_SESSION_TYPE),Dictionary.getConcept("6ef3d796-7940-44fe-b0d9-06ab1b824e5b"));
    }

    /**
     * Individuals Counseled 
     * @return CohortDefinition
     */
    public CohortDefinition individualsCounseled() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals Counseled");
        cd.addSearch("PretestCounselingDone", ReportUtils.map(pretestCounselingDone(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("PretestCounselingDone AND counseledAsIndividuals");
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
        
     /** With HIV Test Results
     * @return CohortDefinition
     */
    public CohortDefinition haveHivTestResults() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS));
    }    
    
    /**
     * Individuals Tested 
     * @return CohortDefinition
     */
    public CohortDefinition individualsTested() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals Tested");
        cd.addSearch("withHivTestResults", ReportUtils.map(haveHivTestResults(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("withHivTestResults AND counseledAsIndividuals");
        return cd;
    }

    /**
     * combine the has obs cohort definiton with the encounter of anc
     * @return CohortDefinition
     */
    public CohortDefinition hasObsAndEncounter(String encounterType, Concept q, Concept ... a){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("ancEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, encounterType)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("hasObs", ReportUtils.map(definitionLibrary.hasObs(q, a), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("ancEncounter AND hasObs");
        return cd;
    }

     /** Received HIV Test Results
     * @return CohortDefinition
     */
    public CohortDefinition receivedHivTestResults() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.RECEIVED_HIV_TEST_RESULTS),Dictionary.getConceptList(Metadata.Concept.YES_CIEL));
    }

    /**
     * Tested HIV Positive
     * @return CohortDefinition
     */    
    public CohortDefinition testedHivPositive() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS), Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE));
	}    

    /**
     * Tested HIV Positive
     * @return CohortDefinition
     */    
    public CohortDefinition testedHivNegative() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS), Dictionary.getConcept(Metadata.Concept.HIV_NEGATIVE));
	}    
	
    /**
     * Ever Tested for HIV Before
     * @return CohortDefinition
     */
    public CohortDefinition everTestedForHivBefore() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.HIV_TEST), Dictionary.getConcept(Metadata.Concept.YES_CIEL));
    }    
    
    /**
     * Tested in last 12 Months
     * @return CohortDefinition
     */
    public CohortDefinition testedInLast12Months() {
    	return definitionLibrary.hasNumericObs(Dictionary.getConcept(Metadata.Concept.TIMES_TESTED_IN_LAST_12_MONTHS),RangeComparator.GREATER_THAN,(double) 0);    	
    }

    /**
     * Tested more than twice in the last 12 Months
     * @return CohortDefinition
     */    
	public CohortDefinition testedMoreThanTwiceInLast12Months() {
    	return definitionLibrary.hasNumericObs(Dictionary.getConcept(Metadata.Concept.TIMES_TESTED_IN_LAST_12_MONTHS),RangeComparator.GREATER_THAN,(double) 2);
    }    
    
    /**
     * Individuals who received HIV Test Results
     * @return CohortDefinition
     */
    public CohortDefinition individualsWhoReceivedHIVTestResults() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals who received HIV Test Results");
        cd.addSearch("receivedHivTestResults", ReportUtils.map(receivedHivTestResults(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("receivedHivTestResults AND counseledAsIndividuals");
        return cd;
	}    

    /**
     * Number of individuals who received HIV results in the last 12months
     * @return CohortDefinition
     */
    public CohortDefinition individualsWhoReceivedHIVTestResultsInLast12Months() {
	    /**
	     * Individuals Who Received HIV Test Results
	     * @return CohortDefinition
	     */
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals who received HIV Test Results in Last 12 Months");
        cd.addSearch("receivedHivTestResults", ReportUtils.map(receivedHivTestResults(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("TestedInLast12Months", ReportUtils.map(testedInLast12Months(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("receivedHivTestResults AND counseledAsIndividuals AND TestedInLast12Months");
        return cd;
	}    
    
    /**
     * Individuals Tested for the first time
     * @return CohortDefinition
     */
    public CohortDefinition individualsTestedFirstTime() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals Tested First Time");
        cd.addSearch("testedFirstTime", ReportUtils.map(everTestedForHivBefore(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedFirstTime AND counseledAsIndividuals");
        return cd;
    }

    /**
     * Individuals who Tested HIV Positive
     * @return CohortDefinition
     */
    public CohortDefinition individualsWhoTestedHivPositive() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals who Tested HIV Positive");
        cd.addSearch("testedHivPositive", ReportUtils.map(testedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedHivPositive AND counseledAsIndividuals");
        return cd;
	}

    /**
     * HIV positive individuals with presumptive TB
     * @return CohortDefinition
     */
	public CohortDefinition individualsWhoTestedHivPositiveAndWithPresumptiveTB() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals who Tested HIV Positive And With Presumptive TB");
        cd.addSearch("tbSuspect", ReportUtils.map(tbSuspect(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("testedHivPositive", ReportUtils.map(testedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedHivPositive AND counseledAsIndividuals AND tbSuspect");
        return cd;
	}

    /**
     * TB Suspect
     * @return CohortDefinition
     */
	public CohortDefinition tbSuspect() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.TB_SUSPECT), Dictionary.getConcept(Metadata.Concept.YES_CIEL));
	}

	/**
	 * Individuals tested more than twice in the last 12 months
	 * @return CohortDefinition
	 */			
	public CohortDefinition individualsTestedMoreThanTwiceInLast12Months() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals tested more than twice in the last 12 months");
        cd.addSearch("testedMoreThanTwiceInlast12Months", ReportUtils.map(testedMoreThanTwiceInLast12Months(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedMoreThanTwiceInlast12Months AND counseledAsIndividuals");
        return cd;		
	}

	/**
	 * Individuals who were Counseled and Tested together as a Couple
	 * @return CohortDefinition
	 */			
	public CohortDefinition individualsCounseledAndTestedAsACouple() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals Counseled and Tested together as a Couple");
        cd.addSearch("tested", ReportUtils.map(haveHivTestResults(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("CounseledAsACouple", ReportUtils.map(counseledAsACouple(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("tested AND CounseledAsACouple");
        return cd;
	}

	/**
	 * Number of individuals who were Tested and Received results together as a Couple
	 * @return CohortDefinition
	 */			
	public CohortDefinition individualsTestedAndReceivedResultsAsACouple() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals who were Tested and Received results together as a Couple");
        cd.addSearch("testedAsACouple", ReportUtils.map(individualsCounseledAndTestedAsACouple(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("ReceivedResults", ReportUtils.map(receivedHivTestResults(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedAsACouple AND ReceivedResults");
        return cd;
	}

	/**
	 * Individuals counseled and tested for PEP
	 * @return
	 */
	public CohortDefinition individualsCounseledAndTestedForPep() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Individuals tested more than twice in the last 12 months");
        cd.addSearch("testedForPep", ReportUtils.map(testedForPep(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedForPep AND counseledAsIndividuals");
        return cd;
	}
	
	/**
	 * Tested for PEP
	 * @return CohortDefiniton
	 */
	public CohortDefinition testedForPep() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.HCT_ENTRY_POINT), Dictionary.getConcept(Metadata.Concept.POST_EXPOSURE_PROPHYLAXIS));
	}

	/**
	 * Number of individuals tested as MARPS
	 * @return CohortDefiniton
	 */
	public CohortDefinition individualsCounseledAndTestedAsMarps() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Number of individuals tested as MARPS");
        cd.addSearch("testedAsMarps", ReportUtils.map(testedAsMarps(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedAsMarps AND counseledAsIndividuals");
        return cd;
	}

	/**
	 * Tested as MARPS
	 * @return
	 */
	private CohortDefinition testedAsMarps() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.HCT_ENTRY_POINT), Dictionary.getConcept(Metadata.Concept.MARPS));
	}

	/**
	 * Linked to care
	 * @return
	 */
	public CohortDefinition clientsLinkedToCare() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.LINKED_TO_CARE), Dictionary.getConcept(Metadata.Concept.YES_CIEL));
	}

	/**
	 * Positive Individuals tested at an early stage
	 * @return CohortDefinition
	 */
	public CohortDefinition hivPositiveIndividualsTestedAtAnEarlyStage() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV Positive Individuals tested at an early stage");
        cd.addSearch("testedAtAnEarlyStage", ReportUtils.map(testedAtEarlyStage(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("testedHIVPositive", ReportUtils.map(testedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("counseledAsIndividuals", ReportUtils.map(counseledAsIndividuals(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("testedHIVPositive AND counseledAsIndividuals AND testedAtAnEarlyStage");
        return cd;
	}

	/**
	 * Tested at an early stage (CD4>500μ)
	 * @return CohortDefinition
	 */
	private CohortDefinition testedAtEarlyStage() {
    	return definitionLibrary.hasNumericObs(Dictionary.getConcept(Metadata.Concept.CD4_COUNT),RangeComparator.GREATER_THAN,(double) 500);
	}
	
	/**
	 * Couples with concordant Positive results
	 * @return CohortDefinition
	 */
	public CohortDefinition couplesWithConcordantPostiveResults() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Couples with concordant Positive results");
        cd.addSearch("partnerTestedHivPositive", ReportUtils.map(partnerTestedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("testedHivPositive", ReportUtils.map(testedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("CounseledAsACouple", ReportUtils.map(counseledAsACouple(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("CounseledAsACouple AND testedHivPositive AND partnerTestedHivPositive");
        return cd;
	}

	/**
	 * Partner Tested HIV Positive
	 * @return
	 */
	private CohortDefinition partnerTestedHivPositive() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.PARTNER_HIV_TEST_RESULT), Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE));
	}

	/**
	 * Partner Tested HIV Negative
	 * @return
	 */
	private CohortDefinition partnerTestedHiVNegative() {
        return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.PARTNER_HIV_TEST_RESULT), Dictionary.getConcept(Metadata.Concept.HIV_NEGATIVE));
	}
	
	/**
	 * Couples with discordant results
	 * @return
	 */
	public CohortDefinition couplesWithDiscordantResults() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Couples with concordant Positive results");
        cd.addSearch("partnerTestedHivPositive", ReportUtils.map(partnerTestedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("testedHivPositive", ReportUtils.map(testedHivPositive(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("partnerTestedHivNegative", ReportUtils.map(partnerTestedHiVNegative(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("testedHivNegative", ReportUtils.map(testedHivNegative(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("CounseledAsACouple", ReportUtils.map(counseledAsACouple(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("CounseledAsACouple AND ((testedHivPositive AND NOT partnerTestedHivPositive) OR (testedHivNegative AND NOT partnerTestedHivNegative))");
        return cd;
	}
	//End HCT Section    

	/**
	 * Total outpatient attendance
	 * @return
	 */	
	public CohortDefinition totalOutPatientAttendance() {
        EncounterCohortDefinition cd = new EncounterCohortDefinition();
        cd.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.OPD_ENCOUNTER)));
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        return cd;
	}

	/**
	 * New outpatient attendance
	 * @return
	 */	
	public CohortDefinition newOutPatientAttendance() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("OutPatient Attendance");
        cd.addSearch("outPatientEncounters", ReportUtils.map(totalOutPatientAttendance(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("newEncounters", ReportUtils.map(newEncounters(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outPatientEncounters AND newEncounters");
        return cd;
	}

	/**
	 * Repeat outpatient attendance
	 * @return
	 */	
	public CohortDefinition repeatOutPatientAttendance() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("OutPatient Attendance");
        cd.addSearch("outPatientEncounters", ReportUtils.map(totalOutPatientAttendance(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("repeatEncounters", ReportUtils.map(repeatEncounters(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outPatientEncounters AND repeatEncounters");
        return cd;
	}

	/**
	 * Repeat Encounters
	 * @return
	 */	
	public CohortDefinition repeatEncounters() {
		return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.TYPE_OF_PATIENT), Dictionary.getConcept(Metadata.Concept.REPEAT_ENCOUNTER));
	}

	/**
	 * New Encounters
	 * @return
	 */	
	public CohortDefinition newEncounters() {
		return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.TYPE_OF_PATIENT), Dictionary.getConcept(Metadata.Concept.NEW_ENCOUNTER));
	}

	/**
	 * Referrals To Unit
	 * @return
	 */	
	public CohortDefinition referralsToUnit() {
		return definitionLibrary.hasTextObs(Dictionary.getConcept(Metadata.Concept.TRANSFER_IN_NUMBER));
	}

	/**
	 * Referrals From Unit
	 * @return
	 */	
	public CohortDefinition referralsFromUnit() {
		return definitionLibrary.hasTextObs(Dictionary.getConcept(Metadata.Concept.REFERRAL_NUMBER));
	}
	
	/**
	 * Referrals To OPD Unit
	 * @return
	 */	
	public CohortDefinition referralsToOPDUnit() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Referrals To OPD Unit");
        cd.addSearch("outPatientEncounters", ReportUtils.map(totalOutPatientAttendance(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("referralsToUnit", ReportUtils.map(referralsToUnit(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outPatientEncounters AND referralsToUnit");
        return cd;
	}

	/**
	 * Referrals From OPD Unit
	 * @return
	 */	
	public CohortDefinition referralsFromOPDUnit() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Referrals From OPD Unit");
        cd.addSearch("outPatientEncounters", ReportUtils.map(totalOutPatientAttendance(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("referralsFromUnit", ReportUtils.map(referralsFromUnit(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outPatientEncounters AND referralsFromUnit");
        return cd;
	}

	/**
	 * Total OPD Malaria Diagnosis
	 * 
	 * @return
	 */
	public CohortDefinition totalOpdMalariaDiagnoses() {
		return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.OPD_DIAGNOSIS),
		    Dictionary.getConceptList(Metadata.Concept.CLINICAL_MALARIA + "," + Metadata.Concept.CONFIRMED_MALARIA + ","
		            + Metadata.Concept.CIEL_MALARIA + "," + Metadata.Concept.MALARIA_IN_PREGNANCY));
	}
	
	/**
	 * Total Microscopic & RDT Malaria Tests Done
	 * @return
	 */		
	public CohortDefinition microscopicAndRdtTestsDone() {
		return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.TYPE_OF_MALARIA_TEST),Dictionary.getConceptList(Metadata.Concept.MALARIAL_SMEAR + ","  + Metadata.Concept.RAPID_TEST_FOR_MALARIA));
	}

	/**
	 * Total Positive Malaria Tests Done
	 * @return
	 */		
	public CohortDefinition positiveMalariaTestResults() {
		return definitionLibrary.hasObs(Dictionary.getConcept(Metadata.Concept.MALARIA_TEST_RESULT),Dictionary.getConcept(Metadata.Concept.POSITIVE));
	}		
	
	/**
	 * Total Confirmed OPD Malaria Diagnosis
	 * @return
	 */		
	public CohortDefinition totalConfirmedOpdMalariaDiagnoses() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Total Confirmed OPD Malaria Diagnosis");
        cd.addSearch("outPatientEncounters", ReportUtils.map(totalOutPatientAttendance(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));        
        cd.addSearch("microscopicAndRdtTestsDone", ReportUtils.map(microscopicAndRdtTestsDone(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("positiveMalariaTestResults", ReportUtils.map(positiveMalariaTestResults(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outPatientEncounters AND microscopicAndRdtTestsDone AND positiveMalariaTestResults");
        return cd;
	}		

	 /** Patients With BMI count Between @minValue and @maxValue 
	  * 
	  * @return CohortDefinition
	  */
	 public CohortDefinition bmiCount(Double minValue, Double maxValue) {
			return definitionLibrary.hasNumericObs(Dictionary.getConcept(Metadata.Concept.BMI), RangeComparator.GREATER_EQUAL, minValue, RangeComparator.LESS_EQUAL, maxValue);
	 }
	
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
