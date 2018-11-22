package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.joda.time.LocalDate;
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
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtStartCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.LongRefillsCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 */
@Handler(supports = {LongRefillsCohortDefinition.class})
public class LongRefillsCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        LongRefillsCohortDefinition cd = (LongRefillsCohortDefinition) cohortDefinition;

        String startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");
        String endDate = DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd");


    /*  query to get patients whose last encounter date in the
       2 last quarters has return visit date wc is after current period
       picks maximum encounters from  2 last quaters  which have return
       visit date greater than current period*/

        String q= "select patient_id from encounter e  inner join obs o on\n" +
                String.format("   e.encounter_id = o.encounter_id where e.encounter_datetime >= date_sub('%s', interval 3 month) and \n",startDate) +
                String.format(   "   e.encounter_datetime <'%s' and o.value_datetime> '%s' and o.concept_id =5096 group by patient_id",startDate,endDate);

        SqlQueryBuilder query = new SqlQueryBuilder(q);

        List<Integer> pIds = evaluationService.evaluateToList(query, Integer.class, context);

        ret.getMemberIds().addAll(pIds);
        return ret;
    }
}
