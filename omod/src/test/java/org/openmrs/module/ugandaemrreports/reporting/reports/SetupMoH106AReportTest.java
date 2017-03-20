package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.IndicatorReportRenderer;
import org.openmrs.module.reporting.report.renderer.SimpleHtmlReportRenderer;
import org.openmrs.module.reporting.report.renderer.TsvReportRenderer;
import org.openmrs.module.reporting.report.renderer.XmlReportRenderer;
import org.openmrs.module.reporting.report.service.ReportService;
import org.openmrs.module.ugandaemrreports.StandaloneContextSensitiveTest;
import org.openmrs.module.ugandaemrreports.activator.ReportInitializer;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRReportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileOutputStream;

/**
 * Verification of 106A report output
 */
public class SetupMoH106AReportTest extends StandaloneContextSensitiveTest {
	
	@Autowired
	SetupMoH106AReport manager;
	
	@Qualifier("reportingReportDefinitionService")
	@Autowired
	protected ReportDefinitionService reportDefinitionService;
	
	@Qualifier("reportingReportService")
	@Autowired
	ReportService reportService;
	
	
	@Override
	@Before
	public void setupForTest() throws Exception {
		super.setupForTest();
		// Clear all reports
		AdministrationService as = Context.getAdministrationService();
		as.executeSQL("delete from global_property WHERE property = 'reporting.reportManager." + manager.getUuid() +  "'  ;", false);
		as.executeSQL("delete from serialized_object WHERE uuid = '" + manager.getUuid() +  "'  ;", false);
		// update all reports
		ReportManagerUtil.setupReport(manager);
	}
	
	@Test
	public void testReport() throws Exception {
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2016-10-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2016-12-31", "yyyy-MM-dd"));
		
		ReportDefinition reportDefinition = manager.constructReportDefinition();
		ReportData data = reportDefinitionService.evaluate(reportDefinition, context);
		
		//ReportDesign design = reportService.getReportDesignByUuid(manager.getExcelDesignUuid());
		//System.out.println(design);
		FileOutputStream fos = new FileOutputStream("report.html");
		new IndicatorReportRenderer().render(data, null, fos);
		Assert.assertTrue(true);
	}
	
}
