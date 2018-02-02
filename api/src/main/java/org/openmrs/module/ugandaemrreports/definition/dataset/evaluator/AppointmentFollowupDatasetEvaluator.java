package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.definition.data.definition.EncounterObsDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AppointmentFollowupDatasetDefinition;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.ugandaemrreports.reports.Helper.processString;
import static org.openmrs.module.ugandaemrreports.reports.Helper.processString3;

@Handler(supports = {AppointmentFollowupDatasetDefinition.class})
public class AppointmentFollowupDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        AppointmentFollowupDatasetDefinition definition = (AppointmentFollowupDatasetDefinition) dataSetDefinition;

        Date startDate = definition.getStartDate();
        Date endDate = definition.getEndDate();

        Map<String, String> answers = new HashMap<>();
        answers.put("160525", "Pre ART");
        answers.put("160524", "ART");
        answers.put("160526", "HIV Exposed Infant");
        answers.put("160446", "Antenatal Clinic (ANC)");
        answers.put("160529", "Tuberculosis (TB)");
        answers.put("165101", "SMS Message");
        answers.put("165102", "Phone Call");
        answers.put("165103", "Home Visit");
        answers.put("5622", "Other Specify");
        answers.put("165105", "SMS Message Not Delivered");
        answers.put("165106", "SMS Message Received");
        answers.put("165107", "Client Visit Rescheduled to");
        answers.put("165109", "Telephone off");
        answers.put("165110", "Telephone not answered");
        answers.put("165111", "Spoke with patient");
        answers.put("165112", "Spoke with someone else");
        answers.put("165114", "Call Treatment Supporter");
        answers.put("165115", "Try to followup patient again on");
        answers.put("165103", "Home visit on");

        String sqlQuery = "SELECT\n" +
                "  e.patient_id,\n" +
                "  e.encounter_id,\n" +
                "  DATE(e.encounter_datetime),\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pit.uuid, ''), COALESCE(identifier, '')))\n" +
                "   FROM patient_identifier pi INNER JOIN patient_identifier_type pit\n" +
                "       ON (pi.identifier_type = pit.patient_identifier_type_id)\n" +
                "   WHERE pi.patient_id = e.patient_id)                                                         AS 'identifiers',\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(':', COALESCE(pat.uuid, ''), COALESCE(value, '')))\n" +
                "   FROM person_attribute pa INNER JOIN person_attribute_type pat\n" +
                "       ON (pa.person_attribute_type_id = pat.person_attribute_type_id)\n" +
                "   WHERE e.patient_id =\n" +
                "         pa.person_id)                                                                         AS 'attributes',\n" +
                "  (SELECT GROUP_CONCAT(CONCAT_WS(' ', COALESCE(given_name, ''), COALESCE(middle_name, ''), COALESCE(family_name, '')))\n" +
                "   FROM person_name pn\n" +
                "   WHERE e.patient_id = pn.person_id)                                                          AS 'names',\n" +
                "  (SELECT group_concat(\n" +
                "      concat_ws(':', o.concept_id, o.value_numeric, o.value_coded, DATE(o.value_datetime), o.value_text))\n" +
                "   FROM obs o\n" +
                "   WHERE o.encounter_id = e.encounter_id AND o.voided = 0)                                     AS obs,\n" +
                "  (SELECT COUNT(DISTINCT ex.encounter_id)\n" +
                "   FROM encounter ex\n" +
                "   WHERE ex.patient_id = e.patient_id AND ex.voided = 0 AND ex.encounter_datetime < e.encounter_datetime AND\n" +
                "         ex.encounter_type NOT IN (SELECT et.encounter_type_id\n" +
                "                                   FROM encounter_type et\n" +
                "                                   WHERE et.uuid IN ('dc551efc-024d-4c40-aeb8-2147c4033778'))) AS visits,\n" +
                "(SELECT DATE(MAX(o.value_datetime))\n" +
                "   FROM obs o\n" +
                "   WHERE o.voided = 0 AND o.person_id = e.patient_id AND o.value_datetime < e.encounter_datetime\n" +
                "         AND o.concept_id = 5096)                                                   AS appointment\n" +
                "FROM encounter e\n" +
                "WHERE e.voided = 0\n" +
                "      AND e.encounter_type IN (SELECT et.encounter_type_id\n" +
                "                               FROM encounter_type et\n" +
                "                               WHERE et.uuid IN ('dc551efc-024d-4c40-aeb8-2147c4033778'))\n";
        if (startDate != null && endDate != null) {
            String d1 = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            String d2 = DateUtil.formatDate(endDate, "yyyy-MM-dd");
            sqlQuery += String.format("      AND e.encounter_datetime BETWEEN '%s' AND '%s';", d1, d2);
        } else if (startDate != null) {
            String d1 = DateUtil.formatDate(startDate, "yyyy-MM-dd");
            sqlQuery += String.format("      AND e.encounter_datetime >= '%s';", d1);
        } else if (endDate != null) {
            String d1 = DateUtil.formatDate(endDate, "yyyy-MM-dd");
            sqlQuery += String.format("      AND e.encounter_datetime <= '%s';", d1);
        }

        SqlQueryBuilder q = new SqlQueryBuilder(sqlQuery);


        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            String patientId = String.valueOf(r[0]);
            String encounterId = String.valueOf(r[1]);
            String encounterDate = String.valueOf(r[2]);
            String identifiers = String.valueOf(r[3]);
            String attributes = String.valueOf(r[4]);
            String names = String.valueOf(r[5]);
            String obs = String.valueOf(r[6]);
            String visits = String.valueOf(r[7]);
            String appointment = String.valueOf(r[8]);
            Map<String, String> processedObs = processString3(obs, answers);

            String clinicNo = processString(identifiers).get("e1731641-30ab-102d-86b0-7a5022ba4115");
            String eidNo = processString(identifiers).get("2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe");
            String telephone = processString3(attributes).get("14d4f066-15f5-102d-96e4-000c29c2a5d7");

            String returnVisits = processedObs.get("160530");
            String followupMethod = processedObs.get("165100");
            String followupOutcome = processedObs.get("165104");
            String nextAction = processedObs.get("165113");
            String revisit = processedObs.get("165108");
            String nextFollowupDate = processedObs.get("165116");


            pdh.addCol(row, "Family Name", names);
            pdh.addCol(row, "HIV Clinic No", clinicNo);
            pdh.addCol(row, "EID No", eidNo);
            pdh.addCol(row, "Telephone", telephone);
            pdh.addCol(row, "Care", returnVisits);
            pdh.addCol(row, "Visits", visits);
            pdh.addCol(row, "Appointment Date", appointment);
            pdh.addCol(row, "Followup Date", nextFollowupDate);

            dataSet.addRow(row);
        }
        return dataSet;
    }
}
