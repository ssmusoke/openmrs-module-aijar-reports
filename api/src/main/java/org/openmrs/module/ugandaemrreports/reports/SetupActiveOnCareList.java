package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.SMCAddressCalculation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Daily Appointments List report
 */
@Component
public class SetupActiveOnCareList extends UgandaEMRDataExportManager {

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
        return "929ccd38-7efb-4b7d-b6a5-255b258d5d01";
    }

    @Override
    public String getUuid() {
        return "9d6c7e51-2257-4f56-addc-e37e8272ff9d";
    }

    @Override
    public String getName() {
        return "Active Patients in Care";
    }

    @Override
    public String getDescription() {
        return "Active Patients in Care";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "ActiveoncareList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:ACTIVEONCARE_LIST");
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

        CohortDefinition deadPatients = df.getDeadPatientsDuringPeriod();
        CohortDefinition transferedOut = hivCohortDefinitionLibrary.getPatientsTransferredOutDuringPeriod();
        CohortDefinition exclusionpatients =df.getPatientsInAny(deadPatients,transferedOut);

        CohortDefinition hadEncounterInPeriod = hivCohortDefinitionLibrary.getArtPatientsWithEncounterOrSummaryPagesBetweenDates();

        CohortDefinition returnVisitDuringPeriod = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getReturnVisitDate(),
                Arrays.asList(hivMetadata.getARTEncounterEncounterType()), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition longAppointments = df.getPatientsWithLongRefills();
        CohortDefinition eligiblePatientOnCare = df.getPatientsInAny(returnVisitDuringPeriod,longAppointments,hadEncounterInPeriod);
        CohortDefinition definition =df.getPatientsNotIn(eligiblePatientOnCare,exclusionpatients);

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(definition));
        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        addColumn(dsd, "Family Name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Sex", builtInPatientData.getGender());
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd,"Age",hivPatientData.getAgeDuringPeriod());
        addColumn(dsd, "Telephone", basePatientData.getTelephone());
        addColumn(dsd,"Address",basePatientData.getAddressFull());
        addColumn(dsd,"EnrollmentDate",hivPatientData.getEnrollmentDate());
        addColumn(dsd,"ARTStartDate",hivPatientData.getArtStartDate());
        addColumn(dsd,"BaseLineCD4",hivPatientData.getBaselineCD4());
        addColumn(dsd,"BaselineRegimen",hivPatientData.getBaselineRegimen());
        addColumn(dsd,"CurrentRegimen",hivPatientData.getCurrentRegimen());

        addColumn(dsd,"lastEncounterDate",hivPatientData.getLastEncounterByEndDate());
        addColumn(dsd,"latestViralLoadDate",hivPatientData.getLastViralLoadDateByEndDate());
        addColumn(dsd,"latestViralLoad",hivPatientData.getViralLoadByEndDate());
        addColumn(dsd,"latestVLQualitative",hivPatientData.getVLQualitativeByEndDate());
        addColumn(dsd,"returnVisitDate",hivPatientData.getLastReturnDateByEndDate());




        rd.addDataSetDefinition("ACTIVEONCARE_LIST", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(definition));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.5";
    }
}
