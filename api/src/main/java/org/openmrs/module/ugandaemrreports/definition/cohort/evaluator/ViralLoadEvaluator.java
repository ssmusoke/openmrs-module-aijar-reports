package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ViralLoadCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 */
@Handler(supports = {ViralLoadCohortDefinition.class})
public class ViralLoadEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ViralLoadCohortDefinition cd = (ViralLoadCohortDefinition) cohortDefinition;

        HqlQueryBuilder obsQuery = new HqlQueryBuilder();
        obsQuery.select(new String[]{"o.personId, o.encounter.encounterId"});
        obsQuery.from(Obs.class, "o");
        obsQuery.whereEqual("o.concept", hivMetadata.getViralLoadDate());

        if (cd.getStartDate() != null && cd.getEndDate() != null) {
            obsQuery.whereBetweenInclusive("o.valueDatetime", cd.getStartDate(), cd.getEndDate());
        } else if (cd.getStartDate() != null) {
            obsQuery.whereGreaterOrEqualTo("o.valueDatetime", cd.getStartDate());
        } else if (cd.getEndDate() != null) {
            obsQuery.whereLessOrEqualTo("o.valueDatetime", cd.getEndDate());
        }

        Map<Integer, Integer> obsResults = evaluationService.evaluateToMap(obsQuery, Integer.class, Integer.class, context);

        Set<Integer> encounterIds = new HashSet<Integer>(obsResults.values());
        Set<Integer> patients = new HashSet<Integer>(obsResults.keySet());
        
        HqlQueryBuilder detectedQuery = new HqlQueryBuilder();
        detectedQuery.select(new String[]{"o.personId"});
        detectedQuery.from(Obs.class, "o");
        detectedQuery.whereEqual("o.concept", hivMetadata.getViralLoadDetection());
        detectedQuery.whereIn("o.encounter.encounterId", encounterIds);

        HqlQueryBuilder copiesQuery = new HqlQueryBuilder();
        copiesQuery.select(new String[]{"o.personId"});
        copiesQuery.from(Obs.class, "o");
        copiesQuery.whereEqual("o.concept", hivMetadata.getViralLoadCopies());
        copiesQuery.whereIn("o.encounter.encounterId", encounterIds);


        if (cd.getCopiesFrom() != null && cd.getCopiesTo() != null) {
            copiesQuery.whereBetweenInclusive("o.valueNumeric", cd.getCopiesFrom(), cd.getCopiesTo());
        } else if (cd.getCopiesFrom() != null) {
            copiesQuery.whereGreaterOrEqualTo("o.valueNumeric", cd.getCopiesFrom());
        } else if (cd.getCopiesTo() != null) {
            copiesQuery.whereLessOrEqualTo("o.valueNumeric", cd.getCopiesTo());

        }

        if (cd.getDetected()) {
            if (cd.getCopiesFrom() != null || cd.getCopiesTo() != null) {
                List<Integer> copies = evaluationService.evaluateToList(copiesQuery, Integer.class, context);
                patients = new HashSet<Integer>(copies);
            } else {
                List<Integer> detected = evaluationService.evaluateToList(detectedQuery, Integer.class, context);
                patients = new HashSet<Integer>(detected);
            }
        } else if (cd.getNotDetected()) {
            List<Integer> detectedOnly = evaluationService.evaluateToList(detectedQuery, Integer.class, context);

            HashSet<Integer> detectedSet = new HashSet<Integer>(detectedOnly);

            patients.removeAll(detectedSet);

        }

        ret.setMemberIds(patients);

        return ret;
    }

}
