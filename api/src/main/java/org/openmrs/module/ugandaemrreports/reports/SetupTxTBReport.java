package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.reporting.library.cohort.ARTCohortLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *  TX Current Report
 */
@Component
public class SetupTxTBReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary commonCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    ARTCohortLibrary artCohortLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "761271ee-5fc9-44b0-9d77-547015fa8164";
    }

    @Override
    public String getUuid() {
        return "beb0d204-c3f1-43da-81cb-7fa7b9dc31fd";
    }

    @Override
    public String getName() {
        return "Tx TB Report";
    }

    @Override
    public String getDescription() {
        return "Tx TB Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_TB.xls");
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
        rd.addDataSetDefinition("TX_TB", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxTBAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


       CohortDefinition onArtBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();

        CohortDefinition havingBaseRegimenBeforeQuarter = hivCohortDefinitionLibrary.getPatientsHavingBaseRegimenBeforePeriod();

        CohortDefinition havingArtStartDateBeforeQuarter = hivCohortDefinitionLibrary.getArtStartDateBeforePeriod();

        CohortDefinition beenOnArtBeforeQuarter = df.getPatientsInAny(onArtBeforeQuarter, havingArtStartDateBeforeQuarter, havingBaseRegimenBeforeQuarter);



        CohortDefinition assessedForTBDuringQuarter = hivCohortDefinitionLibrary.getAssessedForTBDuringPeriod();

        CohortDefinition diagnosedWithTBDuringQuarter = hivCohortDefinitionLibrary.getDiagnosedWithTBDuringPeriod();
        CohortDefinition screenedNegativeForTB = hivCohortDefinitionLibrary.getScreenedForTBNegativeDuringPeriod();

        CohortDefinition startedART = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();

        CohortDefinition startedOnTBTreatment = artCohortLibrary.patientStartedOnTBTreatmentInQuarter();

        CohortDefinition newlyOnArtAndStartedTBTreatment =df.getPatientsInAll(startedART,startedOnTBTreatment);
        CohortDefinition previouslyOnArtAndStartedTBTreatment =df.getPatientsInAll(beenOnArtBeforeQuarter,startedOnTBTreatment);

        CohortDefinition newlyOnARTAndDiagnosedWithTB = df.getPatientsInAll(diagnosedWithTBDuringQuarter,startedART);
        CohortDefinition newlyOnARTAndScreenedNegativeWithTB = df.getPatientsInAll(screenedNegativeForTB,startedART);

        CohortDefinition previouslyOnARTAndDiagnosedWithTB = df.getPatientsInAll(diagnosedWithTBDuringQuarter,beenOnArtBeforeQuarter);
        CohortDefinition previouslyOnARTAndScreenedNegativeWithTB = df.getPatientsInAll(screenedNegativeForTB,beenOnArtBeforeQuarter);

        disaggregateCohortWithAge(dsd,newlyOnArtAndStartedTBTreatment,"a","b","newlyOnArtAndStartedTBTreatment");
        disaggregateCohortWithAge(dsd,previouslyOnArtAndStartedTBTreatment,"c","d","previouslyonARTAndStartingTBTx");
        disaggregateCohortWithAge(dsd,newlyOnARTAndDiagnosedWithTB,"e","f","newlyOnARTAndDiagnosedWithTB");
        disaggregateCohortWithAge(dsd,newlyOnARTAndScreenedNegativeWithTB,"g","h","newlyOnARTAndScreenedNegativeWithTB");
        disaggregateCohortWithAge(dsd,previouslyOnARTAndDiagnosedWithTB,"i","j","previouslyOnARTAndDiagnosedWithTB");
        disaggregateCohortWithAge(dsd,previouslyOnARTAndScreenedNegativeWithTB,"k","l","previouslyOnARTAndScreenedNegativeWithTB");


        return rd;
    }

    private void disaggregateCohortWithAge(CohortIndicatorDataSetDefinition dsd,CohortDefinition cd,String below15Key,String above15Key,String label){
       if(below15Key!=null)
           addAgeGender(dsd,below15Key,label + "below15" ,cd,"below15");
       if(above15Key!=null)
           addAgeGender(dsd,above15Key,label + "above15+",cd,"above15+");
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition,String ageDimension) {
        addIndicator(dsd, "1"+key , label + " (FeMales)", cohortDefinition, "age="+ageDimension+"female");
        addIndicator(dsd, "2"+key , label + " (Males)", cohortDefinition, "age="+ageDimension+"male");

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
        return "0.1.2.8";
    }
}
