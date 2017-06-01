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
public class BabyStatusDataConveter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept concept = ((Obs) obj).getValueCoded();

        if(concept.equals(Dictionary.getConcept("9d9e6b5a-8b5d-4b8c-8ab7-9fdabb279493"))){
            return "AL";
        }
        else if(concept.equals(Dictionary.getConcept("811ff634-8d81-454f-9b9d-2850345796d6"))){
            return "NND7";
        }
        else if(concept.equals(Dictionary.getConcept("95121db8-6c2a-48e0-b281-cf2dc8229dd1"))){
            return "NND28";
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