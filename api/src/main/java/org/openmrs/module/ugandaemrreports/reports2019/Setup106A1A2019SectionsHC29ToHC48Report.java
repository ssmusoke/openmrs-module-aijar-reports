package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MedianBaselineCD4DatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 */
@Component
public class Setup106A1A2019SectionsHC29ToHC48Report extends UgandaEMRDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public String getExcelDesignUuid() {
        return "9ac8b336-292a-4714-90e1-584f4074ea1e";
    }

    @Override
    public String getUuid() {
        return "a2d6170e-1930-4f21-920c-6106e39058e8";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A 2019 Sections HC29 To HC48";
    }

    @Override
    public String getDescription() {
        return "This is the 2019 version of the HMIS 106A Section 1A Sections HC29 To HC48 ";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1A2019HC29ToHC48Report.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        rd.addDataSetDefinition("m",Mapped.mapStraightThrough(getMedianCD4AtARTInitiation()));
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("x", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension finerAgeDisaggregations = commonDimensionLibrary.getFinerAgeDisaggregations();
        dsd.addDimension("age", Mapped.mapStraightThrough(finerAgeDisaggregations));

        CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();

        CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();


        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition clientsStartedOnARTAtThisFacilityDuringPeriod = df.getPatientsNotIn(havingArtStartDateDuringQuarter,transferredInTheQuarter);

        CohortDefinition onCPTDuringQuarter = hivCohortDefinitionLibrary.getOnCPTDuringPeriod();
        CohortDefinition eligibleForCPTDuringQuarter = hivCohortDefinitionLibrary.getEligibleOnCPTDuringPeriod();

        CohortDefinition assessedForTBDuringQuarter = hivCohortDefinitionLibrary.getAssessedForTBDuringPeriod();
        CohortDefinition presumptiveTBDuringQuarter = hivCohortDefinitionLibrary.getPresumptiveTBDuringPeriod();

        CohortDefinition diagnosedWithTBDuringQuarter = hivCohortDefinitionLibrary.getDiagnosedWithTBDuringPeriod();

        CohortDefinition onTBRxDuringQuarter = hivCohortDefinitionLibrary.getPatientsOnTBRxDuringPeriod();
        CohortDefinition onTBRxBeforeQuarter = hivCohortDefinitionLibrary.getStartedTBRxBeforePeriod();

        CohortDefinition patientsAssessedForNutritionDuringQuarter = hivCohortDefinitionLibrary.getPatientsAssessedForNutrition();
        CohortDefinition patientMalnourishedDuringPeriod = df.getPatientsInAny(hivCohortDefinitionLibrary.getPatientsWithModeratelyAcuteMalnutritionDuringPeriod(),
                hivCohortDefinitionLibrary.getPatientsWithSevereAcuteMalnutritionDuringPeriod(),hivCohortDefinitionLibrary.getPatientsWithSevereAcuteMalnutritionWithOedemaDuringPeriod());
        CohortDefinition patientsWhoReceivedTherapeuticFoods = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("8531d1a7-9793-4c62-adab-f6716cf9fabb"),hivMetadata.getARTEncounterPageEncounterType(),
                Arrays.asList(hivMetadata.getConcept("598dba00-b878-474c-9a10-9998f1748228"),hivMetadata.getConcept("76d127ee-7e5b-467a-b1d4-eab8ebcf2c37")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition onThirdLineRegimenDuringQuarter = df.getWorkFlowStateCohortDefinition(hivMetadata.getThirdLineRegimenState());
        CohortDefinition activePatientsInCareDuringPeriod = hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("90");

        CohortDefinition patientsWhoHadAViralLoadTest6MonthsAfterArtInitiation = addStartDateAndEndDateParameters(hivCohortDefinitionLibrary.getPatientsWhoHadAViralLoadTestPeriodAfterArtInitiationByEndDate("6"));
        CohortDefinition patientsWhoHadAViralLoadTest12MonthsAfterArtInitiation = addStartDateAndEndDateParameters( hivCohortDefinitionLibrary.getPatientsWhoHadAViralLoadTestPeriodAfterArtInitiationByEndDate("12"));
        CohortDefinition patientsWhoHadAViralLoadTest6MonthsAfterArtInitiationAndAreVirallySupressed = addStartDateAndEndDateParameters( hivCohortDefinitionLibrary.getPatientsWhoHadAViralLoadTestPeriodAfterArtInitiationAndAreSuppressedByEndDate("6"));
        CohortDefinition patientsWhoHadAViralLoadTest12MonthsAfterArtInitiationAndAreVirallySupressed = addStartDateAndEndDateParameters( hivCohortDefinitionLibrary.getPatientsWhoHadAViralLoadTestPeriodAfterArtInitiationAndAreSuppressedByEndDate("12"));

        CohortDefinition patientsWhoSwitchedFrom1stLineTo2nd = addStartDateAndEndDateParameters(Cohorts.getPatientsWhoSwitchedFromFirstLineToSecondLineAsOnARTSummaryDuringPeriod());
        CohortDefinition patientsWhoSwitchedFrom2ndLineTo3rd = addStartDateAndEndDateParameters(Cohorts.getPatientsWhoSwitchedFromSecondLineToThirdLineAsOnARTSummaryDuringPeriod());

        CohortDefinition startedTBDuringQuarter = df.getPatientsNotIn(onTBRxDuringQuarter, onTBRxBeforeQuarter);


        CohortDefinition onFirstLineRegimen = df.getPatientsInAll(activePatientsInCareDuringPeriod, df.getWorkFlowStateCohortDefinition(hivMetadata.getFirstLineRegimenState()));
        CohortDefinition onSecondLineRegimen = df.getPatientsInAll(activePatientsInCareDuringPeriod, df.getWorkFlowStateCohortDefinition(hivMetadata.getSecondLineRegimenState()));


        CohortDefinition accessedTBLAM = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.TB_LAM_RESULTS),hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition accessedCRAG = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CRAG_RESULTS),hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition startedTPTDuringQuarter = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getConcept("483939c7-79ba-4ca4-8c3e-346488c97fc7"),hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition startedTPTSixMonthsAgo = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getConcept("483939c7-79ba-4ca4-8c3e-346488c97fc7"),hivMetadata.getARTSummaryPageEncounterType(),"6m", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition completedTPTDuringPeriod = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getConcept("813e21e7-4ccb-4fe9-aaab-3c0e40b6e356"),hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition positiveForTBLAM = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.TB_LAM_RESULTS),hivMetadata.getARTEncounterPageEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.POSITIVE)), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition positiveForCRAG = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CRAG_RESULTS),hivMetadata.getARTEncounterPageEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.POSITIVE)), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition patientsPositiveForTBLAMAndTreatedForTB= df.getPatientsInAll(positiveForTBLAM,onTBRxDuringQuarter);
        CohortDefinition activePatientsOnTBTreatment =df.getPatientsInAll(onTBRxDuringQuarter,activePatientsInCareDuringPeriod);
        CohortDefinition activeOnArtStartedTBRxDuringPeriod = df.getPatientsInAll( activePatientsOnTBTreatment, startedTBDuringQuarter);
        CohortDefinition activeOnArtPreviouslyOnTBRx = df.getPatientsInAll(activePatientsOnTBTreatment,onTBRxBeforeQuarter);
        CohortDefinition onFluconazoleTreatment = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("7dea17d4-17eb-43b6-8029-785aeb6aa453"), hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.GREATER_THAN, 0.0, BaseObsCohortDefinition.TimeModifier.ANY);


        CohortDefinition clientsWhoHadViralLoadTakenDuringThePast12Months = hivCohortDefinitionLibrary.getPatientsWhoHadAViralLoadTestDuringThePastPeriodFromEndDate("12m");
        CohortDefinition clientsWhoHadViralLoadTakenDuringThePast12MonthsAndVirallySupressed =df.getPatientsInAll(clientsWhoHadViralLoadTakenDuringThePast12Months, df.getPatientsWithNumericObsFromPastEndPeriodToEndDate(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),
                RangeComparator.LESS_THAN,1000.0,"12m", BaseObsCohortDefinition.TimeModifier.ANY));
        CohortDefinition nonSuppressedClientsDuringPeriod = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.GREATER_EQUAL,1000.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsNonSuppressedByEndOfLastPeriod = df.getPatientsWithNumericObsByEndOfPreviousDate(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.GREATER_EQUAL,1000.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsNonSuppressedByEndDate = df.getPatientsWithNumericObsByEndDate(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.GREATER_EQUAL,1000.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition cd4DuringPeriod = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.GREATER_EQUAL,0.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsNonSuppressedAndTestedWithCD4 = df.getPatientsInAll(patientsNonSuppressedByEndDate,cd4DuringPeriod);
        CohortDefinition cd4DuringPeriodLessThan200 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getCD4(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.LESS_EQUAL,200.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200 = df.getPatientsInAll(cd4DuringPeriodLessThan200,patientsNonSuppressedAndTestedWithCD4);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200AndAccessedTBLAM = df.getPatientsInAll(patientsNonSuppressedCd4DuringPeriodLessThan200,accessedTBLAM);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForTBLAM = df.getPatientsInAll(patientsNonSuppressedCd4DuringPeriodLessThan200,positiveForTBLAM);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForTBLAMAndTreatedForTB = df.getPatientsInAll(patientsNonSuppressedCd4DuringPeriodLessThan200,patientsPositiveForTBLAMAndTreatedForTB);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200AndAccessedCRAG = df.getPatientsInAll(patientsNonSuppressedCd4DuringPeriodLessThan200,accessedCRAG);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForCRAG = df.getPatientsInAll(patientsNonSuppressedCd4DuringPeriodLessThan200,positiveForCRAG);
        CohortDefinition patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForCRAGTreatedWithFluconazole= df.getPatientsInAll(patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForCRAG,onFluconazoleTreatment);

        CohortDefinition activePatientsScreenedForTB = df.getPatientsInAll(activePatientsInCareDuringPeriod,assessedForTBDuringQuarter);


        CohortDefinition activePatientsPresumptiveTBDuringQuarter = df.getPatientsInAll(activePatientsInCareDuringPeriod,presumptiveTBDuringQuarter);
        CohortDefinition activePatientsDiagnosedWithTBDuringQuarter = df.getPatientsInAll(activePatientsInCareDuringPeriod,diagnosedWithTBDuringQuarter);
        CohortDefinition startedTPTDuringQuarterAndNewlyStartedOnART = df.getPatientsInAll(startedTPTDuringQuarter,clientsStartedOnARTAtThisFacilityDuringPeriod);
        CohortDefinition startedTPTDuringQuarterAndPreviouslyOnART = df.getPatientsInAll(startedTPTDuringQuarter,onArtBeforeQuarter);
        CohortDefinition activePatientsAssessedForNutritionDuringQuarter = df.getPatientsInAll(patientsAssessedForNutritionDuringQuarter,activePatientsInCareDuringPeriod);
        CohortDefinition activePatientsThatAreMalnourishedDuringQuarter = df.getPatientsInAll(patientMalnourishedDuringPeriod,activePatientsInCareDuringPeriod);
        CohortDefinition activePatientsThatAreMalnourishedWhoReceivedTherapeuticFoods = df.getPatientsInAll(activePatientsThatAreMalnourishedDuringQuarter,patientsWhoReceivedTherapeuticFoods);
        CohortDefinition activeOnARTAndHadAViralLoadTest6MonthsAfterArtInitiation = df.getPatientsInAll(activePatientsInCareDuringPeriod,patientsWhoHadAViralLoadTest6MonthsAfterArtInitiation);
        CohortDefinition activeOnARTAndHadAViralLoadTest12MonthsAfterArtInitiation = df.getPatientsInAll(activePatientsInCareDuringPeriod,patientsWhoHadAViralLoadTest12MonthsAfterArtInitiation);
        CohortDefinition activeOnARTAndHadAViralLoadTest6MonthsAfterArtInitiationVirallySupressed = df.getPatientsInAll(activePatientsInCareDuringPeriod,patientsWhoHadAViralLoadTest6MonthsAfterArtInitiationAndAreVirallySupressed);
        CohortDefinition nonSuppressedARTClientsWhoHadNonSuppressedRepeatVL = df.getPatientsInAll(nonSuppressedClientsDuringPeriod, patientsNonSuppressedByEndOfLastPeriod);
        CohortDefinition nonSuppressedARTClientsWhoSwitchedFrom1stTo2nd= df.getPatientsInAll(nonSuppressedARTClientsWhoHadNonSuppressedRepeatVL, patientsWhoSwitchedFrom1stLineTo2nd);
        CohortDefinition nonSuppressedARTClientsWhoSwitchedFrom2ndTo3rd= df.getPatientsInAll(nonSuppressedARTClientsWhoHadNonSuppressedRepeatVL, patientsWhoSwitchedFrom2ndLineTo3rd);

        CohortDefinition HIVDRTestedDuringPeriod= df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getConcept("164989AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),null,Arrays.asList(hivMetadata.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.ANY);

        addAgeGender(dsd, "29", "active ART clients eligible for CPT", df.getPatientsInAll(activePatientsInCareDuringPeriod,eligibleForCPTDuringQuarter));
        addAgeGender(dsd, "30", "active ART clients on CPT", df.getPatientsInAll(activePatientsInCareDuringPeriod,onCPTDuringQuarter));
        addAgeGender(dsd, "31", "active ART clients screened for TB at last visit in the quarter", activePatientsScreenedForTB);
        addAgeGender(dsd, "32", "active ART clients with presumptive TB during quarter", activePatientsPresumptiveTBDuringQuarter);
        addAgeGender(dsd, "33", "active ART clients diagnosed with TB during quarter", activePatientsDiagnosedWithTBDuringQuarter );
        addAgeGender(dsd, "34a-", "active ART clients and on TB treatment newly started on TB treatment during quarter", activeOnArtStartedTBRxDuringPeriod);
        addAgeGender(dsd, "34b-", "active ART clients and on TB treatment previously on TB treatment",activeOnArtPreviouslyOnTBRx);
        addAgeGender(dsd, "35a-", "started TPT during period and newly started on ART",startedTPTDuringQuarterAndNewlyStartedOnART);
        addAgeGender(dsd, "35b-", "started TPT during period and previously on ART",startedTPTDuringQuarterAndPreviouslyOnART);
        addAgeGender(dsd, "36", "started TPT 6 months ago",startedTPTSixMonthsAgo);
        addAgeGender(dsd, "37", "completed TPT successfully during quarter",completedTPTDuringPeriod);
        addAgeGender(dsd, "38", "activeOnARTAssessedForMalnutrition",activePatientsAssessedForNutritionDuringQuarter);
        addAgeGender(dsd, "39", "activeOnART who are Malnourished",activePatientsThatAreMalnourishedDuringQuarter);
        addAgeGender(dsd, "40", "active Patients That Are Malnourished Who Received Therapeutic/Supplementary Foods",activePatientsThatAreMalnourishedWhoReceivedTherapeuticFoods);
        addAgeGender(dsd, "41a-", "activeOnART patients who had a viral load test at 6 months after art initiation total tested",activeOnARTAndHadAViralLoadTest6MonthsAfterArtInitiation);
        addAgeGender(dsd, "41b-", "activeOnART patients who had a viral load test at 6 months after art initiation with suppressed VL",activeOnARTAndHadAViralLoadTest6MonthsAfterArtInitiationVirallySupressed);
        addAgeGender(dsd, "42a-", "clients Who Had Viral Load Taken During The Past 12Months total ",df.getPatientsInAll(activePatientsInCareDuringPeriod,clientsWhoHadViralLoadTakenDuringThePast12Months));
        addAgeGender(dsd, "42b-", "clients Who Had Viral Load Taken During The Past 12Months virally suppressed ",df.getPatientsInAll(activePatientsInCareDuringPeriod,clientsWhoHadViralLoadTakenDuringThePast12MonthsAndVirallySupressed ));
        addAgeGender(dsd, "43a-", "activeOnART patients who had a viral load test at 12 months after art initiation total tested",activeOnARTAndHadAViralLoadTest12MonthsAfterArtInitiation);
        addAgeGender(dsd, "43b-", "activeOnART patients who had a viral load test at 12 months after art initiation who are suppressed",patientsWhoHadAViralLoadTest12MonthsAfterArtInitiationAndAreVirallySupressed);
        addAgeGender(dsd, "44a-", "patients who had a repeat VL tht remained non suppressed during reporting period",nonSuppressedARTClientsWhoHadNonSuppressedRepeatVL);
        addAgeGender(dsd, "44b-", "patients who had a repeat VL tht remained non suppressed & switched from 1st to 2nd line ",nonSuppressedARTClientsWhoSwitchedFrom1stTo2nd);
        addAgeGender(dsd, "44c-", "patients who had a repeat VL tht remained non suppressed & switched from 2nd to 3rd line",nonSuppressedARTClientsWhoSwitchedFrom2ndTo3rd);
        addAgeGender(dsd, "45a-", "patients who had HIVDR test during period and on First line",df.getPatientsInAll(onFirstLineRegimen,HIVDRTestedDuringPeriod));
        addAgeGender(dsd, "45b-", "patients who had HIVDR test during period and on  2nd line ",df.getPatientsInAll(onSecondLineRegimen,HIVDRTestedDuringPeriod));
        addAgeGender(dsd, "45c-", "patients who had HIVDR test during period and on 3rd line",df.getPatientsInAll(onThirdLineRegimenDuringQuarter,HIVDRTestedDuringPeriod));
        addAgeGender(dsd, "46", "patients who are non suppressed & tested for CD4",patientsNonSuppressedAndTestedWithCD4);
        addAgeGender(dsd, "47a-", "non suppressed & tested for CD4 and accessed TB LAM",patientsNonSuppressedCd4DuringPeriodLessThan200AndAccessedTBLAM);
        addAgeGender(dsd, "47b-", "non suppressed & tested for CD4 and positive 4 TB LAM",patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForTBLAM);
        addAgeGender(dsd, "47c-", "non suppressed & tested for CD4 and positive & treated",patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForTBLAMAndTreatedForTB);
        addAgeGender(dsd, "48a-", "non suppressed & tested for CD4 and accessed CRAG",patientsNonSuppressedCd4DuringPeriodLessThan200AndAccessedCRAG);
        addAgeGender(dsd, "48b-", "non suppressed & tested for CD4 and positive 4 CRAG",patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForCRAG);
        addAgeGender(dsd, "48c-", "non suppressed & tested for CD4 and treated with fluconazole",patientsNonSuppressedCd4DuringPeriodLessThan200AndPositiveForCRAGTreatedWithFluconazole);


        return rd;
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "a", label + " (Below 1 Males)", cohortDefinition, "age=below1male");
        addIndicator(dsd, key + "b", label + " (Below 1 Females)", cohortDefinition, "age=below1female");
        addIndicator(dsd, key + "c", label + " (Between 1 and 1 Males)", cohortDefinition, "age=between1and4male");
        addIndicator(dsd, key + "d", label + " (Between 1 and 4 Females)", cohortDefinition, "age=between1and4female");
        addIndicator(dsd, key + "e", label + " (Between 5 and 9 Males)", cohortDefinition, "age=between5and9male");
        addIndicator(dsd, key + "f", label + " (Between 5 and 9 Females)", cohortDefinition, "age=between5and9female");
        addIndicator(dsd, key + "g", label + " (Between 10 and 14 Males)", cohortDefinition, "age=between10and14male");
        addIndicator(dsd, key + "h", label + " (Between 10 and 14 Females)", cohortDefinition, "age=between10and14female");
        addIndicator(dsd, key + "i", label + " (Between 15 and 19 Males)", cohortDefinition, "age=between15and19male");
        addIndicator(dsd, key + "j", label + " (Between 15 and 19 Females)", cohortDefinition, "age=between15and19female");
        addIndicator(dsd, key + "k", label + " (Between 20 and 24 Males)", cohortDefinition, "age=between20and24male");
        addIndicator(dsd, key + "l", label + " (Between 20 and 24 Females)", cohortDefinition, "age=between20and24female");
        addIndicator(dsd, key + "m", label + " (Between 25 and 29 Males)", cohortDefinition, "age=between25and29male");
        addIndicator(dsd, key + "n", label + " (Between 25 and 29 Females)", cohortDefinition, "age=between25and29female");
        addIndicator(dsd, key + "o", label + " (Between 30 and 34 Males)", cohortDefinition, "age=between30and34male");
        addIndicator(dsd, key + "p", label + " (Between 30 and 34 Females)", cohortDefinition, "age=between30and34female");
        addIndicator(dsd, key + "q", label + " (Between 35 and 39 Males)", cohortDefinition, "age=between35and39male");
        addIndicator(dsd, key + "r", label + " (Between 35 and 39 Females)", cohortDefinition, "age=between35and39female");
        addIndicator(dsd, key + "s", label + " (Between 40 and 44 Males)", cohortDefinition, "age=between40and44male");
        addIndicator(dsd, key + "t", label + " (Between 40 and 44 Females)", cohortDefinition, "age=between40and44female");
        addIndicator(dsd, key + "u", label + " (Between 45 and 49 Males)", cohortDefinition, "age=between45and49male");
        addIndicator(dsd, key + "v", label + " (Between 45 and 49 Females)", cohortDefinition, "age=between45and49female");
        addIndicator(dsd, key + "w", label + " (Above 50 Males)", cohortDefinition, "age=above50male");
        addIndicator(dsd, key + "x", label + " (Above 50 Females)", cohortDefinition, "age=above50female");
        addIndicator(dsd, key + "y", label + " Total", cohortDefinition, "");
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    public CohortDefinition addParameters(CohortDefinition cohortDefinition){
        return   df.convert(cohortDefinition, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition addStartDateAndEndDateParameters(CohortDefinition cohortDefinition){
        return   df.convert(cohortDefinition, ObjectUtil.toMap("startDate=startDate,endDate=endDate"));
    }

    private DataSetDefinition getMedianCD4AtARTInitiation() {
        MedianBaselineCD4DatasetDefinition dsd = new MedianBaselineCD4DatasetDefinition();
       dsd.setParameters(getParameters());
        return dsd;
    }

    @Override
    public String getVersion() {
        return "1.0.2";
    }
}
