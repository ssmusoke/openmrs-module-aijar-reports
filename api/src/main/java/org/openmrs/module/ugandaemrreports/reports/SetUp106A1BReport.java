package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.aggregation.MedianAggregator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
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

public class SetUp106A1BReport extends UgandaEMRDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private HIVPatientDataLibrary hivPatientDataLibrary;

    @Autowired
    private DataFactory df;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

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


        CohortDefinition transferInRegimen = df.getPatientsWithConcept(hivMetadata.getArtTransferInRegimen(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transferInRegimenOther = df.getPatientsWithConcept(hivMetadata.getOtherArtTransferInRegimen(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transferInRegimenDate = df.getPatientsWhoseObs(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType());
        CohortDefinition transferInFrom = hivCohortDefinitionLibrary.getPatientsWithTransferInPlace();

        CohortDefinition patientsTransferredIn = df.getPatientsInAny(transferInRegimen, transferInRegimenOther, transferInRegimenDate, transferInFrom);

        CohortDefinition patientsWithCD4 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4(), hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsHavingCD4LessThan250 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4(), hivMetadata.getARTEncounterPageEncounterType(), RangeComparator.LESS_EQUAL, 250.0, BaseObsCohortDefinition.TimeModifier.LAST);


        for (int i = 0; i <= 6; i++) {

            if (i == 0) {
                olderThan = "6m";
            } else {
                olderThan = i + "y";
            }

            CohortDefinitionDimension pregnancyDimension = commonDimensionLibrary.get106bEMTCTGroup(olderThan);
            dsd.addDimension("cohort" + String.valueOf(i), Mapped.mapStraightThrough(pregnancyDimension));


            CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod(olderThan);
            CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod(olderThan + "-1d");

            PatientDataDefinition baseCD4 = hivPatientDataLibrary.getBaselineCD4(olderThan);

            PatientDataDefinition latestCD4 = hivPatientDataLibrary.getRecentCD4(olderThan);


            CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod(olderThan);
            CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod(olderThan + "-1d");

            CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(olderThan);

            CohortDefinition patientsWithBaseCD4 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), olderThan, BaseObsCohortDefinition.TimeModifier.FIRST);
            CohortDefinition patientsHavingBaseCD4LessThan250 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.LESS_EQUAL, 250.0, olderThan, BaseObsCohortDefinition.TimeModifier.FIRST);


            CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingBaseRegimenBeforeQuarter);
            CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingBaseRegimenDuringQuarter);

            CohortDefinition startedArtDuringQuarter = df.getPatientsInAny(havingArtStartDateDuringQuarter, df.getPatientsNotIn(beenOnArtDuringQuarter, beenOnArtBeforeQuarter));

            CohortDefinition startedArtInFacility = df.getPatientsNotIn(startedArtDuringQuarter, patientsTransferredIn);

            CohortDefinition startedArtInFacilityWithBaseCD4 = df.getPatientsInAll(patientsWithBaseCD4, startedArtInFacility);
            CohortDefinition startedArtInFacilityWithBaseCD4LessThan250 = df.getPatientsInAll(patientsHavingBaseCD4LessThan250, startedArtInFacility);

            CohortDefinition startedArtDuringQuarterFromAnotherFacility = df.getPatientsInAll(startedArtDuringQuarter, patientsTransferredIn);

            CohortDefinition transferredOut = df.getPatientsInAll(startedArtInFacility, hivCohortDefinitionLibrary.getTransferredOutBy(olderThan));


            CohortDefinition netCurrentCohort = df.createPatientComposition("(", startedArtInFacility, "OR", startedArtDuringQuarterFromAnotherFacility, ") AND NOT ", transferredOut);

            CohortDefinition netStopped = df.getPatientsInAll(netCurrentCohort, hivCohortDefinitionLibrary.getInterruptedMedically());
            CohortDefinition netDied = df.getPatientsInAll(netCurrentCohort, hivCohortDefinitionLibrary.gePatientsWhoDiedBy(olderThan));
            CohortDefinition netLostToFollowUp = df.getPatientsInAll(netCurrentCohort, df.getLostToFollowUp());
            CohortDefinition netLost = df.getPatientsInAll(netCurrentCohort, df.getLost());

            CohortDefinition netCurrentCohortAlive = df.createPatientComposition(netCurrentCohort, "AND NOT", df.getPatientsInAny(netStopped, netDied, netLostToFollowUp, netLost));

            CohortDefinition netCurrentCohortAliveWithCD4 = df.getPatientsInAll(netCurrentCohortAlive, patientsWithCD4);
            CohortDefinition netCurrentCohortAliveWithCD4LessThan250 = df.getPatientsInAll(netCurrentCohortAlive, patientsHavingCD4LessThan250);

            addCohort(dsd, String.valueOf(i + 1) + "3", "Started ART in this clinic original cohort", startedArtInFacility, String.valueOf(i));
            addBaseCD4FractionCohort(dsd, String.valueOf(i + 1) + "4", "Fraction of clients with base CD4 less than 250", startedArtInFacilityWithBaseCD4LessThan250, startedArtInFacilityWithBaseCD4, String.valueOf(i));
            addCohortMedianIndicator(dsd, String.valueOf(i + 1) + "5", "Median CD4", baseCD4, startedArtInFacility, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "6", "TI", startedArtDuringQuarterFromAnotherFacility, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "7", "TO", transferredOut, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "8", "Net current cohort", netCurrentCohort, String.valueOf(i));

            addCohort(dsd, String.valueOf(i + 1) + "9", "Stopped", netStopped, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "10", "Died", netDied, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "11", "Lost", netLost, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "12", "Lost to follow up", netLostToFollowUp, String.valueOf(i));
            addCohort(dsd, String.valueOf(i + 1) + "13", "Net current cohort alive and on art", netCurrentCohortAlive, String.valueOf(i));
            addBaseCD4FractionCohort(dsd, String.valueOf(i + 1) + "14", "Percentage net current cohort alive and on art", netCurrentCohortAlive, netCurrentCohort, String.valueOf(i));
            addBaseCD4FractionCohort(dsd, String.valueOf(i + 1) + "15", "Fraction of clients 5yrs & above  with CD4", netCurrentCohortAliveWithCD4LessThan250, netCurrentCohortAliveWithCD4, String.valueOf(i));
            addCohortMedianIndicator(dsd, String.valueOf(i + 1) + "16", "Median CD4", latestCD4, netCurrentCohortAliveWithCD4, String.valueOf(i));

        }
        return rd;
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    public void addIndicatorPercentage(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, CohortDefinition denominator, String dimensions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.FRACTION);
        ci.setDenominator(Mapped.mapStraightThrough(denominator));
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensions);
    }

    public void addMedianIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, PatientDataDefinition data, CohortDefinition cohortDefinition, String cohort) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setAggregator(MedianAggregator.class);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        ci.setType(CohortIndicator.IndicatorType.LOGIC);
        ci.setDataToAggregate(Mapped.mapStraightThrough(data));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), cohort);
    }

    public void addCohort(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String cohort) {
        addIndicator(dsd, key, label + " all", cohortDefinition, "");
        addIndicator(dsd, key + "f", label + " pregnant", cohortDefinition, "cohort" + cohort + "=pregnant");
    }

    public void addBaseCD4FractionCohort(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, CohortDefinition denominator, String cohort) {
        addIndicatorPercentage(dsd, key, label + " all", cohortDefinition, denominator, "");
        addIndicatorPercentage(dsd, key + "f", label + " pregnant", cohortDefinition, denominator, "cohort" + cohort + "=pregnant");
    }

    public void addCohortMedianIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, PatientDataDefinition data, CohortDefinition cohortDefinition, String cohort) {
        addMedianIndicator(dsd, key, label + " all", data, cohortDefinition, "");
        addMedianIndicator(dsd, key + "f", label + " pregnant", data, cohortDefinition, "cohort" + cohort + "=pregnant");
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
