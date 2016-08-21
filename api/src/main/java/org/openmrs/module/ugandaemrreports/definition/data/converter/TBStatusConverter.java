/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.definition.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * TB status converter
 */
public class TBStatusConverter implements DataConverter {

    //***** CONSTRUCTORS *****

    /**
     * Default constructor
     */
    public TBStatusConverter() {
    }

    //***** INSTANCE METHODS *****

    /**
     * @see DataConverter#convert(Object)
     */
    public Object convert(Object original) {
        Obs o = (Obs) original;
        if (o != null) {
            int conceptId = o.getValueCoded().getConceptId();
            if (conceptId == 90079) {
                return "1";
            }
            if (conceptId == 90073) {
                return "2";
            }
            if (conceptId == 90078) {
                return "3";
            }
            if (conceptId == 90071) {
                return "4";
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