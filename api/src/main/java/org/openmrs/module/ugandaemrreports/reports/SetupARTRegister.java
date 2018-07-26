package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.PreARTDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.CommonReportMetadata;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Daily Appointments List report
 */
@Component
public class SetupARTRegister extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private CommonReportMetadata commonMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "7c8301e3-439e-4084-a9f1-dad1a2b6e3c3";
    }

    @Override
    public String getUuid() {
        return "291b3798-ecdc-457e-8f71-9cba159a0e75";
    }

    @Override
    public String getName() {
        return "ART Register";
    }

    @Override
    public String getDescription() {
        return "ART Register";
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
    public ReportDesign buildExcel(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign("cedbd4f6-35bf-4500-b386-bd22d0e34e46", reportDefinition, "FacilityARTRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:6-8,dataset:ART | sheet:2,row:3-5,dataset:ART | sheet:3,row:3-5,dataset:ART | sheet:4,row:3-5,dataset:ART");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        ARTDatasetDefinition dsd = new ARTDatasetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("ART", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.3";
    }
}
