package org.openmrs.module.aijarreports.library;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.aijar.metadata.core.PatientIdentifierTypes;
import org.openmrs.module.aijarreports.definition.data.definition.StatusAtEnrollmentPatientDatasetDefinition;
import org.openmrs.module.aijarreports.definition.data.definition.WhyEligibleForARTPatientDatasetDefinition;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.MapConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PreferredIdentifierDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Attributes of a person not defined within the core OpenMRS definition
 */
@Component
public class HIVPatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {
    @Autowired
    private DataFactory df;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public Class<? super PatientDataDefinition> getDefinitionType() {
        return PatientDataDefinition.class;
    }

    @Override
    public String getKeyPrefix() {
        return "aijar.patientdata.";
    }

    @DocumentedDefinition(value = "clinicnumber", name = "Clinic Number")
    public PatientDataDefinition getClinicNumber() {
        PreferredIdentifierDataDefinition def = new PreferredIdentifierDataDefinition();
        def.setIdentifierType(MetadataUtils.existing(PatientIdentifierType.class, PatientIdentifierTypes.HIV_CARE_NUMBER.uuid()));
        return convert(def, new PropertyConverter(PatientIdentifier.class, "identifier"));
    }

    @DocumentedDefinition(value = "enrollmentdate", name = "Enrollment Date")
    public PatientDataDefinition getEnrollmentDate() {
        return getFirstArtInitialEncounterByEndDate(df.getEncounterDatetimeConverter());
    }

    @DocumentedDefinition(value = "lastvisitdate", name = "Last Visit Date")
    public PatientDataDefinition getLastVisitDate() {
        return getLastARTVisitEncounterByEndDate(df.getEncounterDatetimeConverter());
    }

    @DocumentedDefinition(value = "artstartdate", name = "ART Start Date")
    public PatientDataDefinition getARTStartDate() {
        return df.getObsByEndDate(hivMetadata.getArtStartDate(), df.getObsValueDatetimeConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "lastregimenpickupdate", name = "Last Regimen Pickup Date")
    public PatientDataDefinition getLastRegimenPickupDate() {
        return df.getObsByEndDate(hivMetadata.getCurrentRegimen(), df.getObsDatetimeConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "currentregimen", name = "Current Regimen")
    public PatientDataDefinition getCurrentRegimen() {
        return df.getObsByEndDate(hivMetadata.getCurrentRegimen(), df.getObsValueCodedConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "currentregimendate", name = "Current Regimen Date")
    public PatientDataDefinition getCurrentRegimenDate() {
        return df.getObsByEndDate(hivMetadata.getCurrentRegimen(), df.getObsDatetimeConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "arvduration", name = "ARV Duration")
    public PatientDataDefinition getARVDuration() {
        return df.getObsByEndDate(hivMetadata.getARVDuration(), df.getObsValueNumericConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "expectedreturndate", name = "Expected Return Date")
    public PatientDataDefinition getExpectedReturnDate() {
        return df.getObsByEndDate(hivMetadata.getExpectedReturnDate(), df.getObsValueDatetimeConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "startregimen", name = "Start Regimen")
    public PatientDataDefinition getStartRegimen() {
        return df.getObsByEndDate(hivMetadata.getArtStartRegimen(), df.getObsValueCodedConverter(), TimeQualifier.LAST);
    }

    @DocumentedDefinition(value = "startregimendate", name = "Start Regimen Date")
    public PatientDataDefinition getStartRegimenDate() {
        return df.getObsByEndDate(hivMetadata.getArtStartDate(), df.getObsValueDatetimeConverter(), TimeQualifier.LAST);
    }

    protected PatientDataDefinition getFirstArtInitialEncounterByEndDate(DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTSummaryPageEncounterType().get(0);
        return df.getFirstEncounterOfTypeByEndDate(arvInitial, converter);
    }

    protected PatientDataDefinition getLastARTVisitEncounterByEndDate(DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTEncounterPageEncounterType().get(0);
        return df.getLastEncounterOfTypeByEndDate(arvInitial, converter);
    }

    @DocumentedDefinition("allArtNumbers")
    public PatientDataDefinition getAllArtNumbers() {
        PatientIdentifierType pit = hivMetadata.getHIVIdentifier();
        return df.getAllIdentifiersOfType(pit, df.getIdentifierCollectionConverter());
    }

    public PatientDataDefinition getEntryPoint() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getEntryPoint(), df.getObsValueCodedConverter());
    }

    public PatientDataDefinition getFirstEID() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getPCRAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstLactating() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getLactatingAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstTB() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getTBAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstPregnant() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getPregnantAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstTransferIn() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getTransferInAtEnrollment(), df.getObsValueCodedConverter());
    }

    public PatientDataDefinition getCPTStartDate() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getCPTDosage(), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getINHStartDate() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getINHDosage(), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getTBStartDate() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getTBStartDate(), df.getObsValueDatetimeConverter());
    }

    public PatientDataDefinition getTBStopDate() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getTBStopDate(), df.getObsValueDatetimeConverter());
    }


    public PatientDataDefinition getWHOStage1Date() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage1()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getWHOStage2Date() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage2()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getWHOStage3Date() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage3()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getWHOStage4Date() {
        return getEncounterPageObsValueDuringPeriod(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage4()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getARTEligibilityDate() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getDateEligibleForART(), df.getObsValueDatetimeConverter());
    }

    public PatientDataDefinition getARTEligibilityAndReadyDate() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getDateEligibleAndReadyForART(), df.getObsValueDatetimeConverter());
    }

    public PatientDataDefinition getARTEligibilityWHOStage() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getDateEligibilityWHOStage(), df.getObsValueCodedConverter());
    }

    public PatientDataDefinition getARTEligibilityCD4() {
        return getSummaryPageObsValueDuringPeriod(hivMetadata.getDateEligibilityWHOStage(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getStatusAtEnrollment() {
        StatusAtEnrollmentPatientDatasetDefinition def = new StatusAtEnrollmentPatientDatasetDefinition();
        MapConverter c = new MapConverter(": ", ", ", null, new ObjectFormatter());
        return df.convert(def, c);
    }

    public PatientDataDefinition getWhyEligibleForART() {
        WhyEligibleForARTPatientDatasetDefinition def = new WhyEligibleForARTPatientDatasetDefinition();
        MapConverter c = new MapConverter(": ", ", ", null, new ObjectFormatter());
        return df.convert(def, c);
    }

    @DocumentedDefinition(value = "cd4atenrollment", name = "CD4 at Enrollment")
    public PatientDataDefinition getCD4AtEnrollment() {
        return df.getObsByEndDate(hivMetadata.getCD4AtEnrollment(), df.getObsValueNumericConverter(), TimeQualifier.FIRST);
    }

    @DocumentedDefinition(value = "baselinecd4", name = "Baseline CD4")
    public PatientDataDefinition getBaselineCD4() {
        return df.getObsByEndDate(hivMetadata.getBaselineCD4(), df.getObsValueNumericConverter(), TimeQualifier.FIRST);
    }

    public PatientDataDefinition getBaselineCD4(String olderThan) {
        return df.getObsByEndDate(hivMetadata.getBaselineCD4(), df.getObsValueNumericConverter(), olderThan, TimeQualifier.FIRST);
    }

    public PatientDataDefinition getRecentCD4() {
        return df.getObsByEndDate(hivMetadata.getCD4(), df.getObsValueNumericConverter(), TimeQualifier.FIRST);
    }

    @DocumentedDefinition(value = "cd4At6months", name = "CD4 at 6 months")
    public PatientDataDefinition getCD4At6months() {
        return df.getObsByEndDate(hivMetadata.getBaselineCD4(), df.getObsValueNumericConverter(), TimeQualifier.LAST);
    }

    /*@DocumentedDefinition(value = "cd4At6monthsdate", name = "Date of CD4 at 6 months")
    public PatientDataDefinition getDateofCD4At6months() {
    }

    @DocumentedDefinition(value = "cd4At12months", name = "CD4 at 12 months")
    public PatientDataDefinition getCD4At12months() {
    }

    @DocumentedDefinition(value = "cd4At12monthsdate", name = "Date of CD4 at 12 months")
    public PatientDataDefinition getDateofCD4At12months() {
    }*/

    // ***** CONVENIENCE METHODS

    public PatientDataDefinition convert(PatientDataDefinition pdd, DataConverter converter) {
        return convert(pdd, null, converter);
    }

    public PatientDataDefinition convert(PatientDataDefinition pdd, Map<String, String> renamedParameters, DataConverter converter) {
        ConvertedPatientDataDefinition convertedDefinition = new ConvertedPatientDataDefinition();
        convertedDefinition.setDefinitionToConvert(ParameterizableUtil.copyAndMap(pdd, convertedDefinition, renamedParameters));
        if (converter != null) {
            convertedDefinition.setConverters(Arrays.asList(converter));
        }
        return convertedDefinition;
    }

    protected PatientDataDefinition getObsOnArtInitialEncounter(Concept question, DataConverter converter) {
        EncounterType artSummary = hivMetadata.getARTSummaryEncounter();
        return df.getFirstObsByEndDate(question, Arrays.asList(artSummary), converter);
    }

    protected PatientDataDefinition getSummaryPageObsValueDuringPeriod(Concept question, DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTSummaryEncounter();
        return df.getObsValueDuringPeriod(question, Arrays.asList(arvInitial), converter);
    }

    protected PatientDataDefinition getEncounterPageObsValueDuringPeriod(Concept question, DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTEncounterEncounterType();
        return df.getObsValueDuringPeriod(question, Arrays.asList(arvInitial), converter);
    }

    protected PatientDataDefinition getEncounterPageObsValueDuringPeriod(Concept question, List<Concept> answers, DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTEncounterEncounterType();
        return df.getObsValueDuringPeriod(question, Arrays.asList(arvInitial), answers, converter);
    }
}
