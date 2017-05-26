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
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static org.openmrs.module.ugandaemrreports.UgandaEMRReportUtil.map;
import static org.openmrs.module.ugandaemrreports.reporting.utils.EmrReportingUtils.cohortIndicator;

/**
 * Created by Nicholas Ingosi on 5/23/17.
 * * Library of ANC related indicator definitions. All indicators require parameters ${startDate} and ${endDate}
 */
@Component
public class Moh105IndicatorLibrary {

    @Autowired
    private Moh105CohortLibrary cohortLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary cclibrary;

    /**
     * Number of female patients with ANC 1st visit
     */
    public CohortIndicator anc1stVisit(){
        return cohortIndicator("Patients who have ANC 1st Visit", map(cohortLibrary.femaleAndHasAncVisit(0.0, 1.0), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Number of female patients with ANC 4th visit
     */
    public CohortIndicator anc4thVisit(){
        return cohortIndicator("Patients who have ANC 4th Visit", map(cohortLibrary.femaleAndHasAncVisit(3.0, 4.0), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Number of female patients with ANC 4th visit and above
     */
    public CohortIndicator anc4thPlusVisit(){
        return cohortIndicator("Patients who have ANC 4th Visit and above", map(cohortLibrary.femaleAndHasAncVisit(4.0, 9.0), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Total number of patients with ANC visits
     * @return CohortIndicator
     */
    public CohortIndicator totalAncVisits() {
        return cohortIndicator("Patients who have ANC Visits", map(cohortLibrary.femaleAndHasAncVisit(0.0, 9.0), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Referral to ANC unit from community services
     * @return CohortIndicator
     */
    public CohortIndicator referalToAncUnitFromCommunityServices(){
        return cohortIndicator("Referral to ANC unit from community services", map(cclibrary.hasObs(Dictionary.getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd"), Dictionary.getConcept("03997d45-f6f7-4ee2-a6fe-b16985e3495d")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Referral to ANC unit total
     * @return CohortIndicator
     */
    public CohortIndicator referalToAncUnitTotal(){
        return cohortIndicator("Referral to ANC unit totals", map(cclibrary.hasObs(Dictionary.getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd"), Dictionary.getConcept("03997d45-f6f7-4ee2-a6fe-b16985e3495d"), Dictionary.getConcept("14714862-6c78-49da-b65b-f249cccddfb6")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Referral from ANC unit total
     * @return CohortIndicator
     */
    public CohortIndicator referalFromAncUnitTotal(){
        return cohortIndicator("Referral from ANC unit totals", map(cclibrary.hasObs(Dictionary.getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd"), Dictionary.getConcept("6442c9f6-25e8-4c8e-af8a-e9f6845ceaed"), Dictionary.getConcept("3af0aae4-4ea7-489d-a5be-c5339f7c5a77")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }
    /**
     * Referral from ANC unit FSG
     * @return CohortIndicator
     */
    public CohortIndicator referalFromAncUniFsg(){
        return cohortIndicator("Referral from ANC unit FSG", map(cclibrary.hasObs(Dictionary.getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd"), Dictionary.getConcept("3af0aae4-4ea7-489d-a5be-c5339f7c5a77")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * IPT(IPT1) Dose
     * @return CohortIndicator
     */
    public CohortIndicator iptDose(Concept answer){
        return cohortIndicator("Ipt dose on "+answer.getName().getName(), map(cclibrary.hasObs(Dictionary.getConcept("1da3cb98-59d8-4bfd-b0bb-c9c1bcd058c6"), answer), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * A9-Pregnant Women receiving Iron/Folic Acid on ANC 1 st Visit
     * @return CohortIndicator
     */
    public CohortIndicator pregnantAndReceivingIronOrFolicAcidAnc1stVisit() {
        return cohortIndicator("Pregnant Women receiving Iron/Folic Acid on ANC 1 st Visit", map(cohortLibrary.pregnantAndReceivingIronOrFolicAcidAnc1stVisit(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant women having different obs
     * @return CohortIndicator
     */
    public CohortIndicator pregnantAndReceivingServices(Concept question, Concept answer) {
        return cohortIndicator("Receiving different servicess", map(cclibrary.hasObs(question, answer), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant women tested for syphilis
     * @return CohortIndicator
     */
    public CohortIndicator pregnantAndTestedForSyphilis() {
        return cohortIndicator("Pregnant women tested for syphilis", map(cclibrary.hasObs(Dictionary.getConcept("275a6f72-b8a4-4038-977a-727552f69cb8")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant Women newly tested for HIV this pregnancy (TR & TRR)
     * @return CohortIndicator
     */
    public CohortIndicator pregnantWomenNewlyTestedForHivThisPregnancyTRAndTRR() {
        return cohortIndicator("Pregnant Women newly tested for HIV this pregnancy (TR & TRR)", map(cclibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"), Dictionary.getConcept("05f16fc5-1d82-4ce8-9b44-a3125fbbf2d7"), Dictionary.getConcept("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant Women tested HIV+ for 1st time this pregnancy (TRR) at any visit
     * @return CohortIndicator
     */
    public CohortIndicator pregnantWomenNewlyTestedForHivThisPregnancyTRR() {
        return cohortIndicator("Pregnant Women tested HIV+ for 1st time this pregnancy (TRR) at any visit", map(cclibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"),  Dictionary.getConcept("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * HIV+ Pregnant women assessed by CD4 or WHO clinical stage the 1 time
     * @return CohortIndicator
     */
    public CohortIndicator hivPositiveAndAccessedWithCd4WhoStage(Concept question) {
        return cohortIndicator("Pregnant women assessed by "+question.getName().getName(), map(cohortLibrary.hivPositiveAndAccessedWithCd4WhoStage(question), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**Pregnant women assessed by
     * HIV+ Pregnant Women initiated on ART for EMTCT (ART)
     * @return CohortIndicator
     */
    public CohortIndicator hivPositiveInitiatedART() {
        return cohortIndicator("Pregnant Women tested HIV+ and initiated on ART", map(cclibrary.hasObs(Dictionary.getConcept("a615f932-26ee-449c-8e20-e50a15232763"),  Dictionary.getConcept("026e31b7-4a26-44d0-8398-9a41c40ff7d3")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant women who knew status before 1st ANC total (TRK+TRRK)
     * @return CohortIndicator
     */
    public CohortIndicator pregnantTrkTrrk() {
        return cohortIndicator("Pregnant women who knew status before 1st ANC total (TRK+TRRK)", map(cclibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"), Dictionary.getConcept("81bd3e58-9389-41e7-be1a-c6723f899e56"), Dictionary.getConcept("1f177240-85f6-4f10-964a-cfc7722408b3")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant women who knew status before 1st ANC total TRRK
     * @return CohortIndicator
     */
    public CohortIndicator pregnantTrrk() {
        return cohortIndicator("Pregnant women who knew status before 1st ANC total TRRK", map(cclibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"), Dictionary.getConcept("1f177240-85f6-4f10-964a-cfc7722408b3")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant Women already on ART before 1 ANC (ART-K)
     * @return CohortIndicator
     */
    public CohortIndicator alreadyOnARTK(){
        return cohortIndicator("Pregnant Women already on ART before 1 ANC (ART-K)", map(cclibrary.hasObs(Dictionary.getConcept("a615f932-26ee-449c-8e20-e50a15232763"), Dictionary.getConcept("2aa7d442-6cbb-4609-9dd3-bc2ad6f05016")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant Women re-tested later in pregnancy (TR+&TRR+)
     * @return CohortIndicator
     */
    public CohortIndicator retestedTrTrrPlus(){
        return cohortIndicator("Pregnant Women re-tested later in pregnancy (TR+&TRR+))", map(cclibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"), Dictionary.getConcept("81bd3e58-9389-41e7-be1a-c6723f899e56"), Dictionary.getConcept("1f177240-85f6-4f10-964a-cfc7722408b3")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Pregnant Women testing HIV+ on a retest (TRR+)
     * @return CohortIndicator
     */
    public CohortIndicator retestedTrrPlus() {
        return cohortIndicator("Pregnant Women testing HIV+ on a retest (TRR+)", map(cclibrary.hasObs(Dictionary.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74"), Dictionary.getConcept("1f177240-85f6-4f10-964a-cfc7722408b3")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * HIV+ Pregnant Women initiated on Cotrimoxazole
     * @return CohortIndicator
     */
    public CohortIndicator initiatedOnCtx() {
        return cohortIndicator("Pregnant Women initiated on Cotrimoxazole", map(cclibrary.hasObs(Dictionary.getConcept("1da3cb98-59d8-4bfd-b0bb-c9c1bcd058c6"), Dictionary.getConcept("fca28768-50dc-4d6b-a3d2-2aae3b376b27")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     * Male partiners received HIV test results in eMTCT total
     * @return CohortIndicator
     */
    public CohortIndicator malePatinersRecievedHivResultTotal() {
        return cohortIndicator("Male partners received HIV test results in eMTCT - Totals", map(cclibrary.hasObs(Dictionary.getConcept("62a37075-fc2a-4729-8950-b9fae9b22cfb"), Dictionary.getConcept("05f16fc5-1d82-4ce8-9b44-a3125fbbf2d7"), Dictionary.getConcept("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

    /**
     *Male partiners received HIV test results in eMTCT HIV+
     * @return CohortIndicator
     */
    public CohortIndicator malePatinersRecievedHivResultHivPositive() {
        return cohortIndicator("Male partners received HIV test results in eMTCT - HIV+", map(cclibrary.hasObs(Dictionary.getConcept("62a37075-fc2a-4729-8950-b9fae9b22cfb"), Dictionary.getConcept("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a")), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    }

}
