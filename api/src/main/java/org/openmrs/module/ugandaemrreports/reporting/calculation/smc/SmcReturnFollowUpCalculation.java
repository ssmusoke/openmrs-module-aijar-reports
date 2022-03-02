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
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.BooleanResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

import java.time.Duration;
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
        EncounterType type = Context.getEncounterService().getEncounterTypeByUuid("244da86d-f80e-48fe-aba9-067f241905ee");
        CalculationResultMap followUpEncounters = Calculations.allEncounters(type, cohort, context);
        CalculationResultMap encounter = Calculations.lastEncounter(MetadataUtils.existing(EncounterType.class, "244da86d-f80e-48fe-aba9-067f241905ee"), cohort, context);
        CalculationResultMap followUpDateObservation = Calculations.lastObs(Dictionary.getConcept("785aceb7-2bd5-4955-ba69-627ca1befa64"), cohort, context);
        for (Integer ptId : cohort) {
            boolean cameForVisit = false;
            Encounter circumcisedDate = EmrCalculationUtils.encounterResultForPatient(encounter, ptId);
            Date followUpDate = EmrCalculationUtils.datetimeObsResultForPatient(followUpDateObservation, ptId);

            ListResult encounterList = (ListResult) followUpEncounters.get(ptId);
            List<Encounter> listResult = CalculationUtils.extractResultValues(encounterList);

            for (Encounter encounter1 : listResult) {

                Integer numberofDays = DateUtil.getDaysBetween(circumcisedDate.getEncounterDatetime(), followUpDate);

                if (followUpDate != null) {
                    if (visit != null && visit <= 2 && numberofDays <= 2) {
                        cameForVisit = true;
                    } else if (visit != null && visit <= 7 && numberofDays <= 7) {
                        cameForVisit = true;
                    } else if (visit != null && visit <= 14 && numberofDays <= 14) {
                        cameForVisit = true;
                    } else {
                        cameForVisit = true;
                    }
                }
                ret.put(ptId, new BooleanResult(cameForVisit, this));
            }

        }

        return ret;
    }
}