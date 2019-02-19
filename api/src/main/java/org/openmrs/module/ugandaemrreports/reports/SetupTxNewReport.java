package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
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
public class SetupTxNewReport extends UgandaEMRDataExportManager {

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
    private ARTCohortLibrary artCohortLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "941f0989-635a-473e-96a8-ada6e2d6409e";
    }

    @Override
    public String getUuid() {
        return "621890e1-3549-4ccf-ad93-c206bd3e7c17";
    }

    @Override
    public String getName() {
        return "Tx New Report";
    }

    @Override
    public String getDescription() {
        return "Tx New Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "MER_TX_NEW.xls");
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
        rd.addDataSetDefinition("TX_NEW", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


        CohortDefinition havingArtStartDateDuringQuarter = hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod();
//        CohortDefinition pregnantOrLactating = hivCohortDefinitionLibrary.getPregnantOrLactating();

//        CohortDefinition pregnantAtFirstEncounter = hivCohortDefinitionLibrary.getPatientsPregnantAtFirstEncounter();

        CohortDefinition diagnosedWithTBDuringQuarter = hivCohortDefinitionLibrary.getDiagnosedWithTBDuringPeriod();

        CohortDefinition onTBRxDuringQuarter = hivCohortDefinitionLibrary.getStartedTBRxDuringPeriod();
        CohortDefinition onTBRxBeforeQuarter = hivCohortDefinitionLibrary.getStartedTBRxBeforePeriod();

        CohortDefinition pregnant = addParameters(artCohortLibrary.pregnantAtARTStart());
        CohortDefinition lactating =addParameters(artCohortLibrary.lactatingAtARTStart());
        CohortDefinition pregnantOrLactating =df.getPatientsInAll(addParameters(artCohortLibrary.pregnantOrLactatingAtARTStart()),havingArtStartDateDuringQuarter);


        CohortDefinition lactatingAtStartOfArt =df.getPatientsInAll(lactating,havingArtStartDateDuringQuarter);
        CohortDefinition startedArtWhenPregnant = df.getPatientsInAll(pregnant, havingArtStartDateDuringQuarter);
        CohortDefinition startedArtAndDiagnosedWithTB =df.getPatientsInAll(havingArtStartDateDuringQuarter,df.getPatientsInAny(diagnosedWithTBDuringQuarter,onTBRxDuringQuarter,onTBRxBeforeQuarter));

        addGender(dsd,"a","All Newly enrolled  on ART ",  havingArtStartDateDuringQuarter);
        addGender(dsd,"b","All Newly enrolled  on ART ",  havingArtStartDateDuringQuarter);

        addAgeGender(dsd, "1", "Start on ART ",  havingArtStartDateDuringQuarter);

        addIndicator(dsd,"2h","Pregnant and lactating enrolled in care ", pregnantOrLactating, "age=between5and14female");
        addIndicator(dsd,"2j","Pregnant and lactating enrolled in care ", pregnantOrLactating,"age=above15female");

        addIndicator(dsd,"10a","started Art And Diagnosed With TB",startedArtAndDiagnosedWithTB,"age=female");
        addIndicator(dsd,"10b","started Art And Diagnosed With TB",startedArtAndDiagnosedWithTB,"age=male");
        addIndicator(dsd,"11a","Were pregnant when started ART",startedArtWhenPregnant,"");
        addIndicator(dsd,"12a","Lactating At start on Art",lactatingAtStartOfArt,"");

        return rd;
    }

    public void addGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition){
        if(key=="a"){
           // addIndicator(dsd, "1"+key, label,cohortDefinition, "age=below1female");
            addIndicator(dsd, "2"+key, label, cohortDefinition, "age=below1female");
            addIndicator(dsd, "3"+key, label, cohortDefinition, "age=between1and4female");
            addIndicator(dsd, "4"+key, label, cohortDefinition, "age=between5and9female");
            addIndicator(dsd, "5"+key, label, cohortDefinition, "age=between10and14female");
            addIndicator(dsd, "6"+key, label, cohortDefinition, "age=between15and19female");
            addIndicator(dsd, "7"+key, label, cohortDefinition, "age=between20and24female");
            addIndicator(dsd, "8"+key, label, cohortDefinition, "age=between25and49female");
            addIndicator(dsd, "9"+key,label, cohortDefinition, "age=above50female");
        }
        else if(key=="b"){
          //  addIndicator(dsd, "1"+key, label,cohortDefinition, "age=below1male");
            addIndicator(dsd, "2"+key, label, cohortDefinition, "age=below1male");
            addIndicator(dsd, "3"+key, label, cohortDefinition, "age=between1and4male");
            addIndicator(dsd, "4"+key, label, cohortDefinition, "age=between5and9male");
            addIndicator(dsd, "5"+key, label, cohortDefinition, "age=between10and14male");
            addIndicator(dsd, "6"+key, label, cohortDefinition, "age=between15and19male");
            addIndicator(dsd, "7"+key, label, cohortDefinition, "age=between20and24male");
            addIndicator(dsd, "8"+key, label, cohortDefinition, "age=between25and49male");
            addIndicator(dsd, "9"+key,label, cohortDefinition, "age=above50male");
        }
    }

    private void addAgeGender(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition) {
        addIndicator(dsd, key + "c", label + " (Below 2 Males)", cohortDefinition, "age=below2male");
        addIndicator(dsd, key + "d", label + " (Below 2 Females)", cohortDefinition, "age=below2female");
        addIndicator(dsd, key + "e", label + " (Between 2 and 5 Males)", cohortDefinition, "age=between2and5male");
        addIndicator(dsd, key + "f", label + " (Between 2 and 5 Females)", cohortDefinition, "age=between2and5female");
        addIndicator(dsd, key + "g", label + " (Between 5 and 14 Males)", cohortDefinition, "age=between5and14male");
        addIndicator(dsd, key + "h", label + " (Between 5 and 14 Females)", cohortDefinition, "age=between5and14female");
        addIndicator(dsd, key + "i", label + " (Above 15 Males)", cohortDefinition, "age=above15male");
        addIndicator(dsd, key + "j", label + " (Above 15 Females)", cohortDefinition, "age=above15female");

    }


    public void addIndicator(CohortIndicatorDataSetDefinition dsd, String key, String label, CohortDefinition cohortDefinition, String dimensionOptions) {
        CohortIndicator ci = new CohortIndicator();
        ci.addParameter(ReportingConstants.START_DATE_PARAMETER);
        ci.addParameter(ReportingConstants.END_DATE_PARAMETER);
        ci.setType(CohortIndicator.IndicatorType.COUNT);
        ci.setCohortDefinition(Mapped.mapStraightThrough(cohortDefinition));
        dsd.addColumn(key, label, Mapped.mapStraightThrough(ci), dimensionOptions);
    }

    public CohortDefinition addParameters(CohortDefinition cohortDefinition){
      return   df.convert(cohortDefinition, ObjectUtil.toMap("onOrAfter=startDate,onOrBefore=endDate"));
    }

    @Override
    public String getVersion() {
        return "0.4.0";
    }
}
