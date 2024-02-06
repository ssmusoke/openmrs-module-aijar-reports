package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;


import antlr.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.json.StringEscapeUtils;
import org.hibernate.SQLQuery;
import org.openmrs.annotation.Handler;
import org.openmrs.api.db.hibernate.DbSessionFactory;
import org.openmrs.module.reporting.common.DateUtil;
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

    @Autowired
    private DbSessionFactory sessionFactory;

    @Override
    public SimpleDataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        AggregateReportDataSetDefinition definition = (AggregateReportDataSetDefinition) dataSetDefinition;

        SimpleDataSet dataSet = new SimpleDataSet(definition, evaluationContext);
        DataSetRow row = getReportQuery(definition, evaluationContext);
        dataSet.addRow(row);

//        List<Object[]> resultSet = getEtl(startDate,endDate);


        return dataSet;
    }

    private List<Object[]> getEtl(String q, EvaluationContext context) {
        SqlQueryBuilder query = new SqlQueryBuilder(q);
        List<Object[]> results = evaluationService.evaluateToList(query, context);
        return results;
    }

    private DataSetRow getReportQuery(AggregateReportDataSetDefinition definition, EvaluationContext evaluationContext) {
        DataSetRow row = new DataSetRow();
        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");
        File file = definition.getReportDesign();

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode rootNode = objectMapper.readTree(file);
            JsonNode reportFieldsArray = rootNode.path("report_fields");

            for (JsonNode reportField : reportFieldsArray) {
                String indicatorName = reportField.path("indicator_name").asText();
                String sqlQuery = reportField.path("sqlQuery").toString();
                String clean_sqlQuery = sqlQuery.replace("\\n", " ");
                sqlQuery = clean_sqlQuery.replace("\"", " ");


                // Replace placeholders with real dates
                String query = sqlQuery.replace(":startDate", startDate)
                        .replace(":endDate", endDate);

                List<Object[]> results = getEtl(query, evaluationContext);
                if (reportField.has("values")) {

                    List<ValueHolder> convertedResults = convertToValueHolderList(results);
                    row = placesValuesToDataSetRow(reportField, convertedResults, row);
                } else if (reportField.has("value_place_holder")) {
                    Object[] result = results.get(0);
                    ValueHolder convertedResult = convertToValueHolderList(result);
                    row = placesValueToDataSetRow(reportField, convertedResult, row);
                }

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return row;
    }

    private List<Object[]> getList(String query) {
        SQLQuery txCurrQuery = sessionFactory.getCurrentSession().createSQLQuery(query);
        List<Object[]> txCurrResult = txCurrQuery.list();
        return txCurrResult;
    }

    public DbSessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public void setSessionFactory(DbSessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    private static DataSetRow placesValuesToDataSetRow(JsonNode reportField, List<ValueHolder> values, DataSetRow row) {
        JsonNode valuesArray = reportField.path("values");
        PatientDataHelper pdh = new PatientDataHelper();


        for (JsonNode valueObject : valuesArray) {
            String dissaggregations1 = valueObject.path("dissaggregations1").asText();
            String dissaggregations2 = valueObject.path("dissaggregations2").asText();
            String valuePlaceHolder = valueObject.path("value_place_holder").asText();
            ValueHolder valueHolder;
            if (!values.isEmpty()) {
                valueHolder = values.stream().filter(v -> v.getDisag1().equals(dissaggregations1)).filter(v -> v.getDisag2().equals(dissaggregations2)).findFirst().orElse(null);
            } else {
                valueHolder = null;
            }
            int count = 0;
            if (valueHolder != null) {
                count = Integer.parseInt(valueHolder.getPlaceholder());
                pdh.addCol(row, valuePlaceHolder, count);
            } else {
                pdh.addCol(row, valuePlaceHolder, count);
            }

        }
        return row;
    }

    private static DataSetRow placesValueToDataSetRow(JsonNode reportField, ValueHolder valueHolder, DataSetRow row) {
        String valuePlaceHolder = reportField.path("value_place_holder").asText();
        PatientDataHelper pdh = new PatientDataHelper();

        int count = 0;
        if (valueHolder != null) {
            count = Integer.parseInt(valueHolder.getPlaceholder());
            pdh.addCol(row, valuePlaceHolder, count);
        } else {
            pdh.addCol(row, valuePlaceHolder, count);
        }


        return row;
    }

    public static List<ValueHolder> convertToValueHolderList(List<Object[]> results) {
        List<ValueHolder> valueHolderList = new ArrayList<>();

        if (results.size() > 0) {
            for (Object[] result : results) {
                //order in the Object[] array is disag1, disag2, placeholder
                String disag1 = result[0].toString();
                String disag2 = result[1].toString();
                String placeholder = result[2].toString();

                ValueHolder valueHolder = new ValueHolder(disag1, disag2, placeholder);
                valueHolderList.add(valueHolder);
            }
        }
        return valueHolderList;
    }

    public static ValueHolder convertToValueHolderList(Object[] result) {

        String disag1 = result[0].toString();
        String disag2 = null;
        String placeholder = result[1].toString();

        ValueHolder valueHolder = new ValueHolder(disag1, disag2, placeholder);

        return valueHolder;
    }
}

class ValueHolder {
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