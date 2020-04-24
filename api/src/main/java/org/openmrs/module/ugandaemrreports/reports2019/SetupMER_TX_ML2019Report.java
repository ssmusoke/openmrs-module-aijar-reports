package org.openmrs.module.ugandaemrreports.reports2019;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.CommonDimensionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.Moh105CohortLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reports.SetupTxNewReport;
import org.openmrs.module.ugandaemrreports.reports.UgandaEMRDataExportManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 *  PMTCT STAT Report
 */
@Component
public class SetupMER_TX_ML2019Report extends UgandaEMRDataExportManager {

    @Autowired
    private SetupTxNewReport setupTxNewReport;

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "adba307c-742d-46e1-8cf2-980054a7b35f";
    }

    @Override
    public String getUuid() {
        return "5b922f62-f844-4962-9465-61cf8f52f4b7";
    }

    @Override
    public String getName() {
        return "MER TX ML 2019 Report";
    }

    @Override
    public String getDescription() {
        return "MER Indicator Report for TX ML 2019 version";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_ML_2019.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {
        String params = "startDate=${startDate},endDate=${endDate}";
        ReportDefinition rd = new ReportDefinition();
        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("TX_ML", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition TX_ML = hivCohortDefinitionLibrary.getPatientsWithNoClinicalContactsByEndDateForDays(28);
        CohortDefinition silentlyTransferred=df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getConcept("8f889d84-8e5c-4a66-970d-458d6d01e8a4"),hivMetadata.getMissedAppointmentEncounterType(),
                Arrays.asList(hivMetadata.getConcept("f57b1500-7ff2-46b4-b183-fed5bce479a9")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();

        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();

        CohortDefinition stoppedARTDuringPeriod = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getConcept("ac98d431-8ebc-4397-8c78-78b0eee0ffe7"), hivMetadata.getARTSummaryPageEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition stoppedARTInterruption = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getConcept("3aaf3680-6240-4819-a704-e20a93841942"),hivMetadata.getARTSummaryPageEncounterType(),
                Arrays.asList(hivMetadata.getConcept("4212962f-437a-4723-b4bd-3ce69fe0aac9")), BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition stoppedARVOnARTCard =df.getPatientsInAll(stoppedARTInterruption,stoppedARTDuringPeriod);
        CohortDefinition stoppedARVTaking=df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getConcept("8f889d84-8e5c-4a66-970d-458d6d01e8a4"),hivMetadata.getMissedAppointmentEncounterType(),
                Arrays.asList(hivMetadata.getConcept("dca26b47-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.LAST);

        CohortDefinition ClientsStoppedonART =df.getPatientsInAny(stoppedARVOnARTCard,stoppedARVTaking);

        CohortDefinition diedTraceOutcome=df.getDeadPatientsDuringPeriod();

        CohortDefinition diedOfTB =df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("dc6527eb-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition diedOfCancer = df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("116030AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition diedOfInfectiousDiseases = df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("73d67c86-06df-4863-9819-ccb2a6bb98f8")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition diedOfNonInfectiousDiseases = df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("13a7b84b-b661-48a5-8315-0bcc0174e5c8")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition diedOfNaturalCauses = df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("84899c95-d455-4293-be6f-7db600af058f")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition diedOfNonNaturalCauses = df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("ab115b14-8a9f-4185-9deb-79c214dc1063")), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition diedOfUnknown =df.getPatientsWithCodedObsDuringPeriod(getCauseOfDeathConcept(),null, Arrays.asList(hivMetadata.getConcept("dcd6865a-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.LAST);

        addAgeAndGender( dsd, "a", "died", df.getPatientsInAll(TX_ML,diedTraceOutcome));
        addAgeAndGender( dsd, "b", "silentlyTransferred", df.getPatientsInAll(TX_ML,silentlyTransferred));
        addAgeAndGender( dsd, "c", "LTFP L3MONTHS",  df.getPatientsInAll(TX_ML,havingArtStartDateDuringQuarter));
        addAgeAndGender( dsd, "d", "LTFP G3MONTHS", df.getPatientsInAll(TX_ML,havingArtStartDateBeforeQuarter));
        addAgeAndGender( dsd, "e", "stoppedARVTaking",  df.getPatientsInAll(TX_ML,ClientsStoppedonART));
        addAgeAndGender( dsd, "f", "died Of TB",  df.getPatientsInAll(TX_ML,diedOfTB));
        addAgeAndGender( dsd, "g", "died Of Cancer",  df.getPatientsInAll(TX_ML,diedOfCancer));
        addAgeAndGender( dsd, "h", " other died Of Infectious Diseases", df.getPatientsInAll(TX_ML, diedOfInfectiousDiseases));
        addAgeAndGender( dsd, "i", "died Of non infectious diseases",  df.getPatientsInAll(TX_ML,diedOfNonInfectiousDiseases));
        addAgeAndGender( dsd, "j", "died Of Natural causes",  df.getPatientsInAll(TX_ML,diedOfNaturalCauses));
        addAgeAndGender( dsd, "k", "died Of non natural causes",  df.getPatientsInAll(TX_ML,diedOfNonNaturalCauses));
        addAgeAndGender( dsd, "l", "unknown cause of death",  df.getPatientsInAll(TX_ML,diedOfUnknown));

        return rd;
    }

    public void addAgeAndGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        /**          females age and gender mapping **/
        setupTxNewReport.addIndicator(dsd, "1" + key, label, cohortDefinition, "age=below1female");
        setupTxNewReport.addIndicator(dsd, "2" + key, label, cohortDefinition, "age=between1and4female");
        setupTxNewReport.addIndicator(dsd, "3" + key, label, cohortDefinition, "age=between5and9female");
        setupTxNewReport.addIndicator(dsd, "4" + key, label, cohortDefinition, "age=between10and14female");
        setupTxNewReport.addIndicator(dsd, "5" + key, label, cohortDefinition, "age=between15and19female");
        setupTxNewReport.addIndicator(dsd, "6" + key, label, cohortDefinition, "age=between20and24female");
        setupTxNewReport.addIndicator(dsd, "7" + key, label, cohortDefinition, "age=between25and29female");
        setupTxNewReport.addIndicator(dsd, "8" + key, label, cohortDefinition, "age=between30and34female");
        setupTxNewReport.addIndicator(dsd, "9" + key, label, cohortDefinition, "age=between35and39female");
        setupTxNewReport.addIndicator(dsd, "10" + key, label, cohortDefinition, "age=between40and44female");
        setupTxNewReport.addIndicator(dsd, "11" + key, label, cohortDefinition, "age=between45and49female");
        setupTxNewReport.addIndicator(dsd, "12" + key, label, cohortDefinition, "age=above50female");
        /**         males age and gender mapping **/
        setupTxNewReport.addIndicator(dsd, "13" + key, label, cohortDefinition, "age=below1male");
        setupTxNewReport.addIndicator(dsd, "14" + key, label, cohortDefinition, "age=between1and4male");
        setupTxNewReport.addIndicator(dsd, "15" + key, label, cohortDefinition, "age=between5and9male");
        setupTxNewReport.addIndicator(dsd, "16" + key, label, cohortDefinition, "age=between10and14male");
        setupTxNewReport.addIndicator(dsd, "17" + key, label, cohortDefinition, "age=between15and19male");
        setupTxNewReport.addIndicator(dsd, "18" + key, label, cohortDefinition, "age=between20and24male");
        setupTxNewReport.addIndicator(dsd, "19" + key, label, cohortDefinition, "age=between25and29male");
        setupTxNewReport.addIndicator(dsd, "20" + key, label, cohortDefinition, "age=between30and34male");
        setupTxNewReport.addIndicator(dsd, "21" + key, label, cohortDefinition, "age=between35and39male");
        setupTxNewReport.addIndicator(dsd, "22" + key, label, cohortDefinition, "age=between40and44male");
        setupTxNewReport.addIndicator(dsd, "23" + key, label, cohortDefinition, "age=between45and49male");
        setupTxNewReport.addIndicator(dsd, "24" + key, label, cohortDefinition, "age=above50male");

    }


    private Concept getCauseOfDeathConcept(){
        return hivMetadata.getConcept("dca2c3f2-30ab-102d-86b0-7a5022ba4115");
    }


    @Override
    public String getVersion() {
        return "0.0.3";
    }
}
