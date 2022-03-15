package org.openmrs.module.ugandaemrreports.reports;

import org.openmrs.EncounterType;
import org.openmrs.module.reporting.ReportingConstants;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.dataset.definition.CohortIndicatorDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.indicator.CohortIndicator;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.ugandaemrreports.library.ARTClinicCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.Cohorts;
import org.openmrs.module.ugandaemrreports.library.CommonCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.DataFactory;
import org.openmrs.module.ugandaemrreports.library.EIDCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.library.Moh105CohortLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * CQI Indicators Report
 */
@Component
public class SetupCareAuditCQIIndicatorsReport extends UgandaEMRDataExportManager {

    @Autowired
    ARTClinicCohortDefinitionLibrary hivCohorts;

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Autowired
    private EIDCohortDefinitionLibrary eidCohortDefinitionLibrary;

    @Autowired
    private CommonCohortDefinitionLibrary cohortDefinitionLibrary;

    @Autowired
    private Moh105CohortLibrary moh105CohortLibrary;


    /**
     * @return the uuid for the report design for exporting to Excel
     */
    @Override
    public String getExcelDesignUuid() {
        return "1691eb39-d3f5-13fc-b54c-033ade306d58";
    }

    @Override
    public String getUuid() {
        return "138140e6-1dd2-11b2-8811-ec47ee6bbc58";
    }

    @Override
    public String getName() {
        return "Care Audit CQI Indicators Report";
    }

    @Override
    public String getDescription() {
        return "Care Audit CQI Indicators Report";
    }

    @Override
    public List<Parameter> getParameters() {
        List<Parameter> l = new ArrayList<>();
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
        return createExcelTemplateDesign(getExcelDesignUuid(), reportDefinition, "CareAuditCQIIndicatorsReport.xls");
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
        rd.addDataSetDefinition("CQI_Indicators", Mapped.mapStraightThrough(dsd));

        CohortDefinition activeInCare = hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28");
        CohortDefinition below9Years = cohortDefinitionLibrary.agedAtMost(9);
        CohortDefinition between10to19Years = cohortDefinitionLibrary.agedBetween(10, 19);

        CohortDefinition activeChildrenInCare = df.getPatientsInAll(activeInCare, below9Years);
        CohortDefinition activeAdolescentsInCare = df.getPatientsInAll(activeInCare, between10to19Years);
        CohortDefinition activeChildrenAndAdolescents = df.getPatientsInAny(activeChildrenInCare, activeAdolescentsInCare);

        CohortDefinition startedARTMoreThan6MonthsAgo = df.getPatientsInAll(activeChildrenAndAdolescents, df.getEncountersOccuringMonthsBeforeStartDate(hivMetadata.getARTSummaryPageEncounterType(), 6));
        CohortDefinition adolscentsDueForViralLoad = df.getPatientsInAll(activeAdolescentsInCare,
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromEndDate("13m"));

        CohortDefinition childDueForViralLoad = df.getPatientsInAll(activeChildrenInCare,
                hivCohortDefinitionLibrary.getPatientsWhoseLastViralLoadWasMonthsAgoFromEndDate("7m"));
        CohortDefinition startedARTMoreThna6MonthsAgoWithActiveViralLoad = df.getPatientsNotIn(startedARTMoreThan6MonthsAgo, df.getPatientsInAny(adolscentsDueForViralLoad, childDueForViralLoad));


        CohortDefinition onFirstLineRegimen = df.getPatientsInAll(activeChildrenAndAdolescents, df.getWorkFlowStateCohortDefinition(hivMetadata.getFirstLineRegimenState()));
        CohortDefinition onSecondLineRegimen = df.getPatientsInAll(activeChildrenAndAdolescents, df.getWorkFlowStateCohortDefinition(hivMetadata.getSecondLineRegimenState()));

        CohortDefinition suppressed = df.getPatientsInAll(activeChildrenAndAdolescents, df.getPatientsWithNumericObsByEndDate(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.LESS_EQUAL,1000.0, BaseObsCohortDefinition.TimeModifier.LAST));
        CohortDefinition suppressedAdolescents = df.getPatientsInAll(onFirstLineRegimen, suppressed);
        CohortDefinition unSuppressed = df.getPatientsInAll(activeChildrenAndAdolescents, df.getPatientsWithNumericObsByEndDate(hivMetadata.getViralLoadCopies(),hivMetadata.getARTEncounterPageEncounterType(),RangeComparator.GREATER_THAN,1000.0, BaseObsCohortDefinition.TimeModifier.LAST));
        CohortDefinition OnFirstLineAndSuppressed = df.getPatientsInAll(onFirstLineRegimen, suppressed);
        CohortDefinition switchedFrom1stTo2nd= df.getPatientsInAll(onFirstLineRegimen, unSuppressed, addStartDateAndEndDateParameters(Cohorts.getPatientsWhoSwitchedFromFirstLineToSecondLineAsOnARTSummaryDuringPeriod()));
        CohortDefinition switchedFrom2ndTo3rd= df.getPatientsInAll(onSecondLineRegimen, unSuppressed, addStartDateAndEndDateParameters(Cohorts.getPatientsWhoSwitchedFromSecondLineToThirdLineAsOnARTSummaryDuringPeriod()));
        CohortDefinition switchedRegimens = df.getPatientsInAny(switchedFrom1stTo2nd, switchedFrom2ndTo3rd);

        CohortDefinition suppressedOnFirstLineOnDTGAndLPVRegimen = df.getPatientsInAll(suppressed, onFirstLineRegimen,
                df.getPatientsWithCodedObsByEndDate(hivMetadata.getCurrentRegimen(), hivMetadata.getARTEncounterPageEncounterType(), hivMetadata.getDTGAndLPVRegimens(), BaseObsCohortDefinition.TimeModifier.LAST));
        CohortDefinition unSuppressedAndOnIAC = df.getPatientsInAll(unSuppressed, df.getAnyEncounterOfTypesBetweenDates(hivMetadata.getIACEncounters()));

        CohortDefinition noClinicalEncounterInPreviousMonth = df.getPatientsInAll( activeChildrenAndAdolescents, hivCohortDefinitionLibrary.getPatientsTxLostToFollowupByDays("30"));
        CohortDefinition returnedToCareInMonth = df.getPatientsInAll( activeChildrenAndAdolescents,
                hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("30"),
                hivCohortDefinitionLibrary.getPatientsWithNoClinicalContactsForAbove28DaysByBeginningOfPeriod());

        CohortDefinition testedUsingFirstDNAPCR = eidCohortDefinitionLibrary.getEIDPatientsTestedUsingFirstDNAPCR();
        CohortDefinition firstDNAPCRWhoseResultsGivenToCareGiver = eidCohortDefinitionLibrary
                .getEIDPatientsTestedUsingFirstDNAPCRWhoseResultsGivenToCareGiver();
        CohortDefinition givenNVPAtBirth = eidCohortDefinitionLibrary.getEIDPatientsGivenNVP();
        CohortDefinition initiatedOnCPT = eidCohortDefinitionLibrary.getEIDPatientsInitiatedOnCPT();
        CohortDefinition heiAged9Months = eidCohortDefinitionLibrary.getInfantsAged9Months();
        CohortDefinition heiAged9MonthsWith2ndDNAPCR = df.getPatientsInAll(heiAged9Months, eidCohortDefinitionLibrary.getEIDInfantsWithSecondDNAPCR());
        CohortDefinition heiAged18Months = eidCohortDefinitionLibrary.getInfantsAged18Months();
        CohortDefinition heiAged18MonthsWithFinalOutcome = df.getPatientsInAll(heiAged9Months, eidCohortDefinitionLibrary.getEIDPatientsWithFinalOutcome());
        CohortDefinition moreThan2yearsHIVPositive = df.getPatientsInAll(eidCohortDefinitionLibrary.getEIDPatientsFinallyPositive(), eidCohortDefinitionLibrary.getInfants2YearsAndOlder());
        CohortDefinition eddInPeriod = df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(hivMetadata.getConcept("dcc033e5-30ab-102d-86b0-7a5022ba4115"),
                hivMetadata.getARTEncounterPageEncounterType(), BaseObsCohortDefinition.TimeModifier.LAST);

        addIndicator(dsd, "a1", "Active Children in Care", activeChildrenInCare);
        addIndicator(dsd, "a2", "Active Adolescents in Care", activeAdolescentsInCare);

        addIndicator(dsd, "b1", "Children and Adolescents in care", activeChildrenAndAdolescents);
        addIndicator(dsd, "b2", "In care for more than 6 months", startedARTMoreThan6MonthsAgo);
        addIndicator(dsd, "b3", "In care for more than 6 months with Active VL", startedARTMoreThna6MonthsAgoWithActiveViralLoad);
        addIndicator(dsd, "b4", "On First Line and Suppressed", OnFirstLineAndSuppressed);
        addIndicator(dsd, "b5", "On 1st line, Suppressed and on DTG or LPV/r regimen", suppressedOnFirstLineOnDTGAndLPVRegimen);
        addIndicator(dsd, "b6", "Unsuppressed and Eligible for Switch", OnFirstLineAndSuppressed);
        addIndicator(dsd, "b7", "Switched", switchedRegimens);
        addIndicator(dsd, "b8", "Suppressed irrespective of regimen line", suppressedAdolescents);
        // TODO: add the check for 3 IAC sessions - currently only those on IAC are considered
        addIndicator(dsd, "b9", "Suppressed and has completed 3IAC sessions", unSuppressedAndOnIAC);
        addIndicator(dsd, "b11", "No clinical contact or drug pickup in previous month", noClinicalEncounterInPreviousMonth);
        addIndicator(dsd, "b12", "Returned to care in month", returnedToCareInMonth);

        addIndicator(dsd, "c1", "Got first DNA PCR in review month", testedUsingFirstDNAPCR);
        addIndicator(dsd, "c3", "First DNA PCR results given to care giver in review month", firstDNAPCRWhoseResultsGivenToCareGiver);
        addIndicator(dsd, "c4", "Received NVP at birth", givenNVPAtBirth);
        addIndicator(dsd, "c5", "Received CPT by 2 months", initiatedOnCPT);
        addIndicator(dsd, "c6", "HEI Aged 9 months", heiAged9Months);
        addIndicator(dsd, "c7", "HEI Aged 9 months who got 2nd DNA PCR", heiAged9MonthsWith2ndDNAPCR);
        addIndicator(dsd, "c8", "HEI Aged 18 months", heiAged18Months);
        addIndicator(dsd, "c9", "HEI Aged 18 months with final outcome", heiAged18MonthsWithFinalOutcome);
        addIndicator(dsd, "c10", "HEI >2yrs HIV Positive", moreThan2yearsHIVPositive);

        addIndicator(dsd, "d1", "Mothers to deliver in month", eddInPeriod);
        addIndicator(dsd, "d2", "HIV Positive Mothers that have a live baby delivered", moh105CohortLibrary.hivPositiveMothers());
        addIndicator(dsd, "d3", "HEI born in reporting month", eidCohortDefinitionLibrary.getEnrolledInCareDuringPeriod());

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

    public CohortDefinition addStartDateAndEndDateParameters(CohortDefinition cohortDefinition){
        return   df.convert(cohortDefinition, ObjectUtil.toMap("startDate=startDate,endDate=endDate"));
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }
}
