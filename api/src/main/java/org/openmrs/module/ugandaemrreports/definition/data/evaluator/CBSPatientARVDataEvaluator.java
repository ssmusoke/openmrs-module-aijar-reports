package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import com.google.common.base.Joiner;
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
import org.openmrs.module.ugandaemrreports.common.PatientARV;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CBSPatientARVDataDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

@Handler(supports = {CBSPatientARVDataDefinition.class})
public class CBSPatientARVDataEvaluator
        implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(CBSPatientARVDataEvaluator.class);

    @Autowired
    private EvaluationService evaluationService;
    @Autowired
    private HIVMetadata hivMetadata;

    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
            throws EvaluationException {
        CBSPatientARVDataDefinition def = (CBSPatientARVDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if ((context.getBaseCohort() != null) && (context.getBaseCohort().isEmpty())) {
            return c;
        }

        LocalDate workingDate = StubDate.dateOf(def.getStartDate());

        String startDateString = workingDate.toString("yyyy-MM-dd");

        Set<Integer> members = def.getCohort().getMemberIds();

        String membersString = Joiner.on(",").join(members);

        Integer encounter = this.hivMetadata.getARTEncounterEncounterType().getEncounterTypeId();
        Integer summary = this.hivMetadata.getARTSummaryEncounter().getEncounterTypeId();

        String query = "SELECT\n  p.person_id,\n  TIMESTAMPDIFF(YEAR, p.birthdate, B.encounter_datetime)                 AS age,\n  TIMESTAMPDIFF(MONTH, summary.encounter_datetime, B.encounter_datetime) AS dt,\n  B.value_coded\nFROM encounter summary INNER JOIN\n  (SELECT\n     e.patient_id,\n     e.encounter_id,\n     e.encounter_datetime,\n     o.value_coded\n   FROM encounter e INNER JOIN obs o\n       ON (\n" + String.format("       o.concept_id = 90315 AND e.patient_id IN (%s) AND o.person_id = e.patient_id AND\n", new Object[]{membersString}) + String.format("       e.encounter_id = o.encounter_id AND encounter_type = %s\n", new Object[]{encounter}) + String.format("       AND encounter_datetime BETWEEN DATE_ADD('%s', INTERVAL 6 MONTH) AND\n", new Object[]{startDateString}) + String.format("       DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY)) B\n", new Object[]{startDateString}) + String.format("    ON (summary.patient_id = B.patient_id AND summary.encounter_type = %s AND\n", new Object[]{summary}) + "        TIMESTAMPDIFF(MONTH, summary.encounter_datetime, B.encounter_datetime) BETWEEN 6 AND 72)\n" + "  INNER JOIN person p ON (summary.patient_id = p.person_id)";

        Integer begin;

        if (members.size() != 0) {
            SqlQueryBuilder q = new SqlQueryBuilder(query);
            List<Object[]> results = this.evaluationService.evaluateToList(q, context);
            begin = Integer.valueOf(1);
            for (Object[] row : results) {
                Integer patientId = Integer.valueOf(String.valueOf(row[0]));
                Integer ageAtEncounter = Integer.valueOf(String.valueOf(row[1]));
                Integer monthsFromEnrollment = Integer.valueOf(String.valueOf(row[2]));
                Integer valueCoded = Integer.valueOf(String.valueOf(row[3]));
                c.addData(begin, new PatientARV(patientId, ageAtEncounter, monthsFromEnrollment, valueCoded));
                begin = Integer.valueOf(begin.intValue() + 1);
            }
        }
        return c;
    }
}
