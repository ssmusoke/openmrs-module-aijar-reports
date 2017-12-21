package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.data.definition.EncounterObsDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Handler(supports = EncounterObsDataDefinition.class, order = 50)
public class EncounterObsDataEvaluator implements PatientDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition patientDataDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        EncounterObsDataDefinition def = (EncounterObsDataDefinition) patientDataDefinition;
        EvaluatedPatientData c = new EvaluatedPatientData(def, evaluationContext);

        if (evaluationContext.getBaseCohort() != null && evaluationContext.getBaseCohort().isEmpty()) {
            return c;
        }

        HqlQueryBuilder q = new HqlQueryBuilder();
        q.select("o.personId", "o");
        q.from(Obs.class, "o");
        q.whereEqual("o.encounter.encounterType", def.getEncounterType());
        q.whereEqual("o.voided", false);
        q.whereEqual("o.encounter.voided", false);
        if (def.getStartDate() != null && def.getEndDate() != null) {
            q.whereBetweenInclusive("o.encounter.encounterDatetime", def.getStartDate(), def.getEndDate());
        } else if (def.getStartDate() != null) {
            q.whereGreaterOrEqualTo("o.encounter.encounterDatetime", def.getStartDate());
        } else if (def.getEndDate() != null) {
            q.whereLessOrEqualTo("o.encounter.encounterDatetime", def.getStartDate());
        }
        q.orderAsc("o.encounter.encounterDatetime");

        List<Object[]> results = evaluationService.evaluateToList(q, evaluationContext);
        for (Object[] row : results) {
            Integer pId = (Integer) row[0];
            Obs o = (Obs) row[1];
            List<Obs> obsForPatient = (List<Obs>) c.getData().get(pId);
            if (obsForPatient == null) {
                obsForPatient = new ArrayList<>();
                c.getData().put(pId, obsForPatient);
            }
            obsForPatient.add(o);
        }
        return c;
    }
}
