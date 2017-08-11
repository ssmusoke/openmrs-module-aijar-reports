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
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by Nicholas Ingosi on 7/21/17.
 */
public class CircumciserNameCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        Set<Integer> male = Filters.male(cohort, context);
        EncounterService service = Context.getEncounterService();
        EncounterRole role = service.getEncounterRoleByUuid("240b26f9-dd88-4172-823d-4a8bfeb7841f");
        CalculationResultMap encounter = Calculations.lastEncounter(MetadataUtils.existing(EncounterType.class, "244da86d-f80e-48fe-aba9-067f241905ee"), cohort, context);
        for(Integer ptId: male){
            String provider = "";
            Encounter enc = EmrCalculationUtils.encounterResultForPatient(encounter, ptId);
            if(enc != null){
                Set<EncounterProvider> providerSet = enc.getEncounterProviders();
                for(EncounterProvider encounterProvider:providerSet){
                    if(encounterProvider.getEncounterRole().equals(role)){
                        provider = encounterProvider.getProvider().getPerson().getPersonName().getFullName();
                    }
                }
            }
            ret.put(ptId, new SimpleResult(provider, this));
        }
        return ret;
    }
}