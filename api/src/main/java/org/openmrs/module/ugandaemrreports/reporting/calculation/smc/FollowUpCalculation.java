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

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.ListResult;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 7/17/17.
 */
public class FollowUpCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {

        CalculationResultMap ret = new CalculationResultMap();
        Integer visit = (map != null && map.containsKey("visit")) ? (Integer) map.get("visit") : null;
        EncounterType type = Context.getEncounterService().getEncounterTypeByUuid("d0f9e0b7-f336-43bd-bf50-0a7243857fa6");

        CalculationResultMap encounters = Calculations.allEncounters(type, cohort, context);
        CalculationResultMap encounterDate = calculate(new SMCEncounterDateCalculation(), cohort, context);

        for(Integer ptId: cohort) {
            String date = "";
            String value = "";
            Date dateReturned = null;
            Date circumDate = EmrCalculationUtils.datetimeResultForPatient(encounterDate, ptId);
            ListResult encounterList = (ListResult) encounters.get(ptId);
            List<Encounter> listResult = CalculationUtils.extractResultValues(encounterList);
            //calculate the date suppossed to be seen
            if(circumDate != null && visit != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(circumDate);
                c.add(Calendar.DATE, visit);
                dateReturned = c.getTime();
            }
            if(listResult.size() > 0){
                for(Encounter enc: listResult) {
                    if(visit != null && visit > 8 && circumDate != null ){
                        value = "Y"+"\n"+formatDate(enc.getDateCreated());
                        break;
                    }
                    else if(visit != null && visit <= 8 && circumDate != null && dateReturned != null && enc.getDateCreated().after(circumDate) && enc.getDateCreated().before(dateReturned)) {
                        value = "Y"+"\n"+formatDate(enc.getDateCreated());
                    }
                }

            }
            ret.put(ptId, new SimpleResult(value, this));
        }
        return ret;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd, MMM, yyyy");
        return date == null?"":dateFormatter.format(date);
    }
}
