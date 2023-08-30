package org.openmrs.module.ugandaemrreports.web.resources;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.openmrs.Concept;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.ConceptMapper;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
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

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object get(HttpServletRequest request, RequestContext context) {
        try {
            String encounterTypeUuid = request.getParameter("uuid");
            Set<String> conceptsUuids = new HashSet<>();
            List<ConceptMapper> conceptMapperList =new ArrayList<>();
            File folder = FileUtils.toFile(getClass().getClassLoader().getResource("_etl/config"));
            if (folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isFile() && file.getName().endsWith(".json")) {
                            File myFile = getFileContainingEncounterUuid(file, encounterTypeUuid);
                            if (myFile != null) {
                                conceptsUuids = extractTableColumnsAttributes(myFile);

                            }
                        }
                    }
                }
            }
            if (!conceptsUuids.isEmpty()) {
                List<Concept> conceptList = getConcepts(conceptsUuids);
                 conceptMapperList= convertConcepts(conceptList,encounterTypeUuid);
            }
            return new ResponseEntity<Object>(conceptMapperList, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<String>(ex.getMessage() + Arrays.toString(ex.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private static File getFileContainingEncounterUuid(File file, String targetEncounterTypeUuid) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File file1 = null;
        try {
            JsonNode fileObject = objectMapper.readTree(file);
            JsonNode encounterNode = fileObject.path("encounter_type_uuid");
            if (encounterNode.asText().equals(targetEncounterTypeUuid)) {
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

    private List<Concept> getConcepts(Set<String> conceptUuids) {
        List<Concept> concepts = new ArrayList<>();
        for (String uuid : conceptUuids) {
            Concept concept =Context.getConceptService().getConceptByUuid(uuid);
            concepts.add(concept);
        }
        return concepts;
    }

    private List<ConceptMapper> convertConcepts(List<Concept> conceptList,String encounterTypeUuid){
        List<ConceptMapper> conceptMappers = new ArrayList<>();
        for (Concept c:conceptList  ) {
            ConceptMapper conceptMapper = new ConceptMapper();
            conceptMapper.setConceptId(c.getConceptId().toString());
            conceptMapper.setConceptName(c.getName().getName());
            conceptMapper.setUuid(c.getUuid());
            conceptMapper.setType(encounterTypeUuid);
            conceptMappers.add(conceptMapper);
        }
        return conceptMappers;
    }


}