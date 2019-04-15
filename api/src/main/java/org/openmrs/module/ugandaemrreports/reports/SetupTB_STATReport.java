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
public class SetupTB_STATReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private TBCohortDefinitionLibrary tbCohortDefinitionLibrary;

    @Autowired
    private Moh105CohortLibrary moh105CohortLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "07fcb8f4-aa5f-4941-8ff1-437a73585765";
    }

    @Override
    public String getUuid() {
        return "a781267d-2793-496f-81a6-aa460df5777e";
    }

    @Override
    public String getName() {
        return "TB STAT Report";
    }

    @Override
    public String getDescription() {
        return "TB STAT Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TB_STAT.xls");
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
        rd.addDataSetDefinition("TB_STAT", Mapped.mapStraightThrough(dsd));
        //rd.addDataSetDefinition("indicators", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTB_STATAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));

        CohortDefinition knownPositivePatientsBeforePeriod = df.getAnyEncounterOfTypesByStartDate(hivMetadata.getARTSummaryPageEncounterType());
        CohortDefinition testedPositiveDuringPeriod =hivCohortDefinitionLibrary.getPatientsWhoTestedHIVPositiveDuringPeriod();
        CohortDefinition HIVPositivePatients = df.getPatientsNotIn(testedPositiveDuringPeriod,knownPositivePatientsBeforePeriod);
        CohortDefinition testedNegativeDuringPeriod = hivCohortDefinitionLibrary.getPatientsWhoTestedHIVNegativeDuringPeriod();
        CohortDefinition HIVNegativePatients = df.getPatientsNotIn(testedNegativeDuringPeriod,knownPositivePatientsBeforePeriod);
        CohortDefinition newAndRelapsedPatients = tbCohortDefinitionLibrary.getNewAndRelapsedPatientsDuringPeriod();


        CohortDefinition newAndRelapsedTBAndknownPositivePatients =df.getPatientsInAll(knownPositivePatientsBeforePeriod,newAndRelapsedPatients);
        CohortDefinition newAndRelapsedTBAndnewlytestedHIVPostivePatients =df.getPatientsInAll(HIVPositivePatients,newAndRelapsedPatients);
        CohortDefinition newAndRelapsedTBAndnewlytestedHIVNegativePatients =df.getPatientsInAll(HIVNegativePatients,newAndRelapsedPatients);

        addGender(dsd,"a","All Newly and relapsed ",  newAndRelapsedPatients,"female");
        addGender(dsd,"b","All Newly and relapsed ",  newAndRelapsedPatients,"male");

        addGender(dsd,"c","All Newly and relapsed and negative ",  newAndRelapsedTBAndnewlytestedHIVNegativePatients,"female");
        addGender(dsd,"d","All Newly and relapsed and negative",  newAndRelapsedTBAndnewlytestedHIVNegativePatients,"male");

        addGender(dsd,"e","All Newly and relapsed and positive ",  newAndRelapsedTBAndnewlytestedHIVPostivePatients,"female");
        addGender(dsd,"f","All Newly and relapsed and positive", newAndRelapsedTBAndnewlytestedHIVPostivePatients,"male");

        addGender(dsd,"g","All Newly and relapsed known positive ",  newAndRelapsedTBAndknownPositivePatients ,"female");
        addGender(dsd,"h","All Newly and relapsed known positive",newAndRelapsedTBAndknownPositivePatients ,"male");



        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition,String gender){

            addIndicator(dsd, "1"+key, label,cohortDefinition, "age=below1"+gender);
            addIndicator(dsd, "2"+key, label, cohortDefinition, "age=between1and4"+gender);
            addIndicator(dsd, "3"+key, label, cohortDefinition, "age=between5and9"+gender);
            addIndicator(dsd, "4"+key, label, cohortDefinition, "age=between10and14"+gender);
            addIndicator(dsd, "5"+key, label, cohortDefinition, "age=between15and19"+gender);
            addIndicator(dsd, "6"+key, label, cohortDefinition, "age=between20and24"+gender);
            addIndicator(dsd, "7"+key, label, cohortDefinition, "age=between25and29"+gender);
            addIndicator(dsd, "8"+key, label, cohortDefinition, "age=between30and34"+gender);
            addIndicator(dsd, "9"+key,label, cohortDefinition, "age=between35and39"+gender);
            addIndicator(dsd, "10"+key,label, cohortDefinition, "age=between40and44"+gender);
            addIndicator(dsd, "11"+key,label, cohortDefinition, "age=between45and49"+gender);
            addIndicator(dsd, "12"+key,label, cohortDefinition, "age=above50"+gender);
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
        return "0.2.0";
    }
}
