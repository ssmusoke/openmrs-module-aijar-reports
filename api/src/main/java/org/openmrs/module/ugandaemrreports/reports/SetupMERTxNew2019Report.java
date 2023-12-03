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
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.settings;
import static org.openmrs.module.ugandaemrreports.library.CommonDatasetLibrary.getUgandaEMRVersion;

/**
 *  TX Current Report
 */
@Component
public class SetupMERTxNew2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private ARTCohortLibrary artCohortLibrary;

    @Autowired
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "df5cfe63-f5c0-4b95-bdc9-f5767dc1ba17";
    }

    public String getJSONDesignUuid() {
        return "bb0d79b3-1c85-4d27-8902-c4f901f85968";
    }

    @Override
    public String getUuid() {
        return "65fec0844-1970-43c5-bf77-b296415daa34";
    }

    @Override
    public String getName() {
        return "MER Indicator Report For Tx New 2019 ";
    }

    @Override
    public String getDescription() {
        return "Tx New Report 2019";
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

    /**
     * Build the report design for the specified report, this allows a user to override the report design by adding
     * properties and other metadata to the report design
     *
     * @param reportDefinition
     * @return The report design
     */
    @Override
    public ReportDesign buildReportDesign(ReportDefinition reportDefinition) {
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_NEW_2019.xls");
    }

    public ReportDesign buildJSONReportDesign(ReportDefinition reportDefinition) {
        return createJSONTemplateDesign(getJSONDesignUuid(), reportDefinition, "MER_TX_NEW_2019.json");
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
        rd.addDataSetDefinition("TX", Mapped.mapStraightThrough(dsd));
        rd.addDataSetDefinition("S", Mapped.mapStraightThrough(settings()));
        rd.addDataSetDefinition("aijar", Mapped.mapStraightThrough(getUgandaEMRVersion()));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getFinerAgeWith55And65Ranges();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition havingArtStartDateDuringQuarter = df.getPatientsNotIn(hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(),hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod());
        CohortDefinition lactating = addParameters(artCohortLibrary.lactatingAtARTStart());


        CohortDefinition lactatingAtStartOfArt = df.getPatientsInAll(lactating, havingArtStartDateDuringQuarter);

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        addIndicator(dsd,"15a","All newly started on ART above 60 female",df.getPatientsInAll(havingArtStartDateDuringQuarter,females,cohortDefinitionLibrary.agedAtLeast(60)),"");
        addIndicator(dsd,"15b","All newly started on ART above 60 male",df.getPatientsInAll(havingArtStartDateDuringQuarter,males,cohortDefinitionLibrary.agedAtLeast(60)),"");


        Helper.addGender(dsd, "a", "All Newly started  on ART ", havingArtStartDateDuringQuarter);
        Helper.addGender(dsd, "b", "All Newly started  on ART ", havingArtStartDateDuringQuarter);


        addIndicator(dsd, "LAC", "Lactating At start on Art", lactatingAtStartOfArt, "");
        addIndicator(dsd, "TOTAL", "Total At start on Art", havingArtStartDateDuringQuarter, "");

        addIndicator(dsd,"PWIDSf","PWIDs TX Curr on ART female",df.getPatientsInAll(females,PWIDS,havingArtStartDateDuringQuarter),"");
        addIndicator(dsd,"PWIDSm","PWIDs TX Curr on ART male",df.getPatientsInAll(males,PWIDS,havingArtStartDateDuringQuarter),"");

       addIndicator(dsd,"PIPf","PIPs TX Curr on ART female",df.getPatientsInAll(females,PIPS,havingArtStartDateDuringQuarter),"");
       addIndicator(dsd,"PIPm","PIPs TX Curr on ART male",df.getPatientsInAll(males,PIPS,havingArtStartDateDuringQuarter),"");

        addIndicator(dsd,"PWIDS","PWIDs TX Curr on ART",df.getPatientsInAll(PWIDS,havingArtStartDateDuringQuarter),"");
        addIndicator(dsd,"PIPS","PIPs TX Curr on ART",df.getPatientsInAll(PIPS,havingArtStartDateDuringQuarter),"");
       addIndicator(dsd,"TOTAL_KP","TOTAL KP",df.getPatientsInAll(df.getPatientsInAny(PIPS,PWIDS),havingArtStartDateDuringQuarter),"");


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
    public String getVersion() {
        return "0.2.8";
    }
}