package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts a visit id to visit type name
 */
public class VisitTypeFromIdConverter implements DataConverter {

	public VisitTypeFromIdConverter() {
	}

	@Override
	public Object convert(Object original) {
		if (original != null) {
			VisitService vs = Context.getVisitService();
			Visit v = vs.getVisit((Integer) original);
			return (v.getVisitType() == null)? "": v.getVisitType().getName();
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
