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
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.SmcReturnFollowUpCalculation;
import org.openmrs.module.ugandaemrreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
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
	
//cohorts for SMC 105 section
    /**
     *
     * @return CohortDefinition
     */
    public CohortDefinition siteTypeFacilitySurgicalCircumcision(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        Concept dorsal = Dictionary.getConcept("e63ac8e3-5027-43c3-9421-ce995ea039cf");
        Concept sleeve = Dictionary.getConcept("0ee1b2ae-2961-41d6-9fe0-7d9f876232ae");
        cd.setName("Facility and surgical");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("facility", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802"), Dictionary.getConcept("f2aa1852-fcfe-484b-a6ef-1613bd3a1a7f")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("surgical", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("bd66b11f-04d9-46ed-a367-2c27c15d5c71"), dorsal, sleeve), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("facility AND surgical AND smcEncounter");

        return cd;

    }

    /**
     *
     * @return CohortDefinition
     */
    public CohortDefinition siteTypeFacilityDeviceCircumcision(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        Concept forceps = Dictionary.getConcept("0308bd0a-0e28-4c62-acbd-5ea969c296db");
        cd.setName("Facility and Device");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("facility", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802"), Dictionary.getConcept("f2aa1852-fcfe-484b-a6ef-1613bd3a1a7f")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("device", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("bd66b11f-04d9-46ed-a367-2c27c15d5c71"), forceps), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("facility AND device AND smcEncounter");

        return cd;

    }

    /**
     *
     * @return CohortDefinition
     */
    public CohortDefinition siteTypeOutReachSurgicalCircumcision(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        Concept dorsal = Dictionary.getConcept("e63ac8e3-5027-43c3-9421-ce995ea039cf");
        Concept sleeve = Dictionary.getConcept("0ee1b2ae-2961-41d6-9fe0-7d9f876232ae");
        cd.setName("Outreach and surgical");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("outreach", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802"), Dictionary.getConcept("03596df2-09bc-4d1f-94fd-484411ac9012")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("surgical", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("bd66b11f-04d9-46ed-a367-2c27c15d5c71"), dorsal, sleeve), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outreach AND surgical AND smcEncounter");

        return cd;

    }
    /**
     *
     * @return CohortDefinition
     */

    public CohortDefinition siteTypeOutReachDeviceCircumcision(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        Concept forceps = Dictionary.getConcept("0308bd0a-0e28-4c62-acbd-5ea969c296db");
        cd.setName("Outreach and device");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("outreach", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802"), Dictionary.getConcept("03596df2-09bc-4d1f-94fd-484411ac9012")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("device", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("bd66b11f-04d9-46ed-a367-2c27c15d5c71"), forceps), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("outreach AND device AND smcEncounter");

        return cd;

    }
    /**
     *@param answer
     * @return CohortDefinition
     */
    public CohortDefinition counseledTestedForHivResults(Concept answer) {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Counseled and Tested for HIV and have results");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("counseled", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("cd8a8a72-4046-4595-94d0-52138534272a"), Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("results", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("29c47b5c-b27d-499c-b52c-7be676a0a78f"), answer), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("counseled AND results AND smcEncounter");
        return cd;
    }

    /**
     *
     *
     * @return CohortDefinition
     */
    public CohortDefinition counseledTestedForHiv() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Counseled and Tested for HIV");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("counseled", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("cd8a8a72-4046-4595-94d0-52138534272a"), Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("tested", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("29c47b5c-b27d-499c-b52c-7be676a0a78f")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("counseled AND tested AND smcEncounter");
        return cd;
    }

    /**
     * Number of clients circumcised who returned for a follow up within 6 weeks
     * @return CohortDefinition
     */
    public CohortDefinition clientsCircumcisedAndReturnedWithin6Weeks(Integer visit){
        CalculationCohortDefinition cd = new CalculationCohortDefinition("returned", new SmcReturnFollowUpCalculation());
        cd.setName("clients returned for visit");
        cd.addParameter(new Parameter("onDate", "End Date", Date.class ));
        cd.addCalculationParameter("visit", visit);
        return cd;
    }
    /**
     * 
     */
    public CohortDefinition clientsCircumcisedAndReturnedWithin6WeeksAndHaveSmcEncounter(int visit) {
    	CompositionCohortDefinition cd = new CompositionCohortDefinition();
    	cd.setName("Returned for visit and has SMC encounter within period");
    	cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        
        cd.addSearch("visit", ReportUtils.map(clientsCircumcisedAndReturnedWithin6Weeks(visit), "onDate=${onOrBefore}"));
        cd.addSearch("smcEncounter", ReportUtils.map(definitionLibrary.hasEncounter(MetadataUtils.existing(EncounterType.class, Metadata.EncounterType.SMC_ENCOUNTER)), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("visit AND smcEncounter");
    return cd;
    }

}