package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.joda.time.LocalDate;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.DueForViralLoadPatientDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@Handler(supports = {DueForViralLoadPatientDataDefinition.class})
public class DueForViralLoadPatientDataEvaluator
        implements CohortDefinitionEvaluator {
    @Autowired
    EvaluationService evaluationService;
    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext evaluationContext) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, evaluationContext);

        DueForViralLoadPatientDataDefinition def = (DueForViralLoadPatientDataDefinition) cohortDefinition;

        LocalDate startDate = StubDate.dateOf(def.getStartDate());
        LocalDate endDate = StubDate.dateOf(def.getEndDate());

        String endDateString = endDate.toString("yyyy-MM-dd");
        String startDateString = startDate.toString("yyyy-MM-dd");

//        the query retrieves all clients that have a 12 months difference between there last vl date and are also above
//                15 yrs,it also picks those that have a 6months difference from last vl date and are below 15

        String query ="SELECT id FROM \n" +
                " (Select o.person_id as id\n" +
                "  from obs o where o.concept_id =163023 \n" +
                "   And " +
                String.format("TIMESTAMPDIFF(MONTH,o.value_datetime,'%s') > 11 AND o.voided=0 group by o.person_id) A \n",endDateString) +
                "   INNER JOIN \n" +
                "   (select p.person_id\n" +
                " from person p where " +
                String.format("TIMESTAMPDIFF(YEAR,p.birthdate,'%s')>15)B\n",endDateString) +
                "   ON A.id = B.person_id\n" +
                "   UNION ALL\n" +
                "Select id from \n" +
                "   (Select o.person_id as id\n" +
                "  from obs o where o.concept_id =163023 \n" +
                "   And " +
                String.format("TIMESTAMPDIFF(MONTH,o.value_datetime,'%s') = 6 group by o.person_id)C \n",endDateString) +
                "   INNER JOIN\n" +
                "   (select p.person_id \n" +
                "from person p where " +
                String.format("TIMESTAMPDIFF(YEAR,p.birthdate,'%s')<15)D \n",endDateString) +
                "   ON C.id=D.person_id \n";


        SqlQueryBuilder q = new SqlQueryBuilder(query);
        List<Object[]> results = evaluationService.evaluateToList(q, evaluationContext);

        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            ret.addMember(pId);
        }

        return ret;
    }
}