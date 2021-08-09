package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 */
public class ClientCareStatusConverter implements DataConverter {

    public ClientCareStatusConverter() {
    }

    @Override
    public Object convert(Object original) {

        Person p = (Person)original;
        if (p != null) {
            if(p.getDead()){
                return "Dead";
            }else{
                if(p.getVoided()){
                    return "Transferred Out";
                }else{
                    return " In care";
                }
            }
        }
       return null;
    }

    /**
     * @see DataConverter#getDataType()
     */@Override
    public Class<?> getDataType() {
        return String.class;
    }

    /**
     * @see DataConverter#getInputDataType()
     */@Override
    public Class<?> getInputDataType() {
        return Concept.class;
    }

}
