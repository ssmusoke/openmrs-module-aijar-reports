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
public class Setup106A1A2019SectionsHC01ToHC15Report extends UgandaEMRDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private CommonCohortLibrary commonCohortLibrary;

    @Autowired
    private ARTCohortLibrary artCohortLibrary;
    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public String getExcelDesignUuid() {
        return "d305ad12-3080-49d4-802b-20a6ff6bed4a";
    }

    @Override
    public String getUuid() {
        return "072b526a-4140-4c43-9f5c-6d1fa74d7c42";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A 2019 Sections HC01 To HC15";
    }

    @Override
    public String getDescription() {
        return "This is the 2019 version of the HMIS 106A Section 1A Sections HC01 To HC15";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1A2019HC01ToHC15Report.xls");
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

        CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();



        CohortDefinition hadEncounterInQuarter = hivCohortDefinitionLibrary.getArtPatientsWithEncounterOrSummaryPagesBetweenDates();

        CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();
        CohortDefinition transferredInBeforeQuarter = hivCohortDefinitionLibrary.getTransferredInToCareBeforePeriod();

        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();

        CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod();

        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition clientsStartedOnARTAtThisFacilityDuringPeriod = df.getPatientsNotIn(havingArtStartDateDuringQuarter,transferredInTheQuarter);
        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();

        CohortDefinition enrolledWhenPregnantOrLactating = hivCohortDefinitionLibrary.getEnrolledInCareToCareWhenPregnantOrLactating();
        CohortDefinition startedARTWhenPregnantOrLactating = hivCohortDefinitionLibrary.getStartedOnARTWhenPregnantOrLactating();


        CohortDefinition assessedForTBDuringQuarter = hivCohortDefinitionLibrary.getAssessedForTBDuringPeriod();

        CohortDefinition diagnosedWithTBDuringQuarter = hivCohortDefinitionLibrary.getDiagnosedWithTBDuringPeriod();

        CohortDefinition patientsWithBaselineCD4 = hivCohortDefinitionLibrary.getPatientsWithBaselineCD4();
        CohortDefinition baseCD4L200 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(),RangeComparator.LESS_EQUAL, 200.0, BaseObsCohortDefinition.TimeModifier.FIRST);


        CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);
        CohortDefinition longRefillPatients = df.getPatientsWithLongRefills();
        CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter,longRefillPatients);

        CohortDefinition enrolledDuringTheQuarter = df.getPatientsNotIn(enrolledInTheQuarter, transferredInTheQuarter);

         CohortDefinition activePatientsInCareDuringPeriod = hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("90");



        CohortDefinition onPreArt = df.getPatientsNotIn(hadEncounterInQuarter, df.getPatientsInAny(beenOnArtBeforeQuarter, beenOnArtDuringQuarter));

        CohortDefinition clientsStartedOnARTAtThisFacilityBeforePeriod = df.getPatientsNotIn(havingArtStartDateBeforeQuarter,transferredInBeforeQuarter);
        CohortDefinition newHIVVerificationsDuringPeriod = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getConcept("dce12b4f-30ab-102d-86b0-7a5022ba4115"),hivMetadata.getARTSummaryPageEncounterType(),BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition positiveForTBLAM = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.TB_LAM_RESULTS),hivMetadata.getARTEncounterPageEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.POSITIVE)), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition positiveForCRAG = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CRAG_RESULTS),hivMetadata.getARTEncounterPageEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.POSITIVE)), BaseObsCohortDefinition.TimeModifier.ANY);
         CohortDefinition patientsInitiatedOnTBTreatmentDuringPeriod = hivCohortDefinitionLibrary.getPatientsWhoseTBStartDateDuringThePeriod();
        CohortDefinition patientsInitiatedOnFluconazoleTreatmentDuringPeriod = hivCohortDefinitionLibrary.getPatientsWhoseFluconazoleStartDateIsDuringThePeriod();
        CohortDefinition positiveForTBLAMAndInitiatedOnTBTreatmentDuringPeriod = df.getPatientsInAll(positiveForTBLAM, patientsInitiatedOnTBTreatmentDuringPeriod);
        CohortDefinition positiveForCRAGAndInitiatedOnFluconazoleTreatmentDuringPeriod = df.getPatientsInAll(positiveForCRAG, patientsInitiatedOnFluconazoleTreatmentDuringPeriod);


        CohortDefinition activePatientsOnPreArt = df.getPatientsInAll(activePatientsInCareDuringPeriod,onPreArt);
        CohortDefinition patientWithConfirmedAdvancedDisease = hivCohortDefinitionLibrary.getPatientsWithConfirmedAdvancedDiseaseDuringPeriod();
        CohortDefinition newClientsWithBaselineCd4LessEqual200 = df.getPatientsInAll(clientsStartedOnARTAtThisFacilityDuringPeriod,baseCD4L200);
         // 2019 Report Cohorts
        CohortDefinition enrolledInTheQuarterAndScreenedForTB = df.getPatientsInAll(enrolledDuringTheQuarter, assessedForTBDuringQuarter);
        CohortDefinition enrolledInTheQuarterAndDiagnosedWithTB = df.getPatientsInAll(enrolledDuringTheQuarter, diagnosedWithTBDuringQuarter);
        CohortDefinition newlyEnrolledPregnantOrLactatingDuringPeriod= df.getPatientsInAll(enrolledDuringTheQuarter,enrolledWhenPregnantOrLactating);
        CohortDefinition activePatientsOnPreARTWithConfirmedAdvancedDisease =df.getPatientsInAll(patientWithConfirmedAdvancedDisease,activePatientsOnPreArt);
        CohortDefinition newPositivesStartedOnArtInSameQuarter = df.getPatientsInAll(newHIVVerificationsDuringPeriod,havingArtStartDateDuringQuarter);
        CohortDefinition pregnantOrLactatingAndNewOnARTDuringQuarter= df.getPatientsInAll(startedARTWhenPregnantOrLactating,havingArtStartDateDuringQuarter);

        CohortDefinition patientsStartedArtDuringQuarterWithBaselineCD4= df.getPatientsInAll(patientsWithBaselineCD4,clientsStartedOnARTAtThisFacilityDuringPeriod);

        addAgeGender(dsd, "1", "New Clients enrolled in care during reporting quarter", enrolledDuringTheQuarter);
        addAgeGender(dsd, "2", "Pregnant and lactating enrolled in care", newlyEnrolledPregnantOrLactatingDuringPeriod);
        addAgeGender(dsd, "3a-", "No. of clients newly enrolled into Care - Screened for TB", enrolledInTheQuarterAndScreenedForTB);
        addAgeGender(dsd, "3b-", "No. of clients newly enrolled into Care - Diagnosed with TB", enrolledInTheQuarterAndDiagnosedWithTB);
        addAgeGender(dsd, "4", "No. of active clients on Pre-ART by end of quarter", activePatientsOnPreArt);
        addAgeGender(dsd, "5", "No. of active clients on Pre-ART with confirmed Adv Disease", activePatientsOnPreARTWithConfirmedAdvancedDisease);
        addAgeGender(dsd, "6", "No. of CRAG positive clients identified during period", positiveForCRAG);
        addAgeGender(dsd, "7", "No. of TB LAM positive clients initiated on TB Treatment", positiveForTBLAMAndInitiatedOnTBTreatmentDuringPeriod);
        addAgeGender(dsd, "8", "No. of CRAG positive clients initiated on Fluconazole pre-emptive Therapy", positiveForCRAGAndInitiatedOnFluconazoleTreatmentDuringPeriod);
        addAgeGender(dsd, "9", "Cumulative ever enrolled on ART by end of previous quarter", clientsStartedOnARTAtThisFacilityBeforePeriod);
        addAgeGender(dsd, "10", "new positives initiated on ART in the same quarter", newPositivesStartedOnArtInSameQuarter);
        addAgeGender(dsd, "11", "new clients started on ART in the same quarter",clientsStartedOnARTAtThisFacilityDuringPeriod);
        addAgeGender(dsd, "12", "pregnant and lactating clients started on ART in the same quarter", pregnantOrLactatingAndNewOnARTDuringQuarter);
        addAgeGender(dsd, "13", "new clients started on ART with Baseline CD4", patientsStartedArtDuringQuarterWithBaselineCD4 );
        addAgeGender(dsd, "15", "new clients started on ART with Baseline CD4 less equal to 200", newClientsWithBaselineCd4LessEqual200 );

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


    private DataSetDefinition getMedianCD4AtARTInitiation() {
        MedianBaselineCD4DatasetDefinition dsd = new MedianBaselineCD4DatasetDefinition();
       dsd.setParameters(getParameters());
        return dsd;
    }

    @Override
    public String getVersion() {
        return "1.0.1";
    }
}
