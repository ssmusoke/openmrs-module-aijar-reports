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
import org.openmrs.module.reporting.common.Birthdate;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;

/**
 * Who Stage data converter
 */
public class BirthDateConverter implements DataConverter {

    /**
     * Default constructor
     */
    public BirthDateConverter() {
    }

    //***** INSTANCE METHODS *****

    /**
     * @see DataConverter#convert(Object)
     */
    public Object convert(Object original) {
        Birthdate o = (Birthdate) original;
        if (o != null) {
            return (new DateConverter("MMM d, yyyy")).convert(o.getBirthdate());
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
        return Birthdate.class;
    }
}