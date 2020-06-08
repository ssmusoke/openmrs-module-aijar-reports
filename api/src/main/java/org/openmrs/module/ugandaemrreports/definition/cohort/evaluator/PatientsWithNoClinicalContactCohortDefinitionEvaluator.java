package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.PatientsWithNoClinicalContactDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * This cohort evaluates patients who have not had clinical contact
 * ever since their last visit date and not appeared since their last
 * expected appointment
 */
@Handler(supports = {PatientsWithNoClinicalContactDefinition.class})
public class PatientsWithNoClinicalContactCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired

    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        PatientsWithNoClinicalContactDefinition cd = (PatientsWithNoClinicalContactDefinition) cohortDefinition;

        // Getting Maximum appointment date during the period
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

        // Getting Maximum encounter visit date
        HqlQueryBuilder encounterQuery = new HqlQueryBuilder();
        encounterQuery.select("e.patient.patientId", "max(e.encounterDatetime)");
        encounterQuery.from(Encounter.class, "e");

        if (cd.getStartDate() != null && cd.getEndDate() != null) {
            encounterQuery.whereBetweenInclusive("e.encounterDatetime", cd.getStartDate(), cd.getEndDate());
        } else if (cd.getStartDate() != null) {
            encounterQuery.whereGreaterOrEqualTo("e.encounterDatetime", cd.getStartDate());
        } else if (cd.getEndDate() != null) {
            encounterQuery.whereLessOrEqualTo("e.encounterDatetime", cd.getEndDate());
        }
        encounterQuery.whereEqual("e.patient.personVoided", false);
        encounterQuery.groupBy("e.patient.patientId");

        Map<Integer, Date> obsResults = evaluationService.evaluateToMap(obsQuery, Integer.class, Date.class, context);
        Map<Integer, Date> encounterResults = evaluationService.evaluateToMap(encounterQuery, Integer.class, Date.class, context);

        for (Map.Entry<Integer, Date> entry : obsResults.entrySet()) {
            Integer patientId = entry.getKey();
            Date encounterDate = encounterResults.get(patientId);

                if(entry.getValue().after(encounterDate)) {
                    ret.addMember(patientId);
                }
        }
        return ret;
    }
}
