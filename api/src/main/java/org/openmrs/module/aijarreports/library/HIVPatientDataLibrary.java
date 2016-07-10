package org.openmrs.module.aijarreports.library;

import org.openmrs.Concept;
import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.aijar.metadata.core.PatientIdentifierTypes;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.definition.data.converter.*;
import org.openmrs.module.aijarreports.definition.data.definition.FUStatusPatientDataDefinition;
import org.openmrs.module.aijarreports.definition.data.definition.StatusAtEnrollmentPatientDatasetDefinition;
import org.openmrs.module.aijarreports.definition.data.definition.WhyEligibleForARTPatientDatasetDefinition;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.ObjectUtil;
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
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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
        return getSummaryPageObsValue(hivMetadata.getEntryPoint(), df.getObsValueCodedConverter());
    }

    public PatientDataDefinition getFirstEID() {
        return getSummaryPageObsValue(hivMetadata.getPCRAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstLactating() {
        return getSummaryPageObsValue(hivMetadata.getLactatingAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstTB() {
        return getSummaryPageObsValue(hivMetadata.getTBAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstPregnant() {
        return getSummaryPageObsValue(hivMetadata.getPregnantAtEnrollment(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getFirstTransferIn() {
        return getSummaryPageObsValue(hivMetadata.getTransferInAtEnrollment(), new TIStatusConverter());
    }

    public PatientDataDefinition getCPTStartDate() {
        return getEncounterPageObsValue(hivMetadata.getCPTDosage(), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getCPTStatusDuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getCPTDosage(), num, new YesConverter());
    }

    public PatientDataDefinition getINHStatusDuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getINHDosage(), num, new YesConverter());
    }

    public PatientDataDefinition getTBStatusDuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getTBStatus(), num, new TbStatusConverter());
    }

    public PatientDataDefinition getDeadStatusDuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getDead(), num, new DeadStatusConverter());
    }

    public PatientDataDefinition getTOStatusDuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getTransferredOut(), num, new TOStatusConverter());
    }

    public PatientDataDefinition getCD4DuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getCD4(), num, df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getNutritionalStatusDuringQuarter(Integer num) {
        return getLastObsValueDuringQuarter(hivMetadata.getMalnutrition(), num, new NutritionalStatusConverter());
    }

    public PatientDataDefinition getINHStartDate() {
        return getEncounterPageObsValue(hivMetadata.getINHDosage(), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getTBStartDate() {
        return getEncounterPageObsValue(hivMetadata.getTBStartDate(), df.getObsValueDatetimeConverter());
    }

    public PatientDataDefinition getTBStopDate() {
        return getEncounterPageObsValue(hivMetadata.getTBStopDate(), df.getObsValueDatetimeConverter());
    }


    public PatientDataDefinition getWHOStage1Date() {
        return getEncounterPageObsValue(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage1()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getWHOStage2Date() {
        return getEncounterPageObsValue(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage2()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getWHOStage3Date() {
        return getEncounterPageObsValue(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage3()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getWHOStage4Date() {
        return getEncounterPageObsValue(hivMetadata.getWHOClinicalStage(), Arrays.asList(hivMetadata.getWHOClinicalStage4()), df.getObsDatetimeConverter());
    }

    public PatientDataDefinition getARTEligibilityDate() {
        return getSummaryPageObsValue(hivMetadata.getDateEligibleForART(), df.getObsValueDatetimeConverter());
    }

    public PatientDataDefinition getARTEligibilityAndReadyDate() {
        return getSummaryPageObsValue(hivMetadata.getDateEligibleAndReadyForART(), df.getObsValueDatetimeConverter());
    }

    public PatientDataDefinition getARTEligibilityWHOStage() {
        return getSummaryPageObsValue(hivMetadata.getDateEligibilityWHOStage(), df.getObsValueCodedConverter());
    }

    public PatientDataDefinition getARTEligibilityCD4() {
        return getSummaryPageObsValue(hivMetadata.getDateEligibilityWHOStage(), df.getObsValueNumericConverter());
    }

    public PatientDataDefinition getStatusAtEnrollment() {
        StatusAtEnrollmentPatientDatasetDefinition def = new StatusAtEnrollmentPatientDatasetDefinition();
        MapConverter c = new MapConverter(": ", ", ", null, new ObjectFormatter());
        return df.convert(def, c);
    }

    public PatientDataDefinition getFUStatus(Integer periodToAdd) {
        FUStatusPatientDataDefinition def = new FUStatusPatientDataDefinition();
        def.setPeriodToAdd(periodToAdd);
        MapConverter c = new MapConverter(": ", ", ", null, new ObjectFormatter());
        return df.convert(def, c);
    }

    public PatientDataDefinition getWhyEligibleForART() {
        WhyEligibleForARTPatientDatasetDefinition def = new WhyEligibleForARTPatientDatasetDefinition();
        def.addParameter(new Parameter("onDate", "On Date", Date.class));
        MapConverter c = new MapConverter(": ", ", ", null, new ObjectFormatter());
        return df.convert(def, ObjectUtil.toMap("onDate=startDate"), c);
    }

    public PatientDataDefinition havingVisitDuringQuarter(Integer quarter) {
        return df.hasVisitDuringPeriod(Period.QUARTERLY, quarter, df.getObsDatetimeConverter());
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

    protected PatientDataDefinition getSummaryPageObsValue(Concept question, DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTSummaryEncounter();
        return df.getObsValue(question, Arrays.asList(arvInitial), converter);
    }

    protected PatientDataDefinition getEncounterPageObsValue(Concept question, DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTEncounterEncounterType();
        return df.getObsValue(question, Arrays.asList(arvInitial), converter);
    }

    protected PatientDataDefinition getEncounterPageObsValue(Concept question, List<Concept> answers, DataConverter converter) {
        EncounterType arvInitial = hivMetadata.getARTEncounterEncounterType();
        return df.getObsValue(question, Arrays.asList(arvInitial), answers, converter);
    }

    protected PatientDataDefinition getFirstObsValueDuringQuarter(Concept question, Integer periodToAdd, DataConverter converter) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("includeEncounters", false);
        map.put("obsPeriod", Period.QUARTERLY);
        map.put("whichObs", TimeQualifier.FIRST);
        return df.getObsValueDuringPeriod(question, periodToAdd, map, converter);
    }

    protected PatientDataDefinition getFirstObsValueDuringMonth(Concept question, Integer periodToAdd, DataConverter converter) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("includeEncounters", false);
        map.put("obsPeriod", Period.MONTHLY);
        map.put("whichObs", TimeQualifier.FIRST);
        return df.getObsValueDuringPeriod(question, periodToAdd, map, converter);
    }

    protected PatientDataDefinition getLastObsValueDuringQuarter(Concept question, Integer periodToAdd, DataConverter converter) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("includeEncounters", false);
        map.put("obsPeriod", Period.QUARTERLY);
        map.put("whichObs", TimeQualifier.LAST);
        return df.getObsValueDuringPeriod(question, periodToAdd, map, converter);
    }

    protected PatientDataDefinition getLastObsValueDuringMonth(Concept question, Integer periodToAdd, DataConverter converter) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("includeEncounters", false);
        map.put("obsPeriod", Period.MONTHLY);
        map.put("whichObs", TimeQualifier.LAST);
        return df.getObsValueDuringPeriod(question, periodToAdd, map, converter);
    }

    protected PatientDataDefinition getObsValueDuringPeriod(Concept question, Period period, Integer periodToAdd, TimeQualifier timeQualifier, DataConverter converter) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("includeEncounters", false);
        map.put("obsPeriod", period);
        map.put("whichObs", timeQualifier);
        return df.getObsValueDuringPeriod(question, periodToAdd, map, converter);
    }
}
