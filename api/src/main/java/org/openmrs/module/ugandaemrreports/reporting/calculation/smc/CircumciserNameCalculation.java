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

import org.openmrs.EncounterRole;
import org.openmrs.Obs;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 7/21/17.
 */
public class CircumciserNameCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        EncounterService service = Context.getEncounterService();
        EncounterRole role = service.getEncounterRoleByUuid("240b26f9-dd88-4172-823d-4a8bfeb7841f");
        CalculationResultMap cadreMap = Calculations.lastObs(Dictionary.getConcept("911c5daf-e6ce-4255-abae-5ceb8fdcb5a2"), cohort, context);
        for(Integer ptId: cohort) {
            String name = "";
            Obs cadreMapObs = EmrCalculationUtils.obsResultForPatient(cadreMap, ptId);
            if(role != null && cadreMapObs != null){
                name = role.getName()+" - "+cadreMapObs.getValueText();
            }
            ret.put(ptId, new SimpleResult(name, this));
        }
        return ret;
    }
}