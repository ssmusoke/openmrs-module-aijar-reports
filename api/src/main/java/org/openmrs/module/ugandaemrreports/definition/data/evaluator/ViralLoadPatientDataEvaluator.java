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
import org.openmrs.module.ugandaemrreports.definition.data.definition.ViralLoadPatientDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Handler(supports = {ViralLoadPatientDataDefinition.class})
public class ViralLoadPatientDataEvaluator
        implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(ViralLoadPatientDataEvaluator.class);
    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
            throws EvaluationException {
        ViralLoadPatientDataDefinition def = (ViralLoadPatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if ((context.getBaseCohort() != null) && (context.getBaseCohort().isEmpty())) {
            return c;
        }

        LocalDate startDate = StubDate.dateOf(def.getStartDate());
        LocalDate endDate = StubDate.dateOf(def.getEndDate());

        String endDateString = endDate.toString("yyyy-MM-dd");
        String startDateString = startDate.toString("yyyy-MM-dd");


        String query = "SELECT\n  C.person_id,\n  TIMESTAMPDIFF(MONTH, C.value_datetime, B.value_datetime) AS art_start,\n  B.value_coded,\n  B.value_numeric\nFROM (SELECT\n        person_id,\n        value_datetime\n      FROM obs\n" + String.format("      WHERE concept_id = 99161 AND value_datetime BETWEEN '%s' AND '%s') C\n", new Object[]{startDateString, endDateString}) + "  INNER JOIN\n" + "  (SELECT\n" + "     A.person_id,\n" + "     A.value_datetime,\n" + "     vlq.value_coded,\n" + "     vln.value_numeric\n" + "   FROM\n" + "     (SELECT\n" + "        person_id,\n" + "        encounter_id,\n" + "        value_datetime\n" + "      FROM obs\n" + "      WHERE\n" + "        concept_id = 163023 AND voided = 0\n" + "     ) A LEFT JOIN obs vlq\n" + "       ON (\n" + "       A.person_id = vlq.person_id AND A.encounter_id = vlq.encounter_id AND\n" + "       vlq.concept_id = 1305 AND vlq.voided = 0)\n" + "     LEFT JOIN obs vln\n" + "       ON (\n" + "       vln.person_id = A.person_id AND vln.encounter_id = A.encounter_id AND\n" + "       vln.concept_id = 856 AND vln.voided = 0)) B ON (B.person_id = C.person_id)";


        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = this.evaluationService.evaluateToList(q, context);
        Integer i = Integer.valueOf(1);
        for (Object[] row : results) {
            String patientIdString = String.valueOf(row[0]);
            String monthsSinceArtString = String.valueOf(row[1]);
            String valueCodedString = String.valueOf(row[2]);
            String valueNumericString = String.valueOf(row[3]);

            Integer patientId = Integer.valueOf(-1);
            Integer monthsSinceArt = Integer.valueOf(-1);
            Integer valueCoded = Integer.valueOf(-1);
            Double valueNumeric = Double.valueOf(-1.0D);

            if (!patientIdString.equalsIgnoreCase("null")) {
                patientId = Integer.valueOf(patientIdString);
            }
            if (!monthsSinceArtString.equalsIgnoreCase("null")) {
                monthsSinceArt = Integer.valueOf(monthsSinceArtString);
            }
            if (!valueCodedString.equalsIgnoreCase("null")) {
                valueCoded = Integer.valueOf(valueCodedString);
            }
            if (!valueNumericString.equalsIgnoreCase("null")) {
                valueNumeric = Double.valueOf(valueNumericString);
            }

            c.addData(i, new ViralLoad(patientId, monthsSinceArt, valueCoded, valueNumeric));

            i = Integer.valueOf(i.intValue() + 1);
        }
        return c;
    }
}