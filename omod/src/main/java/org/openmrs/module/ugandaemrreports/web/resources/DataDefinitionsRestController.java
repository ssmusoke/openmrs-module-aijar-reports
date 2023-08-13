package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + DataEvaluatorRestController.UGANDAEMRREPORTS + DataEvaluatorRestController.DATA_SET)
public class DataDefinitionsRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_SET = "/dataTypes";



}
