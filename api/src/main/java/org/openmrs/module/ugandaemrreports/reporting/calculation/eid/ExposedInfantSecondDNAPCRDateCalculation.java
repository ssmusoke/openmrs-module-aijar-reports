package org.openmrs.module.ugandaemrreports.reporting.calculation.eid;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;
import org.openmrs.module.ugandaemrreports.reporting.calculation.Calculations;
import org.openmrs.module.ugandaemrreports.reporting.calculation.EmrCalculationUtils;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

/**
 * The date when the infant is due for the second DNA PCR
 */
public class ExposedInfantSecondDNAPCRDateCalculation extends AbstractPatientCalculation {
	
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params,
	                                     PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		String question = (params != null && params.containsKey("question")) ? (String) params.get("question") : null;
		CalculationResultMap questionMap = Calculations.lastObs(Dictionary.getConcept(question), cohort, context);
		
		
		PatientService patientService = Context.getPatientService();
		
		
		for (int ptId : cohort) {
			Calendar birthDate = Calendar.getInstance();
			birthDate.setTime(patientService.getPatient(ptId).getBirthdate());
			
			Obs breastFeedingStatus = EmrCalculationUtils.obsResultForPatient(questionMap, ptId);
			Calendar secondDNAPCRDate = Calendar.getInstance();
			
			
			if (breastFeedingStatus != null && breastFeedingStatus.getValueCoded().getUuid().equals("0f46cbdc-54cb-40bd-8f75-72abcf6fc852")) {
				// infant is no longer breast feeding now add 6 weeks to the date breast feeding stopped
				secondDNAPCRDate.setTime(breastFeedingStatus.getEncounter().getEncounterDatetime());
				// now add 6 weeks
				secondDNAPCRDate.add(Calendar.WEEK_OF_YEAR, 6);
				
			} else {
				// baby is still breast feeding so add 14 months or since the breast feeding status is unknown
				secondDNAPCRDate = birthDate;
				secondDNAPCRDate.add(Calendar.MONTH, 14);
			}
			
			
			
			ret.put(ptId, new SimpleResult(secondDNAPCRDate.getTime(), this));
		}
		
		return ret;
	}
}
