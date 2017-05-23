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

import org.openmrs.Obs;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.BooleanResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 5/23/17.
 */
public class ANCVisitStages extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Double stage = (params != null && params.containsKey("stage")) ? (Double) params.get("stage") : null;

        CalculationResultMap ancVist = Calculations.allObs(Dictionary.getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), cohort, context);

        for(Integer ptId: cohort){
            boolean stageFound = false;
            ListResult listResult = (ListResult) ancVist.get(ptId);
            List<Obs> allObs = CalculationUtils.extractResultValues(listResult);
            for(Obs obs:allObs) {
                if(obs != null && stage != null && obs.getValueNumeric().doubleValue() == stage){
                    stageFound = true;
                    break;
                }
            }

            ret.put(ptId, new BooleanResult(stageFound, this));
        }
        return ret;
    }
}
