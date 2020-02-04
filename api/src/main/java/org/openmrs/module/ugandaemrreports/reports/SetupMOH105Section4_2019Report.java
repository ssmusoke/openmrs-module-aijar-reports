package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.period;
import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;

/**
 * Daily Appointments List report
 */
@Component

public class SetupMOH105Section4_2019Report extends UgandaEMRDataExportManager {

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
    private CommonReportDimensionLibrary dimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;

    private static final String PARAMS = "startDate=${startDate},endDate=${endDate}";


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "ae528aaa-fddc-444b-a0e3-c466cbbc5a3c";
    }
    public String getJSONDesignUuid() {
        return "fecd3eaf-4417-4ede-8e69-b9b91bfe69cc";
    }

    @Override
    public String getUuid() {
        return "27f4804f-ec6f-466e-b4ea-21f9ca584880";
    }

    @Override
    public String getName() {
        return "Section 4 HCT";
    }

    @Override
    public String getDescription() {
        return "Section 4 HCT";
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
        List<ReportDesign> l = new ArrayList<ReportDesign>();
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
        ReportDesign rd = createExcelTemplateDesign("04c3d0f2-fd33-4b48-ada7-fc78346917b3", reportDefinition, "HMIS105Section4_2019.xls");
        return rd;
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "HMIS105Section4_2019.json");
        return rd;
    }


    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding
     * properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */



    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("105", Mapped.mapStraightThrough(eid()));
        rd.addDataSetDefinition("P", Mapped.mapStraightThrough(period()));
        return rd;

    }

    protected DataSetDefinition eid() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.HTCAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        /**
         * Health Facility Testing Points
         */
        addRowWithColumns(dsd, "HT01A1","HT1A-Number of individuals with Ward as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithWardHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01A2","HT1B-Number of individuals with Ward as  HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithWardHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "HT01A3","HT1C-Number of individuals with Ward as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithWardHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01B1","HT2A-Number of individuals with OPD as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithOPDHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01B2","HT2B-Number of individuals with OPD as HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithOPDHCTEntryPointandnewlyPostive());
        addRowWithColumns(dsd, "HT01B3","HT2C-Number of individuals with OPD as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithOPDHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01C1","HT3A-Number of individuals with ART CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithART_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01C2","HT3B-Number of individuals with ART CLINIC as HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithART_CLINICHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "HT01C3","HT3C-Number of individuals with ART CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithART_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01D1","HT4A-Number of individuals with TB CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithTB_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01D2","HT4B-Number of individuals with TB CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithTB_CLINICHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "HT01D3","HT4C-Number of individuals with TB CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithTB_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01E1","HT5A-Number of individuals with NUTRITION UNIT CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithNUTRITION_UNIT_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01E2","HT5B-Number of individuals with NUTRITION UNIT CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithNUTRITION_UNIT_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "HT01E3","HT5C-Number of individuals with NUTRITION UNIT CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithNUTRITION_UNIT_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01F1","HT6A-Number of individuals with STI UNIT CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithSTI_UNIT_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01F2","HT6B-Number of individuals with STI UNIT CLINIC as HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithSTI_UNIT_CLINICHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "HT01F3","HT6C-Number of individuals with STI UNIT CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithSTI_UNIT_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01G1","HT7A-Number of individuals with YCC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithYCC_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01G2","HT7B-Number of individuals with YCC CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithYCC_CLINICHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "HT01G3","HT7C-Number of individuals with YCC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithYCC_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01H1","HT8A-Number of individuals with ANC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithANC_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01H2","HT8B-Number of individuals with ANC CLINIC as HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithANC_CLINICHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "HT01H3","HT8C-Number of individuals with ANC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithANC_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "HT01I1","HT9A-Number of individuals with MATERNITY CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithMaternityDeptAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01I2","HT9B-Number of individuals with MATERNITY CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithMaternityDeptAsHCTEntryPointandNewlyPositive());
        addRowWithColumns(dsd, "HT01I3","HT9C-Number of individuals with MATERNITY CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithMaternityDeptAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT01J1","HT10A-Number of individuals with PNC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithPNCAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01J2","HT10B-Number of individuals with PNC CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithPNCAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT01J3","HT10C-Number of individuals with PNC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithPNCAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT01K1","HT11A-Number of individuals with Family Planning CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithFamilyPlanningAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01K2","HT11B-Number of individuals with Family Planning CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithFamilyPlanningAsHCTEntryPointandNewlyPositive());
        addRowWithColumns(dsd, "HT01K3","HT11C-Number of individuals with Family Planning CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithFamilyPlanningAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT01L1","HT12A-Number of individuals with SMC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithSMCAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01L2","HT12B-Number of individuals with SMC CLINIC as HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithSMCAsHCTEntryPointandNewlyPositive());
        addRowWithColumns(dsd, "HT01L3","HT12C-Number of individuals with SMC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithSMCAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT01M1","HT13A-Number of individuals with EID CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithEIDAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01M2","HT13B-Number of individuals with EID CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithEIDAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT01M3","HT13C-Number of individuals with EID CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithEIDAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT01N1","HT14A-Number of individuals with Other Facility Based Entry Points  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithOtherFacilityBasedEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT01N2","HT14B-Number of individuals with Other Facility Based Entry Points  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithOtherFacilityBasedEntryPointsAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT01N3","HT14C-Number of individuals with Other Facility Based Entry Points  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithOtherFacilityBasedEntryPointsAsHCTEntryPointandLinkedtoCare());

        /**
         * Community testing Points for HCT

         */
        addRowWithColumns(dsd, "HT02A1","HT15A-Number of individuals with Work Place  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithWorkPlaceEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT02A2","HT15B-Number of individuals with Work Place  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithWorkPlaceEntryPointsAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT02A3","HT15C-Number of individuals with Work Place  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithWorkPlaceEntryPointsAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT02B1","HT16A-Number of individuals with HBCT  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithHBCTEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT02B2","HT16B-Number of individuals with HBCT  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithHBCTEntryPointsAsHCTEntryPointandNewlyPositive());
        addRowWithColumns(dsd, "HT02B3","HT16C-Number of individuals with HBCT  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithHBCTEntryPointsAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT02C1","HT17A-Number of individuals with DIC  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithDICEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT02C2","HT17B-Number of individuals with DIC  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithDICEntryPointsAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT02C3","HT17C-Number of individuals with DIC  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithDICEntryPointsAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT02D1","HT18A-Number of individuals with HotSpots  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithHotSpotsEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT02D2","HT18B-Number of individuals with HotSpots  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithHotSpotsEntryPointsAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT02D3","HT18C-Number of individuals with HotSpots  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithHotSpotsEntryPointsAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "HT02E1","HT19A-Number of individuals with Other Community testing Points  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithOtherCommunityestingPointsEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "HT02E2","HT19B-Number of individuals with Other Community testing Points  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithOtherCommunityestingPointsEntryPointsAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "HT02E3","HT19C-Number of individuals with Other Community testing Points  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithOtherCommunityestingPointsEntryPointsAsHCTEntryPointandLinkedtoCare());


        /**
         * Number of Individuals Tested, New HIV Positive, Linked to Care
         */
        addRowWithColumns(dsd, "HT03A1","HT20A-Total Number of Individual  who tested for HIV", indicatorLibrary.clientsTestedForHIV());
        addRowWithColumns(dsd, "HT03A2","HT20B-Total Number of Individual  tested  positive ", indicatorLibrary.clientsNewlyPositive());
        addRowWithColumns(dsd, "HT03A3","HT20C-Total Number of Individual linked to care  ", indicatorLibrary.clientsLinkedToCare());

        /**
         * Number of individuals by PITC Facility Approach
         */
        addRowWithColumns(dsd, "HT04A1","HT21A-Number of individuals by  PITC Health Facility Testing Approach and Tested For HIV", indicatorLibrary.individualswithHealthFacilityAsPITCTestingApproachAndTestedForHIV());
        addRowWithColumns(dsd, "HT04A2","HT21B-Number of individuals  by PITC Health Facility Testing Approach and Newly Positive  ", indicatorLibrary.individualswithHealthFacilityAsPITCTestingApproachAndNewlyPositive());
        addRowWithColumns(dsd, "HT04A3","HT21C-Number of individuals by PITC Health Facility Testing Approach and Linked to Care", indicatorLibrary.individualswithHealthFacilityAsPITCTestingApproachAndLinkedToCare());

            /**
             * Number of Individuals by PITC Community Approach
             */
        addRowWithColumns(dsd, "HT04B1","HT22A-Number of individuals by  PITC Community  Testing Approach ", indicatorLibrary.individualswithCommunityAsPITCTestingApproachAndTestedForHIV());
        addRowWithColumns(dsd, "HT04B2","HT22B-Number of individuals  by PITC Community  Testing Approach ", indicatorLibrary.individualswithCommunityAsPITCTestingApproachAndTestedPositive());
        addRowWithColumns(dsd, "HT04B3","HT22C-Number of individuals by PITC Community  Testing Approach", indicatorLibrary.individualswithCommunityAsPITCTestingApproachAndLinkedToCare());

        /**
         * Number of Individuals by CICT/VICT Approach
         */
        addRowWithColumns(dsd, "HT05A1","HT23A-Number of individuals by  CICT/VCT Facility Based  Testing Approach ", indicatorLibrary.individualswithFacilityBasedCICTTestingApproachAndTestedForHIV());
        addRowWithColumns(dsd, "HT05A2","HT23B-Number of individuals  by CICT/VCT Facility Based  Testing Approach ", indicatorLibrary.individualswithFacilityBasedCICTTestingApproachAndTestedPositive());
        addRowWithColumns(dsd, "HT05A3","HT23C-Number of individuals by CICT/VCT Facility Based  Testing Approach", indicatorLibrary.individualswithFacilityBasedCICTTestingApproachAndLinkedToCare());

        addRowWithColumns(dsd, "HT05B1","HT24A-Number of individuals by  CICT/VCT  Community  Testing Approach ", indicatorLibrary.individualswithCommunityCICTTestingApproachAndTestedForHIV());
        addRowWithColumns(dsd, "HT05B2","HT24B-Number of individuals  by CICT/VCT  Community  Testing Approach ", indicatorLibrary.individualswithCommunityCICTTestingApproachAndTestedPositive());
        addRowWithColumns(dsd, "HT05B3","HT24C-Number of individuals by CICT/VCT  Community  Testing Approach", indicatorLibrary.individualswithCommunityCICTTestingApproachAndLinkedToCare());

        /**
         * NUmber of Individuals by special Category
         */
        addRowWithColumns(dsd, "4.HT25A","HT25A-Number of Prisoners", indicatorLibrary.indivudualsCategorisedAsPrisonersandTestedForHIV());
        addRowWithColumns(dsd, "4.HT25B","HT25B-Number of Prisoners ", indicatorLibrary.indivudualsCategorisedAsPrisonersandNewlyPositive());
        addRowWithColumns(dsd, "4.HT25C","HT25C-Number of Prisoners", indicatorLibrary.indivudualsCategorisedAsPrisonersandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT26A","HT25A-Number of PWIDs", indicatorLibrary.indivudualsCategorisedAsPWIDsandTestedForHIV());
        addRowWithColumns(dsd, "4.HT26B","HT25B-Number of PWIDs ", indicatorLibrary.indivudualsCategorisedAsPWIDsandNewlyPositive());
        addRowWithColumns(dsd, "4.HT26C","HT25C-Number of PWIDs", indicatorLibrary.indivudualsCategorisedAsPWIDsandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT27A","HT27A-Number of Uniformed Men", indicatorLibrary.indivudualsCategorisedAsUniformedMenandTestedForHIV());
        addRowWithColumns(dsd, "4.HT27B","HT27B-Number of Uniformed Men ", indicatorLibrary.indivudualsCategorisedAsUniformedMenandNewlyPositive());
        addRowWithColumns(dsd, "4.HT27C","HT27C-Number of Uniformed Men", indicatorLibrary.indivudualsCategorisedAsUniformedMensandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT28A","HT28A-Number of Migrant Workers", indicatorLibrary.indivudualsCategorisedAsMigrantWorkersandTestedForHIV());
        addRowWithColumns(dsd, "4.HT28B","HT28B-Number of Migrant Workers ", indicatorLibrary.indivudualsCategorisedAsMigrantWorkersandTestedPositive());
        addRowWithColumns(dsd, "4.HT28C","HT28C-Number of Migrant Workers", indicatorLibrary.indivudualsCategorisedAsMigrantWorkerssandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT29A","HT29A-Number of Truckers", indicatorLibrary.indivudualsCategorisedAsTruckerDriversandTestedForHIV());
        addRowWithColumns(dsd, "4.HT29B","HT29B-Number of Truckers ", indicatorLibrary.indivudualsCategorisedAsTruckerDriversandTestedPositive());
        addRowWithColumns(dsd, "4.HT29C","HT29C-Number of Truckers", indicatorLibrary.indivudualsCategorisedAsTruckerDriverssandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT30A","HT30A-Number of Fisher Forks", indicatorLibrary.indivudualsCategorisedAsFisherFolksandTestedForHIV());
        addRowWithColumns(dsd, "4.HT30B","HT30B-Number of Fisher Forks ", indicatorLibrary.indivudualsCategorisedAsFisherFolksandTestedPositive());
        addRowWithColumns(dsd, "4.HT30C","HT30C-Number of Fisher Forks", indicatorLibrary.indivudualsCategorisedAsFisherFolkssandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT31A","HT31A-Number of Refugees", indicatorLibrary.indivudualsCategorisedAsRefugeesandTestedForHIV());
        addRowWithColumns(dsd, "4.HT31B","HT31B-Number of Refugees ", indicatorLibrary.indivudualsCategorisedAsRefugeesandTestedPositive());
        addRowWithColumns(dsd, "4.HT31C","HT31C-Number of Refugees", indicatorLibrary.indivudualsCategorisedAsRefugeessandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT32A","HT32A-Number of Pregnant Women", indicatorLibrary.indivudualsCategorisedAsPregnantWomenandTestedForHIV());
        addRowWithColumns(dsd, "4.HT32B","HT32B-Number of Pregnant Women ", indicatorLibrary.indivudualsCategorisedAsPregnantWomenandTestedPositive());
        addRowWithColumns(dsd, "4.HT32C","HT32C-Number of Pregnant Women", indicatorLibrary.indivudualsCategorisedAsPregnantWomenandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT33A","HT33A-Number of BreastFeeding Women", indicatorLibrary.indivudualsCategorisedAsBreastFeedingWomenandTestedForHIV());
        addRowWithColumns(dsd, "4.HT33B","HT33B-Number of BreastFeeding Women ", indicatorLibrary.indivudualsCategorisedAsBreastFeedingWomenandTestedPositive());
        addRowWithColumns(dsd, "4.HT33C","HT33C-Number of BreastFeeding Women", indicatorLibrary.indivudualsCategorisedAsBreastFeedingWomenandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT34A","HT34A-Number of AGYW", indicatorLibrary.indivudualsCategorisedAsAGYWandTestedForHIV());
        addRowWithColumns(dsd, "4.HT34B","HT34B-Number of AGYW ", indicatorLibrary.indivudualsCategorisedAsAGYWandTestedPositive());
        addRowWithColumns(dsd, "4.HT34C","HT34C-Number of AGYW", indicatorLibrary.indivudualsCategorisedAsAGYWandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT35A","HT35A-Number of PWDs", indicatorLibrary.indivudualsCategorisedAsPWDsandTestedForHIV());
        addRowWithColumns(dsd, "4.HT35B","HT35B-Number of PWDs ", indicatorLibrary.indivudualsCategorisedAsPWDsandTestedPositive());
        addRowWithColumns(dsd, "4.HT35C","HT35C-Number of PWDs", indicatorLibrary.indivudualsCategorisedAsPWDsandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT36A","HT36A-Number of Others", indicatorLibrary.indivudualsCategorisedAsOthersandTestedForHIV());
        addRowWithColumns(dsd, "4.HT36B","HT36B-Number of Others ", indicatorLibrary.indivudualsCategorisedAsOthersandTestedPositive());
        addRowWithColumns(dsd, "4.HT36C","HT36C-Number of Others", indicatorLibrary.indivudualsCategorisedAsOthersandLinkedtoCare());



        addRowWithColumns(dsd, "4.HT38A","HT38A-Total Number of Partners tested", indicatorLibrary.totalNumberofPartnersTested());
        addRowWithColumns(dsd, "4.HT38B","HT38B-Total Number of Partners tested Positive ", indicatorLibrary.partnersTestedHIVPositive());




        addRowWithColumns(dsd, "4.H3","H3-Number of Individuals who received HIV test results", indicatorLibrary.individualsWhoReceivedHIVTestResults());
        addRowWithColumns(dsd, "4.H5","H5-Number of individuals tested for the first time", indicatorLibrary.individualsTestedForTheFirstTime());
        addRowWithColumns(dsd, "4.H6","H6-Number of Individuals who tested HIV positive", indicatorLibrary.individualsWhoTestedHivPositive());
        addRowWithColumns(dsd, "4.H14","H14-Individuals With Long Term test Results", indicatorLibrary.individualswithLongTermTestsResults());
        addRowWithColumns(dsd, "4.H15","H15-Individuals With Recent  test Results", indicatorLibrary.individualswithRecentTestsResults());

        addRowWithColumns(dsd, "4.H4","H4- Number of individuals who received HIV results in the last 12months", indicatorLibrary.individualsWhoReceivedHIVTestResultsInLast12Months());
        addRowWithColumns(dsd, "4.H1","H1-Number of Individuals counseled", indicatorLibrary.individualsCounselled());
        addRowWithColumns(dsd, "4.H2","H2-Number of Individuals tested", indicatorLibrary.individualsTested());
        addRowWithColumns(dsd, "4.H7","H7-HIV positive individuals with presumptive TB", indicatorLibrary.individualsWhoTestedHivPositiveAndWithPresumptiveTb());
        addRowWithColumns(dsd, "4.H8","H8-Number of Individuals tested more than twice in the last 12 months", indicatorLibrary.individualsTestedMoreThanTwiceInLast12Months());
        addRowWithColumns(dsd, "4.H9","H9-Number of individuals who were Counseled and Tested together as a Couple", indicatorLibrary.individualsCounseledAndTestedAsCouple());
        addRowWithColumns(dsd, "4.H10","H10-Number of individuals who were Tested and Received results together as a Couple", indicatorLibrary.individualsTestedAndReceivedResultsAsACouple());
        addRowWithColumns(dsd, "4.H11","H11-Number of couples with Concordant positive results", indicatorLibrary.couplesWithConcordantPositiveResults());
        addRowWithColumns(dsd, "4.H12","H12- Number of couples with Discordant results", indicatorLibrary.couplesWithDiscordantResults());
        addRowWithColumns(dsd, "4.H13","H13-Individuals counseled and tested for PEP", indicatorLibrary.individualsCounselledAndTestedForPep());


//        addRowWithColumns(dsd, "4.H15","H15-Number of positive individuals who tested at an early stage (CD4>500µ)", indicatorLibrary.hivPositiveIndividualsTestedAtAnEarlyStage());
//        addRowWithColumns(dsd, "4.H16","H16-Number of clients who have been linked to care", indicatorLibrary.clientsLinkedToCare());

        return dsd;
    }

    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {

        addIndicator(dsd, key + "aM", label + " (Below 1 year) Male", cohortIndicator, "gender=M|age=Below1yr");
        addIndicator(dsd, key + "aF", label + " (Below 1 year) Female", cohortIndicator, "gender=F|age=Below1yr");
        addIndicator(dsd, key + "bM", label + " (Between 1 and 4 Years) Male", cohortIndicator, "gender=M|age=Between1And4yrs");
        addIndicator(dsd, key + "bF", label + " (Between  1 and 4 Years) Female", cohortIndicator, "gender=F|age=Between1And4yrs");
        addIndicator(dsd, key + "lM", label + " (Between 5 and 9 Years) Male", cohortIndicator, "gender=M|age=Between5And9yrs");
        addIndicator(dsd, key + "lF", label + " (Between 5 and 9 Years) Female", cohortIndicator, "gender=F|age=Between5And9yrs");
        addIndicator(dsd, key + "cM", label + " (Between 10 and 14 Years) Male", cohortIndicator, "gender=M|age=Between10And14yrs");
        addIndicator(dsd, key + "cF", label + " (Between 10 and 14 Years) Female", cohortIndicator, "gender=F|age=Between10And14yrs");
        addIndicator(dsd, key + "dM", label + " (Between 15 and 19 Years) Male", cohortIndicator, "gender=M|age=Between15And19yrs");
        addIndicator(dsd, key + "dF", label + " (Between 15 and 19 Years) Female", cohortIndicator, "gender=F|age=Between15And19yrs");
        addIndicator(dsd, key + "eM", label + " (Between 20 and 24 Years) Male", cohortIndicator, "gender=M|age=Between20And24yrs");
        addIndicator(dsd, key + "eF", label + " (Between 20 and 24 Years) Female", cohortIndicator, "gender=F|age=Between20And24yrs");
        addIndicator(dsd, key + "mM", label + " (Between 25 and 29 Years) Male", cohortIndicator, "gender=M|age=Between25And29yrs");
        addIndicator(dsd, key + "mF", label + " (Between 25 and 29 Years) Female", cohortIndicator, "gender=F|age=Between25And29yrs");
        addIndicator(dsd, key + "fM", label + " (Between 30 and 34 Years) Male", cohortIndicator, "gender=M|age=Between30And34yrs");
        addIndicator(dsd, key + "fF", label + " (Between 30 and 34 Years) Female", cohortIndicator, "gender=F|age=Between30And34yrs");
        addIndicator(dsd, key + "gM", label + " (Between 35 and 39 Years) Male", cohortIndicator, "gender=M|age=Between35And39yrs");
        addIndicator(dsd, key + "gF", label + " (Between 35 and 39 Years) Female", cohortIndicator, "gender=F|age=Between35And39yrs");
        addIndicator(dsd, key + "hM", label + " (Between 40 and 44 Years) Male", cohortIndicator, "gender=F|age=Between40And44yrs");
        addIndicator(dsd, key + "hF", label + " (Between 40 and 44 Years) Female", cohortIndicator, "gender=F|age=Between40And44yrs");
        addIndicator(dsd, key + "jM", label + " (Between 45 and 49 Years) Male", cohortIndicator, "gender=M|age=Between45And49yrs");
        addIndicator(dsd, key + "jF", label + " (Between 45 and 49 Years) Female", cohortIndicator, "gender=F|age=Between45And49yrs");
        addIndicator(dsd, key + "kM", label + " (>50) Male", cohortIndicator, "gender=M|age=GreaterThan50yrs");
        addIndicator(dsd, key + "kF", label + " (>50) Female", cohortIndicator, "gender=F|age=GreaterThan50yrs");
        addIndicator(dsd, key + "g", label + " (Total) ", cohortIndicator, "");     

    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator, String dimensionOptions) {
        dsd.addColumn(key, label, ReportUtils.map(cohortIndicator, PARAMS), dimensionOptions);

    }

    @Override
    public String getVersion() {
        return "4.0.4";
    }
}
