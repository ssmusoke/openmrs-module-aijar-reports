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
 * Created by Nicholas Ingosi on 5/16/17.
 */
public class IfoIycfMncCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap iyoMap = Calculations.lastObs(Dictionary.getConcept("dc9a00a2-30ab-102d-86b0-7a5022ba4115"), cohort, context);
        CalculationResultMap iyfcMap = Calculations.lastObs(Dictionary.getConcept("5d993591-9334-43d9-a208-11b10adfad85"), cohort, context);
        CalculationResultMap mncMap = Calculations.lastObs(Dictionary.getConcept("af7dccfd-4692-4e16-bd74-5ac4045bb6bf"), cohort, context);

        for(Integer ptId: cohort) {
            String feedingOption = "";
            String iyfcOption = "";
            String mncOption = "";
            String results = "";

            Concept iyoConceptAns = EmrCalculationUtils.codedObsResultForPatient(iyoMap, ptId);
            Concept iyfcConceptAns = EmrCalculationUtils.codedObsResultForPatient(iyfcMap, ptId);
            Concept mncConceptAns = EmrCalculationUtils.codedObsResultForPatient(mncMap, ptId);

            if(iyoConceptAns != null && iyoConceptAns.equals(Dictionary.getConcept("dcbd637e-30ab-102d-86b0-7a5022ba4115"))) {
                feedingOption = "EBF";
            }
            else if(iyoConceptAns != null && iyoConceptAns.equals(Dictionary.getConcept("40fdb5b6-e8ac-424d-988c-f2f2937348db"))) {
                feedingOption = "RF";
            }

            else if(iyoConceptAns != null && iyoConceptAns.equals(Dictionary.getConcept("dcd5487d-30ab-102d-86b0-7a5022ba4115"))) {
                feedingOption = "MF";
            }
            //loop through
            if(iyfcConceptAns != null && iyfcConceptAns.equals(Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
                iyfcOption = "Y";
            }
            else if(iyfcConceptAns != null && iyfcConceptAns.equals(Dictionary.getConcept("1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
                iyfcOption = "N";
            }
            //loop through
            if(mncConceptAns != null && mncConceptAns.equals(Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
                mncOption = "Y";
            }
            else if(mncConceptAns != null && mncConceptAns.equals(Dictionary.getConcept("1066AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
                mncOption = "N";
            }

            results =feedingOption+"\n"+iyfcOption+"\n"+mncOption;
            ret.put(ptId, new SimpleResult(results, this));
        }
        return ret;
    }
}
