package org.openmrs.module.aijarreports.metadata;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    public Concept getDateOfNVP() {
        return getConcept("99771");
    }

    public Concept getDateOfCPT() {
        return getConcept("99773");
    }

    public Concept getFirstPCRTestDate() {
        return getConcept("99606");
    }

    public Concept getSecondPCRTestDate() {
        return getConcept("99436");
    }

    public Concept getRepeatPCRTestDate() {
        return getConcept("162876");
    }

    public Concept get18MonthsRapidPCRTestDate() {
        return getConcept("162879");
    }

    public Concept getFirstPCRTestResultGivenToCareProviderDate() {
        return getConcept("99438");
    }

    public Concept getSecondPCRTestResultGivenToCareProviderDate() {
        return getConcept("99442");
    }

    public Concept getRepeatPCRTestResultGivenToCareProviderDate() {
        return getConcept("162882");
    }

    public Concept get18MonthsRapidPCRTestResultGivenToCareProviderDate() {
        return getConcept("162883");
    }

    public Concept getFirstPCRTestResults() {
        return getConcept("99435");
    }

    public Concept getSecondPCRTestResults() {
        return getConcept("99440");
    }

    public Concept getRepeatPCRTestResults() {
        return getConcept("162881");
    }

    public Concept get18MonthsRapidPCRTestResults() {
        return getConcept("162880");
    }

    public Concept getPositiveResult() {
        return getConcept("703");
    }

    public Concept getNegativeResult() {
        return getConcept("664");
    }

    public Concept getFinalStatus() {
        return getConcept("99797");
    }

    public Concept getFinalOutcome() {
        return getConcept("99428");
    }

    public Concept getFinalOutcomeDischargedNegative() {
        return getConcept("99427");
    }

    public Concept getFinalOutcomeReferredToArtClinic() {
        return getConcept("99430");
    }

    public Concept getFinalOutcomeLost() {
        return getConcept("5240");
    }

    public Concept getFinalOutcomeTransferred() {
        return getConcept("90306");
    }

    public Concept getFinalOutcomeDied() {
        return getConcept("99112");
    }

    public PatientIdentifierType getPatientsWithEIDIdentifier() {
        return getPatientIdentifierType("2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe");
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

    public Concept getTransferIn() {
        return getConcept("99110");
    }

    public Concept getYes() {
        return getConcept("90003");
    }
}
