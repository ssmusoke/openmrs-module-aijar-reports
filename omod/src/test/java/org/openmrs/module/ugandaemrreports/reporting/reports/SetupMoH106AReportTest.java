package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.report.ReportData;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.module.reporting.report.renderer.CsvReportRenderer;
import org.openmrs.module.ugandaemrreports.StandaloneContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Verification of 106A report output
 */
public class SetupMoH106AReportTest extends StandaloneContextSensitiveTest {
	
	@Autowired
	SetupMoH106AReport manager;
	
	@Qualifier("reportingReportDefinitionService")
	@Autowired
	protected ReportDefinitionService reportDefinitionService;
	
	@Test
	public void testReport() throws Exception {
		EvaluationContext context = new EvaluationContext();
		context.addParameterValue("startDate", DateUtil.parseDate("2016-10-01", "yyyy-MM-dd"));
		context.addParameterValue("endDate", DateUtil.parseDate("2016-12-31", "yyyy-MM-dd"));
		
		ReportDefinition reportDefinition = manager.constructReportDefinition();
		ReportData data = reportDefinitionService.evaluate(reportDefinition, context);
		new CsvReportRenderer().render(data, null, System.out);
		Assert.assertTrue(true);
	}
	
}
