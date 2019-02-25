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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.DSDMUnsupressedVLCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.DSDMVirallySupressedCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;

/**
 */
@Handler(supports = {DSDMUnsupressedVLCohortDefinition.class})
public class DSDMUnsupressedVLEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        DSDMUnsupressedVLCohortDefinition cd = (DSDMUnsupressedVLCohortDefinition) cohortDefinition;

        String startDate = "";
        String endDate = "";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");


        String sql = "select person_id from obs where concept_id = (select concept_id from concept where concept.uuid='dc8d83e3-30ab-102d-86b0-7a5022ba4115') \n" +
                String.format("   and obs_datetime between '%s' and '%s' and value_numeric >=1000 and voided=0 group by person_id",startDate,endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(sql);

        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);

        HashSet<Integer> detectedSet = new HashSet<Integer>(results);
        ret.setMemberIds(detectedSet);
        return ret;
    }
}
