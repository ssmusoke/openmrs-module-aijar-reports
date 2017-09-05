package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.CD4;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CD4PatientDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 */
@Handler(supports = CD4PatientDataDefinition.class)
public class CD4PatientDataEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(CD4PatientDataEvaluator.class);

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        CD4PatientDataDefinition def = (CD4PatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        Date artDate = def.getStartDate();
        LocalDate beginningDate = StubDate.dateOf(artDate);

        String startDate = "";
        String endDate = "";

        List<Date> dates = getDates(beginningDate, def.getPeriod(), def.getPeriodInterval(), def.getPeriodDifference());

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
                "  INNER JOIN person p ON (p.person_id = B.person_id)\n" +
                "WHERE TIMESTAMPDIFF(YEAR, p.birthdate, A.encounter_datetime) >= 5 AND A.value_numeric IS NOT NULL\n" +
                "GROUP BY A.person_id";

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);

        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            Double cd4 = (Double) row[2];
            c.addData(pId, new CD4(cd4));
        }
        return c;
    }
}
