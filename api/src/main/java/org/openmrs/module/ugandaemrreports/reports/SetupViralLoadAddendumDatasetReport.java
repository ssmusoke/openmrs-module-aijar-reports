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
public class SetupViralLoadAddendumDatasetReport extends UgandaEMRDataExportManager {
    @Autowired
    private DataFactory df;

    public String getExcelDesignUuid() {
        return "854def86-36dc-4701-ac7f-b5af6bcda795";
    }

    public String getUuid() {
        return "6633f9c9-dffb-4e6b-bfcc-e2ddb08805ca";
    }

    public String getName() {
        return "Viral Load Addendum Quarterly Report";
    }

    public String getDescription() {
        return "Viral Load Addendum Quarterly Report";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "ViralLoadAddendumQuarterlyReport.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:6-7,dataset:viral");
        rd.setProperties(props);
        return rd;
    }


    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        ViralLoadDatasetDefinition viralLoadDatasetDefinition = new ViralLoadDatasetDefinition();
        viralLoadDatasetDefinition.setParameters(getParameters());

        rd.addDataSetDefinition("viral", Mapped.mapStraightThrough(viralLoadDatasetDefinition));
        return rd;
    }

    public String getVersion() {
        return "0.0.2";
    }
}