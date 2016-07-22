package org.openmrs.module.aijarreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.LocalDate;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.common.Periods;
import org.openmrs.module.aijarreports.common.StubDate;
import org.openmrs.module.aijarreports.definition.data.definition.ObsForPersonInPeriodDataDefinition;
import org.openmrs.module.aijarreports.metadata.HIVMetadata;
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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by carapai on 13/05/2016.
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

        Period encounterPeriod = def.getEncounterPeriod();

        Date anotherDate = def.getOnDate();

        Period obsPeriod = def.getObsPeriod();

        LocalDate workingDate = StubDate.dateOf(DateUtil.formatDate(anotherDate, "yyyy-MM-dd"));

        LocalDate localEncounterStartDate = null;
        LocalDate localEncounterEndDate = null;

        LocalDate localObsStartDate = null;
        LocalDate localObsEndDate = null;

        HqlQueryBuilder encounterQuery = new HqlQueryBuilder();

        encounterQuery.select(new String[]{"e.encounterId"});

        encounterQuery.from(Encounter.class, "e");

        if (encounterPeriod != null) {
            if (def.getPeriodToAdd() > 0) {
                if (encounterPeriod == Period.QUARTERLY) {
                    List<LocalDate> dates = Periods.addQuarters(workingDate, def.getPeriodToAdd());
                    localEncounterStartDate = dates.get(0);
                    localEncounterEndDate = dates.get(1);
                } else if (encounterPeriod == Period.MONTHLY) {
                    List<LocalDate> dates = Periods.addMonths(workingDate, def.getPeriodToAdd());
                    localEncounterStartDate = dates.get(0);
                    localEncounterEndDate = dates.get(1);
                }


            } else {
                if (encounterPeriod == Period.QUARTERLY) {
                    localEncounterStartDate = Periods.quarterStartFor(workingDate);
                    localEncounterEndDate = Periods.quarterEndFor(workingDate);
                } else if (encounterPeriod == Period.MONTHLY) {
                    localEncounterStartDate = Periods.monthStartFor(workingDate);
                    localEncounterEndDate = Periods.monthEndFor(workingDate);
                }
            }

            encounterQuery.whereBetweenInclusive("e.encounterDatetime", localEncounterStartDate.toDate(), localEncounterEndDate.toDate());
        }

        if (def.getWhichEncounter() != null && def.getWhichEncounter() == TimeQualifier.FIRST) {
            encounterQuery.orderAsc("e.encounterDatetime");
        } else if (def.getWhichEncounter() != null && def.getWhichEncounter() == TimeQualifier.LAST) {
            encounterQuery.orderDesc("e.encounterDatetime");

        }

        HqlQueryBuilder artStartQuery = new HqlQueryBuilder();
        artStartQuery.select("o.personId", "MIN(o.obsDatetime)", "MIN(o.valueDatetime)");
        artStartQuery.from(Obs.class, "o");
        artStartQuery.wherePersonIn("o.personId", context);
        artStartQuery.whereIn("o.concept", hivMetadata.getConceptList("90315,99161,99061,99064,99269"));
        artStartQuery.groupBy("o.personId");

        List<Integer> encounters = this.evaluationService.evaluateToList(encounterQuery, Integer.class, context);

        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("o.personId", "o");
        q.from(Obs.class, "o");
        q.wherePersonIn("o.personId", context);

        if (def.getQuestion() != null) {
            q.whereEqual("o.concept", def.getQuestion());
        }

        if (def.isEncountersInclusive()) {
            q.whereIdIn("o.encounter", encounters);
        }

        if (def.getEncounterTypes() != null) {
            if (!def.getEncounterTypes().isEmpty())
                q.whereIn("o.encounter.encounterType", def.getEncounterTypes());
        }

        if (def.getAnswers() != null) {
            q.whereIn("o.valueCoded", def.getAnswers());
        }

        if (obsPeriod != null) {
            if (def.getPeriodToAdd() > 0) {
                if (obsPeriod == Period.MONTHLY) {
                    List<LocalDate> dates = Periods.addMonths(workingDate, def.getPeriodToAdd());
                    localObsStartDate = dates.get(0);
                    localObsEndDate = dates.get(1);
                } else if (obsPeriod == Period.QUARTERLY) {
                    List<LocalDate> dates = Periods.addQuarters(workingDate, def.getPeriodToAdd());
                    localObsStartDate = dates.get(0);
                    localObsEndDate = dates.get(1);
                }
            } else {
                if (obsPeriod == Period.MONTHLY) {
                    localObsStartDate = Periods.monthStartFor(workingDate);
                    localObsEndDate = Periods.monthEndFor(workingDate);
                } else if (obsPeriod == Period.QUARTERLY) {
                    localObsStartDate = Periods.quarterStartFor(workingDate);
                    localObsEndDate = Periods.quarterEndFor(workingDate);
                }
            }

            if (def.isValueDatetime()) {
                q.whereBetweenInclusive("o.valueDatetime", localObsStartDate.toDate(), localObsEndDate.toDate());
            } else {
                q.whereBetweenInclusive("o.obsDatetime", localObsStartDate.toDate(), localObsEndDate.toDate());
            }

        }


        if (def.getWhichObs() == TimeQualifier.LAST) {
            q.orderDesc("o.obsDatetime");
        } else {
            q.orderAsc("o.obsDatetime");
        }

        q.groupBy("o.personId");


        List<Object[]> queryResult = evaluationService.evaluateToList(q, context);

        ListMap<Integer, Obs> obsForPatients = new ListMap<Integer, Obs>();

        for (Object[] row : queryResult) {
            obsForPatients.putInList((Integer) row[0], (Obs) row[1]);
        }

        Map<Integer, Map<Integer, Date>> m = new HashMap<Integer, Map<Integer, Date>>();


        for (Integer pId : obsForPatients.keySet()) {
            List<Obs> l = obsForPatients.get(pId);
            Obs obs = l.get(0);
            c.addData(pId, obs);
        }

        return c;
    }
}
