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
 */
public class TetanusDataConverter implements DataConverter {

    @Override
    public Object convert(Object obj) {

        if (obj == null) {
            return "";
        }

        Obs obs = ((Obs) obj);

        if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("0192ca59-b647-4f88-b07e-8fda991ba6d6"))) {
            return "1st";
        }

        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("f1d5afce-8dfe-4d2d-b24b-051815d61848"))) {
            return "2nd";
        }

        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("a5497b5a-7da1-42d2-9985-b5ec695b4199"))) {
            return "3rd";
        }

        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("da40fa2a-074f-4d90-a875-5bb8316bc753"))) {
            return "4th";
        }

        else if (obs.getValueCoded() != null && obs.getValueCoded().equals(Dictionary.getConcept("18f11bf4-c986-4cdd-b31c-fb189ea39333"))) {
            return "5th";
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
