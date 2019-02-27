package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
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
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.DSDMCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.LostPatientsCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 */
@Handler(supports = {DSDMCohortDefinition.class})
public class DSDMCohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        DSDMCohortDefinition cd = (DSDMCohortDefinition) cohortDefinition;

        String startDate = "";
        String endDate = "";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");

//         total numbers enrolled on a program
        String query = "SELECT p.patient_id FROM patient a inner join  patient_program p on a.patient_id = p.patient_id  " + " " + String.format("   WHERE  p.date_enrolled between '%s' AND '%s' AND p.date_completed IS NULL GROUP BY p.patient_id  \n", startDate, endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);
        HashSet<Integer> detectedSet = new HashSet<Integer>(results);
        ret.setMemberIds(detectedSet);
        return ret;
    }
}
