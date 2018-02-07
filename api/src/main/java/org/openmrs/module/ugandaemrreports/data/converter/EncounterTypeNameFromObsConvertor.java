package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Returns an encounter type name from an Obs
 */
public class EncounterTypeNameFromObsConvertor implements DataConverter{
    @Override
    public Object convert(Object obj) {
        if (obj == null) {
            return "";
        }

        Obs obs = ((Obs) obj);
        return obs.getEncounter().getEncounterType().getName();
    }

    @Override
    public Class<?> getInputDataType() {
        return Obs.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }
}
