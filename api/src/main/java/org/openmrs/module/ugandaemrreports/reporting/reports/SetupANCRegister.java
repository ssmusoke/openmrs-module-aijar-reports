package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.Anc1TimingConverter;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultConverter;
import org.openmrs.module.ugandaemrreports.data.converter.EmctCodesConverter;
import org.openmrs.module.ugandaemrreports.data.converter.FpcDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.FreeLlinDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.IptCtxConverter;
import org.openmrs.module.ugandaemrreports.data.converter.MebendazoleDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeConverter;
import org.openmrs.module.ugandaemrreports.data.converter.SyphilisTestConverter;
import org.openmrs.module.ugandaemrreports.data.converter.TetanusDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.AgeLimitCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.ArvDrugsPreArtNumberCalcultion;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.BloodPressureCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.FolicAcidCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.IronGivenCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.IycfMncCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.PersonAddressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.ReferalCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WeightHeightMuacInrCalcultion;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WhoCd4VLCalculation;
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
 * Created by ningosi on 4/20/17.
 */
@Component
public class SetupANCRegister extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;

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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "ANCRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:ANC-DSD");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "f717a684-3c78-11e7-adec-507b9dc4c741";
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
        rd.addDataSetDefinition("ANC-DSD", Mapped.mapStraightThrough(dataSetDefinition()));
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

    private Concept getConcept(String uuid) {
        return Dictionary.getConcept(uuid);
    }

    private DataDefinition villageParish(){
        CalculationDataDefinition cdf =new CalculationDataDefinition("village+parish", new PersonAddressCalculation());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    private DataDefinition age(Integer lower, Integer upper) {
        CalculationDataDefinition cdf = new CalculationDataDefinition("Age-"+lower+"-"+upper+"yrs", new AgeLimitCalculation());
        cdf.addCalculationParameter("lowerLimit", lower);
        cdf.addCalculationParameter("upperLimit", upper);
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }

    private DataDefinition weightHeightMuacInr() {
        CalculationDataDefinition cdf = new CalculationDataDefinition("whmi", new WeightHeightMuacInrCalcultion());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    private DataDefinition bloodPressure(){
        CalculationDataDefinition cdf = new CalculationDataDefinition("bp", new BloodPressureCalculation());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    private DataDefinition whoCd4Vl(){
        CalculationDataDefinition cd = new CalculationDataDefinition("WHO/CD4/VL", new WhoCd4VLCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    private DataDefinition arvDrugsPreArtNumber(){
        CalculationDataDefinition cd = new CalculationDataDefinition("ARVs drugs/Pre-ART No", new ArvDrugsPreArtNumberCalcultion());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    private DataDefinition iycfMnc(){
        CalculationDataDefinition cd = new CalculationDataDefinition("IYCF/MNC", new IycfMncCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    private DataDefinition referal(){
        CalculationDataDefinition cd = new CalculationDataDefinition("Referal In/Out", new ReferalCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    private DataDefinition ironGiven() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Iron given", new IronGivenCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataDefinition folicAcidGiven() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Folic acid given", new FolicAcidCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataSetDefinition dataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName(getName());
        dsd.addParameters(getParameters());
        dsd.addRowFilter(Cohorts.genderAndHasAncEncounter(true, false, "044daI6d-f80e-48fe-aba9-037f241905Pe"), "startDate=${startDate},endDate=${endDate}");


        //start constructing of the dataset
        PersonAttributeType attribute = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");

        //start adding columns here

        dsd.addColumn("Serial No", sdd.definition("Serial No",  getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Client No", sdd.definition("Client No",  getConcept("38460266-6bcd-47e8-844c-649d34323810")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Name of Client", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Village+Parish", villageParish(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", attribute), "", new PersonAttributeConverter());
        dsd.addColumn("Age-10-19yrs", age(10,19), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age-20-24yrs", age(20,24), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age-25+yrs", age(25,200), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("ANC Visit", sdd.definition("ANC Visit",  getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Gravida", sdd.definition("Gravida",  getConcept("dcc39097-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Parity", sdd.definition("Parity",  getConcept("1053AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Gestational Age", sdd.definition("Gestational Age",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("ANC1 Timing", sdd.definition("ANC1 Timing",  getConcept("3a862ab6-7601-4412-b626-d373c1d4a51e")), "onOrAfter=${startDate},onOrBefore=${endDate}", new Anc1TimingConverter());
        dsd.addColumn("EDD", sdd.definition("EDD", getConcept("dcc033e5-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("WHMI", weightHeightMuacInr(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("BP", bloodPressure(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("EMTCT codesW", sdd.definition("EMTCT codesW", getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesConverter());
        dsd.addColumn("EMTCT codesP", sdd.definition("EMTCT codesP", getConcept("62a37075-fc2a-4729-8950-b9fae9")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesConverter());
        dsd.addColumn("Diagnosis", sdd.definition("Diagnosis", getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("WHO/CD4/VL", whoCd4Vl(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("ARVs drugs/Pre-ART No", arvDrugsPreArtNumber(), "onDate=${endDate}",  new CalculationResultConverter());
        dsd.addColumn("IYCF/MNC", iycfMnc(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("TB Status", sdd.definition("TB Status", getConcept("dce02aa1-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Haemoglobin", sdd.definition("Haemoglobin", getConcept("dc548e89-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Syphilis testW", sdd.definition("Syphilis testW", getConcept("275a6f72-b8a4-4038-977a-727552f69cb8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SyphilisTestConverter());
        dsd.addColumn("Syphilis testP", sdd.definition("Syphilis testP", getConcept("d8bc9915-ed4b-4df9-9458-72ca1bc2cd06")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SyphilisTestConverter());
        dsd.addColumn("FPC", sdd.definition("FPC", getConcept("0815c786-5994-49e4-aa07-28b662b0e428")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FpcDataConverter());
        dsd.addColumn("TT", sdd.definition("TT", getConcept("39217e3d-6a39-4679-bf56-f0954a7ffdb8")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TetanusDataConverter());
        dsd.addColumn("IPT/CTX", sdd.definition("IPT/CTX", getConcept("1da3cb98-59d8-4bfd-b0bb-c9c1bcd058c6")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IptCtxConverter());
        dsd.addColumn("Free LLIN", sdd.definition("Free LLIN", getConcept("3e7bb52c-e6ae-4a0b-bce0-3b36286e8658")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FreeLlinDataConverter());
        dsd.addColumn("Mebendazole", sdd.definition("Mebendazole", getConcept("9d6abbc4-707a-4ec7-a32a-4090b1c3af87")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MebendazoleDataConverter());
        dsd.addColumn("Iron given", ironGiven(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Folic acid given", folicAcidGiven(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Other treatments", sdd.definition("Other treatments", getConcept("2aa72406-436e-490d-8aa4-d5336148204f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Referal In/Out", referal(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Risk Factor/Complications", sdd.definition("Risk Factor/Complications", getConcept("120186AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

        return dsd;
    }
}
