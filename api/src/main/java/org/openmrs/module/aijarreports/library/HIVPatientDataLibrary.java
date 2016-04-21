package org.openmrs.module.aijarreports.library;

import java.util.Arrays;
import java.util.Map;

import org.openmrs.EncounterType;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.module.aijar.metadata.core.PatientIdentifierTypes;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PreferredIdentifierDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.openmrs.module.reporting.evaluation.parameter.ParameterizableUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

	@DocumentedDefinition(value="clinicnumber", name="Clinic Number")
	public PatientDataDefinition getClinicNumber() {
		PreferredIdentifierDataDefinition def = new PreferredIdentifierDataDefinition();
		def.setIdentifierType(MetadataUtils.existing(PatientIdentifierType.class, PatientIdentifierTypes.HIV_CARE_NUMBER.uuid()));
		return convert(def, new PropertyConverter(PatientIdentifier.class, "identifier"));
	}

	@DocumentedDefinition(value="enrollmentdate", name="Enrollment Date")
	public PatientDataDefinition getEnrollmentDate() {
		return getFirstArtInitialEncounterByEndDate(df.getEncounterDatetimeConverter());
	}

	@DocumentedDefinition(value="artstartdate", name="ART Start Date")
	public PatientDataDefinition getARTStartDate() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getARTStartDate()), df.getObsValueDatetimeConverter());
	}

	@DocumentedDefinition(value="lastregimenpickupdate", name="Last Regimen Pickup Date")
	public PatientDataDefinition getLastRegimenPickupDate() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getCurrentRegimen()), df.getObsDatetimeConverter());
	}

	@DocumentedDefinition(value="currentregimen", name="Current Regimen")
	public PatientDataDefinition getCurrentRegimen() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getCurrentRegimen()), df.getObsValueCodedConverter());
	}

	@DocumentedDefinition(value="currentregimendate", name="Current Regimen Date")
	public PatientDataDefinition getCurrentRegimenDate() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getCurrentRegimen()), df.getObsDatetimeConverter());
	}

	@DocumentedDefinition(value="arvduration", name="ARV Duration")
	public PatientDataDefinition getARVDuration() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getARVDuration()), df.getObsValueNumericConverter());
	}

	@DocumentedDefinition(value="expectedreturndate", name="Expected Return Date")
	public PatientDataDefinition getExpectedReturnDate() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getExpectedReturnDate()), df.getObsValueDatetimeConverter());
	}
	@DocumentedDefinition(value="startregimen", name="Start Regimen")
	public PatientDataDefinition getStartRegimen() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getStartRegimen()), df.getObsValueCodedConverter());
	}
	@DocumentedDefinition(value="startregimendate", name="Start Regimen Date")
	public PatientDataDefinition getStartRegimenDate() {
		return convert(df.getMostRecentObsByEndDate(hivMetadata.getStartRegimenDate()), df.getObsValueDatetimeConverter());
	}

	protected PatientDataDefinition getFirstArtInitialEncounterByEndDate(DataConverter converter) {
		EncounterType arvInitial = hivMetadata.getARTSummaryPageEncounterType().get(0);
		return df.getFirstEncounterOfTypeByEndDate(arvInitial, converter);
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
}
