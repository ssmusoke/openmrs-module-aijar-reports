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

            List<ConceptMapper> conceptMapperList  =  Helper.getConceptByEncounterTypeUuid(encounterTypeUuid);

            return new ResponseEntity<Object>(conceptMapperList, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<String>(ex.getMessage() + Arrays.toString(ex.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


}