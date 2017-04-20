package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 07/06/2016.
 */
@Component

public class SetupAdherenceReport extends UgandaEMRDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public String getExcelDesignUuid() {
        return "c98ab976-9c9d-4a28-9760-ac3119c8ef29";
    }

    @Override
    public String getUuid() {
        return "167cf66c-0715-488b-b159-d5f391774095";
    }

    @Override
    public String getName() {
        return "Adherence report";
    }

    @Override
    public String getDescription() {
        return "Adherence report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "AdherenceReport.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension adherenceDimension = commonDimensionLibrary.getAdherenceGroup();
        dsd.addDimension("adherence", Mapped.mapStraightThrough(adherenceDimension));


        CohortDefinition adherence = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getAdherence(), Arrays.asList(hivMetadata.getARTEncounterEncounterType()), BaseObsCohortDefinition.TimeModifier.ANY);

        addAgeGender(dsd, "1", "Adherence", adherence);


        return rd;
    }

    public void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "a", label + " (Good)", cohortDefinition, "adherence=good");
        addIndicator(dsd, key + "b", label + " (Fair)", cohortDefinition, "adherence=fair");
        addIndicator(dsd, key + "c", label + " (Poor)", cohortDefinition, "adherence=poor");
        addIndicator(dsd, key + "d", label + " Total", cohortDefinition, "");
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
