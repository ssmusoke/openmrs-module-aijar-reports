package org.openmrs.module.ugandaemrreports.web.resources;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ReflectionUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * End point will handle dataSet evaluation passed
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + EvaluateDataSetRestController.UGANDAEMRREPORTS + EvaluateDataSetRestController.DATA_SET)
public class EvaluateDataSetRestController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String DATA_SET = "/dataSet";

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getDataSetData(@RequestParam String startDate, @RequestParam String endDate,
                                 @RequestParam(required = false, value = "type") Class<? extends DataSetDefinition> type) {
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

            DataSet dataSet = service.evaluate(dataSetDefinition, context);

            List<SimpleObject> traceReportData = new ArrayList<SimpleObject>();
            traceReportData.addAll(convertDataSetToSimpleObject(dataSet));
            if(traceReportData!=null){
                return new ResponseEntity<List<SimpleObject>>(traceReportData, HttpStatus.OK);
            }else{
                return new ResponseEntity<String>("{no data }", HttpStatus.OK);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return new ResponseEntity<String>(Arrays.toString(ex.getStackTrace()), HttpStatus.INTERNAL_SERVER_ERROR);
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

    public List<SimpleObject> convertDataSetToSimpleObject(DataSet d) {
        Iterator iterator = d.iterator();

        List<SimpleObject> dataList = new ArrayList<SimpleObject>();
        while (iterator.hasNext()) {
            DataSetRow r = (DataSetRow) iterator.next();
            Map<String, Object> columns = r.getColumnValuesByKey();
            Set<String> keys = columns.keySet();
            SimpleObject details = new SimpleObject();

            for (String key : keys) {
                details.add(key, r.getColumnValue(key));
            }
            dataList.add(details);

        }
        return dataList;
    }

}