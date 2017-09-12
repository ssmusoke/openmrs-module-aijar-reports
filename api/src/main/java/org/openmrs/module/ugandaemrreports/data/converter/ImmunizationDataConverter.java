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
public class ImmunizationDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept concept = ((Obs) obj).getValueCoded();
        if(concept != null && concept.equals(Dictionary.getConcept("dc8e1be9-30ab-102d-86b0-7a5022ba4115"))){
            return "✔";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("dc883964-30ab-102d-86b0-7a5022ba4115"))){
            return "✔";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("782AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))){
            return "✔";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("680f7f8d-eac6-44b4-8899-101fa2c4f873"))){
            return "✔";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("62d87122-0a08-4e04-b57c-7e1b2f821854"))){
            return "✔";
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