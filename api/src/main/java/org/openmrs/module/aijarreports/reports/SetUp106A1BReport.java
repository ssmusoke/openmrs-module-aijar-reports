package org.openmrs.module.aijarreports.reports;

import org.openmrs.api.PatientSetService;
import org.openmrs.module.aijarreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.library.DataFactory;
import org.openmrs.module.aijarreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 07/06/2016.
 */
@Component

public class SetUp106A1BReport extends AijarDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CohortDefinitionService cohortDefinitionService;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;

    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119c8ef23";
    }

    @Override
    public String getUuid() {
        return "167cf668-0715-488b-b159-d5f391774038";
    }

    @Override
    public String getName() {
        return "HMIS 106A1B";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1B";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start date (Start of quarter)", Date.class));
        l.add(new Parameter("endDate", "End date (End of quarter)", Date.class));
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1BReport.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("indicators_106a1b", Mapped.mapStraightThrough(dsd));
        String olderThan;

        CohortDefinition enrolledWhenPregnantOrLactating = hivCohortDefinitionLibrary.getPregnantOrLactating();
        CohortDefinition transferInRegimen = df.getPatientsWithConcept(hivMetadata.getArtTransferInRegimen(), PatientSetService.TimeModifier.ANY);
        CohortDefinition transferInRegimenOther = df.getPatientsWithConcept(hivMetadata.getOtherArtTransferInRegimen(), PatientSetService.TimeModifier.ANY);
        CohortDefinition transferInRegimenDate = df.getPatientsWhoseObs(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType());
        CohortDefinition patientsWithCD4 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4(), hivMetadata.getARTEncounterPageEncounterType(), PatientSetService.TimeModifier.LAST);
        CohortDefinition patientsHavingCD4LessThan250 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.LESS_EQUAL, 250.0, PatientSetService.TimeModifier.LAST);
        CohortDefinition patientsOlderThan4Years = commonCohortDefinitionLibrary.agedAtLeast(5);
        CohortDefinition transferInFrom = hivCohortDefinitionLibrary.getPatientsWithTransferInPlace();
        CohortDefinition patientsTransferredIn = df.getPatientsInAny(transferInRegimen, transferInRegimenOther, transferInRegimenDate, transferInFrom);
        CohortDefinition medicallyInterrupted = hivCohortDefinitionLibrary.getInterruptedMedically();
        CohortDefinition deadPatients = hivCohortDefinitionLibrary.gePatientsWhoDied();
        CohortDefinition transferOut = hivCohortDefinitionLibrary.getTransferredOut();
        CohortDefinition lostToFollow = df.getLostToFollowUp();
        CohortDefinition lost = df.getLost();

        for (int i = 0; i <= 6; i++) {

            if (i == 0) {
                olderThan = "6m";
            } else {
                olderThan = i + "y";
            }

            CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod(olderThan);
            CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod(olderThan + "-1d");

            CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod(olderThan);
            CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod(olderThan + "-1d");

            CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(olderThan);
            CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod(olderThan + "-1d");

            CohortDefinition patientsWithBaseCD4 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTEncounterPageEncounterType(), olderThan, PatientSetService.TimeModifier.LAST);
            CohortDefinition patientsHavingBaseCD4LessThan250 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.LESS_EQUAL, 250.0, olderThan, PatientSetService.TimeModifier.FIRST);


            CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);
            CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter, havingBaseRegimenDuringQuarter);

            CohortDefinition startedArtDuringQuarter = df.getPatientsNotIn(beenOnArtDuringQuarter, beenOnArtBeforeQuarter);

            CohortDefinition netTransferIn = df.getPatientsInAll(patientsTransferredIn, havingArtStartDateDuringQuarter);
            CohortDefinition startedArtInFacility = df.getPatientsNotIn(startedArtDuringQuarter, patientsTransferredIn);
            CohortDefinition patientsOver4YearsWithBaseCD4 = df.getPatientsInAll(startedArtInFacility, patientsOlderThan4Years, patientsWithBaseCD4);
            CohortDefinition patientsOver4YearsWithBaseCD4LessThan250 = df.getPatientsInAll(startedArtInFacility, patientsOlderThan4Years, patientsHavingBaseCD4LessThan250);
            CohortDefinition netTransferredOut = df.getPatientsInAll(startedArtInFacility, transferOut);
            CohortDefinition netCurrentCohort = df.createPatientComposition("(", startedArtInFacility, "OR", netTransferIn, ") AND NOT ", netTransferredOut);
            CohortDefinition netStopped = df.getPatientsInAll(netCurrentCohort, medicallyInterrupted);
            CohortDefinition netDied = df.getPatientsInAll(netCurrentCohort, deadPatients);
            CohortDefinition netLostToFollowUp = df.getPatientsInAll(netCurrentCohort, lostToFollow);
            CohortDefinition netLost = df.getPatientsInAll(netCurrentCohort, lost);
            CohortDefinition patientsOver4YearsWithCD4 = df.getPatientsInAll(netCurrentCohort, patientsOlderThan4Years, patientsWithCD4);
            CohortDefinition patientsOver4YearsWithCD4LessThan250 = df.getPatientsInAll(netCurrentCohort, patientsOlderThan4Years, patientsHavingCD4LessThan250);
            CohortDefinition netCurrentCohortAlive = df.createPatientComposition(netCurrentCohort, "AND NOT", df.getPatientsInAny(netStopped, netDied, netLostToFollowUp));

            CohortDefinition netTransferInMothers = df.getPatientsInAll(netTransferIn, enrolledWhenPregnantOrLactating);
            CohortDefinition startedArtInFacilityMothers = df.getPatientsInAll(startedArtInFacility, enrolledWhenPregnantOrLactating);
            CohortDefinition patientsOver4YearsWithBaseCD4Mothers = df.getPatientsInAll(patientsOver4YearsWithBaseCD4, enrolledWhenPregnantOrLactating);
            CohortDefinition patientsOver4YearsWithBaseCD4LessThan250Mothers = df.getPatientsInAll(patientsOver4YearsWithBaseCD4LessThan250, enrolledWhenPregnantOrLactating);
            CohortDefinition netTransferredOutMothers = df.getPatientsInAll(netTransferredOut, enrolledWhenPregnantOrLactating);
            CohortDefinition netCurrentCohortMothers = df.getPatientsInAll(netCurrentCohort, enrolledWhenPregnantOrLactating);
            CohortDefinition netStoppedMothers = df.getPatientsInAll(netStopped, enrolledWhenPregnantOrLactating);
            CohortDefinition netDiedMothers = df.getPatientsInAll(netDied, enrolledWhenPregnantOrLactating);
            CohortDefinition netLostToFollowUpMothers = df.getPatientsInAll(netLostToFollowUp, enrolledWhenPregnantOrLactating);
            CohortDefinition netLostMothers = df.getPatientsInAll(netLost, enrolledWhenPregnantOrLactating);
            CohortDefinition patientsOver4YearsWithCD4Mothers = df.getPatientsInAll(patientsOver4YearsWithCD4, enrolledWhenPregnantOrLactating);
            CohortDefinition patientsOver4YearsWithCD4LessThan250Mothers = df.getPatientsInAll(patientsOver4YearsWithCD4LessThan250, enrolledWhenPregnantOrLactating);
            CohortDefinition netCurrentCohortAliveMothers = df.getPatientsInAll(netCurrentCohortAlive, enrolledWhenPregnantOrLactating);

            // For Debugging
            addIndicator(dsd, String.valueOf(i + 1) + "A1", "On ART This Quarter", onArtDuringQuarter);
            addIndicator(dsd, String.valueOf(i + 1) + "A2", "On ART Before Quarter", onArtBeforeQuarter);
            addIndicator(dsd, String.valueOf(i + 1) + "A3", "Having Base Regimen in the Quarter", havingBaseRegimenDuringQuarter);
            addIndicator(dsd, String.valueOf(i + 1) + "A4", "Having Base Regimen Before Quarter", havingBaseRegimenBeforeQuarter);
            addIndicator(dsd, String.valueOf(i + 1) + "A5", "Art Start During Quarter", havingArtStartDateDuringQuarter);
            addIndicator(dsd, String.valueOf(i + 1) + "A6", "Art Start Before Quarter", havingArtStartDateBeforeQuarter);

            // End Debugging

            addIndicator(dsd, String.valueOf(i + 1) + "3", "Started ART in this clinic original cohort", startedArtInFacility);
            addIndicatorPercentage(dsd, String.valueOf(i + 1) + "4", "Fraction of clients and above with base cd4 < 250 numerator", patientsOver4YearsWithBaseCD4LessThan250, patientsOver4YearsWithBaseCD4);
            // addIndicator(dsd, String.valueOf(i + 1) + "5", "Median base CD4", patientsOlderThan4Years);
            addIndicator(dsd, String.valueOf(i + 1) + "6", "TI", netTransferIn);
            addIndicator(dsd, String.valueOf(i + 1) + "7", "TO", netTransferredOut);
            addIndicator(dsd, String.valueOf(i + 1) + "8", "Net current Cohort", netCurrentCohort);
            addIndicator(dsd, String.valueOf(i + 1) + "9", "Stopped", netStopped);
            addIndicator(dsd, String.valueOf(i + 1) + "10", "Died", netDied);
            addIndicator(dsd, String.valueOf(i + 1) + "11", "Lost", netLost);
            addIndicator(dsd, String.valueOf(i + 1) + "12", "Lost to follow up", netLostToFollowUp);
            addIndicator(dsd, String.valueOf(i + 1) + "13", "Net current cohort alive and on art", netCurrentCohortAlive);
            addIndicatorPercentage(dsd, String.valueOf(i + 1) + "14", "Percentage alive and on art", netCurrentCohortAlive, netCurrentCohort);
            addIndicatorPercentage(dsd, String.valueOf(i + 1) + "15", "Fraction of clients and above with cd4 < 250", patientsOver4YearsWithCD4LessThan250, patientsOver4YearsWithCD4);
            // addIndicator(dsd, String.valueOf(i + 1) + "16", "Median CD4", patientsOlderThan4Years);

            addIndicator(dsd, String.valueOf(i + 1) + "3f", "Mothers started ART in this clinic original cohort", startedArtInFacilityMothers);
            addIndicatorPercentage(dsd, String.valueOf(i + 1) + "4f", "Fraction of mothers and above with base cd4 < 250 numerator", patientsOver4YearsWithBaseCD4LessThan250Mothers, patientsOver4YearsWithBaseCD4Mothers);
            // addIndicator(dsd, String.valueOf(i + 1) + "5f", "Median base CD4 for mothers", patientsOlderThan4Years);
            addIndicator(dsd, String.valueOf(i + 1) + "6f", "Mothers TI", netTransferInMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "7f", "Mothers TO", netTransferredOutMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "8f", "Mothers Net current Cohort", netCurrentCohortMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "9f", "Mothers Stopped", netStoppedMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "10f", "Mothers Died", netDiedMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "11f", "Mothers Lost", netLostMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "12f", "Mothers Lost to follow up", netLostToFollowUpMothers);
            addIndicator(dsd, String.valueOf(i + 1) + "13f", "Mothers Net current cohort alive and on art", netCurrentCohortAliveMothers);
            addIndicatorPercentage(dsd, String.valueOf(i + 1) + "14f", "Percentage of mothers alive and on art", netCurrentCohortAliveMothers, netCurrentCohortMothers);
            addIndicatorPercentage(dsd, String.valueOf(i + 1) + "15f", "Fraction of mothers and above with cd4 < 250", patientsOver4YearsWithCD4LessThan250Mothers, patientsOver4YearsWithCD4Mothers);
            // addIndicator(dsd, String.valueOf(i + 1) + "16f", "Median CD4 for mothers", patientsOlderThan4Years);

        }
        return rd;
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), "");
    }

    public void addIndicatorPercentage(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, CohortDefinition denominator) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.FRACTION);
        ci.setDenominator(Mapped.mapStraightThrough(denominator));
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), "");
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
