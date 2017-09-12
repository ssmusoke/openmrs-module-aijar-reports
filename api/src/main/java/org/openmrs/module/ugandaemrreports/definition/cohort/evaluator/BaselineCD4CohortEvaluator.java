package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.BaselineCD4CohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;


@Handler(supports = {BaselineCD4CohortDefinition.class})
public class BaselineCD4CohortEvaluator implements CohortDefinitionEvaluator {
    @Autowired
    EvaluationService evaluationService;
    @Autowired
    HIVMetadata hivMetadata;

    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        BaselineCD4CohortDefinition cd = (BaselineCD4CohortDefinition) cohortDefinition;

        Integer of = this.hivMetadata.getCD4().getConceptId();

        String query = "SELECT A.patient_id\nFROM\n  (SELECT\n     e.patient_id,\n     MIN(e.encounter_datetime) AS dt,\n     o.value_numeric\n   FROM obs o INNER JOIN encounter e\n       ON (o.concept_id = 5497 AND o.encounter_id = e.encounter_id AND o.voided = 0 AND e.voided = 0)\n   GROUP BY e.patient_id) A\n";


        if (cd.getValueNumericFrom() != null && cd.getValueNumericTo() != null) {
            query = query + String.format("WHERE A.value_numeric >= %s AND A.value_numeric < %s \n", cd.getValueNumericFrom(), cd.getValueNumericTo());
        } else if (cd.getValueNumericFrom() != null) {
            query = query + String.format("WHERE A.value_numeric >= %s \n", cd.getValueNumericFrom());
        } else if (cd.getValueNumericTo() != null) {
            query = query + String.format("WHERE A.value_numeric <= %s \n", cd.getValueNumericTo());
        }
        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<Object[]> results = this.evaluationService.evaluateToList(q, context);

        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            ret.addMember(pId);
        }
        return ret;
    }
}
