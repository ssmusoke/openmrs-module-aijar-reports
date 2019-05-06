package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.ugandaemrreports.common.DSDMModel;

/**
 */
public class DSDMModelDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
    public DSDMModelDataDefinition() {
        super();
    }

    public DSDMModelDataDefinition(String name) {
        super(name);
    }

    @Override
    public Class<?> getDataType() {
        return DSDMModel.class;
    }

}
