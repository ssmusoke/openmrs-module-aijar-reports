package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.openmrs.Concept;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultConverter;
import org.openmrs.module.ugandaemrreports.data.converter.EmctCodesConverter;
import org.openmrs.module.ugandaemrreports.data.converter.IptCtxConverter;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeConverter;
import org.openmrs.module.ugandaemrreports.data.converter.SyphilisTestConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.AgeLimitCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.ArvDrugsPreArtNumberCalcultion;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.BloodPressureCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.IycfMncCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.PersonAddressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WeightHeightMuacInrCalcultion;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WhoCd4VLCalculation;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
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

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "c357f8ea-25ca-11e7-8712-507b9dc4c741";
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
        props.put("repeatingSections", "sheet:1,row:10,dataset:ANC");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "d63d0202-25ca-11e7-8479-507b9dc4c741";
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
        rd.setParameters(getParameters());
        rd.addDataSetDefinition("ANC-DSD", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.2";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }

    private Concept getConcept(String uuid) {
        return Context.getConceptService().getConceptByUuid(uuid);
    }

    private DataDefinition definition(String name, Concept concept) {
        ObsForPersonDataDefinition obsForPersonDataDefinition = new ObsForPersonDataDefinition();
        obsForPersonDataDefinition.setName(name);
        obsForPersonDataDefinition.setQuestion(concept);
        obsForPersonDataDefinition.setWhich(TimeQualifier.LAST);
        return obsForPersonDataDefinition;
    }

    private DataDefinition villageParish(){
        CalculationDataDefinition cdf =new CalculationDataDefinition("village+parish", new PersonAddressCalculation());
        return cdf;
    }
    private DataDefinition age(Integer lower, Integer upper) {
        CalculationDataDefinition cdf = new CalculationDataDefinition("Age-"+lower+"-"+upper+"yrs", new AgeLimitCalculation());
        cdf.addCalculationParameter("lowerLimit", lower);
        cdf.addCalculationParameter("upperLimit", upper);
        return cdf;
    }

    private DataDefinition weightHeightMuacInr() {
        CalculationDataDefinition cdf = new CalculationDataDefinition("whmi", new WeightHeightMuacInrCalcultion());
        return cdf;
    }
    private DataDefinition bloodPressure(){
        CalculationDataDefinition cdf = new CalculationDataDefinition("bp", new BloodPressureCalculation());
        return cdf;
    }
    private DataDefinition whoCd4Vl(){
        return new CalculationDataDefinition("WHO/CD4/VL", new WhoCd4VLCalculation());
    }
    private DataDefinition arvDrugsPreArtNumber(){
        return new CalculationDataDefinition("ARVs drugs/Pre-ART No", new ArvDrugsPreArtNumberCalcultion());
    }
    private DataDefinition iycfMnc(){
        return new CalculationDataDefinition("IYCF/MNC", new IycfMncCalculation());
    }

    private DataSetDefinition dataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        String parameterMapping = "startDate=${startDate},endDate=${endDate}";
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(onlyFemaleWithAncEncounterType(), parameterMapping );

        //start constructing of the dataset
        PersonAttributeType attribute = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");

        //start adding columns here

        dsd.addColumn("Serial No", definition("Serial No",  getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "", new ObsDataConverter());
        dsd.addColumn("Client No", definition("Client No",  getConcept("38460266-6bcd-47e8-844c-649d34323810")), "", new ObsDataConverter());
        dsd.addColumn("Name of Client", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Village+Parish", villageParish(), "", new CalculationResultConverter());
        dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", attribute), "", new PersonAttributeConverter());
        dsd.addColumn("Age-10-19yrs", age(10,19), "", new CalculationResultConverter());
        dsd.addColumn("Age-20-24yrs", age(20,24), "", new CalculationResultConverter());
        dsd.addColumn("Age-25+yrs", age(25,200), "", new CalculationResultConverter());
        dsd.addColumn("ANC Visit", definition("ANC Visit",  getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb")), "", new ObsDataConverter());
        dsd.addColumn("Gravida", definition("Gravida",  getConcept("dcc39097-30ab-102d-86b0-7a5022ba4115")), "", new ObsDataConverter());
        dsd.addColumn("Parity", definition("Parity",  getConcept("1053AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "", new ObsDataConverter());
        dsd.addColumn("Gestational Age", definition("Gestational Age",  getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115")), "", new ObsDataConverter());
        dsd.addColumn("ANC1 Timing", definition("ANC1 Timing",  getConcept("3a862ab6-7601-4412-b626-d373c1d4a51e")), "", new ObsDataConverter());
        dsd.addColumn("EDD", definition("EDD", getConcept("dcc033e5-30ab-102d-86b0-7a5022ba4115")), "", new ObsDataConverter());
        dsd.addColumn("WHMI", weightHeightMuacInr(), "", new CalculationResultConverter());
        dsd.addColumn("BP", bloodPressure(), "", new CalculationResultConverter());
        dsd.addColumn("EMTCT codesW", definition("EMTCT codesW", getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "", new EmctCodesConverter());
        dsd.addColumn("EMTCT codesP", definition("EMTCT codesP", getConcept("62a37075-fc2a-4729-8950-b9fae9")), "", new EmctCodesConverter());
        dsd.addColumn("Diagnosis", definition("Diagnosis", getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "", new ObsDataConverter());
        dsd.addColumn("WHO/CD4/VL", whoCd4Vl(), "", new CalculationResultConverter());
        dsd.addColumn("ARVs drugs/Pre-ART No", arvDrugsPreArtNumber(), "",  new CalculationResultConverter());
        dsd.addColumn("IYCF/MNC", iycfMnc(), "", new CalculationResultConverter());
        dsd.addColumn("TB Status", definition("TB Status", getConcept("dce02aa1-30ab-102d-86b0-7a5022ba4115")), "", new ObsDataConverter());
        dsd.addColumn("Haemoglobin", definition("Haemoglobin", getConcept("dc548e89-30ab-102d-86b0-7a5022ba4115")), "", new ObsDataConverter());
        dsd.addColumn("Syphilis testW", definition("Syphilis testW", getConcept("275a6f72-b8a4-4038-977a-727552f69cb8")), "", new SyphilisTestConverter());
        dsd.addColumn("Syphilis testP", definition("Syphilis testP", getConcept("d8bc9915-ed4b-4df9-9458-72ca1bc2cd06")), "", new SyphilisTestConverter());
        dsd.addColumn("FPC", definition("FPC", getConcept("0815c786-5994-49e4-aa07-28b662b0e428")), "", new ObsDataConverter());
        dsd.addColumn("TT", definition("FPC", getConcept("0815c786-5994-49e4-aa07-28b662b0e428")), "", new ObsDataConverter());
        dsd.addColumn("IPT/CTX", definition("IPT/CTX", getConcept("1da3cb98-59d8-4bfd-b0bb-c9c1bcd058c6")), "", new IptCtxConverter());
        dsd.addColumn("Free LLIN", definition("Free LLIN", getConcept("3e7bb52c-e6ae-4a0b-bce0-3b36286e8658")), "", new ObsDataConverter());
        dsd.addColumn("Mebendazole", definition("Mebendazole", getConcept("9d6abbc4-707a-4ec7-a32a-4090b1c3af87")), "", new ObsDataConverter());
        dsd.addColumn("Iron given", definition("Iron given", getConcept("315825e8-8ba4-4551-bdd1-aa4e02a36639")), "", new ObsDataConverter());
        dsd.addColumn("Folic acid given", definition("Folic acid given", getConcept("8c346216-c444-4528-a174-5139922218ed")), "", new ObsDataConverter());
        dsd.addColumn("Other treatments", definition("Other treatments", getConcept("2aa72406-436e-490d-8aa4-d5336148204f")), "", new ObsDataConverter());
        dsd.addColumn("Referal In/Out", definition("Referal In/Out", getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd")), "", new ObsDataConverter());
        dsd.addColumn("Risk Factor/Complications", definition("Risk Factor/Complications", getConcept("120186AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "", new ObsDataConverter());

        return dsd;
    }

    private CohortDefinition onlyFemaleWithAncEncounterType(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();

        GenderCohortDefinition gender = new GenderCohortDefinition();
        gender.setFemaleIncluded(true);
        gender.setMaleIncluded(false);

        EncounterCohortDefinition enc = new EncounterCohortDefinition();
        enc.addParameter(new Parameter("onOrAfter", "Start Date", Date.class));
        enc.addParameter(new Parameter("onOrBefore", "End Date", Date.class));
        enc.addEncounterType(Context.getEncounterService().getEncounterTypeByUuid("044daI6d-f80e-48fe-aba9-037f241905Pe"));

        //combine the 2 cohortDefinitions
        cd.setName("femaleAndInANC");
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addSearch("gender", ReportUtils.map(gender, ""));
        cd.addSearch("anc", ReportUtils.map(enc, "onOrAfter=${startDate},onOrBefore=${endDate}"));
        cd.setCompositionString("gender AND enc");
        return cd;
    }
}
