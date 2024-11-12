package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
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
import org.openmrs.module.ugandaemrreports.common.PersonDemographics;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.VLExchangeDatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openmrs.module.ugandaemrreports.reports.Helper.*;


/**
 */
@Handler(supports = {VLExchangeDatasetDefinition.class})
public class VLExchangeDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;


    Map<Integer,String> drugNames = new HashMap<>();
    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        VLExchangeDatasetDefinition definition = (VLExchangeDatasetDefinition) dataSetDefinition;


        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        PatientDataHelper pdh = new PatientDataHelper();

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        startDate = startDate+" 00:00:00";
        endDate = endDate+" 23:59:59";
        context = ObjectUtil.nvl(context, new EvaluationContext());

        String dataQuery = String.format("SELECT pi.identifier as HIV_clinic_no,\n" +
                "       p.birthdate,\n" +
                "       TIMESTAMPDIFF(YEAR,p.birthdate,CURRENT_DATE) AS age,\n" +
                "       p.gender,\n" +
                "       accession_number              AS specimen_id,\n" +
                "       specimen_source,\n" +
                "       DATE(date_activated),\n" +
                "       DATE(send_request_sync_task.date_sent) AS send_request_date_sent,\n" +
                "       send_request_sync_task.status AS send_request_status,\n" +
                "       send_request_sync_task.status_code AS send_request_status_code,\n" +
                "       program_data_task.status as program_data_status,\n" +
                "       program_data_task.status_code as program_data_status_code,\n" +
                "       DATE(program_data_task.date_created) as program_data_date,\n" +
                "       DATE(request_result_task.date_created) as request_results_date,\n" +
                "       request_result_task.status as request_results,\n" +
                "       request_result_task.status_code as request_status_code\n" +
                "\n" +
                "FROM (SELECT orders.patient_id,orders.date_activated,orders.accession_number, test_order.specimen_source\n" +
                "      FROM orders\n" +
                "         INNER JOIN test_order ON (test_order.order_id = orders.order_id)\n" +
                "WHERE accession_number IS NOT NULL\n" +
                "  AND specimen_source IS NOT NULL\n" +
                "  AND orders.instructions = 'REFER TO cphl'\n" +
                "  AND orders.concept_id = 165412\n" +
                "  AND orders.voided = 0\n" +
                "  AND orders.date_activated >= '%s'\n" +
                "  AND orders.date_activated <= '%s')cohort\n" +
                "LEFT JOIN person p  on p.person_id = cohort.patient_id\n" +
                "LEFT JOIN sync_task send_request_sync_task on send_request_sync_task.sync_task = accession_number and send_request_sync_task.sync_task_type = (SELECT sync_task_type_id from sync_task_type where uuid='3551ca84-06c0-432b-9064-fcfeefd6f4ec')\n" +
                "LEFT JOIN sync_task request_result_task on request_result_task.sync_task = accession_number and request_result_task.sync_task_type = (SELECT sync_task_type_id from sync_task_type where uuid='3396dcf0-2106-4e73-9b90-c63978c3a8b4')\n" +
                "LEFT JOIN sync_task program_data_task on program_data_task.sync_task = accession_number and program_data_task.sync_task_type = (SELECT sync_task_type_id from sync_task_type where uuid='f9b2fa5d-5d37-4fd9-b20a-a0cab664f520')\n" +
        "LEFT JOIN patient_identifier pi ON pi.patient_id = cohort.patient_id and pi.identifier_type = (SELECT patient_identifier_type_id from patient_identifier_type where uuid = 'e1731641-30ab-102d-86b0-7a5022ba4115');",startDate,endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);

        List<Object[]> results = evaluationService.evaluateToList(q, context);

        if(!results.isEmpty()) {
            for (Object[] e : results) {
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "HIV Clinic No", e[0]);
                pdh.addCol(row, "Birthdate", e[1]);
                pdh.addCol(row, "Age", e[2]);
                pdh.addCol(row, "Sex", e[3]);
                pdh.addCol(row, "Specimen ID", e[4]);
                pdh.addCol(row, "Specimen source", e[5]);
                pdh.addCol(row, "Date Ordered", e[6]);
                pdh.addCol(row, "send_request_date_sent", e[7]);
                pdh.addCol(row, "send_request_status", e[8]);
                pdh.addCol(row, "send_request_status_code", e[9]);
                pdh.addCol(row, "program_data_status", e[10]);
                pdh.addCol(row, "program_data_status_code", e[11]);
                pdh.addCol(row, "program_data_date", e[12]);
                pdh.addCol(row, "request_results_date", e[13]);
                pdh.addCol(row, "request_results", e[14]);
                pdh.addCol(row, "request_status_code", e[15]);

                dataSet.addRow(row);
            }

        }
        return dataSet;
    }

}
