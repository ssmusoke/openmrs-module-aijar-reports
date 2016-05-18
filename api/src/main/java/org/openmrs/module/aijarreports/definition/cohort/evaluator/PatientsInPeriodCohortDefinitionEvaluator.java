package org.openmrs.module.aijarreports.definition.cohort.evaluator;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.definition.cohort.definition.PatientsInPeriodCohortDefinition;
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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by carapai on 20/04/2016.
 */
@Handler(supports = {PatientsInPeriodCohortDefinition.class})
public class PatientsInPeriodCohortDefinitionEvaluator implements CohortDefinitionEvaluator {
    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        PatientsInPeriodCohortDefinition cd = (PatientsInPeriodCohortDefinition) cohortDefinition;

        Period period = cd.getPeriod();
        Date beginPeriod = cd.getStartDate();

        Date beginning = null;
        Date ending = null;

        if (period == Period.MONTHLY) {
            beginning = DateUtil.getStartOfMonth(beginPeriod);
            ending = DateUtil.getEndOfMonth(beginPeriod);
        } else if (period == period.QUARTERLY) {

        } else if (period == period.YEARLY) {
            beginning = DateUtil.getStartOfYear(beginPeriod);
            ending = DateUtil.getEndOfYear(beginPeriod);
        }

        HqlQueryBuilder encQuery = new HqlQueryBuilder();
        encQuery.select(new String[]{"e.encounterId, e.patient.patientId"});
        encQuery.from(Encounter.class, "e");
        encQuery.whereIn("e.encounterType", cd.getEncounterTypes());
        encQuery.whereBetweenInclusive("e.encounterDatetime", beginning, ending);
        if (cd.getWhichEncounter() == TimeQualifier.LAST) {
            encQuery.orderAsc("e.encounterDatetime");
        } else if (cd.getWhichEncounter() == TimeQualifier.FIRST) {
            encQuery.orderDesc("e.encounterDatetime");
        }

        HashSet people = new HashSet();

        if (cd.getWhichEncounter() != TimeQualifier.LAST && cd.getWhichEncounter() != TimeQualifier.FIRST) {
            List q1 = this.evaluationService.evaluateToList(encQuery, Integer.class, context);
            people.addAll(q1);
        } else {
            Map q = this.evaluationService.evaluateToMap(encQuery, Integer.class, Integer.class, context);
            people.addAll(q.values());
        }

        if (cd.isIncludeObs()) {
            HqlQueryBuilder q2 = new HqlQueryBuilder();
            q2.select(new String[]{"o.personId"});
            q2.from(Obs.class, "o");
            q2.whereEqual("o.concept", cd.getQuestion());
            q2.whereIdIn("o.person", people);
            q2.whereBetweenInclusive("o.encounter.encounterDatetime", beginning, ending);

            if (cd.getAnswers() != null) {
                q2.whereIn("o.valueCoded", cd.getAnswers());
            }

            List pIds = this.evaluationService.evaluateToList(q2, Integer.class, context);
            ret.getMemberIds().addAll(pIds);
            return ret;
        } else {
            ret.getMemberIds().addAll(people);
            return ret;
        }
    }
}
