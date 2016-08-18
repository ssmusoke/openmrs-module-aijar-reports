package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.LostPatientsCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 28/06/2016.
 */
@Handler(supports = {LostPatientsCohortDefinition.class})
public class LostPatientsCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        LostPatientsCohortDefinition cd = (LostPatientsCohortDefinition) cohortDefinition;

        HqlQueryBuilder obsQuery = new HqlQueryBuilder();
        obsQuery.select("o.person.personId", "max(o.valueDatetime)");
        obsQuery.from(Obs.class, "o");
        obsQuery.wherePersonIn("o.person.personId", context);
        obsQuery.whereEqual("o.concept.conceptId", 5096);
        obsQuery.whereLessOrEqualTo("o.valueDatetime", cd.getEndDate());
        obsQuery.whereEqual("o.person.personVoided", false);

        obsQuery.groupBy("o.person.personId");


        List<Object[]> queryResults = evaluationService.evaluateToList(obsQuery, context);

        for (Object[] row : queryResults) {
            Integer patientId = (Integer) row[0];
            Date nextAppointmentDate = (Date) row[1];

            Integer daysBetweenLastAppointmentAndCurrentDate = DateUtil.getDaysBetween(nextAppointmentDate, cd.getEndDate());

            if (cd.getMaximumDays() != null && cd.getMinimumDays() != null && daysBetweenLastAppointmentAndCurrentDate >= cd.getMinimumDays() && daysBetweenLastAppointmentAndCurrentDate <= cd.getMaximumDays()) {
                ret.addMember(patientId);
            } else if (cd.getMinimumDays() != null && daysBetweenLastAppointmentAndCurrentDate >= cd.getMinimumDays()) {
                ret.addMember(patientId);
            } else if (cd.getMaximumDays() != null && daysBetweenLastAppointmentAndCurrentDate <= cd.getMaximumDays()) {
                ret.addMember(patientId);
            }

        }
        return ret;
    }
}
