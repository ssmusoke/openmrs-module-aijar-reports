package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HIEOutcomesCohortDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.EIDCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.Moh105IndicatorLibrary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.period;

/**
 * CBS EID Cohort Report
 */
@Component
public class SetupHEIOutComes extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    private Moh105IndicatorLibrary definitionLibrary;


    private static final String PARAMS = "startDate=${startDate},endDate=${endDate}";


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "2090o268-d8b1-11e6-bf26-cec0c932ce01";
    }

    @Override
    public String getUuid() {
        return "209b94d4-d8b1-11e6-bf26-cec0c932ce01";
    }

    @Override
    public String getName() {
        return "HIV Exposed Infant Outcomes 105 Section";
    }

    @Override
    public String getDescription() {
        return "HEI 105 Section";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HIE_OUTCOMES.xls");
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        HIEOutcomesCohortDataSetDefinition dsd = new HIEOutcomesCohortDataSetDefinition();
        dsd.setYearly(Boolean.FALSE);
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("HIE", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    @Override
    public String getVersion() {
        return "1.17";
    }
}
