package org.openmrs.module.ugandaemrreports.web.resources;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * End point will handle dataSet evaluation passed
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + EncounterTypeConceptsRestController.UGANDAEMRREPORTS + EncounterTypeConceptsRestController.DATA_SET)
public class EncounterTypeConceptsRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_SET = "/concepts/encountertype";

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getConcepts(HttpServletRequest request) {
        try {
            String encounterTypeUuid = request.getParameter("uuid");
            String folderPath = "/path/to/json/files";

            File folder = new File(folderPath);
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".json")) {
                          String path = getFileContainingEncounterUuid(file, encounterTypeUuid);
                          System.out.println(path+" my path");
                          if(path!="" && path!=null){
                            Set<String> conceptsUuids = extractTableColumnsAttributes(path);


                          }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            return new ResponseEntity<String>(ex.getMessage() + Arrays.toString(ex.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




    private static String getFileContainingEncounterUuid(File file, String targetEncounterTypeUuid) {
        ObjectMapper objectMapper = new ObjectMapper();
        String filePath ="";
        try {
            JsonData jsonData = objectMapper.readValue(file, JsonData.class);

            if (jsonData.encounter_type_uuid.equals(targetEncounterTypeUuid)) {
                filePath = file.getPath();
                System.out.println("File of interest: " + file.getName());
            }
        } catch (IOException e) {
            System.err.println("Error reading JSON file: " + file.getName());
            e.printStackTrace();
        }
        return filePath;
    }

    private static Set<String> extractTableColumnsAttributes(String filePath) throws IOException {
        Set<String> attributeValues = new HashSet<>();
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(filePath);

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

    class JsonData {

        String encounter_type_uuid;
        String table_columns;


        public String getEncounter_type_uuid() {
            return encounter_type_uuid;
        }

        public String getTable_columns() {
            return table_columns;
        }
    }

}