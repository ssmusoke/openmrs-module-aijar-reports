package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 */
public class NutritionalStatusConverter implements DataConverter {

    //***** CONSTRUCTORS *****

    /**
     * Default constructor
     */
    public NutritionalStatusConverter() {
    }

    //***** INSTANCE METHODS *****

    /**
     * @see DataConverter#convert(Object)
     */
    public Object convert(Object original) {
        Obs o = (Obs) original;
        if (o != null) {
            int conceptId = o.getValueCoded().getConceptId();
            if (conceptId == 99271) {
                return "MAM";
            }
            if (conceptId == 99272) {
                return "SAM";
            }
            if (conceptId == 99273) {
                return "SAMO";
            }
            if (conceptId == 99473) {
                return "PWG/PA";
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
