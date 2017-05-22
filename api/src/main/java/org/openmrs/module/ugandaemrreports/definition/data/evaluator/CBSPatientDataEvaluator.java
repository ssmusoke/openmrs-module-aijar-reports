 package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

 import com.google.common.base.Joiner;
 import java.util.List;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.LocalDate;
 import org.openmrs.Cohort;
 import org.openmrs.EncounterType;
 import org.openmrs.annotation.Handler;
 import org.openmrs.module.reporting.common.ObjectUtil;
 import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
 import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
 import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
 import org.openmrs.module.reporting.evaluation.EvaluationContext;
 import org.openmrs.module.reporting.evaluation.EvaluationException;
 import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
 import org.openmrs.module.reporting.evaluation.service.EvaluationService;
 import org.openmrs.module.ugandaemrreports.common.PatientMonthData;
 import org.openmrs.module.ugandaemrreports.common.StubDate;
 import org.openmrs.module.ugandaemrreports.definition.data.definition.CBSPatientDataDefinition;
 import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
 import org.springframework.beans.factory.annotation.Autowired;

 @Handler(supports={CBSPatientDataDefinition.class})
 public class CBSPatientDataEvaluator
   implements PatientDataEvaluator
 {
   protected static final Log log = LogFactory.getLog(CBSPatientDataEvaluator.class);

   @Autowired
   private EvaluationService evaluationService;
   @Autowired
   private HIVMetadata hivMetadata;

   public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext evaluationContext)
     throws EvaluationException
   {
     CBSPatientDataDefinition def = (CBSPatientDataDefinition)definition;

     EvaluatedPatientData c = new EvaluatedPatientData(def, evaluationContext);

     LocalDate workingDate = StubDate.dateOf(def.getStartDate());

     String startDateString = workingDate.toString("yyyy-MM-dd");

     String membersString = Joiner.on(",").join(def.getCohort().getMemberIds());

     evaluationContext = (EvaluationContext)ObjectUtil.nvl(evaluationContext, new EvaluationContext());

     Integer encounter = this.hivMetadata.getARTEncounterEncounterType().getEncounterTypeId();
     Integer summary = this.hivMetadata.getARTSummaryEncounter().getEncounterTypeId();


     String query = "SELECT\n  patient_id,\n" + String.format("  TIMESTAMPDIFF(MONTH, encounter_datetime, DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY) AS art_start,\n", new Object[] { startDateString }) + "  1                                                                                                    AS other\n" + "FROM encounter\n" + String.format("WHERE patient_id IN (%s) AND encounter_type = %s AND\n", new Object[] { membersString, encounter }) + String.format("      encounter_datetime BETWEEN DATE_ADD('%s', INTERVAL 6 MONTH) AND\n", new Object[] { startDateString }) + String.format("      DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY\n", new Object[] { startDateString }) + "UNION ALL\n" + "SELECT\n" + "  person_id,\n" + String.format("  TIMESTAMPDIFF(MONTH, value_datetime, DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY) AS art_start,\n", new Object[] { startDateString }) + "  2                                                                                                AS other\n" + "FROM obs\n" + "WHERE\n" + String.format("  person_id IN (%s) AND concept_id = 5096 AND value_datetime BETWEEN DATE_ADD('%s', INTERVAL 6 MONTH) AND\n", new Object[] { membersString, startDateString }) + String.format("  DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY\n", new Object[] { startDateString }) + "UNION ALL\n" + "SELECT\n" + "  person_id,\n" + String.format("  TIMESTAMPDIFF(MONTH, death_date, DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY) AS art_start,\n", new Object[] { startDateString }) + "  3                                                                                            AS other\n" + "FROM person\n" + String.format("WHERE person_id IN (%s) AND death_date BETWEEN DATE_ADD('%s', INTERVAL 6 MONTH) AND\n", new Object[] { membersString, startDateString }) + String.format("DATE_ADD('%s', INTERVAL 75 MONTH) - INTERVAL 1 DAY", new Object[] { startDateString });

     Integer begin;
     if (def.getCohort().size() != 0) {
       SqlQueryBuilder q = new SqlQueryBuilder(query);

       List<Object[]> results = this.evaluationService.evaluateToList(q, evaluationContext);
       begin = Integer.valueOf(1);
       for (Object[] row : results) {
         Integer patientId = Integer.valueOf(String.valueOf(row[0]));
         Integer month = Integer.valueOf(String.valueOf(row[1]));
         Integer type = Integer.valueOf(String.valueOf(row[2]));
         c.addData(begin, new PatientMonthData(patientId, month, type));
         begin = Integer.valueOf(begin.intValue() + 1);
       }
     }
     return c;
   }
 }
