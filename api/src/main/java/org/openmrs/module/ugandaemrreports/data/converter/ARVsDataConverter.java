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
public class ARVsDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept arvResultsConcept = ((Obs) obj).getValueCoded();

        if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("026e31b7-4a26-44d0-8398-9a41c40ff7d3"))){
            return "ART";
        }
        else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("2aa7d442-6cbb-4609-9dd3-bc2ad6f05016"))){
            return "ARTK";
        }

        else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("2c000b41-f9d7-40c1-8de0-bce91dbae932"))){
            return "ART✔";
        }
        else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("bbc63761-0741-4583-9396-a34d3a18601c"))){
            return "ARTK✔";
        }
        else  if(arvResultsConcept != null && arvResultsConcept.equals(Dictionary.getConcept("dc9b0596-30ab-102d-86b0-7a5022ba4115"))){
            return "NA";
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
