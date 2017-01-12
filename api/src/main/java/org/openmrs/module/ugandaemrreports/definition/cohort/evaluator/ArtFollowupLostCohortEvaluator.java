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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtFollowupLostCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 * Created by carapai on 20/04/2016.
 */
@Handler(supports = {ArtFollowupLostCohortDefinition.class})
public class ArtFollowupLostCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ArtFollowupLostCohortDefinition cd = (ArtFollowupLostCohortDefinition) cohortDefinition;

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

        String query = "SELECT\n" +
                "  C.person_id\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     A.person_id,\n" +
                "     MAX(B.value_datetime) AS dt,\n" +
                "     A.value_datetime\n" +
                "   FROM (SELECT\n" +
                "           person_id,\n" +
                "           value_datetime\n" +
                "         FROM obs\n" +
                String.format("         WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') A\n", startDate, endDate) +
                "     INNER JOIN\n" +
                "     (SELECT\n" +
                "        person_id,\n" +
                "        value_datetime\n" +
                "      FROM obs\n" +
                "      WHERE concept_id = 5096) B\n" +
                String.format("       ON (B.person_id = A.person_id AND B.value_datetime <= DATE_ADD(A.value_datetime, INTERVAL %s %s))\n", cd.getPeriodDifference(), cd.getPeriod()) +
                "   GROUP BY A.person_id) C INNER JOIN (SELECT\n" +
                "                                         D.person_id,\n" +
                "                                         MAX(E.encounter_datetime) AS max_encounter\n" +
                "                                       FROM (SELECT\n" +
                "                                               person_id,\n" +
                "                                               value_datetime\n" +
                "                                             FROM obs\n" +
                "                                             WHERE concept_id = 99161 AND\n" +
                String.format("                                                   value_datetime BETWEEN '%s' AND '%s') D\n", startDate, endDate) +
                "                                         INNER JOIN\n" +
                "                                         (SELECT\n" +
                "                                            patient_id,\n" +
                "                                            encounter_datetime\n" +
                "                                          FROM encounter) E\n" +
                "                                           ON (E.patient_id = D.person_id AND\n" +
                "                                               E.encounter_datetime <= DATE_ADD(D.value_datetime, INTERVAL 1 QUARTER))\n" +
                "                                       GROUP BY D.person_id) F\n";
        if (cd.getLostToFollowup()) {
            query = query + "    ON (F.person_id = C.person_id AND datediff(F.max_encounter, C.dt) >= 90)";
        } else {
            query = query + "    ON (F.person_id = C.person_id AND datediff(F.max_encounter, C.dt) BETWEEN 7 AND 89 )";

        }

        SqlQueryBuilder q = new SqlQueryBuilder(query);
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            ret.addMember(pId);
        }

        return ret;
    }


}
