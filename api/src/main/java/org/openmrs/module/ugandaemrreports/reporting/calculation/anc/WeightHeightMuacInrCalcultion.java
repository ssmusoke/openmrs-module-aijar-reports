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
package org.openmrs.module.ugandaemrreports.reporting.calculation.anc;

import org.openmrs.Concept;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 4/29/17.
 */
public class WeightHeightMuacInrCalcultion extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {

        CalculationResultMap ret = new CalculationResultMap();

        //calculation result maps to fetch each of the last obs
        CalculationResultMap weightObsMap = Calculations.lastObs(Dictionary.getConcept("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), cohort, context);
        CalculationResultMap heightObsMap = Calculations.lastObs(Dictionary.getConcept("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), cohort, context);
        CalculationResultMap muacObsMap = Calculations.lastObs(Dictionary.getConcept("5f86d19d-9546-4466-89c0-6f80c101191b"), cohort, context);
        CalculationResultMap inrNoObsMap = Calculations.lastObs(Dictionary.getConcept("b644c29c-9bb0-447e-9f73-2ae89496a709"), cohort, context);

        for(Integer ptId : cohort) {
            String results = "";
            String weight = "";
            String height = "";
            String muac = "";
            String inr = "";

            Double weightObs = EmrCalculationUtils.numericObsResultForPatient(weightObsMap, ptId);
            Double heightObs = EmrCalculationUtils.numericObsResultForPatient(heightObsMap, ptId);
            Concept muacAnswer = EmrCalculationUtils.codedObsResultForPatient(muacObsMap, ptId);
            Double inrNoObs = EmrCalculationUtils.numericObsResultForPatient(inrNoObsMap, ptId);

            if(weightObs != null) {
                weight = weightObs.toString();
            }
            if(heightObs != null){
                height = heightObs.toString();
            }
            if(inrNoObs != null) {
                inr= inrNoObs.toString();
            }

            if(muacAnswer != null && muacAnswer.equals(Dictionary.getConcept("8846c03f-67bf-4aeb-8ca7-39bf79b4ebf3"))) {
                muac = "R"; //Red
            }
            else if(muacAnswer != null && muacAnswer.equals(Dictionary.getConcept("de330d01-5586-4eed-a645-e04b6bd13701"))) {
                muac = "Y"; //Yellow
            }
            else if(muacAnswer != null && muacAnswer.equals(Dictionary.getConcept("a3b1734c-4743-4b9d-8e71-08d0459d29b9"))) {
                muac = "G"; //Green
            }
            results = weight+"\n"+height+"\n"+muac+"\n"+inr;
            ret.put(ptId, new SimpleResult(results, this));

        }

        return ret;
    }
}
