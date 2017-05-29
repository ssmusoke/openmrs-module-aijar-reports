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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by Nicholas Ingosi on 4/30/17.
 */
public class WhoCd4VLCalculation extends AbstractPatientCalculation {
    @Override
    public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params, PatientCalculationContext context) {
        CalculationResultMap ret = new CalculationResultMap();

        CalculationResultMap whomap = Calculations.lastObs(Dictionary.getConcept("dcdff274-30ab-102d-86b0-7a5022ba4115"), cohort, context);
        CalculationResultMap cd4Map = Calculations.lastObs(Dictionary.getConcept("dcbcba2c-30ab-102d-86b0-7a5022ba4115"), cohort, context);
        CalculationResultMap vlMap = Calculations.lastObs(Dictionary.getConcept("dc8d83e3-30ab-102d-86b0-7a5022ba4115"), cohort, context);
        CalculationResultMap cd4DateMap = Calculations.lastObs(Dictionary.getConcept("159376AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), cohort, context);
        CalculationResultMap vlDateMap = Calculations.lastObs(Dictionary.getConcept("0b434cfa-b11c-4d14-aaa2-9aed6ca2da88"), cohort, context);

        for(Integer ptId : cohort) {
            String whoStage = "";
            String cd4Count = "";
            String cd4CountDate = "";
            String vlCount = "";
            String vlCountDate = "";
            String results = "";
            String cd4CountAndDate = "";
            String vlCountAndDate = "";

            Concept whoResultsConcept = EmrCalculationUtils.codedObsResultForPatient(whomap, ptId);
            Double  cdResultsValue = EmrCalculationUtils.numericObsResultForPatient(cd4Map, ptId);
            Double  vlResultValue = EmrCalculationUtils.numericObsResultForPatient(vlMap, ptId);
            Date cd4Date = EmrCalculationUtils.datetimeObsResultForPatient(cd4DateMap, ptId);
            Date vlDate = EmrCalculationUtils.datetimeObsResultForPatient(vlDateMap, ptId);

            if(cdResultsValue != null){
                cd4Count = cdResultsValue.toString();
            }

            if(vlResultValue != null){
                vlCount = vlResultValue.toString();
            }

            if(cd4Date != null){
                cd4CountDate = formatDate(cd4Date);
            }

            if(vlDate != null){
                vlCountDate = formatDate(vlDate);
            }

            //start calculating the who stagings
            //1,2,3,4,T1,T2,T3,T4
            if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda2bc2-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "1";
            }
            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda3251-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "2";
            }
            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda3663-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "3";
            }
            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda3a80-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "4";
            }
            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd25e735-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "T1";
            }
            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd2666a5-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "T2";
            }

            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd266d64-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "T3";
            }
            else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd269c18-30ab-102d-86b0-7a5022ba4115"))) {
                whoStage = "T4";
            }

            cd4CountAndDate = cd4Count+" "+cd4CountDate;
            vlCountAndDate = vlCount+" "+vlCountDate;

            results = whoStage+"\n"+cd4CountAndDate+"\n"+vlCountAndDate;

            ret.put(ptId, new SimpleResult(results, this));
        }

        return ret;
    }

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
        if (date == null) {
            return "";
        }

        return dateFormatter.format(date);
    }
}
