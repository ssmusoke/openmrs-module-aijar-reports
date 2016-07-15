package org.openmrs.module.aijarreports.reports;

import org.openmrs.module.aijarreports.library.*;
import org.openmrs.module.aijarreports.metadata.CommonReportMetadata;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Daily Appointments List report
 */
@Component
public class SetupARTRegister extends AijarDataExportManager {

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
        return "98e9202d-8c00-415f-9882-43917181f023";
    }

    @Override
    public String getUuid() {
        return "9c85e206-c3cd-4dc1-b332-13f1d02f1c54";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "FacilityARTRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:4,dataset:ART");
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

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("ART", Mapped.mapStraightThrough(dsd));

        dsd.addSortCriteria("Date ART Started", SortCriteria.SortDirection.ASC);

        addColumn(dsd, "Date ART Started", hivPatientData.getARTStartDate());

        CohortDefinition duringMonth = Cohorts.getPatientHavingARTDuringMonth();
        CohortDefinition beforeMonth = Cohorts.getPatientHavingARTBeforeMonth();
        CohortDefinition enrolledThisMonth = df.getPatientsNotIn(duringMonth,beforeMonth);

        dsd.addRowFilter(Mapped.mapStraightThrough(enrolledThisMonth));
        addColumn(dsd, "Unique ID no", hivPatientData.getClinicNumber());
        addColumn(dsd, "Patient Clinic ID", builtInPatientData.getPatientId());
        addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Gender", builtInPatientData.getGender());
        addColumn(dsd, "Address", basePatientData.getTraditionalAuthority());
        addColumn(dsd, "CPT Start Date", hivPatientData.getCPTStartDate());
        addColumn(dsd, "INH Start Date", hivPatientData.getINHStartDate());
        addColumn(dsd, "TB Start Date", hivPatientData.getTBStartDate());
        addColumn(dsd, "TB Stop Date", hivPatientData.getTBStopDate());

        for (int i = 0; i <= 71; i++) {
            addColumn(dsd, "CPT" + (i + 1), hivPatientData.getCPTStatusDuringMonth(i));
            addColumn(dsd, "TB" + (i + 1), hivPatientData.getTBStatusDuringMonth(i));
            addColumn(dsd, "ARV" + (i + 1), hivPatientData.getARVRegimenDuringMonth(i));
            addColumn(dsd, "ADH" + (i + 1), hivPatientData.getARVADHDuringMonth(i));
        }

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
