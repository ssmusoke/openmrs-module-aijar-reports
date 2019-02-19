package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  TX Current Report
 */
@Component
public class SetupTxCurrentReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private BuiltInPatientDataLibrary builtInPatientData;

    @Autowired
    private HIVPatientDataLibrary hivPatientData;

    @Autowired
    private BasePatientDataLibrary basePatientData;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "a8f394fe-c459-4b70-bf5b-7513e50ab122";
    }

    @Override
    public String getUuid() {
        return "f4b2cc4b-79a9-487f-9bb1-34c8112deb94";
    }

    @Override
    public String getName() {
        return "Tx Current Report";
    }

    @Override
    public String getDescription() {
        return "Tx Current Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_CURRENT.xls");
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
        rd.addDataSetDefinition("TX_CURRENT", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxCurrentAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition hadEncounterInQuarter = hivCohortDefinitionLibrary.getArtPatientsWithEncounterOrSummaryPagesBetweenDates();

        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();

        CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod();
        CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod();

        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();


        CohortDefinition childrenOnFirstLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(), hivCohortDefinitionLibrary.getChildrenOnFirstLineRegimenDuringPeriod());
        CohortDefinition childrenOnSecondLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHChildren(), hivCohortDefinitionLibrary.getChildrenOnSecondLineRegimenDuringPeriod());

        CohortDefinition adultsOnFirstLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(), hivCohortDefinitionLibrary.getAdultsOnFirstLineRegimenDuringPeriod());
        CohortDefinition adultsOnSecondLineDuringQuarter = df.getPatientsInAll(commonCohortDefinitionLibrary.MoHAdult(), hivCohortDefinitionLibrary.getAdultsOnSecondLineRegimenDuringPeriod());

        CohortDefinition onThirdLineRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsOnThirdLineRegimenDuringPeriod();

        CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);
        CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter, havingBaseRegimenDuringQuarter);

        CohortDefinition onPreArt = df.getPatientsNotIn(hadEncounterInQuarter, df.getPatientsInAny(beenOnArtBeforeQuarter, beenOnArtDuringQuarter));

        CohortDefinition onFirstLineRegimen = df.getPatientsInAll(beenOnArtDuringQuarter, df.getPatientsInAny(childrenOnFirstLineDuringQuarter, adultsOnFirstLineDuringQuarter));
        CohortDefinition onSecondLineRegimen = df.getPatientsInAll(beenOnArtDuringQuarter, df.getPatientsInAny(childrenOnSecondLineDuringQuarter, adultsOnSecondLineDuringQuarter));

        addIndicator(dsd, "1a", "All currently receiving  ART ",beenOnArtDuringQuarter, "age=below1female");
        addIndicator(dsd, "1b", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between1and4female");
        addIndicator(dsd, "1c", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between5and9female");
        addIndicator(dsd, "1d", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between10and14female");
        addIndicator(dsd, "1e", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between15and19female");
        addIndicator(dsd, "1f", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between20and24female");
        addIndicator(dsd, "1g", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between25and29female");
        addIndicator(dsd, "1h", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between30and34female");
        addIndicator(dsd, "1i", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between35and39female");
        addIndicator(dsd, "1j", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between40and49female");
        addIndicator(dsd, "1k", "All currently receiving  ART", beenOnArtDuringQuarter, "age=above50female");
        addIndicator(dsd, "1l", "All active Pre-Art in the quarter", onPreArt, "age=below1female");
        addIndicator(dsd, "1m", "All active Pre-Art in the quarter", onPreArt, "age=between1and4female");
        addIndicator(dsd, "1n", "All active Pre-Art in the quarter", onPreArt,  "age=between5and9female");
        addIndicator(dsd, "1o", "All active Pre-Art in the quarter", onPreArt, "age=between10and14female");
        addIndicator(dsd, "1p", "All active Pre-Art in the quarter", onPreArt,"age=between15and19female");
        addIndicator(dsd, "1q", "All active Pre-Art in the quarter", onPreArt,  "age=between20and24female");
        addIndicator(dsd, "1r", "All active Pre-Art in the quarter", onPreArt, "age=between25and29female");
        addIndicator(dsd, "1s", "All active Pre-Art in the quarter", onPreArt, "age=between30and34female");
        addIndicator(dsd, "1t", "All active Pre-Art in the quarter", onPreArt,  "age=between35and39female");
       addIndicator(dsd, "1u", "All active Pre-Art in the quarter", onPreArt, "age=between40and49female");
        addIndicator(dsd, "1v", "All active Pre-Art in the quarter", onPreArt, "age=above50female");

        addIndicator(dsd, "2a", "All currently receiving  ART ", beenOnArtDuringQuarter, "age=below1male");
        addIndicator(dsd, "2b", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between1and4male");
        addIndicator(dsd, "2c", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between5and9male");
        addIndicator(dsd, "2d", "All currently receiving  ART", beenOnArtDuringQuarter,"age=between10and14male");
        addIndicator(dsd, "2e", "All currently receiving  ART", beenOnArtDuringQuarter,"age=between15and19male");
        addIndicator(dsd, "2f", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between20and24male");
        addIndicator(dsd, "2g", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between25and29male");
        addIndicator(dsd, "2h", "All currently receiving  ART",beenOnArtDuringQuarter, "age=between30and34male");
        addIndicator(dsd, "2i", "All currently receiving  ART", beenOnArtDuringQuarter, "age=between35and39male");
        addIndicator(dsd, "2j", "All currently receiving  ART",beenOnArtDuringQuarter, "age=between40and49male");
        addIndicator(dsd, "2k", "All currently receiving  ART", beenOnArtDuringQuarter, "age=above50male");
        addIndicator(dsd, "2l", "All active Pre-Art in the quarter", onPreArt, "age=below1male");
        addIndicator(dsd, "2m", "All active Pre-Art in the quarter", onPreArt,"age=between1and4male");
        addIndicator(dsd, "2n", "All active Pre-Art in the quarter", onPreArt, "age=between5and9male");
        addIndicator(dsd, "2o", "All active Pre-Art in the quarter", onPreArt, "age=between10and14male");
        addIndicator(dsd, "2p", "All active Pre-Art in the quarter", onPreArt, "age=between15and19male");
        addIndicator(dsd, "2q", "All active Pre-Art in the quarter", onPreArt, "age=between20and24male");
        addIndicator(dsd, "2r", "All active Pre-Art in the quarter", onPreArt,"age=between25and29male");
        addIndicator(dsd, "2s", "All active Pre-Art in the quarter", onPreArt, "age=between30and34male");
        addIndicator(dsd, "2t", "All active Pre-Art in the quarter", onPreArt,  "age=between35and39male");
        addIndicator(dsd, "2u", "All active Pre-Art in the quarter", onPreArt, "age=between40and49male");
        addIndicator(dsd, "2v", "All active Pre-Art in the quarter", onPreArt, "age=above50male");

        addAgeGender(dsd, "1.1", "All active on Art on 1st line", onFirstLineRegimen);
        addAgeGender(dsd, "2.1", "All active on Art on 2nd line", onSecondLineRegimen);
        addAgeGender(dsd, "3.1", "All active on Art on 3rd line", onThirdLineRegimenDuringQuarter);
        addIndicator(dsd, "4.1j", "PreArt" + " Total", onPreArt, "age=child");
        addIndicator(dsd, "4.2j", "PreArt" + " Total", onPreArt, "age=adult");

        return rd;
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "c", label + " (Below 2 Males)", cohortDefinition, "age=below2male");
        addIndicator(dsd, key + "d", label + " (Below 2 Females)", cohortDefinition, "age=below2female");
        addIndicator(dsd, key + "e", label + " (Between 2 and 5 Males)", cohortDefinition, "age=between2and5male");
        addIndicator(dsd, key + "f", label + " (Between 2 and 5 Females)", cohortDefinition, "age=between2and5female");
        addIndicator(dsd, key + "g", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "h", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "i", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "k", label + " (Above 15 Females)", cohortDefinition, "age=above15female");

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
        return "0.53";
    }
}
