package org.openmrs.module.ugandaemrreports.reporting.calculation.eid;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.calculation.result.SimpleResult;
import org.openmrs.module.ugandaemrreports.reporting.calculation.AbstractPatientCalculation;

/**
 * The date when the infant is due for the first DNA PCR
 */
public class InfantFirstDNAPCRDateCalculation extends AbstractPatientCalculation {
	
	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> params,
	                                     PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		Integer duration = (params != null && params.containsKey("duration")) ? (Integer) params.get("duration") : 0;
		String durationType = (params != null && params.containsKey("durationType")) ? (String) params.get("durationType") : null;
		PatientService patientService = Context.getPatientService();
		
		
		for (int ptId : cohort) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(patientService.getPatient(ptId).getBirthdate());
			
			if (durationType == null || durationType.equals("d")) {
				cal.add(Calendar.DAY_OF_YEAR, duration);
			}
			if (durationType.equals("w")) {
				cal.add(Calendar.WEEK_OF_YEAR, duration);
			}
			if (durationType.equals("m")) {
				cal.add(Calendar.MONTH, duration);
			}
			if (durationType.equals("y")) {
				cal.add(Calendar.YEAR, duration);
			}
			
			ret.put(ptId, new SimpleResult(cal.getTime(), this));
		}
		return ret;
	}
}
