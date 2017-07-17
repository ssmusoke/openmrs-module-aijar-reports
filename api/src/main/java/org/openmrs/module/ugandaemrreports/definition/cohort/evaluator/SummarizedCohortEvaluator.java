package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Cohort;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.SummarizedCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ViralLoadCohortDefinition;
import org.openmrs.module.ugandaemrreports.lucene.UgandaEMRLucene;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by carapai on 28/06/2016.
 */
@Handler(supports = {SummarizedCohortDefinition.class})
public class SummarizedCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        SummarizedCohortDefinition cd = (SummarizedCohortDefinition) cohortDefinition;
        String period = UgandaEMRLucene.getObsPeriod(cd.getStartDate(), cd.getPeriod());
        String query = cd.getQuery();
        if (period != null && query != null) {
            query = UgandaEMRLucene.joinQuery(query, UgandaEMRLucene.constructSQLQuery("period", cd.getPeriodComparator(), period), Enums.UgandaEMRJoiner.AND);
        } else if (period != null) {
            query = UgandaEMRLucene.constructSQLQuery("period", cd.getPeriodComparator(), period);
        }

        if (query != null) {
            query = "SELECT patients FROM obs_summary WHERE " + query;
        } else {
            query = "SELECT patients FROM obs_summary";
        }

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);

        List<String> results = evaluationService.evaluateToList(q, String.class, context);

        for (String row : results) {
            Cohort c = new Cohort(row);
            ret.getMemberIds().addAll(c.getMemberIds());
        }
        return ret;
    }

}
