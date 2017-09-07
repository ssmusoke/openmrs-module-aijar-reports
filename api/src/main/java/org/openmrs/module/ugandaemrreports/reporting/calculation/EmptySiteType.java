package org.openmrs.module.ugandaemrreports.reporting.calculation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.openmrs.Obs;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class EmptySiteType extends AbstractPatientCalculation{

	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map,
			PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		Set<Integer> onlyMale = Filters.male(cohort, context);
		
		CalculationResultMap siteType = Calculations.lastObs(Dictionary.getConcept("ac44b5f2-cf57-43ca-bea0-8b392fe21802"), cohort, context);
		for(Integer ptId: onlyMale) {
			boolean hasSiteType = false;
			Obs siteTypeObs = EmrCalculationUtils.obsResultForPatient(siteType, ptId);
			if(siteTypeObs == null) {
				hasSiteType = true;
			}
			ret.put(ptId, new BooleanResult(hasSiteType, this));
					
		}
		return ret;
	}

}
