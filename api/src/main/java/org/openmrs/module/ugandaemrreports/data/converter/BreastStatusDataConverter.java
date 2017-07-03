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
 * Created by Nicholas Ingosi on 5/16/17.
 */
public class BreastStatusDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Concept concept = ((Obs) obj).getValueCoded();

        if(concept != null && concept.equals(Dictionary.getConcept("e4c6b70d-aaa6-490b-8edc-01c44d982cb2"))) {
            return "FOM";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("5e416f86-aaf1-4ae4-96f0-30226b9fdd5f"))) {
            return "SS";
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