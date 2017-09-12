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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 */
public class WhoCd4VLCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        String question = (params != null && params.containsKey("question")) ? (String) params.get("question") : null;
        String answer = (params != null && params.containsKey("answer")) ? (String) params.get("answer") : null;

        CalculationResultMap questionMap = Calculations.lastObs(Dictionary.getConcept(question), cohort, context);
        CalculationResultMap dateMap = Calculations.lastObs(Dictionary.getConcept(answer), cohort, context);

        for(Integer ptId : cohort) {
            String value = "";
            String date = "";
            String results = "";

            Double  questionMapResults = EmrCalculationUtils.numericObsResultForPatient(questionMap, ptId);
            Date dateMapResults = EmrCalculationUtils.datetimeObsResultForPatient(dateMap, ptId);

            if(questionMapResults != null){
                value = questionMapResults.toString();
            }

            if(dateMapResults != null){
                date = formatDate(dateMapResults);
            }

            results = value+"  "+date;

            ret.put(ptId, new SimpleResult(results, this));
        }

        return ret;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date == null) {
            return "";
        }

        return dateFormatter.format(date);
    }
}
