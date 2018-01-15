package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.ugandaemrreports.definition.data.definition.EncounterObsDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.data.definition.Obs4EncounterDataDefinition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

@Handler(supports = Obs4EncounterDataDefinition.class, order = 50)
public class Obs4EncounterDataEvaluator implements PatientDataEvaluator {

    @Autowired
    private EvaluationService evaluationService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition patientDataDefinition, EvaluationContext evaluationContext) throws EvaluationException {

        Obs4EncounterDataDefinition def = (Obs4EncounterDataDefinition) patientDataDefinition;
        EvaluatedPatientData c = new EvaluatedPatientData(def, evaluationContext);

        SqlQueryBuilder q = new SqlQueryBuilder();
        q.append(query);
        return c;
    }
}
