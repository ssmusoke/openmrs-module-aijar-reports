package org.openmrs.module.ugandaemrreports.reporting.calculation;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.openmrs.Obs;
import org.openmrs.calculation.patient.PatientCalculationContext;
import org.openmrs.calculation.result.CalculationResultMap;
import org.openmrs.module.ugandaemrreports.reporting.cohort.Filters;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class EmptyProcedureMethods extends AbstractPatientCalculation{

	@Override
	public CalculationResultMap evaluate(Collection<Integer> cohort, Map<String, Object> map,
			PatientCalculationContext context) {
		CalculationResultMap ret = new CalculationResultMap();
		Set<Integer> onlyMale = Filters.male(cohort, context);
		
		CalculationResultMap procedureMethod = Calculations.lastObs(Dictionary.getConcept("bd66b11f-04d9-46ed-a367-2c27c15d5c71"), cohort, context);
		for(Integer ptId: onlyMale) {
			boolean hasProcedureMethod = false;
			Obs procedureMethodObs = EmrCalculationUtils.obsResultForPatient(procedureMethod, ptId);
			if(procedureMethodObs == null) {
				hasProcedureMethod = true;
			}
			ret.put(ptId, new BooleanResult(hasProcedureMethod, this));
					
		}
		return ret;
	}

}
