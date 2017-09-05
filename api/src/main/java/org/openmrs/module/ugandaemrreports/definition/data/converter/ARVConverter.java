package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 */
public class ARVConverter implements DataConverter {

    public ARVConverter() {
    }

    public Object convert(Object original) {
        Obs o = (Obs) original;
        if (o != null) {
            int conceptId = o.getValueCoded().getConceptId();

            if (conceptId == 99015) {
                return "1a";
            }
            if (conceptId == 99016) {
                return "1b";
            }
            if (conceptId == 99005) {
                return "1c";
            }
            if (conceptId == 99006) {
                return "1d";
            }
            if (conceptId == 99039) {
                return "1e";
            }
            if (conceptId == 99040) {
                return "1f";
            }
            if (conceptId == 99041) {
                return "1g";
            }
            if (conceptId == 99042) {
                return "1h";
            }
            if (conceptId == 99007) {
                return "2a2";
            }
            if (conceptId == 99008) {
                return "2a4";
            }
            if (conceptId == 99044) {
                return "2b";
            }
            if (conceptId == 99043) {
                return "2c";
            }
            if (conceptId == 99282) {
                return "2d2";
            }
            if (conceptId == 99283) {
                return "2d4";
            }
            if (conceptId == 99046) {
                return "2e";
            }
            if (conceptId == 99017) {
                return "5a";
            }
            if (conceptId == 99018) {
                return "5b";
            }
            if (conceptId == 99045) {
                return "5f";
            }
            if (conceptId == 99284) {
                return "5g";
            }
            if (conceptId == 99285) {
                return "5h";
            }
            if (conceptId == 99286) {
                return "5j";
            }
            if (conceptId == 90002) {
                return "othr";
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
