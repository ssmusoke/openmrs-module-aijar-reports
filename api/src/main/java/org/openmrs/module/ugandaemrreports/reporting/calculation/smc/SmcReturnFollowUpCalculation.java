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
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.BooleanResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class SmcReturnFollowUpCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Integer visit = (map != null && map.containsKey("visit")) ? (Integer) map.get("visit") : null;
        EncounterType type = Context.getEncounterService().getEncounterTypeByUuid("d0f9e0b7-f336-43bd-bf50-0a7243857fa6");
        CalculationResultMap followUpEncounters = Calculations.allEncounters(type, cohort, context);
        CalculationResultMap encounter = Calculations.lastEncounter(MetadataUtils.existing(EncounterType.class, "244da86d-f80e-48fe-aba9-067f241905ee"), cohort, context);
        for(Integer ptId:cohort){
            boolean cameForVisit = false;
            Encounter circumcisedDate = EmrCalculationUtils.encounterResultForPatient(encounter, ptId);
            ListResult encounterList = (ListResult) followUpEncounters.get(ptId);
            List<Encounter> listResult = CalculationUtils.extractResultValues(encounterList);
            
          
            if(circumcisedDate != null && visit != null && listResult.size() > 0) {
            	for(Encounter enc: listResult) {
	         
	                if (visit > 7 && enc.getEncounterDatetime().after(circumcisedDate.getEncounterDatetime()) && enc.getEncounterDatetime().after(getDate(circumcisedDate.getEncounterDatetime(), 7))) {
	                    cameForVisit = true;
	                }
	                else if (visit == 2 && enc.getEncounterDatetime().after(circumcisedDate.getEncounterDatetime()) && enc.getEncounterDatetime().before(getDate(circumcisedDate.getEncounterDatetime(), 3))) {
	                    cameForVisit = true;
	                }
	                else if (visit == 7 && enc.getEncounterDatetime().after(circumcisedDate.getEncounterDatetime()) && enc.getEncounterDatetime().before(getDate(circumcisedDate.getEncounterDatetime(), 8)) && enc.getEncounterDatetime().after(getDate(circumcisedDate.getEncounterDatetime(), 2))) {
	                    cameForVisit = true;
	                }
            	}
            }
            ret.put(ptId, new BooleanResult(cameForVisit, this));
        }
        return ret;
    }

    private Date getDate(Date date, int days){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }
}