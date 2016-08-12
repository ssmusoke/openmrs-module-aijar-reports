package org.openmrs.module.aijarreports.activator;

import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.service.AppFrameworkService;

/**
 * Module configuration initializer
 */
public class AppConfigInitializer implements Initializer {
	
	@Override
	public void started() {
		AppFrameworkService appFrameworkService = Context.getService(AppFrameworkService.class);
		// disable the default reportingui app
		appFrameworkService.disableApp("reportingui.reports");
		
	}
	
	@Override
	public void stopped() {
		
	}
}
