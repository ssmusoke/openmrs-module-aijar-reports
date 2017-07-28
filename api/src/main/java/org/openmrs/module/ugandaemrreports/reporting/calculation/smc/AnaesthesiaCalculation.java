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

import org.openmrs.Obs;
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
 * Created by Nicholas Ingosi on 7/17/17.
 */
public class AnaesthesiaCalculation extends AbstractPatientCalculation{
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        CalculationResultMap local = Calculations.lastObs(Dictionary.getConcept("db9f397b-0632-4e43-b022-585ce3a48656"), cohort, context);
        CalculationResultMap anticeptic = Calculations.lastObs(Dictionary.getConcept("941571fc-9264-4675-b200-1c1fdb78a1c7"), cohort, context);
        CalculationResultMap other = Calculations.lastObs(Dictionary.getConcept("1d8d0ca2-8973-4838-8955-3d09340044a8"), cohort, context);

        for(Integer ptId:cohort) {
            String results = "";
            Obs localObs = EmrCalculationUtils.obsResultForPatient(local, ptId);
            Obs anticepticObs = EmrCalculationUtils.obsResultForPatient(anticeptic, ptId);
            Obs otherObs = EmrCalculationUtils.obsResultForPatient(other, ptId);

            if(localObs != null && localObs.getValueCoded().equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))){
                results = "1";
            }
            else if(anticepticObs != null && anticepticObs.getValueCoded().equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))){
                results = "2";
            }
            else if(otherObs != null){
                results = "4";
            }

            ret.put(ptId, new SimpleResult(results, this));
        }

        return ret;
    }
}