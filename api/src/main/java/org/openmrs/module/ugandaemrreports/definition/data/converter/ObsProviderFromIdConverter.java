package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an Obs Id to the corresponding creator name.
 * We first try to retrieve the last user who changed the Obs with Obs#getChangedBy()
 * and if that returns null, we use Obs#getCreator() to retrieve the creator of the Obs
 */
public class ObsProviderFromIdConverter implements DataConverter {

	public ObsProviderFromIdConverter() {
	}

	@Override
	public Object convert(Object original) {
		Obs o = (Obs) Context.getObsService().getObs((Integer) original);
		if (o == null) {
			return null;
		}
		User provider = o.getChangedBy() != null ? o.getChangedBy() : o.getCreator();
		return provider.getPersonName().getFullName();
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
