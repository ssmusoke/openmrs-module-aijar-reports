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
public class SetupTxCurrent_30DaysReport extends UgandaEMRDataExportManager {

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
        return "Tx_Current30Days Report";
    }

    @Override
    public String getDescription() {
        return "MER Indicator Report For Tx Curr with Lost To Followup taken as 30 Days After last Missed Appointment";
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

        CohortDefinition deadPatients = df.getDeadPatientsDuringPeriod();
        CohortDefinition transferedOut = hivCohortDefinitionLibrary.getPatientsTransferredOutByEndOfPeriod();
        CohortDefinition tx_Curr_lost_to_followup = df.getPatientsWhoHaveNotComeAfterTheirLastMissedAppointmentByMinimumDays(30);
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

        insertingValuesIntoTemplate(dsd,beenOnArtDuringQuarter);

        return rd;
    }


    public static void insertingValuesIntoTemplate( CohortIndicatorDataSetDefinition dsd,CohortDefinition cohortDefinition){
        addIndicator(dsd, "1a", "All currently receiving  ART ",cohortDefinition, "age=below1female");
        addIndicator(dsd, "1b", "All currently receiving  ART", cohortDefinition, "age=between1and4female");
        addIndicator(dsd, "1c", "All currently receiving  ART", cohortDefinition, "age=between5and9female");
        addIndicator(dsd, "1d", "All currently receiving  ART", cohortDefinition, "age=between10and14female");
        addIndicator(dsd, "1e", "All currently receiving  ART", cohortDefinition, "age=between15and19female");
        addIndicator(dsd, "1f", "All currently receiving  ART", cohortDefinition, "age=between20and24female");
        addIndicator(dsd, "1g", "All currently receiving  ART", cohortDefinition, "age=between25and29female");
        addIndicator(dsd, "1h", "All currently receiving  ART", cohortDefinition, "age=between30and34female");
        addIndicator(dsd, "1i", "All currently receiving  ART", cohortDefinition, "age=between35and39female");
        addIndicator(dsd, "1j", "All currently receiving  ART", cohortDefinition, "age=between40and44female");
        addIndicator(dsd, "1k", "All currently receiving  ART", cohortDefinition, "age=between45and49female");
        addIndicator(dsd, "1l", "All currently receiving  ART", cohortDefinition, "age=above50female");


        addIndicator(dsd, "2a", "All currently receiving  ART ",cohortDefinition, "age=below1male");
        addIndicator(dsd, "2b", "All currently receiving  ART", cohortDefinition, "age=between1and4male");
        addIndicator(dsd, "2c", "All currently receiving  ART", cohortDefinition, "age=between5and9male");
        addIndicator(dsd, "2d", "All currently receiving  ART", cohortDefinition,"age=between10and14male");
        addIndicator(dsd, "2e", "All currently receiving  ART", cohortDefinition,"age=between15and19male");
        addIndicator(dsd, "2f", "All currently receiving  ART", cohortDefinition, "age=between20and24male");
        addIndicator(dsd, "2g", "All currently receiving  ART", cohortDefinition, "age=between25and29male");
        addIndicator(dsd, "2h", "All currently receiving  ART", cohortDefinition, "age=between30and34male");
        addIndicator(dsd, "2i", "All currently receiving  ART", cohortDefinition, "age=between35and39male");
        addIndicator(dsd, "2j", "All currently receiving  ART", cohortDefinition, "age=between40and44male");
        addIndicator(dsd, "2k", "All currently receiving  ART", cohortDefinition, "age=between45and49male");
        addIndicator(dsd, "2l", "All currently receiving  ART", cohortDefinition, "age=above50male");

    }

    public static void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        Helper.addIndicator(dsd,key,label,cohortDefinition,dimensionOptions);   }

    @Override
    public String getVersion() {
        return "0.8.5.1";
    }
}
