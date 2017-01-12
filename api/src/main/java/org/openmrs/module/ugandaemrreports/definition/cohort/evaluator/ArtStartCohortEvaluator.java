package org.openmrs.module.ugandaemrreports.definition.cohort.evaluator;

import org.joda.time.LocalDate;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.CohortDefinitionEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.Enums;
import org.openmrs.module.ugandaemrreports.common.Periods;
import org.openmrs.module.ugandaemrreports.common.StubDate;
import org.openmrs.module.ugandaemrreports.definition.cohort.definition.ArtStartCohortDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.openmrs.module.ugandaemrreports.common.Helper.getDates;

/**
 * Created by carapai on 20/04/2016.
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
        /*SqlQueryBuilder q = new SqlQueryBuilder();
        String query = String.format("select F.person_id from (select C.person_id,e.encounter_datetime as dt from (select A.person_id,B.encounter_id from (select person_id from obs where person_id not in (select person_id from obs where concept_id = 99161) group by person_id) A inner join (select person_id,min(encounter_id) as encounter_id from obs where concept_id = 90315 group by person_id) B on(A.person_id = B.person_id))C inner join encounter e on(e.encounter_id = C.encounter_id) union all select person_id,value_datetime as dt FROM obs WHERE concept_id = 99161) F where F.dt BETWEEN %s AND %s", DateUtil.formatDate(cd.getStartDate(), "yyyy-MM-dd"), DateUtil.formatDate(cd.getEndDate(), "yyyy-MM-dd"));
        q.append(query);
        Cohort c = new Cohort(evaluationService.evaluateToList(q, Integer.class, context));
        return new EvaluatedCohort(c, cd, context);*/

        Date artDate = cd.getStartDate();
        LocalDate beginningDate = StubDate.dateOf(artDate);

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

        List<Integer> pIds = this.evaluationService.evaluateToList(obsQuery, Integer.class, context);
        ret.getMemberIds().addAll(pIds);
        return ret;
    }
}
