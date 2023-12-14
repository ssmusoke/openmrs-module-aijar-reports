package org.openmrs.module.ugandaemrreports.reports;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AggregateReportDataSetDefinition;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 */
@Component

public class SetupTestDesignReport extends AggregateReportDataExportManager {

    @Override
    public String getExcelDesignUuid() {
        return "be745bcb-5d2e-4611-abad-e9e0e953800e";
    }

    @Override
    public String getUuid() {
        return "640d1942-0943-46d2-8cca-109db963b1e5";
    }

    @Override
    public String getName() {
        return "TEST DESIGN";
    }

    @Override
    public String getDescription() {
        return "TEST DESIGN";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start date (Start of quarter)", Date.class));
        l.add(new Parameter("endDate", "End date (End of quarter)", Date.class));
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1BReport.xls");
        Properties props = new Properties();
        props.put("repeatingSections", "sheet:1,row:8,dataset:HMIS106A1B");
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


        AggregateReportDataSetDefinition dsd = new AggregateReportDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        dsd.setReportDesign(getJsonReportDesign());
        rd.addDataSetDefinition("HMIS106A1B", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public File getJsonReportDesign() {
         return getReportDesignFile(getUuid());
    }

    @Override
    public String getVersion() {
        return "0.1.8.0";
    }
}
