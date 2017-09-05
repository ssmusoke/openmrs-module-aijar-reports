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
import org.openmrs.Obs;
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
 */
public class ReferalCalculation extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap referalMap = Calculations.lastObs(Dictionary.getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd"), cohort, context);
        CalculationResultMap referalOutMap = Calculations.lastObs(Dictionary.getConcept("c9159851-557b-4c09-8942-65b7989aa20a"), cohort, context);

        for(Integer ptId:cohort) {
            String referOutReason = "";
            String refer = "";

            Concept referralOption = EmrCalculationUtils.codedObsResultForPatient(referalMap, ptId);
            Obs obs = EmrCalculationUtils.obsResultForPatient(referalOutMap, ptId);

            if(obs != null){
                referOutReason = obs.getValueText();
            }

            if(referralOption != null && referralOption.equals(Dictionary.getConcept("6442c9f6-25e8-4c8e-af8a-e9f6845ceaed"))) {
                refer = "REF OUT "+referOutReason;
            }
            else if(referralOption != null && referralOption.equals(Dictionary.getConcept("14714862-6c78-49da-b65b-f249cccddfb6"))) {
                refer = "REF IN";
            }
            else if(referralOption != null && referralOption.equals(Dictionary.getConcept("03997d45-f6f7-4ee2-a6fe-b16985e3495d"))) {
                refer = "C/REF IN";
            }
            else if(referralOption != null && referralOption.equals(Dictionary.getConcept("3af0aae4-4ea7-489d-a5be-c5339f7c5a77"))) {
                refer = "FSG";
            }

            ret.put(ptId, new SimpleResult(refer, this));

        }

        return ret;
    }
}
