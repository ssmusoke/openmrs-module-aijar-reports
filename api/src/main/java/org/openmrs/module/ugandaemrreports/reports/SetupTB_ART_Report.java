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
public class SetupTB_ART_Report extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;
    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private TBCohortDefinitionLibrary tbCohortDefinitionLibrary;



    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "b0033cdc-3c96-4227-9c1d-13874a74830c";
    }

    @Override
    public String getUuid() {
        return "ebab92eb-90b8-460d-a356-c2f7800e068f";
    }

    @Override
    public String getName() {
        return "TB ART Report";
    }

    @Override
    public String getDescription() {
        return "TB ART Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TB_ART.xls");
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
        rd.addDataSetDefinition("TB_ART", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension = commonDimensionLibrary.getTB_STATAndTB_ARTAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));



        CohortDefinition newAndRelapsedPatients = tbCohortDefinitionLibrary.getNewAndRelapsedPatientsDuringPeriod();
        CohortDefinition withDocumentedHIVPostiveStatus = df.getAnyEncounterOfTypesByEndOfDate(hivMetadata.getARTSummaryPageEncounterType());

        CohortDefinition patientsHIVPositiveWIthARTEncounterAndOnTBTrtment =df.getPatientsInAll(newAndRelapsedPatients,withDocumentedHIVPostiveStatus);
        CohortDefinition newOnART = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
        CohortDefinition havingRegimenBeforePeriod = hivCohortDefinitionLibrary.getPatientsHavingRegimenBeforePeriod();
        CohortDefinition transferredInToCareDuringPeriod= hivCohortDefinitionLibrary.getTransferredInToCareDuringPeriod();
        CohortDefinition alreadyOnART = df.getPatientsInAll(havingRegimenBeforePeriod,transferredInToCareDuringPeriod);

        CohortDefinition testedPositiveDuringPeriod =hivCohortDefinitionLibrary.getPatientsWhoTestedHIVPositiveDuringPeriod();
        CohortDefinition newHivPOSAndTBPatientsDuringPeriod =df.getPatientsInAll(testedPositiveDuringPeriod,newAndRelapsedPatients);

        CohortDefinition newOnArtAndTBPatients =df.getPatientsInAll(newOnART,patientsHIVPositiveWIthARTEncounterAndOnTBTrtment);
        CohortDefinition alreadyOnARTAndTBpatients =df.getPatientsInAll(patientsHIVPositiveWIthARTEncounterAndOnTBTrtment,alreadyOnART);
        CohortDefinition TBSTAT_POS_Patients =  df.getPatientsInAny(newHivPOSAndTBPatientsDuringPeriod,patientsHIVPositiveWIthARTEncounterAndOnTBTrtment);

        addGender(dsd,"a","new On ART",  newOnArtAndTBPatients,"female");
        addGender(dsd,"b","new on ART ",  newOnArtAndTBPatients,"male");

        addGender(dsd,"c","already On ART",  alreadyOnARTAndTBpatients,"female");
        addGender(dsd,"d","already on ART ", alreadyOnARTAndTBpatients,"male");

        addGender(dsd,"e","TBSTAT_POS_Patients",  TBSTAT_POS_Patients,"female");
        addGender(dsd,"f","TBSTAT_POS_Patients", TBSTAT_POS_Patients,"male");


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
        return "0.1.2";
    }
}