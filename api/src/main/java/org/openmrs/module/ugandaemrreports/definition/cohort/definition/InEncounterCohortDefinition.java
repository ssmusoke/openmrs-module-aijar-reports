package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import java.util.Date;
import java.util.List;

import org.openmrs.EncounterType;
import org.openmrs.module.reporting.cohort.definition.BaseCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

/**
 */
public class InEncounterCohortDefinition extends BaseCohortDefinition {

	private static final long serialVersionUID = 1L;

	@ConfigurationProperty
	private Date startDate;

	@ConfigurationProperty
	private Date endDate;

	@ConfigurationProperty
	private List<EncounterType> encounterTypes;

	public InEncounterCohortDefinition() {
		super();
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<EncounterType> getEncounterTypes() {
		return encounterTypes;
	}

	public void setEncounterTypes(List<EncounterType> encounterTypes) {
		this.encounterTypes = encounterTypes;
	}
}
