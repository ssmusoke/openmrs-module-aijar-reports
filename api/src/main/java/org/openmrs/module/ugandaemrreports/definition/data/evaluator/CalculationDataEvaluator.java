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
package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

/**
 */

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.patient.PatientCalculationService;
import org.openmrs.calculation.result.CalculationResult;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;

import java.util.Date;
import java.util.Map;

/**
 * Evaluates a {@link org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition} to produce a PatientData
 */
@Handler(supports = CalculationDataDefinition.class, order = 50)
public class CalculationDataEvaluator implements PatientDataEvaluator {

    /**
     * @see PatientDataEvaluator#evaluate(org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition, org.openmrs.module.reporting.evaluation.EvaluationContext)
     */
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {

        CalculationDataDefinition def = (CalculationDataDefinition) definition;
        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        // return right away if there is nothing to evaluate
        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        // Use date from cohort definition, or from ${date} or ${endDate} or now
        Date onDate = def.getOnDate();
        if (onDate == null) {
            onDate = (Date) context.getParameterValue("date");
            if (onDate == null) {
                onDate = (Date) context.getParameterValue("endDate");
                if (onDate == null) {
                    onDate = new Date();
                }
            }
        }
        // evaluate the calculation
        PatientCalculationService service = Context.getService(PatientCalculationService.class);
        PatientCalculationContext calcContext = service.createCalculationContext();
        calcContext.setNow(onDate);
        CalculationResultMap resultMap = service.evaluate(context.getBaseCohort().getMemberIds(), def.getCalculation(), def.getCalculationParameters(), calcContext);

        // move data into return object
        for (Map.Entry<Integer, CalculationResult> entry : resultMap.entrySet()) {
            c.addData(entry.getKey(), entry.getValue());
        }

        return c;
    }
}
