package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class ARVsAdministerdToBabyDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("dc9b0596-30ab-102d-86b0-7a5022ba4115"))) {
            return "NA";
        }
        else if (value != null && value.equals(Dictionary.getConcept("04711d8f-c60d-4b1c-9451-0b32debbb8b0"))) {
            return "NVP Syrup";
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
