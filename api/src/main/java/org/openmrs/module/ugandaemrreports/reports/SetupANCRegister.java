package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
        props.put("repeatingSections", "sheet:1,row:10,dataset:Intergrated Antenatal Register");
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


        CohortDefinition definition = df.getDeadPatientsDuringPeriod();

        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.addRowFilter(Mapped.mapStraightThrough(definition));

        //start adding columns here
        addColumn(dsd, "Serial No", null);
        addColumn(dsd, "Client No", null);
        dsd.addColumn("Name of Client", new PreferredNameDataDefinition(), (String) null);
        addColumn(dsd, "Village+parish", null);
        addColumn(dsd, "Phone Number", null);
        addColumn(dsd, "Age-10-19yrs", null);
        addColumn(dsd, "Age-20-24yrs", null);
        addColumn(dsd, "Age-25andAboveyrs", null);
        addColumn(dsd, "ANC Visit", null);
        addColumn(dsd, "Gravida", null);
        addColumn(dsd, "Parity", null);
        addColumn(dsd, "Gestational Age", null);
        addColumn(dsd, "ANC1 Timing", null);
        addColumn(dsd, "EDD", null);
        addColumn(dsd, "WHM", null);
        addColumn(dsd, "BP", null);
        addColumn(dsd, "EMTCT codesW", null);
        addColumn(dsd, "EMTCT codesP", null);
        addColumn(dsd, "Diagnosis", null);
        addColumn(dsd, "WHO/CD4/VL", null);
        addColumn(dsd, "ARVs drugs/Pre-ART No of Client", null);
        addColumn(dsd, "IYCF/MNC", null);
        addColumn(dsd, "TB Status", null);
        addColumn(dsd, "Haemoglobin", null);
        addColumn(dsd, "Syphilis test results-W", null);
        addColumn(dsd, "Syphilis test results-P", null);
        addColumn(dsd, "FPC", null);
        addColumn(dsd, "TT", null);
        addColumn(dsd, "IPT/CTX", null);
        addColumn(dsd, "Free LLIN", null);
        addColumn(dsd, "Mebendazole", null);
        addColumn(dsd, "Iron", null);
        addColumn(dsd, "Folic Acid", null);
        addColumn(dsd, "Other treatments", null);
        addColumn(dsd, "Referal In/Out", null);
        addColumn(dsd, "Risk Factor/Complications", null);

        return rd;
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }
}
