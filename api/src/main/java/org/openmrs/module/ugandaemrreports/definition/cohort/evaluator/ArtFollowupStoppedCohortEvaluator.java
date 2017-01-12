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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtFollowupStoppedCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 * Created by carapai on 20/04/2016.
 */
@Handler(supports = {ArtFollowupStoppedCohortDefinition.class})
public class ArtFollowupStoppedCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ArtFollowupStoppedCohortDefinition cd = (ArtFollowupStoppedCohortDefinition) cohortDefinition;

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
                "FROM\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     value_datetime\n" +
                "   FROM obs\n" +
                String.format("   WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') A\n", startDate, endDate) +
                "  INNER JOIN\n" +
                "  (SELECT\n" +
                "     o.person_id,\n" +
                "     MAX(e.encounter_datetime) AS dt\n" +
                "   FROM\n" +
                "     obs o INNER JOIN (SELECT\n" +
                "                         person_id,\n" +
                "                         value_datetime\n" +
                "                       FROM obs\n" +
                String.format("                       WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') B\n", startDate, endDate) +
                "       ON (o.person_id = B.person_id)\n" +
                "     INNER JOIN encounter e\n" +
                "       ON (e.encounter_id = o.encounter_id AND o.concept_id = 90315 AND\n" +
                String.format("           e.encounter_datetime <= DATE_ADD(B.value_datetime, INTERVAL %s %s))\n", cd.getPeriodDifference(), cd.getPeriod()) +
                "   GROUP BY o.person_id) C\n" +
                "    ON (A.person_id = C.person_id)\n" +
                "  INNER JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     value_datetime\n" +
                "   FROM obs\n" +
                "   WHERE concept_id = 99084) D\n" +
                "    ON (D.person_id = C.person_id AND D.value_datetime <= C.dt)";

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
