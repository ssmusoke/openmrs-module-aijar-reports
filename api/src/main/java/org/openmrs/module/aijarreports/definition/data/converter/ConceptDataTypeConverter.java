package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts a Concept id to Concept data type
 */
public class ConceptDataTypeConverter implements DataConverter {

	public ConceptDataTypeConverter() {
	}

	@Override
	public Object convert(Object original) {
		ConceptService cs = Context.getConceptService();
		Concept c = cs.getConcept((Integer) original);
		return c.getDatatype().getName();
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
