package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reports.Helper;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * HCT TST FACILITY Report
 */
@Component
public class SetupHCT_TST_Facility2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private Moh105CohortLibrary moh105CohortLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "c531e014-4a14-4fde-bd15-19f4c36b4d77";
    }
    public String getJSONDesignUuid() {
        return "b177a052-2d2e-40bb-889b-d85c3ac21bf7";
    }

    @Override
    public String getUuid() {
        return "9d041ea6-2052-4203-8e40-c47fc359b8e1";
    }

    @Override
    public String getName() {
        return "HCT_TST_Facility Report 2019";
    }

    @Override
    public String getDescription() {
        return "HCT_TST_Facility Report 2019";
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
        List<ReportDesign> l = new ArrayList<>();
        l.add(buildReportDesign(reportDefinition));
        l.add(buildJSONReportDesign(reportDefinition));
        return l;
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_HTS_TST_Facility_2019.xls");
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "MER_HTS_TST.json");
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
        rd.addDataSetDefinition("HCT_TST_2019", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


        CohortDefinition testedPositiveDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition testedNegativeDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_NEGATIVE)),BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition APNReasonForTesting = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("f06ce85a-69a3-4415-8cc3-e2b51a4ae6a1")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition IndexClientTestingReasonForTesting = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("0e19ee29-a7bf-4580-9313-7853cdc412c1")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition SNSReasonForTesting = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("b1f34616-de68-4b90-9bc4-f0397692befa")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition patientThroughSTIClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("dcd98f72-30ab-102d-86b0-7a5022ba4115"));
        CohortDefinition patientThroughTBClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("165048AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughIPDClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("c09c3d3d-d07d-4d34-84f0-89ea4fd5d6d5"));
        CohortDefinition patientThroughNutritionEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("11c12455-2f54-4bb5-b051-0ecfd4a5fe96"));
        CohortDefinition nutritionAbove5Years = df.getPatientsInAll(patientThroughNutritionEntryPoint,cohortDefinitionLibrary.agedAtLeast(6));
        CohortDefinition patientThroughOPDEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("160542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughFamilyPlanningEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("164984AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughYCCEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("e9469d61-b0c3-4785-81c6-057c7bc099fc"));
        CohortDefinition YCCAbove5Years = df.getPatientsInAll(patientThroughYCCEntryPoint,cohortDefinitionLibrary.agedAtLeast(6));
        CohortDefinition OPDORFamilyPlaningOrYCCorNutrition = df.getPatientsInAny(YCCAbove5Years,nutritionAbove5Years,patientThroughOPDEntryPoint,patientThroughFamilyPlanningEntryPoint);
        CohortDefinition patientThroughMaternityEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("160456AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughPNCEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("165046AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughSMCEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("409eae6b-9457-4896-b5fa-2667ad5ceffc"));

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition patientsTestedOnFirstANC = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329"),hivMetadata.getHCTEncounterType(), RangeComparator.LESS_EQUAL, 1.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsTestedOnOtherANCs = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329"),hivMetadata.getHCTEncounterType(), RangeComparator.GREATER_THAN, 1.0, BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PMTCT = df.getPatientsInAny(patientsTestedOnOtherANCs,patientThroughMaternityEntryPoint,patientThroughPNCEntryPoint);

        CohortDefinition patientThroughOtherSpecifyEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("dcd68a88-30ab-102d-86b0-7a5022ba4115"));

        /** facility level mapping **/
        addAgeAndGender(dsd,"e","tested positive through the STI Clinic",df.getPatientsInAll(patientThroughSTIClinicEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"f","tested negative through the STI Clinic",df.getPatientsInAll(patientThroughSTIClinicEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"g","tested positive through the IPD Clinic",df.getPatientsInAll(patientThroughIPDClinicEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"h","tested negative through the IPD Clinic",df.getPatientsInAll(patientThroughIPDClinicEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"i","tested positive through the SMC Clinic",df.getPatientsInAll(patientThroughSMCEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"j","tested negative through the SMC Clinic",df.getPatientsInAll(patientThroughSMCEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"k","tested positive with APN reason for testing",df.getPatientsInAll(df.getPatientsInAny(APNReasonForTesting,IndexClientTestingReasonForTesting),testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"l","tested negative with APN reason for testing",df.getPatientsInAll(df.getPatientsInAny(APNReasonForTesting,IndexClientTestingReasonForTesting),testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"y","tested positive with SNS reason for testing",df.getPatientsInAll(SNSReasonForTesting,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"z","tested negative with SNS reason for testing",df.getPatientsInAll(SNSReasonForTesting,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"m","tested positive through the TB Clinic",df.getPatientsInAll(patientThroughTBClinicEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"n","tested negative through the TB Clinic",df.getPatientsInAll(patientThroughTBClinicEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"o","tested positive through the YCC Clinic",df.getPatientsInAll(patientThroughYCCEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"p","tested negative through the YCC Clinic",df.getPatientsInAll(patientThroughYCCEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"q","tested positive through the Malnutrition Clinic",df.getPatientsInAll(patientThroughNutritionEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"r","tested negative through the Malnutrition Clinic",df.getPatientsInAll(patientThroughNutritionEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"s","tested positive through the OPDORFamilyPlaningOrYCCorNutrition",df.getPatientsInAll(OPDORFamilyPlaningOrYCCorNutrition,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"t","tested negative through the OPDORFamilyPlaningOrYCCorNutrition",df.getPatientsInAll(OPDORFamilyPlaningOrYCCorNutrition,testedNegativeDuringPeriod));

        Helper.addIndicator(dsd,"PIPa","PIPa positive females",df.getPatientsInAll(females,PIPS,testedPositiveDuringPeriod),"");
        Helper.addIndicator(dsd,"PIPb","PIPb positive males",df.getPatientsInAll(males,PIPS,testedPositiveDuringPeriod),"");
        Helper.addIndicator(dsd,"PIPc","PIPc negative females",df.getPatientsInAll(females,PIPS,testedNegativeDuringPeriod),"");
        Helper.addIndicator(dsd,"PIPd","PIPd negative males",df.getPatientsInAll(males,PIPS,testedNegativeDuringPeriod),"");

        Helper.addIndicator(dsd,"PWIDSa","PWIDSa positive females",df.getPatientsInAll(females,PWIDS,testedPositiveDuringPeriod),"");
        Helper.addIndicator(dsd,"PWIDSb","PWIDSb positive males",df.getPatientsInAll(males,PWIDS,testedPositiveDuringPeriod),"");
        Helper.addIndicator(dsd,"PWIDSc","PWIDSc negative females",df.getPatientsInAll(females,PWIDS,testedNegativeDuringPeriod),"");
        Helper.addIndicator(dsd,"PWIDSd","PWIDSd negative males",df.getPatientsInAll(males,PWIDS,testedNegativeDuringPeriod),"");

        /** community level mappings**/
        addAgeAndGender(dsd,"a","tested positive through other specify entry point",df.getPatientsInAll(patientThroughOtherSpecifyEntryPoint,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"b","tested negative through other specify entry point",df.getPatientsInAll(patientThroughOtherSpecifyEntryPoint,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"c","tested positive through the VCT Approach",df.getPatientsInAll(patientsTestedOnFirstANC,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"d","tested negative through the VCT Approach",df.getPatientsInAll(patientsTestedOnFirstANC,testedNegativeDuringPeriod));

        addAgeAndGender(dsd,"u","tested positive through the Other points",df.getPatientsInAll(PMTCT,testedPositiveDuringPeriod));
        addAgeAndGender(dsd,"v","tested negative through the Other point",df.getPatientsInAll(PMTCT,testedNegativeDuringPeriod));

        Helper.addIndicator(dsd,"3c","ANC female below 10 years positive",df.getPatientsInAll(patientsTestedOnFirstANC,testedPositiveDuringPeriod,cohortDefinitionLibrary.agedAtMost(9)),"");
        Helper.addIndicator(dsd,"3d","ANC female below 10 years negative",df.getPatientsInAll(patientsTestedOnFirstANC,testedNegativeDuringPeriod,cohortDefinitionLibrary.agedAtMost(9)),"");

        Helper.addIndicator(dsd,"3u","other points PMTCT female below 10 years positive",df.getPatientsInAll(PMTCT,testedPositiveDuringPeriod,cohortDefinitionLibrary.agedAtMost(9)),"");
        Helper.addIndicator(dsd,"3v","other points PMTCT female below 10 years negative",df.getPatientsInAll(PMTCT,testedNegativeDuringPeriod,cohortDefinitionLibrary.agedAtMost(9)),"");


        return rd;
    }

    public void addAgeAndGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        /**          females age and gender mapping **/
        Helper.addIndicator(dsd, "1" + key, label, cohortDefinition, "age=below1female");
        Helper.addIndicator(dsd, "2" + key, label, cohortDefinition, "age=between1and4female");
        Helper.addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between5and9female");
        Helper.addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between10and14female");
        Helper.addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between15and19female");
        Helper.addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between20and24female");
        Helper.addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between25and29female");
        Helper.addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between30and34female");
        Helper.addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between35and39female");
        Helper.addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between40and44female");
        Helper.addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between45and49female");
        Helper.addIndicator(dsd, "12" + key, label, cohortDefinition, "age=above50female");
        /**         males age and gender mapping **/
        Helper.addIndicator(dsd, "13" + key, label, cohortDefinition, "age=below1male");
        Helper.addIndicator(dsd, "14" + key, label, cohortDefinition, "age=between1and4male");
        Helper.addIndicator(dsd, "15" + key, label, cohortDefinition, "age=between5and9male");
        Helper.addIndicator(dsd, "16" + key, label, cohortDefinition, "age=between10and14male");
        Helper.addIndicator(dsd, "17" + key, label, cohortDefinition, "age=between15and19male");
        Helper.addIndicator(dsd, "18" + key, label, cohortDefinition, "age=between20and24male");
        Helper.addIndicator(dsd, "19" + key, label, cohortDefinition, "age=between25and29male");
        Helper.addIndicator(dsd, "20" + key, label, cohortDefinition, "age=between30and34male");
        Helper.addIndicator(dsd, "21" + key, label, cohortDefinition, "age=between35and39male");
        Helper.addIndicator(dsd, "22" + key, label, cohortDefinition, "age=between40and44male");
        Helper.addIndicator(dsd, "23" + key, label, cohortDefinition, "age=between45and49male");
        Helper.addIndicator(dsd, "24" + key, label, cohortDefinition, "age=above50male");

    }

    public CohortDefinition getpatientTestedThroughFacilityEntryPoint(Concept entryPointConcept){
        CohortDefinition entryPoint = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("720a1e85-ea1c-4f7b-a31e-cb896978df79"),hivMetadata.getHCTEncounterType(),
                Arrays.asList(entryPointConcept), BaseObsCohortDefinition.TimeModifier.LAST);
        return entryPoint;
    }

    @Override
    public String getVersion() {
        return "0.3.4";
    }
}
