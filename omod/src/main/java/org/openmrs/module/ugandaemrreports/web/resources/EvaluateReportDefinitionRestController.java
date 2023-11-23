package org.openmrs.module.ugandaemrreports.web.resources;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationUtil;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
import org.openmrs.module.reporting.report.renderer.template.TemplateEngine;
import org.openmrs.module.reporting.report.renderer.template.TemplateEngineManager;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + EvaluateReportDefinitionRestController.UGANDAEMRREPORTS + EvaluateReportDefinitionRestController.SET)
public class EvaluateReportDefinitionRestController {
    public static final String JSON_REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.TextTemplateRenderer";

    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String SET = "/reportingDefinition";


    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getReportData(@RequestParam String startDate, @RequestParam String endDate,
                                @RequestParam(required = true, value = "uuid") String reportDefinitionUuid,
                                @RequestParam(required = false, value = "renderType") String rendertype) {
        try {
            if (!validateDateIsValidFormat(endDate)) {
                SimpleObject message = new SimpleObject();
                message.put("error", "given date " + endDate + "is not valid");
                return new ResponseEntity<SimpleObject>(message, HttpStatus.BAD_REQUEST);

            }
            EvaluationContext context = new EvaluationContext();

            ReportDefinitionService service = Context.getService(ReportDefinitionService.class);
            ReportDefinition rd = service.getDefinitionByUuid(reportDefinitionUuid);
            ReportData reportData = null;
            if (rd != null) {
                Map<String, Object> parameterValues = new HashMap<String, Object>();

                parameterValues.put("endDate", DateUtil.parseYmd(endDate));
                parameterValues.put("startDate", DateUtil.parseYmd(startDate));

                context.setParameterValues(parameterValues);

                reportData = getReportDefinitionService().evaluate(rd, context);

            }

            if (rendertype == null) {
                Map<String, List<SimpleObject>> listMap = new HashMap<>();
                Map<String, DataSet> dataSets = reportData.getDataSets();
                Set<String> keySet = dataSets.keySet();
                for (String key : keySet) {
                    DataSet dataSet = dataSets.get(key);
                    List<SimpleObject> simpleObjectList = convertDataSetToSimpleObject(dataSet);
                    listMap.put(key, simpleObjectList);
                }

                return new ResponseEntity<Map<String, List<SimpleObject>>>(listMap, HttpStatus.OK);
            } else {

                List<ReportDesign> reportDesigns = Context.getService(ReportService.class).getReportDesigns(rd, null, false);

                ReportDesign reportDesign = reportDesigns.stream().filter(p -> "JSON".equals(p.getName())).findAny().orElse(null);
                if (reportDesign != null) {
                    String reportRendergingMode = JSON_REPORT_RENDERER_TYPE + "!" + reportDesign.getUuid();
                    RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
                    if (!renderingMode.getRenderer().canRender(rd)) {
                        throw new IllegalArgumentException("Unable to render Report with " + reportRendergingMode);
                    }

                    String report = createJson(reportData, reportDesign,rendertype);

                    return new ResponseEntity<Object>(report, HttpStatus.OK);
                }else{
                    return new ResponseEntity<String>("{'Error': 'No design to preview report'}", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

        } catch (Exception ex) {
            return new ResponseEntity<String>("{Error: " + ex.getMessage()+"}", HttpStatus.INTERNAL_SERVER_ERROR);
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

    private ReportDefinitionService getReportDefinitionService() {
        return Context.getService(ReportDefinitionService.class);
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
                Object object = r.getColumnValue(key);
                if (object == null) {
                    details.add(key, "");
                } else {
                    try {
                        details.add(key, object.toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            dataList.add(details);

        }
        return dataList;
    }

    private String createJson(ReportData reportData, ReportDesign reportDesign,String renderType) {
        HashMap<String, String> map = new HashMap<>();
        String jsonText="";
        try {

            File file = new File(OpenmrsUtil.getApplicationDataDirectory() + "sendReports");
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            Writer pw = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            TextTemplateRenderer textTemplateRenderer = new TextTemplateRenderer();
            ReportDesignResource reportDesignResource = textTemplateRenderer.getTemplate(reportDesign);
            String templateContents = new String(reportDesignResource.getContents(), StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(templateContents);
            templateContents = jsonNode.get(renderType).asText();

             jsonText = processJsonPayLoadTemplateWithWebView(pw, templateContents, reportData, reportDesign, fileOutputStream);

            pw.close();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return jsonText;
    }


    private String processJsonPayLoadTemplateWithWebView(Writer pw, String templateContents, ReportData reportData, ReportDesign reportDesign, FileOutputStream fileOutputStream) throws IOException, RenderingException {

        try {
            TextTemplateRenderer textTemplateRenderer = new TextTemplateRenderer();
            Map<String, Object> replacements = textTemplateRenderer.getBaseReplacementData(reportData, reportDesign);
            String templateEngineName = reportDesign.getPropertyValue("templateType", (String) null);
            TemplateEngine engine = TemplateEngineManager.getTemplateEngineByName(templateEngineName);
            if (engine != null) {
                Map<String, Object> bindings = new HashMap();
                bindings.put("reportData", reportData);
                bindings.put("reportDesign", reportDesign);
                bindings.put("data", replacements);
                bindings.put("util", new ObjectUtil());
                bindings.put("dateUtil", new DateUtil());
                bindings.put("msg", new MessageUtil());
                templateContents = engine.evaluate(templateContents, bindings);
            }

            String prefix = textTemplateRenderer.getExpressionPrefix(reportDesign);
            String suffix = textTemplateRenderer.getExpressionSuffix(reportDesign);
            templateContents = EvaluationUtil.evaluateExpression(templateContents, replacements, prefix, suffix).toString();
            pw.write(templateContents.toString());
            return templateContents;

        } catch (RenderingException var17) {
            throw var17;
        } catch (Throwable var18) {
            throw new RenderingException("Unable to render results due to: " + var18, var18);
        }
    }

}
