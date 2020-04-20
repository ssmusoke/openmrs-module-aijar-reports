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
package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.*;
import org.openmrs.module.ugandaemrreports.library.BasePatientDataLibrary;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
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
public class SetupPNCRegister2019 extends UgandaEMRDataExportManager {

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
        return "c7e95e4d-6f83-44f6-ab0c-f55391fa5d2a";
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        l.add(buildExcelReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */

    @Override

    public ReportDesign buildReportDesign (ReportDefinition reportDefinition) {
        ReportDesign rd = createCSVDesign(getExcelDesignUuid(), reportDefinition);
        return rd;
    }
    public ReportDesign buildExcelReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign("816ebd6e-a2c9-4eda-a1a0-0515a713939c", reportDefinition, "PNCRegister2019.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8-10,dataset:PNC");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "ffed8404-7f81-4c9e-a9eb-db2673397cc0";
    }

    @Override
    public String getName() {
        return "Integrated Postnatal Register 2019";
    }

    @Override
    public String getDescription() {
        return "PostNatal Register 2019";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        rd.addDataSetDefinition("PNC", Mapped.mapStraightThrough(dataSetDefinition()));
        return rd;
    }

    @Override
    public String getVersion() {
        return "1.0.5";
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
        dsd.setName("PNC");
        dsd.addParameters(getParameters());
        dsd.addRowFilter(Cohorts.genderAndHasAncEncounter(true, false, "fa6f3ff5-b784-43fb-ab35-a08ab7dbf074"), "startDate=${startDate},endDate=${endDate}");

        //start adding columns
        dsd.addColumn("Serial No", sdd.definition("Serial No",  sdd.getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Client No", sdd.definition("Client No", sdd.getConcept("ef1f4c7a-2b90-4412-83bb-87ae8094ce4c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("NIN", sdd.getNationalIDNumber(), "");
        dsd.addColumn("Mother Name",  new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Village", basePatientDataLibrary.getVillage(),(String)null);
        dsd.addColumn("Parish", basePatientDataLibrary.getParish(),(String)null);
        dsd.addColumn("County", basePatientDataLibrary.getCounty(),(String)null);
        dsd.addColumn("District", basePatientDataLibrary.getDistrict(),(String)null);
        dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", sdd.getPhoneNumber()), "", new PersonAttributeDataConverter());
        dsd.addColumn("Alternate Phone", new PersonAttributeDataDefinition("Alternate Phone", sdd.getAlternatePhoneNumber()), "", new PersonAttributeDataConverter());
        dsd.addColumn("Nationality", new PersonAttributeDataDefinition("Nationality", sdd.getPatientNationality()), "", new NationalityPersonalAttributeDataConverter());
        dsd.addColumn("ClientAge", sdd.getAgeDataDefinition(10,200), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("6 Hours", sdd.definition("6 Hours", sdd.getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("6 Days", sdd.definition("6 Days", sdd.getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("6 Weeks", sdd.definition("6 Weeks", sdd.getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("6 Months", sdd.definition("6 Months", sdd.getConcept("1732AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TimingForPNCDataConverter());
        dsd.addColumn("FP", sdd.definition("FP", sdd.getConcept("dc7620b3-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FpPNCDataConverter());
        dsd.addColumn("Breast Status", sdd.definition("Breast Status", sdd.getConcept("07c10f5c-17fd-4a7e-8d72-c2252f589da0")), "onOrAfter=${startDate},onOrBefore=${endDate}", new BreastStatusDataConverter());
        dsd.addColumn("Cervix Status", sdd.definition("Cervix Status", sdd.getConcept("d858f8cb-fe9e-4131-8d91-cd9929cc53de")), "onOrAfter=${startDate},onOrBefore=${endDate}", new CervixStatusDataConverter());
        dsd.addColumn("Weight", sdd.definition("Weight",  sdd.getConcept("5089AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Pre-ART NoW", sdd.definition("Pre-ART NoW",  sdd.getConcept("9fc5bf9d-a79e-4548-9207-b2afbbeba796")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Pre-ART NoP", sdd.definition("Pre-ART NoP",  sdd.getConcept("723c45bf-57eb-4865-b5c9-4a0ad6a36522")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("MUAC", sdd.definition("MUAC",  sdd.getConcept("5f86d19d-9546-4466-89c0-6f80c101191b")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MUACDataConverter());
        dsd.addColumn("INR NO", sdd.definition("INR NO",  sdd.getConcept("b644c29c-9bb0-447e-9f73-2ae89496a709")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("EMTCT codesW", sdd.definition("EMTCT codesW", sdd.getConcept("d5b0394c-424f-41db-bc2f-37180dcdbe74")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
        dsd.addColumn("EMTCT codesP", sdd.definition("EMTCT codesP", sdd.getConcept("62a37075-fc2a-4729-8950-b9fae9")), "onOrAfter=${startDate},onOrBefore=${endDate}", new EmctCodesDataConverter());
        dsd.addColumn("ART CodeW", sdd.definition("ART CodeW", sdd.getConcept("a615f932-26ee-449c-8e20-e50a15232763")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ARVsDataConverter());
        dsd.addColumn("ART CodeP", sdd.definition("ART CodeP", sdd.getConcept("11dafd93-23c1-4b89-86e0-593e5f7ca386")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ARVsDataConverter());
        dsd.addColumn("Iron", sdd.definition("Iron", sdd.getConcept("315825e8-8ba4-4551-bdd1-aa4e02a36639")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("Folic Acid", sdd.definition("Folic Acid", sdd.getConcept("8c346216-c444-4528-a174-5139922218ed")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("Vit A", sdd.definition("Vit A", sdd.getConcept("88ec2c8b-eb7b-4595-8612-1871568507a5")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("CTX", sdd.definition("CTX", sdd.getConcept("d12abd7f-c90d-4798-9240-0f2f81977183")), "onOrAfter=${startDate},onOrBefore=${endDate}", new RoutineAdminDataConverter());
        dsd.addColumn("Diagnosis-M", sdd.definition("Diagnosis-M", sdd.getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MotherDiagnosisDataConverter());
        dsd.addColumn("WHO", sdd.definition("WHO", sdd.getConcept("dcdff274-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new WHODataConverter());
        dsd.addColumn("CD4", sdd.getWHOCD4ViralLoadCalculation("dcbcba2c-30ab-102d-86b0-7a5022ba4115", "159376AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("VL",sdd.getWHOCD4ViralLoadCalculation("dc8d83e3-30ab-102d-86b0-7a5022ba4115", "0b434cfa-b11c-4d14-aaa2-9aed6ca2da88"), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Other treatment mother", sdd.definition("Other treatment mother", sdd.getConcept("2aa72406-436e-490d-8aa4-d5336148204f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Baby status", sdd.definition("Baby status", sdd.getConcept("dd8a2ad9-16f6-44db-82d7-87d6eef14886")), "onOrAfter=${startDate},onOrBefore=${endDate}", new BabyStatusDataConveter());
        dsd.addColumn("Age", sdd.definition("Age", sdd.getConcept("164438AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("WT", sdd.definition("WT", sdd.getConcept("94e4aeea-84d0-4207-aacb-ce38fe8e109c")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Diagnosis-C", sdd.definition("Diagnosis-C", sdd.getConcept("a16b3a8e-6412-4344-908a-2a96344fa017")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("IFO", sdd.definition("IFO", sdd.getConcept("dc9a00a2-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IFODataConverter());
        dsd.addColumn("IYCF", sdd.definition("IYCF", sdd.getConcept("5d993591-9334-43d9-a208-11b10adfad85")), "onOrAfter=${startDate},onOrBefore=${endDate}", new IYCFDataConverter());
        dsd.addColumn("MNC", sdd.definition("MNC", sdd.getConcept("af7dccfd-4692-4e16-bd74-5ac4045bb6bf")), "onOrAfter=${startDate},onOrBefore=${endDate}", new MNCDataConverter());
        dsd.addColumn("BCG", sdd.definition("BCG", sdd.getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("OPV", sdd.definition("OPV", sdd.getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("DPT", sdd.definition("DPT", sdd.getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("Vit A", sdd.definition("Vit A", sdd.getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("PCV", sdd.definition("PCV", sdd.getConcept("dc918618-30ab-102d-86b0-7a5022ba4115")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ImmunizationDataConverter());
        dsd.addColumn("Other treatment child", sdd.definition("Other treatment child", sdd.getConcept("59560ede-43e2-4e56-a47e-0f876779f0e1")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("RTW/RFW", sdd.referredToOrFrom(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("FSG Enrollment", sdd.definition("FSG Enrollment", sdd.getConcept("7e8e0ef3-2fda-4c76-8cf6-c7a6f1584ff2")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FSGEnrollmentDataConverter());
        dsd.addColumn("Enrolled at MBCP", sdd.definition("Enrolled at MBCP", sdd.getConcept("f0d12a70-04a3-4f7f-992b-bed4ad331908")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());

        return dsd;
    }


}