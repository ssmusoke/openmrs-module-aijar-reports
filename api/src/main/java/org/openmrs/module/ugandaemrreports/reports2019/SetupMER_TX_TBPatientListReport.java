package org.openmrs.module.ugandaemrreports.reports2019;


import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.TBCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.TBPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 *  MER TX TB Patient Listing Report
 */
@Component
public class SetupMER_TX_TBPatientListReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private TBPatientDataLibrary tbPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private TBCohortDefinitionLibrary tbCohortDefinitionLibrary;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {return "3ac0b510-93ec-4089-a125-9cc9c6e9d605";}

    public String getCSVDesignUuid() {
        return "19b3a8ed-cd2f-41c3-b62b-36e9aff7aeb8";
    }

    @Override
    public String getUuid() {
        return "69162ab2-6d60-4e31-8c1b-89b8bb2f28c2";
    }

    @Override
    public String getName() {
        return "Patient List for TX TB MER Indicator Report";
    }

    @Override
    public String getDescription() {
        return "TX TB Patient List Report";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }


    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<>();
        l.add(buildReportDesign(reportDefinition));
        l.add(buildCSVReportDesign(reportDefinition));
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_TB_PatientList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:TX_TB_PatientList");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    public ReportDesign buildCSVReportDesign(ReportDefinition reportDefinition) {
        return createCSVDesign(getCSVDesignUuid(), reportDefinition);
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("TX_TB_PatientList", Mapped.mapStraightThrough(dsd));

        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();

        CohortDefinition startedTBTreatmentDuringPeriod = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getTBStatus(), hivMetadata.getARTEncounterPageEncounterType(), Arrays.asList(hivMetadata.getTBStatusRx()), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition newOnARTAndStartedTBTreatmentDuringPeriod = df.getPatientsInAll(havingArtStartDateDuringQuarter,startedTBTreatmentDuringPeriod);
        CohortDefinition previouslyOnARTAndStartedTBTreatmentDuringPeriod = df.getPatientsInAll(havingArtStartDateBeforeQuarter,startedTBTreatmentDuringPeriod);

        CohortDefinition ARTPatientsWithTB =  df.getPatientsInAny(newOnARTAndStartedTBTreatmentDuringPeriod,previouslyOnARTAndStartedTBTreatmentDuringPeriod);

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(ARTPatientsWithTB));

        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        addColumn(dsd, "EID No", hivPatientData.getEIDNumber());
        dsd.addColumn("Patient Name", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", new BirthdateDataDefinition(), "", new BirthdateConverter("MMM dd,yyyy"));
        addColumn(dsd, "Telephone", basePatientData.getTelephone());
        addColumn(dsd, "Age", builtInPatientData.getAgeAtStart());
        addColumn(dsd, "Enrollment Date", hivPatientData.getSummaryPageDate());
        addColumn(dsd, "Art Start Date", hivPatientData.getArtStartDate());
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd, "ART Start Date", hivPatientData.getArtStartDate());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
        addColumn(dsd,"VL Qualitative",hivPatientData.getVLQualitativeByEndDate());
        addColumn(dsd, "VL Copies",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd,"Directions",hivPatientData.getDirectionsToPatientAddress());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastARTVisitEncounterByEndOfPreviousPeriod(df.getEncounterDatetimeConverter()));
        addColumn(dsd, "Expected Return Date", hivPatientData.getLatestExpectedReturnDateBeforeStartDate());
        addColumn(dsd,"TPT Start Date",hivPatientData.getTPTInitiationDate());
        addColumn(dsd,"TPT End Date",hivPatientData.getTPTCompletionDate());
        addColumn(dsd,"Patient Type",tbPatientData.getPatientType());
        addColumn(dsd,"TB Status",tbPatientData.getTBStatus());
        addColumn(dsd,"TB RX Start Date",hivPatientData.getTBStartDate());
        addColumn(dsd,"TB RX End Date",hivPatientData.getTBStopDate());

        rd.addDataSetDefinition("TX_TB_PatientList", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(ARTPatientsWithTB));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.0.3";
    }
}