package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class RoutineMedicationDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("dc73ee9c-30ab-102d-86b0-7a5022ba4115"))) {
            return "TEO";
        } else if (value != null && value.equals(Dictionary.getConcept("64533b34-1c86-48e0-85a5-5d5a1aab97ce"))) {
            return "Vitamin K";
        }
        else if (value != null && value.equals(Dictionary.getConcept("5af778c9-ae5e-4fa7-8a6b-5e1844236691"))) {
            return "Chlorhexidine";
        }
        else if (value != null && value.equals(Dictionary.getConcept("5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "Other";
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
