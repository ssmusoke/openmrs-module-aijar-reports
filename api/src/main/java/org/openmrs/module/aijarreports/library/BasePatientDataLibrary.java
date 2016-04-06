package org.openmrs.module.aijarreports.library;

import org.openmrs.module.aijarreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.data.converter.PropertyConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.openmrs.module.reporting.definition.library.DocumentedDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BasePatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {

	@Autowired
	private DataFactory df;

	@Autowired
	private HIVMetadata metadata;

	@Override
	public String getKeyPrefix() {
		return "aijar.patientdata.";
	}

	@Override
	public Class<? super PatientDataDefinition> getDefinitionType() {
		return PatientDataDefinition.class;
	}

	@DocumentedDefinition(value="birthdate", name="Birth Date")
	public PatientDataDefinition getBirthdate() {
		return df.convert(new BirthdateDataDefinition(), new PropertyConverter(Birthdate.class, "birthdate"));
	}
}
