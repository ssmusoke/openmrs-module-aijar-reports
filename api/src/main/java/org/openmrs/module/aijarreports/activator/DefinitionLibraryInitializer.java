package org.openmrs.module.aijarreports.activator;

import java.util.List;

import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.definition.library.DefinitionLibrary;
import org.openmrs.module.reporting.definition.library.LibraryDefinitionSummary;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initializes library definitions and serializes them to the database so that they are available via the reportingui
 */
public class DefinitionLibraryInitializer implements Initializer {

	@Autowired
	AllDefinitionLibraries allDefinitionLibraries;
	/**
	 * Run during the activator started method
	 */
	@Override
	public void started() {
		List<LibraryDefinitionSummary> cohortDefintionSummaries = allDefinitionLibraries.getDefinitionSummaries(CohortDefinition.class);
		// check how many have been loaded
		if (cohortDefintionSummaries == null) {
			System.out.println("No cohort definition summaries loaded");
		}  else {
			System.out.print("There are " + cohortDefintionSummaries.size() + " Cohort definitions ");
			// now save the cohorts
			for (LibraryDefinitionSummary cohortDefinition : cohortDefintionSummaries) {
				System.out.println("Persisting Cohort definition " + cohortDefinition.toString());
				Context.getService(CohortDefinitionService.class).saveDefinition(allDefinitionLibraries
						.cohortDefinition(cohortDefinition.getKey(), cohortDefinition.getParameters()));
			}
		}

		List<LibraryDefinitionSummary> patientDataDefintionSummaries = allDefinitionLibraries.getDefinitionSummaries(PatientDataDefinition.class);
		// check how many have been loaded
		if (patientDataDefintionSummaries == null) {
			System.out.println("No patient definition summaries loaded");
		}  else {
			System.out.print("There are " + patientDataDefintionSummaries.size() + " patient data definitions ");
			// now save the cohorts
			for (LibraryDefinitionSummary patientDataDefinition : patientDataDefintionSummaries) {
				System.out.println("Persisting Cohort definition " + patientDataDefinition.toString());
				Context.getService(PatientDataService.class).saveDefinition(allDefinitionLibraries.getDefinition(PatientDataDefinition.class, patientDataDefinition.getKey()));
			}
		}
	}
	
	private void setupDefintiionLibrary(DefinitionLibrary definitionLibrary) {
	}
	
	/**
	 * Run during the activator stopped method
	 */
	@Override
	public void stopped() {

	}
}
