package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

/**
 * Created by codehub on 7/11/17.
 */
public class SmcProcedureDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Obs obs = ((Obs) obj);
        if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("e63ac8e3-5027-43c3-9421-ce995ea039cf"))){
            return 1;
        }
        else if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("0308bd0a-0e28-4c62-acbd-5ea969c296db"))){
            return 3;
        }
        else if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("0ee1b2ae-2961-41d6-9fe0-7d9f876232ae"))){
            return 2;
        }
        else if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("dcd68a88-30ab-102d-86b0-7a5022ba4115"))){
            return 5;
        }
        return null;
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