package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
@Component
public class SetupDueForViralLoad extends UgandaEMRDataExportManager {
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
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

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

        CohortDefinition onARTFor6Months = hivCohortDefinitionLibrary.getPatientsWhoStartedArtMonthsAgo("6m");
        CohortDefinition viralLoadDuringperiod = hivCohortDefinitionLibrary.getPatientsWithViralLoadDuringPeriod();

        CohortDefinition adultsDueForViralLoad = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(),
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromPeriod("12m"));

        CohortDefinition childDueForViralLoad = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(),
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromPeriod("6m"));

        CohortDefinition onArtFor6MonthsAndNoViralLoadTaken = df.getPatientsNotIn(onARTFor6Months,viralLoadDuringperiod);
        CohortDefinition dueForViralLoad = df.getPatientsInAny(onArtFor6MonthsAndNoViralLoadTaken,childDueForViralLoad,adultsDueForViralLoad);
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(dueForViralLoad));
        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        dsd.addColumn( "Patient Name",  new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn( "Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd, "HIV Enrolled Date", hivPatientData.getSummaryPageDate());
        addColumn(dsd, "ART Start Date", hivPatientData.getARTStartDate());
        addColumn(dsd, "Viral Load Date", hivPatientData.getLastViralLoadDateByEndDate());
        addColumn(dsd, "Viral Load Qualitative", hivPatientData.getVLQualitativeByEndDate());
        addColumn(dsd, "Viral Load", hivPatientData.getViralLoadByEndDate());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastEncounterByEndDate());
        addColumn(dsd, "Appointment Date", hivPatientData.getLastReturnDateByEndDate());
        addColumn(dsd, "Telephone", basePatientData.getTelephone());

        rd.addDataSetDefinition("DUE_FOR_VIRAL_LOAD", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(dueForViralLoad));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.66";
    }
}

