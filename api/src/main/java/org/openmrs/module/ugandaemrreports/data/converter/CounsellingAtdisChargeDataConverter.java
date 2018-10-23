package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class CounsellingAtdisChargeDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs) obj).getValueCoded();
        if (value != null && value.equals(Dictionary.getConcept("162936"))) {
            return "Counselled";
        } else if (value != null && value.equals(Dictionary.getConcept("162937"))) {
            return "Not Counselled";
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
