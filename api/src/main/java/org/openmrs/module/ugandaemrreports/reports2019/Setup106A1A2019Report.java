package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AggregateReportDataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EMRVersionDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.MedianBaselineCD4DatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.CommonCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reports.AggregateReportDataExportManager;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;

/**
 */
@Component
public class Setup106A1A2019Report extends AggregateReportDataExportManager {


    @Override
    public String getExcelDesignUuid() {
        return "cdf55e2e-bc5b-4787-9b3c-90fc0320896d";
    }

    public String getJSONDesignUuid() {
        return "418e20db-7d80-4ac6-9404-c8f3cc6c0f4f";
    }

    @Override
    public String getUuid() {
        return "5718901d-3dae-4a0d-9847-df345bf0de39";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A 2019";
    }

    @Override
    public String getDescription() {
        return "This is the 2019 version of the HMIS 106A Section 1A ";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start date (Start of quarter)", Date.class));
        l.add(new Parameter("endDate", "End date (End of quarter)", Date.class));
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        l.add(buildJSONReportDesign(reportDefinition));

        return l;
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1A2019Report.xls");
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "106A1A2019Report.json");
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        AggregateReportDataSetDefinition dsd = new AggregateReportDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.setReportDesign(getJsonReportDesign());
        rd.addDataSetDefinition("x", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("aijar",Mapped.mapStraightThrough(getUgandaEMRVersion()));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        return rd;
    }

    public static DataSetDefinition getUgandaEMRVersion(){
        EMRVersionDatasetDefinition dsd= new EMRVersionDatasetDefinition();
        return dsd;
    }

    @Override
    public String getVersion() {
        return "3.2.1";
    }
}
