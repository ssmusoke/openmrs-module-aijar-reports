package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class SkinContactDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        Concept value = ((Obs) obj).getValueCoded();
        if (value == null) {
            return "";
        }

        if (value != null && value.equals(Dictionary.getConcept("dcd695dc-30ab-102d-86b0-7a5022ba4115"))) {
            return "Yes";
        } else if (value != null && value.equals(Dictionary.getConcept("dcd69c06-30ab-102d-86b0-7a5022ba4115"))) {
            return "No";
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
