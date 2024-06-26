package org.openmrs.module.ugandaemrreports.web.resources;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.util.Json;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationUtil;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportDesignResource;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingException;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.renderer.TextTemplateRenderer;
import org.openmrs.module.reporting.report.renderer.template.TemplateEngine;
import org.openmrs.module.reporting.report.renderer.template.TemplateEngineManager;
import org.openmrs.module.reporting.report.service.ReportService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + EvaluateReportDefinitionRestController.UGANDAEMRREPORTS + EvaluateReportDefinitionRestController.SET)
public class EvaluateReportDefinitionRestController {
    public static final String JSON_REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.TextTemplateRenderer";
    public static final String EXCEL_REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.XlsReportRenderer";

    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String SET = "/reportingDefinition";

    public String rdUuid;

    @Autowired
    public GenericConversionService conversionService;

    @Autowired
    public ReportService reportService;


    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object getReportData(HttpServletRequest request,
                                @RequestParam(required = true, value = "uuid") String reportDefinitionUuid,
                                @RequestParam(required = false, value = "renderType") String rendertype) {
        try {
            rdUuid = reportDefinitionUuid;
            if (!validateDateIsValidFormat(request.getParameter("endDate"))) {
                SimpleObject message = new SimpleObject();
                message.put("error", "given date " + request.getParameter("endDate") + "is not valid");

                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON).body(message);

            }
            EvaluationContext context = new EvaluationContext();
            ReportDefinitionService service = Context.getService(ReportDefinitionService.class);
            ReportDefinition rd = service.getDefinitionByUuid(reportDefinitionUuid);
            ReportData reportData = null;
            if (rd != null) {
                Collection<Parameter> missingParameters = new ArrayList<Parameter>();
                Map<String, Object> parameterValues = new HashMap<String, Object>();

                for (Parameter parameter : rd.getParameters()) {
                    String submitted = request.getParameter(parameter.getName());
                    if (parameter.getCollectionType() != null) {
                        throw new IllegalStateException("Collection parameters not yet implemented");
                    }
                    Object converted;
                    if (StringUtils.isEmpty(submitted)) {
                        converted = parameter.getDefaultValue();
                    } else {
                        converted = conversionService.convert(submitted, parameter.getType());
                    }
                    if (converted == null) {
                        missingParameters.add(parameter);
                    }
                    parameterValues.put(parameter.getName(), converted);
                }

                context.setParameterValues(parameterValues);

//                makeExcelReportRequest(rd,parameterValues);
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


                return ResponseEntity.status(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON).body(listMap);
            } else {

                List<ReportDesign> reportDesigns = Context.getService(ReportService.class).getReportDesigns(rd, null, false);

                ReportDesign reportDesign = reportDesigns.stream().filter(p -> "JSON".equals(p.getName())).findAny().orElse(null);

                if (reportDesign != null) {
                    String reportRendergingMode = JSON_REPORT_RENDERER_TYPE + "!" + reportDesign.getUuid();
                    RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
                    if (!renderingMode.getRenderer().canRender(rd)) {
                        throw new IllegalArgumentException("Unable to render Report with " + reportRendergingMode);
                    }

                    JsonNode report = createPayload(reportData, reportDesign, rendertype);

                        return ResponseEntity.status(HttpStatus.OK)
                                .contentType(MediaType.APPLICATION_JSON).body(report.toString());

                } else {
                    return new ResponseEntity<String>("{'Error': 'No design to preview report'}", HttpStatus.INTERNAL_SERVER_ERROR);
                }
            }

        } catch (Exception ex) {
            return new ResponseEntity<String>("{Error: " + ex.getMessage() + "}", HttpStatus.INTERNAL_SERVER_ERROR);
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

    private JsonNode createPayload(ReportData reportData, ReportDesign reportDesign, String renderType) {
        JsonNode payLoad = null;
        try {

            File file = new File(OpenmrsUtil.getApplicationDataDirectory() + "/sendReports");
            FileOutputStream fileOutputStream = new FileOutputStream(file);

            Writer pw = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
            TextTemplateRenderer textTemplateRenderer = new TextTemplateRenderer();
            ReportDesignResource reportDesignResource = textTemplateRenderer.getTemplate(reportDesign);
            String templateContents = new String(reportDesignResource.getContents(), StandardCharsets.UTF_8);

            templateContents = fillTemplateWithReportData(pw, templateContents, reportData, reportDesign, fileOutputStream);
            String wholePayLoad = fillTemplateWithReportData(pw, templateContents, reportData, reportDesign, fileOutputStream);

            ObjectMapper objectMapper = new ObjectMapper();
            payLoad = objectMapper.readTree(wholePayLoad);



            pw.close();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return payLoad;
    }


    private String fillTemplateWithReportData(Writer pw, String templateContents, ReportData reportData, ReportDesign reportDesign, FileOutputStream fileOutputStream) throws IOException, RenderingException {

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

    private String fillJsonReportDesign(Writer pw, String templateContents, ReportData reportData, ReportDesign reportDesign, FileOutputStream fileOutputStream) throws IOException, RenderingException {

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

            templateContents = EvaluationUtil.evaluateExpression(templateContents, replacements, "\"#", "#\"").toString();
            pw.write(templateContents.toString());
            return templateContents;

        } catch (RenderingException var17) {
            throw var17;
        } catch (Throwable var18) {
            throw new RenderingException("Unable to render results due to: " + var18, var18);
        }
    }


    public static String replaceQuotesInValuePlaceHolders(String input,String reportAlias) {
        int index = input.indexOf("\"value_place_holder\": \"");
        while (index != -1) {
            int startIndex = index + "\"value_place_holder\": \"".length();
            int endIndex = input.indexOf("\"", startIndex);
            if (endIndex != -1) {
                String valuePlaceHolder = input.substring(startIndex, endIndex);
                String replacedValue = "#"+reportAlias+"." + valuePlaceHolder + "#";
                input = input.substring(0, startIndex) + replacedValue + input.substring(endIndex);
            }
            index = input.indexOf("\"value_place_holder\": \"", endIndex);
        }
        return input;
    }



}
