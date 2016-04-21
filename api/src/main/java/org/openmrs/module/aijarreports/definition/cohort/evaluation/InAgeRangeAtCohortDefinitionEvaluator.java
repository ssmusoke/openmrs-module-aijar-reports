/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.aijarreports.definition.cohort.evaluation;

import org.openmrs.Cohort;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.definition.cohort.definition.InAgeRangeAtCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.AgeRange;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

@Handler(supports = {InAgeRangeAtCohortDefinition.class})
public class InAgeRangeAtCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {

        InAgeRangeAtCohortDefinition cd = (InAgeRangeAtCohortDefinition) cohortDefinition;

        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("p.patientId, p.birthdate");
        q.from(Patient.class, "p");
        q.whereEqual("p.voided", false);
        Cohort c = new Cohort();

        AgeRange ageRange = new AgeRange(cd.getMinAge(), cd.getMinAgeUnit(), cd.getMaxAge(), cd.getMaxAgeUnit(), "");

        for (Object[] row : evaluationService.evaluateToList(q, context)) {
            Integer pId = (Integer) row[0];
            Date bd = (Date) row[1];
            Age age = new Age(bd);
            if (ageRange.isInRange(age)) {
                c.addMember(pId);
            }
        }

        return new EvaluatedCohort(c, cd, context);
    }
}