package org.openmrs.module.aijarreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;

import java.util.Map;

/**
 * Created by carapai on 16/05/2016.
 */
public class WhyEligibleForARTPatientDatasetDefinition extends BaseDataDefinition implements PatientDataDefinition {

    public WhyEligibleForARTPatientDatasetDefinition() {
        super();
    }

    public WhyEligibleForARTPatientDatasetDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return Map.class;
    }
}
