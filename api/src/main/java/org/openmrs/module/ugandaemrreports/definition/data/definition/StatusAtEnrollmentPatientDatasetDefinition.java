package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;

import java.util.Map;

/**
 */
public class StatusAtEnrollmentPatientDatasetDefinition extends BaseDataDefinition implements PatientDataDefinition {

    public StatusAtEnrollmentPatientDatasetDefinition() {
        super();
    }

    public StatusAtEnrollmentPatientDatasetDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return Map.class;
    }
}
