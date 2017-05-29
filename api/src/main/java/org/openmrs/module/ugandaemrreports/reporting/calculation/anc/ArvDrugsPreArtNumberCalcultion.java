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

import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
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
 * Created by Nicholas Ingosi on 4/30/17.
 */
public class ArvDrugsPreArtNumberCalcultion extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        CalculationResultMap arvGivenMap = Calculations.lastObs(Dictionary.getConcept("a615f932-26ee-449c-8e20-e50a15232763"), cohort, context);
        PatientIdentifierType patientIdentifierType = Context.getPatientService().getPatientIdentifierTypeByUuid("e1731641-30ab-102d-86b0-7a5022ba4115");
        PatientService patientService = Context.getPatientService();

        for(Integer ptId: cohort){
            String results = "";
            String arvResults = "";
            String pIdentifier = "";

            Concept arvResultsConcept = EmrCalculationUtils.codedObsResultForPatient(arvGivenMap, ptId);
            Patient patient = patientService.getPatient(ptId);

            PatientIdentifier patientIdentifier = patient.getPatientIdentifier(patientIdentifierType);


            if(patientIdentifier != null){
               pIdentifier = patientIdentifier.getIdentifier();
            }

            results = arvResults+"\n"+pIdentifier;

            ret.put(ptId, new SimpleResult(results, this));



        }
        return ret;
    }
}
