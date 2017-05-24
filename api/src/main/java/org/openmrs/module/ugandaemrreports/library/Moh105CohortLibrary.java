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
package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.ANCVisit4PlusCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.ANCVisitStages;
import org.openmrs.module.ugandaemrreports.reporting.cohort.definition.CalculationCohortDefinition;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by Nicholas Ingosi on 5/23/17.
 */
@Component
public class Moh105CohortLibrary {

    @Autowired
    CommonCohortDefinitionLibrary definitionLibrary;

    public CohortDefinition femaleAndHasAncVisit(Double stage){

        CalculationCohortDefinition calculationCohortDefinition = new CalculationCohortDefinition("ancVist "+stage, new ANCVisitStages());
        calculationCohortDefinition.addParameter(new Parameter("onDate", "On Date", Date.class));
        calculationCohortDefinition.addCalculationParameter("stage", stage);

        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.setName("Female and has ANC "+stage+" Visit");
        cd.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        cd.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        cd.addSearch("female", ReportUtils.map(definitionLibrary.females(), ""));
        cd.addSearch("ancVist", ReportUtils.map(calculationCohortDefinition, "onDate=${onOrBefore}"));
        cd.setCompositionString("female AND ancVist");
        return cd;
    }

    public CohortDefinition femaleAndHas4PlusAncVisit() {
        CalculationCohortDefinition cd = new CalculationCohortDefinition("anc4thPlus", new ANCVisit4PlusCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
}
