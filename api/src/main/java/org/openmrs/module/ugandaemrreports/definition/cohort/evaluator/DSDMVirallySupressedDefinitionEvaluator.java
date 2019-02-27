package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.DSDMAdherenceCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.DSDMVirallySupressedCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 */
@Handler(supports = {DSDMVirallySupressedCohortDefinition.class})
public class DSDMVirallySupressedDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        DSDMVirallySupressedCohortDefinition cd = (DSDMVirallySupressedCohortDefinition) cohortDefinition;

        String startDate = "";
        String endDate = "";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");


        String sql = "select o.person_id from obs  o inner  join (SELECT person_id, max(obs_datetime) as max_date from obs \n " +
                " where concept_id= (select concept_id from concept where concept.uuid='dc8d83e3-30ab-102d-86b0-7a5022ba4115')  \n" +
                String.format(   "   and obs_datetime >= date_sub('%s', interval 12 month)  and obs_datetime < '%s' and value_numeric < 1000 and voided=0 group by person_id ) t on t.person_id =o.person_id  \n ",startDate,startDate) +
                " where o.obs_datetime = max_date and  concept_id= (select concept_id from concept where concept.uuid='dc8d83e3-30ab-102d-86b0-7a5022ba4115') and o.voided= 0 group by o.person_id ";

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(sql);

        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);

        HashSet<Integer> detectedSet = new HashSet<Integer>(results);
        ret.setMemberIds(detectedSet);
        return ret;
    }
}
