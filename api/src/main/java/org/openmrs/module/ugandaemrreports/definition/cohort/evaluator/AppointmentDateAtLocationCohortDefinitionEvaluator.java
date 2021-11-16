package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.BaseObsCohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.evaluator.DateObsCohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.AppointmentDateAtLocationCohortDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * Evaluates a DateObsCohortDefinition and produces a Cohort
 */
@Handler(supports=AppointmentDateAtLocationCohortDefinition.class, order=1)
public class AppointmentDateAtLocationCohortDefinitionEvaluator extends BaseObsCohortDefinitionEvaluator {

	@Autowired
	private DateObsCohortDefinitionEvaluator dateObsCohortDefinitionEvaluator;

	public AppointmentDateAtLocationCohortDefinitionEvaluator() { }

    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {
        AppointmentDateAtLocationCohortDefinition cd = (AppointmentDateAtLocationCohortDefinition) cohortDefinition;

        Location location = cd.getLocation();

        DateObsCohortDefinition dateObsCohortDefinition = cd;

		if(location!=null){

			/*adding up ART Clinic Location with data that has no location attached */
			if(!location.getUuid().equals("86863db4-6101-4ecf-9a86-5e716d6504e4")){
				dateObsCohortDefinition.setLocationList(Arrays.asList(location));
			}
		}

		return dateObsCohortDefinitionEvaluator.evaluate(dateObsCohortDefinition,context);
    }
	
}
