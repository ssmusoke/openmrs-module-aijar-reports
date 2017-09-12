package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ViralLoadCohortDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Handler(supports = {ViralLoadCohortDataDefinition.class})
public class ViralLoadCohortDataEvaluator
        implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(ViralLoadCohortDataEvaluator.class);
    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
            throws EvaluationException {
        ViralLoadCohortDataDefinition def = (ViralLoadCohortDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if ((context.getBaseCohort() != null) && (context.getBaseCohort().isEmpty())) {
            return c;
        }

        LocalDate endDate = StubDate.dateOf(def.getEndDate());

        String endDateString = endDate.toString("yyyy-MM-dd");


        String query = "SELECT\n" +
                "  C.person_id,\n" +
                String.format("  TIMESTAMPDIFF(MONTH, C.value_datetime, '%s')             AS months_btn_art_and_report,\n", endDateString) +
                "  TIMESTAMPDIFF(MONTH, C.value_datetime, B.value_datetime) AS months_btn_art_and_viral,\n" +
                "  IF(B.value_coded IS NULL, -1, B.value_coded)             AS 'value_coded',\n" +
                "  IF(B.value_numeric IS NULL, -1, B.value_numeric)         AS 'value_numeric'\n" +
                "FROM (SELECT\n" +
                "        person_id,\n" +
                "        value_datetime\n" +
                "      FROM obs\n" +
                "      WHERE concept_id = 99161\n" +
                "            AND voided = 0) C INNER JOIN\n" +
                "  (SELECT\n" +
                "     A.person_id,\n" +
                "     MAX(A.value_datetime) AS value_datetime,\n" +
                "     vlq.value_coded,\n" +
                "     vln.value_numeric\n" +
                "   FROM (SELECT\n" +
                "           person_id,\n" +
                "           encounter_id,\n" +
                "           value_datetime\n" +
                "         FROM obs\n" +
                "         WHERE\n" +
                String.format("           concept_id = 163023 AND voided = 0 AND value_datetime <= '%s'\n", endDateString) +
                "         UNION ALL\n" +
                "         SELECT\n" +
                "           person_id,\n" +
                "           encounter_id,\n" +
                "           obs_datetime AS 'value_datetime'\n" +
                "         FROM obs\n" +
                String.format("         WHERE concept_id = 856 AND voided = 0 AND obs_datetime <= '%s'\n", endDateString) +
                "               AND encounter_id\n" +
                "                   NOT IN (SELECT encounter_id\n" +
                "                           FROM obs\n" +
                "                           WHERE concept_id =\n" +
                String.format("                                 163023 AND voided = 0 AND value_datetime <= '%s')\n", endDateString) +
                "        ) A LEFT JOIN\n" +
                "     obs vlq ON (A.person_id = vlq.person_id AND\n" +
                "                 A.encounter_id = vlq.encounter_id AND\n" +
                "                 vlq.concept_id = 1305 AND vlq.voided = 0)\n" +
                "     LEFT JOIN obs vln ON (vln.person_id = A.person_id AND\n" +
                "                           vln.encounter_id =\n" +
                "                           A.encounter_id AND\n" +
                "                           vln.concept_id = 856 AND\n" +
                "                           vln.voided = 0)\n" +
                "   GROUP BY A.person_id) B\n" +
                "    ON (B.person_id = C.person_id)";


        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = this.evaluationService.evaluateToList(q, context);
        Integer i = 1;
        for (Object[] row : results) {
            Integer patientId = Integer.valueOf(String.valueOf(row[0]));
            Integer monthsBetweenReportDateAndArt = Integer.valueOf(String.valueOf(row[1]));
            Integer monthsBetweenArtAndViralLoad = Integer.valueOf(String.valueOf(row[2]));
            Integer valueCoded = Integer.valueOf(String.valueOf(row[3]));
            Double valueNumeric = Double.valueOf(String.valueOf(row[4]));
            c.addData(i, new ViralLoad(patientId, monthsBetweenReportDateAndArt, monthsBetweenArtAndViralLoad, valueCoded, valueNumeric));

            i = i + 1;
        }
        return c;
    }
}