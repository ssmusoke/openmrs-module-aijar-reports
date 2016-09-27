package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.openmrs.Person;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.common.DeathDate;
import org.openmrs.module.ugandaemrreports.definition.data.definition.DeathDateDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

/**
 * Created by carapai on 15/09/2016.
 */
@Handler(supports = DeathDateDataDefinition.class, order = 50)
public class DeathDateDataDefinitionEvaluator implements PersonDataEvaluator {

    @Autowired
    EvaluationService evaluationService;

    @Override
    public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context) throws EvaluationException {

        EvaluatedPersonData c = new EvaluatedPersonData(definition, context);

        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("p.personId", "p.deathDate");
        q.from(Person.class, "p");
        q.wherePersonIn("p.personId", context);

        List<Object[]> results = evaluationService.evaluateToList(q, context);

        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            Date deathDate = (Date) row[1];
            if (deathDate != null) {
                c.addData(pId, new DeathDate(deathDate));
            } else {
                c.addData(pId, null);
            }
        }

        return c;
    }
}
