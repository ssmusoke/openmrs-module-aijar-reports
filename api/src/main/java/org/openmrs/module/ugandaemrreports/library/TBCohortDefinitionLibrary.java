package org.openmrs.module.ugandaemrreports.library;

import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.ProgramEnrollmentCohortDefinition;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.metadata.TBMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;

import static org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary.getConcept;

/**
 */
@Component
public class TBCohortDefinitionLibrary extends BaseDefinitionLibrary<CohortDefinition> {

    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private CommonDimensionLibrary commonDimensionLibrary;

    @Autowired
    private TBMetadata tbMetadata;

    @Override
    public Class<? super CohortDefinition> getDefinitionType() {
        return CohortDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "ugemr.cohort.tb.";
    }


    public CohortDefinition getEverEnrolledInTBCare() {
        return df.getPatientsWithIdentifierOfType(tbMetadata.getTBIdentifier());
    }

    public CohortDefinition getNewAndRelapsedPatientsDuringPeriod() {
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getNewPatientType(), tbMetadata.getRelapsedPatientType()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getEnrolledOnDSTBDuringPeriod(){
        ProgramEnrollmentCohortDefinition cd = new ProgramEnrollmentCohortDefinition();
        cd.setName("Enrolled in program During Period");
        cd.addParameter(new Parameter("onOrBefore", "Enrolled on or before", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "Enrolled on or after", Date.class));
        cd.setPrograms(Arrays.asList(commonDimensionLibrary.getProgramByUuid("de5d54ae-c304-11e8-9ad0-529269fb1459")));
        return df.convert(cd, ObjectUtil.toMap("onOrAfter=endDate,onOrBefore=endDate"));

    }

    public CohortDefinition getEnrolledOnDSTBDuringPeriod(String olderThan){
        ProgramEnrollmentCohortDefinition cd = new ProgramEnrollmentCohortDefinition();
        cd.setName("Enrolled in program During Period");
        cd.addParameter(new Parameter("onOrBefore", "Enrolled on or before", Date.class));
        cd.addParameter(new Parameter("onOrAfter", "Enrolled on or after", Date.class));
        cd.setPrograms(Arrays.asList(commonDimensionLibrary.getProgramByUuid("de5d54ae-c304-11e8-9ad0-529269fb1459")));
        return df.convert(cd, ObjectUtil.toMap("onOrAfter=endDate-"+olderThan+",onOrBefore=endDate-"+olderThan));

    }

    public CohortDefinition getNewPatientsDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getNewPatientType()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getRelapsedPatientsDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getRelapsedPatientType()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTreatedAfterLTFPPatientsDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getTreatmentAfterLTFPPatientType()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTreatedAfterFailurePatientsDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getTreatmentAfterFailurePatientType()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getTreatmentHistoryUnknownPatientsDuringPeriod(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTypeOfPatient(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getTreatmentHistoryUnknownPatientType()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsStartedOnTreatmentDuringperiod(){
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(tbMetadata.getDateStartedTBTreatment(),tbMetadata.getTBEnrollmentEncounterType(), BaseObsCohortDefinition.TimeModifier.LAST);
    }

    public CohortDefinition getPatientsWhoseHIVStatusIsNewlyDocumented(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getHIVStatusCategory(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getNewlyPositiveHIVStatus(),tbMetadata.getNegativeHIVStatus(),tbMetadata.getUnknownHIVStatus()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoseHIVStatusIsKnownPositive(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getHIVStatusCategory(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getKnownPositiveHIVStatus()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWhoseHIVStatusIsNewlyPositive(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getHIVStatusCategory(),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(tbMetadata.getNewlyPositiveHIVStatus()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsStartedOnARTOnTBEnrollment(){
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(getConcept("ab505422-26d9-41f1-a079-c3d222000440"),tbMetadata.getTBEnrollmentEncounterType(), BaseObsCohortDefinition.TimeModifier.LAST);
    }

    public CohortDefinition getPatientsOnCPTOnTBEnrollment(){
        return df.getPatientsWhoseObsValueDateIsBetweenStartDateAndEndDate(getConcept("481c5fdb-4719-4be3-84c0-a64172a426c7"),tbMetadata.getTBEnrollmentEncounterType(), BaseObsCohortDefinition.TimeModifier.LAST);
    }

    public CohortDefinition getPatientsOnCPT(){
        return df.getPatientsWithCodedObsDuringPeriod(getConcept("bb77f9f0-9743-4c60-8e70-b20b5e800a50"),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("1065AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA")) ,BaseObsCohortDefinition.TimeModifier.LAST);
    }

    public CohortDefinition getPatientsWhoAreAlreadyOnART(){
        return df.getPatientsWithCodedObsDuringPeriod(getConcept("dca25616-30ab-102d-86b0-7a5022ba4115"),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("dca2817c-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsInitiatedOnART(){
        return df.getPatientsWithCodedObsDuringPeriod(getConcept("dca25616-30ab-102d-86b0-7a5022ba4115"),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("dca2670e-30ab-102d-86b0-7a5022ba4115")), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsOnFacilityDOTSTreatmentModel(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTreatmentModel(),null,Arrays.asList(tbMetadata.getFacilityDOTsTreatmentModel()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsOnCommunityDOTSTreatmentModel(){
        return df.getPatientsWithCodedObsDuringPeriod(tbMetadata.getTreatmentModel(),null,Arrays.asList(tbMetadata.getDigitalCommunityDOTsTreatmentModel(),tbMetadata.getNonDigitalCommunityDOTsTreatmentModel()), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsWithTreatmentSupporters(){
        return df.getPatientsWithCodedObsDuringPeriod(getConcept("805a9d40-8922-4fb0-8208-7c0fdf57936a"),tbMetadata.getTBEnrollmentEncounterType(), BaseObsCohortDefinition.TimeModifier.ANY);
    }

    public CohortDefinition getPatientsReferredFromComunity(){
        return df.getPatientsWithCodedObsDuringPeriod(getConcept("67ea4375-0f4f-4e67-b8b0-403942753a4d"),tbMetadata.getTBEnrollmentEncounterType(),Arrays.asList(getConcept("1ca0db4a-c5c6-48ed-9e17-84905f4487eb")), BaseObsCohortDefinition.TimeModifier.ANY);
    }











}
