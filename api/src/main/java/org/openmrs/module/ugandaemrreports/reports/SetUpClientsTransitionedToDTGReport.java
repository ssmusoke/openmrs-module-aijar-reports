package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
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
public class SetUpClientsTransitionedToDTGReport extends UgandaEMRDataExportManager {

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
        return "e38117c0-9ddd-4d51-b3b7-a39e38fd85a1";
    }

    @Override
    public String getExcelDesignUuid() {
        return "6e4805da-5630-477b-8a2f-01e77a6b88d9";
    }

    public String getCSVDesignUuid() {
        return "f27df46e-d7ae-4716-9492-4c1fc5be2a1e";
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

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     * @param reportDefinition
     * @return The report design
     */
    public ReportDesign buildExcelReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "TransitionedToDTG.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:TRANSITIONED_TO_DTG");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }


    @Override
    public String getName() {
        return "Patients Transitioned to DTG/TLD Report";
    }

    @Override
    public String getDescription() {
        return "Patients transitioned to DTG/TLD regimens";
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

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        CohortDefinition patientsOnDTGInPeriod = df.getPatientsNotIn(hivCohortDefinitionLibrary.getPatientsOnDTGRegimenInPeriod(), df.getDeadPatientsDuringPeriod(), hivCohortDefinitionLibrary.getTransferredOut());
        CohortDefinition patientsOnDTGBeforePeriod = df.getPatientsNotIn(hivCohortDefinitionLibrary.getPatientsOnDTGRegimenBeforePeriod(), df.getDeadPatientsByEndOfPreviousDate("1d"));
        CohortDefinition transitionedToDTGCohort =  df.getPatientsNotIn(patientsOnDTGInPeriod, patientsOnDTGBeforePeriod);

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(transitionedToDTGCohort, "startDate=${startDate},endDate=${endDate}");
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
        addColumn(dsd, "Previous Regimen", hivPatientData.getPreviousRegimen());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "Last Appointment Date",hivPatientData.getLastEncounterByEndDate());
        addColumn(dsd, "Next Appointment Date",hivPatientData.getExpectedReturnDate());

        rd.addDataSetDefinition("TRANSITIONED_TO_DTG", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(transitionedToDTGCohort));

        return rd;
    }


    @Override
    public String getVersion() {
        return "3.0.0";
    }
}



