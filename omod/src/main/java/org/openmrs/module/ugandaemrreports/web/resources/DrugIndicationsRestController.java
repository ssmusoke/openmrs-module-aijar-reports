package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.Concept;
import org.openmrs.OrderType;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrreports.api.UgandaEMRReportsService;
import org.openmrs.module.ugandaemrreports.web.resources.mapper.ConceptMapper;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.report.ReportConstants;
import org.openmrs.reporting.ReportObjectWrapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.openmrs.module.ugandaemrreports.web.resources.Helper.convertConcepts;


/**
 * End point will handle dataSet evaluation passed
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + DrugIndicationsRestController.UGANDAEMRREPORTS + DrugIndicationsRestController.DATA_SET)
public class DrugIndicationsRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_SET = "/order/indications";

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object get(HttpServletRequest request,
                      @RequestParam(required = true, value = "uuid") String uuid, RequestContext context) {
        OrderType ot = Context.getOrderService().getOrderTypeByUuid(uuid);
        List<Object> drugOrderNonCodedReasons = Context.getService(UgandaEMRReportsService.class).getNonCodedOrderReasons(ot);
        List<Concept> drugOrderCodedReasons = Context.getService(UgandaEMRReportsService.class).getCodedOrderReasons(ot);

        return new ResponseEntity<>(drugOrderNonCodedReasons, HttpStatus.OK);
    }


}