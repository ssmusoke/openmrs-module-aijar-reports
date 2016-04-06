package org.openmrs.module.aijarreports.library;

import org.openmrs.module.aijarreports.metadata.HIVMetadata;
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
public class EIDCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {
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
		return "aijar.cohort.eid.";
	}

	@DocumentedDefinition(value = "exposedinfant", name="Exposed Infants in Period")
	public CohortDefinition getEnrolledInCareDuringPeriod() {
		EncounterCohortDefinition q = new EncounterCohortDefinition();
		q.setEncounterTypeList(hivMetadata.getEIDSummaryPageEncounterType());
		q.addParameter(df.getStartDateParameter());
		q.addParameter(df.getEndDateParameter());
		return q;
	}
}
