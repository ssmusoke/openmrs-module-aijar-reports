package org.openmrs.module.aijarreports.definition.cohort.evaluation;

import org.openmrs.Cohort;
import org.openmrs.Encounter;
import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.definition.cohort.definition.InEncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

/**
 * Created by carapai on 20/04/2016.
 */
@Handler(supports = {InEncounterCohortDefinition.class})
public class InEncounterCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        InEncounterCohortDefinition cd = (InEncounterCohortDefinition) cohortDefinition;

        Date beginning = DateUtil.getStartOfMonth(cd.getStartDate());
        Date ending = DateUtil.getEndOfMonth(cd.getStartDate());

        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select(new String[]{"distinct e.patient.patientId"});
        q.from(Encounter.class, "e");
        q.whereEqual("e.voided", false);
        q.whereIn("e.encounterType", cd.getEncounterTypes());
        q.whereBetweenInclusive("e.encounterDatetime", beginning, ending);
        Cohort c = new Cohort(evaluationService.evaluateToList(q, Integer.class, context));
        return new EvaluatedCohort(c, cd, context);
    }
}
