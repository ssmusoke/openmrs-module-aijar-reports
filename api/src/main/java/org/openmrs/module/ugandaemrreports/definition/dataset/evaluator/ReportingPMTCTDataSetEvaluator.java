package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.SQLQuery;
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

    @Autowired
    private DbSessionFactory sessionFactory;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ReportingPMTCTDataSetDefinition definition = (ReportingPMTCTDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        String cohortSelected = definition.getCohortList();

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        //getting all clients who have a PMTCT status in their latest encounter
        SqlCohortDefinition sqlCohortDefinition = new SqlCohortDefinition("select DISTINCT o.person_id as patient from obs o WHERE o.voided = 0 and concept_id=90041 and value_coded in (1065,99601) and obs_datetime<= CURRENT_DATE() and obs_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR) union\n" +
                "SELECT person_a as patient from relationship r inner join person p on r.person_a = p.person_id inner join relationship_type rt on r.relationship = rt.relationship_type_id and rt.uuid='8d91a210-c2cc-11de-8d13-0010c6dffd0f' where p.gender='F' and r.person_b in (SELECT DISTINCT e.patient_id from encounter e INNER JOIN encounter_type et\n" +
                "                                                                                                                                                                                                                                                                                                                             ON e.encounter_type = et.encounter_type_id WHERE e.voided = 0 and et.uuid in('9fcfcc91-ad60-4d84-9710-11cc25258719','4345dacb-909d-429c-99aa-045f2db77e2b') and encounter_datetime<= CURRENT_DATE() and encounter_datetime>= DATE_SUB(CURRENT_DATE(), INTERVAL 1 YEAR));");
        Cohort PMTCTMothers = Context.getService(CohortDefinitionService.class).evaluate(sqlCohortDefinition, context);


        Cohort baseCohort = null;
        Cohort eidBaseCohort= null;

        if (cohortSelected != null) {

            if (cohortSelected.equals("Patients with encounters")) {
                SqlCohortDefinition cd = new SqlCohortDefinition("select distinct client_id from mamba_fact_encounter_hiv_art_card where encounter_date >='"+ startDate + "' and encounter_date <= '"+ endDate+"';");
                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cd, context);

                SqlCohortDefinition eidCohort = new SqlCohortDefinition("select DISTINCT e.patient_id from encounter e inner join encounter_type et on e.encounter_type = et.encounter_type_id  where e.voided=0 and et.uuid in ('9fcfcc91-ad60-4d84-9710-11cc25258719','4345dacb-909d-429c-99aa-045f2db77e2b') and encounter_datetime >= '"+ startDate + "' and encounter_datetime <=  '"+ endDate+"';");
                eidBaseCohort = Context.getService(CohortDefinitionService.class).evaluate(eidCohort, context);

            } else if (cohortSelected.equals("Patients on appointment")) {
                SqlCohortDefinition appointmentCohortDefinition = new SqlCohortDefinition("SELECT client_id\n" +
                        "FROM (SELECT client_id, MAX(return_visit_date) returndate FROM mamba_fact_encounter_hiv_art_card GROUP BY client_id) a\n" +
                        "WHERE returndate BETWEEN '" + startDate + "' AND '" + endDate + "]" + "';");

                baseCohort = Context.getService(CohortDefinitionService.class).evaluate(appointmentCohortDefinition, context);

                SqlCohortDefinition eidCohort = new SqlCohortDefinition("SELECT person_id from (select o.person_id, max(value_datetime) from obs o INNER JOIN encounter e on o.encounter_id = e.encounter_id inner join encounter_type et on e.encounter_type = et.encounter_type_id and et. uuid='4345dacb-909d-429c-99aa-045f2db77e2b' where concept_id=5096 and o.voided=0 and value_datetime >=  '"+ startDate + "'  and value_datetime <= '"+ endDate+"' group by person_id)A ");
                eidBaseCohort = Context.getService(CohortDefinitionService.class).evaluate(eidCohort, context);

            }

        }
        String cohortIds = "";

        if (!baseCohort.isEmpty() && !PMTCTMothers.isEmpty()) {
            Collection allMothers = CollectionUtils.intersection(baseCohort.getMemberIds(), PMTCTMothers.getMemberIds());

            cohortIds = convertToCommaSeparatedString(allMothers);

            String query = " SELECT audit_tool.*,C.PMTCT_STATUS," +
                    "C.id as EID_id, C.client_id as EID_mother, EDD, EID_NO, EID_DOB, EID_AGE, EID_WEIGHT, EID_NEXT_APPT, EID_FEEDING, CTX_START, CTX_AGE, 1ST_PCR_DATE, 1ST_PCR_AGE, 1ST_PCR_RESULT, 1ST_PCR_RECEIVED, 2ND_PCR_DATE, " +
                    "2ND_PCR_AGE, 2ND_PCR_RESULT, 2ND_PCR_RECEIVED, REPEAT_PCR_DATE, REPEAT_PCR_AGE, REPEAT_PCR_RESULT, REPEAT_PCR_RECEIVED, RAPID_PCR_DATE, RAPID_PCR_AGE, RAPID_PCR_RESULT, FINAL_OUTCOME, LINKAGE_NO, NVP_AT_BIRTH,BREAST_FEEDING_STOPPED, " +
                    " PMTCT_ENROLLMENT_DATE FROM mamba_fact_eid_patients C\n" +
                    "         inner join mamba_fact_audit_tool_art_patients audit_tool on C.client_id =audit_tool.client_id where C.client_id in (" + cohortIds + ")";

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
        }

        if (!eidBaseCohort.isEmpty() ) {

            cohortIds = convertToCommaSeparatedString(eidBaseCohort.getMemberIds());

            String query = "SELECT baby,last_encounter_date, appt_date,EID_NO, EID_DOB, EID_AGE, EID_WEIGHT, EID_NEXT_APPT, EID_FEEDING, CTX_START, CTX_AGE, 1ST_PCR_DATE, 1ST_PCR_AGE, 1ST_PCR_RESULT, 1ST_PCR_RECEIVED, 2ND_PCR_DATE,\n" +
            "                    2ND_PCR_AGE, 2ND_PCR_RESULT, 2ND_PCR_RECEIVED, REPEAT_PCR_DATE, REPEAT_PCR_AGE, REPEAT_PCR_RESULT, REPEAT_PCR_RECEIVED, RAPID_PCR_DATE, RAPID_PCR_AGE, RAPID_PCR_RESULT, FINAL_OUTCOME, LINKAGE_NO, NVP_AT_BIRTH,BREAST_FEEDING_STOPPED,PMTCT_STATUS\n" +
                    "FROM mamba_fact_eid_patients c\n" +
                    "         LEFT JOIN (SELECT e.patient_id, MAX(encounter_datetime) last_encounter_date\n" +
                    "                    FROM encounter e\n" +
                    "                             INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id\n" +
                    "                    WHERE e.voided = 0\n" +
                    "                      AND et.uuid IN ('9fcfcc91-ad60-4d84-9710-11cc25258719', '4345dacb-909d-429c-99aa-045f2db77e2b')\n" +
                    "                    GROUP BY patient_id) last_enc ON c.baby = last_enc.patient_id\n" +
                    "         LEFT JOIN (SELECT o.person_id, MAX(value_datetime) appt_date\n" +
                    "                    FROM obs o\n" +
                    "                             INNER JOIN encounter e ON o.encounter_id = e.encounter_id\n" +
                    "                             INNER JOIN encounter_type et ON e.encounter_type = et.encounter_type_id AND\n" +
                    "                                                             et.uuid = '4345dacb-909d-429c-99aa-045f2db77e2b'\n" +
                    "                    WHERE concept_id = 5096\n" +
                    "                      AND o.voided = 0\n" +
                    "                    GROUP BY person_id) a ON c.baby = a.person_id where c.baby in (" + cohortIds + ") and c.client_id is NULL";

            List<Object[]> results = getEtl(query);
            PatientDataHelper pdh = new PatientDataHelper();
            if (results.size() > 0 && !results.isEmpty()) {
                for (Object[] o : results) {
                    DataSetRow row = new DataSetRow();

                    pdh.addCol(row, "last_visit_date", o[1]);
                    pdh.addCol(row, "next_appointment_date", o[2]);
                    pdh.addCol(row, "Preg", o[30]);

                    pdh.addCol(row, "EID_NO", o[3]);
                    pdh.addCol(row, "EID_DOB", o[4]);
                    pdh.addCol(row, "EID_AGE", o[5]);
                    pdh.addCol(row, "EID_WEIGHT", o[6]);
                    pdh.addCol(row, "EID_NAPPT", o[7]);
                    pdh.addCol(row, "feeding", o[8]);
                    pdh.addCol(row, "CTX_DATE", o[9]);
                    pdh.addCol(row, "CTX_AGE", o[10]);
                    pdh.addCol(row, "1PCR_DATE", o[11]);
                    pdh.addCol(row, "1PCR_AGE", o[12]);
                    pdh.addCol(row, "1PCR_RESULT", o[13]);
                    pdh.addCol(row, "1PCR_GIVEN", o[14]);
                    pdh.addCol(row, "2PCR_DATE", o[15]);
                    pdh.addCol(row, "2PCR_AGE", o[16]);
                    pdh.addCol(row, "2PCR_RESULT", o[17]);
                    pdh.addCol(row, "2PCR_GIVEN", o[18]);

                    pdh.addCol(row, "3PCR_DATE", o[19]);
                    pdh.addCol(row, "3PCR_AGE", o[20]);
                    pdh.addCol(row, "3PCR_RESULT", o[21]);
                    pdh.addCol(row, "3PCR_GIVEN", o[22]);

                    pdh.addCol(row, "RAPID_DATE", o[23]);
                    pdh.addCol(row, "RAPID_AGE", o[24]);
                    pdh.addCol(row, "RAPID_RESULT", o[25]);
                    pdh.addCol(row, "OUTCOME", o[26]);
                    pdh.addCol(row, "LINKAGE_NO", o[27]);
                    pdh.addCol(row, "NVP", o[28]);
                    pdh.addCol(row, "STOPPED_BF", o[29]);


//                    adding nulls for mother side
                    pdh.addCol(row, "EDD", "");
                    pdh.addCol(row, "id", "");
                    pdh.addCol(row, "nationality", "");
                    pdh.addCol(row, "gender", "");
                    pdh.addCol(row, "date_of_birth", "");
                    pdh.addCol(row, "age", "");
                    pdh.addCol(row, "marital_status", "");

                    pdh.addCol(row, "special_category", "");

                    pdh.addCol(row, "client_status", "");

                    pdh.addCol(row, "art_start_date", "");

                    pdh.addCol(row, "current_regimen", "");
                    pdh.addCol(row, "regimen_line", "");
                    pdh.addCol(row, "current_arv_regimen_start_date", "");
                    pdh.addCol(row, "adherence", "");
                    pdh.addCol(row, "prescription_duration", "");

                    pdh.addCol(row, "current_vl", "");
                    pdh.addCol(row, "vl_result_sample_date", "");
                    pdh.addCol(row, "new_vl_sample_date", "");


                    pdh.addCol(row, "iacs_no", "");
                    pdh.addCol(row, "repeat_vl_collection_date", "");
                    pdh.addCol(row, "repeat_vl_results_after_iacs", "");
                    pdh.addCol(row, "hivdrt_results", "");
                    pdh.addCol(row, "date_dr_results_received", "");
                    pdh.addCol(row, "decision", "");
                    pdh.addCol(row, "pss", "");
                    pdh.addCol(row, "ovc_screening", "");
                    pdh.addCol(row, "nutrition_status", "");
                    pdh.addCol(row, "family_planning_status", "");
                    pdh.addCol(row, "cacx_screening", "");

                    pdh.addCol(row, "hepatitis_b_status", "");
                    pdh.addCol(row, "syphillis_status", "");

                    pdh.addCol(row, "tpt_status", "");
                    pdh.addCol(row, "tb_status", "");
                    pdh.addCol(row, "tb_lam_crag_results", "");
                    pdh.addCol(row, "who_stage", "");
                    pdh.addCol(row, "advanced_disease", "");
                    pdh.addCol(row, "duration_on_art", "");
                    pdh.addCol(row, "side_effects", "");
                    pdh.addCol(row, "sample_type", "");
                    pdh.addCol(row, "known_status_children", "");
                    pdh.addCol(row, "pos_status_children", "");
                    pdh.addCol(row, "known_status_spouse", "");
                    pdh.addCol(row, "po_status_spouse", "");
                    pdh.addCol(row, "age_group", "");
                    pdh.addCol(row, "PMTCT_DATE", "");
                    dataSet.addRow(row);
                }
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

    private List<Object[]> getEtl(String q) {
        System.out.println("query "+ q);
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

