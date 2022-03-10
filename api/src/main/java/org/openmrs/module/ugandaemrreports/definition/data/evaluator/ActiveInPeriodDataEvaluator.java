package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ActiveInPeriodDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 */
@Handler(supports = ActiveInPeriodDataDefinition.class, order = 1)
public class ActiveInPeriodDataEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(ActiveInPeriodDataEvaluator.class);

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        ActiveInPeriodDataDefinition def = (ActiveInPeriodDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            log.info("Base Cohort for ActiveInPeriod is null or empty");
            System.out.println("Base Cohort for ActiveInPeriod is null or empty");
            return c;
        }

        String startDate = DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(def.getEndDate(), "yyyy-MM-dd");

        String query = "SELECT o.person_id, e.encounter_type,\n" +
                "       IF (MAX(last_encounter.last_encounter_date) > MAX(last_visit.return_visit_date), 1, IF(CURRENT_DATE() - MAX(last_encounter.last_encounter_date) < 28, 1, 0)) AS is_active,\n" +
                "       MAX(last_visit.return_visit_date) AS return_visit_date,\n" +
                "       MAX(last_encounter.last_encounter_date) as last_encounter_date\n" +
                "    FROM obs o\n" +
                "    INNER JOIN encounter e on o.encounter_id = e.encounter_id\n" +
                "    INNER JOIN encounter_type et on e.encounter_type = et.encounter_type_id AND et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'\n" +
                "    LEFT JOIN (SELECT MAX(e2.encounter_datetime) as last_encounter_date, e2.patient_id\n" +
                "                FROM encounter e2 INNER JOIN encounter_type et2 on e2.encounter_type = et2.encounter_type_id AND et2.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'\n" +
                "                AND e2.encounter_datetime < '" + startDate + "' GROUP BY e2.patient_id) last_encounter\n" +
                "        ON last_encounter.patient_id = o.person_id\n" +
                "    LEFT JOIN (SELECT MAX(IF(o2.concept_id = 5096, o2.value_datetime, NULL)) AS return_visit_date, o2.person_id\n" +
                "               FROM obs o2 INNER JOIN encounter e3 ON o2.encounter_id = e3.encounter_id INNER JOIN encounter_type et3 on e3.encounter_type = et3.encounter_type_id AND et3.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'\n" +
                "                   AND e3.encounter_datetime < '"+ endDate + "' GROUP BY o2.person_id) last_visit\n" +
                "              ON last_visit.person_id = o.person_id\n" +
                "    WHERE e.encounter_datetime BETWEEN '" + startDate + "' AND '" + endDate + "'\n" +
                "GROUP BY o.person_id";

        log.info("Running active in care report definition with startDate " + startDate + " and endDate " + endDate + " and SQL query " + query);
        System.out.println("Running active in care report definition with startDate " + startDate + " and endDate " + endDate + " and SQL query " + query);

        SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder(query);

        List<Object[]> results = evaluationService.evaluateToList(sqlQueryBuilder, context);

        for (Object[] row : results) {
            Obs o = new Obs();
            if (String.valueOf(row[2]).equals("1")) {
                o.setValueText("Y");
            } else {
                o.setValueText("N");
            }
            c.addData(Integer.valueOf(String.valueOf(row[0])), o);
            log.info("Adding active data for patient with id " + row[0]);
        }

        return c;
    }
}
