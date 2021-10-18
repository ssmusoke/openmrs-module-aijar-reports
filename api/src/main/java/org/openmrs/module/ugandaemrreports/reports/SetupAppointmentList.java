package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Daily Appointments List report
 */
@Component
public class SetupAppointmentList extends UgandaEMRDataExportManager {

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



    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "98e92026-8c00-415f-9882-43917181f02d";
    }

    public String getCSVDesignUuid() {
        return "6f73facc-05dd-49d5-9a2a-646fe731afaa";
    }

    @Override
    public String getUuid() {
        return "9c85e20b-c3ce-4dc1-b332-13f1d02f1c5c";
    }

    @Override
    public String getName() {
        return "Appointments List";
    }

    @Override
    public String getDescription() {
        return "Lists Patients With Appointments in a particular period";
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
        l.add(buildExcelReportDesign(reportDefinition));
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
       return createCSVDesign(getCSVDesignUuid(), reportDefinition);
    }

    public ReportDesign buildExcelReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "AppointmentList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:APPOINTMENT_LIST");
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

        CohortDefinition definition = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getReturnVisitDate(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), BaseObsCohortDefinition.TimeModifier.ANY);

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(definition));
        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        addColumn(dsd, "EID No", hivPatientData.getEIDNumber());
        dsd.addColumn("Patient Name", new PreferredNameDataDefinition(), (String) null);
        addColumn(dsd, "Sex", builtInPatientData.getGender());
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd, "ART Start Date", hivPatientData.getArtStartDate());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd,"VL Qualitative",hivPatientData.getVLQualitativeByEndDate());
        addColumn(dsd,"DSDM Model", hivPatientData.getDSDMModel());
        addColumn(dsd,"DSDM Model Enrollment Date",   hivPatientData.getDSDMEnrollmentDate());
        addColumn(dsd, "Appointment Date", hivPatientData.getExpectedReturnDateBetween());
        addColumn(dsd, "Telephone", basePatientData.getTelephone());

        rd.addDataSetDefinition("APPOINTMENT_LIST", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(definition));

        return rd;
    }

    @Override
    public String getVersion() {
        return "4.0.2";
    }
}
