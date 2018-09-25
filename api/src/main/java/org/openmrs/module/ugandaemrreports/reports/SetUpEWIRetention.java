package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class SetUpEWIRetention extends UgandaEMRDataExportManager {
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
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;
    @Autowired
    private ARTCohortLibrary artCohortLibrary;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "53b870e7-ecb9-4b57-8a0c-2778a884e4d3";
    }
    @Override
    public String getUuid() {
        return "34d5e226-d408-4bc4-bda7-f6b5407aefed";
    }
    @Override
    public String getName() {
        return " Early Warning Indicators - Retention";
    }
    @Override
    public String getDescription() {
        return "Retention";
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
     *created by solemabrothers
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "Retention.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:16,dataset:LOST_TO_FOLLOW_UP");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }
    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        String startDate = "12m";
        String enddate   ="15m";

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        CohortDefinition cleintsLostToFollowUp = hivCohortDefinitionLibrary.getEarlyWarningIndicatorDataAbstractionCohort();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(cleintsLostToFollowUp));
        addColumn( dsd,"Patient ID", builtInPatientData.getPatientId());
        dsd.addColumn( "Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd, "Age", builtInPatientData.getAgeAtStart());
        addColumn(dsd, "HIV Enrolled Date", hivPatientData.getEnrollmentDate());
        addColumn(dsd, "ART Start Date", hivPatientData.getArtStartDate());
        addColumn(dsd, "Last Clinical Consultation",hivPatientData.getLastARTEncounter());

        addColumn(dsd, "Last Clinical Consultation Missed",hivPatientData.getLastVisitDate());

        rd.addDataSetDefinition("LOST_TO_FOLLOW_UP", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(cleintsLostToFollowUp));
        return rd;
    }
    @Override
    public String getVersion() {
        return "0.58";
    }
}


