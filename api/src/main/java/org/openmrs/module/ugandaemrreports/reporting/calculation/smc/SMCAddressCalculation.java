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
        Set<Integer> male = Filters.male(cohort, context);
        for(Integer ptId:male) {
            Person person = Context.getPersonService().getPerson(ptId);
            String district = "";
            String parish = "";
            String subCounty = "";
            String vilZoneCell = "";
            String address = "";
            if(person.getPersonAddress() != null && person.getPersonAddress().getCountyDistrict() != null) {
                district = person.getPersonAddress().getCountyDistrict();
            }

            if(person.getPersonAddress() != null && person.getPersonAddress().getAddress4() != null) {
                parish = person.getPersonAddress().getAddress4();
            }

            if(person.getPersonAddress() != null && person.getPersonAddress().getAddress4() != null) {
                subCounty = person.getPersonAddress().getAddress3();
            }

            if(person.getPersonAddress() != null && person.getPersonAddress().getAddress4() != null) {
                vilZoneCell = person.getPersonAddress().getAddress5();
            }
            address = district+"\n"+parish+"\n"+subCounty+"\n"+vilZoneCell;

            ret.put(ptId, new SimpleResult(address, this));
        }
        return ret;
    }
}