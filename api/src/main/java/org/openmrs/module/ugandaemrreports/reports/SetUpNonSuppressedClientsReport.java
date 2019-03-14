package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 */
@Component
public class SetUpNonSuppressedClientsReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;


    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private ARTCohortLibrary artCohortLibrary;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getUuid() {
        return "8067d0cf-9925-4377-9b16-31e4c8500fe1";
    }

    @Override
    public String getExcelDesignUuid() {
        return "2e42dc77-71d7-4665-a49b-0c1247d0e8e3 ";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     * @SolemaBrothers
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "NonSuppressedViralLoad.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:NON_SUPPRESSED_VIRAL_LOAD");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }


    @Override
    public String getName() {
        return "Non Suppressed Viral Load Report";
    }

    @Override
    public String getDescription() {
        return "Patients with Non Suppressed Viral Loads";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();
//
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        CohortDefinition artcohortDefinition = df.getPatientsWithNonSuppressedViralLoad();


        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(artcohortDefinition));
        addColumn(dsd, "Clinic number", hivPatientData.getClinicNumber());
        addColumn(dsd, "Middle Name", builtInPatientData.getPreferredMiddleName());
        addColumn(dsd, "Surname", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Sex", builtInPatientData.getGender());
        addColumn(dsd, "Telephone", basePatientData.getTelephone());
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd, "Age", builtInPatientData.getAgeAtStart());
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd,"Enrollment Date",hivPatientData.getEnrollmentDate());
        addColumn(dsd, "Art Start Date", hivPatientData.getARTStartDate());
        addColumn(dsd,  "Regimen Start Date", hivPatientData.getFirstRegimenPickupDate());
        addColumn(dsd, "VL Date", hivPatientData.getLastViralLoadDateByEndDate());
        addColumn(dsd, "Clinic Stage", hivPatientData.getWHOClinicStage());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getViralLoadByEndDate());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "Last Appointment Date",hivPatientData.getLastEncounterByEndDate());
        addColumn(dsd, "Next Appointment Date",hivPatientData.getExpectedReturnDate());

        rd.addDataSetDefinition("NON_SUPPRESSED_VIRAL_LOAD", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(artcohortDefinition));

        return rd;
    }


    @Override
    public String getVersion() {
        return "2.0.9";
    }
}



