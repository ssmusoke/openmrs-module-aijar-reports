package org.openmrs.module.ugandaemrreports.reports;

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
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.converter.BirthDateConverter;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.openmrs.module.ugandaemrreports.library.Cohorts.transferIn;

/**
 */
@Component
public class SetupTransferInList extends UgandaEMRDataExportManager {

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
        return "e55efc18-cd94-46d0-bbc4-9a11ee49d84e";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "TransferInList.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:TI");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "795050b6-3804-46d7-b49f-cb146a6cbf74";
    }

    @Override
    public String getName() {
        return "Transfer In List";
    }

    @Override
    public String getDescription() {
        return "List of Clients Transferred In";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName("TI");
        dsd.addParameters(getParameters());
        dsd.addRowFilter(hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod(), "startDate=${startDate},endDate=${endDate}");


        //start constructing of the dataset
        PersonAttributeType phoneNumber = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");

        //identifier
        PatientIdentifierType ARTNo = MetadataUtils.existing(PatientIdentifierType.class, "e1731641-30ab-102d-86b0-7a5022ba4115");
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDefART = new ConvertedPatientDataDefinition("identifier",
                new PatientIdentifierDataDefinition(ARTNo.getName(), ARTNo), identifierFormatter);

        //start adding columns here

        dsd.addColumn("ARTNo", identifierDefART, "");
        dsd.addColumn("Names", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("DOB", builtInPatientData.getBirthdate(), "", new BirthDateConverter());
        dsd.addColumn("Age", new AgeDataDefinition(), "", new AgeConverter("{y}"));
        dsd.addColumn("Sex", new GenderDataDefinition(), (String) null);
        dsd.addColumn("PhoneNumber", new PersonAttributeDataDefinition("Phone Number", phoneNumber), "", new PersonAttributeDataConverter());
        addColumn(dsd,"Parish",df.getPreferredAddress("address4"));
        addColumn(dsd,"Village",df.getPreferredAddress("address5"));
        addColumn(dsd, "HIVEnrolledDate", hivPatientData.getEnrollmentDate());
        addColumn(dsd, "HealthCenterName", hivPatientData.getTransferInFacility());
        addColumn(dsd, "TransferInDate", hivPatientData.getSummaryPageDate());
        addColumn(dsd, "ArtStartDate", hivPatientData.getArtStartDate());
        addColumn(dsd, "TransferInRegimen", hivPatientData.getTransferInRegimen());
        addColumn(dsd, "BaselineCd4", hivPatientData.getAnyBaselineCD4());
        addColumn(dsd, "BaselineRegimen", hivPatientData.getBaselineRegimen());
        addColumn(dsd,"TPT Start Date",hivPatientData.getTPTInitiationDate());
        addColumn(dsd,"TPT End Date",hivPatientData.getTPTCompletionDate());
        addColumn(dsd,"Last TPT Status",hivPatientData.getTPTLastTPTStatus());

        rd.addDataSetDefinition("TI", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "1.1.3";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }
}
