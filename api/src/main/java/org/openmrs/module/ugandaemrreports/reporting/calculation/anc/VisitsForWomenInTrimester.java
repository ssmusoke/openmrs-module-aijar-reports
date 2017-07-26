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
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.BooleanResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 7/26/17.
 */
public class VisitsForWomenInTrimester extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Integer limit = (params != null && params.containsKey("trisemster")) ? (Integer) params.get("trisemster") : null;
        CalculationResultMap lastLmp = Calculations.lastObs(Dictionary.getConcept("27d8e650-615a-473c-954f-ec934b0131d5"), cohort, context);
        for(Integer ptId: cohort){
            Date lmpDate = EmrCalculationUtils.datetimeObsResultForPatient(lastLmp, ptId);
            boolean hasFirstAncInFirstTrimester = false;
            if(lmpDate != null && limit != null) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(lmpDate);
                calendar.add(Calendar.MONTH, limit);

                PatientCalculationContext ctx = Context.getService(PatientCalculationService.class).createCalculationContext();
                ctx.setNow(calendar.getTime());

                //now find the trimester obs
                CalculationResultMap trimester = Calculations.lastObs(Dictionary.getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), Arrays.asList(ptId), ctx);
                if(trimester != null) {
                    Obs obs = EmrCalculationUtils.obsResultForPatient(trimester, ptId);
                    if(obs != null && obs.getValueNumeric() != null && obs.getValueNumeric() == 1.0) {
                        hasFirstAncInFirstTrimester = true;
                    }
                }
            }
            ret.put(ptId, new BooleanResult(hasFirstAncInFirstTrimester, this));
        }
        return ret;
    }
}
