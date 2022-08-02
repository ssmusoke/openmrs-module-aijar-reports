package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

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


        Calendar newEndDate = toCalendar(cd .getEndDate());
        newEndDate.add(Calendar.MONTH, -3);
        Date previousPeriodEndDate = newEndDate.getTime();

        Calendar newStartDate = toCalendar(cd .getStartDate());
        newStartDate.add(Calendar.MONTH, -3);
        Date previousPeriodStartDate = newStartDate.getTime();
        EvaluationContext myPreviouscontext =new EvaluationContext();
        parameterValues.put("startDate",previousPeriodStartDate);
        parameterValues.put("endDate", previousPeriodEndDate);
        myPreviouscontext.setParameterValues(parameterValues);

        Cohort activeInPreviousPeriod= cohortDefinitionService.evaluate(hivCohortDefinitionLibrary.getActivePatientsWithLostToFollowUpAsByDays("28"),myPreviouscontext);
        ret.setMemberIds(activeInPreviousPeriod.getMemberIds());
        return ret;


    }

    public static Calendar toCalendar(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }
}
