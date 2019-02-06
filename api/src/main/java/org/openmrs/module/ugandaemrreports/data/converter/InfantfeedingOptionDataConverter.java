package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class InfantfeedingOptionDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("dcbd637e-30ab-102d-86b0-7a5022ba4115"))) {
            return "EBF";
        } else if (value != null && value.equals(Dictionary.getConcept("40fdb5b6-e8ac-424d-988c-f2f2937348db"))) {
            return "RP";
        } else if (value != null && value.equals(Dictionary.getConcept("dcd5487d-30ab-102d-86b0-7a5022ba4115"))) {
            return "MF";
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
