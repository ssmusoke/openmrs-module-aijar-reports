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

    @DocumentedDefinition(value = "artstartdate", name = "ART Start Date")
    public PatientDataDefinition getARTStartDate() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getARTStartDate()), df.getObsValueDatetimeConverter());
    }

    @DocumentedDefinition(value = "lastregimenpickupdate", name = "Last Regimen Pickup Date")
    public PatientDataDefinition getLastRegimenPickupDate() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getCurrentRegimen()), df.getObsDatetimeConverter());
    }

    @DocumentedDefinition(value = "currentregimen", name = "Current Regimen")
    public PatientDataDefinition getCurrentRegimen() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getCurrentRegimen()), df.getObsValueCodedConverter());
    }

    @DocumentedDefinition(value = "currentregimendate", name = "Current Regimen Date")
    public PatientDataDefinition getCurrentRegimenDate() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getCurrentRegimen()), df.getObsDatetimeConverter());
    }

    @DocumentedDefinition(value = "arvduration", name = "ARV Duration")
    public PatientDataDefinition getARVDuration() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getARVDuration()), df.getObsValueNumericConverter());
    }

    @DocumentedDefinition(value = "expectedreturndate", name = "Expected Return Date")
    public PatientDataDefinition getExpectedReturnDate() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getExpectedReturnDate()), df.getObsValueDatetimeConverter());
    }

    @DocumentedDefinition(value = "startregimen", name = "Start Regimen")
    public PatientDataDefinition getStartRegimen() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getStartRegimen()), df.getObsValueCodedConverter());
    }

    @DocumentedDefinition(value = "startregimendate", name = "Start Regimen Date")
    public PatientDataDefinition getStartRegimenDate() {
        return convert(df.getMostRecentObsByEndDate(hivMetadata.getStartRegimenDate()), df.getObsValueDatetimeConverter());
    }

    protected PatientDataDefinition getFirstArtInitialEncounterByEndDate(DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTSummaryPageEncounterType().get(0);
        return df.getFirstEncounterOfTypeByEndDate(arvInitial, converter);
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
