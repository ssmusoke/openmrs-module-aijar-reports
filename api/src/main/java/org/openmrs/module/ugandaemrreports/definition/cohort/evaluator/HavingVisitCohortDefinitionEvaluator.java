package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.HavingVisitCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 */
@Handler(supports = {HavingVisitCohortDefinition.class})
public class HavingVisitCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        HavingVisitCohortDefinition cd = (HavingVisitCohortDefinition) cohortDefinition;
        SqlQueryBuilder q = new SqlQueryBuilder();
        String query = String.format("select distinct person_id from (select person_id,value_datetime,ADDDATE(value_datetime, interval 90 day) as dt from obs where concept_id = 5096  having dt between '%s' and '%s') D", DateUtil.formatDate(cd.getStartDate(),"yyyy-MM-dd"), DateUtil.formatDate(cd.getEndDate(),"yyyy-MM-dd"));
        q.append(query);
        Cohort c = new Cohort(evaluationService.evaluateToList(q, Integer.class, context));
        return new EvaluatedCohort(c, cd, context);
    }
}
