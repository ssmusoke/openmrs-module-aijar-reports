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
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.htcAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        //start building the columns for the report
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
        addRowWithColumns(dsd, "4.H14","H14-Number of individuals tested as MARPS", indicatorLibrary.individualsCounselledAndTestedMarps());
        addRowWithColumns(dsd, "4.H15","H15-Number of positive individuals who tested at an early stage (CD4>500µ)", indicatorLibrary.hivPositiveIndividualsTestedAtAnEarlyStage());
        addRowWithColumns(dsd, "4.H16","H16-Number of clients who have been linked to care", indicatorLibrary.clientsLinkedToCare());

        return dsd;
    }

    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {

        addIndicator(dsd, key + "aM", label + " (Between 18 Months and 4 Years) Male", cohortIndicator, "gender=M|age=Between18MonthsAnd4Years");
        addIndicator(dsd, key + "aF", label + " (Between 18 Months and 4 Years) Female", cohortIndicator, "gender=F|age=Between18MonthsAnd4Years");
        addIndicator(dsd, key + "bM", label + " (Between 5 and 9 Years) Male", cohortIndicator, "gender=M|age=Between5And9Yrs");
        addIndicator(dsd, key + "bF", label + " (Between 5 and 9 Years) Female", cohortIndicator, "gender=F|age=Between5And9Yrs");
        addIndicator(dsd, key + "cM", label + " (Between 10 and 14 Years) Male", cohortIndicator, "gender=M|age=Between10And14Yrs");
        addIndicator(dsd, key + "cF", label + " (Between 10 and 14 Years) Female", cohortIndicator, "gender=F|age=Between10And14Yrs");
        addIndicator(dsd, key + "dM", label + " (Between 15 and 18 Years) Male", cohortIndicator, "gender=M|age=Between15And18Yrs");
        addIndicator(dsd, key + "dF", label + " (Between 15 and 18 Years) Female", cohortIndicator, "gender=F|age=Between15And18Yrs");
        addIndicator(dsd, key + "eM", label + " (Between 19 and 49 Years) Male", cohortIndicator, "gender=M|age=Between19And49Yrs");
        addIndicator(dsd, key + "eF", label + " (Between 19 and 49 Years) Female", cohortIndicator, "gender=F|age=Between19And49Yrs");
        addIndicator(dsd, key + "fM", label + " (>49) Male", cohortIndicator, "gender=M|age=GreaterThan49Yrs");
        addIndicator(dsd, key + "fF", label + " (>49) Female", cohortIndicator, "gender=F|age=GreaterThan49Yrs");
        addIndicator(dsd, key + "g", label + " (Total) ", cohortIndicator, "");

    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator, String dimensionOptions) {
        dsd.addColumn(key, label, ReportUtils.map(cohortIndicator, PARAMS), dimensionOptions);
    }


    @Override
    public String getVersion() {
        return "2.0";
    }
}
