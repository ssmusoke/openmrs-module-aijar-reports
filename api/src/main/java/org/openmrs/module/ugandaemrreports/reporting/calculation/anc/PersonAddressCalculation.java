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

import org.openmrs.Person;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;

import java.util.Collection;
import java.util.Map;

/**
 */
public class PersonAddressCalculation extends AbstractPatientCalculation {

    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        PersonService personService = Context.getPersonService();
        for(Integer ptId : cohort) {
            String villageParish = "";
            String village = "";
            String parish = "";
            String subcounty="";
            String district="";

            Person person = personService.getPerson(ptId);
            if(person.getPersonAddress() != null && person.getPersonAddress().getAddress5() != null) {
                village = person.getPersonAddress().getAddress5();
            }

            if(person.getPersonAddress() != null && person.getPersonAddress().getAddress4() != null) {
                parish = person.getPersonAddress().getAddress4();
            }
                villageParish = village+"\n" + parish;

            ret.put(ptId, new SimpleResult(villageParish, this));
        }
        return ret;
    }
}
