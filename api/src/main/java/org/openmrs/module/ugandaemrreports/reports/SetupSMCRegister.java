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
package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.Concept;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.CalculationResultDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.DuringSurgeryDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.DuringSurgeryDateDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.FaciltyAndOutReachDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.GradeDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.HctDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.SmcProcedureDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.TypeOfAeDataConverter;
import org.openmrs.module.ugandaemrreports.definition.data.definition.CalculationDataDefinition;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.AgeFromEncounterDateCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.AnaesthesiaCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.CircumciserNameCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.FollowUpCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.SMCAdrressCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.SMCEncounterDateCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.smc.STICalculation;
import org.openmrs.module.ugandaemrreports.reporting.dataset.definition.SharedDataDefintion;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Created by Nicholas Ingosi on 5/17/17.
 */
@Component
public class SetupSMCRegister extends UgandaEMRDataExportManager {
    @Autowired
    private DataFactory df;

    @Autowired
    SharedDataDefintion sdd;
    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "7def95f0-3b38-11e7-b8de-507b9dc4c741";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "SMCRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:10,dataset:SMC");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public String getUuid() {
        return "c1122646-3b37-11e7-aa22-507b9dc4c741";
    }

    @Override
    public String getName() {
        return "Safe Male Circumcision Register";
    }

    @Override
    public String getDescription() {
        return "Safe Male Circumcision Register";
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.addParameters(getParameters());
        rd.addDataSetDefinition("SMC", Mapped.mapStraightThrough(dataSetDefinition()));
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

    private CohortDefinition getSmcUsedEncounters(){
        CompositionCohortDefinition cd = new CompositionCohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));
        cd.addSearch("client", ReportUtils.map(Cohorts.genderAndHasAncEncounter(false, true, "244da86d-f80e-48fe-aba9-067f241905ee"), "startDate=${startDate},endDate=${endDate}"));
        cd.addSearch("followup", ReportUtils.map(Cohorts.genderAndHasAncEncounter(false, true, "d0f9e0b7-f336-43bd-bf50-0a7243857fa6"), "startDate=${startDate},endDate=${endDate}"));
        cd.setCompositionString("client OR followup");
        return cd;
    }

    private DataSetDefinition dataSetDefinition() {
        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName("SMC");
        dsd.addParameters(getParameters());
        dsd.addRowFilter(getSmcUsedEncounters(), "startDate=${startDate},endDate=${endDate}");

        PatientIdentifierType serialNo= MetadataUtils.existing(PatientIdentifierType.class, "37601abe-2ee0-4493-8ac7-22b4972190cf");
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(serialNo.getName(), serialNo), identifierFormatter);

        dsd.addColumn("Date", getEncounterDate(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Serial No", identifierDef, "");
        dsd.addColumn("Names of Client", new PreferredNameDataDefinition(), (String) null);
        dsd.addColumn("Age<2yrs", getAgeFromEncounterDate(0, 2), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Age2<5yrs", getAgeFromEncounterDate(2, 5), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Age5<15yrs", getAgeFromEncounterDate(5, 15), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Age15<49yrs", getAgeFromEncounterDate(15, 49), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Age<49yrs", getAgeFromEncounterDate(49, 200), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Address", address(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Facility/Outreach", sdd.definition("Facility/Outreach", getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802")), "onOrAfter=${startDate},onOrBefore=${endDate}", new FaciltyAndOutReachDataConverter());
        dsd.addColumn("STI", sti(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("HTC", sdd.definition("HTCM", getConcept("29c47b5c-b27d-499c-b52c-7be676a0a78f")), "onOrAfter=${startDate},onOrBefore=${endDate}", new HctDataConverter());
        dsd.addColumn("Date of TT1", sdd.definition("Date of TT1", getConcept("ed8eebcb-0d31-4c85-ab37-402ba2b9f78d")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Date of TT2", sdd.definition("Date of TT1", getConcept("5d50f724-6766-421b-bcb1-1133054b7621")), "onOrAfter=${startDate},onOrBefore=${endDate}", new ObsDataConverter());
        dsd.addColumn("Procedure", sdd.definition("Procedure", getConcept("bd66b11f-04d9-46ed-a367-2c27c15d5c71")), "onOrAfter=${startDate},onOrBefore=${endDate}", new SmcProcedureDataConverter());
        dsd.addColumn("Type anasthesia", anaesthesia(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("Circumciser name", circumcisoName(), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("48hrs", followUps(2), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("7days", followUps(7), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn(">7days", followUps(8), "onDate=${endDate}", new CalculationResultDataConverter());
        dsd.addColumn("During surgery", sdd.definition("During surgery", getConcept("654e7039-4629-46bb-9fc9-0f6dd101ce6a")), "onOrAfter=${startDate},onOrBefore=${endDate}", new DuringSurgeryDataConverter());
        dsd.addColumn("Date of AE", sdd.definition("Date of AE", getConcept("654e7039-4629-46bb-9fc9-0f6dd101ce6a")), "onOrAfter=${startDate},onOrBefore=${endDate}", new DuringSurgeryDateDataConverter());
        dsd.addColumn("Type of AE", sdd.definition("Type of AE",  getConcept("654e7039-4629-46bb-9fc9-0f6dd101ce6a")), "onOrAfter=${startDate},onOrBefore=${endDate}", new TypeOfAeDataConverter());
        dsd.addColumn("Grade of AE", sdd.definition("Grade of AE", getConcept("e34976b9-1aff-489d-b959-4da1f7272499")), "onOrAfter=${startDate},onOrBefore=${endDate}", new GradeDataConverter());
        //dsd.addColumn("Action", sdd.definition("Action", getConcept("")), "onOrAfter=${startDate},onOrBefore=${endDate}", null);

        return dsd;
    }

    private DataDefinition getEncounterDate() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Date", new SMCEncounterDateCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataDefinition getAgeFromEncounterDate(Integer lower, Integer upper) {
        CalculationDataDefinition cd = new CalculationDataDefinition("Date", new AgeFromEncounterDateCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        cd.addCalculationParameter("lower", lower);
        cd.addCalculationParameter("upper", upper);
        return cd;
    }
    private DataDefinition address() {
        CalculationDataDefinition cd = new CalculationDataDefinition("address", new SMCAdrressCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;

    }

    private Concept getConcept(String uuid) {
        return Dictionary.getConcept(uuid);
    }

    private DataDefinition sti(){
        CalculationDataDefinition cd = new CalculationDataDefinition("sti", new STICalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataDefinition anaesthesia() {
        CalculationDataDefinition cd = new CalculationDataDefinition("anaesthesia", new AnaesthesiaCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }

    private DataDefinition followUps(Integer visit) {
        CalculationDataDefinition cd = new CalculationDataDefinition("visits", new FollowUpCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        cd.addCalculationParameter("visit", visit);
        return cd;
    }

    private DataDefinition circumcisoName() {
        CalculationDataDefinition cd = new CalculationDataDefinition("Circumciser name", new CircumciserNameCalculation());
        cd.addParameter(new Parameter("onDate", "On Date", Date.class));
        return cd;
    }


}
