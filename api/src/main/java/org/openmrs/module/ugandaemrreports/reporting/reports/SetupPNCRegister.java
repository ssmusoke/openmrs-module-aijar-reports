/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.reporting.reports;

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
import org.openmrs.module.ugandaemrreports.data.converter.BabyStatusDataConveter;
import org.openmrs.module.ugandaemrreports.data.converter.BreastStatusDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultConverter;
import org.openmrs.module.ugandaemrreports.data.converter.CervixStatusDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.EmctCodesConverter;
import org.openmrs.module.ugandaemrreports.data.converter.FpPNCDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.ImmunizationDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.MotherDiagnosisDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeConverter;
import org.openmrs.module.ugandaemrreports.data.converter.RoutineAdminDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.TimingForPNCDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.AgeLimitCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.ArvDrugsPreArtNumberCalcultion;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.PersonAddressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WeightHeightMuacInrCalcultion;
import org.openmrs.module.ugandaemrreports.reporting.calculation.anc.WhoCd4VLCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.pnc.IfoIycfMncCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.pnc.RtwRfwCalculation;
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
 * Created by Nicholas Ingosi on 5/10/17.
 */
@Component
public class SetupPNCRegister extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;

    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "6625672c-3ee1-11e7-ad03-507b9dc4c741";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "PNCRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:7,dataset:PNC-DSD");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "489baf0e-3ee1-11e7-a1d1-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "Integrated Postnatal Register";
    }

    @Override
    public String getDescription() {
        return "Contains mothers information after delivery";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        rd.addDataSetDefinition("PNC-DSD", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.6";
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
        dsd.setName(getName());
        dsd.addParameters(getParameters());
        dsd.addRowFilter(Cohorts.genderAndHasAncEncounter(true, false, "fa6f3ff5-b784-43fb-ab35-a08ab7dbf074"), "startDate=${startDate},endDate=${endDate}");

        PersonAttributeType attribute = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
        PatientIdentifierType clientNo = MetadataUtils.existing(PatientIdentifierType.class, "758ef6e4-9ceb-4137-bc8d-9246dc7b41fe");
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(clientNo.getName(), clientNo), identifierFormatter);



        //start adding columns
        dsd.addColumn("Serial No", sdd.definition("Serial No",  getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Client No", identifierDef, "");
        dsd.addColumn("Mother Name", new PreferredNameDataDefinition(), (String) null);
        //dsd.addColumn("Father Name", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Village+Parish", villageParish(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", attribute), "", new PersonAttributeConverter());
        dsd.addColumn("Age-10-19yrs", age(10,19), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age-20-24yrs", age(20,24), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Age-25+yrs", age(25,200), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("6 Hours", sdd.definition("6 Hours", getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("6 Days", sdd.definition("6 Days", getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("6 Weeks", sdd.definition("6 Weeks", getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("6 Months", sdd.definition("6 Months", getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("FP", sdd.definition("FP", getConcept("dc7620b3-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FpPNCDataConverter());
        dsd.addColumn("Breast Status", sdd.definition("Breast Status", getConcept("07c10f5c-17fd-4a7e-8d72-c2252f589da0")), "onOrAfter=${startDate},onOrBefore=${endDate}", new BreastStatusDataConverter());
        dsd.addColumn("Cervix Status", sdd.definition("Cervix Status", getConcept("d858f8cb-fe9e-4131-8d91-cd9929cc53de")), "onOrAfter=${startDate},onOrBefore=${endDate}", new CervixStatusDataConverter());
        dsd.addColumn("WHMI-M", weightHeightMuacInr(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("EMTCT codesW", sdd.definition("EMTCT codesW", getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesConverter());
        dsd.addColumn("EMTCT codesP", sdd.definition("EMTCT codesP", getConcept("62a37075-fc2a-4729-8950-b9fae9")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesConverter());
        dsd.addColumn("ARVs drugs/Pre-ART No", arvDrugsPreArtNumber(), "onDate=${endDate}",  new CalculationResultConverter());
        dsd.addColumn("Iron", sdd.definition("Iron", getConcept("315825e8-8ba4-4551-bdd1-aa4e02a36639")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("Folic Acid", sdd.definition("Folic Acid", getConcept("8c346216-c444-4528-a174-5139922218ed")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("Vit A", sdd.definition("Vit A", getConcept("88ec2c8b-eb7b-4595-8612-1871568507a5")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("CTX", sdd.definition("CTX", getConcept("d12abd7f-c90d-4798-9240-0f2f81977183")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("Diagnosis-M", sdd.definition("Diagnosis", getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MotherDiagnosisDataConverter());
        dsd.addColumn("WHO/CD4/VL", whoCd4Vl(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("Other treatment mother", sdd.definition("Other treatment mother", getConcept("2aa72406-436e-490d-8aa4-d5336148204f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Baby status", sdd.definition("Baby status", getConcept("dd8a2ad9-16f6-44db-82d7-87d6eef14886")), "onOrAfter=${startDate},onOrBefore=${endDate}", new BabyStatusDataConveter());
        dsd.addColumn("Age", sdd.definition("Age", getConcept("164438AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("WT", sdd.definition("WT", getConcept("94e4aeea-84d0-4207-aacb-ce38fe8e109c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Diagnosis-C", sdd.definition("Diagnosis-C", getConcept("a16b3a8e-6412-4344-908a-2a96344fa017")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("IFO/IYCF/MNC", ifoIycfMnc(), "onDate=${endDate}", new CalculationResultConverter());
        dsd.addColumn("BCG", sdd.definition("BCG", getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("OPV", sdd.definition("OPV", getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("DPT", sdd.definition("DPT", getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("Vit A", sdd.definition("Vit A", getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("PCV", sdd.definition("PCV", getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("Other treatment child", sdd.definition("Other treatment child", getConcept("59560ede-43e2-4e56-a47e-0f876779f0e1")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("RTW/RFW", referredToOrFrom(), "onDate=${endDate}", new CalculationResultConverter());

        return dsd;
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
        CalculationDataDefinition cdf = new CalculationDataDefinition("WHMI", new WeightHeightMuacInrCalcultion());
        cdf.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cdf;
    }
    private DataDefinition arvDrugsPreArtNumber(){
        CalculationDataDefinition cd = new CalculationDataDefinition("ARVs drugs/Pre-ART No", new ArvDrugsPreArtNumberCalcultion());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    private DataDefinition whoCd4Vl(){
        CalculationDataDefinition cd = new CalculationDataDefinition("WHO/CD4/VL", new WhoCd4VLCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
    private DataDefinition ifoIycfMnc(){
        CalculationDataDefinition cd = new CalculationDataDefinition("IFO/IYCF/MNC", new IfoIycfMncCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataDefinition referredToOrFrom(){
        CalculationDataDefinition cd = new CalculationDataDefinition("RTW/RFW", new RtwRfwCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }
}
