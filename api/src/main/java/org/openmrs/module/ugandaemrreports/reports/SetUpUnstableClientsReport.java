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
public class SetUpUnstableClientsReport extends UgandaEMRDataExportManager {

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
        return "1dd5626a-f1f6-4073-81d4-a7cf4f3afd4e";
    }

    @Override
    public String getExcelDesignUuid() {
        return "364443e9-2183-4deb-9c57-f527cd789600";
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
        props.put("repeatingSections", "sheet:1,row:4,dataset:UNSTABLECLIENTS");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }


    @Override
    public String getName() {
        return "Unstable Clients";
    }

    @Override
    public String getDescription() {
        return "Unstable Clients";
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
        CohortDefinition patientsOnThirdLineRegimenDuringPeriod = hivCohortDefinitionLibrary.getPatientsOnThirdLineRegimenDuringPeriod();
        CohortDefinition clinicalStage3 = hivCohortDefinitionLibrary.getPatientsOnClinicalStage3();
        CohortDefinition clinicalStage4 = hivCohortDefinitionLibrary.getPatientsOnClinicalStage4();
        CohortDefinition clinicalStage1or2 = df.getOnClinicalStage1or2();

        CohortDefinition patientsWithBadAdherence = hivCohortDefinitionLibrary.getPatientsWithPoorAdherence();
//        CohortDefinition artcohortDefinition = df.getPatientsInAny(patientsOnThirdLineRegimenDuringPeriod,patientsWithBadAdherence,df.getPatientsNotIn(clinicalStage1or2));
        CohortDefinition artcohortDefinition = df.getPatientsNotIn(df.getPatientsOnArtForMoreThansMonths(12));



        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(artcohortDefinition));
        addColumn(dsd, "Clinic number", hivPatientData.getClinicNumber());
        addColumn(dsd, "Middle Name", builtInPatientData.getPreferredMiddleName());
        addColumn(dsd, "Surname", builtInPatientData.getPreferredFamilyName());
        addColumn(dsd, "Given Name", builtInPatientData.getPreferredGivenName());
        addColumn(dsd, "Sex", builtInPatientData.getGender());
        dsd.addColumn("Birth Date", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        addColumn(dsd,  "Regimen Start Date", hivPatientData.getFirstRegimenPickupDate());
        addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
        addColumn(dsd, "Clinic Stage", hivPatientData.getWHOClinicStage());
        addColumn(dsd, "Adherence",hivPatientData.getAdherence());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "Age", hivPatientData.getAgeDuringPeriod());
        addColumn(dsd, "VL Qualitative",hivPatientData.getViralLoadQualitative());
        addColumn(dsd,"1",hivPatientData.getAdherence(0));
//        addColumn(dsd,"2",hivPatientData.getAdherence(1));
//        addColumn(dsd,"3",hivPatientData.getAdherence(2));
//        addColumn(dsd,"4",hivPatientData.getAdherence(3));
//        addColumn(dsd,"5",hivPatientData.getAdherence(4));
//        addColumn(dsd,"6",hivPatientData.getAdherence(5));
//        dsd.addColumn("Stable", sdd.definition("Stable", hivMetadata.getCurrentRegimen()), "onOrAfter=${startDate},onOrBefore=${endDate}", new RegimenLineConverter());

        rd.addDataSetDefinition("UNSTABLECLIENTS", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(artcohortDefinition));

        return rd;
    }


    @Override
    public String getVersion() {
        return "1.1.6";
    }
}



