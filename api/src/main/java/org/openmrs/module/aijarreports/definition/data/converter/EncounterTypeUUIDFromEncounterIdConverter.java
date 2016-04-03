package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an Encounter ID to the corresponding Encounter Type uuid
 */
public class EncounterTypeUUIDFromEncounterIdConverter implements DataConverter {

    public EncounterTypeUUIDFromEncounterIdConverter() { }

    @Override
    public Object convert(Object original) {
    	if( original != null){
			EncounterService es  = Context.getEncounterService();
			Encounter e = es.getEncounter((Integer) original);
			return (e.getEncounterType() == null)? "": e.getEncounterType().getUuid();
		}else{
			return "";
		}
    }

    @Override
    public Class<?> getInputDataType() {
        return Integer.class;
    }

    @Override
    public Class<?> getDataType() {
        return Object.class;
    }
}
