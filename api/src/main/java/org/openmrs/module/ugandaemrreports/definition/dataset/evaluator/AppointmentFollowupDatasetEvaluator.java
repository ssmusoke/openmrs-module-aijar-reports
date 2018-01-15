package org.openmrs.module.ugandaemrreports.definition.dataset.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.ugandaemrreports.definition.data.definition.EncounterObsDataDefinition;
import org.openmrs.module.ugandaemrreports.definition.dataset.definition.AppointmentFollowupDatasetDefinition;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Metadata;

/**
 */
@Handler(supports = {AppointmentFollowupDatasetDefinition.class})
public class AppointmentFollowupDatasetEvaluator implements DataSetEvaluator {

    @Override
    public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
        SimpleDataSet dataSet = new SimpleDataSet(dataSetDefinition, context);
        AppointmentFollowupDatasetDefinition definition = (AppointmentFollowupDatasetDefinition) dataSetDefinition;

        EncounterObsDataDefinition eidEncounterObsDefinition = new EncounterObsDataDefinition();
        eidEncounterObsDefinition.setEncounterType(Dictionary.getEncounterType(Metadata.EncounterType.APPOINTMENT_FOLLOWUP_ENCOUNTER));

        EvaluatedPatientData eidEvaluatedEncounterObs = Context.getService(PatientDataService.class).evaluate(eidEncounterObsDefinition, context);

        return dataSet;
    }
}
