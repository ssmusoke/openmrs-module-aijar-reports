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
public class BloodPressureCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        //calculation result maps to fetch each of the last obs
        CalculationResultMap systollicObsMap = Calculations.lastObs(Dictionary.getConcept("5085AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), cohort, context);
        CalculationResultMap diastollicObsMap = Calculations.lastObs(Dictionary.getConcept("5086AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), cohort, context);

        for(Integer ptId: cohort) {
            String fullBp = "";
            Double systollicObs = EmrCalculationUtils.numericObsResultForPatient(systollicObsMap, ptId);
            Double diastollicObs = EmrCalculationUtils.numericObsResultForPatient(diastollicObsMap, ptId);

            if(systollicObs != null && diastollicObs != null) {
                fullBp = systollicObs.toString()+"/"+diastollicObs.toString();
            }
            ret.put(ptId, new SimpleResult(fullBp, this));
        }
        return ret;
    }
}
