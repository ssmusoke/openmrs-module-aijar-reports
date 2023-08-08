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

        Cohort baseCohort =null;

        if (cohortSelected != null) {

            if (cohortSelected.equals("Patients with encounters")) {
                EncounterCohortDefinition cd = new EncounterCohortDefinition();
                cd.setEncounterTypeList(hivMetadata.getArtEncounterTypes());
                cd.setOnOrAfter(definition.getStartDate());
                cd.setOnOrBefore(definition.getEndDate());

                baseCohort =Context.getService(CohortDefinitionService.class).evaluate(cd, context);

            } else if (cohortSelected.equals("Patients on appointment")) {
                SqlCohortDefinition appointmentCohortDefinition = new SqlCohortDefinition( "SELECT client_id\n" +
                        "FROM (SELECT client_id, MAX(return_visit_date) returndate FROM mamba_fact_encounter_hiv_art_card GROUP BY client_id) a\n" +
                        "WHERE returndate BETWEEN '"+ startDate +"' AND '"+ endDate + "]"+"';");

                baseCohort =Context.getService(CohortDefinitionService.class).evaluate(appointmentCohortDefinition, context);
            }

        }
        String cohortIds ="";

        if(baseCohort.getMemberIds().size()>0 && baseCohort!=null){
            cohortIds = setToCommaSeparatedString(baseCohort.getMemberIds());
        }
        String query ="SELECT *\n" +
                "    FROM mamba_fact_audit_tool_art_patients audit_tool where client_id in ("+ cohortIds + ")";

        List<Object[]> results = getEtl(query);
        PatientDataHelper pdh = new PatientDataHelper();
        if(results.size()>0 && !results.isEmpty()) {
            for (Object[] o : results) {
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "ID", o[2]);
                pdh.addCol(row, "Gender", o[3]);
                pdh.addCol(row, "Date of Birth",  o[4]);
                pdh.addCol(row, "Age", o[5]);
                pdh.addCol(row, "DSDM", o[8]);


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
