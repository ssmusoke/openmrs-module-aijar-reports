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
public class IycfMncCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap iyfcMap = Calculations.lastObs(Dictionary.getConcept("5d993591-9334-43d9-a208-11b10adfad85"), cohort, context);
        CalculationResultMap mncMap = Calculations.lastObs(Dictionary.getConcept("af7dccfd-4692-4e16-bd74-5ac4045bb6bf"), cohort, context);

        for(Integer ptId: cohort) {
            String resultsIyfc = "";
            String resultsMnc = "";
            String results = "";
            Concept iyfcResults = EmrCalculationUtils.codedObsResultForPatient(iyfcMap, ptId);
            Concept mncResults = EmrCalculationUtils.codedObsResultForPatient(mncMap, ptId);

            if(iyfcResults != null && iyfcResults.equals(Dictionary.getConcept(Dictionary.YES_CIEL))){
                resultsIyfc = "Y";
            }

            else if(iyfcResults != null && iyfcResults.equals(Dictionary.getConcept("1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))){
                resultsIyfc = "N";
            }

            if(mncResults != null && mncResults.equals(Dictionary.getConcept(Dictionary.YES_CIEL))){
                resultsMnc = "Y";
            }

            else if(mncResults != null && mncResults.equals(Dictionary.getConcept("1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))){
                resultsMnc = "N";
            }

            results = resultsIyfc + "\n" + resultsMnc;
            ret.put(ptId, new SimpleResult(results, this));
        }

        return ret;
    }
}
