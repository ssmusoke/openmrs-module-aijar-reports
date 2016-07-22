package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Created by carapai on 06/07/2016.
 */
public class FunctionalStatusConverter implements DataConverter {

    //***** CONSTRUCTORS *****

    /**
     * Default constructor
     */
    public FunctionalStatusConverter() {
    }

    //***** INSTANCE METHODS *****

    /**
     * @see DataConverter#convert(Object)
     */
    public Object convert(Object original) {
        Obs o = (Obs) original;
        if (o != null) {
            int conceptId = o.getValueCoded().getConceptId();
            if (conceptId == 90037) {
                return "A";
            }
            if (conceptId == 90038) {
                return "W";
            }
            if (conceptId == 90039) {
                return "B";
            }
            return ObjectUtil.format(o.getValueCoded());
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
