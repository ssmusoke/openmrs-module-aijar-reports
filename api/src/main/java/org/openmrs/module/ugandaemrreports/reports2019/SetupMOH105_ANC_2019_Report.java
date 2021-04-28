package org.openmrs.module.ugandaemrreports.reports2019;

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
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
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

public class SetupMOH105_ANC_2019_Report extends UgandaEMRDataExportManager {

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
        return "aa23c2b1-ca83-47fb-9846-b16294fc03d6";
    }
    public String getJSONDesignUuid() {
        return "f78a54c7-dd5b-4b36-8b9d-c77847c16118";
    }

    @Override
    public String getUuid() {
        return "ab8a7376-ce75-49ac-910c-dffe9a38d445";
    }

    @Override
    public String getName() {
        return "HMIS 105 Section 2: ANTENANTAL ";
    }

    @Override
    public String getDescription() {
        return "HMIS 105 Section 2: ANTENANTAL";
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
        ReportDesign rd = createExcelTemplateDesign("04c3d0f2-fd33-4b48-ada7-fc7838566917b3", reportDefinition, "HMOH105MCHSection_2019.xls");
        return rd;
    }

//    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
//        ReportDesign rd = createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "HMIS105Section4_2019.json");
//        return rd;
//    }


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
        rd.addDataSetDefinition("105", Mapped.mapStraightThrough(antentalDataSetDefinition()));
        rd.addDataSetDefinition("P", Mapped.mapStraightThrough(period()));
        return rd;

    }

    protected DataSetDefinition antentalDataSetDefinition() {
        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();
        dsd.setParameters(getParameters());
        dsd.addDimension("age", ReportUtils.map(dimensionLibrary.MCHAgeGroups(), "effectiveDate=${endDate}"));
        dsd.addDimension("gender", ReportUtils.map(dimensionLibrary.gender()));

        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));


        addRowWithColumns(dsd, "AN01","Number of females below the age of 15", indicatorLibrary.individualsWithWardHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "AN01","HT1B-Number of individuals with Ward as  HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithWardHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "AN01","HT1C-Number of individuals with Ward as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithWardHCTEntryPointandLinkedToCare());
        addRowWithColumns(dsd, "AN01","HT1A-Number of individuals with Ward as  HTC Entry Point And Test for HIV", indicatorLibrary.individualsWithWardHCTEntryPointandTestedForHIV());
        addRowWithColumns(dsd, "AN01","HT1B-Number of individuals with Ward as  HTC Entry Point and Newly Positive", indicatorLibrary.individualsWithWardHCTEntryPointandNewlyPostive());
        addRowWithColumns(dsd, "AN01","HT1C-Number of individuals with Ward as  HTC Entry Point and Linked to Care", indicatorLibrary.individualsWithWardHCTEntryPointandLinkedToCare());



        return dsd;
    }

    public void addRowWithColumns(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator) {

        addIndicator(dsd, key + "dF", label + " (Between 15 and 19 Years) Female", cohortIndicator, "gender=F|age=Between15And19yrs");
        addIndicator(dsd, key + "eF", label + " (Between 20 and 24 Years) Female", cohortIndicator, "gender=F|age=Between20And24yrs");
        addIndicator(dsd, key + "mF", label + " (Between 25 and 29 Years) Female", cohortIndicator, "gender=F|age=Between25And29yrs");
        addIndicator(dsd, key + "fF", label + " (Between 30 and 34 Years) Female", cohortIndicator, "gender=F|age=Between30And34yrs");
        addIndicator(dsd, key + "gF", label + " (Between 35 and 39 Years) Female", cohortIndicator, "gender=F|age=Between35And39yrs");
        addIndicator(dsd, key + "hF", label + " (Between 40 and 44 Years) Female", cohortIndicator, "gender=F|age=Between40And44yrs");
        addIndicator(dsd, key + "jF", label + " (Between 45 and 49 Years) Female", cohortIndicator, "gender=F|age=Between45And49yrs");
        addIndicator(dsd, key + "kF", label + " (>50) Female", cohortIndicator, "gender=F|age=GreaterThan50yrs");
        addIndicator(dsd, key + "g", label + " (Total) ", cohortIndicator, "");
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortIndicator cohortIndicator, String dimensionOptions) {
        dsd.addColumn(key, label, ReportUtils.map(cohortIndicator, PARAMS), dimensionOptions);

    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
