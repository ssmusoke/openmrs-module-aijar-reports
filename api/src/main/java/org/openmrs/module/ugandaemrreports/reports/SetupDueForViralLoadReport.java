package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
@Component
public class SetupDueForViralLoadReport extends UgandaEMRDataExportManager {
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

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "19ec94ea-defe-4b1d-ae29-79c13de557b3";
    }

    @Override
    public String getUuid() {
        return "23ef26dd-8e26-4109-b8f2-d102a119b901";
    }

    @Override
    public String getName() {
        return "Due For Viral Load";
    }

    @Override
    public String getDescription() {
        return "Due For Viral Load";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "DueForViralLoad.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:DUE_FOR_VIRAL_LOAD");
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

        CohortDefinition patientsOnArt = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(patientsOnArt));
        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        dsd.addColumn( "Patient Name",  new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn( "Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd, "Age", builtInPatientData.getAgeAtStart());
        addColumn(dsd, "HIV Enrolled Date", hivPatientData.getEnrollmentDate());
        addColumn(dsd, "ART Start Date", hivPatientData.getArtStartDate());
        addColumn(dsd, "Viral Load Date", hivPatientData.getViralLoadDate());
        addColumn(dsd, "Viral Load Qualitative", hivPatientData.getViralLoadQualitative());
        addColumn(dsd, "Viral Load", hivPatientData.getCurrentViralLoad());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastVisitDate());




        addColumn(dsd, "Appointment Date", hivPatientData.getExpectedReturnDateBetween());
        addColumn(dsd, "Telephone", basePatientData.getTelephone());

        rd.addDataSetDefinition("DUE_FOR_VIRAL_LOAD", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(patientsOnArt));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.56";
    }
}


