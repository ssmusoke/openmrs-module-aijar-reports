package org.openmrs.module.aijarreports.reports;

import org.openmrs.api.PatientSetService;
import org.openmrs.module.aijarreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.library.DataFactory;
import org.openmrs.module.aijarreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
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

public class SetUp106A1BReport extends AijarDataExportManager {
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private DataFactory df;

    @Override
    public String getExcelDesignUuid() {
        return "b98ab976-9c9d-4a28-9760-ac3119c8ef23";
    }

    @Override
    public String getUuid() {
        return "167cf668-0715-488b-b159-d5f391774038";
    }

    @Override
    public String getName() {
        return "HMIS 106A1B";
    }

    @Override
    public String getDescription() {
        return "HMIS 106A1B";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "106A1BReport.xls");
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
        rd.addDataSetDefinition("indicators_106a1b", Mapped.mapStraightThrough(dsd));

        CohortDefinition transferredOut = hivCohortDefinitionLibrary.getTransferredOut();
        addIndicator(dsd, "123", "Transferred", transferredOut);

        for (int i = 1; i <= 6; i++) {

            CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod(i + "y");
            CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod(i + "y-1d");

            CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod(i + "y-1d");
            CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod(i + "y-1d");

            CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(i + "y");
            CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod(i + "y-1d");

            CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);
            CohortDefinition beenOnArtDuringQuarter = df.getPatientsInAny(onArtDuringQuarter, havingArtStartDateDuringQuarter, havingBaseRegimenDuringQuarter);

            CohortDefinition startedArtDuringQuarter = df.getPatientsNotIn(beenOnArtDuringQuarter, beenOnArtBeforeQuarter);

            CohortDefinition patientsHavingCD4LessThan250 = df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getBaselineCD4(), hivMetadata.getARTSummaryPageEncounterType(), RangeComparator.LESS_EQUAL, 250.0, i + "y", PatientSetService.TimeModifier.FIRST);
            CohortDefinition patientsOlderThan4Years = commonCohortDefinitionLibrary.agedAtLeast(5);
            CohortDefinition transferredInTheQuarter = hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod(i + "y");
            CohortDefinition medicallyInterrupted = hivCohortDefinitionLibrary.getInterruptedMedically();
            CohortDefinition deadPatients = hivCohortDefinitionLibrary.gePatientsWhoDied();
            CohortDefinition appointments = hivCohortDefinitionLibrary.getAppointments();


            addIndicator(dsd, String.valueOf(i) + "3", "3", startedArtDuringQuarter);
            addIndicator(dsd, String.valueOf(i) + "4", "4", patientsHavingCD4LessThan250);
            addIndicator(dsd, String.valueOf(i) + "5", "5", patientsOlderThan4Years);
            addIndicator(dsd, String.valueOf(i) + "6", "6", appointments);


        }
        return rd;
    }

    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), "");
    }

    @Override
    public String getVersion() {
        return "0.1";
    }
}
