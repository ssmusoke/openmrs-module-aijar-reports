package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.common.util.DateUtil;
import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
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
                "  e.encounter_datetime,\n" +
                "  o.concept_id,\n" +
                "  o.value_coded,\n" +
                "  o.value_datetime,\n" +
                "  o.value_numeric,\n" +
                "  o.value_text,\n" +
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
            String patientIdString = String.valueOf(row[0]);
            String encounterIdString = String.valueOf(row[1]);
            String encounterDateString = String.valueOf(row[2]);
            String conceptString = String.valueOf(row[3]);
            String valueCodedString = String.valueOf(row[4]);
            String valueDatetimeString = String.valueOf(row[5]);
            String valueNumericString = String.valueOf(row[6]);
            String valueTextString = String.valueOf(row[7]);
            String birthDateString = String.valueOf(row[8]);

            Integer valueCoded = -1;
            Date valueDatetime = null;
            Double valueNumeric = -1.0;
            String valueText = "";
            Date birthDate = null;

            Date encounterDate = null;

            Integer patientId = Integer.valueOf(patientIdString);
            Integer encounterId = Integer.valueOf(encounterIdString);

            try {
                encounterDate = DateUtil.parseDate(encounterDateString);
            } catch (ParseException e) {
            }

            Integer concept = Integer.valueOf(conceptString);

            if (!valueCodedString.equalsIgnoreCase("null")) {
                valueCoded = Integer.valueOf(valueCodedString);
            }

            if (!valueNumericString.equalsIgnoreCase("null")) {
                valueNumeric = Double.valueOf(valueNumericString);
            }

            if (!valueDatetimeString.equalsIgnoreCase("null")) {
                try {
                    valueDatetime = DateUtil.parseDate(valueDatetimeString);
                } catch (ParseException e) {
                    valueDatetime = null;
                }
            }
            if (!valueTextString.equalsIgnoreCase("null")) {
                valueText = valueTextString;
            }

            try {
                birthDate = DateUtil.parseDate(birthDateString);
            } catch (ParseException e) {
            }

            c.addData(i, new PatientNonSuppressingData(patientId, encounterId, encounterDate, concept, valueCoded, valueDatetime, valueNumeric, valueText, birthDate));

            i += 1;
        }
        return c;
    }
}