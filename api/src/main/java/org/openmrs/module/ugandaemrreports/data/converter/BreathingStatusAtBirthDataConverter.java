package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class BreathingStatusAtBirthDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("911808cd-f455-4a63-9e18-aeee1f74adf0"))) {
            return "SS";
        } else if (value != null && value.equals(Dictionary.getConcept("e6004c96-2eaf-41f4-874e-6c3203bc1c40"))) {
            return "BM";
        }
        else if (value != null && value.equals(Dictionary.getConcept("d5ff53b2-1821-43e5-9abe-8c5e86d9639b"))) {
            return "BMD";
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
