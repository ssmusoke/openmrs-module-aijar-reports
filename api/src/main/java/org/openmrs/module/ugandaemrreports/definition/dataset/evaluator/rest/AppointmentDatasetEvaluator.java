package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator.rest;

import org.openmrs.Encounter;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.rest.AppointmentDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.openmrs.module.ugandaemrreports.reports.Helper.processString;
import static org.openmrs.module.ugandaemrreports.reports.Helper.processString3;

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

        String sqlQuery ="SELECT p.person_id, TIMESTAMPDIFF(YEAR,birthdate,':endDate') as Age,gender from person p inner join\n" +
                "(SELECT person_id,max(value_datetime) return_date from obs where concept_id=5096 and voided=0 GROUP BY person_id)A on p.person_id =A.person_id where A.return_date >= ':startDate' and A.return_date <=':endDate'";

        sqlQuery= sqlQuery.replaceAll(":endDate",endDate);
        sqlQuery= sqlQuery.replaceAll(":startDate",startDate);
        SqlQueryBuilder q = new SqlQueryBuilder(sqlQuery);


        List<Object[]> results = evaluationService.evaluateToList(q, context);

        PatientDataHelper pdh = new PatientDataHelper();

        for (Object[] r : results) {
            DataSetRow row = new DataSetRow();

            String patientId = String.valueOf(r[0]);
            String age = String.valueOf(r[1]);
            String gender = String.valueOf(r[2]);

            pdh.addCol(row, "identifier", patientId);
            pdh.addCol(row, "age", age);
            pdh.addCol(row, "gender", gender);

            dataSet.addRow(row);
        }
        return dataSet;
    }
}
