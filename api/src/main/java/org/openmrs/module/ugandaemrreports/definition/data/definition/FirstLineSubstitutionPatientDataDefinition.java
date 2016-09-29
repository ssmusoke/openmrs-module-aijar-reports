package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

/**
 * Created by carapai on 13/09/2016.
 */
public class FirstLineSubstitutionPatientDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
    @Override
    public Class<?> getDataType() {
        return Obs.class;
    }

    @ConfigurationProperty
    private int substitutionOrSwitchNo = 1;
    @ConfigurationProperty
    private Concept what;

    public int getSubstitutionOrSwitchNo() {
        return substitutionOrSwitchNo;
    }

    public void setSubstitutionOrSwitchNo(int substitutionOrSwitchNo) {
        this.substitutionOrSwitchNo = substitutionOrSwitchNo;
    }

    public Concept getWhat() {
        return what;
    }

    public void setWhat(Concept what) {
        this.what = what;
    }
}
