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

    public PatientIdentifierType getHIVIdentifier() {
        return getPatientIdentifierType("e1731641-30ab-102d-86b0-7a5022ba4115");
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

    public Concept getEntryPoint() {
        return getConcept("90200");
    }

    public Concept getPCRAtEnrollment() {
        return getConcept("99149");
    }

    public Concept getLactatingAtEnrollment() {
        return getConcept("99601");
    }

    public Concept getTBAtEnrollment() {
        return getConcept("99600");
    }

    public Concept getPregnantAtEnrollment() {
        return getConcept("99602");
    }

    public Concept getTransferInAtEnrollment() {
        return getConcept("99110");
    }

    public Concept getCPTDosage() {
        return getConcept("99037");
    }

    public Concept getINHDosage() {
        return getConcept("99604");
    }

    public Concept getTBStartDate() {
        return getConcept("90217");
    }

    public Concept getTBStopDate() {
        return getConcept("90310");
    }


    public Concept getWHOClinicalStage() {
        return getConcept("90203");
    }

    public Concept getWHOClinicalStage1() {
        return getConcept("90033");
    }

    public Concept getWHOClinicalStage2() {
        return getConcept("90034");
    }

    public Concept getWHOClinicalStage3() {
        return getConcept("90035");
    }

    public Concept getWHOClinicalStage4() {
        return getConcept("90036");
    }


    public Concept getDateEligibleForART() {
        return getConcept("90297");
    }

    public Concept getDateEligibleAndReadyForART() {
        return getConcept("90299");
    }


    public Concept getDateEligibilityWHOStage() {
        return getConcept("99083");
    }

    public Concept getDateEligibilityCD4() {
        return getConcept("99082");
    }

    public Concept getDateEligibilityTB() {
        return getConcept("99600");
    }

    public Concept getDateEligibilityBreastFeeding() {
        return getConcept("99601");
    }

    public Concept getDateEligibilityPregnant() {
        return getConcept("99602");
    }

    public Concept getCD4AtEnrollment() {
        return getConcept("99082");
    }

    public Concept getBaselineCD4() {
        return getConcept("99071");
    }  // CD4 at ART start



    public EncounterType getARTSummaryEncounter() {
        return getEncounterType("8d5b27bc-c2cc-11de-8d13-0010c6dffd0f");
    }

    public EncounterType getARTEncounterEncounterType() {
        return getEncounterType("8d5b2be0-c2cc-11de-8d13-0010c6dffd0f");
    }


}
