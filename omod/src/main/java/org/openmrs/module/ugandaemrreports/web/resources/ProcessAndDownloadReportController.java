package org.openmrs.module.ugandaemrreports.web.resources;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.Report;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.ui.framework.page.FileDownload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + ProcessAndDownloadReportController.UGANDAEMRREPORTS + ProcessAndDownloadReportController.SET)
public class ProcessAndDownloadReportController {
    public static final String UGANDAEMRREPORTS = "/ugandaemrreports";
    public static final String SET = "/reportDownload";
    public static final String EXCEL_REPORT_RENDERER_TYPE = "org.openmrs.module.reporting.report.renderer.XlsReportRenderer";

    @Autowired
    public GenericConversionService conversionService;

    @Autowired
    public ReportService reportService;

    @ExceptionHandler(APIAuthenticationException.class)
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Object download(HttpServletRequest request,
                           @RequestParam(required = true, value = "uuid") String reportDefinitionUuid) {
        try {
            if (!validateDateIsValidFormat(request.getParameter("endDate"))) {
                SimpleObject message = new SimpleObject();
                message.put("error", "Given date " + request.getParameter("endDate") + "is not valid");

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

               return downloadExcelReport(rd,parameterValues);
            }else {
                SimpleObject message = new SimpleObject();
                message.put("error", "Report definition" + "not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .contentType(MediaType.APPLICATION_JSON).body(message);
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


    private Object downloadExcelReport(ReportDefinition rd, Map<String, Object> parameterValues) {
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, parameterValues));
        reportRequest.setStatus(ReportRequest.Status.REQUESTED);
        List<ReportDesign> reportDesigns = reportService.getReportDesigns(rd, null, false);

        ReportDesign reportDesign = reportDesigns.stream().filter(p -> "Excel".equals(p.getName())).findAny().orElse(null);
        RenderingMode renderingMode = null;
        if (reportDesign != null) {
            String reportRenderingMode = EXCEL_REPORT_RENDERER_TYPE + "!" + reportDesign.getUuid();
            renderingMode = new RenderingMode(reportRenderingMode);
            if (!renderingMode.getRenderer().canRender(rd)) {
                throw new IllegalArgumentException("Unable to render Report with " + reportRenderingMode);
            }
            reportRequest.setRenderingMode(renderingMode);
        }
        Report report = reportService.runReport(reportRequest);

        //download report;
        String filename = renderingMode.getRenderer().getFilename(report.getRequest()).replace(" ", "_");
        String contentType = renderingMode.getRenderer().getRenderedContentType(report.getRequest());
        byte[] data = report.getRenderedOutput();

        if (data == null) {
            throw new IllegalStateException("Error retrieving the report");
        } else {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename="+filename)
                    .body(data);
        }

    }
}
