package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ObsWithEncountersCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 */
@Handler(supports = { ObsWithEncountersCohortDefinition.class })
public class ObsWithEncountersCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

	@Autowired
	EvaluationService evaluationService;

	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
			throws EvaluationException {
		EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
		ObsWithEncountersCohortDefinition cd = (ObsWithEncountersCohortDefinition) cohortDefinition;

		Date beginning = DateUtil.getStartOfMonth(cd.getStartDate());
		Date ending = DateUtil.getEndOfMonth(cd.getStartDate());

		Date endingOfEnding = DateUtil.getEndOfMonth(cd.getEndDate());

		HqlQueryBuilder encQuery = new HqlQueryBuilder();
		encQuery.select(new String[] { "e.patient.patientId, e.encounterId" });
		encQuery.from(Encounter.class, "e");
		encQuery.whereIn("e.encounterType", cd.getEncounterTypes());
		encQuery.whereBetweenInclusive("e.encounterDatetime", beginning, ending);
		if (cd.getWhichEncounter() == TimeQualifier.LAST) {
			encQuery.orderAsc("e.encounterDatetime").orderAsc("e.dateCreated");
		} else if (cd.getWhichEncounter() == TimeQualifier.FIRST) {
			encQuery.orderDesc("e.encounterDatetime").orderDesc("e.dateCreated");
		}

		HashSet personsToInclude = new HashSet();
		if (cd.getWhichEncounter() != TimeQualifier.LAST && cd.getWhichEncounter() != TimeQualifier.FIRST) {
			List q1 = this.evaluationService.evaluateToList(encQuery, Integer.class, context);
			personsToInclude.addAll(q1);
		} else {
			Map q = this.evaluationService.evaluateToMap(encQuery, Integer.class, Integer.class, context);
			personsToInclude.addAll(q.values());
		}

		HqlQueryBuilder q2 = new HqlQueryBuilder();
		q2.select(new String[] { "o.personId" });
		q2.from(Obs.class, "o");
		q2.whereEqual("o.concept", cd.getQuestion());
		q2.whereIdIn("o.person", personsToInclude);
		q2.whereBetweenInclusive("o.encounter.encounterDatetime", beginning, endingOfEnding);

		if (cd.getAnswers() != null) {
			q2.whereIn("o.valueCoded", cd.getAnswers());
		}

		List pIds = this.evaluationService.evaluateToList(q2, Integer.class, context);
		ret.getMemberIds().addAll(pIds);
		return ret;
	}
}
