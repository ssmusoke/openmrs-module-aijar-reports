package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Defines all the Cohort Definitions instances from the ART clinic
 */
@Component
public class ARTClinicCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

	@Autowired
	private DataFactory df;

	@Autowired
	private HIVMetadata hivMetadata;

	@Override
	public Class<? super CohortDefinition> getDefinitionType() {
		return CohortDefinition.class;
	}

	@Override
	public String getKeyPrefix() {
		return "ugemr.cohort.art.";
	}

	@DocumentedDefinition(value = "enrolledincare", name = "Patients Enrolled in Care As of Date")
	public CohortDefinition getPatientsEnrolledInCareBetweenStartAndEndDate() {
		EncounterCohortDefinition q = new EncounterCohortDefinition();
		q.setEncounterTypeList(hivMetadata.getARTSummaryPageEncounterType());
		q.addParameter(df.getStartDateParameter());
		q.addParameter(df.getEndDateParameter());
		return q;
	}

	@DocumentedDefinition(value = "returnvisitdate", name = "Patients with return visit on date")
	public CohortDefinition getPatientsWithReturnVisitDateOnEndDate() {
		return df.getPatientsWhoseObsValueDateIsOnSpecifiedDate(hivMetadata.getReturnVisitDate(), hivMetadata.getARTEncounterPageEncounterType());
	}

	/*@DocumentedDefinition(value = "startedARTBetweenStartAndEndDate")
	public CohortDefinition getPatientsThatStartedARTBetweenStartAndEndDate() {
		return null;
	}*/

}
