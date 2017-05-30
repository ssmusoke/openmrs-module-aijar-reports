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
import org.openmrs.PatientIdentifier;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.utils.CalculationUtils;

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
        PatientIdentifierDataDefinition dfn = new PatientIdentifierDataDefinition("preARTNo", Context.getPatientService().getPatientIdentifierTypeByUuid("e1731641-30ab-102d-86b0-7a5022ba4115"));
        CalculationResultMap identifier = CalculationUtils.evaluateWithReporting(dfn, cohort, params, null, context);

        for(Integer ptId: cohort){
            String results = "";
            String arvResults = "";
            String pIdentifier = "";

            Concept arvResultsConcept = EmrCalculationUtils.codedObsResultForPatient(arvGivenMap, ptId);
            PatientIdentifier patientIdentifier = EmrCalculationUtils.resultForPatient(identifier, ptId);

            if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("026e31b7-4a26-44d0-8398-9a41c40ff7d3"))){
                arvResults = "ART";
            }
           else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("2aa7d442-6cbb-4609-9dd3-bc2ad6f05016"))){
                arvResults = "ARTK";
            }

            else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("2c000b41-f9d7-40c1-8de0-bce91dbae932"))){
                arvResults = "ART✔";
            }
            else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("bbc63761-0741-4583-9396-a34d3a18601c"))){
                arvResults = "ARTK✔";
            }
            else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("dc9b0596-30ab-102d-86b0-7a5022ba4115"))){
                arvResults = "NA";
            }

            if(patientIdentifier != null){
               pIdentifier = patientIdentifier.getIdentifier();
            }

            results = arvResults+"\n"+pIdentifier;

            ret.put(ptId, new SimpleResult(results, this));



        }
        return ret;
    }
}
