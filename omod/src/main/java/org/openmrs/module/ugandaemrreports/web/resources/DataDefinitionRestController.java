package org.openmrs.module.ugandaemrreports.web.resources;

import org.apache.commons.io.FileUtils;
import org.openmrs.Concept;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.ConceptMapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + DataDefinitionRestController.UGANDAEMRREPORTS + DataDefinitionRestController.DATA_DEFINITION)

public class DataDefinitionRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_DEFINITION = "/dataDefinition";

    @RequestMapping(method = RequestMethod.POST,consumes = "application/json")
    @ResponseBody
    public Object evaluate(@RequestBody SimpleObject object, RequestContext context) {
        try {
            String cohort = object.get("cohort");
            String startDate = object.get("startDate");
            String endDate = object.get("endDate");

            return new ResponseEntity<Object>(object, HttpStatus.OK);

        } catch (Exception ex) {
            return new ResponseEntity<String>(ex.getMessage() + Arrays.toString(ex.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
