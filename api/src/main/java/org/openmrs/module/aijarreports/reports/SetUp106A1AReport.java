package org.openmrs.module.aijarreports.reports;

import org.openmrs.module.aijarreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.library.CommonDimensionLibrary;
import org.openmrs.module.aijarreports.library.DataFactory;
import org.openmrs.module.aijarreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
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

public class SetUp106A1AReport extends AijarDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

@Autowired
    private CommonDimensionLibrary commonDimensionLibrary;
    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119c8ef34";
    }

    @Override
    public String getUuid() {
        return "167cf668-0715-488b-b159-d5f391774091";
    }

    @Override
    public String getName() {
        return "HMIS 106A1A";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1A";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1AReport.xls");
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

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.get106aAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition getEnrolledBeforeQuarter = hivCohortDefinitionLibrary.getEnrolledInCareByEndOfPreviousDate();
        CohortDefinition getEnrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();

        addAgeGender(dsd, "1", "Patients ever enrolled in care by the end of the previous quarter", getEnrolledBeforeQuarter);
        addAgeGender(dsd, "2", "New patients enrolled during quarter", getEnrolledInTheQuarter);

        return rd;
    }

    public void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "a", label + " (Below 2 Males)", cohortDefinition, "age=below2male");
        addIndicator(dsd, key + "b", label + " (Below 2 Females)", cohortDefinition, "age=below2female");
        addIndicator(dsd, key + "c", label + " (Between 2 and 5 Males)", cohortDefinition, "age=between2and5male");
        addIndicator(dsd, key + "d", label + " (Between 2 and 5 Females)", cohortDefinition, "age=between2and5female");
        addIndicator(dsd, key + "e", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "f", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "g", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "h", label + " (Above 15 Females)", cohortDefinition, "age=above15female");
        addIndicator(dsd, key + "i", label + " Total", cohortDefinition,"");
    }

    public void addAge(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "j", label + " (Between 0 and 15 years)", cohortDefinition, "age=child");
        addIndicator(dsd, key + "k", label + " (Above 15 years)", cohortDefinition, "age=adult");
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
