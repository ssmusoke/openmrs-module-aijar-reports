package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.AgeConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.GlobalPropertyParametersDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 */
@Component
public class SetupEMTCTDataExport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "eec9f392-ff80-4667-b4db-135d345aa5aa";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "EMTCTDataExport.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:2,dataset:APP");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "bf79f017-8591-4eaf-88c9-1cde33226517";
    }

    @Override
    public String getName() {
        return "EMTCT Data Export";
    }

    @Override
    public String getDescription() {
        return "Export for the EMTCT module integration";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName("APP");
        dsd.addParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(Cohorts.patientWithAppoinment()));


        //start constructing of the dataset
        PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");

        //identifier
        PatientIdentifierType ARTNo = MetadataUtils.existing(PatientIdentifierType.class, "e1731641-30ab-102d-86b0-7a5022ba4115");
        PatientIdentifierType EIDNo = MetadataUtils.existing(PatientIdentifierType.class, "2c5b695d-4bf3-452f-8a7c-fe3ee3432ffe");
        PatientIdentifierType OpenMRSID = MetadataUtils.existing(PatientIdentifierType.class, "05a29f94-c0ed-11e2-94be-8c13b969e334");
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDefART = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(ARTNo.getName(), ARTNo), identifierFormatter);
        DataDefinition identifierDefEID = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(EIDNo.getName(), EIDNo), identifierFormatter);
        DataDefinition identifierDefOpenMRSID = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(OpenMRSID.getName(), OpenMRSID), identifierFormatter);


        //start adding columns here
        dsd.addColumn("ARTNo", identifierDefART, "");
        dsd.addColumn("EIDNo", identifierDefEID, "");
        dsd.addColumn("OpenMRSID", identifierDefOpenMRSID, "");
        dsd.addColumn("Names", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("DOB", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{y}"));
        dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("PhoneNumber", new PersonAttributeDataDefinition("Phone Number", phoneNumber), "", new PersonAttributeDataConverter());
        dsd.addColumn("EDD", sdd.definition("EDD", getConcept("dcc033e5-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("NextAppointmentDate", sdd.definition("NextAppointmentDate", getConcept("dcac04cf-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

        rd.addDataSetDefinition("APP", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }

    protected DataSetDefinition settings() {
        GlobalPropertyParametersDatasetDefinition cst = new GlobalPropertyParametersDatasetDefinition();
        cst.setName("S");
        cst.setGp("aijar.healthCenterName");
        return cst;
    }

    private Concept getConcept(String uuid) {
        return Dictionary.getConcept(uuid);
    }
}
