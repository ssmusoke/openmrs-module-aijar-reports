package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an visit id to visit uuid
 */
public class VisitUUIDFromIdConverter implements DataConverter {

	public VisitUUIDFromIdConverter() {
	}

	@Override
	public Object convert(Object original) {
		if (original != null) {
			VisitService vs = Context.getVisitService();
			Visit v = vs.getVisit((Integer) original);
			return (v == null) ? "" : v.getUuid();
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
