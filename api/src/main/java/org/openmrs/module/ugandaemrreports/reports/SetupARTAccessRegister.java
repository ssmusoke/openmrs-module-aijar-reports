package org.openmrs.module.ugandaemrreports.reports;


import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ARTAccessDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * ART Access Register
 */
@Component
public class SetupARTAccessRegister extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "b3f6f05c-03cd-4c65-bca3-bd91e3cbb43b";
    }

    @Override
    public String getUuid() {
        return "9ec815ed-e3ac-4710-a480-360e2777c7d1";
    }

    @Override
    public String getName() {
        return "ART Access Register";
    }

    @Override
    public String getDescription() {
        return "Encounters imported from ART Access";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(df.getStartDateParameter());
        l.add(df.getEndDateParameter());
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList<ReportDesign>();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding
     * properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override

    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "ARTAccessRegister.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:DISP_LIST");
        props.put("sortWeight", "5000");
        rd.setProperties(props);
        return rd;
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());


        ARTAccessDatasetDefinition dsd = new ARTAccessDatasetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());


        rd.addDataSetDefinition("DISP_LIST", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1.4";
    }
}
