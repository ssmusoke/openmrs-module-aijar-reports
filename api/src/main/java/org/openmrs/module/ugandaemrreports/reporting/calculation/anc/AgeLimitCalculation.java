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
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 4/28/17.
 */
public class AgeLimitCalculation extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Integer lowerLimit = (params != null && params.containsKey("lowerLimit")) ? (Integer) params.get("lowerLimit") : null;
        Integer upperLimit = (params != null && params.containsKey("upperLimit")) ? (Integer) params.get("upperLimit") : null;
        CalculationResultMap ages = Calculations.ages(cohort, context);

        System.out.println("The date passed is ::::"+context.getNow());
        for (int ptId : cohort) {
            Integer age = null;
            Integer ageInyears = ((Age) ages.get(ptId).getValue()).getFullYears();
            if(ageInyears != null && lowerLimit != null && lowerLimit >= 25 && ageInyears >= 25){
                age = ageInyears;
            }
            else if(ageInyears != null && lowerLimit != null && upperLimit != null && ageInyears >= lowerLimit && ageInyears <= upperLimit){
                age = ageInyears;
            }

            ret.put(ptId, new SimpleResult(age, this));
        }
        return ret;
    }
}
