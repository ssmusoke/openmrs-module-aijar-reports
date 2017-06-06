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
 * Created by Nicholas Ingosi on 5/2/17.
 */
public class MebendazoleDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }
        Obs obs = ((Obs) obj);

        if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("a7a9d632-b266-4085-9a5e-57fc8dd56f0c"))) {
            return "✔";
        }
        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("0134c1d3-a255-46b2-ac77-38c0edcd9e53"))) {
            return "x";
        }
        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("beb5523f-ec5a-46fe-a7c9-f3270a05f4b4"))) {
            return "ND";
        }
        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("dca06bae-30ab-102d-86b0-7a5022ba4115"))) {
            return "C";
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
