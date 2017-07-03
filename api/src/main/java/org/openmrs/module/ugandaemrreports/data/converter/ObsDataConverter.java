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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Nicholas on 4/27/17.
 */
public class ObsDataConverter implements DataConverter {
    @Override
    public Object convert(Object obj) {
        if (obj == null) {
            return "";
        }

        Obs obs = ((Obs) obj);

        if (obs.getValueCoded() != null) {
            return obs.getValueCoded().getName().getName();
        }

        else if (obs.getValueDate() != null) {
            return formatDate(obs.getValueDate());
        }

        else if (obs.getValueDatetime() != null) {
            return formatDate(obs.getValueDatetime());
        }

        else if (obs.getValueNumeric() != null) {
            return obs.getValueNumeric().toString();
        }

        else if (obs.getValueText() != null) {
            return obs.getValueText();
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

    private String formatDate(Date date) {
        DateFormat dateFormatter = new SimpleDateFormat("MMM d, yyyy");
        return dateFormatter.format(date);
    }
}
