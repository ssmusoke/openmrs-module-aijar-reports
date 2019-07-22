package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

public class TestedForMoreThanOnceInLast12MonthsConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "NO";
        }
        Double value  = ((Obs)obj).getValueNumeric();
        if(value != null && value > 1 ) {
            return "YES";
        }
        else if(value != null && value < 1) {
            return "NO";
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

