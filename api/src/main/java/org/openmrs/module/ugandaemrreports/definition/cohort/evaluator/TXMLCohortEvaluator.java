package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.api.CohortService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ActivesInCareCohortDefinition;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.TXMLCohortDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVCohortDefinitionLibrary;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

@Handler(supports = {TXMLCohortDefinition.class})
public class TXMLCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    private HIVCohortDefinitionLibrary hivCohortDefinitionLibrary;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context) throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        TXMLCohortDefinition cd = (TXMLCohortDefinition) cohortDefinition;

        CohortDefinitionService cohortDefinitionService = Context.getService(CohortDefinitionService.class);
        Map<String, Object> parameterValues = new HashMap<String, Object>();


        Calendar newEndDate = toCalendar(cd.getEndDate());
        Calendar newStartDate = toCalendar(cd.getStartDate());

        newEndDate.add(Calendar.MONTH, -3);
        Date previousPeriodEndDate = newEndDate.getTime();

        newStartDate.add(Calendar.MONTH, -3);
        Date previousPeriodStartDate = newStartDate.getTime();

        ActivesInCareCohortDefinition thisActivePeriod = new ActivesInCareCohortDefinition();
        thisActivePeriod.setLostToFollowupDays("28");
        thisActivePeriod.setStartDate(cd.getStartDate());
        thisActivePeriod.setEndDate(cd.getEndDate());
        Cohort activeInPeriod = cohortDefinitionService.evaluate(thisActivePeriod,context);

        ActivesInCareCohortDefinition previousPeriod = new ActivesInCareCohortDefinition();
        previousPeriod.setEndDate(previousPeriodEndDate);
        previousPeriod.setStartDate(previousPeriodStartDate);
        previousPeriod.setLostToFollowupDays("28");
        Cohort activeInPreviousPeriod = cohortDefinitionService.evaluate(previousPeriod,context);

        Set<Integer> tx_ml_set = new HashSet(CollectionUtils.subtract(activeInPreviousPeriod.getMemberIds(), activeInPeriod.getMemberIds()));


        ret.setMemberIds(tx_ml_set);
        return ret;


    }

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
