package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
//import org.openmrs.module.ugandaemrreports.common.Period;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.common.Periods;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ObsForPersonInPeriodDataDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ListMap;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 */
@Handler(supports = ObsForPersonInPeriodDataDefinition.class, order = 50)
public class ObsForPersonInPeriodDataDefinitionEvaluator implements PatientDataEvaluator {
    protected static final Log log = LogFactory.getLog(ObsForPersonInPeriodDataDefinitionEvaluator.class);

    @Autowired
    private EvaluationService evaluationService;

    @Autowired
    private HIVMetadata hivMetadata;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        ObsForPersonInPeriodDataDefinition def = (ObsForPersonInPeriodDataDefinition) definition;

        EvaluatedPatientData c = new EvaluatedPatientData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        Map<Integer, Date> m = new HashMap<Integer, Date>();

        Enums.Period period = def.getPeriod();

        LocalDate workingDate = StubDate.dateOf(DateUtil.formatDate(def.getStartDate(), "yyyy-MM-dd"));

        List<LocalDate> periods = Periods.getDatesDuringPeriods(workingDate, def.getPeriodToAdd(), period);

        LocalDate localEncounterStartDate = periods.get(0);
        LocalDate localEncounterEndDate = periods.get(1);


        HqlQueryBuilder encounterQuery = new HqlQueryBuilder();

        if (def.getWhichEncounter() != null && def.getWhichEncounter() == TimeQualifier.FIRST) {
            encounterQuery.select(new String[]{"e.encounterId", "e.patient.patientId", "MIN(e.encounterDatetime)"});
        } else if (def.getWhichEncounter() != null && def.getWhichEncounter() == TimeQualifier.LAST) {
            encounterQuery.select(new String[]{"e.encounterId", "e.patient.patientId", "MAX(e.encounterDatetime)"});
        } else {
            encounterQuery.select(new String[]{"e.encounterId", "e.patient.patientId", "e.encounterDatetime"});
        }

        encounterQuery.from(Encounter.class, "e");

        if (period != null) {
            encounterQuery.groupBy("e.patient.patientId");
        }

        encounterQuery.whereBetweenInclusive("e.encounterDatetime", localEncounterStartDate.toDate(), localEncounterEndDate.toDate());

        Set<Integer> encounters = getEncounterIds(encounterQuery, context);


        HqlQueryBuilder artStartQuery = new HqlQueryBuilder();
        artStartQuery.select("o.personId", "MIN(o.valueDatetime)");
        artStartQuery.from(Obs.class, "o");
        artStartQuery.wherePersonIn("o.personId", context);
        artStartQuery.whereIn("o.concept", hivMetadata.getConceptList("99161"));
        artStartQuery.groupBy("o.personId");


        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("o.personId", "o");
        q.from(Obs.class, "o");
        q.wherePersonIn("o.personId", context);

        if (def.getQuestion() != null) {
            q.whereEqual("o.concept", def.getQuestion());
        }

        q.whereIdIn("o.encounter", encounters);


        if (def.getAnswers() != null) {
            q.whereIn("o.valueCoded", def.getAnswers());
        }

        //q.whereBetweenInclusive("o.obsDatetime", localEncounterStartDate.toDate(), localEncounterEndDate.toDate());

        q.groupBy("o.personId");


        List<Object[]> queryResult = evaluationService.evaluateToList(q, context);

        ListMap<Integer, Obs> obsForPatients = new ListMap<Integer, Obs>();

        if (period == Enums.Period.QUARTERLY) {
            m = getPatientDateMap(artStartQuery, context);
        }

        for (Object[] row : queryResult) {
            obsForPatients.putInList((Integer) row[0], (Obs) row[1]);
        }

        for (Integer pId : obsForPatients.keySet()) {
            List<Obs> l = obsForPatients.get(pId);
            Obs obs = l.get(0);

            if (period == Enums.Period.QUARTERLY) {
                /*if (m.containsKey(pId)) {
                    Date date = m.get(pId);
                    Date date2 = obs.getObsDatetime();

                    if (date2.before(date)) {
                        c.addData(pId, obs);
                    }
                }*/
                c.addData(pId, obs);
            } else {
                c.addData(pId, obs);
            }
        }

        return c;
    }

    protected Map<Integer, Date> getPatientDateMap(HqlQueryBuilder query, EvaluationContext context) {
        Map<Integer, Date> m = new HashMap<Integer, Date>();
        List<Object[]> queryResults = evaluationService.evaluateToList(query, context);
        for (Object[] row : queryResults) {
            m.put((Integer) row[0], (Date) row[1]);
        }
        return m;
    }

    protected Set<Integer> getEncounterIds(HqlQueryBuilder query, EvaluationContext context) {
        Set<Integer> m = new HashSet<Integer>();
        List<Object[]> queryResults = evaluationService.evaluateToList(query, context);
        for (Object[] row : queryResults) {
            m.add((Integer) row[0]);
        }
        return m;
    }
}
