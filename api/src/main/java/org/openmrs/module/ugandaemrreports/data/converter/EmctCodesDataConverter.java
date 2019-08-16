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
public class EmctCodesDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept value = ((Obs)obj).getValueCoded();

        if(value != null && value.equals(Dictionary.getConcept("25c448ff-5fe4-4a3a-8c0a-b5aaea9d5465"))) {
          return "TRR";
        }

        else if(value != null && value.equals(Dictionary.getConcept("6da9b915-8668-4642-8ed4-7d2a346881cb"))) {
            return "C";
        }
        else if(value != null && value.equals(Dictionary.getConcept("05f16fc5-1d82-4ce8-9b44-a3125fbbf2d7"))) {
            return "T";
        }
        else if(value != null && value.equals(Dictionary.getConcept("48723c9c-c931-4fde-81cd-6178c9a9a70b"))) {
            return "=CONCATENATE(\"TR\",UNICHAR(8730))";
        }
        else if(value != null && value.equals(Dictionary.getConcept("4d301db0-c517-4556-9f7c-d837bac90144"))) {
            return "=CONCATENATE(\"TRR\",UNICHAR(8730))";
        }
        else if(value != null && value.equals(Dictionary.getConcept("81bd3e58-9389-41e7-be1a-c6723f899e56"))) {
            return "TRK";
        }
        else if(value != null && value.equals(Dictionary.getConcept("1f177240-85f6-4f10-964a-cfc7722408b3"))) {
            return "TRRK";
        }
        else if(value != null && value.equals(Dictionary.getConcept("a08d9331-b437-485c-8eff-1923f3d43630"))) {
            return "TR+";
        }
        else if(value != null && value.equals(Dictionary.getConcept("8dcaefaa-aa91-4c24-aaeb-122cff549ab3"))) {
            return "TRR+";
        }
        else if(value != null && value.equals(Dictionary.getConcept("86e394fd-8d85-4cb3-86d7-d4b9bfc3e43a"))) {
            return "TR";
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
