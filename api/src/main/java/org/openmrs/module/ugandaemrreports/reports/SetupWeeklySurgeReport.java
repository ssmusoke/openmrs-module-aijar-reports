package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.metadata.SMCMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.getUgandaEMRVersion;
import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;
import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConcept;

/**
 *  TX Current Report
 */
@Component
public class SetupWeeklySurgeReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private SMCMetadata smcMetadata;

    @Autowired
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "8347cd85-6c14-4509-9e78-77d2f0cc664a";
    }

    public String getJSONDesignUuid() {
        return "98b4d8d6-17da-45f2-a825-87a8f6522e13";
    }

    @Override
    public String getUuid() {
        return "e7102e5c-b90d-4a4a-b763-20518eadbae5";
    }

    @Override
    public String getName() {
        return "SURGE REPORT";
    }

    @Override
    public String getDescription() {
        return "SURGE REPORT";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "SURGE.xls");
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "SURGE.json");
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
        rd.addDataSetDefinition("TX", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("aijar", Mapped.mapStraightThrough(getUgandaEMRVersion()));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getSurgeWeeklyAgeDimension();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


        CohortDefinition TX_New = df.getPatientsNotIn(hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(),hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod());
        CohortDefinition SMC =df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(smcMetadata.getCircumcisionDate(), null, BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition initiatedOnIPT =hivCohortDefinitionLibrary.getTPTStartDateBetweenPeriod();
        CohortDefinition patientsStartedOnTLDDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(),hivMetadata.getARTSummaryPageEncounterType(),Arrays.asList(Dictionary.getConcept("a58d12c5-abc2-4575-8fdb-f30960f348fc")), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition emtctCareEntries = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCareEntryPoint(),hivMetadata.getARTSummaryPageEncounterType(),Arrays.asList(hivMetadata.getEMTCTCareEntryPoint()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition TLDThroughEMTCT = df.getPatientsInAll(emtctCareEntries,patientsStartedOnTLDDuringPeriod);

        CohortDefinition TLDThroughART = df.getPatientsNotIn(patientsStartedOnTLDDuringPeriod,TLDThroughEMTCT);

        CohortDefinition dueForSecondVisitInReportingPeriod = df.getCohortDefinitionBySql("SELECT obs.person_id FROM obs INNER JOIN (SELECT person_id,value_datetime AS artStartDate FROM obs WHERE concept_id=99165 AND  voided=0)A\n" +
                "    ON A.person_id=obs.person_id WHERE concept_id=5096 AND voided=0 AND value_datetime >=:startDate AND value_datetime<=:endDate AND DATE(artStartDate)=DATE(obs.obs_datetime)");

        CohortDefinition onTLDBeforePeriod = df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("a58d12c5-abc2-4575-8fdb-f30960f348fc")),"1d", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onTLDDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("a58d12c5-abc2-4575-8fdb-f30960f348fc")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transitionToTLD = df.getPatientsNotIn(onTLDDuringPeriod,onTLDBeforePeriod);
        CohortDefinition pregnantOrBFeeding = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getPregnant(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getYesPregnant(),hivMetadata. getLactatingAtEnrollment()), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition transitionedToTLDThroughEMTCT = df.getPatientsInAll(transitionToTLD,pregnantOrBFeeding);
        CohortDefinition transitionedToTLDThroughARTClinic = df.getPatientsNotIn(transitionToTLD,transitionedToTLDThroughEMTCT);

        CohortDefinition recentHIVInfectionDuringPeriod = hivCohortDefinitionLibrary.getPatientWithRecentHIVInfectionDuringPeriod();

        CohortDefinition onABC3TCDTGBeforePeriod = df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("6cc36637-596a-4426-92cf-170f76ea437d")),"1d", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onABC3TCDTGDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("6cc36637-596a-4426-92cf-170f76ea437d")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transitionToABC3TCDTG = df.getPatientsNotIn(onABC3TCDTGDuringPeriod,onABC3TCDTGBeforePeriod);

        CohortDefinition onABC3TCLPVRBeforePeriod = df.getPatientsWithCodedObsByEndOfPreviousDate(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("14c56659-3d4e-4b88-b3ff-e2d43dbfb865")),"1d", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition onABC3TCLPVRDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), null, Arrays.asList(Dictionary.getConcept("14c56659-3d4e-4b88-b3ff-e2d43dbfb865")), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition transitionToABC3TCLPVR = df.getPatientsNotIn(onABC3TCLPVRDuringPeriod,onABC3TCLPVRBeforePeriod);


        addIndicator(dsd,"BABIES","TX_NEW 0-11 m", TX_New,"age=below1Year");
        addIndicator(dsd,"1a","TX_NEW 1-14 between1And14female", TX_New,"age=between1And14female");
        addIndicator(dsd,"1b","TX_NEW 1-14 between1And14male", TX_New,"age=between1And14male");
        addIndicator(dsd,"2a","TX_NEW 15+ above15female", TX_New,"age=above15female");
        addIndicator(dsd,"2b","TX_NEW 15+ above15male", TX_New,"age=above15male");

        addIndicator(dsd,"3b","VMMC_CIRC <15", SMC,"age=less15male");
        addIndicator(dsd,"4b","VMMC_CIRC 15-29", SMC,"age=between15And29male");
        addIndicator(dsd,"5b","VMMC_CIRC 30", SMC,"age=above30male");

        addIndicator(dsd,"6a","TB_IPT <5", initiatedOnIPT,"age=less5Years");
        addIndicator(dsd,"7a","TB_IPT 5-14", initiatedOnIPT,"age=between5And14Years");
        addIndicator(dsd,"8a","TB_IPT 15+ females", initiatedOnIPT,"age=above15female");
        addIndicator(dsd,"8b","TB_IPT 15+ males", initiatedOnIPT,"age=above15male");

        addIndicator(dsd,"9a","TLD <15", patientsStartedOnTLDDuringPeriod,"age=less15Years");
        addIndicator(dsd,"10a","TLD 15+ females", patientsStartedOnTLDDuringPeriod,"age=above15female");
        addIndicator(dsd,"10b","TLD 15+ males", patientsStartedOnTLDDuringPeriod,"age=above15male");

        addIndicator(dsd,"11a","TLD ART <15", TLDThroughART,"age=less15Years");
        addIndicator(dsd,"12a","TLD ART 15+ females", TLDThroughART,"age=above15female");
        addIndicator(dsd,"12b","TLD ART 15+ males", TLDThroughART,"age=above15male");

        addIndicator(dsd,"13a","TLD MBCP <15", TLDThroughEMTCT,"age=less15Years");
        addIndicator(dsd,"14a","TLD MBCP 15+ females", TLDThroughEMTCT,"age=above15female");
        addIndicator(dsd,"14b","TLD MBCP 15+ males", TLDThroughEMTCT,"age=above15male");

        addIndicator(dsd,"13a","TLD MBCP <15", TLDThroughEMTCT,"age=less15Years");
        addIndicator(dsd,"14a","TLD MBCP 15+ females", TLDThroughEMTCT,"age=above15female");
        addIndicator(dsd,"14b","TLD MBCP 15+ males", TLDThroughEMTCT,"age=above15male");

        addIndicator(dsd,"15a","TLD Transitioned <15", transitionToTLD,"age=less15Years");
        addIndicator(dsd,"16a","TLD Transitioned 15+ females", transitionToTLD,"age=above15female");
        addIndicator(dsd,"16b","TLD Transitioned 15+ males", transitionToTLD,"age=above15male");

        addIndicator(dsd,"17a","TLD Transitioned ART <15", transitionedToTLDThroughARTClinic,"age=less15Years");
        addIndicator(dsd,"18a","TLD Transitioned ART 15+ females", transitionedToTLDThroughARTClinic,"age=above15female");
        addIndicator(dsd,"18b","TLD Transitioned ART 15+ males", transitionedToTLDThroughARTClinic,"age=above15male");

        addIndicator(dsd,"19a","TLD Transitioned MBCP <15", transitionedToTLDThroughEMTCT,"age=less15Years");
        addIndicator(dsd,"20a","TLD Transitioned MBCP 15+ females", transitionedToTLDThroughEMTCT,"age=above15female");
        addIndicator(dsd,"20b","TLD Transitioned MBCP 15+ males", transitionedToTLDThroughEMTCT,"age=above15male");

        addIndicator(dsd,"21a","TX_SV(D) <15", dueForSecondVisitInReportingPeriod,"age=less15female");
        addIndicator(dsd,"21b","TX_SV(D) <15", dueForSecondVisitInReportingPeriod,"age=less15male");
        addIndicator(dsd,"22a","TX_SV(D) 15+ females", dueForSecondVisitInReportingPeriod,"age=above15female");
        addIndicator(dsd,"22b","TX_SV(D) 15+ males", dueForSecondVisitInReportingPeriod,"age=above15male");

        addIndicator(dsd,"21c","TX_SV(N) <15", dueForSecondVisitInReportingPeriod,"age=less15female");
        addIndicator(dsd,"21d","TX_SV(N) <15", dueForSecondVisitInReportingPeriod,"age=less15male");
        addIndicator(dsd,"22c","TX_SV(N) 15+ females", dueForSecondVisitInReportingPeriod,"age=above15female");
        addIndicator(dsd,"22d","TX_SV(N) 15+ males", dueForSecondVisitInReportingPeriod,"age=above15male");

        addIndicator(dsd,"23a","TX_ PRO ABC/3TC/DTG <3", transitionToABC3TCDTG,"age=less3Years");
        addIndicator(dsd,"23b","TX_ PRO ABC/3TC/DTG 3-9", transitionToABC3TCDTG,"age=between3And9Years");
        addIndicator(dsd,"23c","TX_ PRO ABC/3TC/DTG 10-14", transitionToABC3TCDTG,"age=between10And14years");
        addIndicator(dsd,"23d","TX_ PRO ABC/3TC/DTG 15-19", transitionToABC3TCDTG,"age=between15And19Years");

        addIndicator(dsd,"24a","TX_ PRO ABC/3TC/LPV/r <3", transitionToABC3TCLPVR,"age=less3Years");
        addIndicator(dsd,"24b","TX_ PRO ABC/3TC/LPV/r 3-9", transitionToABC3TCLPVR,"age=between3And9Years");
        addIndicator(dsd,"24c","TX_ PRO ABC/3TC/LPV/r 10-14", transitionToABC3TCLPVR,"age=between10And14years");
        addIndicator(dsd,"24d","TX_ PRO ABC/3TC/LPV/r 15-19", transitionToABC3TCLPVR,"age=between15And19Years");

        addIndicator(dsd,"25a","TX_ PRO TLD <3", transitionToTLD,"age=less3Years");
        addIndicator(dsd,"25b","TX_ PRO TLD 3-9", transitionToTLD,"age=between3And9Years");
        addIndicator(dsd,"25c","TX_ PRO TLD 10-14", transitionToTLD,"age=between10And14years");
        addIndicator(dsd,"25d","TX_ PRO TLD 15-19", transitionToTLD,"age=between15And19Years");

        addIndicator(dsd,"26a","HTS RECENT <15 FEMALE", recentHIVInfectionDuringPeriod,"age=less15female");
        addIndicator(dsd,"26b","HTS RECENT <15 MALE", recentHIVInfectionDuringPeriod,"age=less15male");
        addIndicator(dsd,"27a","HTS RECENT 15+ females", recentHIVInfectionDuringPeriod,"age=above15female");
        addIndicator(dsd,"27b","HTS RECENT 15+ males", recentHIVInfectionDuringPeriod,"age=above15male");

        CohortDefinition patientsTestedThroughHealthFacility =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("46648b1d-b099-433b-8f9c-3815ff1e0a0f"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("ecb88326-0a3f-44a5-9bbf-df4bfc3239e1")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsTestedThroughCommunity =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("46648b1d-b099-433b-8f9c-3815ff1e0a0f"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("4f4e6d1d-4343-42cc-ba47-2319b8a84369")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition testedPositiveDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition testedPositiveBefore = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.PREVIOUS_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_POSITIVE)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition testedNegativeDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept(Metadata.Concept.CURRENT_HIV_TEST_RESULTS),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept(Metadata.Concept.HIV_NEGATIVE)),BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition testingForFirstTime = df.getPatientsWithCodedObsDuringPeriod(getConcept(Metadata.Concept.FIRST_HIV_TEST),hivMetadata.getHCTEncounterType(),Arrays.asList(getConcept(Metadata.Concept.YES_WHO)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition IndexClientTestingReasonForTesting = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("0e19ee29-a7bf-4580-9313-7853cdc412c1")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition SNSReasonForTesting = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2afe1128-c3f6-4b35-b119-d17b9b9958ed"),hivMetadata.getHCTEncounterType(),Arrays.asList(Dictionary.getConcept("b1f34616-de68-4b90-9bc4-f0397692befa")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition VCT = df.getPatientsWithCodedObsDuringPeriod(getConcept(Metadata.Concept.COUNSELLING_APPROACH),hivMetadata.getHCTEncounterType(),Arrays.asList(getConcept(Metadata.Concept.CICT)), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientThroughMaternityEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("160456AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughPNCEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("165046AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));

        CohortDefinition indexFacilityClientTesting = df.getPatientsInAll(IndexClientTestingReasonForTesting,patientsTestedThroughHealthFacility);
        CohortDefinition indexCommunityClientTesting = df.getPatientsInAll(IndexClientTestingReasonForTesting,patientsTestedThroughCommunity);
        CohortDefinition FirstPositiveTest =df.getPatientsInAll(testedPositiveDuringPeriod,testingForFirstTime);
        CohortDefinition repeatPositive = df.getPatientsInAll(testedPositiveBefore,testedPositiveDuringPeriod);

        CohortDefinition patientsTestedOnFirstANC = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329"),hivMetadata.getHCTEncounterType(), RangeComparator.LESS_EQUAL, 1.0, BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientsTestedOnOtherANCs = df.getPatientsWithNumericObsDuringPeriod(Dictionary.getConcept("c7231d96-34d8-4bf7-a509-c810f75e3329"),hivMetadata.getHCTEncounterType(), RangeComparator.GREATER_THAN, 1.0, BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PMTCT = df.getPatientsInAny(patientsTestedOnOtherANCs,patientThroughMaternityEntryPoint,patientThroughPNCEntryPoint);
        CohortDefinition VCTCommunity = df.getPatientsInAll(VCT,patientsTestedThroughCommunity);

        CohortDefinition mobile = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("4f4e6d1d-4343-42cc-ba47-2319b8a84369"),hivMetadata.getHCTEncounterType(),hivMetadata.getConceptList("29d1a223-4ce4-43df-96fc-6d53c0e022b1,6080ad91-fc24-49dd-aa5d-3ce7c1b4ce2e,03596df2-09bc-4d1f-94fd-484411ac9012"), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition patientThroughSTIClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("dcd98f72-30ab-102d-86b0-7a5022ba4115"));
        CohortDefinition patientThroughTBClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("165048AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughIPDClinicEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("c09c3d3d-d07d-4d34-84f0-89ea4fd5d6d5"));
        CohortDefinition patientThroughNutritionEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("11c12455-2f54-4bb5-b051-0ecfd4a5fe96"));

        CohortDefinition patientThroughOPDEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("160542AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughFamilyPlanningEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("164984AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"));
        CohortDefinition patientThroughYCCEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("e9469d61-b0c3-4785-81c6-057c7bc099fc"));

        CohortDefinition patientThroughSMCEntryPoint = getpatientTestedThroughFacilityEntryPoint(Dictionary.getConcept("409eae6b-9457-4896-b5fa-2667ad5ceffc"));

        fillPerIndicator(dsd,"28","29","SNS positive ist positive test",df.getPatientsInAll(SNSReasonForTesting,FirstPositiveTest));
        fillPerIndicator(dsd,"30","31","SNS positive repeat positive test",df.getPatientsInAll(SNSReasonForTesting,repeatPositive));
        fillPerIndicator(dsd,"32","33","SNS negative",df.getPatientsInAll(SNSReasonForTesting,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"34","35","Index Facility client testing positive ist positive test",df.getPatientsInAll(indexFacilityClientTesting,FirstPositiveTest));
        fillPerIndicator(dsd,"36","37","Index Facility client testing positive repeat positive test",df.getPatientsInAll(indexFacilityClientTesting,repeatPositive));
        fillPerIndicator(dsd,"38","39","Index Facility client testing negative",df.getPatientsInAll(indexFacilityClientTesting,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"40","41","PMTCT ANC 1 postive ist positive test",df.getPatientsInAll(patientsTestedOnFirstANC,FirstPositiveTest));
        fillPerIndicator(dsd,"42","43","PMTCT ANC 1 postive repeat positive test",df.getPatientsInAll(patientsTestedOnFirstANC,repeatPositive));
        fillPerIndicator(dsd,"44","45","PMTCT ANC 1 negative",df.getPatientsInAll(patientsTestedOnFirstANC,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"46","47","PMTCT post ANC postive ist positive test",df.getPatientsInAll(PMTCT,FirstPositiveTest));
        fillPerIndicator(dsd,"48","50","PMTCT post ANC postive repeat positive test",df.getPatientsInAll(PMTCT,repeatPositive));
        fillPerIndicator(dsd,"51","52","PMTCT post ANC negative",df.getPatientsInAll(PMTCT,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"53","54","Malnutrition positive ist positive test",df.getPatientsInAll(patientThroughNutritionEntryPoint,FirstPositiveTest));
        fillPerIndicator(dsd,"55","56","Malnutrition positive repeat positive test",df.getPatientsInAll(patientThroughNutritionEntryPoint,repeatPositive));
        fillPerIndicator(dsd,"57","58","Malnutrition negative",df.getPatientsInAll(patientThroughNutritionEntryPoint,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"59","60","Pediatric positive ist positive test",df.getPatientsInAll(patientThroughYCCEntryPoint,FirstPositiveTest));
        fillPerIndicator(dsd,"61","62","Pediatric positive repeat positive test",df.getPatientsInAll(patientThroughYCCEntryPoint,repeatPositive));
        fillPerIndicator(dsd,"63","64","Pediatric negative",df.getPatientsInAll(patientThroughYCCEntryPoint,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"65","66","TB Clinic positive ist positive test",df.getPatientsInAll(patientThroughTBClinicEntryPoint,FirstPositiveTest));
        fillPerIndicator(dsd,"67","68","TB Clinic positive repeat positive test",df.getPatientsInAll(patientThroughTBClinicEntryPoint,repeatPositive));
        fillPerIndicator(dsd,"69","70","TB Clinic negative",df.getPatientsInAll(patientsTestedOnFirstANC,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"71","72","Other PITC positive ist positive test",df.getPatientsInAll(patientThroughOPDEntryPoint,FirstPositiveTest));
        fillPerIndicator(dsd,"73","74","Other PITC positive repeat positive test",df.getPatientsInAll(patientThroughOPDEntryPoint,repeatPositive));
        fillPerIndicator(dsd,"75","76","Other PITC negative",df.getPatientsInAll(patientThroughOPDEntryPoint,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"77","78","Index Community testing positive ist positive test",df.getPatientsInAll(indexCommunityClientTesting,FirstPositiveTest));
        fillPerIndicator(dsd,"79","80","Index Community testing positive repeat positive test",df.getPatientsInAll(indexCommunityClientTesting,repeatPositive));
        fillPerIndicator(dsd,"81","82","Index Community testing negative",df.getPatientsInAll(indexCommunityClientTesting,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"83","84","VCT Community positive ist positive test",df.getPatientsInAll(VCTCommunity,FirstPositiveTest));
        fillPerIndicator(dsd,"85","86","VCT Community positive repeat positive test",df.getPatientsInAll(VCTCommunity,repeatPositive));
        fillPerIndicator(dsd,"87","88","VCT Community negative",df.getPatientsInAll(VCTCommunity,testedNegativeDuringPeriod));

        fillPerIndicator(dsd,"89","90","Mobile Community positive ist positive test",df.getPatientsInAll(mobile,FirstPositiveTest));
        fillPerIndicator(dsd,"91","92","Mobile Community positive repeat positive test",df.getPatientsInAll(mobile,repeatPositive));
        fillPerIndicator(dsd,"93","94","Mobile Community negative",df.getPatientsInAll(mobile,testedNegativeDuringPeriod));
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

    private void fillPerIndicator(CohortIndicatorDataSetDefinition dsd,String rowIndicator1,String rowIndicator2,String label,CohortDefinition cohortDefinition){
        addIndicator(dsd,rowIndicator1+"a",label+"less15female", cohortDefinition,"age=less15female");
        addIndicator(dsd,rowIndicator1+"b",label+"less15male", cohortDefinition,"age=less15male");
        addIndicator(dsd,rowIndicator2+"a",label+"above15female", cohortDefinition,"age=above15female");
        addIndicator(dsd,rowIndicator2+"b",label+"above15male", cohortDefinition,"age=above15male");
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
        return "0.1.7.1";
    }
}