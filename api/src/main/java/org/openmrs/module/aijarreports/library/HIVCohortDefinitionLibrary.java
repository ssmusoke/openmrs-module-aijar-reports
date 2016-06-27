package org.openmrs.module.aijarreports.library;

import org.openmrs.api.PatientSetService;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Created by carapai on 12/05/2016.
 */
@Component
public class HIVCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

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
        return "aijar.cohort.hiv.";
    }


    public CohortDefinition getEverEnrolledInCare() {
        return df.getPatientsWithIdentifierOfType(hivMetadata.getHIVIdentifier());
    }

    public CohortDefinition getYearlyPatientsOnCare() {
        return df.getPatientsInPeriod(hivMetadata.getARTSummaryPageEncounterType(), Period.YEARLY);
    }

    public CohortDefinition getEnrolledInCareByEndOfPreviousDate() {
        return df.getAnyEncounterOfTypesByEndOfPreviousDate(hivMetadata.getARTSummaryPageEncounterType());
    }

    public CohortDefinition getEnrolledInCareBetweenDates() {
        return df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getARTSummaryPageEncounterType());
    }

    public CohortDefinition getArtPatientsWithEncounterOrSummaryPagesBetweenDates() {
        return df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getArtEncounterTypes());

    }

    public CohortDefinition getTransferredInToCareDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferIn(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredInToCareDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferIn(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredOut() {
        return df.getPatientsWithCodedObs(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredOut(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getInterruptedMedically() {
        return df.getPatientsWithCodedObs(hivMetadata.getArtInterruption(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getArtInterruptionStopped()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition gePatientsWhoDied() {
        return df.getPatientsWithCodedObs(hivMetadata.getDead(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredInToCareBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getTransferIn(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getEnrolledInCareToCareWhenPregnantOrLactating() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getEntryPoint(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getEMTCTAtEnrollment()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getOnINHDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getINHDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getOnINHDuringBeforePeriod() {
        return df.getPatientsWithNumericObsByEndOfPreviousDate(hivMetadata.getINHDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDateBetweenPeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDateBetweenPeriod(String olderThan) {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDateBeforePeriod() {
        return df.getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY, "1", "");
    }

    public CohortDefinition getArtStartDateBeforePeriod(String olderThan) {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getArtRegimenTransferInDateBetweenPeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getArtRegimenTransferInDateBeforePeriod() {
        return df.getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY, "1", "");
    }

    public CohortDefinition getPatientsHavingRegimenBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingRegimenBeforePeriod(String olderThan) {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingRegimenDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenBeforePeriod(String olderThan) {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingTransferInRegimenBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getArtTransferInRegimen(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingTransferInRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtTransferInRegimen(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingTransferInRegimenDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtTransferInRegimen(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getOnCPTDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCPTDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getOnCPTBeforePeriod() {
        return df.getPatientsWithNumericObsByEndOfPreviousDate(hivMetadata.getCPTDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getAccessedForTBDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getDiagnosedWithTBDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusDiagnosed()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getStartedTBRxBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusRx()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getStartedTBRxDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusRx()), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoseTBStartDateDuringThePeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getTBStartDate(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoWeightWasTakenDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBodyWeight(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoBaseWeightWasTakenDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineBodyWeight(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoHeightWasTakenDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBodyHeight(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoMUACWasTakenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMUAC(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoseOedemaWasTakenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getOedema(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsAssessedForMalnutrition() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMalnutrition(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.MAX);
    }

    //
    public CohortDefinition getPatientsWhoMUACWasRedOrYellowDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMUAC(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getMUACYellow(), hivMetadata.getMUACRed()), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoseOedemaWasYesDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getOedema(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYes()), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoAreMalnourishedDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMalnutrition(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getMalnutritionYes(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWithGoodAdherence() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getGoodAdherence()), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getChildrenOnFirstLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsChildren(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getChildrenOnSecondLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsChildren(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getAdultsOnFirstLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsAdults(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getAdultsOnSecondLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsAdults(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsOnThirdLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getThirdLineDrugs(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsStartedArtBasedOnCD4() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4AtEnrollment(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsPregnantAtFirstEncounter() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getPregnant(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYesPregnant()), PatientSetService.TimeModifier.FIRST);
    }

    public CohortDefinition getLastVisitDateByEndOfQuarter() {
        return df.getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(hivMetadata.getVisitDate(), hivMetadata.getArtEncounterTypes(), PatientSetService.TimeModifier.MAX, null, null);
    }

    public CohortDefinition getAppointments() {
        return df.getLastVisitInTheQuarter(hivMetadata.getVisitDate(), PatientSetService.TimeModifier.MAX);
    }

    public CohortDefinition getEligibleAndReadyByEndOfQuarter() {
        return df.getPatientsWhoseObsValueDateIsByEndDate(hivMetadata.getDateEligibleAndReadyForART(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWithFirstEncounterInThePeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), PatientSetService.TimeModifier.ANY);
    }


}
