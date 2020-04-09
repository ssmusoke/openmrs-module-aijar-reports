package org.openmrs.module.ugandaemrreports.reports2019;

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
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  TX Current Report
 */
@Component
public class SetupTxRTT2019Report extends UgandaEMRDataExportManager {

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
    private HIVMetadata hivMetadata;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "346034a1-1668-4a1a-8dc0-f42f944f05f3";
    }

    @Override
    public String getUuid() {
        return "c17ea006-b599-45c4-94c9-33f6f4d99f4c";
    }

    @Override
    public String getName() {
        return "MER Indicator Report For Tx RTT ";
    }

    @Override
    public String getDescription() {
        return "Tx RTT Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_RTT.xls");
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
        rd.addDataSetDefinition("TX_RTT", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition males = cohortDefinitionLibrary.males();
        CohortDefinition females = cohortDefinitionLibrary.females();

        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition missedAppointment28DaysAndAbove = df.getPatientsWhoHaveNotComeAfterTheirLastMissedAppointmentByMinimumDays(28);
        CohortDefinition RTT = df.getPatientsInAll(onArtDuringQuarter,missedAppointment28DaysAndAbove);

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);


        addGender(dsd, "a", "RTT ", RTT);
        addGender(dsd, "b", "RTT ", RTT);

        addIndicator(dsd,"PWIDSf","PWIDs TX Curr on ART female",df.getPatientsInAll(females,PWIDS,RTT),"");
        addIndicator(dsd,"PWIDSm","PWIDs TX Curr on ART male",df.getPatientsInAll(males,PWIDS,RTT),"");

        addIndicator(dsd,"PIPf","PIPs TX Curr on ART female",df.getPatientsInAll(females,PIPS,RTT),"");
        addIndicator(dsd,"PIPm","PIPs TX Curr on ART male",df.getPatientsInAll(males,PIPS,RTT),"");

        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        if (key == "a") {
            addIndicator(dsd, "2" + key, label, cohortDefinition, "age=below1female");
            addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between1and4female");
            addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between5and9female");
            addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between10and14female");
            addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between15and19female");
            addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between20and24female");
            addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between25and29female");
            addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between30and34female");
            addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between35and39female");
            addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between40and44female");
            addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between45and49female");
            addIndicator(dsd, "13" + key, label, cohortDefinition, "age=above50female");
        } else if (key == "b") {
            addIndicator(dsd, "2" + key, label, cohortDefinition, "age=below1male");
            addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between1and4male");
            addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between5and9male");
            addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between10and14male");
            addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between15and19male");
            addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between20and24male");
            addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between25and29male");
            addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between30and34male");
            addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between35and39male");
            addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between40and44male");
            addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between45and49male");
            addIndicator(dsd, "13" + key, label, cohortDefinition, "age=above50male");
        }
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
        return "0.0.1.1";
    }
}