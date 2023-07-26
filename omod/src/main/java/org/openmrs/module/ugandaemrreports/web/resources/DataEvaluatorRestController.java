package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * End point will handle dataSet evaluation passed
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + DataEvaluatorRestController.UGANDAEMRREPORTS + DataEvaluatorRestController.DATA_SET)
public class DataEvaluatorRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_SET = "/dataSet";

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getDataSetData(@RequestParam String startDate, @RequestParam String endDate,
                                 @RequestParam(required=false, value="type") Class<? extends DataSetDefinition> type) {
        try {
            if (!validateDateIsValidFormat(endDate)) {
                SimpleObject message = new SimpleObject();
                message.put("error", "given date " + endDate + "is not valid");
                return new ResponseEntity<SimpleObject>(message, HttpStatus.BAD_REQUEST);

            }
            EvaluationContext context = new EvaluationContext();

            DataSetDefinitionService service = Context.getService(DataSetDefinitionService.class);
            DataSetDefinition dataSetDefinition = type.newInstance();

            context.addParameterValue("endDate", DateUtil.parseYmd(endDate));
            context.addParameterValue("startDate", DateUtil.parseYmd(startDate));
            ReflectionUtil.setPropertyValue(dataSetDefinition, "startDate", DateUtil.parseYmd(startDate));
            ReflectionUtil.setPropertyValue(dataSetDefinition, "endDate", DateUtil.parseYmd(endDate));

            DataSet dataSet = service.evaluate(dataSetDefinition,context);

            List<SimpleObject> traceReportData = new ArrayList<SimpleObject>();
            traceReportData.addAll(getTraceReportData(dataSet));

            return new ResponseEntity<List<SimpleObject>>(traceReportData, HttpStatus.OK);
        } catch (Exception ex) {
            return new ResponseEntity<String>(ex.getMessage() + Arrays.toString(ex.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Boolean validateDateIsValidFormat(String date) {
        try {
            DateUtil.parseYmd(date);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public List<SimpleObject> getTraceReportData(DataSet d) {
        Iterator iterator = d.iterator();
        List<SimpleObject> dataList = new ArrayList<SimpleObject>();
        while (iterator.hasNext()) {
            DataSetRow r = (DataSetRow) iterator.next();
            SimpleObject details = new SimpleObject();
            details.add("identifier", r.getColumnValue("identifier"));
            details.add("age", r.getColumnValue("age"));
            details.add("gender", r.getColumnValue("gender"));
            dataList.add(details);
        }
        return dataList;
    }

}