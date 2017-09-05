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
package org.openmrs.module.ugandaemrreports.reporting.calculation.pnc;

import org.openmrs.Concept;
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
 */
public class RtwRfwCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();
        CalculationResultMap referredToWhereMap = Calculations.lastObs(Dictionary.getConcept("dce015bb-30ab-102d-86b0-7a5022ba4115"), cohort, context);
        CalculationResultMap clinicReferredToMap = Calculations.lastObs(Dictionary.getConcept("7ac9966e-71a5-4c61-8998-e763fd94536f"), cohort, context);
        CalculationResultMap whereReferredFromMap = Calculations.lastObs(Dictionary.getConcept("dcdffef2-30ab-102d-86b0-7a5022ba4115"), cohort, context);
        CalculationResultMap clinicReferredFromMap = Calculations.lastObs(Dictionary.getConcept("03420802-8337-4546-9aa9-2ae023b2b47b"), cohort, context);
        for(Integer ptId: cohort){

            Obs referredToWhereObs = EmrCalculationUtils.obsResultForPatient(referredToWhereMap, ptId);
            Concept clinicReferredToAns = EmrCalculationUtils.codedObsResultForPatient(clinicReferredToMap, ptId);
            Obs whereReferredFromObs = EmrCalculationUtils.obsResultForPatient(whereReferredFromMap, ptId);
            Concept clinicReferredFromAns = EmrCalculationUtils.codedObsResultForPatient(clinicReferredFromMap, ptId);

            String referredToResults = "";
            String whereReferredFromResults = "";
            String overalResults = "";
            if(referredToWhereObs != null && clinicReferredToAns != null){
                if(clinicReferredToAns.equals(Dictionary.getConcept("6abc57f2-3718-42f6-84c4-4f71e921d590"))){
                    referredToResults=referredToWhereObs.getValueText()+"-"+1;
                }
                else if(clinicReferredToAns.equals(Dictionary.getConcept("37549919-5934-448e-b110-cc7868e73ff2"))){
                    referredToResults=referredToWhereObs.getValueText()+"-"+2;
                }
                else if(clinicReferredToAns.equals(Dictionary.getConcept("9028e51b-0c27-4b72-bde6-fadba72d1396"))){
                    referredToResults=referredToWhereObs.getValueText()+"-"+3;
                }
            }

            if(whereReferredFromObs != null && clinicReferredFromAns != null){
                if(clinicReferredFromAns.equals(Dictionary.getConcept("6abc57f2-3718-42f6-84c4-4f71e921d590"))){
                    whereReferredFromResults=whereReferredFromObs.getValueText()+"-"+1;
                }
                else if(clinicReferredFromAns.equals(Dictionary.getConcept("37549919-5934-448e-b110-cc7868e73ff2"))){
                    whereReferredFromResults=whereReferredFromObs.getValueText()+"-"+2;
                }
                else if(clinicReferredFromAns.equals(Dictionary.getConcept("9028e51b-0c27-4b72-bde6-fadba72d1396"))){
                    whereReferredFromResults=whereReferredFromObs.getValueText()+"-"+3;
                }
            }
            overalResults = referredToResults+"/"+whereReferredFromResults;
            ret.put(ptId, new SimpleResult(overalResults, this));
        }
        return ret;
    }
}