package org.openmrs.module.ugandaemrreports.web.resources;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.Program;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortSearchHistory;
import org.openmrs.cohort.impl.PatientSearchCohortDefinitionProvider;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.ugandaemrreports.common.PatientDataHelper;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.Cohort;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.ConceptMapper;
import org.openmrs.reporting.PatientFilter;
import org.openmrs.reporting.PatientSearch;
import org.openmrs.util.ReportingcompatibilityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Helper {

    public static List<ConceptMapper> getConceptByEncounterTypeUuid(String encounterTypeUuid) throws IOException {
        Set<String> conceptsUuids = new HashSet<>();
        List<ConceptMapper> conceptMapperList = new ArrayList<>();
        File[] files = getMambaConfigFiles();
        File myFile = getFileContainingEncounterUuid(files, encounterTypeUuid);
        if (myFile != null) {
            conceptsUuids = extractTableColumnsAttributes(myFile);
        }

        if (!conceptsUuids.isEmpty()) {
            List<Concept> conceptList = getConcepts(conceptsUuids);
            conceptMapperList = convertConcepts(conceptList, encounterTypeUuid);
        }

        return conceptMapperList;
    }

    public static File getFileContainingEncounterUuid(File[] files, String targetEncounterTypeUuid) {
        File myFile = null;
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".json")) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    try {
                        JsonNode fileObject = objectMapper.readTree(file);
                        JsonNode encounterNode = fileObject.path("encounter_type_uuid");
                        if (encounterNode.asText().equals(targetEncounterTypeUuid)) {
                            myFile = file;
                        }
                    } catch (IOException e) {
                        System.err.println("Error reading JSON file: " + file.getName());
                        e.printStackTrace();
                    }
                }
            }
        }
        return myFile;
    }

    private static File getFileContainingTableName(File file, String tableName) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File file1 = null;
        try {
            JsonNode fileObject = objectMapper.readTree(file);
            JsonNode tableNode = fileObject.path("flat_table_name");
            if (tableNode.asText().equals(tableName)) {
                file1 = file;
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + file.getName());
            e.printStackTrace();
        }
        return file1;
    }

    private static Set<String> extractTableColumnsAttributes(File file) throws IOException {
        Set<String> attributeValues = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();

        JsonNode rootNode = objectMapper.readTree(file);
        JsonNode tableColumnsNode = rootNode.path("table_columns");

        Iterator<String> fieldNames = tableColumnsNode.fieldNames();
        while (fieldNames.hasNext()) {
            String attributeName = fieldNames.next();
            String attributeValue = tableColumnsNode.get(attributeName).asText();
            attributeValues.add(attributeValue);
        }

        return attributeValues;
    }

    private static List<Concept> getConcepts(Set<String> conceptUuids) {
        List<Concept> concepts = new ArrayList<>();
        for (String uuid : conceptUuids) {
            Concept concept = Context.getConceptService().getConceptByUuid(uuid);
            concepts.add(concept);
        }
        return concepts;
    }

    private static List<ConceptMapper> convertConcepts(List<Concept> conceptList, String encounterTypeUuid) {
        List<ConceptMapper> conceptMappers = new ArrayList<>();
        for (Concept c : conceptList) {
            ConceptMapper conceptMapper = new ConceptMapper();
            conceptMapper.setConceptId(c.getConceptId().toString());
            conceptMapper.setConceptName(c.getName().getName());
            conceptMapper.setUuid(c.getUuid());
            conceptMapper.setType(encounterTypeUuid);
            conceptMappers.add(conceptMapper);
        }
        return conceptMappers;
    }

    public static String getColumnNameInTable(String conceptUuid, File file) throws IOException {
        String columnName = null;
        if (file != null) {
            columnName = getColumnNameFromFileByConceptUuid(file, conceptUuid);

        }
        return columnName;
    }

    static File[] getMambaConfigFiles() {
        File folder = FileUtils.toFile(Helper.class.getClassLoader().getResource("_etl/config"));
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            return files;
        } else {
            return null;
        }
    }

    private static String getColumnNameFromFileByConceptUuid(File file, String conceptUuid) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        String columnName = null;
        JsonNode rootNode = objectMapper.readTree(file);
        JsonNode tableColumnsNode = rootNode.path("table_columns");

        Iterator<String> fieldNames = tableColumnsNode.fieldNames();
        while (fieldNames.hasNext()) {
            String attributeName = fieldNames.next();
            String attributeValue = tableColumnsNode.get(attributeName).asText();
            if (attributeValue.equals(conceptUuid)) {
                columnName = attributeName;
            }
        }

        return columnName;
    }

    public static String getMambaTableFromFile(File file) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(file);
        JsonNode tableNameNode = rootNode.path("flat_table_name");
        return tableNameNode.asText();
    }

    public static org.openmrs.cohort.Cohort getCohortMembers(Cohort cohort) throws EvaluationException {
        org.openmrs.cohort.Cohort baseCohort = new org.openmrs.cohort.Cohort();
        String type = cohort.getType();
        String cohortUuid = cohort.getUuid();
        if (cohortUuid != null && type != null) {
            switch (type) {
                case "Report Definition":
                    EvaluationContext context = new EvaluationContext();
                    ReportDefinitionService service = Context.getService(ReportDefinitionService.class);
                    ReportDefinition rd = service.getDefinitionByUuid(cohortUuid);
                    if (rd != null) {
                        Mapped<? extends CohortDefinition> cd = rd.getBaseCohortDefinition();

                        if (cd != null) {
                            org.openmrs.Cohort baseCohort1;
                            List<Map<String, Object>> parameters = cohort.getParameters();

                            Map<String, Object> cohortParameters = getParameters(parameters);
                            ReflectionUtil.setPropertyValue(cd, "startDate", cohortParameters.get("startDate"));
                            ReflectionUtil.setPropertyValue(cd, "endDate", cohortParameters.get("endDate"));

                            context.setParameterValues(cohortParameters);

                            baseCohort1 = Context.getService(CohortDefinitionService.class).evaluate(cd, context);
                            baseCohort = new org. openmrs. cohort. Cohort(baseCohort1.getCommaSeparatedPatientIds());
                        }
                    }
                    break;
                case "Patient Search":
                    PatientSearch patientSearch = Context.getService(UgandaEMRReportsService.class).getPatientSearchByUuid(cohortUuid);
                    PatientSearchCohortDefinitionProvider provider = new PatientSearchCohortDefinitionProvider();
                     baseCohort = provider.evaluate(patientSearch,null);
                    break;
                case "Program":
                    org.openmrs.Cohort baseCohort1 = Context.getService(UgandaEMRReportsService.class).getPatientCurrentlyInProgram(cohortUuid);
                    baseCohort = new org. openmrs. cohort. Cohort(baseCohort1.getCommaSeparatedPatientIds());
                    break;
                case "Cohort":
                    CohortService cohortService1 = Context.getCohortService();
                     baseCohort1 = cohortService1.getCohortByUuid(cohortUuid);
                    baseCohort = new org. openmrs. cohort. Cohort(baseCohort1.getCommaSeparatedPatientIds());
                    break;
            }

        }
        return baseCohort;
    }
    private static Map<String, Object> getParameters(List<Map<String, Object>> list) {

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
}
