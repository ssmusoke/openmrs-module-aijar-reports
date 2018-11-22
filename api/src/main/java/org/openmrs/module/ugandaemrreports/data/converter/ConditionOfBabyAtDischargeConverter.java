package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class ConditionOfBabyAtDischargeConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs)obj).getValueCoded();
        if(value != null && value.equals(Dictionary.getConcept("fda5ad21-6ba4-4526-a0f3-ea1269d43422"))) {
            return "MSB";
        }
        else if(value != null && value.equals(Dictionary.getConcept("7a15616a-c12a-44fc-9a11-553639128b69"))) {
            return "FSB";
        }
        else if(value != null && value.equals(Dictionary.getConcept("ab3a7679-f5ee-48d6-b690-f55a1dfe95ea"))) {
            return "NND";
        }
        else if(value != null && value.equals(Dictionary.getConcept("eb7041a0-02e6-4e9a-9b96-ff65dd09a416"))) {
            return "AL";
        }
        else if(value != null && value.equals(Dictionary.getConcept("23ac7575-f0ea-49a5-855e-b3348ad1da01"))) {
            return "BDF";
        }
        else if(value != null && value.equals(Dictionary.getConcept("161936AAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "ICU";
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
