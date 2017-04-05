package org.openmrs.module.ugandaemrreports.definition.data.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.ugandaemrreports.definition.data.definition.StatusAtEnrollmentPatientDatasetDefinition;
import org.openmrs.module.ugandaemrreports.library.HIVPatientDataLibrary;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.PatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by carapai on 16/05/2016.
 */
@Handler(supports = StatusAtEnrollmentPatientDatasetDefinition.class, order = 50)

public class StatusAtEnrollmentPatientDatasetDefinitionEvaluator implements PatientDataEvaluator {

    @Autowired
    private HIVPatientDataLibrary hivLibrary;

    @Autowired
    private PatientDataService patientDataService;

    @Override
    public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context) throws EvaluationException {

        EvaluatedPatientData pd = new EvaluatedPatientData(definition, context);

        Map<String, PatientDataDefinition> m = new LinkedHashMap<String, PatientDataDefinition>();
        m.put("TI", hivLibrary.getFirstTransferIn());
        m.put("PREGNANT", hivLibrary.getFirstPregnant());
        m.put("TB", hivLibrary.getFirstTB());
        m.put("LACTATING", hivLibrary.getFirstLactating());
        m.put("EID", hivLibrary.getFirstEID());

        for (String questionKey : m.keySet()) {
            PatientDataDefinition def = m.get(questionKey);
            def.addParameter(new Parameter("onDate", "On Date", Date.class));
            PatientData data = patientDataService.evaluate(Mapped.mapStraightThrough(def), context);
            for (Map.Entry<Integer, Object> e : data.getData().entrySet()) {
                Map<String, Object> reasonsForPatient = (Map<String, Object>) pd.getData().get(e.getKey());
                if (reasonsForPatient == null) {
                    reasonsForPatient = new LinkedHashMap<String, Object>();
                    pd.getData().put(e.getKey(), reasonsForPatient);
                }
                reasonsForPatient.put(questionKey, e.getValue());
            }
        }

        return pd;
    }
}
