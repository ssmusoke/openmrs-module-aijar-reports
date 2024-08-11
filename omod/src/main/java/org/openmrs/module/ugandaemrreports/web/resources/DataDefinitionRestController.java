package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.*;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
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

                    HashMap<String, Object> columns = processColumnsData(columnList, baseCohort);
                    for (Integer i : baseCohort.getMemberIds()) {
                        DataSetRow row = createPatientDataRow(i,columns,columnList);
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

    private Map<Integer, Object> convertDatesToStrings(Object object) {

        Map<Integer, Object> newMap = new HashMap<>();
        if (object instanceof Map) {
            Map<Integer, Object> map = (Map<Integer, Object>) object;
            for (Map.Entry<Integer,Object> entry : map.entrySet()) {
                Integer key = entry.getKey();
                Object objectValue = entry.getValue();
                String stringValue = String.valueOf(objectValue);
                map.put(key,stringValue);
            }

        }
        return newMap;
    }

    private Map<Integer, Object> getPersonNames(org.openmrs.cohort.Cohort cohort, String parameter) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return rcs.getPatientAttributes(cohort,"PersonName."+parameter,false);

    }

    private Map<Integer, String> getIdentifiers(org.openmrs.cohort.Cohort cohort, String identifierUuid) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PatientService patientService = Context.getPatientService();

        PatientIdentifierType type =  patientService.getPatientIdentifierTypeByUuid(identifierUuid);
        return rcs.getPatientIdentifierStringsByType(cohort,type);

    }

    private Map<Integer, Object> getPersonAttributes(org.openmrs.cohort.Cohort cohort, String attributeUuid) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PersonService personService = Context.getPersonService();

        PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByUuid(attributeUuid);
        return  rcs.getPersonAttributes(cohort,personAttributeType.getName(),null,null,null, false);

    }

    private Map<Integer, Object> getConceptPersonAttributes(org.openmrs.cohort.Cohort cohort, String attributeUuid) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        PersonService personService = Context.getPersonService();
        PersonAttributeType personAttributeType = personService.getPersonAttributeTypeByUuid(attributeUuid);
        return  rcs.getPersonAttributes(cohort,personAttributeType.getName(),"ConceptName","concept","name", false);
    }

    private Map<Integer, Object> getAges(org.openmrs.cohort.Cohort cohort) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getPatientAttributes(cohort,"Person.birthdate",false);
    }

    private Map<Integer, Object> getPatientAttributes(org.openmrs.cohort.Cohort cohort,String attribute) {
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getPatientAttributes(cohort,attribute,false);
    }

    private HashMap<String, Object> processColumnsData(List<Column> columnList, org.openmrs.cohort.Cohort baseCohort) {
        HashMap<String, Object> columns = new HashMap<>();
        for (Column column : columnList) {
            String expression = column.getExpression();
            String type = column.getType();
            String column_label = column.getLabel();

            if (isExpressionAConcept(expression)) {
                try {

                    Map<Integer, List<List<Object>>> fields = getLatestObsData(column, baseCohort);
                    columns.put(column_label, fields);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                switch (type) {
                    case "PatientIdentifier":

                        Map<Integer, String> identifiers = getIdentifiers(baseCohort, expression);
                        columns.put(column_label, identifiers);

                        break;
                    case "PersonName":
                        Map<Integer, Object> returnedNames = getPersonNames(baseCohort, expression);
                        columns.put(column_label, returnedNames);
                        break;
                    case "PersonAttribute":
                        if (Objects.equals(expression, "8d871f2a-c2cc-11de-8d13-0010c6dffd0f") || Objects.equals(expression, "dec484be-1c43-416a-9ad0-18bd9ef28929")) {
                            Map<Integer, Object> attributes = getConceptPersonAttributes(baseCohort, expression);
                            columns.put(column_label, attributes);
                        } else {
                            Map<Integer, Object> attributes = getPersonAttributes(baseCohort, expression);
                            columns.put(column_label, attributes);

                        }
                        break;
                    case "Demographics":
                        if (expression.equals("Age")) {
                            Map<Integer, Object> birthdates = getAges(baseCohort);

                            Map<Integer, Object> ages = Helper.calculateAges(birthdates);
                            columns.put(column_label, ages);
                        } else {
                            Map<Integer, Object> attributes = getPatientAttributes(baseCohort, "Person." + expression);
                            columns.put(column_label, attributes);
                        }
                        break;
                    case "Address":
                        Map<Integer, Object> addresses = getPatientAttributes(baseCohort, "PersonAddress." + expression);
                        columns.put(column_label, addresses);
                        break;
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

    private Map<Integer, List<List<Object>>> getLatestObsData(Column column, org.openmrs.cohort.Cohort baseCohort) throws IOException {
        ConceptService conceptService = Context.getConceptService();
        Concept c = conceptService.getConceptByUuid(column.getExpression());

        Map<Integer, List<List<Object>>> patientIdObsMap = getLatestNObs(baseCohort,c,column.getModifier(),column.getExtras());

        return patientIdObsMap;
    }


    public Map<Integer, List<List<Object>>> getLatestObs(org.openmrs.cohort.Cohort baseCohort,Concept c,List<String> attrs){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getObservationsValues(baseCohort, c, attrs, null, true);

    }

    public Map<Integer,Object>  getLatestObs(org.openmrs.cohort.Cohort baseCohort,Concept c){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  getObsValue(rcs.getObservationsValues(baseCohort, c, null, null, true),c);


    }

    public Map<Integer, List<List<Object>>>  getLatestNObs(org.openmrs.cohort.Cohort baseCohort,Concept c,Integer noOfObz,List<String> attrs){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
       return  rcs.getObservationsValues(baseCohort, c, attrs, noOfObz, true);

    }

    public Map<Integer, List<List<Object>>>  getLatestNObs(org.openmrs.cohort.Cohort baseCohort,Concept c,Integer noOfObz){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getObservationsValues(baseCohort, c, null, noOfObz, true);

    }

    public Map<Integer, List<List<Object>>>  getFirstObs(org.openmrs.cohort.Cohort baseCohort,Concept c,List<String> attrs){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getObservationsValues(baseCohort, c, attrs, null, false);

    }

    public Map<Integer, List<List<Object>>>  getFirstObs(org.openmrs.cohort.Cohort baseCohort,Concept c){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getObservationsValues(baseCohort, c, null, null, false);

    }

    public Map<Integer, List<List<Object>>>  getFirstNObs(org.openmrs.cohort.Cohort baseCohort,Concept c,Integer noOfObz,List<String> attrs){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getObservationsValues(baseCohort, c, attrs, noOfObz, false);

    }

    public Map<Integer, List<List<Object>>>  getFirstNObs(org.openmrs.cohort.Cohort baseCohort,Concept c,Integer noOfObz){
        ReportingCompatibilityService rcs = Context.getService(ReportingCompatibilityService .class);
        return  rcs.getObservationsValues(baseCohort, c, null, noOfObz, false);

    }

    public Map<Integer, Object> getObsValue(Map<Integer, List<List<Object>>> patientIdObsMap,Concept concept){
        Map<Integer,Object> patientIdObjectMap = new HashMap<>();
        for (Map.Entry<Integer, List<List<Object>>> entry : patientIdObsMap.entrySet()) {
            Integer key = entry.getKey();
            List<List<Object>> obs = entry.getValue();
            Object obsValue =null;
            if (obs != null&& obs.size() > 0) {
                List<Object>listObs= obs.get(0);
                 obsValue =listObs.get(0);


            }
            if (concept.getDatatype().isCoded()) {
                Concept c = (Concept)obsValue;
                obsValue = c.getName().getName();

            } else if (concept.getDatatype().isDate()) {
                obsValue = String.valueOf(obsValue);
            }
            patientIdObjectMap.put(key,obsValue);
        }
        return patientIdObjectMap;
    }

    public DataSetRow createPatientDataRow(Integer patientId,HashMap<String, Object> columns,List<Column> columnParameters){
        DataSetRow row = new DataSetRow();

        PatientDataHelper pdh = new PatientDataHelper();
        for (Column column : columnParameters) {
            String key = column.getLabel();
            List<String> extras = column.getExtras();
            Object columnValue = "";
            Object object = columns.get(key);
            String finalKey = key;
            Column columnParameter = columnParameters.stream().filter(c -> c.getLabel().equals(finalKey)).findFirst().orElse(null);
            assert columnParameter != null;
            int modifier = columnParameter.getModifier();
            if(Objects.equals(key, "Birthdate") || key.equals("Date of death")){
                object =  convertDatesToStrings(object);
            }
            if (object!=null) {
                if (object instanceof Map) {
                    Map<Integer, Object> map = (Map<Integer, Object>) object;
                    Object patientObject = map.get(patientId);
                    if(patientObject instanceof  List){
                        List<Object> objectList = (List<Object>) patientObject;
                        if(objectList.get(0) instanceof List){

                            for(int x = 0; x < modifier; x++){
                                String modifierKey="";
                                modifierKey =  x!=0 ?  finalKey + "_" + x  : finalKey ;
                                if(objectList.size() > x) {
                                    List<Object> objectList1 = (List<Object>)objectList.get(x);
                                    if(!objectList1.isEmpty()){
                                        columnValue = objectList1.get(0);
                                        attachToDataSetRow(key, columnValue, pdh, row);
                                        if(extras!=null) {
                                            for (int v = 0; v < extras.size(); v++) {
                                                String extraValueColumnName = extras.get(v);
                                                key = modifierKey + "_" + extraValueColumnName;
//                                                skip object at index 0
                                                if(v==0){
                                                    continue;
                                                }
                                                columnValue = objectList1.get(v);
                                                attachToDataSetRow(key, columnValue, pdh, row);

                                            }
                                        }
                                    }

                                }
                            }

                        }
                    }else {
                        columnValue= patientObject;
                        if(modifier >1){

                            for(int x = 0; x < modifier; x++){
                                if(x<1) {
                                    attachToDataSetRow(key, columnValue, pdh, row);
                                }else{
                                    attachToDataSetRow(key + "_" + x, columnValue, pdh, row);
                                }

                            }
                        }else {
                            attachToDataSetRow(key, columnValue, pdh, row);
                        }
                    }

                } else if (object instanceof List) {

                    List<Object[]> objects = (List<Object[]>)object;
                    for (Object[] object1 : objects) {
                        int ptId = (int) object1[0];

                        if (ptId == patientId) {
                            columnValue = object1[1];
                        }
                    }
                    attachToDataSetRow(key, columnValue, pdh, row);
                } else {
                    System.out.println("The variable is neither a Map nor a String.");
                }

            }

        }
        return row;

    }

    private static void attachToDataSetRow(String key, Object obj, PatientDataHelper pdh, DataSetRow row) {
        if(obj instanceof  Concept){
            Concept c = (Concept) obj;
            obj = c.getName().getName();
        }else if (obj instanceof Date)
        {
            obj = String.valueOf(obj);
        }
        pdh.addCol(row, key, obj);
    }
}
