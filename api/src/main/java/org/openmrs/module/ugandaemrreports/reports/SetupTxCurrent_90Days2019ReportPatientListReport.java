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
 *  TX Current 90 Days Patient Listing Report - for MoH
 */
@Component
public class SetupTxCurrent_90Days2019ReportPatientListReport extends UgandaEMRDataExportManager {

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
    public String getExcelDesignUuid() {return "ee0e7008-0976-49bc-a9cd-a92425a8c820";}

    public String getCSVDesignUuid() {
        return "63e0c5ab-8995-4a20-8ff5-099556d4d662";
    }

    @Override
    public String getUuid() {
        return "0d297c01-73d7-42f1-95a1-7cb4a163f322";
    }

    @Override
    public String getName() {
        return "Patient List for Tx Current 90 days MER Indicator Report";
    }

    @Override
    public String getDescription() {
        return "TX_Current 90 days Patient List Report";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_CUR_PatientList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:TX_CUR_PatientList");
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
        rd.addDataSetDefinition("TX_CUR_PatientList", Mapped.mapStraightThrough(dsd));

        CohortDefinition activePatientsWithUpto90DaysLost= hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("90");

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(activePatientsWithUpto90DaysLost));

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
        addColumn(dsd, "VL Quantitative",  hivPatientData.getCurrentViralLoad());
        addColumn(dsd,"Directions",hivPatientData.getDirectionsToPatientAddress());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastARTVisitEncounterByEndOfPreviousPeriod(df.getEncounterDatetimeConverter()));
        addColumn(dsd, "Expected Return Date", hivPatientData.getLatestExpectedReturnDateBeforeStartDate());

        rd.addDataSetDefinition("TX_CUR_PatientList", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(activePatientsWithUpto90DaysLost));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.0.3";
    }
}