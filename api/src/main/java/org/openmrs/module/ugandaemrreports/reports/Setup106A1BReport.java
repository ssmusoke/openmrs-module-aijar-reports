package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.aggregation.MedianAggregator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
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

public class Setup106A1BReport extends UgandaEMRDataExportManager {
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
        return "b98ab976-9c9d-4a28-9760-ac3119c0ef23";
    }

    @Override
    public String getUuid() {
        return "167cf668-071e-488b-b159-d5f391774038";
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

        CohortDefinition transferIn = df.getPatientsWhoseObs(hivMetadata.getArtRegimenTransferInDate(), hivMetadata.getARTSummaryPageEncounterType());
        CohortDefinition transferOut = hivCohortDefinitionLibrary.getTransferredOut();

        CohortDefinitionDimension pregnancyDimension = commonDimensionLibrary.get106bEMTCTGroup();
        dsd.addDimension("cohort", Mapped.mapStraightThrough(pregnancyDimension));


        int numberOfQuarters = 2;
        for (int i = 0; i <= 6; i++) {
            if (i > 0) {
                numberOfQuarters = i * 4;
            }

            PatientDataDefinition cd4 = hivPatientDataLibrary.getBaseCD4OnArtDuringQuarter(numberOfQuarters);

            CohortDefinition onArt = hivCohortDefinitionLibrary.getPatientsWhoStartedArtQuartersAgo(numberOfQuarters);
            CohortDefinition baseCD4LE250 = hivCohortDefinitionLibrary.getPatientsOnArtWithCD4QuartersAgo(numberOfQuarters, Boolean.FALSE);
            CohortDefinition allBaseCD4 = hivCohortDefinitionLibrary.getPatientsOnArtWithCD4QuartersAgo(numberOfQuarters, Boolean.TRUE);

            CohortDefinition cD4LE250 = hivCohortDefinitionLibrary.getPatientsOnArtWithCD4BeforeQuartersAgo(numberOfQuarters, Boolean.FALSE);
            CohortDefinition allCD4 = hivCohortDefinitionLibrary.getPatientsOnArtWithCD4BeforeQuartersAgo(numberOfQuarters, Boolean.TRUE);
            CohortDefinition deadPatients = hivCohortDefinitionLibrary.getDeadPatientsOnArtQuartersAgo(numberOfQuarters);
            CohortDefinition stopped = hivCohortDefinitionLibrary.getStoppedPatientsOnSArtQuartersAgo(numberOfQuarters);
            CohortDefinition missed = hivCohortDefinitionLibrary.getPatientsOnArtWhoMissedQuartersAgo(numberOfQuarters);
            CohortDefinition lost = hivCohortDefinitionLibrary.getLostPatientsOnArtQuartersAgo(numberOfQuarters);

            CohortDefinition startedArtInFacility = df.getPatientsNotIn(onArt, transferIn);

            CohortDefinition startedArtDuringQuarterFromAnotherFacility = df.getPatientsInAll(onArt, transferIn);
            CohortDefinition transferredOut = df.getPatientsInAll(onArt, transferOut);

            CohortDefinition netCurrentCohort = df.createPatientComposition("(", startedArtInFacility, "OR", startedArtDuringQuarterFromAnotherFacility, ") AND NOT ", transferredOut);

            CohortDefinition netStopped = df.getPatientsInAll(netCurrentCohort, stopped);
            CohortDefinition netDied = df.getPatientsInAll(netCurrentCohort, deadPatients);
            CohortDefinition netLost = df.getPatientsInAll(netCurrentCohort, missed);
            CohortDefinition netLostToFollowUp = df.getPatientsInAll(netCurrentCohort, lost);

            CohortDefinition netCurrentCohortAlive = df.createPatientComposition(netCurrentCohort, "AND NOT", df.getPatientsInAny(netStopped, netDied, netLostToFollowUp, netLost));

            addCohort(dsd, String.valueOf(i + 1) + "3", "Started ART in this clinic original cohort", startedArtInFacility);
            addCohort(dsd, String.valueOf(i + 1) + "4a", "Clients with base CD4 less than 250", baseCD4LE250);
            addCohort(dsd, String.valueOf(i + 1) + "4b", "Clients with base CD4", allBaseCD4);
            addCohortMedianIndicator(dsd, String.valueOf(i + 1) + "5", "Median CD4", cd4, startedArtInFacility);
            addCohort(dsd, String.valueOf(i + 1) + "6", "TI", startedArtDuringQuarterFromAnotherFacility);
            addCohort(dsd, String.valueOf(i + 1) + "7", "TO", transferredOut);
            addCohort(dsd, String.valueOf(i + 1) + "8", "Net current cohort", netCurrentCohort);

            addCohort(dsd, String.valueOf(i + 1) + "9", "Stopped", netStopped);
            addCohort(dsd, String.valueOf(i + 1) + "10", "Died", netDied);
            addCohort(dsd, String.valueOf(i + 1) + "11", "Lost", netLost);
            addCohort(dsd, String.valueOf(i + 1) + "12", "Lost to follow up", netLostToFollowUp);
            addCohort(dsd, String.valueOf(i + 1) + "13", "Net current cohort alive and on art", netCurrentCohortAlive);
            addCohort(dsd, String.valueOf(i + 1) + "15a", "Fraction of clients 5yrs & above  with CD4 less than 250", cD4LE250);
            addCohort(dsd, String.valueOf(i + 1) + "15b", "Fraction of clients 5yrs & above  with CD4", allCD4);

            //addCohortMedianIndicator(dsd, String.valueOf(i + 1) + "16", "Median CD4", latestCD4, netCurrentCohortAliveWithCD4);
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

    public void addCohort(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key, label + " all", cohortDefinition, "");
        addIndicator(dsd, key + "f", label + " pregnant", cohortDefinition, "cohort=pregnant");
    }

    public void addBaseCD4FractionCohort(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, CohortDefinition denominator, String cohort) {
        addIndicatorPercentage(dsd, key, label + " all", cohortDefinition, denominator, "");
        addIndicatorPercentage(dsd, key + "f", label + " pregnant", cohortDefinition, denominator, "cohort" + cohort + "=pregnant");
    }

    public void addCohortMedianIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, PatientDataDefinition data, CohortDefinition cohortDefinition) {
        addMedianIndicator(dsd, key, label + " all", data, cohortDefinition, "");
        addMedianIndicator(dsd, key + "f", label + " pregnant", data, cohortDefinition, "cohort=pregnant");
    }

    @Override
    public String getVersion() {
        return "0.42";
    }
}
