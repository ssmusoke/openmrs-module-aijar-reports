package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 */
@Component
public class Setup106A1AReport extends UgandaEMRDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;

    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119c8ef34";
    }

    @Override
    public String getUuid() {
        return "167cf668-0715-488b-b159-d5f391774091";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1A";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1AReport.xls");
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
        rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.get106aAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition enrolledBeforeQuarter = hivCohortDefinitionLibrary.getEnrolledInCareByEndOfPreviousDate();
        CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();
        
        CohortDefinition activeWithNoEncounterInQuarter = hivCohortDefinitionLibrary.getActiveWithNoEncounterInQuarter();

        CohortDefinition hadEncounterInQuarter = hivCohortDefinitionLibrary.getArtPatientsWithEncounterOrSummaryPagesBetweenDates();

        CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();
        CohortDefinition transferredInBeforeQuarter = hivCohortDefinitionLibrary.getTransferredInToCareBeforePeriod();

        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();

        CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod();
        CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod();

        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();

        CohortDefinition enrolledWhenPregnantOrLactating = hivCohortDefinitionLibrary.getEnrolledInCareToCareWhenPregnantOrLactating();

        CohortDefinition pregnantAtFirstEncounter = hivCohortDefinitionLibrary.getPatientsPregnantAtFirstEncounter();

        CohortDefinition onINHDuringQuarter = hivCohortDefinitionLibrary.getOnINHDuringPeriod();
        CohortDefinition onINHBeforeQuarter = hivCohortDefinitionLibrary.getOnINHDuringBeforePeriod();

        CohortDefinition onCPTDuringQuarter = hivCohortDefinitionLibrary.getOnCPTDuringPeriod();

        CohortDefinition assessedForTBDuringQuarter = hivCohortDefinitionLibrary.getAccessedForTBDuringPeriod();

        CohortDefinition diagnosedWithTBDuringQuarter = hivCohortDefinitionLibrary.getDiagnosedWithTBDuringPeriod();

        CohortDefinition onTBRxDuringQuarter = hivCohortDefinitionLibrary.getStartedTBRxDuringPeriod();
        CohortDefinition onTBRxBeforeQuarter = hivCohortDefinitionLibrary.getStartedTBRxBeforePeriod();

        CohortDefinition oedemaWasTakenDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoseOedemaWasTakenDuringPeriod();

        CohortDefinition mUACWasTakenDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoMUACWasTakenDuringPeriod();

        CohortDefinition assessedForMalnutritionDuringQuarter = hivCohortDefinitionLibrary.getPatientsAssessedForMalnutrition();

        CohortDefinition heightWasTakenDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoHeightWasTakenDuringPeriod();

        CohortDefinition weightWasTakenDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoWeightWasTakenDuringPeriod();

        CohortDefinition baseWeightWasTakenDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoBaseWeightWasTakenDuringPeriod();

        CohortDefinition mUACWasYellowOrRedDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoMUACWasRedOrYellowDuringPeriod();

        CohortDefinition malnourishedDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoAreMalnourishedDuringPeriod();

        CohortDefinition oedemaWasYesDuringQuarter = hivCohortDefinitionLibrary.getPatientsWhoseOedemaWasYesDuringPeriod();

        CohortDefinition onArtBasedOnCD4 = hivCohortDefinitionLibrary.getPatientsStartedArtBasedOnCD4();

        CohortDefinition eligibleByEndOfQuarter = hivCohortDefinitionLibrary.getEligibleAndReadyByEndOfQuarter();

        CohortDefinition childrenOnFirstLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(), hivCohortDefinitionLibrary.getChildrenOnFirstLineRegimenDuringPeriod());
        CohortDefinition childrenOnSecondLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(), hivCohortDefinitionLibrary.getChildrenOnSecondLineRegimenDuringPeriod());

        CohortDefinition adultsOnFirstLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(), hivCohortDefinitionLibrary.getAdultsOnFirstLineRegimenDuringPeriod());
        CohortDefinition adultsOnSecondLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(), hivCohortDefinitionLibrary.getAdultsOnSecondLineRegimenDuringPeriod());

        CohortDefinition onThirdLineRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsOnThirdLineRegimenDuringPeriod();

        CohortDefinition patientsWithGoodAdherenceDuringQuarter = hivCohortDefinitionLibrary.getPatientsWithGoodAdherence();

        CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);
        CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter, havingBaseRegimenDuringQuarter, activeWithNoEncounterInQuarter);

        CohortDefinition everEnrolledByEndQuarter = df.getPatientsNotIn(enrolledBeforeQuarter, transferredInBeforeQuarter);
        CohortDefinition enrolledDuringTheQuarter = df.getPatientsNotIn(enrolledInTheQuarter, transferredInTheQuarter);
        
        CohortDefinition startedINHDuringQuarter = df.getPatientsNotIn(onINHDuringQuarter, onINHBeforeQuarter);

        CohortDefinition cumulativeEverEnrolled = df.getPatientsInAny(everEnrolledByEndQuarter, enrolledDuringTheQuarter);

        CohortDefinition onPreArt = df.getPatientsNotIn(df.getPatientsInAny(hadEncounterInQuarter, activeWithNoEncounterInQuarter), df.getPatientsInAny(beenOnArtBeforeQuarter, beenOnArtDuringQuarter));

        CohortDefinition onPreArtWhoReceivedCPT = df.getPatientsInAll(onPreArt, onCPTDuringQuarter);

        CohortDefinition onPreArtAssessedForTB = df.getPatientsInAll(onPreArt, assessedForTBDuringQuarter);

        CohortDefinition onPreArtDiagnosedWithTB = df.getPatientsInAll(onPreArt, diagnosedWithTBDuringQuarter);

        CohortDefinition startedTBDuringQuarter = df.getPatientsNotIn(onTBRxDuringQuarter, onTBRxBeforeQuarter);

        CohortDefinition onPreArtStartedTBRx = df.getPatientsInAll(onPreArt, startedTBDuringQuarter);

        CohortDefinition assessedForMalnutrition = df.getPatientsInAny(oedemaWasTakenDuringQuarter, mUACWasTakenDuringQuarter, assessedForMalnutritionDuringQuarter, heightWasTakenDuringQuarter, weightWasTakenDuringQuarter, baseWeightWasTakenDuringQuarter);

        CohortDefinition onPreArtAssessedForMalnutrition = df.getPatientsInAll(onPreArt, assessedForMalnutrition);

        CohortDefinition whoAreMalnourished = df.getPatientsInAny(mUACWasYellowOrRedDuringQuarter, malnourishedDuringQuarter, oedemaWasYesDuringQuarter);
        CohortDefinition onPreArtWhoAreMalnourished = df.getPatientsInAll(onPreArt, whoAreMalnourished);

        CohortDefinition startedArtDuringQuarter = df.getPatientsNotIn(beenOnArtDuringQuarter, beenOnArtBeforeQuarter);

        CohortDefinition startedBasedOnCD4 = df.getPatientsInAll(startedArtDuringQuarter, onArtBasedOnCD4);

        CohortDefinition cumulativeOnArt = df.getPatientsInAny(beenOnArtBeforeQuarter, beenOnArtDuringQuarter);

        CohortDefinition onFirstLineRegimen = df.getPatientsInAll(beenOnArtDuringQuarter, df.getPatientsInAny(childrenOnFirstLineDuringQuarter, adultsOnFirstLineDuringQuarter));
        CohortDefinition onSecondLineRegimen = df.getPatientsInAll(beenOnArtDuringQuarter, df.getPatientsInAny(childrenOnSecondLineDuringQuarter, adultsOnSecondLineDuringQuarter));

        CohortDefinition activeOnArtOnCPT = df.getPatientsInAll(beenOnArtDuringQuarter, onCPTDuringQuarter);
        CohortDefinition activeOnArtAssessedForTB = df.getPatientsInAll(beenOnArtDuringQuarter, assessedForTBDuringQuarter);
        CohortDefinition activeOnArtDiagnosedWithTB = df.getPatientsInAll(beenOnArtDuringQuarter, diagnosedWithTBDuringQuarter);
        CohortDefinition activeOnArtStartedTBRx = df.getPatientsInAll(beenOnArtDuringQuarter, startedTBDuringQuarter);
        CohortDefinition activeOnArtOnTBRx = df.getPatientsInAll(beenOnArtDuringQuarter, onTBRxDuringQuarter);
        CohortDefinition activeOnArtWithGoodAdherence = df.getPatientsInAll(beenOnArtDuringQuarter, patientsWithGoodAdherenceDuringQuarter);

        CohortDefinition activeOnArtAssessedForMalnutrition = df.getPatientsInAll(beenOnArtDuringQuarter, assessedForMalnutrition);

        CohortDefinition activeOnArtWhoAreMalnourished = df.getPatientsInAll(beenOnArtDuringQuarter, whoAreMalnourished);

        CohortDefinition eligibleButNotStartedByQuarter = df.getPatientsNotIn(eligibleByEndOfQuarter, cumulativeOnArt);

        CohortDefinition startedArtWhenPregnant = df.getPatientsInAll(pregnantAtFirstEncounter, startedArtDuringQuarter);

        addAgeGender(dsd, "1", "Patients ever enrolled in care by the end of the previous quarter", everEnrolledByEndQuarter);
        addAgeGender(dsd, "2", "New patients enrolled during quarter", enrolledDuringTheQuarter);
        addAgeGenderFemale(dsd, "3", "Pregnant and lactating enrolled in care ", enrolledWhenPregnantOrLactating);
        addIndicator(dsd, "4i", "INH During Quarter", startedINHDuringQuarter, "");
        addAgeGender(dsd, "5", "All who have ever enrolled up to quarter", cumulativeEverEnrolled);
        addIndicator(dsd, "6i", "Transfer In", transferredInTheQuarter, "");
        addAge(dsd, "7", "On Pre-ART", onPreArt);
        addAge(dsd, "8", "On Pre-ART CPT", onPreArtWhoReceivedCPT);
        addIndicator(dsd, "9i", "On Pre-ART Assessed for TB", onPreArtAssessedForTB, "");
        addIndicator(dsd, "10i", " Pre-ART Diagnosed with TB", onPreArtDiagnosedWithTB, "");
        addIndicator(dsd, "11i", "On Pre-ART started TB Rx", onPreArtStartedTBRx, "");
        addIndicator(dsd, "12i", "On Pre-ART Assessed for Malnutrition", onPreArtAssessedForMalnutrition, "");
        addIndicator(dsd, "13i", "On Pre-ART those who are Malnourished", onPreArtWhoAreMalnourished, "");
        addIndicator(dsd, "14i", "Eligible but not started on art", eligibleButNotStartedByQuarter, "");
        addAgeGender(dsd, "15", "Patients ever enrolled in art by the end of the previous quarter", beenOnArtBeforeQuarter);
        addAgeGender(dsd, "16", "Started Art during the quarter", startedArtDuringQuarter);
        addIndicator(dsd, "17i", "Started Art based on CD4", startedBasedOnCD4, "");
        addAgeGenderFemale(dsd, "18", "Started ART when pregnant ", startedArtWhenPregnant);
        addAgeGender(dsd, "19", "Ever enrolled", cumulativeOnArt);
        addAgeGender(dsd, "20", "First Line Regimen", onFirstLineRegimen);
        addAgeGender(dsd, "21", "Second Line Regimen", onSecondLineRegimen);
        addAgeGender(dsd, "22", "Third Line Regimen", onThirdLineRegimenDuringQuarter);
        addAgeGender(dsd, "23", "On Art Received CPT", activeOnArtOnCPT);

        addIndicator(dsd, "24i", "On Art assessed for TB", activeOnArtAssessedForTB, "");
        addIndicator(dsd, "25i", "On Art diagnosed with TB", activeOnArtDiagnosedWithTB, "");
        addIndicator(dsd, "26i", "On Art started TB Rx this quarter", activeOnArtStartedTBRx, "");
        addIndicator(dsd, "27i", "On Art and TB treatment", activeOnArtOnTBRx, "");
        addIndicator(dsd, "28i", "On Art good adherence", activeOnArtWithGoodAdherence, "");
        addIndicator(dsd, "29i", "On Art assessed for malnutrition", activeOnArtAssessedForMalnutrition, "");
        addIndicator(dsd, "30i", "On Art malnourished", activeOnArtWhoAreMalnourished, "");


        return rd;
    }

    public void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "a", label + " (Below 2 Males)", cohortDefinition, "age=below2male");
        addIndicator(dsd, key + "b", label + " (Below 2 Females)", cohortDefinition, "age=below2female");
        addIndicator(dsd, key + "c", label + " (Between 2 and 5 Males)", cohortDefinition, "age=between2and5male");
        addIndicator(dsd, key + "d", label + " (Between 2 and 5 Females)", cohortDefinition, "age=between2and5female");
        addIndicator(dsd, key + "e", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "f", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "g", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "h", label + " (Above 15 Females)", cohortDefinition, "age=above15female");
        addIndicator(dsd, key + "i", label + " Total", cohortDefinition, "");
    }

    public void addAgeGenderFemale(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "f", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "h", label + " (Above 15 Females)", cohortDefinition, "age=above15female");
        addIndicator(dsd, key + "i", label + " Total", cohortDefinition, "");
    }

    public void addAge(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "a", label + " (Between 0 and 15 years)", cohortDefinition, "age=child");
        addIndicator(dsd, key + "b", label + " (Above 15 years)", cohortDefinition, "age=adult");
        addIndicator(dsd, key + "i", label + " Total", cohortDefinition, "");
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    @Override
    public String getVersion() {
        return "0.1.2";
    }
}
