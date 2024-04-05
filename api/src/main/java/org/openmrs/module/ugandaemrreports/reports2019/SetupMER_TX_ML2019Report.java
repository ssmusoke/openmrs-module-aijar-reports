package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AggregateReportDataSetDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reports.AggregateReportDataExportManager;
import org.openmrs.module.ugandaemrreports.reports.Helper;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  TX_ML Report
 */
@Component
public class SetupMER_TX_ML2019Report extends AggregateReportDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "adba307c-742d-46e1-8cf2-980054a7b35f";
    }

    public String getJSONDesignUuid() {
        return "1fb2bcc5-11f0-4dc2-8987-fce4dc979392";
    }
    @Override
    public String getUuid() {
        return "5b922f62-f844-4962-9465-61cf8f52f4b7";
    }

    @Override
    public String getName() {
        return "MER TX ML 2019 Report";
    }

    @Override
    public String getDescription() {
        return "MER Indicator Report for TX ML 2019 version";
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
        List<ReportDesign> l = new ArrayList<>();
        l.add(buildReportDesign(reportDefinition));
        l.add(buildJSONReportDesign(reportDefinition));
        return l;
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "MER_TX_ML_2019.json");
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_ML_2019.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        AggregateReportDataSetDefinition dsd = new AggregateReportDataSetDefinition();
        dsd.setReportDesign(getJsonReportDesign());
        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("TX", Mapped.mapStraightThrough(dsd));

        return rd;
    }
    @Override
    public String getVersion() {
        return "3.2.8.6";
    }
}
