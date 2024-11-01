package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ReportingAuditToolDataSetDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {ReportingAuditToolDataSetDefinition.class})
public class ReportingAuditToolDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;


    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ReportingAuditToolDataSetDefinition definition = (ReportingAuditToolDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String cohortSelected = definition.getCohortList();

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        Cohort baseCohort = null;

//       excel computed variables in mysql
        String index_testing_children = " IF(children IS NULL, 'N', IF(known_status_children = children, 'Y', 'N')) ";
        String index_testing_spouse = "IF(age < 15, 'NE', IF(partners IS NULL, 'N', IF(known_status_partners = partners, 'Y', 'N')))";
        String vl_profiling = "IF(hiv_viral_load_copies IS NULL, '', IF(hiv_viral_load_copies BETWEEN 0 AND 49, '<50 hiv_viral_load_copies/ml',\n" +
                "                                                IF(hiv_viral_load_copies BETWEEN 50 AND 199,\n" +
                "                                                   '50-199hiv_viral_load_copies/ml',\n" +
                "                                                   IF(hiv_viral_load_copies BETWEEN 200 AND 999, '200-999copies/ml',\n" +
                "                                                      '≥1000copies/ml'))))";
        String vl_updated = "  IF(duration_on_art < 6, 'NA', IF(\n" +
                "               (age <= 19 AND TIMESTAMPDIFF(MONTH, hiv_viral_collection_date, CURRENT_DATE()) < 6) OR\n" +
                "               (age >= 20 AND TIMESTAMPDIFF(MONTH, hiv_viral_collection_date, CURRENT_DATE()) < 12), 'Y',\n" +
                "               'N')) ";


        String next_vl_date = "IF(" + vl_updated + " = 'Y', IF(age <= 19, DATE_ADD(hiv_viral_collection_date, INTERVAL 5 MONTH),\n" +
                "                               DATE_ADD(hiv_viral_collection_date, INTERVAL 11 MONTH)),\n" +
                "          IF(new_sample_collection_date IS NULL, return_date, ''))";

        String followup = "IF(identifier IS NULL, '',\n" +
                "          IF(client_status = 'Active(TX_CURR)', 'N', 'Y'))";

        String index_testing_children_flag = " IF(identifier IS NULL, '', IF((client_status = 'LTFU(TX_ML)' OR client_status = '' OR (" + index_testing_children + " ) = 'NE'), 'NA', IF((IF(children IS NULL, 'N', IF(known_status_children = children, 'Y', 'N'))) = 'N', 'Y',\n" +
                "                                                      'N'))) ";
        String index_testing_spouse_flag = "IF(identifier IS NULL, '', IF((client_status = 'LTFU(TX_ML)' OR client_status = '' OR (" + index_testing_spouse + ") ='NE' OR age < 15), 'NA',\n" +
                "                                     IF((" + index_testing_spouse + ") = 'N',\n" +
                "                                        'Y',\n" +
                "                                        'N'))) ";

        String mmd_flag = "IF(identifier IS NULL, '', IF(client_status = 'LTFU(TX_ML)', 'NA', IF(\n" +
                "               (age < 15 AND arv_days_dispensed <= 84) OR (age >= 15 AND arv_days_dispensed < 168), 'Y',\n" +
                "               'N')))";

        String vl_coverage = "IF((" + vl_updated + ") = 'NA','NA', IF((" + vl_updated + ") = 'N','Y','N'))";

        String vl_bleeding = "IF(" + vl_coverage + "='Y','NA', IF(new_sample_collection_date IS NULL, 'Y', 'N'))";

        String vl_tat = "IF(" + vl_bleeding + " !='N','NA',IF(TIMESTAMPDIFF(MONTH, new_sample_collection_date, CURRENT_DATE()) < 1, 'N','Y'))";

        String iac = "IF(identifier IS NULL, '', IF(" + vl_profiling + " = '' OR " + vl_profiling + " IN ('<50 hiv_viral_load_copies/ml', '50-199hiv_viral_load_copies/ml', '200-999copies/ml'),'NA',IF(iac_sessions >= 1, 'N', 'Y')))";

        String hivdrt_sample ="IF(identifier IS NULL, '',IF("+ iac +"='NA' OR iac_sessions <= 2,'NA', IF((iac_sessions >= 3 AND hivdr_results IS NOT NULL), 'N', 'Y')))";

        String hivdr_tat = "IF(identifier IS NULL, '',IF(("+hivdrt_sample+" !='N' OR hivdr_results IS NOT NULL), 'NA',\n"+
        "IF((TIMESTAMPDIFF(MONTH, date_hivr_results_recieved_at_facility, CURRENT_DATE()) < 1 AND hivdr_results = ''), 'N', 'Y')))";

        String switched = "IF(identifier IS NULL, '', IF(hivdr_results IS NULL, 'NA', IF(\n" +
                "               (decision_outcome = 'Clinical failure' OR decision_outcome = 'Immunological failure' OR\n" +
                "                decision_outcome = 'Virological failure' OR decision_outcome = 'HIV Drug resistance'), 'N',\n" +
                "               'Y')))";
        String pss = "IF(identifier IS NULL, '', IF(pss_issues_identified IS NOT NULL, 'N', 'Y'))";
        String nutrition_support = "IF(nutrition_support IS NOT NULL, 'Y', 'N')";
        String family_planning_assessment = "IF(age < 10, 'NA', IF(family_planning_status IS NOT NULL, 'N', 'Y'))";
        String tpt_flag =" IF(tpt_status = 'CURRENTLY ON INH PROPHYLAXIS FOR TB', 'ON', IF(tpt_status = 'Side effects', 'NE',\n" +
                "                                                                       IF(tpt_status = 'NEVER' OR tpt_status IS NULL OR\n" +
                "                                                                          tpt_status LIKE '%Defaulted%',\n" +
                "                                                                          'Y',\n" +
                "                                                                          'N')))";
        String tb_flag = "IF(tuberculosis_status IS NULL, 'Y', 'N') ";
        String cacx_flag  = "IF(identifier IS NULL, '', IF((gender = 'M' OR age <= 24 OR age >= 50), 'NA',\n" +
                "                                     IF(cervical_cancer_screening = '', 'Y', 'N')))";
        String hep_b_flag = "IF((hepatitis_b_test_qualitative = '' OR hepatitis_b_test_qualitative = 'Not Tested'), 'Y','N') ";
        String syphilis_flag ="IF((syphilis_test_result_for_partner = '' OR syphilis_test_result_for_partner = 'Not Tested'), 'Y','N')";
        String advanced_diziz_flag = "IF(advanced_disease IS NULL, 'Y', 'N')";
        String  ovc_flag ="IF(age >= 18, 'NA', IF((age < 18 AND ovc_screening IS NULL), 'Y', 'N'))";

        String xlsfunctions  = index_testing_children +" AS index_children_tested," +index_testing_spouse +" AS index_spouse_tested," +vl_profiling +" AS vl_profiling," +vl_updated+" AS vl_updated," +next_vl_date+
                "AS Next_vl_date, " + followup+ " AS followup, " +index_testing_children_flag +"AS  index_testing_children_flag, "+ index_testing_spouse_flag+" AS index_testing_spouse_flag, "+ mmd_flag+"AS mmd_flag  , "+vl_coverage+" AS vl_coverage, "+
                vl_bleeding+" AS vl_bleeding, "+vl_tat+"AS vl_tat , "+iac+"AS iac , "+hivdrt_sample+"AS hivdrt_sample," +hivdr_tat+ "AS hivdr_tat , "+ switched+" AS switched,"+ pss+" AS pss, "+nutrition_support+" AS nutrition_support, "+family_planning_assessment+"AS family_planning_flag, "+
                tpt_flag+"AS tpt_flag, "+ tb_flag+"AS tb_flag ,"+ cacx_flag+"AS cacx_flag, "+hep_b_flag+"AS hep_b_flag, "+syphilis_flag+"AS syphilis_flag, "+advanced_diziz_flag+"AS advanced_diziz_flag, "+ovc_flag+"AS ovc_flag ";
        String query = "";
        if (cohortSelected != null) {

            if (cohortSelected.equals("Patients with encounters")) {

                query = "select client_id from mamba_fact_encounter_hiv_art_card where encounter_date >='" + startDate + "' and encounter_date <= '" + endDate + "' group by client_id";

                SqlCohortDefinition cd = new SqlCohortDefinition(query);
                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cd, context);

                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cd, context);

            } else if (cohortSelected.equals("Patients on appointment")) {
                query = "SELECT client_id\n" +
                        "FROM (SELECT client_id, MAX(return_visit_date) returndate FROM mamba_fact_encounter_hiv_art_card GROUP BY client_id) a\n" +
                        "WHERE returndate BETWEEN '" + startDate + "' AND '" + endDate + "'";
                SqlCohortDefinition appointmentCohortDefinition = new SqlCohortDefinition(query);

                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(appointmentCohortDefinition, context);
            }

        }
        String query1 = "SELECT audit_tool.*, " + xlsfunctions +
                " FROM  (" + query + ")A INNER JOIN mamba_fact_audit_tool_art_patients audit_tool on A.client_id = audit_tool.client_id ";
        System.out.println(query1);
        List<Object[]> results = getEtl(query1, context);
        PatientDataHelper pdh = new PatientDataHelper();
        if (results.size() > 0 && !results.isEmpty()) {
            for (Object[] o : results) {
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "ART_No", o[2]);
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
                pdh.addCol(row, "ovc_screening", o[42]);
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
//                pdh.addCol(row, "cacx_date", o[58]);
                pdh.addCol(row, "index_children_tested", o[59]);
                pdh.addCol(row, "index_spouse_tested", o[60]);
                pdh.addCol(row, "vl_profiling", o[61]);
                pdh.addCol(row, "vl_updated", o[62]);
                pdh.addCol(row, "next_vl_date", o[63]);
                pdh.addCol(row, "followup", o[64]);
                pdh.addCol(row, "child_Tested_flag", o[65]);
                pdh.addCol(row, "spouses_Tested_flag", o[66]);
                pdh.addCol(row, "mmd_flag", o[67]);
                pdh.addCol(row, "vl_coverage", o[68]);
                pdh.addCol(row, "vl_bleeding", o[69]);
                pdh.addCol(row, "vl_tat", o[70]);
                pdh.addCol(row, "iac", o[71]);
                pdh.addCol(row, "HIVDR_sampling", o[72]);
                pdh.addCol(row, "HIVDR_tat", o[73]);
                pdh.addCol(row, "switched", o[74]);
                pdh.addCol(row, "pss", o[75]);
                pdh.addCol(row, "nutrition_support_flag", o[76]);
                pdh.addCol(row, "family_planning_flag", o[77]);
                pdh.addCol(row, "tpt_flag", o[78]);
                pdh.addCol(row, "tb_flag", o[79]);
                pdh.addCol(row, "cacx_flag", o[80]);
                pdh.addCol(row, "hep_b_flag", o[81]);
                pdh.addCol(row, "syphilis_flag", o[82]);
                pdh.addCol(row, "adv_disease_flag", o[83]);
                pdh.addCol(row, "ovc_flag", o[84]);


                dataSet.addRow(row);
            }

        }

        return dataSet;
    }


    public static String setToCommaSeparatedString(List<Integer> integerSet) {
        // Convert the set to a sorted list (optional if you want a specific order)
        List<Integer> integerList = new ArrayList<>(integerSet);
        Collections.sort(integerList);

        // Convert each integer element to a string
        List<String> stringList = new ArrayList<>();
        for (Integer i : integerList) {
            stringList.add(String.valueOf(i));
        }

        // Join the elements with commas
        String resultString = String.join(",", stringList);

        return resultString;
    }

    private List<Object[]> getEtl(String q, EvaluationContext context) {
        SqlQueryBuilder query = new SqlQueryBuilder();
        query.append(q);
        List<Object[]> results = evaluationService.evaluateToList(query, context);
        return results;
    }
}
