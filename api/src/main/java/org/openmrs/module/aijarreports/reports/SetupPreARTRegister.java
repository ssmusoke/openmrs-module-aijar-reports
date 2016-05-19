package org.openmrs.module.aijarreports.reports;

import org.openmrs.module.aijarreports.library.*;
import org.openmrs.module.aijarreports.metadata.CommonReportMetadata;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Daily Appointments List report
 */
@Component
public class SetupPreARTRegister extends AijarDataExportManager {

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
        return "98e9202d-8c00-415f-9882-43917181f087";
    }

    @Override
    public String getUuid() {
        return "9c85e206-c3cd-4dc1-b332-13f1d02f1cc2";
    }

    @Override
    public String getName() {
        return "Pre-ART Register";
    }

    @Override
    public String getDescription() {
        return "Pre-ART Register";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        ReportDesign design = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "FacilityPreARTRegister.xls");
        return Arrays.asList(design);
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
        return null;
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
        rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(dsd));

        CohortDefinition everEnrolledCare = hivCohortDefinitionLibrary.getYearlyPatientsOnCare();
        dsd.addRowFilter(Mapped.mapStraightThrough(everEnrolledCare));

        addColumn(dsd, "Date Enrolled", hivPatientData.getEnrollmentDate());
        addColumn(dsd, "Unique ID no", hivPatientData.getClinicNumber());
        addColumn(dsd, "Patient Clinic ID", builtInPatientData.getPatientId());
        addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Gender", builtInPatientData.getGender());
        addColumn(dsd, "Age", builtInPatientData.getAgeAtEnd());
        addColumn(dsd, "Address", basePatientData.getTraditionalAuthority());
        addColumn(dsd, "Entry Point", hivPatientData.getEntryPoint());
        addColumn(dsd, "Status at enrollment", hivPatientData.getStatusAtEnrollment());
        addColumn(dsd, "CPT Start Date", hivPatientData.getCPTStartDate());
        addColumn(dsd, "INH Start Date", hivPatientData.getINHStartDate());
        addColumn(dsd, "TB Start Date", hivPatientData.getTBStartDate());
        addColumn(dsd, "TB Stop Date", hivPatientData.getTBStopDate());
        addColumn(dsd, "1", hivPatientData.getWHOStage1Date());
        addColumn(dsd, "2", hivPatientData.getWHOStage2Date());
        addColumn(dsd, "3", hivPatientData.getWHOStage3Date());
        addColumn(dsd, "4", hivPatientData.getWHOStage4Date());
        addColumn(dsd, "Date Eligible for ART", hivPatientData.getARTEligibilityDate());
        addColumn(dsd, "Why Eligible", hivPatientData.getWhyEligibleForART());
        addColumn(dsd, "Date Eligible and Ready", hivPatientData.getARTEligibilityAndReadyDate());
        addColumn(dsd, "Date ART Started", hivPatientData.getARTStartDate());

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
