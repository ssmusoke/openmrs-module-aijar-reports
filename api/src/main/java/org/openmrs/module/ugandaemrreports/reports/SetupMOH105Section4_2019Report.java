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
 * HTS Section 4 Report report
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
        return "ae528aaa-fddc-444b-a0e3-c466cbbc5a3c ";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS105Section4_2019.xls");
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());



        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("E", Mapped.mapStraightThrough(eid()));
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

        //start building the columns for the report
        addRowWithColumns(dsd, "4.HT1A","HT1A-Number of individuals with Ward as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithWardHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT1B","HT1B-Number of individuals with Ward as  HTC Entry Point and tested Positive", indicatorLibrary.individualsWithWardHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT1C","HT1C-Number of individuals with Ward as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithWardHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT2A","HT2A-Number of individuals with OPD as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithOPDHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT2B","HT2B-Number of individuals with OPD as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithOPDHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT2C","HT2C-Number of individuals with OPD as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithOPDHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT3A","HT3A-Number of individuals with ART CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithART_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT3B","HT3B-Number of individuals with ART CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithART_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT3C","HT3C-Number of individuals with ART CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithART_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT4A","HT4A-Number of individuals with TB CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithTB_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT4B","HT4B-Number of individuals with TB CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithTB_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT4C","HT4C-Number of individuals with TB CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithTB_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT5A","HT5A-Number of individuals with NUTRITION UNIT CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithNUTRITION_UNIT_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT5B","HT5B-Number of individuals with NUTRITION UNIT CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithNUTRITION_UNIT_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT5C","HT5C-Number of individuals with NUTRITION UNIT CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithNUTRITION_UNIT_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT6A","HT6A-Number of individuals with STI UNIT CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithSTI_UNIT_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT6B","HT6B-Number of individuals with STI UNIT CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithSTI_UNIT_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT6C","HT6C-Number of individuals with STI UNIT CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithSTI_UNIT_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT7A","HT7A-Number of individuals with YCC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithYCC_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT7B","HT7B-Number of individuals with YCC CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithYCC_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT7C","HT7C-Number of individuals with YCC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithYCC_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT8A","HT8A-Number of individuals with ANC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithANC_CLINICHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT8B","HT8B-Number of individuals with ANC CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithANC_CLINICHCTEntryPointandTestedPostive());
        addRowWithColumns(dsd, "4.HT8C","HT8C-Number of individuals with ANC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithANC_CLINICHCTEntryPointandLinkedToCare());

        addRowWithColumns(dsd, "4.HT9A","HT9A-Number of individuals with MATERNITY CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithMaternityDeptAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT9B","HT9B-Number of individuals with MATERNITY CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithMaternityDeptAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "4.HT9C","HT9C-Number of individuals with MATERNITY CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithMaternityDeptAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT10A","HT10A-Number of individuals with PNC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithPNCAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT10B","HT10B-Number of individuals with PNC CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithPNCAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "4.HT10C","HT10C-Number of individuals with PNC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithPNCAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT11A","HT11A-Number of individuals with Family Planning CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithFamilyPlanningAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT11B","HT11B-Number of individuals with Family Planning CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithFamilyPlanningAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "4.HT11C","HT11C-Number of individuals with Family Planning CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithFamilyPlanningAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT12A","HT12A-Number of individuals with SMC CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithSMCAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT12B","HT12B-Number of individuals with SMC CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithSMCAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "4.HT12C","HT12C-Number of individuals with SMC CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithSMCAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT13A","HT13A-Number of individuals with EID CLINIC as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithEIDAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT13B","HT13B-Number of individuals with EID CLINIC as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithEIDAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "4.HT13C","HT13C-Number of individuals with EID CLINIC as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithEIDAsHCTEntryPointandLinkedtoCare());

        addRowWithColumns(dsd, "4.HT14A","HT14A-Number of individuals with Other Facility Based Entry Points  as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithOtherFacilityBasedEntryPointsAsHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "4.HT14B","HT14B-Number of individuals with Other Facility Based Entry Points  as HTC Entry Point and tested Positive", indicatorLibrary.individualsWithOtherFacilityBasedEntryPointsAsHCTEntryPointandTestedPositive());
        addRowWithColumns(dsd, "4.HT14C","HT14C-Number of individuals with Other Facility Based Entry Points  as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithOtherFacilityBasedEntryPointsAsHCTEntryPointandLinkedtoCare());




        addRowWithColumns(dsd, "4.HT11","HT11-Number of individuals with Family Planning as  HTC Entry Point", indicatorLibrary.individualsWithFamilyPlanningDeptAsHCTEntryPoint());
        addRowWithColumns(dsd, "4.HT16","HT16-Number of individuals with Work Place as  HTC Entry Point", indicatorLibrary.individualsWithWorkPlaceAsHCTEntryPoint());
        addRowWithColumns(dsd, "4.HT17","HT17-Number of individuals with HBCT as  HTC Entry Point", indicatorLibrary.individualsWithHBCTAsHCTEntryPoint());





        addRowWithColumns(dsd, "4.H1","H1-Number of Individuals counseled", indicatorLibrary.individualsCounselled());
        addRowWithColumns(dsd, "4.H2","H2-Number of Individuals tested", indicatorLibrary.individualsTested());
        addRowWithColumns(dsd, "4.H3","H3-Number of Individuals who received HIV test results", indicatorLibrary.individualsWhoReceivedHIVTestResults());
        addRowWithColumns(dsd, "4.H4","H4- Number of individuals who received HIV results in the last 12months", indicatorLibrary.individualsWhoReceivedHIVTestResultsInLast12Months());
        addRowWithColumns(dsd, "4.H5","H5-Number of individuals tested for the first time", indicatorLibrary.individualsTestedForTheFirstTime());
        addRowWithColumns(dsd, "4.H6","H6-Number of Individuals who tested HIV positive", indicatorLibrary.individualsWhoTestedHivPositive());
        addRowWithColumns(dsd, "4.H7","H7-HIV positive individuals with presumptive TB", indicatorLibrary.individualsWhoTestedHivPositiveAndWithPresumptiveTb());
        addRowWithColumns(dsd, "4.H8","H8-Number of Individuals tested more than twice in the last 12 months", indicatorLibrary.individualsTestedMoreThanTwiceInLast12Months());
        addRowWithColumns(dsd, "4.H9","H9-Number of individuals who were Counseled and Tested together as a Couple", indicatorLibrary.individualsTestedAndReceivedResultsAsACouple());
        addRowWithColumns(dsd, "4.H10","H10-Number of individuals who were Tested and Received results together as a Couple", indicatorLibrary.individualsTestedAndReceivedResultsAsACouple());
        addRowWithColumns(dsd, "4.H11","H11-Number of couples with Concordant positive results", indicatorLibrary.couplesWithConcordantPositiveResults());
        addRowWithColumns(dsd, "4.H12","H12- Number of couples with Discordant results", indicatorLibrary.couplesWithDiscordantResults());
        addRowWithColumns(dsd, "4.H13","H13-Individuals counseled and tested for PEP", indicatorLibrary.individualsCounselledAndTestedForPep());
        addRowWithColumns(dsd, "4.H15","H15-Number of positive individuals who tested at an early stage (CD4>500µ)", indicatorLibrary.hivPositiveIndividualsTestedAtAnEarlyStage());
        addRowWithColumns(dsd, "4.H16","H16-Number of clients who have been linked to care", indicatorLibrary.clientsLinkedToCare());

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
        return "2.0.2.3.6.7.5";
    }
}
