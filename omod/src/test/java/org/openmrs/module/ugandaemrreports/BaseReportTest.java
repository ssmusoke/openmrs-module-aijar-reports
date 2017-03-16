package org.openmrs.module.ugandaemrreports;

import org.junit.Before;
import org.openmrs.module.reporting.report.definition.service.ReportDefinitionService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.openmrs.test.SkipBaseSetup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Sets up basic Mirebalais metadata (instead of the standardTestDataset.xml from openmrs-core)
 */
@SkipBaseSetup // because of TRUNK-4051, this annotation will not be picked up, and you need to declare this on your concrete subclass
public abstract class BaseReportTest extends BaseModuleContextSensitiveTest {
	
	@Qualifier("reportingReportDefinitionService")
	@Autowired
	protected ReportDefinitionService reportDefinitionService;
	
	@Before
	public void setup() throws Exception {
		authenticate();
	}
	
}