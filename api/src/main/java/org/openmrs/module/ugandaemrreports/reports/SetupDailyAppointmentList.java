package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
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
public class SetupDailyAppointmentList extends UgandaEMRDataExportManager {

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
        return "418c7584-dd59-11e6-bf26-cec0c932ce01";
    }

    @Override
    public String getUuid() {
        return "418c78d6-dd59-11e6-bf26-cec0c932ce01";
    }

    @Override
    public String getName() {
        return "Daily Appointments List";
    }

    @Override
    public String getDescription() {
        return "Daily Appointments List";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "AppointmentList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:2,dataset:APPOINTMENT_LIST");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();

        CohortDefinition definition = df.getPatientsWithObsDuringDay(hivMetadata.getReturnVisitDate(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), BaseObsCohortDefinition.TimeModifier.ANY);

        dsd.setName(getName());
        dsd.addRowFilter(Mapped.mapStraightThrough(definition));
        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        addColumn(dsd,"Family Name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd,"Given Name", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd,"Sex", builtInPatientData.getGender());
        addColumn(dsd,"Birth Date", builtInPatientData.getBirthdate());
        addColumn(dsd, "Appointment Date", hivPatientData.getExpectedReturnDateBetween());
        addColumn(dsd,"Telephone",basePatientData.getTelephone());

        rd.addDataSetDefinition("APPOINTMENT_LIST", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(definition));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.3";
    }
}
