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
public class FaciltyAndOutReachDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept concept = ((Obs) obj).getValueCoded();
        if(concept != null && concept.equals(Dictionary.getConcept("f2aa1852-fcfe-484b-a6ef-1613bd3a1a7f"))){
            return "F";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("03596df2-09bc-4d1f-94fd-484411ac9012"))){
            return "OR";
        }
        else if(concept != null && concept.equals(Dictionary.getConcept("63e5387f-74f6-4a92-a71f-7b5dd3ed8432"))){
            return "CAMP";
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