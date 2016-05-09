package org.openmrs.module.aijarreports.library;

import org.openmrs.Concept;
import org.openmrs.api.PatientSetService;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Defines all the Cohort Definitions instances from the ART clinic
 */
@Component
public class EIDCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {
    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "aijar.cohort.eid.";
    }

    @DocumentedDefinition(value = "exposedinfant", name = "Exposed Infants in Period")
    public CohortDefinition getEnrolledInCareDuringPeriod() {
        EncounterCohortDefinition q = new EncounterCohortDefinition();
        q.setEncounterTypeList(hivMetadata.getEIDSummaryPageEncounterType());
        q.addParameter(df.getStartDateParameter());
        q.addParameter(df.getEndDateParameter());
        return q;
    }

    public CohortDefinition getPatientsWithObsValueAtArtInitiationAtLocationByEnd(Concept question, Concept... values) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setEncounterTypeList(hivMetadata.getEIDSummaryPageEncounterType());
        cd.setTimeModifier(PatientSetService.TimeModifier.ANY);
        cd.setQuestion(question);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(Arrays.asList(values));
        return df.convert(cd, null);
    }

    public CohortDefinition getPatients2to14YearsOldAtPreArtStateStartAtLocationByEndDate() {
        return df.getPatientsWhoStartedStateWhenInAgeRangeAtLocationByEndDate(0, Age.Unit.YEARS, 100, Age.Unit.YEARS);
    }

    @DocumentedDefinition(value = "allEidPatients")
    public CohortDefinition getAllEIDPatients() {
        return df.getAnyEncounterOfType(hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients1")
    public CohortDefinition getEIDPatientsGivenNVP() {
        return df.getObsWithEncounters(hivMetadata.getDateOfNVP(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients2")
    public CohortDefinition getEIDPatientsInitiatedOnCPT() {
        return df.getObsWithEncounters(hivMetadata.getDateOfCPT(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients3")
    public CohortDefinition getEIDPatientsTestedUsingFirstDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getFirstPCRTestDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients4")
    public CohortDefinition getEIDPatientsTestedUsingSecondDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getSecondPCRTestDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients5")
    public CohortDefinition getEIDPatientsTestedUsingABTest() {
        return df.getObsWithEncounters(hivMetadata.get18MonthsRapidPCRTestDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients6")
    public CohortDefinition getEIDPatientsTestedUsingFirstDNAPCRWhoseResultsGivenToCareGiver() {
        return df.getObsWithEncounters(hivMetadata.getFirstPCRTestResultGivenToCareProviderDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients7")
    public CohortDefinition getEIDPatientsTestedUsingSecondDNAPCRWhoseResultsGivenToCareGiver() {
        return df.getObsWithEncounters(hivMetadata.getSecondPCRTestResultGivenToCareProviderDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients8")
    public CohortDefinition getEIDPatientsTestedUsingABTestWhoseResultsGivenToCareGiver() {
        return df.getObsWithEncounters(hivMetadata.get18MonthsRapidPCRTestResultGivenToCareProviderDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    @DocumentedDefinition(value = "eidPatients9")
    public CohortDefinition getEIDPatientsTestedPositiveUsingFirstDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getFirstPCRTestResults(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    @DocumentedDefinition(value = "eidPatients10")
    public CohortDefinition getEIDPatientsTestedPositiveUsingSecondDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getSecondPCRTestResults(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    @DocumentedDefinition(value = "eidPatients11")
    public CohortDefinition getEIDPatientsTestedPositiveUsingABTest() {
        return df.getObsWithEncounters(hivMetadata.get18MonthsRapidPCRTestResults(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    @DocumentedDefinition(value = "eidPatients12")
    public CohortDefinition getEIDPatientsWhoDied() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getFinalOutcomeDied()));
    }

    @DocumentedDefinition(value = "eidPatients13")
    public CohortDefinition getEIDPatientsFinallyPositive() {
        return df.getObsWithEncounters(hivMetadata.getFinalStatus(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    @DocumentedDefinition(value = "eidPatients14")
    public CohortDefinition getEIDPatientsFinallyNegative() {
        return df.getObsWithEncounters(hivMetadata.getFinalStatus(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getNegativeResult()));
    }

    @DocumentedDefinition(value = "eidPatients15")
    public CohortDefinition getEIDPatientsFinallyTransferredOut() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getFinalOutcomeTransferred()));
    }

    @DocumentedDefinition(value = "eidPatients16")
    public CohortDefinition getEIDTransferIns() {
        return df.getObsWithEncounters(hivMetadata.getTransferIn(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()));
    }

    @DocumentedDefinition(value = "eidPatients17")
    public CohortDefinition getEIDLostToFollowup() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getFinalOutcomeLost()));
    }

    @DocumentedDefinition(value = "hasAnEIDNumber")
    public CohortDefinition getPatientsWithAnEIDNumber() {
        return df.getPatientsWithIdentifierOfType(hivMetadata.getPatientsWithEIDIdentifier());
    }
}
