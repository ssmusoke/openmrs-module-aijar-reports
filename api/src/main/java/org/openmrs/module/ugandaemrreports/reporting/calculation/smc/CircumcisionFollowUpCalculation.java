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
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 */
public class CircumcisionFollowUpCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {

        CalculationResultMap ret = new CalculationResultMap();
        Set<Integer> male = Filters.male(cohort, context);
        Integer visit = (map != null && map.containsKey("visit")) ? (Integer) map.get("visit") : null;
        EncounterType type = Context.getEncounterService().getEncounterTypeByUuid("d0f9e0b7-f336-43bd-bf50-0a7243857fa6");

        CalculationResultMap followUpEncounters = Calculations.allEncounters(type, male, context);
        CalculationResultMap encounterDate = calculate(new SMCEncounterDateCalculation(), male, context);

        for(Integer ptId: male) {
            String value = "N";
            Date circumDate = EmrCalculationUtils.datetimeResultForPatient(encounterDate, ptId);
            ListResult encounterList = (ListResult) followUpEncounters.get(ptId);
            List<Encounter> listResult = CalculationUtils.extractResultValues(encounterList);

            if(listResult.size() == 0 && circumDate != null && visit != null){
                if(visit > 7 && today().after(getDate(circumDate, 8))){
                    value = "N";
                }
                else if(visit == 7 && today().after(getDate(circumDate, 2)) && today().before(getDate(circumDate, 8))) {
                   value = "N";
                }

                else if(visit == 2 && today().after(circumDate) && today().before(getDate(circumDate, 3))) {
                    value = "N";
                }

            }
            else if(listResult.size() > 0 && circumDate != null && visit != null){

                for(Encounter enc: listResult) {
                    if(visit > 7  && enc.getEncounterDatetime() != null && enc.getEncounterDatetime().after(getDate(circumDate, 7))){
                        value = "Y" + "\r\n" + formatDate(enc.getEncounterDatetime());
                        break;
                    }
                    else if(visit > 7  && enc.getEncounterDatetime() != null && today().after(getDate(enc.getEncounterDatetime(), 8))){
                        value = "N";
                    }
                    else if(visit == 2 && enc.getEncounterDatetime() != null) {

                        if (enc.getEncounterDatetime().after(circumDate) && enc.getEncounterDatetime().before(getDate(circumDate, 3))) {
                            value = "Y" + "\r\n" + formatDate(enc.getEncounterDatetime());
                            break;
                        } else if (today().after(circumDate) && today().before(getDate(enc.getEncounterDatetime(), 3))) {
                            value = "N";
                        } else if (enc.getEncounterDatetime().after(circumDate) && enc.getEncounterDatetime().after(getDate(circumDate, 2)) && enc.getEncounterDatetime().before(getDate(circumDate, 7))) {
                            value = "N";
                        }
                    }
                    else if(visit == 7 && enc.getEncounterDatetime() != null){
                        if ((enc.getEncounterDatetime().after(getDate(circumDate, 2))) && (enc.getEncounterDatetime().before(getDate(circumDate, 8)))) {
                            value = "Y" + "\r\n" + formatDate(enc.getEncounterDatetime());
                            break;

                        } else if (today().after(getDate(enc.getEncounterDatetime(), 2)) && today().before(getDate(enc.getEncounterDatetime(), 8))) {
                            value = "N";
                        } else if (enc.getEncounterDatetime().after(getDate(circumDate, 7))) {
                            value = "N";
                        }
                    }

                }

            }
            ret.put(ptId, new SimpleResult(value, this));
        }
        return ret;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        return date == null?"":dateFormatter.format(date);
    }
    private Date today(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        return c.getTime();
    }

    private Date getDate(Date date, int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
}