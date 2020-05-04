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

        String dataQuery = String.format("SELECT AVG(dd.value_numeric) as median_val from\n" +
                "(select p.value_numeric, @rownum:=@rownum+1 as `row_number`, @total_rows:=@rownum from (SELECT @rownum:=0) r, obs p inner join  encounter e on p.encounter_id = e.encounter_id  inner join encounter_type t on  t.encounter_type_id =e.encounter_type\n" +
                "where p.obs_datetime between '%s' and '%s' and  t.uuid='8d5b27bc-c2cc-11de-8d13-0010c6dffd0f' and p.concept_id= 99071 and p.person_id in (select person_id from obs o  where o.voided = 0 and o.concept_id = 99161 and  o.value_datetime between '%s' and '%s')  order by p.value_numeric)  as dd\n" +
                "where dd.row_number IN ( FLOOR((@total_rows+1)/2), FLOOR((@total_rows+2)/2) );",startDate,endDate,startDate,endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(dataQuery);
//        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);
        List<Object[]> results = evaluationService.evaluateToList(q, context);

        if(results.size()>0 && !results.isEmpty()&& results.get(0)[0]==null){
            pdh.addCol(row, "14y",String.valueOf(results.get(0)[0]));
        }else{
            pdh.addCol(row, "14y","");
        }

        dataSet.addRow(row);
        return dataSet;
    }

}
