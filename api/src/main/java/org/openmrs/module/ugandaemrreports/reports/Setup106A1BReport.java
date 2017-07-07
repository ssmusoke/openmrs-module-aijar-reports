package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1BDataSetDefinition;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Created by carapai on 07/06/2016.
 */
@Component

public class Setup106A1BReport extends UgandaEMRDataExportManager {

    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119c0ef23";
    }

    @Override
    public String getUuid() {
        return "167cf668-071e-488b-b159-d5f391774038";
    }

    @Override
    public String getName() {
        return "HMIS 106A1B";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1B";
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

        HMIS106A1BDataSetDefinition dsd = new HMIS106A1BDataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("HMIS106A1B", Mapped.mapStraightThrough(dsd));
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.42";
    }
}