package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  TX Current Report
 */
@Component
public class SetupTxNewReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "941f0989-635a-473e-96a8-ada6e2d6409e";
    }

    @Override
    public String getUuid() {
        return "621890e1-3549-4ccf-ad93-c206bd3e7c17";
    }

    @Override
    public String getName() {
        return "Tx New Report";
    }

    @Override
    public String getDescription() {
        return "Tx New Report";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }


    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding
     * properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_NEW.xls");
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
        rd.addDataSetDefinition("TX_NEW", Mapped.mapStraightThrough(dsd));
        //rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxCurrentAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition enrolledOnOrBeforeQuarter = hivCohortDefinitionLibrary.getEnrolledInCareByEndOfPreviousDate();
        CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();

        // CohortDefinition activeWithNoEncounterInQuarter = hivCohortDefinitionLibrary.getActiveWithNoEncounterInQuarter();

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
        CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter, havingBaseRegimenDuringQuarter);

        CohortDefinition everEnrolledByEndQuarter = df.getPatientsNotIn(enrolledOnOrBeforeQuarter, enrolledInTheQuarter);
        CohortDefinition enrolledDuringTheQuarter = df.getPatientsNotIn(enrolledInTheQuarter, transferredInTheQuarter);

        CohortDefinition startedINHDuringQuarter = df.getPatientsNotIn(onINHDuringQuarter, onINHBeforeQuarter);

        CohortDefinition cumulativeEverEnrolled = df.getPatientsInAny(everEnrolledByEndQuarter, enrolledDuringTheQuarter);

        CohortDefinition onPreArt = df.getPatientsNotIn(hadEncounterInQuarter, df.getPatientsInAny(beenOnArtBeforeQuarter, beenOnArtDuringQuarter));

        CohortDefinition onPreArtWhoReceivedCPT = df.getPatientsInAll(onPreArt, onCPTDuringQuarter);

        CohortDefinition onPreArtAssessedForTB = df.getPatientsInAll(onPreArt, assessedForTBDuringQuarter);

        CohortDefinition onPreArtDiagnosedWithTB = df.getPatientsInAll(onPreArt, diagnosedWithTBDuringQuarter);

        CohortDefinition startedTBDuringQuarter = df.getPatientsNotIn(onTBRxDuringQuarter, onTBRxBeforeQuarter);

        CohortDefinition onPreArtStartedTBRx = df.getPatientsInAll(onPreArt, startedTBDuringQuarter);

        CohortDefinition assessedForMalnutrition = df.getPatientsInAny(oedemaWasTakenDuringQuarter, mUACWasTakenDuringQuarter, assessedForMalnutritionDuringQuarter, heightWasTakenDuringQuarter, weightWasTakenDuringQuarter, baseWeightWasTakenDuringQuarter);

        CohortDefinition onPreArtAssessedForMalnutrition = df.getPatientsInAll(onPreArt, assessedForMalnutrition);

        CohortDefinition whoAreMalnourished = df.getPatientsInAny(mUACWasYellowOrRedDuringQuarter, malnourishedDuringQuarter, oedemaWasYesDuringQuarter);
        CohortDefinition onPreArtWhoAreMalnourished = df.getPatientsInAll(onPreArt, whoAreMalnourished);

        CohortDefinition startedBasedOnCD4 = df.getPatientsInAll(havingArtStartDateDuringQuarter, onArtBasedOnCD4);

        CohortDefinition cumulativeOnArt = df.getPatientsInAny(havingArtStartDateBeforeQuarter, havingArtStartDateDuringQuarter);

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

        CohortDefinition startedArtWhenPregnant = df.getPatientsInAll(pregnantAtFirstEncounter, havingArtStartDateDuringQuarter);

        addIndicator(dsd, "1a", "All currently receiving  ART ",beenOnArtDuringQuarter, "age=below1female");
        addIndicator(dsd, "1b", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between1and4female");
        addIndicator(dsd, "1c", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between5and9female");
        addIndicator(dsd, "1d", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between10and14female");
        addIndicator(dsd, "1e", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between15and19female");
        addIndicator(dsd, "1f", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between20and24female");
        addIndicator(dsd, "1g", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between25and29female");
        addIndicator(dsd, "1h", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between30and34female");
        addIndicator(dsd, "1i", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between35and39female");
        addIndicator(dsd, "1j", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between40and49female");
        addIndicator(dsd, "1k", "All currently receiving  ART", beenOnArtDuringQuarter, "age=above50female");
        addIndicator(dsd, "1l", "All active Pre-Art in the quarter", onPreArt, "age=below1female");
        addIndicator(dsd, "1m", "All active Pre-Art in the quarter", onPreArt, "age=between1and4female");
        addIndicator(dsd, "1n", "All active Pre-Art in the quarter", onPreArt,  "age=between5and9female");
        addIndicator(dsd, "1o", "All active Pre-Art in the quarter", onPreArt, "age=between10and14female");
        addIndicator(dsd, "1p", "All active Pre-Art in the quarter", onPreArt,"age=between15and19female");
        addIndicator(dsd, "1q", "All active Pre-Art in the quarter", onPreArt,  "age=between20and24female");
        addIndicator(dsd, "1r", "All active Pre-Art in the quarter", onPreArt, "age=between25and29female");
        addIndicator(dsd, "1s", "All active Pre-Art in the quarter", onPreArt, "age=between30and34female");
        addIndicator(dsd, "1t", "All active Pre-Art in the quarter", onPreArt,  "age=between35and39female");
       addIndicator(dsd, "1u", "All active Pre-Art in the quarter", onPreArt, "age=between40and49female");
        addIndicator(dsd, "1v", "All active Pre-Art in the quarter", onPreArt, "age=above50female");

        addIndicator(dsd, "2a", "All currently receiving  ART ", beenOnArtDuringQuarter, "age=below1male");
        addIndicator(dsd, "2b", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between1and4male");
        addIndicator(dsd, "2c", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between5and9male");
        addIndicator(dsd, "2d", "All currently receiving  ART", beenOnArtDuringQuarter,"age=between10and14male");
        addIndicator(dsd, "2e", "All currently receiving  ART", beenOnArtDuringQuarter,"age=between15and19male");
        addIndicator(dsd, "2f", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between20and24male");
        addIndicator(dsd, "2g", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between25and29male");
        addIndicator(dsd, "2h", "All currently receiving  ART",beenOnArtDuringQuarter, "age=between30and34male");
        addIndicator(dsd, "2i", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between35and39male");
        addIndicator(dsd, "2j", "All currently receiving  ART",beenOnArtDuringQuarter, "age=between40and49male");
        addIndicator(dsd, "2k", "All currently receiving  ART", beenOnArtDuringQuarter, "age=above50male");
        addIndicator(dsd, "2l", "All active Pre-Art in the quarter", onPreArt, "age=below1male");
        addIndicator(dsd, "2m", "All active Pre-Art in the quarter", onPreArt,"age=between1and4male");
        addIndicator(dsd, "2n", "All active Pre-Art in the quarter", onPreArt, "age=between5and9male");
        addIndicator(dsd, "2o", "All active Pre-Art in the quarter", onPreArt, "age=between10and14male");
        addIndicator(dsd, "2p", "All active Pre-Art in the quarter", onPreArt, "age=between15and19male");
        addIndicator(dsd, "2q", "All active Pre-Art in the quarter", onPreArt, "age=between20and24male");
        addIndicator(dsd, "2r", "All active Pre-Art in the quarter", onPreArt,"age=between25and29male");
        addIndicator(dsd, "2s", "All active Pre-Art in the quarter", onPreArt, "age=between30and34male");
        addIndicator(dsd, "2t", "All active Pre-Art in the quarter", onPreArt,  "age=between35and39male");
        addIndicator(dsd, "2u", "All active Pre-Art in the quarter", onPreArt, "age=between40and49male");
        addIndicator(dsd, "2v", "All active Pre-Art in the quarter", onPreArt, "age=above50male");

        addAgeGender(dsd, "1.1", "All active on Art on 1st line", onFirstLineRegimen);
        addAgeGender(dsd, "2.1", "All active on Art on 2nd line", onSecondLineRegimen);
        addAgeGender(dsd, "3.1", "All active on Art on 3rd line", onThirdLineRegimenDuringQuarter);
        addIndicator(dsd, "4.1j", "PreArt" + " Total", onPreArt, "age=child");
        addIndicator(dsd, "4.2j", "PreArt" + " Total", onPreArt, "age=adult");



      //  addAgeGender(dsd, "4.1", "All who have ever enrolled up to quarter", cumulativeEverEnrolled);


        //add all values up to cell 11





       // rd.setBaseCohortDefinition(Mapped.mapStraightThrough(definition));

        return rd;
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "c", label + " (Below 2 Males)", cohortDefinition, "age=below2male");
        addIndicator(dsd, key + "d", label + " (Below 2 Females)", cohortDefinition, "age=below2female");
        addIndicator(dsd, key + "e", label + " (Between 2 and 5 Males)", cohortDefinition, "age=between2and5male");
        addIndicator(dsd, key + "f", label + " (Between 2 and 5 Females)", cohortDefinition, "age=between2and5female");
        addIndicator(dsd, key + "g", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "h", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "i", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "k", label + " (Above 15 Females)", cohortDefinition, "age=above15female");

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
        return "0.1";
    }
}
