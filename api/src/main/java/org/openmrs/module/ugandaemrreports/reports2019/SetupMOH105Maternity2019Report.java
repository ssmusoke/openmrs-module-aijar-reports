package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
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

public class SetupMOH105Maternity2019Report extends UgandaEMRDataExportManager {

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
        return "a8d028f3-7573-41ce-8bf6-0be7c6094df4";
    }


    @Override
    public String getUuid() {
        return "2e24eed4-98a9-4143-af30-f4737df2282e";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 2.2: MATERNITY ";
    }

    @Override
    public String getDescription() {
        return "HMIS 105 Section 2.2: MATERNITY";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS105Section2.2Maternity.xls");
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
        rd.addDataSetDefinition("105", Mapped.mapStraightThrough(maternityDatasetDefinition()));
        rd.addDataSetDefinition("P", Mapped.mapStraightThrough(period()));
        return rd;

    }

    protected DataSetDefinition maternityDatasetDefinition() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.MCHAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        addRowWithColumns(dsd, "MA01","Total Number of Maternity Visits ",indicatorLibrary.maternityAdmissions());
        addRowWithColumns(dsd, "MA02","Total Number of Maternity Referrals ",indicatorLibrary.maternityReferrals());
        addRowWithColumns(dsd, "MA03"," Maternity Referrals from Community ",indicatorLibrary.maternityReferralsFromCommunity());
        addRowWithColumns(dsd, "MA04"," Maternity Referrals Out ",indicatorLibrary.maternityReferralsOut());
        addRowWithColumns(dsd, "MA05","Total Number of Maternity Deliveries by age group ",indicatorLibrary.totalNumberofDeliveries());
        addRowWithColumns(dsd, "MA06","Total Number of babies born live  ",indicatorLibrary.totalNumberofBabiesBornAlive());
        addRowWithColumns(dsd, "MA07","Total Number live babies below normal weight  ",indicatorLibrary.liveBirthDeliveriesAndBelowNormalWeight());
        addRowWithColumns(dsd, "MA08","Total Number of fresh still births  ",indicatorLibrary.freshStillBirth());
        addRowWithColumns(dsd, "MA09","Total Number of fresh still births below normal weight  ",indicatorLibrary.freshStillBirthsAndBelowNormalWeight());
        addRowWithColumns(dsd, "MA10","Total Number of Marcerated ",indicatorLibrary.marceratedStillBirth());
        addRowWithColumns(dsd, "MA11","Total Number of Marcerated still births below normal weight  ",indicatorLibrary.marceratedStillBirthAndBelowNormalWeight());
        addRowWithColumns(dsd, "MA12","Total Preterm Births  ",indicatorLibrary.totalPretermBirths());
        addRowWithColumns(dsd, "MA13","Total Preterm Births and Alive  ",indicatorLibrary.pretermLiveBriths());
        addRowWithColumns(dsd, "MA14","Preterm Births Below Normal Weight",indicatorLibrary.pretermBrithsBelowNormalWeight());
        addRowWithColumns(dsd, "MA15","Total Born before arrival",indicatorLibrary.babiesBornBeforeArrival());
        addRowWithColumns(dsd, "MA16","Total Born before arrival and are alive",indicatorLibrary.babiesBornBeforeArrivalandAlive());
        addRowWithColumns(dsd, "MA17","Total Born before arrival and below normal weight",indicatorLibrary.babiesBornBeforeArrivalandBelowNormalWeight());
        addRowWithColumns(dsd, "MA18","Preterm Births Below Normal Weight",indicatorLibrary.lowBirthWeightInitiatedOnKangaroo());
        addRowWithColumns(dsd, "MA19","Live Babies discharged ",indicatorLibrary.totalNumberofBabiesBornAlive());
        addRowWithColumns(dsd, "MA20","Babies born with defects ",indicatorLibrary.babiesBornWithDefects());
        addRowWithColumns(dsd, "MA21","Received Long Lasting Insecticide Treated Net",indicatorLibrary.longLastingInsecticideTreatedNetGiven());
        addRowWithColumns(dsd, "MA22","New Born Deaths (0 - 7 days)",indicatorLibrary.newBornDeaths());
        addRowWithColumns(dsd, "MA23","New Born Deaths (8 - 28 days)",indicatorLibrary.neonantalBornDeaths());
        addRowWithColumns(dsd, "MA24","Maternal Deaths",indicatorLibrary.maternalDeaths());
        addRowWithColumns(dsd, "MA25","Mothers who breastfed within an hour after delivery",indicatorLibrary.mothersWhoBreastFedWithinAnHour());
        addRowWithColumns(dsd, "MA26","Mothers who breastfed within an hour after delivery and HIV Positive.",indicatorLibrary.mothersWhoBreastFedWithinAnHourAndHIVPositive());
        addRowWithColumns(dsd, "MA27","Mothers  who tested ",indicatorLibrary.mothersTestedForHIVfortheFirstitme());
        addRowWithColumns(dsd, "MA28","Mothers  who tested positive on the first time testing",indicatorLibrary.mothersTestedForHIVPositivefortheFirstitme());
        addRowWithColumns(dsd, "MA29","Mothers  who retested for HIV in the Labour ward",indicatorLibrary.mothersRetestedforHIV());
        addRowWithColumns(dsd, "MA30","Mothers  who retested positive for HIV in the Labour ward",indicatorLibrary.mothersRetestedPositiveforHIV());
        addRowWithColumns(dsd, "MA31","Mothers initiating ART in the maternity ward",indicatorLibrary.mothersInitiatedOnARTinMaternity());
        addRowWithColumns(dsd, "MA32","Male Partners recieved HIV testing results ",indicatorLibrary.malePartnersTestedOnMaternityVisit());
        addRowWithColumns(dsd, "MA33","Male Partners recieved positive HIV testing results ",indicatorLibrary.malePartnersTestedPositiiveOnMaternityVisit());
        addRowWithColumns(dsd, "MA34","Male Partners initiated on ART ",indicatorLibrary.malePartnersInitiatedOnARTinMaternity());
        addRowWithColumns(dsd, "MA35","Discordant Couples",indicatorLibrary.discordantResultsinMaternity());
        addRowWithColumns(dsd, "MA36","Maternal Nutritional Counselling",indicatorLibrary.maternalNutritioninMaternity());
        addRowWithColumns(dsd, "MA37","Maternal Nutritional Counselling and Positive",indicatorLibrary.maternalNutritionAndHIVPositiveinMaternity());
        addRowWithColumns(dsd, "MA38","Infant feeding",indicatorLibrary.infantFeedingInMaternity());
        addRowWithColumns(dsd, "MA39","Infant feeding and tested positive",indicatorLibrary.infantFeedingCounsellingAndHIVPositiveinMaternity());
        addRowWithColumns(dsd, "MA40","Deliveries to HIV+ Mothers",indicatorLibrary.deliveriesInUnit());
        addRowWithColumns(dsd, "MA41","Live Births in Delivery Unit",indicatorLibrary.liveBirthsDeliveriesInUnit());
        addRowWithColumns(dsd, "MA42"," Number of babies given NVP Syrup",indicatorLibrary.hivExposedInfantsStartARV());
        addRowWithColumns(dsd, "MA43"," Number of high Risk baboes",indicatorLibrary.hivExposedInfantsatHighRisk());
        addRowWithColumns(dsd, "MA44"," Number of Babies with Asyphyxia",indicatorLibrary.babiesWithBrithAsyphxia());
        addRowWithColumns(dsd, "MA45"," Live babies Resuscitated",indicatorLibrary.livebabiesResuscitated());
        addRowWithColumns(dsd, "MA46","Babbies that recieved PNC at 6 hours",indicatorLibrary.babiesRecievedPNCatSixHours());
        addRowWithColumns(dsd, "MA47","Mothers that recieved PNC at 6 hours",indicatorLibrary.mothersRecievedPNCatSixHours());
        addRowWithColumns(dsd, "MA48","Babies that recieved PNC at 24 hours",indicatorLibrary.babiesRecievedPNCat24Hours());
        addRowWithColumns(dsd, "MA49","Mothers that recieved PNC at 24 hours",indicatorLibrary.mothersRecievedPNCat24Hours());
        addRowWithColumns(dsd, "MA50","Oxytoncin used in management of 3rd stage of labour",indicatorLibrary.oxytocinUsedInManagementofThirdStageLabour());
        addRowWithColumns(dsd, "MA51","Misoprostol used in management of 3rd stage of labour",indicatorLibrary.misoprostolUsedInManagementofThirdStageLabour());
        addRowWithColumns(dsd, "MA52","Egometrine used in management of 3rd stage of labour",indicatorLibrary.egometrineUsedInManagementofThirdStageLabour());
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
        return "1.0.2.8";
    }
}
