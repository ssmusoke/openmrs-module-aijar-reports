package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.hibernate.type.StandardBasicTypes;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.MapDataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AdherenceDataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AppointmentDataSetDefinition;
import org.hibernate.SQLQuery;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.HashMap;
import java.util.List;


@Handler(supports = {AppointmentDataSetDefinition.class})
public class AppointmentDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    ConceptService conceptService;

    @Autowired
    PatientService patientService;
    @Autowired
    private DbSessionFactory sessionFactory;

    @Autowired
    private LocationService locationService;

    private EvaluationContext context;
    @Override
    public SimpleDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        AppointmentDataSetDefinition definition = (AppointmentDataSetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        PatientDataHelper pdh = new PatientDataHelper();

        context = evaluationContext;
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, evaluationContext);

        List<Object[]> resultSet = getEtl(startDate,endDate);

        if (resultSet.size() > 0 && !resultSet.isEmpty()) {
            for (Object[] e : resultSet) {
                DataSetRow row = new DataSetRow();
                pdh.addCol(row, "Clinic No", e[0]);
                pdh.addCol(row, "EID No", e[1]);
                pdh.addCol(row, "Patient Name", e[2]);
                pdh.addCol(row, "Sex", e[3]);
                pdh.addCol(row, "Birth Date", e[4]);
                pdh.addCol(row, "Age", e[5]);
                pdh.addCol(row, "Parish", e[6]);
                pdh.addCol(row, "Village", e[7]);

                dataSet.addRow(row);

            }
        }


        return dataSet;
    }

    private List<Object[]> getEtl(String startDate ,String endDate) {
        SQLQuery txCurrQuery = sessionFactory.getCurrentSession().createSQLQuery("sp_fact_encounter_hiv_art_query(:startDate,:endDate)");
        txCurrQuery.setParameter("startDate", startDate);
        txCurrQuery.setParameter("endDate", endDate);
        List<Object[]> txCurrResult = txCurrQuery.list();
        return txCurrResult;
    }

    public DbSessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public void setSessionFactory(DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }
}