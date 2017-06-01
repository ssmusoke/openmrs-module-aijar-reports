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

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.CoreUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.ugandaemrreports.UgandaEMRReportUtil.map;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by Nicholas Ingosi on 5/23/17.
 */
@Component
public class Moh105CohortLibrary {

    @Autowired
    CommonCohortDefinitionLibrary definitionLibrary;

    public CohortDefinition femaleAndHasAncVisit(double lower, double upper){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Female and has ANC Visit");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("female", ReportUtils.map(definitionLibrary.females(), ""));
        cd.addSearch("ancVist", ReportUtils.map(totalAncVisits(lower, upper), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("female AND ancVist");
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
        cd.setOperator1(RangeComparator.GREATER_EQUAL);
        cd.setValue1(lower);
        cd.setOperator2(RangeComparator.LESS_EQUAL);
        cd.setValue2(upper);
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
        cd.setCompositionString("femaleAndHasAncVisit AND (takingIron OR takingFolic)");
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
        cd.addSearch("hivPositive", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("dce0e886-30ab-102d-86b0-7a5022ba4115"), Dictionary.getConcept("dcdf4241-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("assessedBy", ReportUtils.map(definitionLibrary.hasObs(question), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.setCompositionString("hivPositive AND assessedBy");
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
     * HIV+ women
     * @return CohortDefinition
     */
    public CohortDefinition hivPositiveWomen() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("HIV+ Women");
        cd.addSearch("female", ReportUtils.map(definitionLibrary.females(), ""));
        cd.addSearch("hivPositive", ReportUtils.map(definitionLibrary.hasObs(Dictionary.getConcept("dce0e886-30ab-102d-86b0-7a5022ba4115"), Dictionary.getConcept("dcdf4241-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
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
        cd.addSearch("Age10To19", ReportUtils.map(definitionLibrary.agedBetween(10,19), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("maternalDeaths AND Age10To19");
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
        cd.addSearch("Age20To24", ReportUtils.map(definitionLibrary.agedBetween(20,24), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("maternalDeaths AND Age20To24");
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
        cd.addSearch("Age25AndABove", ReportUtils.map(definitionLibrary.agedAtLeast(25), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("maternalDeaths AND Age25AndABove");
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
        cd.addSearch("Age10To19", ReportUtils.map(definitionLibrary.agedBetween(10,19), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("maternalDeaths AND Age10To19");
        return cd;
    }

    /**
     * Maternal deaths - Age 20-24
     * @return CohortDefinition
     */
    public CohortDefinition maternalDeathsAge20To24() {
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.setName("Maternal deaths 20-24");
        cd.addSearch("maternalDeaths", ReportUtils.map(maternalDeaths(), "onOrAfter=${onOrAfter},onOrBefore=${onOrBefore}"));
        cd.addSearch("Age20To24", ReportUtils.map(definitionLibrary.agedBetween(20,24), "onOrAfter=${startDate},onOrBefore=${endDate}"));
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
        cd.addSearch("Age25AndAbove", ReportUtils.map(definitionLibrary.agedAtLeast(25), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("maternalDeaths AND Age25AndAbove");
        return cd;
    }
    
}
