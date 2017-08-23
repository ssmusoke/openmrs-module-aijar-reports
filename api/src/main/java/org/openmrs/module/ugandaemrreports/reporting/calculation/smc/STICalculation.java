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
package org.openmrs.module.ugandaemrreports.reporting.calculation.smc;

import org.openmrs.Obs;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class STICalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Set<Integer> male = Filters.male(cohort, context);
        //CalculationResultMap bleedingDisorderMap = Calculations.lastObs(Dictionary.getConcept("ddbcb5df-dfed-4031-b43a-7808593b1a23"), male, context);
        CalculationResultMap uretheralDischargeMap = Calculations.lastObs(Dictionary.getConcept("5d3a6f44-aba0-47f2-9eb6-07241243909e"), male, context);
        CalculationResultMap painOnUrinationMap = Calculations.lastObs(Dictionary.getConcept("c73c5f65-938b-4072-972f-5d057fb4213c"), male, context);
        //CalculationResultMap swellingOfSrotumMap = Calculations.lastObs(Dictionary.getConcept("36b0c35e-274a-4ae7-a2dd-0f0f7d3bd25c"), male, context);
        CalculationResultMap otherSpecifymMap = Calculations.lastObs(Dictionary.getConcept("b8b59408-c9c7-4522-9e22-0ac79ae103a7"), male, context);
        CalculationResultMap genitalUlcersmMap = Calculations.lastObs(Dictionary.getConcept("ab684ea7-9ad5-4efa-9210-f324e0dd1fa6"), male, context);
        CalculationResultMap penileWartsmMap = Calculations.lastObs(Dictionary.getConcept("3df66d01-13ab-4d48-8aff-44699fed3765"), male, context);
        //CalculationResultMap retractingForeSkinMap = Calculations.lastObs(Dictionary.getConcept("62b70a43-a4ce-4993-8dfb-4118f9cd9b2b"), male, context);
        //CalculationResultMap erectileDisfunctionMap = Calculations.lastObs(Dictionary.getConcept("bc6b8019-5c3d-42ba-94b5-75e5da172a09"), male, context);

        for(Integer ptId:male) {
            String results = "";

            //Obs bleeding = EmrCalculationUtils.obsResultForPatient(bleedingDisorderMap, ptId);
            Obs urethera = EmrCalculationUtils.obsResultForPatient(uretheralDischargeMap, ptId);
            Obs pain = EmrCalculationUtils.obsResultForPatient(painOnUrinationMap, ptId);
            //Obs swelling = EmrCalculationUtils.obsResultForPatient(swellingOfSrotumMap, ptId);
            Obs other = EmrCalculationUtils.obsResultForPatient(otherSpecifymMap, ptId);
            Obs genital = EmrCalculationUtils.obsResultForPatient(genitalUlcersmMap, ptId);
            Obs penile = EmrCalculationUtils.obsResultForPatient(penileWartsmMap, ptId);
            //bs retracting = EmrCalculationUtils.obsResultForPatient(retractingForeSkinMap, ptId);
            //Obs erectile = EmrCalculationUtils.obsResultForPatient(erectileDisfunctionMap, ptId);

            if(urethera != null && urethera.getValueCoded().equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))) {
                results = results+" 1";
            }
            if(pain != null && pain.getValueCoded().equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))) {
                results = results+" 2";
            }
            if(other != null) {
                results = results+" 9";;
            }
            if(genital != null && genital.getValueCoded().equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))) {
                results = results+" 5";
            }
            if(penile != null && penile.getValueCoded().equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))) {
                results = results+" 6";
            }



            ret.put(ptId, new SimpleResult(results, this));

        }
        return ret;
    }
}