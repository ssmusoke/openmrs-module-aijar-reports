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
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
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

public class SetupMOH105SMC2019Report extends UgandaEMRDataExportManager {

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
        return "0b095617-de87-4f7c-a15b-a53b9a2e43b4";
    }


    @Override
    public String getUuid() {
        return "d7c9aca3-1640-4761-ba9c-32dbbf109ecf";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 2: SMC ";
    }

    @Override
    public String getDescription() {
        return "HMIS 105 Section 2: SMC";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "SMC_105_Section.xls");
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
        rd.addDataSetDefinition("105", Mapped.mapStraightThrough(smcDataSetDefinition()));
        rd.addDataSetDefinition("P", Mapped.mapStraightThrough(period()));
        return rd;

    }

    protected DataSetDefinition smcDataSetDefinition() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.SMCAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));


        addRowWithColumns(dsd, "SMC01","SM01. Number of Males Counselled and Tested Postive for HIV at SMC site ",indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("dc866728-30ab-102d-86b0-7a5022ba4115")) );
        addRowWithColumns(dsd, "SMC02","SM01. Number of Males Counselled and Tested Negative for HIV at SMC site ",indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("dc85aa72-30ab-102d-86b0-7a5022ba4115")));
        addRowWithColumns(dsd, "SMC03","SM01. Number of Males Counselled and Tested Incloclusive for HIV at SMC site ",indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("16d4ad2b-c4eb-4e88-a27f-5d9b9b6a9aed")));
        addRowWithColumns(dsd, "SMC04","SM01. Number of Males Counselled and Tested with Known Positive for HIV at SMC site ",indicatorLibrary.counseledAndTestedWithResuls(Dictionary.getConcept("60155e4d-1d49-4e97-9689-758315967e0f")));
        addRowWithColumns(dsd, "SMC05","SM01. Number of Males not Tested for HIV ",indicatorLibrary.individualsNoteTestedForHIV());
        addRowWithColumns(dsd, "SMC06","SM01. Circumcised at Facility using surgical means",indicatorLibrary.circumcisedAtFacilityUsingSurgicalMethods());
        addRowWithColumns(dsd, "SMC07","SM01. Circumcised at Facility using device means",indicatorLibrary.circumcisedAtFacilityUsingDeviceMethods());
        addRowWithColumns(dsd, "SMC08","SM01. Circumcised at Outreach using surgical means",indicatorLibrary.circumcisedAtOutreachUsingSurgicaleMethods());
        addRowWithColumns(dsd, "SMC09","SM01. Circumcised at Facility using device means",indicatorLibrary.circumcisedAtOutreachUsingDeviceMethods());
        addRowWithColumns(dsd,"SMC10", "First follow up visit within 2 days", indicatorLibrary.followupVisitatFacilityUsingSurgical(2));
        addRowWithColumns(dsd,"SMC11", "First follow up visit within 7 days", indicatorLibrary.followupVisitatFacilityUsingSurgical(7));
        addRowWithColumns(dsd,"SMC12", "First follow up visit within 14 days", indicatorLibrary.followupVisitatFacilityUsingSurgical(14));
        addRowWithColumns(dsd,"SMC13", "First follow up visit within beyond 14 days", indicatorLibrary.followupVisitatFacilityUsingSurgical(15));
        addRowWithColumns(dsd,"SMC30", "Moderate Adverse Events at Facility using Surgical Means", indicatorLibrary.moderateAdverseEventsAtFacilityUsingSurgical());
        addRowWithColumns(dsd,"SMC31", "Moderate Adverse Events at Facility using device Means", indicatorLibrary.moderateAdverseEventsAtFacilityUsingDevice());
        addRowWithColumns(dsd,"SMC32", "Moderate Adverse Events at Outreach site  using surgical Means", indicatorLibrary.moderateAdverseEventsAtOutreachsiteUsingSurgical());
        addRowWithColumns(dsd,"SMC33", "Moderate Adverse Events at Outreach site  using device Means", indicatorLibrary.moderateAdverseEventsAtOutreachUsingDevice());
        addRowWithColumns(dsd,"SMC35", "Severe Adverse Events at Facility using Surgical Means", indicatorLibrary.severeAdverseEventsAtFacilityUsingSurgical());
        addRowWithColumns(dsd,"SMC36", "Severe Adverse Events at Facility using device Means", indicatorLibrary.severeAdverseEventsAtFacilityUsingDevice());
        addRowWithColumns(dsd,"SMC37", "Severe Adverse Events at Outreach Site using Surgical Means", indicatorLibrary.severeAdverseEventsAtOutreachUsingSurgical());
        addRowWithColumns(dsd,"SMC38", "Severe Adverse Events at Outreach Site  using Device Means", indicatorLibrary.severeAdverseEventsAtOutreachUsingDevice());

        return dsd;
    }

    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {
        addIndicator(dsd, key + "aM", label + " (Between 0 and 60 days) Male", cohortIndicator, "age=Below2Months");
        addIndicator(dsd, key + "bM", label + " (Between 2 Months and a Year) Male", cohortIndicator, "age=Between2MonthsAnd1Year");
        addIndicator(dsd, key + "cM", label + " (Between 1 and 9 Year) Male", cohortIndicator, "age=Between1And9Year");
        addIndicator(dsd, key + "dM", label + " (Between 10 and 14 Year) Male", cohortIndicator, "age=Between10And14Year");
        addIndicator(dsd, key + "eM", label + " (Between 15 and 19 Year) Male", cohortIndicator, "age=Between15And19Year");
        addIndicator(dsd, key + "fM", label + " (Between 20 and 24 Year) Male", cohortIndicator, "age=Between20And24Year");
        addIndicator(dsd, key + "gM", label + " (Between 25 and 29 Year) Male", cohortIndicator, "age=Between25And29Year");
        addIndicator(dsd, key + "hM", label + " (Between 30 and 34 Year) Male", cohortIndicator, "age=Between30And34Year");
        addIndicator(dsd, key + "iM", label + " (Between 35 and 39 Year) Male", cohortIndicator, "age=Between35And39Year");
        addIndicator(dsd, key + "jM", label + " (Between 40 and 44 Year) Male", cohortIndicator, "age=Between40And44yrs");
        addIndicator(dsd, key + "kM", label + " (Between 45 and 49 Year) Male", cohortIndicator, "age=Between45And49yrs");
        addIndicator(dsd, key + "lM", label + " (Greater than 50) Male", cohortIndicator, "age=GreaterThan50yrs");
        addIndicator(dsd, key + "mM", label + " Total", cohortIndicator, "");




    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator, String dimensionOptions) {
        dsd.addColumn(key, label, ReportUtils.map(cohortIndicator, PARAMS), dimensionOptions);

    }

    @Override
    public String getVersion() {
        return "2.1.5";
    }
}
