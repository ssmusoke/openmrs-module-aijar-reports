package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
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
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AggregateReportDataSetDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


@Handler(supports = {AggregateReportDataSetDefinition.class})
public class AggregateReportDataSetEvaluator implements DataSetEvaluator {

    @Autowired
    EvaluationService evaluationService;


    @Override
    public SimpleDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        AggregateReportDataSetDefinition definition = (AggregateReportDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet =getReportQuery(definition,evaluationContext);


//        List<Object[]> resultSet = getEtl(startDate,endDate);




        return dataSet;
    }

    private List<Object[]> getEtl(String q, EvaluationContext context) {
        SqlQueryBuilder query = new SqlQueryBuilder();
        query.append(q);
        List<Object[]> results = evaluationService.evaluateToList(query, context);
        return results;
    }

    private SimpleDataSet getReportQuery(AggregateReportDataSetDefinition definition,EvaluationContext evaluationContext) {
        SimpleDataSet dataSet = new SimpleDataSet(definition, evaluationContext);
        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");
        File file = definition.getReportDesign();

        ObjectMapper objectMapper = new ObjectMapper();
        String query ="";
        try {
            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode reportFieldsArray = rootNode.path("report_fields");

            for (JsonNode reportField : reportFieldsArray) {
                // Get all fields of the current object
                String indicatorName = reportField.path("indicator_name").asText();
                String databaseTable = reportField.path("database_table").asText();
                String base_column = reportField.path("base_column").asText();
                String field = reportField.path("table_column").asText();
                String operation = reportField.path("operation").asText();

                // Get dissaggregations array
                JsonNode disaggregationArray = reportField.path("dissaggregations");
                StringBuilder group_by_columnns = new StringBuilder();
                List<String> disaggregationValues = new ArrayList<>();
                for (JsonNode disaggregation : disaggregationArray) {
                    String disaggregationValue = disaggregation.asText();
                    group_by_columnns.append(disaggregationValue);
                    group_by_columnns.append(",");
                    disaggregationValues.add(disaggregationValue);
                }
                String group_by = StringUtils.stripEnd(group_by_columnns.toString(), ",");

                // Get parameters array
                JsonNode parametersArray = reportField.path("parameters");
                List<String> parameterValues = new ArrayList<>();
                for (JsonNode parameter : parametersArray) {
                    String parameterValue = parameter.asText();
                    parameterValues.add(parameterValue);
                }

                 query = "SELECT " + group_by + ", count(" + base_column + ") FROM " + databaseTable ;

                // this part only works for date fields
                if(!parameterValues.isEmpty()){
                       String whereClause = " WHERE " + field + " " ;
                       if(operation.equals("between")&& parameterValues.contains("startDate")&& parameterValues.contains("endDate")){
                          whereClause =  whereClause + " >= '" +startDate + "' AND " +  " "+ field + " <= '" + endDate+ "'";

                       }else if (operation.equals("lessOrEqual")&& parameterValues.contains("endDate")){
                           whereClause =  whereClause + " <= '" +endDate +"'";
                       }else if (operation.equals("greaterOrEqual")&& parameterValues.contains("startDate")){
                           whereClause =  whereClause + " >= '" +startDate+"'" ;
                       }
                       query =query + whereClause;
                }

                if(!disaggregationValues.isEmpty()){
                    String grouped_by = " GROUP BY "+ group_by ;
                    query = query + grouped_by;
                }
                System.err.println(query);

                List<Object[]> results = getEtl(query,evaluationContext);

                List<ValueHolder> convertedResults = convertToValueHolderList(results);
                List<ValueHolder> values = new ArrayList<ValueHolder>();
                dataSet = placesValuesToDataSet(reportField, convertedResults,dataSet);

                int cols_no = disaggregationValues.size()+1;

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return dataSet;
    }

    private static SimpleDataSet placesValuesToDataSet(JsonNode reportField, List<ValueHolder> values,SimpleDataSet dataSet) {
        JsonNode valuesArray = reportField.path("values");
        PatientDataHelper pdh = new PatientDataHelper();

        for (JsonNode valueObject : valuesArray) {
            DataSetRow row = new DataSetRow();
            String dissaggregations1 = valueObject.path("dissaggregations1").asText();
            String dissaggregations2 = valueObject.path("dissaggregations2").asText();
            String valuePlaceHolder = valueObject.path("value_place_holder").asText();
            ValueHolder valueHolder = values.stream().filter(v -> v.getDisag1().equals(dissaggregations1)).filter(v -> v.getDisag2().equals(dissaggregations2)).findFirst().orElse(null);

            int count =0;
            if(valueHolder!=null){
                 count =Integer.parseInt(valueHolder.getPlaceholder());
                pdh.addCol(row,valuePlaceHolder,count);
            }else{
                pdh.addCol(row,valuePlaceHolder,count);
            }
            System.out.println("Row " + valuePlaceHolder + "with value" + count);
            dataSet.addRow(row);

        }
        return dataSet;
    }

    public static List<ValueHolder> convertToValueHolderList(List<Object[]> results) {
        List<ValueHolder> valueHolderList = new ArrayList<>();

        for (Object[] result : results) {
            // Assuming the order in the Object[] array is disag1, disag2, placeholder
            String disag1 = result[0].toString();
            String disag2 = result[1].toString();
            String placeholder = result[2].toString();

            ValueHolder valueHolder = new ValueHolder(disag1, disag2, placeholder);
            valueHolderList.add(valueHolder);
        }

        return valueHolderList;
    }
}
 class ValueHolder{
    String disag1;
    String disag2;

    String placeholder;

     public ValueHolder(String disag1, String disag2, String placeholder) {
         this.disag1 = disag1;
         this.disag2 = disag2;
         this.placeholder = placeholder;
     }

     public String getDisag1() {
         return disag1;
     }

     public void setDisag1(String disag1) {
         this.disag1 = disag1;
     }

     public String getDisag2() {
         return disag2;
     }

     public void setDisag2(String disag2) {
         this.disag2 = disag2;
     }

     public String getPlaceholder() {
         return placeholder;
     }

     public void setPlaceholder(String placeholder) {
         this.placeholder = placeholder;
     }
 }