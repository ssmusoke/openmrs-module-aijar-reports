package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator.rest;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.BaseObsCohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.evaluator.DateObsCohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.AppointmentDateAtLocationCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.rest.AppointmentCohortDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@Handler(supports= AppointmentCohortDefinition.class, order=1)
public class AppointmentCohortDefinitionEvaluator extends BaseObsCohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    public AppointmentCohortDefinitionEvaluator() { }

    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) {
        AppointmentCohortDefinition definition = (AppointmentCohortDefinition) cohortDefinition;

        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);

        String startDate = DateUtil.formatDate(definition.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(definition.getEndDate(), "yyyy-MM-dd");

        String query = "SELECT client_id FROM (SELECT client_id, MAX(return_visit_date) returndate FROM mamba_fact_encounter_hiv_art_card GROUP BY client_id) a \n" +
                "  WHERE returndate BETWEEN '" + startDate + "' AND '" + endDate +"'";


        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = evaluationService.evaluateToList(q, context);
        HashSet<Integer> detectedSet = new HashSet<Integer>();
        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            detectedSet.add(pId);

        }

        ret.setMemberIds(detectedSet);
        return ret;
    }

}
