package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.dimension.CohortDefinitionDimension;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.NameOfHealthUnitDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.*;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reports2019.SetupMER_TX_ML2019Report;
import org.openmrs.module.ugandaemrreports.reports2019.SetupTxRTT2019Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.openmrs.module.ugandaemrreports.library.Cohorts.transferIn;

/**
 *  TX Current Report
 */
@Component
public class SetupPediatricDataCallReport extends UgandaEMRDataExportManager {

    @Autowired
    private DataFactory df;

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private SetupMERTxNew2019Report setupTxNewReport;
    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;


    @Autowired
    private SetupTxRTT2019Report setupTxRTT2019Report;




    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "3f503360-51ac-4fa0-ae63-c484dfae51f4";
    }

    @Override
    public String getUuid() {
        return "639026cd-1cb9-46c1-9a06-81208e9ed412";
    }

    @Override
    public String getName() {
        return "Transitioning Children and Viral Load Monitoring  in PMTCT Report";
    }

    @Override
    public String getDescription() {
        return "Transitioning Children and Viral Load Monitoring  in PMTCT Report";
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "DATA_CALL.xls");
    }

    @Override
    public ReportDefinition constructReportDefinition() {

        ReportDefinition rd = new ReportDefinition();

        rd.setUuid(getUuid());
        rd.setName(getName());
        rd.setDescription(getDescription());
        rd.setParameters(getParameters());
        rd.addDataSetDefinition("HC", Mapped.mapStraightThrough(healthFacilityName()));

        CohortIndicatorDataSetDefinition dsd = new CohortIndicatorDataSetDefinition();

        dsd.setParameters(getParameters());
        rd.addDataSetDefinition("COHORT", Mapped.mapStraightThrough(dsd));

        CohortDefinitionDimension ageDimension =commonDimensionLibrary.getTxNewAgeGenderGroup();
        dsd.addDimension("age", Mapped.mapStraightThrough(ageDimension));


//        CohortDefinition males = cohortDefinitionLibrary.males();
//        CohortDefinition females = cohortDefinitionLibrary.females();
        CohortDefinition TX_ML = hivCohortDefinitionLibrary.getPatientsWithNoClinicalContactsByEndDateForDays(28);
        CohortDefinition deadPatientsPreviousQuarter = df.getDeadPatientsByEndDate("3m");

        CohortDefinition tx_Curr_lost_to_followup_previousQuater = hivCohortDefinitionLibrary.getPatientsTxLostToFollowupByDaysInPreviousQuarter("28","3m");
        CohortDefinition transferredOutPreviousQuater =hivCohortDefinitionLibrary.getPatientsTransferredOutDuringPeriod("3m");
        CohortDefinition excludedPatients =df.getPatientsInAny(deadPatientsPreviousQuarter,transferredOutPreviousQuater,tx_Curr_lost_to_followup_previousQuater);

        CohortDefinition transferredInToCareDuringPeriod= hivCohortDefinitionLibrary.getTransferredInToCarePreviousQuarter("3m");
        CohortDefinition havingBaseRegimenDuringQuarter = df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(),"3m", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition havingArtStartDateDuringQuarter =  df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(),"3m", BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition onArtDuringQuarter =  df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(),"3m", BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition patientsWithactiveReturnVisitDate = hivCohortDefinitionLibrary.getPatientsWithEncountersBeforeEndDateOfPreviousQuarterThatHaveReturnVisitDatesByStartDateOfPreviousQuarter("3m");
        CohortDefinition allActivePatients = df.getPatientsInAny(patientsWithactiveReturnVisitDate,transferredInToCareDuringPeriod,havingArtStartDateDuringQuarter,
                onArtDuringQuarter, havingBaseRegimenDuringQuarter);
        CohortDefinition activePatientsinPreviousQuarter= df.getPatientsNotIn(allActivePatients,excludedPatients);

        CohortDefinition TXNewInReportingPeriod = df.getPatientsNotIn(hivCohortDefinitionLibrary.getArtStartDateBetweenPeriod(),transferIn());

        CohortDefinition below15YearsWithInReportingPeriod = cohortDefinitionLibrary.MoHChildren();
        CohortDefinition patientsMaking15YearsWithInReportingPeriod =df.getPatientsInAll(agedAtMostByPreviousQuarter(14), cohortDefinitionLibrary.agedBetween(15,15));

        CohortDefinition activeWithinReportingPeriod = hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28");

        CohortDefinition activePatientsinPreviousQuarterBelow15Years = df.getPatientsInAll(activePatientsinPreviousQuarter,agedAtMostByPreviousQuarter(14));

        CohortDefinition TXMLAndActivePatientsinPreviousQuarter15Years = df.getPatientsInAll(TX_ML,activePatientsinPreviousQuarterBelow15Years);

//         cohorts for pregnant women and viral load taking
        CohortDefinition deadPatients2QuartersBefore = df.getDeadPatientsByEndDate("6m");
        CohortDefinition tx_Curr_lost_to_followup_previous2Quater = hivCohortDefinitionLibrary.getPatientsTxLostToFollowupByDaysInPreviousQuarter("28","6m");
        CohortDefinition transferredOutPrevious2Quaters =hivCohortDefinitionLibrary.getPatientsTransferredOutDuringPeriod("6m");
        CohortDefinition excludedPatients2QuatersBefore =df.getPatientsInAny(deadPatients2QuartersBefore,transferredOutPrevious2Quaters,tx_Curr_lost_to_followup_previous2Quater);

        CohortDefinition transferredInToCare2QuatersBefore= hivCohortDefinitionLibrary.getTransferredInToCarePreviousQuarter("6m");
        CohortDefinition havingBaseRegimen2QuatersBefore= df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getArtStartRegimen(), hivMetadata.getARTSummaryPageEncounterType(),"6m", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition havingArtStartDate2QuatersBefore =  df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getArtStartDate(), hivMetadata.getARTSummaryPageEncounterType(),"6m", BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition onArtDuring2QuartersBefore =  df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(),"6m", BaseObsCohortDefinition.TimeModifier.ANY);

        CohortDefinition patientsWithActiveReturnVisitDate2Quarters = hivCohortDefinitionLibrary.getPatientsWithEncountersBeforeEndDateOfPreviousQuarterThatHaveReturnVisitDatesByStartDateOfPreviousQuarter("6m");
        CohortDefinition allActivePatients2 = df.getPatientsInAny(patientsWithActiveReturnVisitDate2Quarters,transferredInToCare2QuatersBefore,havingArtStartDate2QuatersBefore,
                onArtDuring2QuartersBefore, havingBaseRegimen2QuatersBefore);
        CohortDefinition activePatientsinPrevious2Quarters = df.getPatientsNotIn(allActivePatients2,excludedPatients2QuatersBefore);
        CohortDefinition pregnant2QuatersBefore =   df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getPregnantAtArtStart(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()),"6m", BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition breastFeeding2QuatersBefore =  df.getPatientsWithCodedObsDuringPeriod(hivMetadata.getLactatingAtArtStart(), hivMetadata.getARTSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()),"6m",BaseObsCohortDefinition.TimeModifier.ANY);
        CohortDefinition breastFeedingAndLactating = df.getPatientsInAny(pregnant2QuatersBefore, breastFeeding2QuatersBefore);
        CohortDefinition pregnantPatient2QuatersBefore = df.getPatientsWithCodedObsDuringPeriod(Dictionary.getConcept("dcda5179-30ab-102d-86b0-7a5022ba4115"),hivMetadata.getARTEncounterPageEncounterType(),
                Arrays.asList(Dictionary.getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"),Dictionary.getConcept("9e5ac0a8-6041-4feb-8c07-fe522ef5f9ab")),"6m", BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition activeAndPregnantAndBreastFeedingClients = df.getPatientsInAll(activePatientsinPrevious2Quarters,df.getPatientsInAny(breastFeedingAndLactating,pregnantPatient2QuatersBefore));
        CohortDefinition viraLoadTakenDuringPeriod = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getViralLoadDate(),hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.LAST);
        CohortDefinition viraLoadTakenAndSupressed = df.getPatientsInAll(viraLoadTakenDuringPeriod,df.getPatientsWithNumericObsDuringPeriod(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.LESS_THAN,1000.0, BaseObsCohortDefinition.TimeModifier.LAST));



        CohortDefinition patientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod = setupTxRTT2019Report.getPatientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod();
        CohortDefinition patientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriodAndBackToCareDuringPeriod = df.getPatientsInAll(activeWithinReportingPeriod,patientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod);

        setupTxNewReport.addIndicator(dsd, "d","Active Children in the quarter Before the Reporting Quarter",activePatientsinPreviousQuarterBelow15Years,"");
        setupTxNewReport.addIndicator(dsd, "e","TX_ML_Within_Reporting_Period",TXMLAndActivePatientsinPreviousQuarter15Years ,"");
        setupTxNewReport.addIndicator(dsd, "f","RTT",df.getPatientsInAll( patientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriodAndBackToCareDuringPeriod,agedAtMostByPreviousQuarter(14)) ,"");
        setupTxNewReport.addIndicator(dsd, "g","TX_NEW",df.getPatientsInAll(TXNewInReportingPeriod,below15YearsWithInReportingPeriod) ,"");
        setupTxNewReport.addIndicator(dsd, "h","TX_NEW_Active_turning_15_YearsInReporting_Period",df.getPatientsInAll(df.getPatientsInAny(TXNewInReportingPeriod,activePatientsinPreviousQuarterBelow15Years),patientsMaking15YearsWithInReportingPeriod) ,"");
        setupTxNewReport.addIndicator(dsd, "i","TX_CURR_Children_In_Reporting_Period",df.getPatientsInAll(activeWithinReportingPeriod,below15YearsWithInReportingPeriod) ,"");

        setupTxNewReport.addIndicator(dsd, "j","pregnant and breast feeding",activeAndPregnantAndBreastFeedingClients ,"");
        setupTxNewReport.addIndicator(dsd, "k","pregnant and breast feeding and viral load done at 6months",df.getPatientsInAll(activeAndPregnantAndBreastFeedingClients,viraLoadTakenDuringPeriod) ,"");
        setupTxNewReport.addIndicator(dsd, "l","pregnant and breast feeding and viral load done at 6months and is suppressed",df.getPatientsInAll(activeAndPregnantAndBreastFeedingClients,viraLoadTakenAndSupressed) ,"");
//

        return rd;
    }


    public CohortDefinition agedAtMostByPreviousQuarter(int maxAge) {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("aged at most " + maxAge);
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMaxAge(maxAge);
        UUID uuid = UUID.randomUUID();
        cd.setUuid(String.valueOf(uuid));
        return df.convert(cd, ObjectUtil.toMap("effectiveDate=endDate-3m"));
    }

    private DataSetDefinition healthFacilityName() {
        NameOfHealthUnitDatasetDefinition dsd = new NameOfHealthUnitDatasetDefinition();
        dsd.setFacilityName("aijar.healthCenterName");
        return dsd;
    }

    @Override
    public String getVersion() {
        return "1.2.1";
    }
}
