package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtPregnantCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 */
@Handler(supports = {ArtPregnantCohortDefinition.class})
public class ArtPregnantCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ArtPregnantCohortDefinition cd = (ArtPregnantCohortDefinition) cohortDefinition;

        Date artDate = cd.getStartDate();
        LocalDate beginningDate = StubDate.dateOf(artDate);

        List<Date> dates = getDates(beginningDate, cd.getPeriod(), cd.getPeriodInterval(), cd.getPeriodDifference());

        String startDate = "";
        String endDate = "";

        if (dates.size() == 1) {
            startDate = DateUtil.formatDate(dates.get(0), "yyyy-MM-dd");
            endDate = startDate;
        } else if (dates.size() > 1) {
            startDate = DateUtil.formatDate(dates.get(0), "yyyy-MM-dd");
            endDate = DateUtil.formatDate(dates.get(1), "yyyy-MM-dd");
        }

        String query = "SELECT A.person_id\n" +
                "FROM (SELECT person_id\n" +
                "      FROM obs\n" +
                String.format("      WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') A\n", startDate, endDate) +
                "  INNER JOIN (SELECT person_id\n" +
                "              FROM obs\n" +
                "              WHERE concept_id IN (99072, 99603) AND value_coded = 1065) B ON (A.person_id = B.person_id)";
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);
        HashSet<Integer> detectedSet = new HashSet<Integer>(results);
        ret.setMemberIds(detectedSet);
        return ret;
    }


}
