package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class SexofBabyDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("1535AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Female";
        } else if (value != null && value.equals(Dictionary.getConcept("1534AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Male";
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
