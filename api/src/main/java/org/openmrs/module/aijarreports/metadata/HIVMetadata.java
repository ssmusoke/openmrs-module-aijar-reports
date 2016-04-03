package org.openmrs.module.aijarreports.metadata;

import org.openmrs.Concept;
import org.springframework.stereotype.Component;

/**
 * Metadata definitions for the HIV related reporting
 */
@Component("hivMetadata")
public class HIVMetadata extends Metadata {

	public Concept getReturnVisitDate() {
		return getConcept("5096");
	}
}
