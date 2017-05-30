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
 * Created by Nicholas Ingosi on 4/29/17.
 */
public class SyphilisTestDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        //get the obs value
        Concept value = ((Obs)obj).getValueCoded();
        if(value != null && value.equals(Dictionary.getConcept("db3b19b2-e5f0-48c5-9ab4-dd9e4ad519dd"))){
            return "Rx";
        }
        else if(value != null && value.equals(Dictionary.getConcept("fe247560-8db6-4664-a6bc-e3b873b9b10a"))){
            return "+ve";
        }
        else if(value != null && value.equals(Dictionary.getConcept("0d323507-97ff-4146-917c-11119546c051"))){
            return "NR";
        }
        else if(value != null && value.equals(Dictionary.getConcept("451f794b-2f67-4ac5-bfb6-39cdae7bf4fc"))){
            return "NT";
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
