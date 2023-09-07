package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.Concept;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.Column;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.DataExportMapper;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.Cohort;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + DataDefinitionRestController.UGANDAEMRREPORTS + DataDefinitionRestController.DATA_DEFINITION)

public class DataDefinitionRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_DEFINITION = "/dataDefinition";


    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public Object evaluate(@RequestBody DataExportMapper payload, RequestContext requestContext) {

        Cohort reportCohort = payload.getCohort();
        List<Column> columnList = payload.getColumns();

        EvaluationContext context = new EvaluationContext();
        SimpleDataSet dataSet = new SimpleDataSet(new PatientDataSetDefinition(), context);
        org.openmrs.Cohort baseCohort = new org.openmrs.Cohort();
        if (reportCohort.getUuid() != null && !columnList.isEmpty()) {
            try {
                ReportDefinitionService service = Context.getService(ReportDefinitionService.class);
                ReportDefinition rd = service.getDefinitionByUuid(reportCohort.getUuid());

                if (rd != null) {
                    Mapped<? extends CohortDefinition> cd = rd.getBaseCohortDefinition();

                    if (cd != null) {
                        baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cd, context);


                        List<Map<String, Object>> parameters = reportCohort.getParameters();

                        Map<String, Object> cohortParameters = getParameters(parameters);
                        ReflectionUtil.setPropertyValue(cd, "startDate", cohortParameters.get("startDate"));
                        ReflectionUtil.setPropertyValue(cd, "endDate", cohortParameters.get("endDate"));

                        context.setParameterValues(cohortParameters);

                        baseCohort = Context.getService(CohortDefinitionService.class).evaluate(cd, context);


                        HashMap<String, List<Object[]>> columns = getColumnsData(columnList, baseCohort);
                        for (Integer i : baseCohort.getMemberIds()) {

                            DataSetRow row = new DataSetRow();

                            PatientDataHelper pdh = new PatientDataHelper();
                            for (String key : columns.keySet()) {
                                Object obj = "";
                                List<Object[]> objects = columns.get(key);
                                if(!objects.isEmpty()) {
                                    for (Object[] object : objects) {
                                        int patientId = (int) object[0];

                                        if (patientId == i) {
                                            obj = object[1];
                                        }
                                    }
                                }
                                pdh.addCol(row, key, obj);
                            }
                            dataSet.addRow(row);

                        }

                    } else {
                        return new ResponseEntity<Object>(" No base cohort for this report", HttpStatus.INTERNAL_SERVER_ERROR);
                    }


                }

            } catch (EvaluationException e) {
                return new ResponseEntity<Object>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

            }
        }
        List<SimpleObject> traceReportData = new ArrayList<SimpleObject>();
        traceReportData.addAll(convertDataSetToSimpleObject(dataSet));
        return new ResponseEntity<>(traceReportData, HttpStatus.OK);

    }

    private Map<String, Object> getParameters(List<Map<String, Object>> list) {

        Map<String, Object> parameterValues = new HashMap<String, Object>();
        if (!list.isEmpty()) {
            for (Map<String, Object> objectMap : list) {
                Iterator<String> keys = objectMap.keySet().iterator();
                while (keys.hasNext()) {
                    String key = keys.next();
                    String mapValue = (String) objectMap.get(key);
                    parameterValues.put(key, DateUtil.parseYmd(mapValue));
                }
            }
        }

        return parameterValues;
    }

    private List<Object[]> getPersonNames(org.openmrs.Cohort cohort, String parameter, EvaluationContext context) {
        String query = " Select person_id, " + parameter + " from person_name where person_id in ( " + cohort.getCommaSeparatedPatientIds() + ") and voided =0 group by person_id";
        EvaluationService evaluationService = Context.getService(EvaluationService.class);
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        return results;

    }

    private List<Object[]> getIdentifiers(org.openmrs.Cohort cohort, String identifierUuid, EvaluationContext context) {
        String query = " Select patient_id, identifier from patient_identifier pi inner join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id where pit.uuid='" + identifierUuid + "' and patient_id in ( " + cohort.getCommaSeparatedPatientIds() + ")   group by patient_id ";
        EvaluationService evaluationService = Context.getService(EvaluationService.class);
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        return results;

    }

    private List<Object[]> getPersonAttributes(org.openmrs.Cohort cohort, String attributeUuid, EvaluationContext context) {
        String query = "SELECT  pa.person_id, pa.value from person_attribute pa INNER join person_attribute_type pat on pa.person_attribute_type_id = pat.person_attribute_type_id where pa.voided=0  and  pat.uuid='" + attributeUuid + "' and person_id in ( " + cohort.getCommaSeparatedPatientIds() + ")   group by person_id ";
        EvaluationService evaluationService = Context.getService(EvaluationService.class);
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        return results;

    }

    private List<Object[]> getConceptPersonAttributes(org.openmrs.Cohort cohort, String attributeUuid, EvaluationContext context) {
        String query = "SELECT  pa.person_id, cn.name from person_attribute pa INNER join person_attribute_type pat on pa.person_attribute_type_id = pat.person_attribute_type_id left join concept_name cn on cn.concept_id = pa.value and cn.locale='en' and cn.concept_name_type='FULLY_SPECIFIED' where pa.voided=0  and  pat.uuid='" + attributeUuid + "' and person_id in ( " + cohort.getCommaSeparatedPatientIds() + ")   group by person_id ";
        EvaluationService evaluationService = Context.getService(EvaluationService.class);
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Object[]> results = evaluationService.evaluateToList(q, context);
        return results;

    }

    private HashMap<String, List<Object[]>> getColumnsData(List<Column> columnList, org.openmrs.Cohort baseCohort) {
        HashMap<String, List<Object[]>> columns = new HashMap<>();
        EvaluationContext context = new EvaluationContext();
        for (Column column : columnList) {
            String expression = column.getExpression();
            String type = column.getType();
            String column_label = column.getLabel();

            if (isExpressionAConcept(expression)) {
                try {
                    List<Object[]> fields = getLatestConceptData(type, expression, baseCohort,context);
                    columns.put(column_label, fields);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                if (type.equals("PatientIdentifier")) {

                    List<Object[]> identifiers = getIdentifiers(baseCohort, expression, context);
                    columns.put(column_label, identifiers);

                } else if (type.equals("PersonName")) {

                    List<Object[]> returnedNames = getPersonNames(baseCohort, expression, context);
                    columns.put(column_label, returnedNames);
                } else if (type.equals("PersonAttribute")) {
                    if (Objects.equals(expression, "8d871f2a-c2cc-11de-8d13-0010c6dffd0f") || Objects.equals(expression, "dec484be-1c43-416a-9ad0-18bd9ef28929")) {
                        List<Object[]> attributes = getConceptPersonAttributes(baseCohort, expression, context);
                        columns.put(column_label, attributes);
                    } else {
                        List<Object[]> attributes = getPersonAttributes(baseCohort, expression, context);
                        columns.put(column_label, attributes);

                    }
                }
            }

        }
        return columns;
    }

    public List<SimpleObject> convertDataSetToSimpleObject(DataSet d) {
        Iterator iterator = d.iterator();

        List<SimpleObject> dataList = new ArrayList<SimpleObject>();
        while (iterator.hasNext()) {
            DataSetRow r = (DataSetRow) iterator.next();
            Map<String, Object> columns = r.getColumnValuesByKey();
            Set<String> keys = columns.keySet();
            SimpleObject details = new SimpleObject();

            for (String key : keys) {
                details.add(key, r.getColumnValue(key));
            }
            dataList.add(details);

        }
        return dataList;
    }

    private boolean isExpressionAConcept(String conceptUuid) {
        boolean isConcept = false;
        ConceptService conceptService = Context.getConceptService();
        Concept concept = conceptService.getConceptByUuid(conceptUuid);
        if (concept != null) {
            isConcept = true;
        }
        return isConcept;
    }

    private List<Object[]> getLatestConceptData(String encounterTypeUuid, String conceptUuid, org.openmrs.Cohort baseCohort, EvaluationContext context) throws IOException {
        File[] files = Helper.getMambaConfigFiles();
        File file = Helper.getFileContainingEncounterUuid(files, encounterTypeUuid);
        List<Object[]> results = new ArrayList<>();
        if (file != null) {
            String columnName = Helper.getColumnNameInTable(conceptUuid, file);
            String encounterTable = Helper.getMambaTableFromFile(file);
            String query ="SELECT A.client_id, "+ columnName +" , latest_encounter as obs_ecounter_date from "+ encounterTable + " mfeac inner join\n" +
                    "(SELECT client_id, MAX(encounter_datetime) latest_encounter\n" +
                    "FROM "+  encounterTable + " \n" +
                    "WHERE "+ columnName +" IS NOT NULL\n" +
                    "GROUP BY client_id)A on A.latest_encounter = mfeac.encounter_datetime and A.client_id=mfeac.client_id "+ " where A.client_id in ( " + baseCohort.getCommaSeparatedPatientIds() + ")   group by client_id";

            EvaluationService evaluationService = Context.getService(EvaluationService.class);
            SqlQueryBuilder q = new SqlQueryBuilder();
            q.append(query);
            results = evaluationService.evaluateToList(q, context);

        }
        return results;
    }
}
