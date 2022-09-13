package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  Covid-19 Vaccination Report
 */
@Component
public class SetupCovidVaccinationReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "1180f6f9-cb3d-42f6-b336-4e9a56307f09";
    }

    @Override
    public String getUuid() {
        return "ca24a543-c0dc-45c2-baf7-a73e31060bb3";
    }

    @Override
    public String getName() {
        return "Covid-19 Vaccination Report";
    }

    @Override
    public String getDescription() {
        return "Covid-19 vaccination status of clients";
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
        return l;
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CovidVaccination.xls");
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
        rd.addDataSetDefinition("COVID", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getCovidAgeDimension();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        //People who inject Drugs
        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        // People in Prisons and other enclosed settings
        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition firstDosevaccinated =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("70afc0c1-4972-43c4-8310-18228d5406a9"),hivMetadata.getCovidVaccinationEncounterType(),
                BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition JohnsonVaccinated = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("70afc0c1-4972-43c4-8310-18228d5406a9"),hivMetadata.getCovidVaccinationEncounterType(),
                Arrays.asList(Dictionary.getConcept("8e0dc48e-3b20-413e-a43b-0761180a4a59")),BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition secondDosevaccinated =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("dc6e5ec4-f3c3-4de3-a70b-3d8af81f70d2"),hivMetadata.getCovidVaccinationEncounterType(),
                BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition fullyVaccinated = df.getPatientsInAny(JohnsonVaccinated,secondDosevaccinated);

        // Fully_vaccinated + Boosted
        CohortDefinition boosted =  df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("517f7a82-b498-4090-a3b4-4ea58f2d862a"),
                hivMetadata.getCovidVaccinationEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition fullyVaccinatedBoosted = df.getPatientsInAll(fullyVaccinated,boosted);
        Helper.addCovidIndicator(dsd, "a", "Females who're fully vaccinated", fullyVaccinatedBoosted);
        Helper.addCovidIndicator(dsd, "b", "Males who're fully vaccinated", fullyVaccinatedBoosted);

        // Fully_vaccinated
        Helper.addCovidIndicator(dsd, "c", "Females who're fully vaccinated", fullyVaccinated);
        Helper.addCovidIndicator(dsd, "d", "Males who're fully vaccinated", fullyVaccinated);

        // Partially vaccinated
        CohortDefinition firstDoseNotJJ = df.getPatientsNotIn(firstDosevaccinated,JohnsonVaccinated);
        CohortDefinition partiallyVaccinated = df.getPatientsNotIn(firstDoseNotJJ,secondDosevaccinated);

        Helper.addCovidIndicator(dsd, "e", "Females who're partially vaccinated", partiallyVaccinated);
        Helper.addCovidIndicator(dsd, "f", "Males who're partially vaccinated", partiallyVaccinated);

        // Client contacted but not vaccinated
        CohortDefinition clientContacted = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("2e6897d2-b2e5-48a6-a6bc-20f2d721876a"),hivMetadata.getCovidVaccinationEncounterType(),
                Arrays.asList(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115")),BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition clientContactedUnvaccinated = df.getPatientsNotIn(clientContacted,firstDosevaccinated);

        Helper.addCovidIndicator(dsd, "g", "Females contacted but not vaccinated", clientContactedUnvaccinated);
        Helper.addCovidIndicator(dsd, "h", "Males contacted but not vaccinated", clientContactedUnvaccinated);

        // Referred for vaccination
        CohortDefinition referredForVaccination = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("b4ffe6d7-0793-44bc-864e-0af103b6f455"),hivMetadata.getCovidVaccinationEncounterType(),
                Arrays.asList(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115")),BaseObsCohortDefinition.TimeModifier.ANY);


        Helper.addCovidIndicator(dsd, "k", "Females Referred for vaccination", referredForVaccination);
        Helper.addCovidIndicator(dsd, "m", "Males Referred for vaccination", referredForVaccination);


        addIndicator(dsd, "PWIDa", "Vaccinated PWIDS Females", df.getPatientsInAll(females,PWIDS,firstDosevaccinated), "");
        addIndicator(dsd, "PWIDb", "Vaccinated PWIDS Males", df.getPatientsInAll(males,PWIDS,firstDosevaccinated), "");

        addIndicator(dsd, "PIPESa", "Vaccinated PIPS Females", df.getPatientsInAll(females,PIPS,firstDosevaccinated), "");
        addIndicator(dsd, "PIPESb", "Vaccinated PIPS Males", df.getPatientsInAll(males,PIPS,firstDosevaccinated), "");

        return rd;
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    public CohortDefinition addParameters(CohortDefinition cohortDefinition) {
        return df.convert(cohortDefinition, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    @Override
    public String getVersion() { return "1.0.0"; }
}
