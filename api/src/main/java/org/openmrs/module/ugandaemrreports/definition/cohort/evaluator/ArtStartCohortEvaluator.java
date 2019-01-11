package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.joda.time.LocalDate;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtStartCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 */
@Handler(supports = {ArtStartCohortDefinition.class})
public class ArtStartCohortEvaluator implements CohortDefinitionEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Autowired
    HIVMetadata hivMetadata;

    @Override
    public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
            throws EvaluationException {
        EvaluatedCohort ret = new EvaluatedCohort(cohortDefinition, context);
        ArtStartCohortDefinition cd = (ArtStartCohortDefinition) cohortDefinition;

        Date artDate = cd.getStartDate();
        LocalDate beginningDate = StubDate.dateOf(artDate);

        String startDate = DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd");

        List<Date> dates = getDates(beginningDate, cd.getPeriod(), cd.getPeriodInterval(), cd.getPeriodDifference());

        HqlQueryBuilder obsQuery = new HqlQueryBuilder();
        obsQuery.select("o.person.personId");
        obsQuery.from(Obs.class, "o");
        obsQuery.whereEqual("o.concept", hivMetadata.getArtStartDate());

        if (dates.size() > 1) {
            obsQuery.whereBetweenInclusive("o.valueDatetime", dates.get(0), dates.get(1));
        } else {
            obsQuery.whereEqual("o.valueDatetime", dates.get(0));
        }
        obsQuery.whereEqual("o.person.personVoided", false);

//        period on art  query
        String sql =String.format("select person_id from obs o  where timestampdiff(month ,value_datetime,'%s') >= '%d' and concept_id = \n",startDate,cd.getPeriodDifference()) +
                "(select concept_id from concept where concept.uuid='ab505422-26d9-41f1-a079-c3d222000440') and o.voided= 0 group by person_id ";

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(sql);

        if(cd.getPeriodDifference()!=null && cd.getStartDate()!=null){
            List<Integer> patients=this.evaluationService.evaluateToList(q, Integer.class, context);
            ret.getMemberIds().addAll(patients);
        }else{

            List<Integer> pIds = this.evaluationService.evaluateToList(obsQuery, Integer.class, context);
            ret.getMemberIds().addAll(pIds);}
        return ret;
    }
}
