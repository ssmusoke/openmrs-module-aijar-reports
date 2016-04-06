package org.openmrs.module.aijarreports.activator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.aijarreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.library.EIDCohortDefinitionLibrary;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.definition.library.DefinitionLibrary;
import org.openmrs.module.reporting.definition.library.LibraryDefinitionSummary;
import org.openmrs.module.reporting.report.util.ReportUtil;

/**
 * Initializes library definitions and serializes them to the database so that they are available via the reportingui
 */
public class DefinitionLibraryInitializer implements Initializer {

	protected static final Log log = LogFactory.getLog(ReportInitializer.class);
	/**
	 * Run during the activator started method
	 */
	@Override
	public void started() {
		// remove old definitions which are to be recreated
		removeOldDefinitions();

		// add new defintions
		CohortDefinitionService cohortDefinitionService = Context.getService(CohortDefinitionService.class);


		// Load cohort definitions
		ARTClinicCohortDefinitionLibrary artlib = Context.getRegisteredComponents(ARTClinicCohortDefinitionLibrary.class)
				.get(0);
		EIDCohortDefinitionLibrary eidlib = Context.getRegisteredComponents(EIDCohortDefinitionLibrary.class).get(0);
		List<DefinitionLibrary> libs = new ArrayList<DefinitionLibrary>();
		libs.add(artlib);
		libs.add(eidlib);

		for(DefinitionLibrary<CohortDefinition> aLib : libs) {

			// now save the cohorts
			for (LibraryDefinitionSummary cohortDefinitionSummary : aLib.getDefinitionSummaries()) {
				System.out.println("Persisting Cohort definition key " + cohortDefinitionSummary.getKey() + " name "
						+ cohortDefinitionSummary.getName() + " description " + cohortDefinitionSummary
						.getDescription());
				// save the definition
				cohortDefinitionService.saveDefinition(aLib.getDefinition(cohortDefinitionSummary.getKey()));
			}
		}


		// Patient data definitions


		/*List<LibraryDefinitionSummary> patientDataDefintionSummaries = allDefinitionLibraries.getDefinitionSummaries
		(PatientDataDefinition.class);
		// check how many have been loaded
		if (patientDataDefintionSummaries == null) {
			System.out.println("No patient definition summaries loaded");
		}  else {
			System.out.print("There are " + patientDataDefintionSummaries.size() + " patient data definitions ");
			// now save the cohorts
			for (LibraryDefinitionSummary patientDataDefinition : patientDataDefintionSummaries) {
				System.out.println("Persisting Cohort definition " + patientDataDefinition.toString());
				Context.getService(PatientDataService.class).saveDefinition(allDefinitionLibraries.getDefinition
				(PatientDataDefinition.class, patientDataDefinition.getKey()));
			}
		}*/
	}

	private void removeOldDefinitions() {
		String gpVal = Context.getAdministrationService().getGlobalProperty("aijar.oldDefintionsRemoved");
		if (ObjectUtil.isNull(gpVal)) {
			AdministrationService as = Context.getAdministrationService();
			log.warn("Removing all definitions");
			as.executeSQL("delete from serialized_object WHERE serialized_data LIKE '%aijar.%';", false);
			ReportUtil.updateGlobalProperty("aijar.oldDefintionsRemoved", "true");
		}
	}
	
	/**
	 * Run during the activator stopped method
	 */
	@Override
	public void stopped() {

	}
}
