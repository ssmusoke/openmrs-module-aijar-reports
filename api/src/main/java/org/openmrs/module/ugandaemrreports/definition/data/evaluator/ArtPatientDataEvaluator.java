package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.ArtPatientData;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ArtPatientDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by carapai on 05/07/2016.
 */
@Handler(supports = ArtPatientDataDefinition.class)
public class ArtPatientDataEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(ArtPatientDataEvaluator.class);

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        ArtPatientDataDefinition def = (ArtPatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);
        String sql = "SELECT B.*\n" +
                "FROM\n" +
                "  (SELECT\n" +
                "     e.patient_id,\n" +
                "     e.encounter_datetime,\n" +
                "     o.value_datetime\n" +
                "   FROM encounter e INNER JOIN obs o\n" +
                "       ON (o.person_id = e.patient_id AND o.concept_id = 99161 AND YEAR(o.value_datetime) = 2014 AND\n" +
                "           MONTH(o.value_datetime) = 2 AND e.encounter_type = (SELECT et.encounter_type_id\n" +
                "                                                               FROM encounter_type et\n" +
                "                                                               WHERE\n" +
                "                                                                 et.uuid = '8d5b27bc-c2cc-11de-8d13-0010c6dffd0f'))) A\n" +
                "  INNER JOIN\n" +
                "  (SELECT\n" +
                "     e.encounter_id,\n" +
                "     o.person_id,\n" +
                "     o.concept_id,\n" +
                "     o.value_coded,\n" +
                "     DATE(e.encounter_datetime)    AS enc_date,\n" +
                "     DATE(o.value_datetime)        AS dt_date,\n" +
                "     o.value_numeric,\n" +
                "     o.value_text,\n" +
                "     YEAR(e.encounter_datetime)    AS enc_year,\n" +
                "     YEAR(o.value_datetime)        AS dt_year,\n" +
                "     MONTH(e.encounter_datetime)   AS enc_month,\n" +
                "     MONTH(o.value_datetime)       AS dt_month\n" +
                "   FROM obs o INNER JOIN encounter e ON (o.person_id = e.patient_id AND o.encounter_id = e.encounter_id AND\n" +
                "                                         e.encounter_type = (SELECT et.encounter_type_id\n" +
                "                                                             FROM encounter_type et\n" +
                "                                                             WHERE\n" +
                "                                                               et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f'))) B\n" +
                "    ON (A.patient_id = B.person_id AND B.enc_date >= A.value_datetime)";

        SqlQueryBuilder q = new SqlQueryBuilder(sql);

        List<Object[]> results = evaluationService.evaluateToList(q, context);


        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        for (Object[] row : results) {
            String encounterId = String.valueOf(row[0]);
            String patientId = String.valueOf(row[1]);
            String conceptId = String.valueOf(row[2]);
            String valueCoded = String.valueOf(row[3]);
            String encounterDate = String.valueOf(row[4]);
            String valueDatetime = String.valueOf(row[5]);
            String valueNumeric = String.valueOf(row[6]);
            String valueText = String.valueOf(row[7]);
            String encounterYear = String.valueOf(row[8]);
            String valueDatetimeYear = String.valueOf(row[9]);
            String encounterMonth = String.valueOf(row[10]);
            String valueDatetimeMonth = String.valueOf(row[11]);
            c.addData(Integer.valueOf(patientId), new ArtPatientData(encounterId, patientId, conceptId, valueCoded, encounterDate, valueDatetime, valueNumeric, valueText, encounterYear, valueDatetimeYear, encounterMonth, valueDatetimeMonth));
        }
        return c;
    }
}
