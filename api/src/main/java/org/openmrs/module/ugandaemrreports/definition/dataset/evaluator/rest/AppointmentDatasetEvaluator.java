package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator.rest;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.rest.AppointmentDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;



@Handler(supports = {AppointmentDatasetDefinition.class})
public class AppointmentDatasetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        AppointmentDatasetDefinition definition = (AppointmentDatasetDefinition) dataSetDefinition;


        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        startDate = startDate+" 00:00:00";
        endDate = endDate+" 23:59:59";

        String sqlQuery ="SELECT a.person_id, gender, age, pn.given_name,pn.family_name,pi.identifier,a.return_date\n" +
                "FROM (SELECT p.person_id,\n" +
                "             DATE (MAX(value_datetime))                         return_date,\n" +
                "             TIMESTAMPDIFF(YEAR, birthdate, ':endDate') AS age,\n" +
                "             gender\n" +
                "      FROM obs o\n" +
                "               INNER JOIN person p ON p.person_id = o.person_id AND p.dead = 0\n" +
                "      WHERE concept_id = 5096\n" +
                "        AND o.voided = 0\n" +
                "      GROUP BY person_id) a\n" +
                "         INNER JOIN person_name pn ON pn.person_id = a.person_id AND pn.voided = 0 AND pn.preferred = 1\n" +
                "         INNER JOIN patient_identifier pi ON a.person_id = pi.patient_id\n" +
                "INNER JOIN patient_identifier_type pit ON pi.identifier_type = pit.patient_identifier_type_id and pit.uuid='e1731641-30ab-102d-86b0-7a5022ba4115'\n" +
                "WHERE a.return_date >= ':startDate'\n" +
                "  AND a.return_date <= ':endDate'";

        sqlQuery= sqlQuery.replaceAll(":endDate",endDate);
        sqlQuery= sqlQuery.replaceAll(":startDate",startDate);
        SqlQueryBuilder q = new SqlQueryBuilder(sqlQuery);


        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            String patientId = String.valueOf(r[0]);
            String identifier = String.valueOf(r[5]);
            String gender = String.valueOf(r[1]);
            String age = String.valueOf(r[2]);
            String given_name = String.valueOf(r[3]);
            String family_name = String.valueOf(r[4]);
            String appointment_date = String.valueOf(r[6]);

            pdh.addCol(row, "patientId", patientId);
            pdh.addCol(row, "identifier", identifier);
            pdh.addCol(row, "age", age);
            pdh.addCol(row, "gender", gender);
            pdh.addCol(row, "given_name", given_name);
            pdh.addCol(row, "family_name", family_name);
            pdh.addCol(row, "appointment_date", appointment_date);

            dataSet.addRow(row);
        }
        return dataSet;
    }
}
