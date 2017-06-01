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
 * Created by Nicholas Ingosi on 5/15/17.
 */
public class FpPNCDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if(obj == null){
            return "";
        }
        Concept concept = ((Obs) obj).getValueCoded();

        if(concept.equals(Dictionary.getConcept("38aa1dc0-1aaa-4bdd-b26f-28f960dfb16c"))) {
            return "1";
        }
        else if(concept.equals(Dictionary.getConcept("4b0899f2-395e-4e0f-8b58-d304b214615e"))) {
            return "2";
        }
        else if(concept.equals(Dictionary.getConcept("670b7048-d71e-483a-b2ec-f10d2326dd84"))) {
            return "6";
        }
        else if(concept.equals(Dictionary.getConcept("dc882c84-30ab-102d-86b0-7a5022ba4115"))) {
            return "4";
        }
        else if(concept.equals(Dictionary.getConcept("aeee4ccf-cbf8-473c-9d9f-846643afbf11"))) {
            return "5";
        }
        else if(concept.equals(Dictionary.getConcept("dcb30ba3-30ab-102d-86b0-7a5022ba4115"))) {
            return "8";
        }
        else if(concept.equals(Dictionary.getConcept("dcb2f595-30ab-102d-86b0-7a5022ba4115"))) {
            return "7";
        }
        else if(concept.equals(Dictionary.getConcept("80797AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "3";
        }
        else if(concept.equals(Dictionary.getConcept("dcdd8d8d-30ab-102d-86b0-7a5022ba4115"))) {
            return "11";
        }
        else if(concept.equals(Dictionary.getConcept("bb83fd9d-24c5-4d49-89c0-97e13c792aaf"))) {
            return "12";
        }
        else if(concept.equals(Dictionary.getConcept("dcdd91a7-30ab-102d-86b0-7a5022ba4115"))) {
            return "10";
        }
        else if(concept.equals(Dictionary.getConcept("aaf150a5-92d2-416f-8254-95d34ed9c4ab"))) {
            return "14";
        }
        else if(concept.equals(Dictionary.getConcept("5622AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"))) {
            return "13";
        }
        else if(concept.equals(Dictionary.getConcept("dcb30381-30ab-102d-86b0-7a5022ba4115"))) {
            return "9";
        }
        return null;
    }

    @Override
    public Class<?> getInputDataType() {
        return null;
    }

    @Override
    public Class<?> getDataType() {
        return null;
    }
}