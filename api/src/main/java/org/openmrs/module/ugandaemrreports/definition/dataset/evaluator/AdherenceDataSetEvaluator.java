package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.stat.StatUtils;
import org.joda.time.LocalDate;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.LocationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.encounter.EvaluatedEncounterData;
import org.openmrs.module.reporting.data.encounter.definition.ObsForEncounterDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.SqlPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.*;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.Periods;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AdherenceDataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EIDCohortDataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1BDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by carapai on 17/10/2016.
 */
@Handler(supports = {AdherenceDataSetDefinition.class})
public class AdherenceDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private LocationService locationService;

    @Override
    public MapDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        MapDataSet dataSet = new MapDataSet(dataSetDefinition, evaluationContext);
        AdherenceDataSetDefinition definition = (AdherenceDataSetDefinition) dataSetDefinition;

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        String q = String.format("SELECT person_id,value_coded FROM obs WHERE concept_id = 90221 " +
                "AND obs_datetime BETWEEN '%s' AND '%s' AND voided = 0", startDate, endDate);
        SqlQueryBuilder query = new SqlQueryBuilder(q);
        List<Object[]> results = evaluationService.evaluateToList(query, evaluationContext);

        Multimap<Integer, Integer> allClients = ArrayListMultimap.create();

        for (Object[] o : results) {
            allClients.put(Integer.valueOf(String.valueOf(o[1])), Integer.valueOf(String.valueOf(o[0])));
        }

        Integer good = allClients.get(90156) == null ? 0 : allClients.get(90156).size();
        Integer fair = allClients.get(90157) == null ? 0 : allClients.get(90157).size();
        Integer poor = allClients.get(90158) == null ? 0 : allClients.get(90158).size();

        dataSet.addData(new DataSetColumn("location", "location", String.class),locationService.getDefaultLocation().getDisplayString());
        dataSet.addData(new DataSetColumn("1a", "1a", Integer.class), good);
        dataSet.addData(new DataSetColumn("1b", "1b", Integer.class), fair);
        dataSet.addData(new DataSetColumn("1c", "1c", Integer.class), poor);
        dataSet.addData(new DataSetColumn("1d", "1d", Integer.class), allClients.size());

        return dataSet;
    }
}