package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.HashMap;
import java.util.Map;

public class WeeklySurgeFragmentController {

    protected final Log log = LogFactory.getLog(getClass());

    public void controller(FragmentModel model) {


        try {
            ReportDefinitionService reportDefinitionService= Context.getService(ReportDefinitionService.class);
            ReportDefinition rd = reportDefinitionService.getDefinitionByUuid("e7102e5c-b90d-4a4a-b763-20518eadbae5");
            if (rd == null) {
                throw new IllegalArgumentException("unable to find Analytics Data Export report with uuid "
                        + "ANALYTICS_DATA_EXPORT_REPORT_DEFINITION_UUID");
            }
            String reportRendergingMode = "org.openmrs.module.reporting.report.renderer.TextTemplateRenderer" + "!" + "98b4d8d6-17da-45f2-a825-87a8f6522e13";
            RenderingMode renderingMode = new RenderingMode(reportRendergingMode);
            if (!renderingMode.getRenderer().canRender(rd)) {
                throw new IllegalArgumentException("Unable to render Analytics Data Export with " + reportRendergingMode);
            }
            Map<String, Object> parameterValues = new HashMap<String, Object>();


            LocalDateTime now = LocalDateTime.now();
            LocalDateTime endd = LocalDateTime.now().minusDays(7);

            parameterValues.put("startDate", String.format("%s-%s-%s", now.getYear(), now.getMonthValue(), now.getDayOfMonth()));
            parameterValues.put("endDate", String.format("%s-%s-%s", endd.getYear(), endd.getMonthValue(), endd.getDayOfMonth()));

            EvaluationContext context = new EvaluationContext();

            context.setParameterValues(parameterValues);
            ReportData reportData = reportDefinitionService.evaluate(rd, context);
            ReportRequest reportRequest = new ReportRequest();
            reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
            reportRequest.setRenderingMode(renderingMode);

            File file = new File(OpenmrsUtil.getApplicationDataDirectory() +"surge_report");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);

           String strOutput ="";// this.readOutputFile(strOutput);

            log.error(strOutput);
            model.addAttribute("weeklysurgedata", strOutput);


        } catch (Exception e) {
            log.info("Error rendering the contents of the Analytics data export report to"
                    + OpenmrsUtil.getApplicationDataDirectory() + "surge_report" + e.toString());
            e.printStackTrace();
        }

    }



}
