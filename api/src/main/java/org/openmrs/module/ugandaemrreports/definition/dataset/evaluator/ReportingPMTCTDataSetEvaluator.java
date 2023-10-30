package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.collect.Sets;
import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingPMTCTDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {ReportingPMTCTDataSetDefinition.class})
public class ReportingPMTCTDataSetEvaluator implements DataSetEvaluator {


    @Autowired
    EvaluationService evaluationService;


    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ReportingPMTCTDataSetDefinition definition = (ReportingPMTCTDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);


        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");


        String query = " SELECT audit_tool.*,C.PMTCT_STATUS," +
                        "C.id as EID_id, C.client_id as EID_mother, EDD, EID_NO, EID_DOB, EID_AGE, EID_WEIGHT, EID_NEXT_APPT, EID_FEEDING, CTX_START, CTX_AGE, 1ST_PCR_DATE, 1ST_PCR_AGE, 1ST_PCR_RESULT, 1ST_PCR_RECEIVED, 2ND_PCR_DATE, " +
                        "2ND_PCR_AGE, 2ND_PCR_RESULT, 2ND_PCR_RECEIVED, REPEAT_PCR_DATE, REPEAT_PCR_AGE, REPEAT_PCR_RESULT, REPEAT_PCR_RECEIVED, RAPID_PCR_DATE, RAPID_PCR_AGE, RAPID_PCR_RESULT, FINAL_OUTCOME, LINKAGE_NO, NVP_AT_BIRTH,BREAST_FEEDING_STOPPED, " +
                        " PMTCT_ENROLLMENT_DATE FROM mamba_fact_eid_patients C\n" +
                        "         left join mamba_fact_audit_tool_art_patients audit_tool on C.client_id =audit_tool.client_id ";

                List<Object[]> results = getEtl(query,context);
                PatientDataHelper pdh = new PatientDataHelper();
                if (results.size() > 0 && !results.isEmpty()) {
                    for (Object[] o : results) {
                        DataSetRow row = new DataSetRow();
                        pdh.addCol(row, "id", o[2]);
                        pdh.addCol(row, "nationality", o[3]);
                        pdh.addCol(row, "gender", o[8]);
                        pdh.addCol(row, "date_of_birth", o[5]);
                        pdh.addCol(row, "age", o[6]);
                        pdh.addCol(row, "marital_status", o[4]);

                        pdh.addCol(row, "special_category", o[33]);

                        pdh.addCol(row, "last_visit_date", o[9]);
                        pdh.addCol(row, "next_appointment_date", o[10]);

                        pdh.addCol(row, "client_status", o[11]);

                        pdh.addCol(row, "art_start_date", o[32]);
//                pdh.addCol(row, "duration_on_art", o[13]);

                        pdh.addCol(row, "current_regimen", o[13]);
                        pdh.addCol(row, "regimen_line", o[34]);
                        pdh.addCol(row, "current_arv_regimen_start_date", o[14]);
                        pdh.addCol(row, "adherence", o[15]);
//                pdh.addCol(row, "side_effects", o[14]);
                        pdh.addCol(row, "prescription_duration", o[16]);
//                pdh.addCol(row, "sample_type", o[12]);

                        pdh.addCol(row, "current_vl", o[17]);
                        pdh.addCol(row, "vl_result_sample_date", o[18]);
                        pdh.addCol(row, "new_vl_sample_date", o[19]);


                        pdh.addCol(row, "iacs_no", o[44]);
                        pdh.addCol(row, "repeat_vl_collection_date", o[52]);
                        pdh.addCol(row, "repeat_vl_results_after_iacs", o[47]);
                        pdh.addCol(row, "hivdrt_results", o[45]);
                        pdh.addCol(row, "date_dr_results_received", o[46]);
                        pdh.addCol(row, "decision", o[48]);
                        pdh.addCol(row, "pss", o[36]);
                        pdh.addCol(row, "ovc_screening", o[41]);
                        pdh.addCol(row, "nutrition_status", o[22]);
                        pdh.addCol(row, "family_planning_status", o[21]);
                        pdh.addCol(row, "cacx_screening", o[26]);
//                pdh.addCol(row, "diabetes_status", o[16]);
//                pdh.addCol(row, "htn_status", o[16]);
//                pdh.addCol(row, "mental_health_status", o[16]);
                        pdh.addCol(row, "hepatitis_b_status", o[24]);
                        pdh.addCol(row, "syphillis_status", o[25]);

                        pdh.addCol(row, "tpt_status", o[28]);
                        pdh.addCol(row, "tb_status", o[27]);
//                pdh.addCol(row, "cd4_eligibility", o[23]);
                        pdh.addCol(row, "tb_lam_crag_results", o[29]);
                        pdh.addCol(row, "who_stage", o[30]);
                        pdh.addCol(row, "advanced_disease", o[20]);
                        pdh.addCol(row, "duration_on_art", o[49]);
                        pdh.addCol(row, "side_effects", o[50]);
                        pdh.addCol(row, "sample_type", o[51]);
                        pdh.addCol(row, "known_status_children", o[53]);
                        pdh.addCol(row, "pos_status_children", o[54]);
                        pdh.addCol(row, "known_status_spouse", o[55]);
                        pdh.addCol(row, "po_status_spouse", o[56]);
                        pdh.addCol(row, "age_group", o[57]);
                        pdh.addCol(row, "Preg", o[59]);


                        pdh.addCol(row, "EDD", o[62]);
                        pdh.addCol(row, "EID_NO", o[63]);
                        pdh.addCol(row, "EID_DOB", o[64]);
                        pdh.addCol(row, "EID_AGE", o[65]);
                        pdh.addCol(row, "EID_WEIGHT", o[66]);
                        pdh.addCol(row, "EID_NAPPT", o[67]);
                        pdh.addCol(row, "feeding", o[68]);
                        pdh.addCol(row, "CTX_DATE", o[69]);
                        pdh.addCol(row, "CTX_AGE", o[70]);
                        pdh.addCol(row, "1PCR_DATE", o[71]);
                        pdh.addCol(row, "1PCR_AGE", o[72]);
                        pdh.addCol(row, "1PCR_RESULT", o[73]);
                        pdh.addCol(row, "1PCR_GIVEN", o[74]);
                        pdh.addCol(row, "2PCR_DATE", o[75]);
                        pdh.addCol(row, "2PCR_AGE", o[76]);
                        pdh.addCol(row, "2PCR_RESULT", o[77]);
                        pdh.addCol(row, "2PCR_GIVEN", o[78]);

                        pdh.addCol(row, "3PCR_DATE", o[79]);
                        pdh.addCol(row, "3PCR_AGE", o[80]);
                        pdh.addCol(row, "3PCR_RESULT", o[81]);
                        pdh.addCol(row, "3PCR_GIVEN", o[82]);

                        pdh.addCol(row, "RAPID_DATE", o[83]);
                        pdh.addCol(row, "RAPID_AGE", o[84]);
                        pdh.addCol(row, "RAPID_RESULT", o[85]);
                        pdh.addCol(row, "OUTCOME", o[86]);
                        pdh.addCol(row, "LINKAGE_NO", o[87]);
                        pdh.addCol(row, "NVP", o[88]);
                        pdh.addCol(row, "STOPPED_BF", o[89]);
                        pdh.addCol(row, "PMTCT_DATE", o[90]);

                        dataSet.addRow(row);
                    }
        }

        return dataSet;
    }


    // Function to convert a collection to a comma-separated string
    public static <T> String convertToCommaSeparatedString(Collection<T> collection) {
        StringBuilder result = new StringBuilder();
        String delimiter = "";

        for (T item : collection) {
            result.append(delimiter).append(item);
            delimiter = ",";
        }

        return result.toString();
    }

    private List<Object[]> getEtl(String q,EvaluationContext context) {
        SqlQueryBuilder query = new SqlQueryBuilder();
        query.append(q);
        List<Object[]> results = evaluationService.evaluateToList(query, context);
        return results;
    }
}

