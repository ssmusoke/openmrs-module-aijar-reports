package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.hibernate.SQLQuery;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
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

    @Autowired
    private DbSessionFactory sessionFactory;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ReportingAuditToolDataSetDefinition definition = (ReportingAuditToolDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String cohortSelected = definition.getCohortList();

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        Cohort baseCohort = null;

        if (cohortSelected != null) {

            if (cohortSelected.equals("Patients with encounters")) {
                EncounterCohortDefinition cd = new EncounterCohortDefinition();
                cd.setEncounterTypeList(hivMetadata.getArtEncounterTypes());
                cd.setOnOrAfter(definition.getStartDate());
                cd.setOnOrBefore(definition.getEndDate());

                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cd, context);

            } else if (cohortSelected.equals("Patients on appointment")) {
                SqlCohortDefinition appointmentCohortDefinition = new SqlCohortDefinition("SELECT client_id\n" +
                        "FROM (SELECT client_id, MAX(return_visit_date) returndate FROM mamba_fact_encounter_hiv_art_card GROUP BY client_id) a\n" +
                        "WHERE returndate BETWEEN '" + startDate + "' AND '" + endDate + "]" + "';");

                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(appointmentCohortDefinition, context);
            }

        }
        String cohortIds = "";

        if (baseCohort.getMemberIds().size() > 0 && baseCohort != null) {
            cohortIds = setToCommaSeparatedString(baseCohort.getMemberIds());
        }
        String query = "SELECT *\n" +
                "    FROM mamba_fact_audit_tool_art_patients audit_tool where client_id in (" + cohortIds + ")";

        List<Object[]> results = getEtl(query);
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


                dataSet.addRow(row);
            }


        }
        return dataSet;
    }


    public static String setToCommaSeparatedString(Set<Integer> integerSet) {
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

    private List<Object[]> getEtl(String q) {
        SQLQuery query = sessionFactory.getCurrentSession().createSQLQuery(q);
//        query.setParameter("cohortList", cohortList);
        List<Object[]> results = query.list();
        return results;
    }

    public DbSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

}
