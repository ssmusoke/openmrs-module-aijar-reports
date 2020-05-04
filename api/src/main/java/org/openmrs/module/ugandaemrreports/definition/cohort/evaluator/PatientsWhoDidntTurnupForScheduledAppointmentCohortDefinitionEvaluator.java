package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.PatientsWhoDidntTurnupForScheduledAppointmentCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 */
@Handler(supports = {PatientsWhoDidntTurnupForScheduledAppointmentCohortDefinition.class})
public class PatientsWhoDidntTurnupForScheduledAppointmentCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired

    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        PatientsWhoDidntTurnupForScheduledAppointmentCohortDefinition cd = (PatientsWhoDidntTurnupForScheduledAppointmentCohortDefinition) cohortDefinition;

        // Maximum appointment date during the period
        HqlQueryBuilder obsQuery = new HqlQueryBuilder();
        obsQuery.select("o.person.personId", "max(o.valueDatetime)");
        obsQuery.from(Obs.class, "o");
        obsQuery.whereEqual("o.concept", hivMetadata.getReturnVisitDate());

        if (cd.getStartDate() != null && cd.getEndDate() != null) {
            obsQuery.whereBetweenInclusive("o.valueDatetime", cd.getStartDate(), cd.getEndDate());
        } else if (cd.getStartDate() != null) {
            obsQuery.whereGreaterOrEqualTo("o.valueDatetime", cd.getStartDate());
        } else if (cd.getEndDate() != null) {
            obsQuery.whereLessOrEqualTo("o.valueDatetime", cd.getEndDate());
        }
        obsQuery.whereEqual("o.person.personVoided", false);
        obsQuery.groupBy("o.person.personId");

        // Minimum visit date after appointment date
        HqlQueryBuilder encounterQuery = new HqlQueryBuilder();
        encounterQuery.select("e.patient.patientId", "min(e.encounterDatetime)");
        encounterQuery.from(Encounter.class, "e");

        if (cd.getStartDate() != null) {
            encounterQuery.whereGreaterOrEqualTo("e.encounterDatetime", cd.getStartDate());
        }

        encounterQuery.whereEqual("e.patient.personVoided", false);

        encounterQuery.groupBy("e.patient.patientId");

        // Early visits before the return visit date appointed
        String startDate = "";
        String endDate = "";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");
        String earlyBirdPatientsQuery ="select patient_id from encounter e2 \n" +
                "inner join ( select o.person_id,encounter_datetime, max(value_datetime)as return_date from obs o \n" +
                " inner join encounter e on o.encounter_id = e.encounter_id\n" +
                String.format("where o.concept_id=5096 and  o.value_datetime between '%s' and '%s' \n",startDate,endDate) +
                "and o.voided=0 group by person_id) as t on e2.patient_id =t.person_id \n" +
                " where e2.encounter_datetime > t.encounter_datetime and e2.encounter_datetime < return_date group by patient_id;";
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(earlyBirdPatientsQuery);
        List<Integer> earlyBirdPatientsResults = evaluationService.evaluateToList(q,Integer.class, context);

        Map<Integer, Date> obsResults = evaluationService.evaluateToMap(obsQuery, Integer.class, Date.class, context);
        Map<Integer, Date> encounterResults = evaluationService.evaluateToMap(encounterQuery, Integer.class, Date.class, context);

        for (Map.Entry<Integer, Date> entry : obsResults.entrySet()) {
            Integer patientId = entry.getKey();
            //remove the early comers
            if(earlyBirdPatientsResults.contains(patientId)){
                continue;
            }
            else {
                Date encounterDate = encounterResults.get(patientId);
                if (encounterDate != null) {
                    Integer daysBetweenLastAppointmentAndCurrentDate = DateUtil.getDaysBetween(entry.getValue(), encounterDate);
                    if (cd.getMaximumDays() != null && cd.getMinimumDays() != null && daysBetweenLastAppointmentAndCurrentDate >= cd.getMinimumDays() && daysBetweenLastAppointmentAndCurrentDate <= cd.getMaximumDays()) {
                        ret.addMember(patientId);
                    } else if (cd.getMinimumDays() != null && daysBetweenLastAppointmentAndCurrentDate >= cd.getMinimumDays()) {
                        ret.addMember(patientId);
                    } else if (cd.getMaximumDays() != null && daysBetweenLastAppointmentAndCurrentDate <= cd.getMaximumDays()) {
                        ret.addMember(patientId);
                    }
                } else {
                    // All members who didn't have any encounter after visit date will be considered lost,missed appointment or lost to followup
                    ret.addMember(patientId);
                }
            }
        }

        return ret;
    }
}
