package org.openmrs.module.ugandaemrreports.reporting.calculation.eid;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openmrs.Obs;
import org.openmrs.Relationship;
import org.openmrs.RelationshipType;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

/**
 * Get the infant mother
 */
public class ExposedInfantMotherCalculation extends AbstractPatientCalculation {
	
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params,
	                                     PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		
		PatientService patientService = Context.getPatientService();
		PersonService personService = Context.getPersonService();
		RelationshipType parentRelationshipType = personService.getRelationshipTypeByUuid("8d91a210-c2cc-11de-8d13-0010c6dffd0f");
		
		
		for (int ptId : cohort) {
			// get the relationship for the patient
			List<Relationship> parents = personService.getRelationships(null, patientService.getPatient(ptId).getPerson(), parentRelationshipType);
			
			for (Relationship parent: parents) {
				if (parent.getPersonA().getGender().equals("F")) {
					ret.put(ptId, new SimpleResult(parent.getPersonA(), this));
				}
			}
		}
		
		return ret;
	}
}
