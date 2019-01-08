package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

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
import org.openmrs.module.ugandaemrreports.definition.data.definition.GoodAdherenceCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 */
@Handler(supports = {GoodAdherenceCohortDefinition.class})
public class AdherenceCohortdefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        GoodAdherenceCohortDefinition cd = (GoodAdherenceCohortDefinition) cohortDefinition;

        String startDate = "";
        String endDate = "";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");


        String sql = "select person_id,group_concat(value_coded separator ',') as value_coded from obs where concept_id= \n" +
                "            (select concept_id from concept where concept.uuid='dce03b2f-30ab-102d-86b0-7a5022ba4115') and \n" +
                String.format(   "     obs_datetime >= date_sub('%s', interval 6 month)  and obs_datetime < '%s' and voided=0 group by person_id",startDate,startDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(sql);

        List<Object[]> results = evaluationService.evaluateToList(q, context);
        HashSet<Integer> detectedSet = new HashSet<Integer>();
        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            String adherence =  row[1].toString();

            String[] adherences = adherence.split(",");
            int[] adherence_num_values = Arrays.stream(adherences).mapToInt(Integer::parseInt).toArray();

            boolean allEqual = Sets.newHashSet(Ints.asList(adherence_num_values)).size() == 1;

            if ( allEqual && adherence_num_values[0]==90156){
                detectedSet.add(pId);
            }


        }

        ret.setMemberIds(detectedSet);
        return ret;
    }
}