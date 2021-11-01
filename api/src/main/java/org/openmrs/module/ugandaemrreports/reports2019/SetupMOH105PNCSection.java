package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.dimension.CommonReportDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.period;
import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;


@Component

public class SetupMOH105PNCSection extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;


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
        return "aa23cd2b1-ca83-47fb-9846-b16294fc03d6";
    }


    @Override
    public String getUuid() {
        return "ea0cc959-d7a4-4fe8-a85a-a684da5a836f";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 2.3: POSTNATAL ";
    }

    @Override
    public String getDescription() {
        return "HMIS 105 Section 2.3: POSTNATAL";
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
//        l.add(buildJSONReportDesign(reportDefinition));

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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS_105_Postnatal_Section.xls");
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
        rd.addDataSetDefinition("105", Mapped.mapStraightThrough(postNatalDataSetDefinition()));
        rd.addDataSetDefinition("P", Mapped.mapStraightThrough(period()));
        return rd;

    }

    protected DataSetDefinition postNatalDataSetDefinition() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.MCHAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        addRowWithColumns(dsd, "PN01","Total Number of Postnatal Visits by age groups",indicatorLibrary.postnatalAdmissions());
        addRowWithColumns(dsd, "PN02","PNC Timinng at 24 hours",indicatorLibrary.pncRecievedat24Hours());
        addRowWithColumns(dsd, "PN03","PNC Timing at 6 days",indicatorLibrary.pncRecievedat6Days());
        addRowWithColumns(dsd, "PN04","PNC Timing at 6 weeks",indicatorLibrary.pncRecievedat6Weeks());
        addRowWithColumns(dsd, "PN05","PNC Timing at 6 months",indicatorLibrary.pncRecievedat6Months());
        addRowWithColumns(dsd, "PN06","Total PNC Referrals",indicatorLibrary.totalPNCReferrals());
        addRowWithColumns(dsd, "PN07","PNC referrals from Community",indicatorLibrary.pncReferralsFromCommunity());
        addRowWithColumns(dsd, "PN08","Mothers  who tested ",indicatorLibrary.mothersTestedForHIVfortheFirstitmeonPNC());
        addRowWithColumns(dsd, "PN09","Mothers  who tested positive on the first time testing",indicatorLibrary.mothersTestedForHIVPositivefortheFirstitmeonPNC());
        addRowWithColumns(dsd, "PN10","Breast feeding Mothers  who retested for HIV on PNC Visit",indicatorLibrary.mothersRetestedforHIVonPNC());
        addRowWithColumns(dsd, "PN11","Mothers  who retested positive for HIV on PNC Visit",indicatorLibrary.mothersRetestedPositiveforHIVonPNC());
        addRowWithColumns(dsd, "PN12"," Total HIV + Mothers initiating ART at PNC",indicatorLibrary.totalMOthersInitiatingARTinPNC());
        addRowWithColumns(dsd, "PN13","New Positive Mothers initiating ART at PNC",indicatorLibrary.NewMOthersInitiatingARTinPNC());
        addRowWithColumns(dsd, "PN14","New Positive Mothers  who retested positive for HIV on PNC Visit",indicatorLibrary.mothersPositiveONRetest());
        addRowWithColumns(dsd, "PN15","Positve but never intiated",indicatorLibrary.positveButNeverInitiated());
        addRowWithColumns(dsd, "PN16","Mothers  newly enrolled on FSG ",indicatorLibrary.newlyEnrolledOnFSG());
        addRowWithColumns(dsd, "PN17","Mothers Baby Pair  enrolled at MBCP ",indicatorLibrary.enrolledAtMBCP());
        addRowWithColumns(dsd, "PN18","Male partners received HIV test results in the postnatal setting - Totals",indicatorLibrary.malePatinersRecievedHivResultatPNC());
        addRowWithColumns(dsd, "PN19","Male partners received HIV test results in the postnatal setting",indicatorLibrary.malePatinersRecievedHiPositivevResultatPNC());
        addRowWithColumns(dsd, "PN20","HIV+ male partners initiated on ART in the postnatal setting",indicatorLibrary.malePartnersInitiatedonART());
        addRowWithColumns(dsd, "PN21","Breast feeding mothers given self-testing kits for their male partners",indicatorLibrary.breastFeedingWomenGivenSTKs());
        addRowWithColumns(dsd, "PN22","STK result is positive",indicatorLibrary.positiveSTKResultforPartner());
        addRowWithColumns(dsd, "PN23","STK result is negative ",indicatorLibrary.negativeSTKResultPartner());
        addRowWithColumns(dsd, "PN24","Discordant Couples",indicatorLibrary.discordantResultsPNC());
        addRowWithColumns(dsd, "PN25","Clients screened for Cancer of the Breast",indicatorLibrary.breastScreening());
        addRowWithColumns(dsd, "PN26","Clients with pre-malignant conditions of breast",indicatorLibrary.clientsWithPreMalignantConditions());
        addRowWithColumns(dsd, "PN27","Clients screened for Cancer of the Cervix",indicatorLibrary.clientsScreenedForCervicalCancer());
        addRowWithColumns(dsd, "PN28","Clients with pre-malignant conditions of the Cervix",indicatorLibrary.clientsWithPreMalignantCervicalConditions());
        addRowWithColumns(dsd, "PN29","lactating mothers with nutritional assesmsment",indicatorLibrary.nutritionalAssessmentDone());
        addRowWithColumns(dsd, "PN30","lactating mothers with nutritional assesmsment and they are HIV positve",indicatorLibrary.nutritionalAssessmentDoneAndHIVPositive());
        addRowWithColumns(dsd, "PN31","Postnatal mothers who recieved Maternal Nutrition Counselling",indicatorLibrary.manternalCounsellingDone());
        addRowWithColumns(dsd, "PN32","Maternal Nutrition and Positive ",indicatorLibrary.maternalNutritionAndHIVPositiveatPNC());
        addRowWithColumns(dsd, "PN33","Infant Counselling ",indicatorLibrary.infantFeedingCounsellingatPNC());
        addRowWithColumns(dsd, "PN34","Infant Counselling and Positive ",indicatorLibrary.infantFeedingCounsellingAndHIVPositiveatPNC());

        return dsd;
    }

    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {

        addIndicator(dsd, key + "aF", label + " (Below 15 Years) Female", cohortIndicator, "age=Below15");
        addIndicator(dsd, key + "bF", label + " (Between 15 and 19 Years) Female", cohortIndicator, "age=Between15And19yrs");
        addIndicator(dsd, key + "cF", label + " (Between 20 and 24 Years) Female", cohortIndicator, "age=Between20And24yrs");
        addIndicator(dsd, key + "dF", label + " (Between 25 and 49 Years) Female", cohortIndicator, "age=Between25And49yrs");
        addIndicator(dsd, key + "eF", label + " (>50) Female", cohortIndicator, "age=GreaterThan50yrs");
        addIndicator(dsd, key + "fF", label + " (Total) ", cohortIndicator, "");
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator, String dimensionOptions) {
        dsd.addColumn(key, label, ReportUtils.map(cohortIndicator, PARAMS), dimensionOptions);

    }

    @Override
    public String getVersion() {
        return "1.0.3";
    }
}
