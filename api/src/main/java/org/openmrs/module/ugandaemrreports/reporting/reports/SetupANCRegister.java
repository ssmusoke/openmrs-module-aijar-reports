package org.openmrs.module.ugandaemrreports.reporting.reports;

import org.openmrs.Concept;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.data.converter.ObsDataConverter;
import org.openmrs.module.ugandaemrreports.data.converter.PersonAttributeConverter;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
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
        props.put("repeatingSections", "sheet:1,row:10,dataset:IANC");
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

        PatientDataSetDefinition dsd = new PatientDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());

        //supply the dates
        Date onOrAfter = new Date();
        Date onOrBefore = new Date();
        PersonAttributeType attribute = Context.getPersonService().getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");

        //start adding columns here

        dsd.addColumn("Serial No", new ObsForPersonDataDefinition("Serial No", TimeQualifier.LAST, getConcept("1646AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Client No", new ObsForPersonDataDefinition("Client No", TimeQualifier.LAST, getConcept("38460266-6bcd-47e8-844c-649d34323810"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Name of Client", new PreferredNameDataDefinition(), (String) null);
        //dsd.addColumn("Village", new ObsForPersonDataDefinition("Village", TimeQualifier.LAST, getConcept(""), null, null), "", null);
        //dsd.addColumn("Parish", new ObsForPersonDataDefinition("Parish", TimeQualifier.LAST, getConcept(""), null, null), "", null);
        dsd.addColumn("Phone Number", new PersonAttributeDataDefinition("Phone Number", attribute), "", new PersonAttributeConverter());
        //addColumn(dsd, "Age-10-19yrs", null);
        //addColumn(dsd, "Age-20-24yrs", null);
        //addColumn(dsd, "Age-25andAboveyrs", null);
        dsd.addColumn("ANC Visit", new ObsForPersonDataDefinition("ANC Visit", TimeQualifier.LAST, getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Gravida", new ObsForPersonDataDefinition("Gravida", TimeQualifier.LAST, getConcept("dcc39097-30ab-102d-86b0-7a5022ba4115"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Parity", new ObsForPersonDataDefinition("Parity", TimeQualifier.LAST, getConcept("1053AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Gestational Age", new ObsForPersonDataDefinition("Gestational Age", TimeQualifier.LAST, getConcept("dca0a383-30ab-102d-86b0-7a5022ba4115"), null, null), "", new ObsDataConverter());
        dsd.addColumn("ANC1 Timing", new ObsForPersonDataDefinition("ANC1 Timing", TimeQualifier.LAST, getConcept("3a862ab6-7601-4412-b626-d373c1d4a51e"), null, null), "", new ObsDataConverter());
        dsd.addColumn("EDD", new ObsForPersonDataDefinition("EDD", TimeQualifier.LAST, getConcept("dcc033e5-30ab-102d-86b0-7a5022ba4115"), null, null), "", new ObsDataConverter());
        //dsd.addColumn("WHM", new ObsForPersonDataDefinition("WHM", TimeQualifier.LAST, getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), null, null), "", null);
        //dsd.addColumn("BP", new ObsForPersonDataDefinition("BP", TimeQualifier.LAST, getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), null, null), "", null);
        //dsd.addColumn("EMTCT codesW", new ObsForPersonDataDefinition("EMTCT codesW", TimeQualifier.LAST, getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), null, null), "", null);
        //dsd.addColumn("EMTCT codesP", new ObsForPersonDataDefinition("EMTCT codesP", TimeQualifier.LAST, getConcept("801b8959-4b2a-46c0-a28f-f7d3fc8b98bb"), null, null), "", null);
        dsd.addColumn("Diagnosis", new ObsForPersonDataDefinition("Diagnosis", TimeQualifier.LAST, getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, null), "", new ObsDataConverter());
        //dsd.addColumn(dsd, "WHO/CD4/VL", null);
        //dsd.addColumn(dsd, "ARVs drugs/Pre-ART No of Client", null);
        //dsd.addColumn(dsd, "IYCF/MNC", null);
        //dsd.addColumn("TB Status", new ObsForPersonDataDefinition("Diagnosis", TimeQualifier.LAST, getConcept("1284AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, null), "", null);
        dsd.addColumn("Haemoglobin", new ObsForPersonDataDefinition("Haemoglobin", TimeQualifier.LAST, getConcept("dc548e89-30ab-102d-86b0-7a5022ba4115"), null, null), "", new ObsDataConverter());
        //addColumn(dsd, "Syphilis test results-W", null);
        //addColumn(dsd, "Syphilis test results-P", null);
        dsd.addColumn("FPC", new ObsForPersonDataDefinition("FPC", TimeQualifier.LAST, getConcept("0815c786-5994-49e4-aa07-28b662b0e428"), null, null), "", new ObsDataConverter());
        dsd.addColumn("TT", new ObsForPersonDataDefinition("FPC", TimeQualifier.LAST, getConcept("0815c786-5994-49e4-aa07-28b662b0e428"), null, null), "", new ObsDataConverter());
        //addColumn(dsd, "IPT/CTX", null);
        dsd.addColumn("Free LLIN", new ObsForPersonDataDefinition("Free LLIN", TimeQualifier.LAST, getConcept("3e7bb52c-e6ae-4a0b-bce0-3b36286e8658"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Mebendazole", new ObsForPersonDataDefinition("Mebendazole", TimeQualifier.LAST, getConcept("9d6abbc4-707a-4ec7-a32a-4090b1c3af87"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Iron given", new ObsForPersonDataDefinition("Iron given", TimeQualifier.LAST, getConcept("315825e8-8ba4-4551-bdd1-aa4e02a36639"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Folic acid given", new ObsForPersonDataDefinition("Folic acid given", TimeQualifier.LAST, getConcept("8c346216-c444-4528-a174-5139922218ed"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Other treatments", new ObsForPersonDataDefinition("Other treatments", TimeQualifier.LAST, getConcept("2aa72406-436e-490d-8aa4-d5336148204f"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Referal In/Out", new ObsForPersonDataDefinition("Referal In/Out", TimeQualifier.LAST, getConcept("cd27f0ac-0fd3-4f40-99a3-57742106f5fd"), null, null), "", new ObsDataConverter());
        dsd.addColumn("Risk Factor/Complications", new ObsForPersonDataDefinition("Risk Factor/Complications", TimeQualifier.LAST, getConcept("120186AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"), null, null), "", new ObsDataConverter());

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
        return Context.getConceptService().getConceptByUuid(uuid);
    }
}
