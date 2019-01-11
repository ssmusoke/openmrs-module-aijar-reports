package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.data.definition.DSDMClinicalStage1or2CohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;

/**
 */
@Handler(supports = {DSDMClinicalStage1or2CohortDefinition.class})
public class DSDMClinicalStage1or2CohortDefinitionEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        DSDMClinicalStage1or2CohortDefinition cd = (DSDMClinicalStage1or2CohortDefinition) cohortDefinition;

        String startDate = "";
        String endDate = "";

        startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");


        String sql = "select e.patient_id from obs inner  join encounter e on obs.encounter_id = e.encounter_id \n" +
                "where concept_id=90203 and value_coded in (90033,90034) \n" +
                String.format(     "  and obs.voided=0 and obs.obs_datetime between '%s' and '%s' group by e.patient_id",startDate,endDate);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(sql);
        List<Integer> results = evaluationService.evaluateToList(q, Integer.class, context);

        HashSet<Integer> detectedSet = new HashSet<Integer>(results);
        ret.setMemberIds(detectedSet);
        return ret;
    }
}
