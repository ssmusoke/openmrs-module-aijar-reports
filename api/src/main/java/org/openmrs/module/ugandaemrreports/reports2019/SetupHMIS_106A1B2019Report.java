package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1B2019DataSetDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.HMIS106A1BDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 */
@Component

public class SetupHMIS_106A1B2019Report extends UgandaEMRDataExportManager {
    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Override
    public String getExcelDesignUuid() {
        return "4cdac4c7-4391-48f2-baea-a64f8b1f0090";
    }

    @Override
    public String getUuid() {
        return "edc03eac-37bd-450c-bc10-9c4a7d57a82b";
    }

    @Override
    public String getName() {
        return "HMIS 106A1B 2019 Report";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1B 2019 Report";
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
        ReportDesign rd = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "HMIS_106A1B_2019.xls");
        return rd;

    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        HMIS106A1B2019DataSetDefinition dsd = new HMIS106A1B2019DataSetDefinition();
        dsd.setName(getName());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("HMIS106A1B", Mapped.mapStraightThrough(dsd));

        return rd;
    }

    @Override
    public String getVersion() {
        return "0.0.2.8";
    }
}
