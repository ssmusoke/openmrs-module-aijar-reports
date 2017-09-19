package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.MissedAppointmentCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;

/**
 */
@Handler(supports = {MissedAppointmentCohortDefinition.class})
public class MissedAppointmentCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        MissedAppointmentCohortDefinition cd = (MissedAppointmentCohortDefinition) cohortDefinition;

        SqlQueryBuilder atLeastOneMissedAppointment = new SqlQueryBuilder();

        atLeastOneMissedAppointment.append("select e.patient_id from encounter e inner join obs oi on(oi.concept_id = 99161 and oi.person_id = e.patient_id and e.encounter_datetime  BETWEEN oi.value_datetime and DATE_ADD(oi.value_datetime, INTERVAL 1 YEAR)) inner join (select patient_id,MAX(encounter_datetime) as max_date from encounter group by patient_id) eo on(eo.patient_id = e.patient_id and DATEDIFF(eo.max_date, oi.value_datetime) >= 365) where e.encounter_id not in (select o.encounter_id from obs o where o.concept_id = 90069 group by o.encounter_id ) group by e.patient_id");

        SqlQueryBuilder atLeastOneScheduledAppointment = new SqlQueryBuilder();

        atLeastOneScheduledAppointment.append("select e.patient_id from encounter e inner join obs oi on(oi.concept_id = 99161 and oi.person_id = e.patient_id and e.encounter_datetime  BETWEEN oi.value_datetime and DATE_ADD(oi.value_datetime, INTERVAL 1 YEAR)) inner join (select patient_id,MAX(encounter_datetime) as max_date from encounter group by patient_id) eo on(eo.patient_id = e.patient_id and DATEDIFF(eo.max_date, oi.value_datetime) >= 365) where e.encounter_id in (select o.encounter_id from obs o where o.concept_id = 90069 group by o.encounter_id ) group by e.patient_id");

        List<Integer> atLeastOneMissedAppointmentResult = evaluationService.evaluateToList(atLeastOneMissedAppointment, Integer.class, context);
        List<Integer> atLeastOneScheduledAppointmentResult = evaluationService.evaluateToList(atLeastOneScheduledAppointment, Integer.class, context);

        Collection<Integer> aMinusB = CollectionUtils.subtract(atLeastOneScheduledAppointmentResult, atLeastOneMissedAppointmentResult);

        for (Integer person_id : aMinusB) {
            ret.addMember(person_id);
        }

        return ret;
    }
}
