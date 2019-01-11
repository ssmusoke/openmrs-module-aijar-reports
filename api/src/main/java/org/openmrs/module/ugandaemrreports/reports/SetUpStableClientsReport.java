package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.RegimenLineConverter;
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
public class SetUpStableClientsReport extends UgandaEMRDataExportManager {

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
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private ARTCohortLibrary artCohortLibrary;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getUuid() {
        return "65193385-bdf6-4685-8078-4fef9c9e30d2";
    }

    @Override
    public String getExcelDesignUuid() {
        return "ebf67e8f-e022-4c5e-9729-c583562b896d";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "StabilityReport.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:4,dataset:STABILITYASSESSMENT");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }


    @Override
    public String getName() {
        return "Stable Clients";
    }

    @Override
    public String getDescription() {
        return "Stable Clients";
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
        CohortDefinition patientsWithGoodAdherence  = df.getPatientsWithGoodAdherenceForLast6Months();
        CohortDefinition patientsOnThirdLineRegimenDuringPeriod = hivCohortDefinitionLibrary.getPatientsOnThirdLineRegimenDuringPeriod();
        CohortDefinition stablePatients = df.getOnClinicalStage1or2();
//        CohortDefinition stablePatients =df.getViralLoadDuringPeriod(1.0,999.0);
        CohortDefinition patientsNotIn = df.getPatientsNotIn(patientsWithGoodAdherence,patientsOnThirdLineRegimenDuringPeriod);
//        CohortDefinition viralLoadAndClinincalStage = df.getPatientsInAll(viralLoadPatients,patientsNotIn);
//        CohortDefinition  stablePatients = df.getPatientsInAll(viralLoadAndClinincalStage,patientsNotIn);
//        CohortDefinition  stablePatients = df.getPatientsOnArtForMoreThansMonths(12);





        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(stablePatients));
        addColumn(dsd, "Clinic number", hivPatientData.getClinicNumber());
        addColumn(dsd, "Middle Name", builtInPatientData.getPreferredMiddleName());
        addColumn(dsd, "Surname", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Sex", builtInPatientData.getGender());
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd,  "Regimen Start Date", hivPatientData.getFirstRegimenPickupDate());
        addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
        addColumn(dsd, "Clinic Stage", hivPatientData.getWHOClinicStage());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd, "Adherence",hivPatientData.getAdherence());
        addColumn(dsd, "VL Qualitative",hivPatientData.getViralLoadQualitative());
        addColumn(dsd,"1",hivPatientData.getAdherence(0));
//        addColumn(dsd,"2",hivPatientData.getAdherence(1));
//        addColumn(dsd,"3",hivPatientData.getAdherence(2));
//        addColumn(dsd,"4",hivPatientData.getAdherence(3));
//        addColumn(dsd,"5",hivPatientData.getAdherence(4));
//        addColumn(dsd,"6",hivPatientData.getAdherence(5));
        dsd.addColumn("Stable", sdd.definition("Stable", hivMetadata.getCurrentRegimen()), "onOrAfter=${startDate},onOrBefore=${endDate}", new RegimenLineConverter());

        rd.addDataSetDefinition("STABILITYASSESSMENT", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(stablePatients));

        return rd;
    }



    @Override
    public String getVersion() {
        return "2.0.1.3";
    }
}



