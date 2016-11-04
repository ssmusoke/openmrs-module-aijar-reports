package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
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

public class SetupCBSAdultReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;


    @Override
    public String getExcelDesignUuid() {
        return "d48d51de-0f00-425d-bfa1-1f5a8f6c9f2a";
    }

    @Override
    public String getUuid() {
        return "1479f8ae-5d93-4b2f-9cd6-8b7eb976dc6b";
    }

    @Override
    public String getName() {
        return "CBS Adult Quarterly Report";
    }

    @Override
    public String getDescription() {
        return "CBS Adult Quarterly Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CBSAdultQuarterlyReport.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        String olderThan;

        String[] labels = {"i", "j", "k", "l", "m", "n", "o"};


        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getCBSAdultReportAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition dead = df.getDeadPatientsDuringPeriod();

        CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();
        CohortDefinition regimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition eligibleByEndOfQuarter = hivCohortDefinitionLibrary.getEligibleAndReadyByEndOfQuarter();
        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();
        CohortDefinition transferredOutTheQuarter = hivCohortDefinitionLibrary.getTransferredOutDuringPeriod();
        CohortDefinition pregnantAtFirstEncounter = hivCohortDefinitionLibrary.getPatientsPregnantAtFirstEncounter();

        CohortDefinition enrolledDuringTheQuarter = df.getPatientsNotIn(havingArtStartDateDuringQuarter, transferredInTheQuarter);
        CohortDefinition netCurrentCohort = df.getPatientsNotIn(havingArtStartDateDuringQuarter, transferredOutTheQuarter);
        CohortDefinition eligibleThisQuarter = df.getPatientsInAll(eligibleByEndOfQuarter, enrolledInTheQuarter);

        CohortDefinition clinicalStage = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getDateEligibilityWHOStage(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition clinicalStage1 = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getDateEligibilityWHOStage(), hivMetadata.getARTSummaryPageEncounterType(), hivMetadata.getBaselineClinicalStages12(), BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition clinicalStage2 = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getDateEligibilityWHOStage(), hivMetadata.getARTSummaryPageEncounterType(), hivMetadata.getBaselineClinicalStages3(), BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition clinicalStage3 = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getDateEligibilityWHOStage(), hivMetadata.getARTSummaryPageEncounterType(), hivMetadata.getBaselineClinicalStages4(), BaseObsCohortDefinition.TimeModifier.FIRST);

        CohortDefinition all = df.getPatientsInAll(enrolledInTheQuarter, clinicalStage);
        CohortDefinition hiv = df.getPatientsInAll(enrolledInTheQuarter, clinicalStage1);
        CohortDefinition advanced = df.getPatientsInAll(enrolledInTheQuarter, clinicalStage2);
        CohortDefinition aids = df.getPatientsInAll(enrolledInTheQuarter, clinicalStage3);

        CohortDefinition baseCD4 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition baseCD4L50 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.LESS_THAN, 50.0, BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition baseCD4G50L200 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.GREATER_EQUAL, 50.0, RangeComparator.LESS_THAN, 200.0, BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition baseCD4G200L350 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.GREATER_EQUAL, 200.0, RangeComparator.LESS_THAN, 350.0, BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition baseCD4G350L500 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.GREATER_EQUAL, 350.0, RangeComparator.LESS_THAN, 500.0, BaseObsCohortDefinition.TimeModifier.FIRST);
        CohortDefinition baseCD4G500 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.GREATER_EQUAL, 500.0, BaseObsCohortDefinition.TimeModifier.FIRST);

        CohortDefinition childrenOnSecondLineDuringQuarter = df.getPatientsInAll(cohortDefinitionLibrary.between0And10years(), hivCohortDefinitionLibrary.getChildrenOnSecondLineRegimenDuringPeriod());

        CohortDefinition adultsOnSecondLineDuringQuarter = df.getPatientsInAll(cohortDefinitionLibrary.above10years(), hivCohortDefinitionLibrary.getAdultsOnSecondLineRegimenDuringPeriod());

        CohortDefinition secondLineDuringQuarter = df.getPatientsInAny(childrenOnSecondLineDuringQuarter, adultsOnSecondLineDuringQuarter);
        CohortDefinition onThirdLineRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsOnThirdLineRegimenDuringPeriod();

        CohortDefinition viralLoadG1000 = df.getPatientsInAll(df.getViralLoadDuringPeriod(1000.0), regimenDuringQuarter);

        CohortDefinition failingFirstSecondLine = df.getPatientsInAll(secondLineDuringQuarter, viralLoadG1000);
        CohortDefinition failingThirdSecondLine = df.getPatientsInAll(onThirdLineRegimenDuringQuarter, viralLoadG1000);

        addAgeGender(dsd, "a", "All HIV", all);
        addAgeGender(dsd, "a1", "HIV", hiv);
        addAgeGender(dsd, "a2", "Advanced", advanced);
        addAgeGender(dsd, "a3", "AIDS", aids);

        addAgeGender(dsd, "b", "Number enrolled in care", enrolledInTheQuarter);
        addAgeGender(dsd, "c", "Tested for CD4 < 50", baseCD4);
        addAgeGender(dsd, "c1", "Tested for CD4 < 50", baseCD4L50);
        addAgeGender(dsd, "c2", "Tested for CD4 > 50 < 200", baseCD4G50L200);
        addAgeGender(dsd, "c3", "Tested for CD4 > 200 < 350", baseCD4G200L350);
        addAgeGender(dsd, "c4", "Tested for CD4 > 350 < 500", baseCD4G350L500);
        addAgeGender(dsd, "c5", "Tested for CD4 > 500", baseCD4G500);
        addAgeGender(dsd, "d", "Eligible", eligibleThisQuarter);
        addAgeGender(dsd, "e", "Started this quarter", enrolledDuringTheQuarter);
        addAgeGender(dsd, "f", "Transfer in", transferredInTheQuarter);
        addAgeGender(dsd, "g", "Transfer out", transferredOutTheQuarter);
        addAgeGender(dsd, "h", "Net Current Cohort", netCurrentCohort);
        addAgeGender(dsd, "p", "Number alive and on treatment", regimenDuringQuarter);
        addAgeGender(dsd, "q", "On second line", secondLineDuringQuarter);
        addAgeGender(dsd, "r", "Failing on second line", failingFirstSecondLine);
        addAgeGender(dsd, "s", "On third line", onThirdLineRegimenDuringQuarter);
        addAgeGender(dsd, "t", "Failing on third line", failingThirdSecondLine);
        addAgeGender(dsd, "u", "Number lost to followup", df.getLostToFollowUp());
        addAgeGender(dsd, "v", "Dead", dead);


        for (int i = 0; i <= 6; i++) {

            if (i == 0) {
                olderThan = "6m";
            } else {
                olderThan = i + "y";
            }

            CohortDefinition startedArtMonthsAgo = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(olderThan);

            CohortDefinition viralLoadDuringQuarter = df.getPatientsInAll(startedArtMonthsAgo, df.getViralLoadDuringPeriod());
            CohortDefinition viralLoadDuringQuarterL1000 = df.getPatientsInAll(startedArtMonthsAgo, df.getViralLoadDuringPeriod(Boolean.TRUE));
            CohortDefinition viralLoadDuringQuarterG1000L5000 = df.getPatientsInAll(startedArtMonthsAgo, df.getViralLoadDuringPeriod(1000.0, 5000.0));
            CohortDefinition viralLoadDuringQuarterG10000 = df.getPatientsInAll(startedArtMonthsAgo, df.getViralLoadDuringPeriod(10000.0));

            addAgeGender(dsd, labels[i], "Number with VL", viralLoadDuringQuarter);
            addAgeGender(dsd, labels[i] + "1", "Number with VL", viralLoadDuringQuarterL1000);
            addAgeGender(dsd, labels[i] + "2", "Number with VL", viralLoadDuringQuarterG1000L5000);
            addAgeGender(dsd, labels[i] + "3", "Number with VL", viralLoadDuringQuarterG10000);
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

    public void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "1", label + " (Below 5 Males)", cohortDefinition, "age=below5male|age=below5female");
        addIndicator(dsd, key + "2", label + " (Below 5 Females)", cohortDefinition, "age=below5female");
        addIndicator(dsd, key + "3", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "4", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "5", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "6", label + " (Above 15 Females)", cohortDefinition, "age=above15female");
        addIndicator(dsd, key + "7", label + " Total", cohortDefinition, "");
    }

    @Override
    public String getVersion() {
        return "0.27";
    }
}
