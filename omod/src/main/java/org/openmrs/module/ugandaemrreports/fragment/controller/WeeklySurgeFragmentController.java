package org.openmrs.module.ugandaemrreports.fragment.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportRequest;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.RenderingMode;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsUtil;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class WeeklySurgeFragmentController {

    protected final Log log = LogFactory.getLog(getClass());
    private final String outPutPath =OpenmrsUtil.getApplicationDataDirectory() +"surge_report_dashboard";

    public void controller(FragmentModel model) {

        try {
            ReportDefinitionService reportDefinitionService= Context.getService(ReportDefinitionService.class);
            ReportDefinition rd = reportDefinitionService.getDefinitionByUuid("e7102e5c-b90d-4a4a-b763-20518eadbae5");

            if (rd == null) {
                throw new IllegalArgumentException("unable to find Analytics Data Export report with uuid "
                        + "ANALYTICS_DATA_EXPORT_REPORT_DEFINITION_UUID");
            }

            String reportRendergingMode = "org.openmrs.module.reporting.report.renderer.TextTemplateRenderer"
                    + "!" + "98b4d8d6-17da-45f2-a825-87a8f6522e13";

            RenderingMode renderingMode = new RenderingMode(reportRendergingMode);

            if (!renderingMode.getRenderer().canRender(rd)) {
                throw new IllegalArgumentException("Unable to render Analytics Data Export with " + reportRendergingMode);
            }

            Map<String, Object> parameterValues = new HashMap<String, Object>();

            LocalDateTime eDate = getEndDate();
            LocalDateTime sDate = getStartDate(eDate);

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

            Date startDate = dateFormat.parse(String.format("%s-%s-%s", sDate.getYear(), sDate.getMonthValue(),
                    sDate.getDayOfMonth()));

            Date endDate = dateFormat.parse(String.format("%s-%s-%s", eDate.getYear(), eDate.getMonthValue(),
                    eDate.getDayOfMonth()));

            parameterValues.put("startDate",startDate);
            parameterValues.put("endDate",endDate);

            EvaluationContext context = new EvaluationContext();
            context.setParameterValues(parameterValues);
            ReportData reportData = reportDefinitionService.evaluate(rd, context);

            ReportRequest reportRequest = new ReportRequest();
            reportRequest.setReportDefinition(new Mapped<ReportDefinition>(rd, context.getParameterValues()));
            reportRequest.setRenderingMode(renderingMode);

            File file = new File(outPutPath);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            renderingMode.getRenderer().render(reportData, renderingMode.getArgument(), fileOutputStream);

            String strOutput ="";
            strOutput= this.readOutputFile(strOutput);

            log.info("From WeeklySurgeFragment::" + strOutput.length());
            model.addAttribute("weeklysurgedata", strOutput);
            model.addAttribute("startdate", startDate);
            model.addAttribute("enddate", endDate);

        } catch (Exception e) {
            log.info("Error rendering the contents of the Analytics data export report to"
                    + OpenmrsUtil.getApplicationDataDirectory() + "surge_report" + e.toString());
            e.printStackTrace();
            model.addAttribute("weeklysurgedata", "strOutput");
            model.addAttribute("nodata", "No data");
            model.addAttribute("startdate", "startDate");
            model.addAttribute("enddate", "endDate");
        }

    }

    private String readOutputFile(String strOutput) throws Exception {
        FileInputStream fstreamItem = new FileInputStream(outPutPath);
        DataInputStream inItem = new DataInputStream(fstreamItem);
        BufferedReader brItem = new BufferedReader(new InputStreamReader(inItem));
        String phraseItem;

        if (!(phraseItem = brItem.readLine()).isEmpty()) {
            strOutput = strOutput + phraseItem + System.lineSeparator();
            while ((phraseItem = brItem.readLine()) != null) {
                strOutput = strOutput + phraseItem + System.lineSeparator();
            }
        }

        fstreamItem.close();

        return strOutput;
    }
    private  LocalDateTime getEndDate(){

        LocalDateTime dt=LocalDateTime.now();

        if(dt.getDayOfWeek()== DayOfWeek.MONDAY)
            return dt.minusDays(1);
        else if(dt.getDayOfWeek()== DayOfWeek.TUESDAY)
            return dt.minusDays(2);
        else if(dt.getDayOfWeek()== DayOfWeek.WEDNESDAY)
            return dt.minusDays(3);
        else if(dt.getDayOfWeek()== DayOfWeek.THURSDAY)
            return dt.minusDays(4);
        else if(dt.getDayOfWeek()== DayOfWeek.FRIDAY)
            return dt.minusDays(5);
        else if(dt.getDayOfWeek()== DayOfWeek.SATURDAY)
            return dt.minusDays(6);
        else
            return dt; //for Sunday no need to adjust the date

    }
    private  LocalDateTime getStartDate(LocalDateTime endDate){
            return endDate.minusDays(6);
    }

}
