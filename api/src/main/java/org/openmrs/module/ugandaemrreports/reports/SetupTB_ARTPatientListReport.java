package org.openmrs.module.ugandaemrreports.reports;


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
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *  TB ART Patient Listing Report
 */
@Component
public class SetupTB_ARTPatientListReport extends UgandaEMRDataExportManager {

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
    private BasePatientDataLibrary basePatientData;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {return "a5462c43-4601-462d-85fa-b76441f8fa9b";}

    public String getCSVDesignUuid() {
        return "a2e282b3-75c3-434a-8f13-373e36fd90fd";
    }

    @Override
    public String getUuid() {
        return "a7810d12-7eac-4e83-a78a-7110fd4bc4d3";
    }

    @Override
    public String getName() {
        return "Patient List for TB ART MER Indicator Report";
    }

    @Override
    public String getDescription() {
        return "TB ART Patient List Report";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TB_ART_PatientList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:TB_ART_PatientList");
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
        rd.addDataSetDefinition("TB_ART_PatientList", Mapped.mapStraightThrough(dsd));

        CohortDefinition returnToCareClients= hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28");

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(returnToCareClients));

        addColumn(dsd, "Clinic No", hivPatientData.getClinicNumber());
        addColumn(dsd, "EID No", hivPatientData.getEIDNumber());
        dsd.addColumn("Patient Name", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("Birth Date", new BirthdateDataDefinition(), "", new BirthdateConverter("MMM dd,yyyy"));
        addColumn(dsd, "Telephone", basePatientData.getTelephone());
        addColumn(dsd, "Age", builtInPatientData.getAgeAtStart());
        addColumn(dsd, "Enrollment Date", hivPatientData.getSummaryPageDate());
        addColumn(dsd, "Art Start Date", hivPatientData.getARTStartDate());
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd, "ART Start Date", hivPatientData.getArtStartDate());
        addColumn(dsd, "Current Regimen", hivPatientData.getCurrentRegimen());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd, "VL Date", hivPatientData.getViralLoadDate());
        addColumn(dsd,"VL Qualitative",hivPatientData.getVLQualitativeByEndDate());
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd,"Directions",hivPatientData.getDirectionsToPatientAddress());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastARTVisitEncounterByEndOfPreviousPeriod(df.getEncounterDatetimeConverter()));
        addColumn(dsd, "Expected Return Date", hivPatientData.getLatestExpectedReturnDateBeforeStartDate());
        addColumn(dsd,"TPT Start Date",hivPatientData.getTPTInitiationDate());
        addColumn(dsd,"TPT End Date",hivPatientData.getTPTCompletionDate());

        rd.addDataSetDefinition("TB_ART_PatientList", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(returnToCareClients));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.0.2";
    }
}