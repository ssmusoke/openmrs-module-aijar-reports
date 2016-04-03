package org.openmrs.module.aijarreports.library;

import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.library.BaseDefinitionLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Attributes of a person not defined within the core OpenMRS definition
 */
@Component
public class HIVPatientDataLibrary extends BaseDefinitionLibrary<PatientDataDefinition> {
	@Autowired
	private DataFactory df;

	@Override
	public Class<? super PatientDataDefinition> getDefinitionType() {
		return PatientDataDefinition.class;
	}

	@Override
	public String getKeyPrefix() {
		return "aijar.patientdata.";
	}

	/*@DocumentedDefinition("artclinicnumber")
	public PatientDataDefinition getARTClinicNumber() {
		PatientIdentifierType pit = ;
		return df.getPreferredProgramIdentifierAtLocation(pit, program, new PatientIdentifierConverter());
	}*/
}
