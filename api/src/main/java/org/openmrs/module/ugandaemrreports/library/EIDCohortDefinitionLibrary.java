package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ObsInEncounterCohortDefinition;
import org.openmrs.module.reporting.common.BooleanOperator;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.Parameterizable;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.EncounterCohortDefinition;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.openmrs.module.ugandaemrreports.reporting.utils.CoreUtils;
import org.openmrs.module.ugandaemrreports.reporting.utils.ReportUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

/**
 * Defines all the Cohort Definitions instances from the EID clinic
 */
@Component
public class EIDCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {
    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.cohort.eid.";
    }

    public CohortDefinition getEnrolledInCareDuringPeriod() {
        EncounterCohortDefinition q = new EncounterCohortDefinition();
        q.setEncounterTypeList(hivMetadata.getEIDSummaryPageEncounterType());
        q.addParameter(df.getStartDateParameter());
        q.addParameter(df.getEndDateParameter());
        return q;
    }

    public CohortDefinition getPatientsWithObsValueAtArtInitiationAtLocationByEnd(Concept question, Concept... values) {
        CodedObsCohortDefinition cd = new CodedObsCohortDefinition();
        cd.setEncounterTypeList(hivMetadata.getEIDSummaryPageEncounterType());
        cd.setTimeModifier(BaseObsCohortDefinition.TimeModifier.FIRST);
        cd.setQuestion(question);
        cd.setOperator(SetComparator.IN);
        cd.setValueList(Arrays.asList(values));
        return df.convert(cd, null);
    }

    public CohortDefinition getPatients2to14YearsOldAtPreArtStateStartAtLocationByEndDate() {
        return df.getPatientsWhoStartedStateWhenInAgeRangeAtLocationByEndDate(0, Age.Unit.YEARS, 100, Age.Unit.YEARS);
    }

    public CohortDefinition getAllEIDPatients() {
        return df.getAnyEncounterOfType(hivMetadata.getEIDSummaryPageEncounterType());
    }
    
    /**
     * Get all Exposed Infants
     * @return
     */
    public CohortDefinition getExposedInfants() {
        EncounterCohortDefinition eid = new EncounterCohortDefinition();
        eid.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_SUMMARY_PAGE)));
        eid.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        eid.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return eid;
    }
    
    /**
     * Exposed infants are due for first PCR when they are 6 weeks old excluding those for whom the PCR test has already been done
     * @return
     */
    public CohortDefinition getExposedInfantsDueForFirstPCR() {
        CompositionCohortDefinition infantsDueForFirstPCR = new CompositionCohortDefinition();
        infantsDueForFirstPCR.setName("Infants Due for 1st DNA PCR at 6 weeks");
        infantsDueForFirstPCR.addParameter(new Parameter("endDate", "End Date", Date.class));
        infantsDueForFirstPCR.addParameter(new Parameter("startDate", "Start Date", Date.class));
        
        // all exposed infants
        infantsDueForFirstPCR.addSearch("allExposedInfants", ReportUtils.map(getExposedInfants(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        // get all exposed infants who are 6 weeks and older
        infantsDueForFirstPCR.addSearch("exposedInfantsOlderThan6Weeks", ReportUtils.map(getInfants6weeksAndOlder(), "effectiveDate=${endDate}"));
    
        // infants who have already had their first DNA PCR done
        infantsDueForFirstPCR.addSearch("exposedInfantsWith1stPCRDone", ReportUtils.map(getEIDInfantsWithFirstDNAPCR(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        
        infantsDueForFirstPCR.setCompositionString("(allExposedInfants AND exposedInfantsOlderThan6Weeks) NOT exposedInfantsWith1stPCRDone");
        return infantsDueForFirstPCR;
    }
    
    /**
     * Exposed infants are due for second PCR:
     * - when they are 13 months old
     * - 6 weeks after cessation of breastfeeding
     * - Must have done a first DNA PCR
     *
     * excludes those who have already had a second DNA PCR done
     * @return
     */
    public CohortDefinition getExposedInfantsDueForSecondPCR() {
        CompositionCohortDefinition infantsDueForSecond = new CompositionCohortDefinition();
        infantsDueForSecond.setName("Infants Due for 2nd DNA PCR at 13 weeks and cessation of breast feeding");
        infantsDueForSecond.addParameter(new Parameter("endDate", "End Date", Date.class));
        infantsDueForSecond.addParameter(new Parameter("startDate", "Start Date", Date.class));
        
        // Exposed infants with first DNA PCR done
        infantsDueForSecond.addSearch("exposedInfantsWith1stPCRDone", ReportUtils.map(getEIDInfantsWithFirstDNAPCR(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        
        // get all exposed infants who are 13 months and older
        infantsDueForSecond.addSearch("exposedInfantsOlderThan13Months", ReportUtils.map(getInfants13monthsAndOlder(), "effectiveDate=${endDate}"));
        
        // infants who have already had their second DNA PCR done
        infantsDueForSecond.addSearch("exposedInfantsWith2ndPCRDone", ReportUtils.map(getEIDInfantsWithSecondDNAPCR(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        
        // infants who ceased breast feeding at least 6 weeks ago
        infantsDueForSecond.addSearch("exposedInfantsCeasedBreastFeeding", ReportUtils.map(getExposedInfantsWhoHaveCeasedBreastFeeding(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
        
        infantsDueForSecond.setCompositionString("(exposedInfantsWith1stPCRDone AND (exposedInfantsOlderThan13Months OR exposedInfantsCeasedBreastFeeding)) NOT exposedInfantsWith2ndPCRDone");
        return infantsDueForSecond;
    }
    
    /**
     * Exposed infants due for a rapid test must be 18 months and older
     * @return
     */
    public CohortDefinition getExposedInfantsDueForRapidTest() {
        CompositionCohortDefinition infantsDueForRapidTest = new CompositionCohortDefinition();
        infantsDueForRapidTest.setName("Infants Due for Rapid Test 18 months");
        infantsDueForRapidTest.addParameter(new Parameter("endDate", "End Date", Date.class));
        infantsDueForRapidTest.addParameter(new Parameter("startDate", "Start Date", Date.class));
    
        // All Exposed infants
        infantsDueForRapidTest.addSearch("allExposedInfants", ReportUtils.map(getExposedInfants(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    
        // get all exposed infants who are 13 months and older
        infantsDueForRapidTest.addSearch("exposedInfantsOlderThan18Months", ReportUtils.map(getInfants18monthsAndOlder(), "effectiveDate=${endDate}"));
        
        // infants who have had a rapid test
        infantsDueForRapidTest.addSearch("exposedInfantsWithRapidTest", ReportUtils.map(getEIDInfantsWithRapidTest(), "onOrAfter=${startDate},onOrBefore=${endDate}"));
    
        infantsDueForRapidTest.setCompositionString("((allExposedInfants AND exposedInfantsOlderThan18Months) NOT exposedInfantsWithRapidTest");
        return infantsDueForRapidTest;
    }
    
    
    /**
     * Get all EID Infants with first DNA PCR, this method is unlike getEIDPatientsTestedUsingFirstDNAPCR which only returns patients with DNA PCR in a specific month
     * @return
     */
    public CohortDefinition getEIDInfantsWithFirstDNAPCR() {
        DateObsCohortDefinition dateObsCohortDefinition = new DateObsCohortDefinition();
        dateObsCohortDefinition.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_SUMMARY_PAGE)));
        dateObsCohortDefinition.setQuestion(hivMetadata.getFirstPCRTestDate());
        dateObsCohortDefinition.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        dateObsCohortDefinition.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        dateObsCohortDefinition.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        return dateObsCohortDefinition;
    }
    
    /**
     * Get all Exposed Infants with who have ceased breast feeding
     * @return
     */
    public CohortDefinition getExposedInfantsWhoHaveCeasedBreastFeeding() {
        CodedObsCohortDefinition infantsCeasedBreastFeeding = new CodedObsCohortDefinition();
        infantsCeasedBreastFeeding.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_ENCOUNTER_PAGE)));
        infantsCeasedBreastFeeding.setQuestion(hivMetadata.getBreastFeedingStatus());
        infantsCeasedBreastFeeding.setValueList(Arrays.asList(hivMetadata.getBreastFeedingStatusNoLongerBreastFeeding()));
        infantsCeasedBreastFeeding.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        infantsCeasedBreastFeeding.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        infantsCeasedBreastFeeding.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        infantsCeasedBreastFeeding.setOperator(SetComparator.IN);
        return infantsCeasedBreastFeeding;
    }
    
    /**
     * Get all EID Infants due for appointment
     * @return
     */
    public CohortDefinition getEIDInfantsDueForAppointment() {
        DateObsCohortDefinition dateObsCohortDefinition = new DateObsCohortDefinition();
        dateObsCohortDefinition.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_ENCOUNTER_PAGE)));
        dateObsCohortDefinition.setQuestion(hivMetadata.getReturnVisitDate());
        dateObsCohortDefinition.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        dateObsCohortDefinition.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        dateObsCohortDefinition.setTimeModifier(BaseObsCohortDefinition.TimeModifier.LAST);
        return dateObsCohortDefinition;
    }
    
    /**
     * Get all EID Infants with second DNA PCR, this method is unlike getEIDPatientsTestedUsingFirstDNAPCR which only returns patients with DNA PCR in a specific month
     * @return
     */
    public CohortDefinition getEIDInfantsWithSecondDNAPCR() {
        DateObsCohortDefinition dateObsCohortDefinition = new DateObsCohortDefinition();
        dateObsCohortDefinition.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_SUMMARY_PAGE)));
        dateObsCohortDefinition.setQuestion(hivMetadata.getSecondPCRTestDate());
        dateObsCohortDefinition.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        dateObsCohortDefinition.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        dateObsCohortDefinition.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        return dateObsCohortDefinition;
    }
    
    /**
     * Get all EID Infants with rapid test done
     * @return
     */
    public CohortDefinition getEIDInfantsWithRapidTest() {
        DateObsCohortDefinition dateObsCohortDefinition = new DateObsCohortDefinition();
        dateObsCohortDefinition.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_SUMMARY_PAGE)));
        dateObsCohortDefinition.setQuestion(hivMetadata.get18MonthsRapidPCRTestDate());
        dateObsCohortDefinition.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        dateObsCohortDefinition.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        dateObsCohortDefinition.setTimeModifier(BaseObsCohortDefinition.TimeModifier.ANY);
        return dateObsCohortDefinition;
    }
    
    
    /**
     * Infants who are 6 weeks and above
     * @return
     */
    public CohortDefinition getInfants6weeksAndOlder() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("Infants at least 6 weeks old");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(6);
        cd.setMinAgeUnit(DurationUnit.WEEKS);
        return cd;
    }
    /**
     * Infants who are 13 months and above
     * @return
     */
    public CohortDefinition getInfants13monthsAndOlder() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("Infants at least 13 months old");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(13);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        return cd;
    }

    /**
     * Infants who are 13 months and above
     * @return
     */
    public CohortDefinition getInfantsAged9Months() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("Infants aged 9 months");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(9);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        cd.setMaxAge(10);
        cd.setMaxAgeUnit(DurationUnit.MONTHS);
        return cd;
    }
    /**
     * Infants who are 13 months and above
     * @return
     */
    public CohortDefinition getInfantsAged18Months() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("Infants aged 18 months");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(18);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        cd.setMaxAge(19);
        cd.setMaxAgeUnit(DurationUnit.MONTHS);
        return cd;
    }
    
    /**
     * Infants who are 18 months and above
     * @return
     */
    public CohortDefinition getInfants18monthsAndOlder() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("Infants at least 18 months old");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(18);
        cd.setMinAgeUnit(DurationUnit.MONTHS);
        return cd;
    }
    public CohortDefinition getInfants2YearsAndOlder() {
        AgeCohortDefinition cd = new AgeCohortDefinition();
        cd.setName("Infants at least 2 years old");
        cd.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
        cd.setMinAge(2);
        cd.setMinAgeUnit(DurationUnit.YEARS);
        return cd;
    }
    public CohortDefinition getEIDPatientsGivenNVP() {
        return df.getObsWithEncounters(hivMetadata.getDateOfNVP(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsInitiatedOnCPT() {
        return df.getObsWithEncounters(hivMetadata.getDateOfCPT(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsTestedUsingFirstDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getFirstPCRTestDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsTestedUsingSecondDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getSecondPCRTestDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }
    
    public CohortDefinition getInfantsWhoCeasedBreastFeeding() {
        return df.getObsWithEncounters(hivMetadata.getBreastFeedingStatus(), hivMetadata.getEIDEncounterPageEncounterType(), Arrays.asList(hivMetadata.getBreastFeedingStatusNoLongerBreastFeeding()));
    }

    public CohortDefinition getEIDPatientsTestedUsingABTest() {
        return df.getObsWithEncounters(hivMetadata.get18MonthsRapidPCRTestDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsTestedUsingFirstDNAPCRWhoseResultsGivenToCareGiver() {
        return df.getObsWithEncounters(hivMetadata.getFirstPCRTestResultGivenToCareProviderDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsTestedUsingSecondDNAPCRWhoseResultsGivenToCareGiver() {
        return df.getObsWithEncounters(hivMetadata.getSecondPCRTestResultGivenToCareProviderDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsTestedUsingABTestWhoseResultsGivenToCareGiver() {
        return df.getObsWithEncounters(hivMetadata.get18MonthsRapidPCRTestResultGivenToCareProviderDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsTestedPositiveUsingFirstDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getFirstPCRTestResults(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    public CohortDefinition getEIDPatientsTestedPositiveUsingSecondDNAPCR() {
        return df.getObsWithEncounters(hivMetadata.getSecondPCRTestResults(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    public CohortDefinition getEIDPatientsTestedPositiveUsingABTest() {
        return df.getObsWithEncounters(hivMetadata.get18MonthsRapidPCRTestResults(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    public CohortDefinition getEIDPatientsWithFinalOutcome() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getEIDPatientsWhoDied() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getFinalOutcomeDied()));
    }

    public CohortDefinition getEIDPatientsFinallyPositive() {
        return df.getObsWithEncounters(hivMetadata.getFinalStatus(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getPositiveResult()));
    }

    public CohortDefinition getEIDPatientsFinallyNegative() {
        return df.getObsWithEncounters(hivMetadata.getFinalStatus(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getNegativeResult()));
    }

    public CohortDefinition getEIDPatientsFinallyTransferredOut() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getTransferredOut()));
    }

    public CohortDefinition getEIDTransferIns() {
        return df.getObsWithEncounters(hivMetadata.getTransferIn(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getYes()));
    }

    public CohortDefinition getEIDLostToFollowup() {
        return df.getObsWithEncounters(hivMetadata.getFinalOutcome(), hivMetadata.getEIDSummaryPageEncounterType(), Arrays.asList(hivMetadata.getFinalOutcomeLost()));
    }

    public CohortDefinition getEIDOnART() {
        return df.getObsWithEncounters(hivMetadata.getArtStartDate(), hivMetadata.getEIDSummaryPageEncounterType());
    }

    public CohortDefinition getPatientsWithAnEIDNumber() {
        return df.getPatientsWithIdentifierOfType(hivMetadata.getPatientsWithEIDIdentifier());
    }
    
   
}
