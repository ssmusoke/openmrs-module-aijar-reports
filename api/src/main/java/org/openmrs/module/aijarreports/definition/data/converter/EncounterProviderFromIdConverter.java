package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.Encounter;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an Encounter ID to the corresponding Encounter creator full name
 */
public class EncounterProviderFromIdConverter implements DataConverter {

	public EncounterProviderFromIdConverter() {
	}

	@Override
	public Object convert(Object original) {
		if (original != null) {
			EncounterService es = Context.getEncounterService();
			Encounter e = es.getEncounter((Integer) original);
			User provider = e.getCreator();
			return (provider.getPersonName() == null) ? "" : provider.getPersonName().getFullName();
		} else {
			return "";
		}
	}

	@Override
	public Class<?> getInputDataType() {
		return Integer.class;
	}

	@Override
	public Class<?> getDataType() {
		return Object.class;
	}
}
