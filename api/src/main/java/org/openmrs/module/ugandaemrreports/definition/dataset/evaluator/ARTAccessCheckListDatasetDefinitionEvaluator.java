package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTAccessCheckListDataSetDefinition;

import org.springframework.beans.factory.annotation.Autowired;



/**
 */
@Handler(supports = {ARTAccessCheckListDataSetDefinition.class})
public class ARTAccessCheckListDatasetDefinitionEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    PatientDataHelper pdh = new PatientDataHelper();

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {

        ARTAccessCheckListDataSetDefinition definition = (ARTAccessCheckListDataSetDefinition) dataSetDefinition;


        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);

        EvaluationContext context1 = new EvaluationContext();
        context1.setBaseCohort(Context.getService(CohortM.class));
        return patientDataService.evaluate(definition, context).getData();

        return dataSet;
    }


}
