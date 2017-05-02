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
public class FolicAcidCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap folicAcidMap = Calculations.lastObs(Dictionary.getConcept("8c346216-c444-4528-a174-5139922218ed"), cohort, context);
        CalculationResultMap folicAmountMap = Calculations.lastObs(Dictionary.getConcept("961ff308-bc19-4ae4-ba11-fe29157d20f9"), cohort, context);

        for(Integer ptId: cohort) {
            String folicGiven = "";
            String folicAmountGiven = "";
            Concept folicConcept = EmrCalculationUtils.codedObsResultForPatient(folicAcidMap, ptId);
            Double folicAmount = EmrCalculationUtils.numericObsResultForPatient(folicAmountMap, ptId);
            if(folicAmount != null) {
                folicAmountGiven = folicAmount.toString();
            }
            if(folicConcept != null && folicConcept.equals(Dictionary.getConcept(Dictionary.YES_CIEL))) {
                folicGiven = "✔ "+folicAmountGiven;
            }

            ret.put(ptId, new SimpleResult(folicGiven, this));
        }

        return ret;
    }
}
