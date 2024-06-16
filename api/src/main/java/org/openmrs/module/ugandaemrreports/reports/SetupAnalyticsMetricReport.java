package org.openmrs.module.ugandaemrreports.reports;

import com.google.common.base.Joiner;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AggregateReportDataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EMRVersionDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.TodayDateDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 *  TX Current Report
 */
@Component
public class SetupAnalyticsMetricReport extends AggregateReportDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;


    @Autowired
    private SharedDataDefintion sharedDataDefintion;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "a06b7310-2565-45bd-89af-514b724a1db0";
    }

    public String getJSONDesignUuid() {
        return "13cf6468-07c2-40f5-a388-6fdc8fa8341e";
    }

    @Override
    public String getUuid() {
        return "dcd1f91a-04c8-4ae1-ac44-6abfdc91c98a";
    }

    @Override
    public String getName() {
        return "Analytics Metric Report";
    }

    @Override
    public String getDescription() {
        return "Analytics Metric Report";
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
        return createExcelTemplateDesign("351284d3-a0da-4632-81e0-23e2d4777504", reportDefinition, "METRICS.xls");
    }


    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "METRICS.json");
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());
        AggregateReportDataSetDefinition dsd = new AggregateReportDataSetDefinition();

        dsd.setParameters(getParameters());
        dsd.setReportDesign(getJsonReportDesign());
        rd.addDataSetDefinition("aijar", Mapped.mapStraightThrough(getUgandaEMRVersion()));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(CommonDatasetLibrary.settings()));
        rd.addDataSetDefinition("date", Mapped.mapStraightThrough(getDateToday()));
        rd.addDataSetDefinition("H", Mapped.mapStraightThrough(sharedDataDefintion.healthFacilityName()));
        rd.addDataSetDefinition("TX", Mapped.mapStraightThrough(dsd));

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

    public DataSetDefinition getUgandaEMRVersion(){
        EMRVersionDatasetDefinition dsd= new EMRVersionDatasetDefinition();
        return dsd;
    }

    public DataSetDefinition getDateToday(){
        TodayDateDatasetDefinition dsd= new TodayDateDatasetDefinition();
        return dsd;
    }

    @Override
    public String getVersion() {
        return "0.4.7";
    }
}