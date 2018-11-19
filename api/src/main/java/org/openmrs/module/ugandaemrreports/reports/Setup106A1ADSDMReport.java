package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 */
@Component
public class Setup106A1ADSDMReport extends UgandaEMRDataExportManager {
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
        return "c4b06bdb-ced6-4aec-950b-7ac9d4e940e6";
    }

    @Override
    public String getUuid() {
        return "c05879b8-77db-4a91-90a9-34631cd0ca3b";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A/DSDM";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1A/DSDM";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106ADSDMReport.xls");
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

        CohortDefinitionDimension programDimension = commonDimensionLibrary.getProgramsDimensionGroup();
        dsd.addDimension("program",Mapped.mapStraightThrough(programDimension));

        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();

        CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod();

        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();

        CohortDefinition pregnantAtFirstEncounter = hivCohortDefinitionLibrary.getPatientsPregnantAtFirstEncounter();

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

        CohortDefinition childrenOnFirstLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(), hivCohortDefinitionLibrary.getChildrenOnFirstLineRegimenDuringPeriod());
        CohortDefinition childrenOnSecondLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(), hivCohortDefinitionLibrary.getChildrenOnSecondLineRegimenDuringPeriod());

        CohortDefinition adultsOnFirstLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(), hivCohortDefinitionLibrary.getAdultsOnFirstLineRegimenDuringPeriod());
        CohortDefinition adultsOnSecondLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(), hivCohortDefinitionLibrary.getAdultsOnSecondLineRegimenDuringPeriod());

        CohortDefinition onThirdLineRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsOnThirdLineRegimenDuringPeriod();

        CohortDefinition patientsWithGoodAdherenceDuringQuarter = hivCohortDefinitionLibrary.getPatientsWithGoodAdherence();

        CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter, havingBaseRegimenDuringQuarter);

        CohortDefinition startedTBDuringQuarter = df.getPatientsNotIn(onTBRxDuringQuarter, onTBRxBeforeQuarter);

        CohortDefinition assessedForMalnutrition = df.getPatientsInAny(oedemaWasTakenDuringQuarter, mUACWasTakenDuringQuarter, assessedForMalnutritionDuringQuarter, heightWasTakenDuringQuarter, weightWasTakenDuringQuarter, baseWeightWasTakenDuringQuarter);

        CohortDefinition whoAreMalnourished = df.getPatientsInAny(mUACWasYellowOrRedDuringQuarter, malnourishedDuringQuarter, oedemaWasYesDuringQuarter);

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

        CohortDefinition startedArtWhenPregnant = df.getPatientsInAll(pregnantAtFirstEncounter, havingArtStartDateDuringQuarter);

        CohortDefinition diedDuringPeriod = df.getPatientsInAll(df.getDeadPatientsDuringPeriod());

        CohortDefinition enrolledOnOrBeforeQuarter = hivCohortDefinitionLibrary.getEnrolledInCareByEndOfPreviousDate();
        CohortDefinition enrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();

        CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();

        CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod();

        CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();
        CohortDefinition transferredInBeforeQuarter = hivCohortDefinitionLibrary.getTransferredInToCareBeforePeriod();

        CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);

        CohortDefinition everEnrolledByEndQuarter = df.getPatientsNotIn(enrolledOnOrBeforeQuarter, enrolledInTheQuarter);
        CohortDefinition enrolledDuringTheQuarter = df.getPatientsNotIn(enrolledInTheQuarter, transferredInTheQuarter);

        CohortDefinition cumulativeEverEnrolled = df.getPatientsInAny(everEnrolledByEndQuarter, enrolledDuringTheQuarter);
        CohortDefinition lostToFollowup = df.getPatientsInAll(df.getEverLost(),cumulativeEverEnrolled);

        CohortDefinition enrolledOnDSDMDuringQuarter =df.getEnrolledOnDSDM();
        CohortDefinition goodAdherenceForLast6Months =df.getPatientsWithGoodAdherenceForLast6Months();
        CohortDefinition onClinicalStage1or2 = df.getOnClinicalStage1or2();
        CohortDefinition virallySupressedForLast12Months = df.getPatientsVirallySupressedForLast12Months();
        CohortDefinition onArtFor12MonthsAbove = df.getPatientsOnArtForMoreThansMonths(12);
        CohortDefinition stablePatients = df.getPatientsInAll(virallySupressedForLast12Months,onClinicalStage1or2,
                df.getPatientsInAny(onFirstLineRegimen,onSecondLineRegimen),goodAdherenceForLast6Months,onArtFor12MonthsAbove);

        CohortDefinition unsupressedVL = df.getUnsupressedVLPatients();


        addIndicator(dsd,"CUM","total on art before quater", havingArtStartDateBeforeQuarter,"");

        addIndicator(dsd,"STABLE","total of stable patients",stablePatients,"");

        addProgram(dsd,"1","fbim","new cleints on ART ",havingArtStartDateDuringQuarter);
        addProgram(dsd, "2","fbim", "Started Art based on CD4", startedBasedOnCD4);
        addProgram(dsd, "3", "fbim","Started ART when pregnant ", startedArtWhenPregnant);
        addProgram(dsd,"4","fbim","Ever enrolled", cumulativeOnArt);
        addProgram(dsd,"4","fbg","Ever enrolled", cumulativeOnArt);
        addProgram(dsd,"4","ftr","Ever enrolled", cumulativeOnArt);
        addProgram(dsd,"4","cddp","Ever enrolled", cumulativeOnArt);
        addProgram(dsd,"4","cclad","Ever enrolled", cumulativeOnArt);

        addProgram(dsd,"5","fbim","on First Line Regimen",onFirstLineRegimen);
        addProgram(dsd,"5","fbg","on First Line Regimen",onFirstLineRegimen);
        addProgram(dsd,"5","ftr","on First Line Regimen",onFirstLineRegimen);
        addProgram(dsd,"5","cddp","on First Line Regimen",onFirstLineRegimen);
        addProgram(dsd,"5","cclad","on First Line Regimen",onFirstLineRegimen);

        addProgram(dsd,"6","fbim","on Second Line Regimen",onSecondLineRegimen);
        addProgram(dsd,"6","fbg","on Second Line Regimen",onSecondLineRegimen);
        addProgram(dsd,"6","ftr","on Second Line Regimen",onSecondLineRegimen);
        addProgram(dsd,"6","cddp","on Second Line Regimen",onSecondLineRegimen);
        addProgram(dsd,"6","cclad","on Second Line Regimen",onSecondLineRegimen);

        addProgram(dsd,"7","fbim","on Third Line Regimen",onThirdLineRegimenDuringQuarter);

        addProgram(dsd, "8","fbim", "On Art Received CPT", activeOnArtOnCPT);
        addProgram(dsd, "8","fbg", "On Art Received CPT", activeOnArtOnCPT);
        addProgram(dsd, "8","ftr", "On Art Received CPT", activeOnArtOnCPT);
        addProgram(dsd, "8","cddp", "On Art Received CPT", activeOnArtOnCPT);
        addProgram(dsd, "8","cclad", "On Art Received CPT", activeOnArtOnCPT);

        addProgram(dsd, "9","fbim", "On Art assessed for TB", activeOnArtAssessedForTB);
        addProgram(dsd, "9","fbg", "On Art assessed for TB", activeOnArtAssessedForTB);
        addProgram(dsd, "9","ftr", "On Art assessed for TB", activeOnArtAssessedForTB);
        addProgram(dsd, "9","cddp", "On Art assessed for TB", activeOnArtAssessedForTB);
        addProgram(dsd, "9","cclad", "On Art assessed for TB", activeOnArtAssessedForTB);

        addProgram(dsd, "10", "fbim","On Art diagnosed with TB", activeOnArtDiagnosedWithTB);
        addProgram(dsd, "10", "fbg","On Art diagnosed with TB", activeOnArtDiagnosedWithTB);
        addProgram(dsd, "10", "ftr","On Art diagnosed with TB", activeOnArtDiagnosedWithTB);
        addProgram(dsd, "10", "cddp","On Art diagnosed with TB", activeOnArtDiagnosedWithTB);
        addProgram(dsd, "10", "cclad","On Art diagnosed with TB", activeOnArtDiagnosedWithTB);

        addProgram(dsd, "11","fbim", "On Art started TB Rx this quarter", activeOnArtStartedTBRx);

        addProgram(dsd, "12","fbim", "On Art and TB treatment", activeOnArtOnTBRx);
        addProgram(dsd, "12","fbg", "On Art and TB treatment", activeOnArtOnTBRx);
        addProgram(dsd, "12","ftr", "On Art and TB treatment", activeOnArtOnTBRx);
        addProgram(dsd, "12","cddp", "On Art and TB treatment", activeOnArtOnTBRx);
        addProgram(dsd, "12","cclad", "On Art and TB treatment", activeOnArtOnTBRx);

        addProgram(dsd, "13","fbim", "On Art good adherence", activeOnArtWithGoodAdherence);
        addProgram(dsd, "13","fbg", "On Art good adherence", activeOnArtWithGoodAdherence);
        addProgram(dsd, "13","ftr", "On Art good adherence", activeOnArtWithGoodAdherence);
        addProgram(dsd, "13","cddp", "On Art good adherence", activeOnArtWithGoodAdherence);
        addProgram(dsd, "13","cclad", "On Art good adherence", activeOnArtWithGoodAdherence);

        addProgram(dsd, "14","fbim", "On Art assessed for malnutrition", activeOnArtAssessedForMalnutrition);
        addProgram(dsd, "14","fbg", "On Art assessed for malnutrition", activeOnArtAssessedForMalnutrition);
        addProgram(dsd, "14","ftr", "On Art assessed for malnutrition", activeOnArtAssessedForMalnutrition);
        addProgram(dsd, "14","cddp", "On Art assessed for malnutrition", activeOnArtAssessedForMalnutrition);
        addProgram(dsd, "14","cclad", "On Art assessed for malnutrition", activeOnArtAssessedForMalnutrition);

        addProgram(dsd, "15","fbim", "On Art malnourished", activeOnArtWhoAreMalnourished);
        addProgram(dsd, "15","fbg", "On Art malnourished", activeOnArtWhoAreMalnourished);
        addProgram(dsd, "15","ftr", "On Art malnourished", activeOnArtWhoAreMalnourished);
        addProgram(dsd, "15","cddp", "On Art malnourished", activeOnArtWhoAreMalnourished);
        addProgram(dsd, "15","cclad", "On Art malnourished", activeOnArtWhoAreMalnourished);

        addProgram(dsd,"16","fbg","DSDMEnrollment",enrolledOnDSDMDuringQuarter);
        addProgram(dsd,"16","ftr","DSDMEnrollment",enrolledOnDSDMDuringQuarter);
        addProgram(dsd,"16","cddp","DSDMEnrollment",enrolledOnDSDMDuringQuarter);
        addProgram(dsd,"16","cclad","DSDMEnrollment",enrolledOnDSDMDuringQuarter);

        addProgram(dsd,"17","fbim","unsupressed vl",unsupressedVL);
        addProgram(dsd,"17","fbg","unsupressed vl",unsupressedVL);
        addProgram(dsd,"17","ftr","unsupressed vl",unsupressedVL);
        addProgram(dsd,"17","cddp","unsupressed vl",unsupressedVL);
        addProgram(dsd,"17","cclad","unsupressed vl",unsupressedVL);

        addProgram(dsd,"18","fbim","lost to followup",lostToFollowup);
        addProgram(dsd,"18","fbg","lost to followup",lostToFollowup);
        addProgram(dsd,"18","ftr","lost to followup",lostToFollowup);
        addProgram(dsd,"18","cddp","lost to followup",lostToFollowup);
        addProgram(dsd,"18","cclad","lost to followup",lostToFollowup);

        addProgram(dsd,"19","fbim","Dead",diedDuringPeriod);
        addProgram(dsd,"19","fbg","Dead",diedDuringPeriod);
        addProgram(dsd,"19","ftr","Dead",diedDuringPeriod);
        addProgram(dsd,"19","cddp","Dead",diedDuringPeriod);
        addProgram(dsd,"19","cclad","Dead",diedDuringPeriod);





        return rd;
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
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

    public void addProgram(CohortIndicatorDataSetDefinition dsd, String key,String programKey, String label, CohortDefinition cohortDefinition) {
        addProgramKey(dsd,key,programKey,label,cohortDefinition);
    }

    public void addProgramKey(CohortIndicatorDataSetDefinition dsd, String key, String programkey, String label, CohortDefinition cohortDefinition){
        addIndicator(dsd, key +programkey.toUpperCase()+"a" , label + " (below 2 years male)", cohortDefinition, "program="+programkey+"below2Male");
        addIndicator(dsd, key +programkey.toUpperCase() +"b" , label + " ( below 2 years female)", cohortDefinition, "program="+programkey+"below2Female");
        addIndicator(dsd, key +programkey.toUpperCase()+"c" , label + " (between 2 and 4 years male)", cohortDefinition, "program="+programkey+"between2And4Male");
        addIndicator(dsd, key +programkey.toUpperCase()+"d" , label + " (between 2 and 4 years female)", cohortDefinition, "program="+programkey+"between2And4Female");
        addIndicator(dsd, key +programkey.toUpperCase()+"e" , label + " ( between 5 and 14 years male)", cohortDefinition, "program="+programkey+"between5And14Male");
        addIndicator(dsd, key +programkey.toUpperCase()+"f" , label + " ( between 5 and 14 years female)", cohortDefinition, "program="+programkey+"between5And14Female");
        addIndicator(dsd, key +programkey.toUpperCase()+"g" , label + " ( above 15 years male)", cohortDefinition, "program="+programkey+"above15Male");
        addIndicator(dsd, key +programkey.toUpperCase()+"h" , label + " (above 15 years female)", cohortDefinition, "program="+programkey+"above15Female");

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
        return "0.3";
    }

}
