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
import org.openmrs.module.ugandaemrreports.common.PatientNonSuppressingData;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.common.ViralLoad;
import org.openmrs.module.ugandaemrreports.definition.data.definition.NonSuppressingPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ViralLoadPatientDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.Date;
import java.util.List;


@Handler(supports = {NonSuppressingPatientDataDefinition.class})
public class NonSuppressingPatientDataEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(NonSuppressingPatientDataEvaluator.class);
    @Autowired
    private EvaluationService evaluationService;

    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
            throws EvaluationException {
        NonSuppressingPatientDataDefinition def = (NonSuppressingPatientDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if ((context.getBaseCohort() != null) && (context.getBaseCohort().isEmpty())) {
            return c;
        }

        String query = "SELECT\n" +
                "  o.person_id,\n" +
                "  e.encounter_id,\n" +
                "  DATe(e.encounter_datetime),\n" +
                "  o.concept_id,\n" +
                "  IF(o.value_coded IS NULL, -1, o.value_coded)                       AS 'value_coded',\n" +
                "  IF(o.value_datetime IS NULL, '1900-01-01', DATE(o.value_datetime)) AS 'value_datetime',\n" +
                "  IF(o.value_numeric IS NULL, -1, o.value_numeric)                 AS 'value_numeric',\n" +
                "  IF(o.value_text IS NULL, '', o.value_text)                         AS 'value_text',\n" +
                "  p.birthdate\n" +
                "FROM encounter e INNER JOIN obs o ON (e.encounter_id = o.encounter_id\n" +
                "                                      AND e.encounter_type = (SELECT encounter_type_id\n" +
                "                                                              FROM encounter_type\n" +
                "                                                              WHERE uuid =\n" +
                "                                                                    '38cb2232-30fc-4b1f-8df1-47c795771ee9'))\n" +
                "  INNER JOIN person p ON (p.person_id = o.person_id)";


        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = this.evaluationService.evaluateToList(q, context);
        Integer i = 1;
        for (Object[] row : results) {
            Integer patientId = Integer.valueOf(String.valueOf(row[0]));
            Integer encounterId = Integer.valueOf(String.valueOf(row[1]));
            Date encounterDate = DateUtil.parseYmd(String.valueOf(row[2]));
            Integer concept = Integer.valueOf(String.valueOf(row[3]));
            Integer valueCoded = Integer.valueOf(String.valueOf(row[4]));
            Date valueDatetime = DateUtil.parseYmd(String.valueOf(row[5]));
            Double valueNumeric = Double.valueOf(String.valueOf(row[6]));
            String valueText = String.valueOf(row[7]);
            Date birthDate = DateUtil.parseYmd(String.valueOf(row[8]));
            c.addData(i, new PatientNonSuppressingData(patientId, encounterId, encounterDate, concept, valueCoded, valueDatetime, valueNumeric, valueText, birthDate));
            i += 1;
        }
        return c;
    }
}