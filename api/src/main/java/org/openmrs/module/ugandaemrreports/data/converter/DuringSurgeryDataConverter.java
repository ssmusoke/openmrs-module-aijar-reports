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

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.ugandaemrreports.reporting.metadata.Dictionary;

/**
 * Created by Nicholas Ingosi on 7/17/17.
 */
public class DuringSurgeryDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Obs obs = ((Obs) obj);
        if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("58da0526-00d2-49f8-98e7-4ed0cb6bd672"))){
            return "5";
        }
        else if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("d99b872e-8116-4697-941d-fc14e98d5612"))){
            return "2";
        }
        else if(obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("dcd68a88-30ab-102d-86b0-7a5022ba4115"))){
            return "6";
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