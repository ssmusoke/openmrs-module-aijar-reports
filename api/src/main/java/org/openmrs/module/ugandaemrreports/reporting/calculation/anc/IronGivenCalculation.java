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
 * Created by Nicholas Ingosi on 5/2/17.
 */
public class IronGivenCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {

        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap ironMap = Calculations.lastObs(Dictionary.getConcept("315825e8-8ba4-4551-bdd1-aa4e02a36639"), cohort, context);
        CalculationResultMap ironAmountMap = Calculations.lastObs(Dictionary.getConcept("c02d9887-6a46-43cc-9495-5ec034dc05d6"), cohort, context);

        for(Integer ptId: cohort) {
            String ironGiven = "";
            String ironAmountGiven = "";
            Concept ironConcept = EmrCalculationUtils.codedObsResultForPatient(ironMap, ptId);
            Double ironAmount = EmrCalculationUtils.numericObsResultForPatient(ironAmountMap, ptId);
            if(ironAmount != null) {
                ironAmountGiven = ironAmount.toString();
            }
            if(ironConcept != null && ironConcept.equals(Dictionary.getConcept(Dictionary.YES_CIEL))) {
                ironGiven = "✔ "+ironAmountGiven;
            }

            ret.put(ptId, new SimpleResult(ironGiven, this));
        }

        return ret;
    }
}
