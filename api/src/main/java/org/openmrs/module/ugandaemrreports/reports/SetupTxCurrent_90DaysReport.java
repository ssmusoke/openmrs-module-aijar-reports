package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
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
public class SetupTxCurrent_90DaysReport extends UgandaEMRDataExportManager {

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
        return "52c7d919-c978-4272-ab21-9bd94c123525";
    }

    @Override
    public String getUuid() {
        return "660bdb55-36ea-4e57-8c62-136300035ce3";
    }

    @Override
    public String getName() {
        return "Tx Current90Days Report";
    }

    @Override
    public String getDescription() {
        return "Tx Current_90Days Report";
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
        rd.addDataSetDefinition("TX_CURRENT_90Days", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxCurrentAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition deadPatients = df.getDeadPatientsDuringPeriod();
        CohortDefinition transferedOut = hivCohortDefinitionLibrary.getPatientsTransferredOutDuringPeriod();
        CohortDefinition tx_Curr_lost_to_followup = df.getPatientsWhoHaveNotComeAfterTheirLastMissedAppointmentByMinimumDays(90);
        CohortDefinition excludedPatients =df.getPatientsInAny(deadPatients,transferedOut,tx_Curr_lost_to_followup);


        //cohorts for currently on ART
        CohortDefinition transferredInToCareDuringPeriod= hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();
        CohortDefinition havingBaseRegimenDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenDuringPeriod();
        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition onArtDuringQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenDuringPeriod();
        CohortDefinition longRefills = df.getPatientsWithLongRefills();

        CohortDefinition eligible = df.getPatientsInAny(longRefills,transferredInToCareDuringPeriod,havingArtStartDateDuringQuarter,
                                            onArtDuringQuarter, havingBaseRegimenDuringQuarter);

        CohortDefinition beenOnArtDuringQuarter = df.getPatientsNotIn(eligible,excludedPatients);

        SetupTxCurrent_30DaysReport.insertingValuesIntoTemplate(dsd,beenOnArtDuringQuarter);
        return rd;
    }

    @Override
    public String getVersion() {
        return "0.1.8";
    }
}
