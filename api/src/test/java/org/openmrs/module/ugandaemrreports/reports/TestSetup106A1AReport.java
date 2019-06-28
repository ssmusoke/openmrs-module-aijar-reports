package org.openmrs.module.ugandaemrreports.reports;

import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.ugandaemrreports.StandaloneContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

@SkipBaseSetup
public class TestSetup106A1AReport extends StandaloneContextSensitiveTest {

    @Autowired
    protected ReportDefinitionService reportingReportDefinitionService;

    @Autowired
    private Setup106A1AReport reportManager;

    @Test
    public void test106AExport() throws Exception {

        EvaluationContext context = new EvaluationContext();
        System.out.println("Setting Parameters");
        context.addParameterValue("startDate", DateUtil.parseDate("2018-01-01", "yyyy-MM-dd"));
        context.addParameterValue("endDate", DateUtil.parseDate("2018-03-31", "yyyy-MM-dd"));

        ReportDefinition reportDefinition = reportManager.constructReportDefinition();
        ReportData reportData = reportingReportDefinitionService.evaluate(reportDefinition, context);
        System.out.println("Running the report");

        System.out.println(reportData.toString());

        assertTrue(true);
    }
}
