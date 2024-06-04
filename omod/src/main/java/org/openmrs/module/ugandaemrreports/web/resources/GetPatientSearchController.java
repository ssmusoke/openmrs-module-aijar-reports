package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.cohort.CohortDefinitionItemHolder;
import org.openmrs.cohort.impl.PatientSearchCohortDefinitionProvider;
import org.openmrs.module.reportingcompatibility.service.CohortService;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.reporting.PatientSearch;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * End point will handle dataSet evaluation passed
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + GetPatientSearchController.UGANDAEMRREPORTS + GetPatientSearchController.DATA_SET)
public class GetPatientSearchController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_SET = "/patientsearch";

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Object> getAll() {
        CohortService cs = Context.getService(CohortService.class);
        List<CohortDefinitionItemHolder> cohortDefinitionItemHolders =  cs.getAllCohortDefinitions();

        System.out.println("cohort"+cohortDefinitionItemHolders.size());
        return new ResponseEntity<Object>(cohortDefinitionItemHolders, HttpStatus.OK);
    }



}