package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.NonSuppresssedViralLoadsDataDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 */
@Handler(supports = {NonSuppresssedViralLoadsDataDefinition.class})
public class NonSuprressedViralLoadCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        NonSuppresssedViralLoadsDataDefinition cd = (NonSuppresssedViralLoadsDataDefinition) cohortDefinition;

        String startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");

//        Get all clients with a viral Load Above 999

        String q=  String.format("select e.patient_id from encounter e inner join obs o  on o.encounter_id=e.encounter_id " +
                " where o.concept_id =856 and o.value_numeric >999 and e.encounter_datetime between '%s' and '%s' and o.voided =0",startDate,endDate);

        SqlQueryBuilder query = new SqlQueryBuilder(q);

        List<Integer> pIds = evaluationService.evaluateToList(query, Integer.class, context);

        ret.getMemberIds().addAll(pIds);
        return ret;
    }
}
