package org.openmrs.module.aijarreports.reports;

import org.openmrs.module.aijarreports.library.CommonCohortDefinitionLibrary;
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
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;

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
        ReportDesign design = createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "FacilityPreARTRegister.xls");
        return Arrays.asList(design);
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

        CohortDefinitionDimension ageDimension = new CohortDefinitionDimension();

        CohortDefinition below2Years = cohortDefinitionLibrary.below2Years();
        CohortDefinition between2And4Years = cohortDefinitionLibrary.between2And5Years();
        CohortDefinition between5And14Years = cohortDefinitionLibrary.between5And14Years();
        CohortDefinition above15Years = cohortDefinitionLibrary.above15Years();

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();


        CohortDefinition a = df.getPatientsInAll(below2Years, males);
        CohortDefinition b = df.getPatientsInAll(below2Years, females);
        CohortDefinition c = df.getPatientsInAll(between2And4Years, males);
        CohortDefinition d = df.getPatientsInAll(between2And4Years, females);

        CohortDefinition e = df.getPatientsInAll(between5And14Years, males);
        CohortDefinition f = df.getPatientsInAll(between5And14Years, females);
        CohortDefinition g = df.getPatientsInAll(above15Years, males);
        CohortDefinition h = df.getPatientsInAll(above15Years, females);

        ageDimension.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ageDimension.addCohortDefinition("below2male", Mapped.mapStraightThrough(a));
        ageDimension.addCohortDefinition("below2female", Mapped.mapStraightThrough(b));
        ageDimension.addCohortDefinition("between2and5male", Mapped.mapStraightThrough(c));
        ageDimension.addCohortDefinition("between2and5female", Mapped.mapStraightThrough(d));
        ageDimension.addCohortDefinition("between5and14male", Mapped.mapStraightThrough(e));
        ageDimension.addCohortDefinition("between5and14female", Mapped.mapStraightThrough(f));
        ageDimension.addCohortDefinition("above15male", Mapped.mapStraightThrough(g));
        ageDimension.addCohortDefinition("above15female", Mapped.mapStraightThrough(h));
        ageDimension.addCohortDefinition("child", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedBetween(0, 14)));
        ageDimension.addCohortDefinition("adult", Mapped.mapStraightThrough(cohortDefinitionLibrary.agedAtLeast(15)));
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition allOnCare = hivCohortDefinitionLibrary.getEverEnrolledInCare();
        CohortDefinition getEnrolledBeforeQuarter = hivCohortDefinitionLibrary.getEnrolledInCareByEndOfPreviousDate();
        CohortDefinition getEnrolledInTheQuarter = hivCohortDefinitionLibrary.getEnrolledInCareBetweenDates();

        addAgeGender(dsd, "All HIV Patients", "Patients who are on care", allOnCare);
        addAgeGender(dsd, "Ever enrolled by previous quarter", "Patients enrolled by previous quarter", getEnrolledBeforeQuarter);
        addAgeGender(dsd, "Ever enrolled in the quarter", "Patients enrolled during quarter", getEnrolledInTheQuarter);

        return rd;
    }

    public void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "_a", label + " (Below 2 Males)", cohortDefinition, "age=below2male");
        addIndicator(dsd, key + "_b", label + " (Below 2 Females)", cohortDefinition, "age=below2female");
        addIndicator(dsd, key + "_c", label + " (Between 2 and 5 Males)", cohortDefinition, "age=between2and5male");
        addIndicator(dsd, key + "_d", label + " (Between 2 and 5 Females)", cohortDefinition, "age=between2and5female");
        addIndicator(dsd, key + "_e", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "_f", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "_g", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "_h", label + " (Above 15 Females)", cohortDefinition, "age=above15female");
    }

    public void addAge(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "_j", label + " (Between 0 and 15 years)", cohortDefinition, "age=child");
        addIndicator(dsd, key + "_k", label + " (Above 15 years)", cohortDefinition, "age=adult");
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
