package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  PMTCT STAT Report
 */
@Component
public class SetupPMTCTART2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private SetupPMTCTSTATReport setupPMTCTSTATReport;

    @Autowired
    private Moh105CohortLibrary moh105CohortLibrary;

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private Moh105IndicatorLibrary indicatorLibrary;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "c4fb676e-dcc6-40b9-9ad5-babf6cf912fd";
    }

    @Override
    public String getUuid() {
        return "47246504-69df-4060-b202-e2903305c456";
    }

    @Override
    public String getName() {
        return "MER PMTCT ART 2019 Report";
    }

    @Override
    public String getDescription() {
        return "MER Indicator Report for PMTCT ART 2019 version";
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
        return Arrays.asList(buildReportDesign(reportDefinition));
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_PMTCT_ART_2019.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        String params = "startDate=${startDate},endDate=${endDate}";
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("PMTCT_ART", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getPMTCT_STAT_AgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


        setupPMTCTSTATReport.addGender(dsd,"a","Total  Pregnant With known HIV- status at entry (TRK)",ReportUtils.map(indicatorLibrary.hivPositiveInitiatedART(),params),"female");
        setupPMTCTSTATReport.addGender(dsd,"b","Total  Pregnant Known HIV positives at entry(TRRK)",ReportUtils.map( indicatorLibrary.alreadyOnARTK(),params),"female");
         return rd;
    }





    @Override
    public String getVersion() {
        return "0.1.1";
    }
}
