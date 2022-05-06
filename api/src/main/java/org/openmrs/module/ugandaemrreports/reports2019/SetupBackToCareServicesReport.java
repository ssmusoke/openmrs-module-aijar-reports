package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 */
@Component
public class SetupBackToCareServicesReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    SharedDataDefintion sdd;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "6ea3e334-7c05-4577-8baa-84950afcf04b";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "Back_To_Care_2019.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:2,dataset:BACKTOCARE");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "6f1f996e-6f2c-4491-b055-2560242b1ab9";
    }

    @Override
    public String getName() {
        return "Back To Care Services";
    }

    @Override
    public String getDescription() {
        return "Back To Care Services";
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
        CohortDefinition btccohortDefinition = hivCohortDefinitionLibrary.getPatientsWithBackToCareEncounter();
//start constructing of the dataset
        PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
        addColumn(dsd, "ARTNo", hivPatientData.getClinicNumber());
        dsd.addColumn("Names", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("DOB", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{y}"));
        dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("PhoneNumber", new PersonAttributeDataDefinition("Phone Number", phoneNumber), "", new PersonAttributeDataConverter());
        addColumn(dsd,"District",df.getPreferredAddress("address2"));
        addColumn(dsd,"SubCounty",df.getPreferredAddress("address3"));
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd, "ArtStartDate", hivPatientData.getArtStartDate());
        addColumn(dsd, "Last Visit Date", hivPatientData.getLastVisitDate());
        addColumn(dsd, "Follow Up Visit Date",hivPatientData.getDateOfLastEncounter());
        addColumn(dsd, "Treatment Supporter", hivPatientData.getTreatmentSupporter());
        addColumn(dsd, "TreatmentSupporterNumber", hivPatientData.getTreatmentSupporterPhoneNumber());
        addColumn(dsd, "FollowUpAction", hivPatientData.getBackToCareFollowUpAction());
        addColumn(dsd,"FollowUpOutCome",hivPatientData.getBackToCareFollowUpOutCome());
        addColumn(dsd,"QuarteryOutcome",hivPatientData.getBackToCareFollowUpOutcomebyQuarter());
        dsd.addColumn("FollowUpPerson", sdd.getNameofProvider(), "onDate=${endDate}", new CalculationResultDataConverter());
        rd.addDataSetDefinition("BACKTOCARE", Mapped.mapStraightThrough(dsd));
        rd.setBaseCohortDefinition(Mapped.mapStraightThrough(btccohortDefinition));

        return rd;

    }

    @Override
    public String getVersion() {
        return "2.1.3";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }
}
