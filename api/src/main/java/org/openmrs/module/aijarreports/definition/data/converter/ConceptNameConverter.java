package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts a concept id to concept name
 */
public class ConceptNameConverter implements DataConverter {

	public ConceptNameConverter() {
	}

	@Override
	public Object convert(Object original) {
		ConceptService cs = Context.getConceptService();
		Concept c = cs.getConcept((Integer) original);
		//Think of i18n'd name
		return c.getName(Context.getLocale());
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
