package org.openmrs.module.ugandaemrreports.reports2019;


import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reports.Helper;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

        CohortDefinition tc_curr = hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28");
        CohortDefinition patientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod = hivCohortDefinitionLibrary.getPatientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod();
        CohortDefinition returnToCareClients= df.getPatientsInAll(tc_curr,patientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod);

        CohortDefinition PWIDS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("160666AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition PIPS = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("927563c5-cb91-4536-b23c-563a72d3f829"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("162277AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);


        addGender(dsd, "a", "returnToCareClients ", returnToCareClients);
        addGender(dsd, "b", "returnToCareClients ", returnToCareClients);

        Helper.addIndicator(dsd, "PWIDSf", "PWIDs TX Curr on ART female", df.getPatientsInAll(females, PWIDS, returnToCareClients), "");
        Helper.addIndicator(dsd, "PWIDSm", "PWIDs TX Curr on ART male", df.getPatientsInAll(males, PWIDS, returnToCareClients), "");

        Helper.addIndicator(dsd, "PIPf", "PIPs TX Curr on ART female", df.getPatientsInAll(females, PIPS, returnToCareClients), "");
        Helper.addIndicator(dsd, "PIPm", "PIPs TX Curr on ART male", df.getPatientsInAll(males, PIPS, returnToCareClients), "");

        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        if (key == "a") {
            Helper.addIndicator(dsd, "2" + key, label, cohortDefinition, "age=below1female");
            Helper.addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between1and4female");
            Helper.addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between5and9female");
            Helper.addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between10and14female");
            Helper.addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between15and19female");
            Helper.addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between20and24female");
            Helper.addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between25and29female");
            Helper.addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between30and34female");
            Helper.addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between35and39female");
            Helper.addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between40and44female");
            Helper.addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between45and49female");
            Helper.addIndicator(dsd, "13" + key, label, cohortDefinition, "age=above50female");
        } else if (key == "b") {
            Helper.addIndicator(dsd, "2" + key, label, cohortDefinition, "age=below1male");
            Helper.addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between1and4male");
            Helper.addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between5and9male");
            Helper.addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between10and14male");
            Helper.addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between15and19male");
            Helper.addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between20and24male");
            Helper.addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between25and29male");
            Helper.addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between30and34male");
            Helper.addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between35and39male");
            Helper.addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between40and44male");
            Helper.addIndicator(dsd, "12" + key, label, cohortDefinition, "age=between45and49male");
            Helper.addIndicator(dsd, "13" + key, label, cohortDefinition, "age=above50male");
        }
    }

    @Override
    public String getVersion() {
        return "0.0.5";
    }
}