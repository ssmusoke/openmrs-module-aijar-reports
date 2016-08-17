package org.openmrs.module.aijarreports.page;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.ui.framework.page.PageRequest;
import org.openmrs.ui.framework.page.PageRequestMapper;
import org.springframework.stereotype.Component;

/**
 *
 * Overrides the mapping from the reports app home page from reporting ui to a new one specified in this module
 *
 */
/* @Component - commented out until an issue of beans across modules can be merged into a single list */
public class ReportsAppHomePagePageRequestMapper implements PageRequestMapper {

	protected final Log log = LogFactory.getLog(getClass());

	/**
	 * Implementations should call {@link PageRequest#setProviderNameOverride(String)} and
	 * {@link PageRequest#setPageNameOverride(String)}, and return true if they want to remap a request,
	 * or return false if they didn't remap it.
	 *
	 * @param request may have its providerNameOverride and pageNameOverride set
	 * @return true if this page was mapped (by overriding the provider and/or page), false otherwise
	 */
	public boolean mapRequest(PageRequest request) {
		if (request.getProviderName().equals("reportingui")) {
			if (request.getPageName().equals("reportsapp/home")) {
				// change to the custom login provided by the module
				request.setProviderNameOverride("aijarreports");
				request.setPageNameOverride("reportsHome");
					log.info(request.toString());
				return true;
			}
		}
		return false;
	}
}
