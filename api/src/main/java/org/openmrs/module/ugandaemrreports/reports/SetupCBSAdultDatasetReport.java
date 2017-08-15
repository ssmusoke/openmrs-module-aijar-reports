package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CBSAdultDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.CBSAdultFollowupDatasetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.ViralLoadDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


@Component
public class SetupCBSAdultDatasetReport extends UgandaEMRDataExportManager {
    @Autowired
    private DataFactory df;

    public String getExcelDesignUuid() {
        return "98e9202d-8c00-415f-9994-43917181f023";
    }

    public String getUuid() {
        return "9c85e908-c3cd-4dc1-b145-13f1d02f1c54";
    }

    public String getName() {
        return "CBS Adult Dataset Report";
    }

    public String getDescription() {
        return "CBS Adult Dataset Report";
    }

    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList();
        l.add(this.df.getStartDateParameter());
        l.add(this.df.getEndDateParameter());
        return l;
    }

    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        List<ReportDesign> l = new ArrayList();
        l.add(buildReportDesign(reportDefinition));
        return l;
    }


    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CBSAdultQuarterlyReport.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:5,dataset:adult | sheet:2,row:3,dataset:follow");
        rd.setProperties(props);
        return rd;
    }


    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CBSAdultDatasetDefinition dsd = new CBSAdultDatasetDefinition();
        CBSAdultFollowupDatasetDefinition cbsAdultFollowupDatasetDefinition = new CBSAdultFollowupDatasetDefinition();

        dsd.setName("adult");
        dsd.setParameters(getParameters());


        cbsAdultFollowupDatasetDefinition.setName("follow");
        cbsAdultFollowupDatasetDefinition.setParameters(getParameters());

        rd.addDataSetDefinition("adult", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("follow", Mapped.mapStraightThrough(cbsAdultFollowupDatasetDefinition));
        return rd;
    }

    public String getVersion() {
        return "2.0.7";
    }
}