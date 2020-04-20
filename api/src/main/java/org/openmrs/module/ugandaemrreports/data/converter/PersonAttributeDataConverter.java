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
import org.openmrs.PersonAttribute;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 */
public class PersonAttributeDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {
        if (obj == null) {
            return "";
        }
        String personAttribute = ((PersonAttribute)obj).getValue();
        if(personAttribute!=null)
        {
            if(personAttribute.equals("165317"))
            {
                return "National";
            }
            else if(personAttribute.equals("165318"))
            {
                return "Foreigner";
            }
            else if(personAttribute.equals("160155"))
            {
                return "Refugee";
            }
            else{
                return ((PersonAttribute)obj).getValue();
            }
        }
        return null;
    }

    @Override
    public Class<?> getInputDataType() {
        return PersonAttribute.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }
}
