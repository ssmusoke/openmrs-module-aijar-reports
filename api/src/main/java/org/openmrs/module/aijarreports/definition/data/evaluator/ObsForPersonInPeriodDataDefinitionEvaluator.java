package org.openmrs.module.aijarreports.definition.data.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.aijarreports.definition.data.definition.ObsForPersonInPeriodDataDefinition;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.ListMap;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 13/05/2016.
 */
@Handler(supports = ObsForPersonInPeriodDataDefinition.class, order = 50)
public class ObsForPersonInPeriodDataDefinitionEvaluator implements PersonDataEvaluator {
    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context) throws EvaluationException {
        ObsForPersonInPeriodDataDefinition def = (ObsForPersonInPeriodDataDefinition) definition;
        EvaluatedPersonData c = new EvaluatedPersonData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }

        Period period = def.getPeriod();
        Date beginPeriod = def.getStartDate();

        Date beginning = null;
        Date ending = null;

        if (period == Period.MONTHLY) {
            beginning = DateUtil.getStartOfMonth(beginPeriod);
            ending = DateUtil.getEndOfMonth(beginPeriod);
        } else if (period == period.QUARTERLY) {
            // dd
        } else if (period == period.YEARLY) {
            beginning = DateUtil.getStartOfYear(beginPeriod);
            ending = DateUtil.getEndOfYear(beginPeriod);
        }


        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("o.personId", "o");
        q.from(Obs.class, "o");
        q.wherePersonIn("o.personId", context);
        q.whereEqual("o.concept", def.getQuestion());
        q.whereIn("o.encounter.encounterType", def.getEncounterTypes());


        if (beginning != null) {
            q.whereGreaterOrEqualTo("o.obsDatetime", beginning);
        } else {
            q.whereLessOrEqualTo("o.obsDatetime", ending);
        }

        if (def.getAnswers() != null) {
            q.whereIn("o.valueCoded", def.getAnswers());
        }

        if (def.getValueDatetime() != null) {
            if (def.getRangeComparator() == RangeComparator.LESS_EQUAL) {
                q.whereLessOrEqualTo("o.valueDatetime", def.getValueDatetime());
            } else if (def.getRangeComparator() == RangeComparator.GREATER_EQUAL) {
                q.whereGreaterOrEqualTo("o.valueDatetime", def.getValueDatetime());
            } else {
                q.whereEqual("o.valueDatetime", def.getValueDatetime());
            }
        }

        if (def.getValueNumeric() != null) {
            if (def.getRangeComparator() == RangeComparator.LESS_EQUAL) {
                q.whereLessOrEqualTo("o.valueNumeric", def.getValueDatetime());
            } else if (def.getRangeComparator() == RangeComparator.GREATER_EQUAL) {
                q.whereGreaterOrEqualTo("o.valueNumeric", def.getValueDatetime());
            } else {
                q.whereEqual("o.valueNumeric", def.getValueDatetime());
            }
        }

        if (def.getWhichEncounter() == TimeQualifier.LAST) {
            q.orderDesc("o.obsDatetime");
        } else {
            q.orderAsc("o.obsDatetime");
        }

        List<Object[]> queryResult = evaluationService.evaluateToList(q, context);

        ListMap<Integer, Obs> obsForPatients = new ListMap<Integer, Obs>();
        for (Object[] row : queryResult) {
            obsForPatients.putInList((Integer) row[0], (Obs) row[1]);
        }

        for (Integer pId : obsForPatients.keySet()) {
            List<Obs> l = obsForPatients.get(pId);
            if (def.getWhichEncounter() == TimeQualifier.LAST || def.getWhichEncounter() == TimeQualifier.FIRST) {
                c.addData(pId, l.get(0));
            } else {
                c.addData(pId, l);
            }
        }

        return c;
    }
}
