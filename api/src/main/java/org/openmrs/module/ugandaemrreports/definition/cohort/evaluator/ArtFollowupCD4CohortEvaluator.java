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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtFollowupCD4CohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 */
@Handler(supports = {ArtFollowupCD4CohortDefinition.class})
public class ArtFollowupCD4CohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ArtFollowupCD4CohortDefinition cd = (ArtFollowupCD4CohortDefinition) cohortDefinition;

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

        String query = "SELECT D.person_id\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     A.person_id,\n" +
                "     MAX(B.dt)            AS enc_date,\n" +
                "     MAX(B.value_numeric) AS val\n" +
                "   FROM\n" +
                "     (SELECT\n" +
                "        person_id,\n" +
                "        value_datetime\n" +
                "      FROM obs\n" +
                String.format("      WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') A\n", startDate, endDate) +
                "     INNER JOIN\n" +
                "     (SELECT\n" +
                "        o.person_id,\n" +
                "        o.value_numeric,\n" +
                "        e.encounter_datetime AS dt\n" +
                "      FROM obs o INNER JOIN encounter e ON (concept_id = 5497 AND e.encounter_id = o.encounter_id)) B\n" +
                "       ON (A.person_id = B.person_id)\n" +
                String.format("   WHERE B.dt <= DATE_ADD(A.value_datetime, INTERVAL %s %s)\n", cd.getPeriodDifference(), cd.getPeriod()) +
                "   GROUP BY A.person_id) D\n";

        if (!cd.getAllBaseCD4()) {
            query = query + "WHERE D.val <= 250";
        }

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            ret.addMember(pId);
        }

        return ret;
    }


}
