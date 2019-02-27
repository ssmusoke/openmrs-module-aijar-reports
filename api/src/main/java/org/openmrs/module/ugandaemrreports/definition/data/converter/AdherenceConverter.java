package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.common.Adherence;

import java.util.Comparator;
import java.util.List;

/**
 */
public class AdherenceConverter implements DataConverter {

    private Integer number;

    public AdherenceConverter() {
    }

    public AdherenceConverter(Integer number) {
        this.number = number;
    }

    public Object convert(Object original) {
        List<Adherence> o = (List) original;
        if (o != null) {
            o.sort(Comparator.comparing(Adherence::getObsDatetime).reversed());

            if (o.size() > this.number) {
                return o.get(this.number).getName();
            }
        }
        return null;
    }

    /**
     * @see DataConverter#getDataType()
     */
    public Class<?> getDataType() {
        return String.class;
    }

    /**
     * @see DataConverter#getInputDataType()
     */
    public Class<?> getInputDataType() {
        return Concept.class;
    }
}
