package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  MER 2.0 HTS RECENCY Report
 */
@Component
public class SetupMERHTSRecency2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "d5d1ee9c-c781-4e2d-acef-e480131e00b3";
    }

    @Override
    public String getUuid() {
        return "74955104-b588-4ad1-9dba-eaeb4d607a08";
    }

    @Override
    public String getName() {
        return "HTS_RECENT 2019 Report";
    }

    @Override
    public String getDescription() {
        return "MER Indicator Report for HTS_RECENT 2019";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_HTS_RECENT_2019.xls");
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
        rd.addDataSetDefinition("HTS_RECENCT_2019", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition patientsTestedThroughHealthFacility =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("46648b1d-b099-433b-8f9c-3815ff1e0a0f"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("ecb88326-0a3f-44a5-9bbf-df4bfc3239e1")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsTestedThroughCommunity =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("46648b1d-b099-433b-8f9c-3815ff1e0a0f"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("4f4e6d1d-4343-42cc-ba47-2319b8a84369")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsThroughSTIClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("dcd98f72-30ab-102d-86b0-7a5022ba4115"));
        CohortDefinition patientsThroughTBClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("165048AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientsThroughInWardFacilityEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("c09c3d3d-d07d-4d34-84f0-89ea4fd5d6d5"));
        CohortDefinition patientsThroughOtherFacilityEntryPoints = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("dcd68a88-30ab-102d-86b0-7a5022ba4115"));

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition recentHIVInfectionDuringPeriod = hivCohortDefinitionLibrary.getPatientWithRecentHIVInfectionDuringPeriod();
        CohortDefinition longTermHIVInfectionDuringPeriod = hivCohortDefinitionLibrary.getPatientWithLongTermHIVInfectionDuringPeriod();



        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition femalesPIPsWithRecentInfectionTestedAtFacilityLevel = df.getPatientsInAll(females, PIPS,recentHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);
        CohortDefinition malesPIPsWithRecentInfectionTestedAtFacilityLevel = df.getPatientsInAll(males, PIPS,recentHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);

        CohortDefinition femalesPIPsWithLongTermInfectionTestedAtFacilityLevel = df.getPatientsInAll(females, PIPS,longTermHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);
        CohortDefinition malesPIPsWithLongTermInfectionTestedAtFacilityLevel = df.getPatientsInAll(males, PIPS,longTermHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);

        CohortDefinition patientsThroughSTIClinicAndWithRecentInfection = df.getPatientsInAll(patientsThroughSTIClinicEntryPoint,recentHIVInfectionDuringPeriod);
        CohortDefinition patientsThroughSTIClinicAndWithLongTermInfection = df.getPatientsInAll(patientsThroughSTIClinicEntryPoint,longTermHIVInfectionDuringPeriod);

         CohortDefinition patientsThroughIPDAndWithRecentInfection = df.getPatientsInAll(patientsThroughInWardFacilityEntryPoint,recentHIVInfectionDuringPeriod);
         CohortDefinition patientsThroughIPDAndWithLongTermInfection = df.getPatientsInAll(patientsThroughInWardFacilityEntryPoint,longTermHIVInfectionDuringPeriod);

        CohortDefinition patientsThroughTBClinicAndWithRecentInfection = df.getPatientsInAll(patientsThroughTBClinicEntryPoint,recentHIVInfectionDuringPeriod);
        CohortDefinition patientsThroughTBClinicAndWithLongTermInfection = df.getPatientsInAll(patientsThroughTBClinicEntryPoint,longTermHIVInfectionDuringPeriod);

        CohortDefinition patientsThroughOtherEntryPoints = df.getPatientsNotIn(patientsTestedThroughHealthFacility,df.getPatientsInAll(patientsThroughSTIClinicEntryPoint,patientsThroughTBClinicEntryPoint,patientsThroughInWardFacilityEntryPoint));
        CohortDefinition patientsThroughOtherEntryPointsWithRecentInfection = df.getPatientsInAll(recentHIVInfectionDuringPeriod,patientsThroughOtherEntryPoints);
        CohortDefinition patientsThroughOtherEntryPointsWithLongTermInfection = df.getPatientsInAll(longTermHIVInfectionDuringPeriod,patientsThroughOtherEntryPoints);

        CohortDefinition patientWithRecentInfectionTestedFromCommunity = df.getPatientsInAll(recentHIVInfectionDuringPeriod, patientsTestedThroughCommunity);
        CohortDefinition patientWithLongTermInfectionTestedFromCommunity = df.getPatientsInAll(longTermHIVInfectionDuringPeriod, patientsTestedThroughCommunity);

        CohortDefinition femalesPWIDsWithRecentInfectionTestedAtFacilityLevel = df.getPatientsInAll(females, PWIDS,recentHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);
        CohortDefinition malesPWIDsWithRecentInfectionTestedAtFacilityLevel = df.getPatientsInAll(males, PWIDS,recentHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);

        CohortDefinition femalesPWIDsWithLongTermInfectionTestedAtFacilityLevel = df.getPatientsInAll(females, PWIDS,longTermHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);
        CohortDefinition malesPWIDsWithLongTermInfectionTestedAtFacilityLevel = df.getPatientsInAll(males, PWIDS,longTermHIVInfectionDuringPeriod,patientsTestedThroughHealthFacility);

        CohortDefinition femalesPIPsWithRecentInfectionTestedAtCommunityLevel = df.getPatientsInAll(females, PIPS,recentHIVInfectionDuringPeriod,patientsTestedThroughCommunity);
        CohortDefinition malesPIPsWithRecentInfectionTestedAtCommunityLevel = df.getPatientsInAll(males, PIPS,recentHIVInfectionDuringPeriod,patientsTestedThroughCommunity);

        CohortDefinition femalesPIPsWithLongTermInfectionTestedAtCommunityLevel = df.getPatientsInAll(females, PIPS,longTermHIVInfectionDuringPeriod,patientsTestedThroughCommunity);
        CohortDefinition malesPIPsWithLongTermInfectionTestedAtCommunityLevel = df.getPatientsInAll(males, PIPS,longTermHIVInfectionDuringPeriod,patientsTestedThroughCommunity);

        CohortDefinition femalesPWIDssWithRecentInfectionTestedAtCommunityLevel = df.getPatientsInAll(females, PWIDS,recentHIVInfectionDuringPeriod,patientsTestedThroughCommunity);
        CohortDefinition malesPWIDssWithRecentInfectionTestedAtCommunityLevel = df.getPatientsInAll(males, PWIDS,recentHIVInfectionDuringPeriod,patientsTestedThroughCommunity);

        CohortDefinition femalesPWIDsWithLongTermInfectionTestedAtCommunityLevel = df.getPatientsInAll(females, PWIDS,longTermHIVInfectionDuringPeriod,patientsTestedThroughCommunity);
        CohortDefinition malesPWIDsWithLongTermInfectionTestedAtCommunityLevel = df.getPatientsInAll(males, PWIDS,longTermHIVInfectionDuringPeriod,patientsTestedThroughCommunity);

        addAgeAndGender(dsd, "a", "Patients with recent infection through STI clinic", patientsThroughSTIClinicAndWithRecentInfection);
        addAgeAndGender(dsd, "b", "Patients with long term infection through STI clinic",patientsThroughSTIClinicAndWithLongTermInfection);

        addAgeAndGender(dsd, "c", "Patients with recent infection through IPD clinic", patientsThroughIPDAndWithRecentInfection);
        addAgeAndGender(dsd, "d", "Patients with long term infection through IPD clinic",patientsThroughIPDAndWithLongTermInfection);

        addAgeAndGender(dsd, "e", "Patients with recent infection through TB clinic", patientsThroughTBClinicAndWithRecentInfection);
        addAgeAndGender(dsd, "f", "Patients with long term infection through TB clinic",patientsThroughTBClinicAndWithLongTermInfection);

        addAgeAndGender(dsd, "g", "Patients with recent infection through other clinic", patientsThroughOtherEntryPointsWithRecentInfection);
        addAgeAndGender(dsd, "h", "Patients with long term infection through other clinic",patientsThroughOtherEntryPointsWithLongTermInfection);

        addAgeAndGender(dsd, "i", "Patients with recent infection through other community sites",patientWithRecentInfectionTestedFromCommunity );
        addAgeAndGender(dsd, "j", "Patients with long term infection through other community sites", patientWithLongTermInfectionTestedFromCommunity);

        Helper.addIndicator(dsd, "aa", "PIPS and with recent infection females at facility", femalesPIPsWithRecentInfectionTestedAtFacilityLevel, "");
        Helper.addIndicator(dsd, "bb", "PIPS and with recent infection males at facility", malesPIPsWithRecentInfectionTestedAtFacilityLevel, "");
        Helper.addIndicator(dsd, "cc", "PIPs and with Long term infection females at facility", femalesPIPsWithLongTermInfectionTestedAtFacilityLevel, "");
        Helper.addIndicator(dsd, "dd", "PIPs and with Long term infection males at facility", malesPIPsWithLongTermInfectionTestedAtFacilityLevel, "");

        Helper.addIndicator(dsd, "ee", "PWIDS and with recent infection females at facility", femalesPWIDsWithRecentInfectionTestedAtFacilityLevel, "");
        Helper.addIndicator(dsd, "ff", "PWIDS and with recent infection males at facility", malesPWIDsWithRecentInfectionTestedAtFacilityLevel, "");
        Helper.addIndicator(dsd, "gg", "PWIDS and with long term infection females at facility", femalesPWIDsWithLongTermInfectionTestedAtFacilityLevel, "");
        Helper.addIndicator(dsd, "hh", "PWIDS and with long term infection males at facility", malesPWIDsWithLongTermInfectionTestedAtFacilityLevel, "");

        Helper.addIndicator(dsd, "ii", "PIPS and with recent infection females at community", femalesPIPsWithRecentInfectionTestedAtCommunityLevel, "");
        Helper.addIndicator(dsd, "jj", "PIPS and with recent infection males at community", malesPIPsWithRecentInfectionTestedAtCommunityLevel, "");
        Helper.addIndicator(dsd, "kk", "PIPs and with Long term infection females at community", femalesPIPsWithLongTermInfectionTestedAtCommunityLevel, "");
        Helper.addIndicator(dsd, "ll", "PIPs and with Long term infection males at community", malesPIPsWithLongTermInfectionTestedAtCommunityLevel, "");

        Helper.addIndicator(dsd, "mm", "PWIDS and with recent infection females at community", femalesPWIDssWithRecentInfectionTestedAtCommunityLevel, "");
        Helper.addIndicator(dsd, "nn", "PWIDS and with recent infection males at community", malesPWIDssWithRecentInfectionTestedAtCommunityLevel, "");
        Helper.addIndicator(dsd, "oo", "PWIDS and with long term infection females at community", femalesPWIDsWithLongTermInfectionTestedAtCommunityLevel, "");
        Helper.addIndicator(dsd, "pp", "PWIDS and with long term infection males at community", malesPWIDsWithLongTermInfectionTestedAtCommunityLevel, "");

        return rd;
    }

    public void addAgeAndGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
          /**          females age and gender mapping **/
           Helper.addIndicator(dsd, "1" + key, label, cohortDefinition, "age=between15and19female");
           Helper.addIndicator(dsd, "2" + key, label, cohortDefinition, "age=between20and24female");
           Helper.addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between25and29female");
           Helper.addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between30and34female");
           Helper.addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between35and39female");
           Helper.addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between40and44female");
           Helper.addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between45and49female");
           Helper.addIndicator(dsd, "8" + key, label, cohortDefinition, "age=above50female");
         /**         males age and gender mapping **/
           Helper.addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between15and19male");
           Helper.addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between20and24male");
           Helper.addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between25and29male");
           Helper.addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between30and34male");
           Helper.addIndicator(dsd, "13" + key, label, cohortDefinition, "age=between35and39male");
           Helper.addIndicator(dsd, "14" + key, label, cohortDefinition, "age=between40and44male");
           Helper.addIndicator(dsd, "15" + key, label, cohortDefinition, "age=between45and49male");
           Helper.addIndicator(dsd, "16" + key, label, cohortDefinition, "age=above50male");
    }

    public CohortDefinition addParameters(CohortDefinition cohortDefinition) {
        return df.convert(cohortDefinition, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    public CohortDefinition getpatientTestedThroughFacilityEntryPoint(Concept entryPointConcept){
        CohortDefinition entryPoint = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(entryPointConcept), BaseObsCohortDefinition.TimeModifier.LAST);
    return entryPoint;
    }

    @Override
    public String getVersion() {
        return "0.2.0";
    }
}