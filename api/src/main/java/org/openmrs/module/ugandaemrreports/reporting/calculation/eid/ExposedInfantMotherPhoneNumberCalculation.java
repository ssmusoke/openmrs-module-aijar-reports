package org.openmrs.module.ugandaemrreports.reporting.calculation.eid;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.PersonAttributeType;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;

/**
 * Get the infant mother
 */
public class ExposedInfantMotherPhoneNumberCalculation extends AbstractPatientCalculation {
	
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params,
	                                     PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		
		PatientService patientService = Context.getPatientService();
		PersonService personService = Context.getPersonService();
		RelationshipType parentRelationshipType = personService.getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f");
		PersonAttributeType phoneNumberAttributeType = personService.getPersonAttributeTypeByUuid("14d4f066-15f5-102d-96e4-000c29c2a5d7");
		
		
		for (int ptId : cohort) {
			// get the relationship for the patient
			List<Relationship> parents = personService.getRelationships(null, patientService.getPatient(ptId).getPerson(), parentRelationshipType);
			
			for (Relationship parent: parents) {
				if (parent.getPersonA().getGender().equals("F")) {
					ret.put(ptId, new SimpleResult(parent.getPersonA().getAttribute(phoneNumberAttributeType), this));
				}
			}
		}
		
		return ret;
	}
}
