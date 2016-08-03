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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "FacilityPreARTRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:PRE_ART");
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
        rd.addDataSetDefinition("PRE_ART", Mapped.mapStraightThrough(dsd));

        dsd.addSortCriteria("Date Enrolled", SortCriteria.SortDirection.ASC);

        CohortDefinition everEnrolledCare = Cohorts.getPatientsWhoEnrolledInCareInYear();
        dsd.addRowFilter(Mapped.mapStraightThrough(everEnrolledCare));

        addColumn(dsd, "Date Enrolled", hivPatientData.getEnrollmentDate());
        addColumn(dsd, "Unique ID no", hivPatientData.getClinicNumber());
        addColumn(dsd, "Patient Clinic ID", builtInPatientData.getPatientId());
        addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Gender", builtInPatientData.getGender());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd, "Address", basePatientData.getTraditionalAuthority());
        addColumn(dsd, "Entry Point", hivPatientData.getEntryPoint());

        addColumn(dsd, "TI", hivPatientData.getFirstTransferIn());
        addColumn(dsd, "PREGNANT", hivPatientData.getFirstPregnant());
        addColumn(dsd, "TB", hivPatientData.getFirstTB());
        addColumn(dsd, "LACTATING", hivPatientData.getFirstLactating());
        addColumn(dsd, "EID", hivPatientData.getFirstEID());

        addColumn(dsd, "CPT Start Date", hivPatientData.getCPTStartDate());
        addColumn(dsd, "INH Start Date", hivPatientData.getINHStartDate());
        addColumn(dsd, "TB Start Date", hivPatientData.getTBStartDate());
        addColumn(dsd, "TB Stop Date", hivPatientData.getTBStopDate());
        addColumn(dsd, "1", hivPatientData.getWHOStage1Date());
        addColumn(dsd, "2", hivPatientData.getWHOStage2Date());
        addColumn(dsd, "3", hivPatientData.getWHOStage3Date());
        addColumn(dsd, "4", hivPatientData.getWHOStage4Date());
        addColumn(dsd, "Date Eligible for ART", hivPatientData.getARTEligibilityDate());

        addColumn(dsd, "PREGNANT", hivPatientData.getFirstPregnant());
        addColumn(dsd, "TB", hivPatientData.getFirstTB());
        addColumn(dsd, "LACTATING", hivPatientData.getFirstLactating());
        addColumn(dsd, "CD4", hivPatientData.getARTEligibilityCD4());
        addColumn(dsd, "WHO", hivPatientData.getARTEligibilityWHOStage());

        addColumn(dsd, "Date Eligible and Ready", hivPatientData.getARTEligibilityAndReadyDate());
        addColumn(dsd, "Date ART Started", hivPatientData.getARTStartDate());

        for (int i = 0; i <= 15; i++) {
            addColumn(dsd, "CPT" + (i + 1), hivPatientData.getCPTStatusDuringQuarter(i));
            addColumn(dsd, "INH" + (i + 1), hivPatientData.getINHStatusDuringQuarter(i));
            addColumn(dsd, "TB" + (i + 1), hivPatientData.getTBStatusDuringQuarter(i));
            addColumn(dsd, "CD4" + (i + 1), hivPatientData.getCD4DuringQuarter(i));
            addColumn(dsd, "NUTRITION" + (i + 1), hivPatientData.getNutritionalStatusDuringQuarter(i));
            addColumn(dsd, "FUS" + (i + 1), hivPatientData.havingEncounterDuringQuarter(i));
        }

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
