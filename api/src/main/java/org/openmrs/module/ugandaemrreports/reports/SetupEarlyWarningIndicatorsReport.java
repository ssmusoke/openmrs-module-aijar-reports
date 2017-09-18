package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.EarlyWarningIndicatorsDatasetDefinition;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 */
@Component

public class SetupEarlyWarningIndicatorsReport extends UgandaEMRDataExportManager {

    @Override
    public String getExcelDesignUuid() {
        return "b98ab771-9c9d-4a28-9760-ac3119c8ef2a";
    }

    @Override
    public String getUuid() {
        return "167cf667-071d-488b-b159-d5f391774090";
    }

    @Override
    public String getName() {
        return "Early warning indicators report";
    }

    @Override
    public String getDescription() {
        return "Early warning indicators report";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<Parameter>();
        l.add(new Parameter("startDate", "Start date", Date.class));
        l.add(new Parameter("endDate", "End date", Date.class));
        return l;
    }

    @Override
    public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
        return Arrays.asList(buildReportDesign(reportDefinition));
    }

    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "EarlyWarningIndicatorsReport.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        EarlyWarningIndicatorsDatasetDefinition dsd = new EarlyWarningIndicatorsDatasetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("early", Mapped.mapStraightThrough(dsd));

        return rd;
    }


    @Override
    public String getVersion() {
        return "0.3";
    }
}
