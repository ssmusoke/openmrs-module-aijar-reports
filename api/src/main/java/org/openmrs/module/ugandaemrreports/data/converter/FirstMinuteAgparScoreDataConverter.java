package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class FirstMinuteAgparScoreDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Double value = ((Obs) obj).getValueNumeric();
        if (value != null && value.equals(Dictionary.getConcept("1"))) {
            return "1/10";
        } else if (value != null && value.equals(Dictionary.getConcept("2"))) {
            return "2/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("3"))) {
            return "3/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("4"))) {
            return "4/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("5"))) {
            return "5/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("6"))) {
            return "6/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("7"))) {
            return "7/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("8"))) {
            return "8/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("9"))) {
            return "9/10";
        }
        else if (value != null && value.equals(Dictionary.getConcept("10"))) {
            return "10/10";
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
