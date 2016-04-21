package org.openmrs.module.aijarreports.metadata;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

/**
 * Metadata definitions for the HIV related reporting
 */
@Component("hivMetadata")
public class HIVMetadata extends Metadata {

	public Concept getReturnVisitDate() {
		return getConcept("5096");
	}

	public Concept getARTStartDate() {
		return getConcept("99161");
	}

	public Concept getStartRegimen() {
		return getConcept("99061");
	}

	public Concept getStartRegimenDate() {
		return getConcept("99161");
	}

	public List<EncounterType> getARTSummaryPageEncounterType() {
		List<EncounterType> l = new ArrayList<EncounterType>();
		l.add(MetadataUtils.existing(EncounterType.class, "8d5b27bc-c2cc-11de-8d13-0010c6dffd0f"));
		return l;
	}

	public List<EncounterType> getEIDSummaryPageEncounterType() {
		List<EncounterType> l = new ArrayList<EncounterType>();
		l.add(MetadataUtils.existing(EncounterType.class, "9fcfcc91-ad60-4d84-9710-11cc25258719"));
		return l;
	}

	public Concept getARVDuration() {
		return getConcept("99036");
	}

	public Concept getExpectedReturnDate() {
		return getConcept("5096");
	}

	public Concept getCurrentRegimen() {
		return getConcept("90315");
	}
}
