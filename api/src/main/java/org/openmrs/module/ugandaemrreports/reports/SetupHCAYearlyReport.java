package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EIDCohortDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * CBS EID Cohort Report
 */
@Component
public class SetupHCAYearlyReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "20909268-d8b1-11e6-bf26-cec0c932ce01";
    }

    @Override
    public String getUuid() {
        return "209094d4-d8b1-11e6-bf26-cec0c932ce01";
    }

    @Override
    public String getName() {
        return "HCA Yearly Cohort Analysis";
    }

    @Override
    public String getDescription() {
        return "HCA Yearly Cohort Analysis";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HCAYearlyReport.xls");
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        EIDCohortDataSetDefinition dsd = new EIDCohortDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("hca", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.56";
    }
}
