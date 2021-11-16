package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.ObsForPersonDataEvaluator;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.definition.data.definition.ReturnVisitDatePatientDataDefinition;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;


/**
 */
@Handler(supports = ReturnVisitDatePatientDataDefinition.class, order = 2)
public class ReturnVisitDatePatientDataEvaluator implements PersonDataEvaluator {
    protected static final Log log = LogFactory.getLog(ReturnVisitDatePatientDataEvaluator.class);

    @Autowired
    private ObsForPersonDataEvaluator obsForPersonDataEvaluator;


    @Override
    public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context) throws EvaluationException {
        ReturnVisitDatePatientDataDefinition def = (ReturnVisitDatePatientDataDefinition) definition;

        EvaluatedPersonData c = new EvaluatedPersonData(def, context);

        if (context.getBaseCohort() != null && context.getBaseCohort().isEmpty()) {
            return c;
        }


        EvaluationContext ctx = context.shallowCopy();
        Location location;
        location=def.getLocation();
        ObsForPersonDataDefinition obsForPersonDataDefinition = def;

        if(location!=null){

            /*adding up ART Clinic Location with data that has no location attached */
            if(!location.getUuid().equals("86863db4-6101-4ecf-9a86-5e716d6504e4")){
                obsForPersonDataDefinition.setLocationList(Arrays.asList(location));
            }
        }

        c =  obsForPersonDataEvaluator.evaluate(obsForPersonDataDefinition,ctx);

        return c;
    }
}
