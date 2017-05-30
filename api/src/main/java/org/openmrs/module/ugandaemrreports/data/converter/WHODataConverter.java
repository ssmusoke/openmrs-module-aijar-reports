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
 * Created by Nicholas Ingosi on 5/29/17.
 */
public class WHODataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Concept whoResultsConcept = ((Obs) obj).getValueCoded();

        if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda2bc2-30ab-102d-86b0-7a5022ba4115"))) {
            return "1";
        }
        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda3251-30ab-102d-86b0-7a5022ba4115"))) {
            return "2";
        }
        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda3663-30ab-102d-86b0-7a5022ba4115"))) {
            return "3";
        }
        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dcda3a80-30ab-102d-86b0-7a5022ba4115"))) {
            return "4";
        }
        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd25e735-30ab-102d-86b0-7a5022ba4115"))) {
            return "T1";
        }
        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd2666a5-30ab-102d-86b0-7a5022ba4115"))) {
            return "T2";
        }

        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd266d64-30ab-102d-86b0-7a5022ba4115"))) {
            return "T3";
        }
        else if(whoResultsConcept != null && whoResultsConcept.equals(Dictionary.getConcept("dd269c18-30ab-102d-86b0-7a5022ba4115"))) {
            return "T4";
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
