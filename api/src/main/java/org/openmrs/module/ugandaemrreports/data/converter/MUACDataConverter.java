/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.ugandaemrreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

/**
 */
public class MUACDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs)obj).getValueCoded();
        if(value != null && value.equals(Dictionary.getConcept("a3b1734c-4743-4b9d-8e71-08d0459d29b9"))) {
            return "Y";
        }
        else if(value != null && value.equals(Dictionary.getConcept("de330d01-5586-4eed-a645-e04b6bd13701"))) {
            return "R";
        }
        else if(value != null && value.equals(Dictionary.getConcept("8846c03f-67bf-4aeb-8ca7-39bf79b4ebf3"))) {
            return "G";
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
