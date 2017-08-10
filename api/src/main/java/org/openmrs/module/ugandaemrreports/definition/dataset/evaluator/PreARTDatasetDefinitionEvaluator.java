package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
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
import org.openmrs.module.ugandaemrreports.common.Helper;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.common.Periods;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.PreARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.TreeMap;

/**
 * Created by carapai on 11/05/2016.
 */
@Handler(supports = {PreARTDatasetDefinition.class})
public class PreARTDatasetDefinitionEvaluator implements DataSetEvaluator {
    @Autowired
    private CommonReportMetadata commonReportMetadata;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        PreARTDatasetDefinition definition = (PreARTDatasetDefinition) dataSetDefinition;

        return dataSet;
    }
}
