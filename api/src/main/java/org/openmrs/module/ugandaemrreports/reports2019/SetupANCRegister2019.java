package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.*;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.ANCEncounterDateCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.*;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 */
@Component
public class SetupANCRegister2019 extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;

    @Autowired
    BasePatientDataLibrary basePatientDataLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "e8ef4fe4-3c78-11e7-899c-507b9dc4c741";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        l.add(buildExcelDesign(reportDefinition));
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
        ReportDesign rd = createCSVDesign(getExcelDesignUuid(), reportDefinition);
        return rd;
    }
    public ReportDesign buildExcelDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign("8da7db73-08c9-4afa-9c38-b7751aa5e749", reportDefinition, "ANCRegister2019.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:10-13,dataset:ANC");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "95b4a987-2a8d-41f2-94b2-9c5bbeef9ca9";
    }

    @Override
    public String getName() {
        return "Intergrated Antenatal Register";
    }

    @Override
    public String getDescription() {
        return "It contains ANC information about the mother";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        rd.addDataSetDefinition("ANC", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "2.0.5";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }




    private DataSetDefinition dataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName("ANC");
        dsd.addParameters(getParameters());
        dsd.addRowFilter(Cohorts.genderAndHasAncEncounter(true, false, "044daI6d-f80e-48fe-aba9-037f241905Pe"), "startDate=${startDate},endDate=${endDate}");

        //start adding columns here
        dsd.addColumn("Visit Date", getEncounterDate(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Serial No", sdd.definition("Serial No",  sdd.getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Client No", sdd.definition("Client No",  sdd.getConcept("38460266-6bcd-47e8-844c-649d34323810")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("NIN", sdd.getNationalIDNumber(), "");
        dsd.addColumn("Name of Client", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Village", basePatientDataLibrary.getVillage(),(String)null);
        dsd.addColumn("Parish", basePatientDataLibrary.getParish(),(String)null);
        dsd.addColumn("County", basePatientDataLibrary.getCounty(),(String)null);
        dsd.addColumn("District", basePatientDataLibrary.getDistrict(),(String)null);
        dsd.addColumn("Nationality", new PersonAttributeDataDefinition("Nationality", sdd.getPatientNationality()), "", new NationalityPersonalAttributeDataConverter());
        dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", sdd.getPhoneNumber()), "", new PersonAttributeDataConverter());
        dsd.addColumn("Age", sdd.getAgeDataDefinition(10,200), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("ANC Visit", sdd.definition("ANC Visit",  sdd.getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Gravida", sdd.definition("Gravida",  sdd.getConcept("dcc39097-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Parity", sdd.definition("Parity",  sdd.getConcept("1053AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Gestational Age", sdd.definition("Gestational Age",  sdd.getConcept("dcd034ed-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("ANC1 Timing", sdd.definition("ANC1 Timing",  sdd.getConcept("3a862ab6-7601-4412-b626-d373c1d4a51e")), "onOrAfter=${startDate},onOrBefore=${endDate}", new Anc1TimingDataConverter());
        dsd.addColumn("EDD", sdd.definition("EDD", sdd.getConcept("5b110c7d-8031-4526-9724-f262d6e2733e")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Weight", sdd.definition("Weight",  sdd.getConcept("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Height", sdd.definition("Height",  sdd.getConcept("5090AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("MUAC", sdd.definition("MUAC",  sdd.getConcept("5f86d19d-9546-4466-89c0-6f80c101191b")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MUACDataConverter());
        dsd.addColumn("INR NO", sdd.definition("INR NO",  sdd.getConcept("b644c29c-9bb0-447e-9f73-2ae89496a709")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("BP", sdd.getBloodPressure(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("EMTCT codesW", sdd.definition("EMTCT codesW", sdd.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
        dsd.addColumn("STK Given", sdd.definition("STK Given", sdd.getConcept("609c9aee-92b3-4e17-828a-efc7933f2ecf")), "onOrAfter=${startDate},onOrBefore=${endDate}", new STKDataConverter());
        dsd.addColumn("AgeP", sdd.definition("AgeP", sdd.getConcept("4049d989-b99e-440d-8f70-c222aa9fe45c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("FacilityResult", sdd.definition("FacilityResult", sdd.getConcept("e47d9ead-5b33-4315-9ea0-42669e4491b6")), "onOrAfter=${startDate},onOrBefore=${endDate}", new STKResultDataConverter());

        dsd.addColumn("EMTCT codesP", sdd.definition("EMTCT codesP", sdd.getConcept("62a37075-fc2a-4729-8950-b9fae9")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
        dsd.addColumn("Diagnosis", sdd.definition("Diagnosis", sdd.getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("WHO", sdd.definition("WHO", sdd.getConcept("dcdff274-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new WHODataConverter());
        dsd.addColumn("CD4", sdd.getWHOCD4ViralLoadCalculation("dcbcba2c-30ab-102d-86b0-7a5022ba4115", "159376AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("VL", sdd.getWHOCD4ViralLoadCalculation("dc8d83e3-30ab-102d-86b0-7a5022ba4115", "0b434cfa-b11c-4d14-aaa2-9aed6ca2da88"), "onDate=${endDate}", new CalculationResultDataConverter());
       // dsd.addColumn("ARVs drugs", sdd.definition("ARVs drugs", sdd.getConcept("a615f932-26ee-449c-8e20-e50a15232763")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ARVsDataConverter());
        dsd.addColumn("Pre-ART No", sdd.getPreARTNumber(), "");
        dsd.addColumn("IYCF", sdd.definition("IYCF", sdd.getConcept("5d993591-9334-43d9-a208-11b10adfad85")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IYCFDataConverter());
        dsd.addColumn("MNC", sdd.definition("MNC", sdd.getConcept("af7dccfd-4692-4e16-bd74-5ac4045bb6bf")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MNCDataConverter());
        dsd.addColumn("TB Status", sdd.definition("TB Status", sdd.getConcept("dce02aa1-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Haemoglobin", sdd.definition("Haemoglobin", sdd.getConcept("dc548e89-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Syphilis testW", sdd.definition("Syphilis testW", sdd.getConcept("275a6f72-b8a4-4038-977a-727552f69cb8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SyphilisTestDataConverter());
        dsd.addColumn("Syphilis testP", sdd.definition("Syphilis testP", sdd.getConcept("d8bc9915-ed4b-4df9-9458-72ca1bc2cd06")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SyphilisTestDataConverter());
        dsd.addColumn("FPC", sdd.definition("FPC", sdd.getConcept("0815c786-5994-49e4-aa07-28b662b0e428")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FpcDataConverter());
        dsd.addColumn("TT", sdd.definition("TT", sdd.getConcept("39217e3d-6a39-4679-bf56-f0954a7ffdb8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TetanusDataConverter());
        dsd.addColumn("IPT/CTX", sdd.definition("IPT/CTX", sdd.getConcept("1da3cb98-59d8-4bfd-b0bb-c9c1bcd058c6")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IptCtxDataConverter());
        dsd.addColumn("Free LLIN", sdd.definition("Free LLIN", sdd.getConcept("3e7bb52c-e6ae-4a0b-bce0-3b36286e8658")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FreeLlinDataConverter());
        dsd.addColumn("Mebendazole", sdd.definition("Mebendazole", sdd.getConcept("9d6abbc4-707a-4ec7-a32a-4090b1c3af87")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MebendazoleDataConverter());
        dsd.addColumn("Iron given", sdd.getIronGiven(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Folic acid given", sdd.getFolicAcidGiven(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Other treatments", sdd.definition("Other treatments", sdd.getConcept("2aa72406-436e-490d-8aa4-d5336148204f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Referal In/Out", sdd.referredToOrFrom(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Risk Factor/Complications", sdd.definition("Risk Factor/Complications", sdd.getConcept("120186AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

        return dsd;
    }
    private DataDefinition getEncounterDate() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Visit Date", new ANCEncounterDateCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
}
