package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.annotation.Handler;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MedianBaselineCD4DatasetDefinition;
import org.springframework.beans.factory.annotation.Autowired;


import java.util.*;

/**
 */
@Handler(supports = {MedianBaselineCD4DatasetDefinition.class})
public class MedianBaselineCD4DatasetDefinitionEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        MedianBaselineCD4DatasetDefinition definition = (MedianBaselineCD4DatasetDefinition) dataSetDefinition;


        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");
        DataSetRow row = new DataSetRow();

        PatientDataHelper pdh = new PatientDataHelper();

        String dataQuery = String.format("select p.value_numeric from obs p  inner join  encounter e on p.encounter_id = e.encounter_id  inner join encounter_type t on  t.encounter_type_id =e.encounter_type \n" +
                "where p.obs_datetime between '%s' and '%s' and  t.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' and p.concept_id= 99071 and p.person_id in (select person_id from obs o  where o.voided = 0 and o.concept_id = 99161 and  o.value_datetime between '%s' and '%s')",startDate,endDate,startDate,endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);
        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);
        if(results.size()>0 && !results.isEmpty()){
            Integer[] flattened = new Integer[results.size()];
            results.toArray(flattened);
            pdh.addCol(row, "14y",getmedian(flattened));
        }
        dataSet.addRow(row);
        return dataSet;
    }

    private double getmedian(Integer[] results) {
        // sort array
        Arrays.sort(results);
        double median;
        // get count of scores
        int totalElements = results.length;
        // check if total number of scores is even
        if (totalElements % 2 == 0) {
            int sumOfMiddleElements = results[totalElements / 2] +
                    results[totalElements / 2 - 1];
            // calculate average of middle elements
            median = ((double) sumOfMiddleElements) / 2;
        } else {
            // get the middle element
            median = (double) results[results.length / 2];
        }
        return median;
    }

}
