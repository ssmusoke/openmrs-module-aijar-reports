 package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

 import java.util.Collection;
 import java.util.List;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.TransformerUtils;
 import org.apache.commons.lang.StringUtils;
 import org.openmrs.Concept;
 import org.openmrs.annotation.Handler;
 import org.openmrs.module.reporting.cohort.EvaluatedCohort;
 import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
 import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.EvaluationException;
 import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
 import org.openmrs.module.reporting.evaluation.service.EvaluationService;
 import org.openmrs.module.ugandaemrreports.definition.cohort.definition.BaselineClinicalStageCohortDefinition;
 import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
 import org.springframework.beans.factory.annotation.Autowired;




 @Handler(supports={BaselineClinicalStageCohortDefinition.class})
 public class BaselineClinicalStageCohortEvaluator
   implements CohortDefinitionEvaluator
 {
   @Autowired
   EvaluationService evaluationService;
   @Autowired
   HIVMetadata hivMetadata;

   public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
     throws EvaluationException
   {
     EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
     BaselineClinicalStageCohortDefinition cd = (BaselineClinicalStageCohortDefinition)cohortDefinition;

     Integer of = this.hivMetadata.getWHOClinicalStage().getConceptId();

     String query = "SELECT\n  A.patient_id\nFROM\n  (SELECT\n     e.patient_id,\n     e.encounter_id,\n     MIN(e.encounter_datetime)\n   FROM encounter e\n   WHERE e.encounter_type = (SELECT et.encounter_type_id\n                             FROM encounter_type et\n                             WHERE et.uuid = '8d5b2be0-c2cc-11de-8d13-0010c6dffd0f')\n   GROUP BY e.patient_id) A INNER JOIN obs o\n    ON (o.encounter_id = A.encounter_id AND o.concept_id = 90203)\n";

     if ((cd.getValues() != null) &&
       (cd.getValues().size() > 0)) {
       Collection conceptIds = CollectionUtils.collect(cd.getValues(), TransformerUtils.invokerTransformer("getConceptId"));
       String conceptIdsString = StringUtils.join(conceptIds, ',');
       query = query + String.format("WHERE o.value_coded IN (%s)", new Object[] { conceptIdsString });
     }


     SqlQueryBuilder q = new SqlQueryBuilder();
     q.append(query);

     List<Object[]> results = this.evaluationService.evaluateToList(q, context);

     for (Object[] row : results) {
       Integer pId = (Integer)row[0];
       ret.addMember(pId);
     }
     return ret;
   }
 }
