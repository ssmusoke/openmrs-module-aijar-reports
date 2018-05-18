package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.PreARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Pre-ART Register
 */
@Component
public class SetupPreARTCSVRegister extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "59f5f5fb-8a85-4b1d-ab09-229277540d38";
    }

    @Override
    public String getUuid() {
        return "a4743b94-c76a-4d9d-90f1-d48003394da6";
    }

    @Override
    public String getName() {
        return "Pre-ART CSV Register";
    }

    @Override
    public String getDescription() {
        return "Pre-ART CSV Register";
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
        l.add(buildExcel(reportDefinition));
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
        ReportDesign rd = createCSVDesign(getExcelDesignUuid(), reportDefinition);
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PreARTDatasetDefinition dsd = new PreARTDatasetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("PRE_ART_CSV", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    public ReportDesign buildExcel(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign("568867cc-591a-42ec-8a79-affaf6eea2f9", reportDefinition, "FacilityPreARTRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:PRE_ART_CSV");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
