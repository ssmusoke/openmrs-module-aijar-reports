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

import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 5/19/17.
 */
public class AgeFromEncounterDateCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Integer lower = (params != null && params.containsKey("lower")) ? (Integer) params.get("lower") : null;
        Integer upper = (params != null && params.containsKey("upper")) ? (Integer) params.get("upper") : null;

        CalculationResultMap encounterDate = calculate(new SMCEncounterDateCalculation(), cohort, context);
        for(Integer ptId:cohort) {
            Date dateAtRegistration;
            Integer ageAtRegistration = null;
            Integer age = null;
            Date birthDate = Context.getPersonService().getPerson(ptId).getBirthdate();
            if(encounterDate != null) {
                dateAtRegistration = EmrCalculationUtils.datetimeResultForPatient(encounterDate, ptId);
                if(dateAtRegistration != null && birthDate != null){
                    ageAtRegistration = ageInYears(birthDate, dateAtRegistration);
                    if(ageAtRegistration != null && ageAtRegistration < 1 && lower != null && lower == 0) {
                        age = ageInMonths(birthDate, dateAtRegistration);
                    }
                    else if(lower != null && upper != null & ageAtRegistration != null) {
                    	if(ageAtRegistration > 0 &&  lower == 0 && ageAtRegistration < 2 && upper == 2) {
                        age = ageAtRegistration;
                    	}
                    	else if(ageAtRegistration > 1 &&  lower == 2 && ageAtRegistration < 5 && upper == 5) {
                            age = ageAtRegistration;
                        	}
                    	else if(ageAtRegistration > 4 &&  lower == 5 && ageAtRegistration < 15 && upper == 15) {
                            age = ageAtRegistration;
                        	}
                    	else if(ageAtRegistration > 14 &&  lower == 15 && ageAtRegistration < 49 && upper == 49) {
                            age = ageAtRegistration;
                        	}
                    	else if(ageAtRegistration > 49 && lower == 49 && ageAtRegistration >= 49 && upper > 49){
                            age = ageAtRegistration;
                        }
                    }
                }
            }
            ret.put(ptId, new SimpleResult(age, this));
        }
        return ret;
    }

    private Integer ageInYears(Date dob, Date encounterDate){
        Integer age;
        Calendar calDOB = getCalendar(dob);
        Calendar calEncounterDate = getCalendar(encounterDate);
        age = calEncounterDate.get(Calendar.YEAR) - calDOB.get(Calendar.YEAR);
        return age;
    }

    private Integer ageInMonths(Date dob, Date encounterDate){
        Integer age;
        Calendar calDOB = getCalendar(dob);
        Calendar calEncounterDate = getCalendar(encounterDate);
        age = calEncounterDate.get(Calendar.MONTH) - calDOB.get(Calendar.MONTH);
        return age;
    }


    private Calendar getCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}