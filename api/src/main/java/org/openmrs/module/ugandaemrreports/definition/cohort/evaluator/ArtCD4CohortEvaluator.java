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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtCD4CohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 */
@Handler(supports = {ArtCD4CohortDefinition.class})
public class ArtCD4CohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ArtCD4CohortDefinition cd = (ArtCD4CohortDefinition) cohortDefinition;

        Date artDate = cd.getStartDate();
        LocalDate beginningDate = StubDate.dateOf(artDate);

        String startDate = "";
        String endDate = "";

        List<Date> dates = getDates(beginningDate, cd.getPeriod(), cd.getPeriodInterval(), cd.getPeriodDifference());

        if (dates.size() == 1) {
            startDate = DateUtil.formatDate(dates.get(0), "yyyy-MM-dd");
            endDate = startDate;
        } else if (dates.size() > 1) {
            startDate = DateUtil.formatDate(dates.get(0), "yyyy-MM-dd");
            endDate = DateUtil.formatDate(dates.get(1), "yyyy-MM-dd");
        }

        String query = "SELECT\n" +
                "  A.person_id,\n" +
                "  MAX(A.encounter_datetime),\n" +
                "  A.value_numeric\n" +
                "FROM (SELECT\n" +
                "        o.person_id,\n" +
                "        e.encounter_datetime,\n" +
                "        o.value_numeric\n" +
                "      FROM\n" +
                "        obs o INNER JOIN encounter e\n" +
                "          ON (e.encounter_id = o.encounter_id AND (o.concept_id IN (5497, 730) OR o.person_id IN (SELECT person_id\n" +
                "                                                                                                      FROM obs\n" +
                "                                                                                                      WHERE concept_id =\n" +
                "                                                                                                            99071)))) A\n" +
                "  INNER JOIN\n" +
                "  (SELECT\n" +
                "     person_id,\n" +
                "     value_datetime\n" +
                "   FROM obs\n" +
                String.format("   WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') B\n", startDate, endDate) +
                "    ON (B.person_id = A.person_id AND A.encounter_datetime <= B.value_datetime)\n" +
                "  INNER JOIN person p ON (p.person_id = B.person_id)\n";

        if (cd.getAllBaseCD4()) {
            query = query + "WHERE TIMESTAMPDIFF(YEAR, p.birthdate, A.encounter_datetime) >= 5\n";
        } else {
            query = query + "WHERE A.value_numeric <= 250 AND TIMESTAMPDIFF(YEAR, p.birthdate, A.encounter_datetime) >= 5\n";
        }

        query = query + "GROUP BY A.person_id";

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
