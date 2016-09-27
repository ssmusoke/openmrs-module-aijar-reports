package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.ugandaemrreports.definition.data.definition.EMTCTPatientDataDefinition;
import org.openmrs.module.ugandaemrreports.metadata.HIVMetadata;
import org.openmrs.module.reporting.common.ListMap;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by carapai on 15/07/2016.
 */
@Handler(supports = EMTCTPatientDataDefinition.class, order = 50)

public class EMTCTPatientDataDefinitionEvaluator implements PatientDataEvaluator {

    @Autowired
    private HIVMetadata hivMetadata;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {
        EMTCTPatientDataDefinition def = (EMTCTPatientDataDefinition) definition;
        EvaluatedPatientData c = new EvaluatedPatientData(def, context);
        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("o.personId", "o");
        q.from(Obs.class, "o");
        q.wherePersonIn("o.personId", context);
        q.whereEqual("o.concept", hivMetadata.getEDD());
        q.whereGreaterOrEqualTo("o.obsDatetime", def.getOnDate());
        q.groupBy("o.personId");
        q.groupBy("o.valueDatetime");

        List<Object[]> queryResult = evaluationService.evaluateToList(q, context);

        ListMap<Integer, Obs> obsForPatients = new ListMap<Integer, Obs>();

        for (Object[] row : queryResult) {
            obsForPatients.putInList((Integer) row[0], (Obs) row[1]);
        }

        for (Integer pId : obsForPatients.keySet()) {
            List<Obs> l = obsForPatients.get(pId);
            if(obsForPatients.size() > def.getPregnancyNo() && l.size() > def.getPregnancyNo()){
                Obs obs = l.get(def.getPregnancyNo());
                c.addData(pId, obs);
            }
        }
        return c;
    }
}
