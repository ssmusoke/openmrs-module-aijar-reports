package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.EncounterType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reportingcompatibility.service.ReportService.TimeModifier;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
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
        return "ugemr.cohort.hiv.";
    }


    public CohortDefinition getEverEnrolledInCare() {
        return df.getPatientsWithIdentifierOfType(hivMetadata.getHIVIdentifier());
    }

    public CohortDefinition getYearlyPatientsOnCare() {
        return df.getPatientsInPeriod(hivMetadata.getARTSummaryPageEncounterType(), Enums.Period.YEARLY);
    }

    public CohortDefinition getEnrolledInCareByEndOfPreviousDate() {
        return df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getARTSummaryPageEncounterType());
    }

    public CohortDefinition getEnrolledInCareBetweenDates() {
        return df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getARTSummaryPageEncounterType());
    }

    public CohortDefinition getArtPatientsWithEncounterOrSummaryPagesBetweenDates() {
        return df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getArtEncounterTypes());

    }

    public CohortDefinition getTransferredInToCareDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferIn(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredInToCareDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferIn(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredOut() {
        return df.getPatientsWithCodedObs(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredOutDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredOutBy(String olderThan) {
        return df.getPatientsWithCodedObsByEndDate(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredOut(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getInterruptedMedically() {
        return df.getPatientsWithCodedObs(hivMetadata.getArtInterruption(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getArtInterruptionStopped()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getInterruptedMedically(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtInterruption(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getArtInterruptionStopped()), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition gePatientsWhoDied() {
        return df.getPatientsWithCodedObs(hivMetadata.getDead(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition gePatientsWhoDiedBy(String olderThan) {
        return df.getPatientsWithCodedObsByEndDate(hivMetadata.getDead(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition gePatientsWhoDied(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getDead(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTransferredInToCareBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getTransferIn(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getEnrolledInCareToCareWhenPregnantOrLactating() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getEntryPoint(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getEMTCTAtEnrollment()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPregnantOrLactating() {
        return df.getPatientsWithCodedObs(hivMetadata.getEMTCTAtEnrollment(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.FIRST);
    }

    //    public CohortDefinition getPregnantAtArtStart() {
    //        return df.getPatientsWithCodedObs(hivMetadata.getPregnantAtArtStart(), Arrays.asList(hivMetadata.getYesPregnant()), BaseObsCohortDefinition.TimeModifier.ANY);
    //    }
    //
    //
    //    public CohortDefinition getLactatingAtArtStart() {
    //        return df.getPatientsWithCodedObs(hivMetadata.getLactatingAtArtStart(), Arrays.asList(hivMetadata.getYesPregnant()), BaseObsCohortDefinition.TimeModifier.ANY);
    //    }

    public CohortDefinition getPregnantOrLactating(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getEMTCTAtEnrollment(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getOnINHDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getINHDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getOnINHDuringBeforePeriod() {
        return df.getPatientsWithNumericObsByEndOfPreviousDate(hivMetadata.getINHDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDateBetweenPeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDate() {
        return df.getPatientsWhoseObsValueAfterDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDateBetweenPeriod(String olderThan) {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getArtStartDateBeforePeriod() {
        return df.getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY, "1d", null);
    }

    public CohortDefinition getArtStartDateBeforePeriod(String olderThan) {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getArtRegimenTransferInDateBetweenPeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getArtRegimenTransferInDateBeforePeriod() {
        return df.getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY, "1d", null);
    }

    public CohortDefinition getPatientsHavingRegimenBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingRegimenBeforePeriod(String olderThan) {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingRegimenDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenBeforePeriod(String olderThan) {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingBaseRegimenDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingTransferInRegimenBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getArtTransferInRegimen(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingTransferInRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtTransferInRegimen(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsTransferredOutDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTransferredOut(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsHavingTransferInRegimenDuringPeriod(String olderThan) {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtTransferInRegimen(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getOnCPTDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCPTDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getOnCPTBeforePeriod() {
        return df.getPatientsWithNumericObsByEndOfPreviousDate(hivMetadata.getCPTDosage(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getAccessedForTBDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getDiagnosedWithTBDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusDiagnosed()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getScreenedForTBNegativeDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusNoSignsOrSymptoms()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getStartedTBRxBeforePeriod() {
        return df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusRx()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getStartedTBRxDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusRx()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoseTBStartDateDuringThePeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getTBStartDate(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoWeightWasTakenDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBodyWeight(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoBaseWeightWasTakenDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineBodyWeight(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoHeightWasTakenDuringPeriod() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBodyHeight(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoMUACWasTakenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMUAC(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoseOedemaWasTakenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getOedema(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsAssessedForMalnutrition() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMalnutrition(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    //
    public CohortDefinition getPatientsWhoMUACWasRedOrYellowDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMUAC(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getMUACYellow(), hivMetadata.getMUACRed()), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoseOedemaWasYesDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getOedema(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYes()), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWhoAreMalnourishedDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getMalnutrition(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getMalnutritionYes(), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getPatientsWithGoodAdherence() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getGoodAdherence()), BaseObsCohortDefinition.TimeModifier.MAX);
    }

    public CohortDefinition getChildrenOnFirstLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsChildren(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getChildrenOnFirstLineRegimen() {
        return df.getPatientsWithCodedObs(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsChildren(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getChildrenOnSecondLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getSecondLineDrugsChildren(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getChildrenOnSecondLineRegimen() {
        return df.getPatientsWithCodedObs(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getSecondLineDrugsChildren(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getAdultsOnFirstLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsAdult(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getAdultsOnFirstLineRegimen() {
        return df.getPatientsWithCodedObs(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getFirstLineDrugsAdults(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getAdultsOnSecondLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getSecondLineDrugsAdult(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getAdultsOnSecondLineRegimen() {
        return df.getPatientsWithCodedObs(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getSecondLineDrugsAdults(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsOnThirdLineRegimenDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getThirdLineDrugs(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsOnThirdLineRegimen() {
        return df.getPatientsWithCodedObs(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getThirdLineDrugs(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsStartedArtBasedOnCD4() {
        return df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4AtEnrollment(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.GREATER_THAN, 0.0, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsPregnantAtFirstEncounter() {
        return df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getPregnant(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYesPregnant()), BaseObsCohortDefinition.TimeModifier.FIRST);
    }

    public CohortDefinition getPregnantPatientsAtArtStart() {
        return df.getPatientsWithCodedObs(hivMetadata.getPregnantAtArtStart(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYesPregnant()), BaseObsCohortDefinition.TimeModifier.FIRST);
    }

    public CohortDefinition getLactatingPatientsAtArtStart() {
        return df.getPatientsWithCodedObs(hivMetadata.getLactatingAtArtStart(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYesPregnant()), BaseObsCohortDefinition.TimeModifier.FIRST);
    }

    public CohortDefinition getLastVisitDateByEndOfQuarter() {
        return df.getPatientsWhoseMostRecentObsDateIsBetweenValuesByEndDate(hivMetadata.getReturnVisitDate(), hivMetadata.getArtEncounterTypes(), BaseObsCohortDefinition.TimeModifier.MAX, null, null);
    }

    public CohortDefinition getAppointments() {
        return df.getLastVisitInTheQuarter(hivMetadata.getReturnVisitDate(), TimeModifier.MAX);
    }

    public CohortDefinition getEligibleAndReadyByEndOfQuarter() {
        return df.getPatientsWhoseObsValueDateIsByEndDate(hivMetadata.getDateEligibleAndReadyForART(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getEarlyWarningIndicatorDataAbstractionCohort() {
        return df.EarlyWarningIndicatorDataAbstractionCohort(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWithFirstEncounterInThePeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }


    public CohortDefinition getPatientsWithTransferInPlace() {
        return df.getTextBasedObs(hivMetadata.getTransferInPlace());
    }

    public CohortDefinition getPatientsWhoStartedArtQuartersAgo(Integer quartersBack) {
        return df.getWhoStartedArtDuringPeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack);
    }

    public CohortDefinition getPatientsWhoStartedPregnantQuartersAgo(Integer quartersBack) {
        return df.getPregnantOrLactatingDuringPeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack);
    }

    public CohortDefinition getPatientsOnArtWithCD4QuartersAgo(Integer quartersBack, Boolean allBaseCD4) {
        return df.getStartedArtWithCD4DuringPeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack, allBaseCD4);
    }

    public CohortDefinition getDeadPatientsOnArtQuartersAgo(Integer quartersBack) {
        return df.getDeadPatientsOnArtBeforePeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack);
    }

    public CohortDefinition getStoppedPatientsOnSArtQuartersAgo(Integer quartersBack) {
        return df.getStoppedPatientsOnArtDuringPeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack);
    }

    public CohortDefinition getPatientsOnArtWhoMissedQuartersAgo(Integer quartersBack) {
        return df.getLostPatientsOnArtDuringPeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack, Boolean.FALSE);
    }

    public CohortDefinition getLostPatientsOnArtQuartersAgo(Integer quartersBack) {
        return df.getLostPatientsOnArtDuringPeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack, Boolean.TRUE);
    }

    public CohortDefinition getPatientsOnArtWithCD4BeforeQuartersAgo(Integer quartersBack, Boolean allBaseCD4) {
        return df.getStartedArtWithCD4BeforePeriod(Enums.Period.QUARTERLY, Enums.PeriodInterval.BEFORE, quartersBack, allBaseCD4);
    }

    public CohortDefinition getActiveWithNoEncounterInQuarter() {
        return df.getActiveInPeriodWithoutVisit();
    }

    public CohortDefinition getPatientsWhoStartedArtMonthsAgo(String monthsBack) {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(),
                hivMetadata.getARTSummaryPageEncounterType(), monthsBack, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWithViralLoadDuringPeriod() {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getViralLoadDate(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()),BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWithViralLoadDuringPeriod(String monthsBack) {
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getViralLoadDate(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()), monthsBack, BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoseLastViralLoadWasMonthsAgoFromPeriod(String monthsBack){
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getViralLoadDate(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()),monthsBack,BaseObsCohortDefinition.TimeModifier.LAST
                );
    }

    public CohortDefinition getPatientsWhoseLastViralLoadWasMonthsAgoFromEndDate(String monthsBack){
        return df.getPatientsWhoseObsValueDateIsByEndDate(hivMetadata.getViralLoadDate(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()),BaseObsCohortDefinition.TimeModifier.LAST,monthsBack);
    }

    public CohortDefinition getPatientsWithLastViralLoadByEndDate(){
        return df.getPatientsWhoseObsValueDateIsByEndDate(hivMetadata.getViralLoadDate(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()),BaseObsCohortDefinition.TimeModifier.ANY
        );
    }

    public CohortDefinition getPatientsWithArtStartDateByEndDate(String olderThan) {
        return df.getPatientsWhoseObsValueDateIsByEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY, olderThan);
    }

    public CohortDefinition getPatientsWhoTestedHIVPositiveDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),null,Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE)),BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoTestedHIVNegativeDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),null,Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_NEGATIVE)),BaseObsCohortDefinition.TimeModifier.ANY);
    }
}
