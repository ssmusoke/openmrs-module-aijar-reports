package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.common.BooleanOperator;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
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
    
    public CohortDefinition getExposedInfants() {
        EncounterCohortDefinition eid = new EncounterCohortDefinition();
        eid.setEncounterTypeList(Arrays.asList(CoreUtils.getEncounterType(Metadata.EncounterType.EID_SUMMARY_PAGE)));
        eid.addParameter(new Parameter("onOrBefore", "On or Before", Date.class));
        eid.addParameter(new Parameter("onOrAfter", "On or After", Date.class));
        return eid;
    }
    
    /**
     * Exposed infants are due for first PCR when they are 6 weeks old
     * @return
     */
    public CohortDefinition getExposedInfantsDueForFirstPCR() {
        CompositionCohortDefinition infantsOfAge = new CompositionCohortDefinition();
        infantsOfAge.addParameter(new Parameter("endDate", "End Date", Date.class));
        infantsOfAge.addParameter(new Parameter("startDate", "Start Date", Date.class));
        // get all exposed infants who are 6 weeks and above
        infantsOfAge.initializeFromQueries(BooleanOperator.AND, getExposedInfants(), getInfants6weeksAndOlder());
    
        // infants of age less those who have already had their first DNA PCF
        CompositionCohortDefinition dueForFirstPCR = new CompositionCohortDefinition();
        dueForFirstPCR.initializeFromQueries(BooleanOperator.NOT, infantsOfAge, getEIDPatientsTestedUsingFirstDNAPCR());
        
        return dueForFirstPCR;
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
