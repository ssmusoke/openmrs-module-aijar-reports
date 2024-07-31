package org.openmrs.module.ugandaemrreports.web.resources;

import javassist.expr.Instanceof;
import org.openmrs.*;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.logic.op.In;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reportingcompatibility.service.ReportingCompatibilityService;
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

        Cohort cohort = payload.getCohort();
        List<Column> columnList = payload.getColumns();

        EvaluationContext context = new EvaluationContext();
        SimpleDataSet dataSet = new SimpleDataSet(new PatientDataSetDefinition(), context);
        org.openmrs.cohort.Cohort baseCohort = new org.openmrs.cohort.Cohort();
        if (cohort.getUuid() != null && !columnList.isEmpty()) {
            try {
                baseCohort = Helper.getCohortMembers(cohort);

                if (!baseCohort.isEmpty()) {

                    HashMap<String, Object> columns = getColumnsData(columnList, baseCohort);
                    for (Integer i : baseCohort.getMemberIds()) {

                        DataSetRow row = new DataSetRow();

                        PatientDataHelper pdh = new PatientDataHelper();
                        for (String key : columns.keySet()) {
                            Object obj = "";
                            Object object = columns.get(key);
                            if (object!=null) {
                                if (object instanceof Map) {
                                    Map<Integer, Object> map = (Map<Integer, Object>) object;
                                    obj = map.get(i);

                                } else if (object instanceof List) {

                                    List<Object[]> objects = (List<Object[]>)object;
                                    for (Object[] object1 : objects) {
                                        int patientId = (int) object1[0];

                                        if (patientId == i) {
                                            obj = object1[1];
                                        }
                                    }
                                } else {
                                    System.out.println("The variable is neither a Map nor a String.");
                                }

                            }
                            pdh.addCol(row, key, obj);
                        }
                        dataSet.addRow(row);

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

    private Map<Integer, Object> getPersonNames(org.openmrs.cohort.Cohort cohort, String parameter, EvaluationContext context) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PatientService patientService = Context.getPatientService();
        PersonService personService = Context.getPersonService();

        return rcs.getPatientAttributes(cohort,"PersonName."+parameter,false);

    }

    private Map<Integer, String> getIdentifiers(org.openmrs.cohort.Cohort cohort, String identifierUuid, EvaluationContext context) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PatientService patientService = Context.getPatientService();
        PersonService personService = Context.getPersonService();

        PatientIdentifierType type =  patientService.getPatientIdentifierTypeByUuid(identifierUuid);
        return rcs.getPatientIdentifierStringsByType(cohort,type);

    }

    private Map<Integer, Object> getPersonAttributes(org.openmrs.cohort.Cohort cohort, String attributeUuid, EvaluationContext context) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PatientService patientService = Context.getPatientService();
        PersonService personService = Context.getPersonService();

        PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByUuid(attributeUuid);
        return  rcs.getPersonAttributes(cohort,personAttributeType.getName(),null,null,null, false);

    }

    private Map<Integer, Object> getConceptPersonAttributes(org.openmrs.cohort.Cohort cohort, String attributeUuid, EvaluationContext context) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PatientService patientService = Context.getPatientService();
        PersonService personService = Context.getPersonService();
        PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByUuid(attributeUuid);
        return  rcs.getPersonAttributes(cohort,personAttributeType.getName(),"ConceptName","concept","name", false);
    }

    private HashMap<String, Object> getColumnsData(List<Column> columnList, org.openmrs.cohort.Cohort baseCohort) {
        HashMap<String, Object> columns = new HashMap<>();
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

                    Map<Integer, String> identifiers = getIdentifiers(baseCohort, expression, context);
                    columns.put(column_label, identifiers);

                } else if (type.equals("PersonName")) {
                    Map<Integer, Object> returnedNames = getPersonNames(baseCohort, expression, context);
                    columns.put(column_label, returnedNames);
                } else if (type.equals("PersonAttribute")) {
                    if (Objects.equals(expression, "8d871f2a-c2cc-11de-8d13-0010c6dffd0f") || Objects.equals(expression, "dec484be-1c43-416a-9ad0-18bd9ef28929")) {
                        Map<Integer, Object> attributes = getConceptPersonAttributes(baseCohort, expression, context);
                        columns.put(column_label, attributes);
                    } else {
                        Map<Integer, Object> attributes = getPersonAttributes(baseCohort, expression, context);
                        columns.put(column_label, attributes);

                    }
                }
//                } else if (type.equals("Demographics")) {
//                    if (expression.equals("Age")) {
//                        dataExportReportObject.addSimpleColumn(column_label, "$!{fn.calculateAge($fn.getPatientAttr('Person', 'birthdate'))}");
//                    } else {
//                        dataExportReportObject.addSimpleColumn(column_label, "$!{fn.getPatientAttr('Person', '" + expression + "')}");
//                    }
//                } else if (type.equals("Address")) {
//                    dataExportReportObject.addSimpleColumn(column_label, "$!{fn.getPatientAttr('PersonAddress', '" + expression + "')}");
//                }
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

    private List<Object[]> getLatestConceptData(String encounterTypeUuid, String conceptUuid, org.openmrs.cohort.Cohort baseCohort, EvaluationContext context) throws IOException {
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
