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

import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nicholas Ingosi  on 5/22/17.
 */
public class SMCAddressCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        String address = (map != null && map.containsKey("address")) ? (String) map.get("address") : null;
        Set<Integer> male = Filters.male(cohort, context);
        for(Integer ptId:male) {
            Person person = Context.getPersonService().getPerson(ptId);
            String addressValue = "";
            if(address != null && person.getPersonAddress() != null) {
	            if(address.equals("district") && person.getPersonAddress().getCountyDistrict() != null) {
	            	addressValue = person.getPersonAddress().getCountyDistrict();
	            }
	            else if(address.equals("subcounty") && person.getPersonAddress().getAddress3() != null) {
	            	addressValue = person.getPersonAddress().getAddress3();
	            }
	            else if(address.equals("village") && person.getPersonAddress().getAddress5() != null) {
	            	addressValue = person.getPersonAddress().getAddress5();
	            } 
            }

            ret.put(ptId, new SimpleResult(addressValue, this));
        }
        return ret;
    }
}