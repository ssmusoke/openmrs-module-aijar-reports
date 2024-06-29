package org.openmrs.module.ugandaemrreports.web.resources;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.reports.AggregateReportDataExportManager;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.ConceptMapper;

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

    public static File getReportDesignFile(String report_uuid) {

        File folder = FileUtils.toFile(AggregateReportDataExportManager.class.getClassLoader().getResource("report_designs"));
        if (folder.isDirectory()) {


            File[] files = folder.listFiles();
            File myFile = null;
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".json")) {
                        ObjectMapper objectMapper = new ObjectMapper();
                        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                        try {
                            JsonNode fileObject = objectMapper.readTree(file);
                            JsonNode encounterNode = fileObject.path("report_uuid");
                            if (encounterNode.asText().equals(report_uuid)) {
                                myFile = file;
                                break;
                            }
                        } catch (IOException e) {
                            System.err.println("Error reading JSON file: " + file.getName());
                            e.printStackTrace();
                        }
                    }
                }
            }

            return myFile;
        } else {
            return null;
        }
    }
}
