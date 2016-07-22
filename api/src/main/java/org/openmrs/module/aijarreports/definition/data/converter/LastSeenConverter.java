/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.aijarreports.definition.data.converter;

import org.openmrs.module.aijarreports.common.PatientData;
import org.openmrs.module.aijarreports.common.Period;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Who Stage data converter
 */
public class LastSeenConverter implements DataConverter {

    /**
     * Default constructor
     */
    public LastSeenConverter() {
    }

    //***** INSTANCE METHODS *****

    /**
     * @see DataConverter#convert(Object)
     */
    public Object convert(Object original) {

        PatientData o = (PatientData) original;

        if (o != null) {
            if (o.getPeriod() == Period.MONTHLY) {
                if (o.getEncounterDate() != null && o.getNumberOfSinceLastVisit() < 90) {
                    return "\u2713";
                } else {
                    if (o.getDeathDate() != null && o.getEncounterDate() == null) {
                        return "DEAD";
                    } else if (o.getEncounterDate() == null && o.isTransferredOut()) {
                        return "TO";
                    } else if (o.getEncounterDate() == null && o.getNumberOfSinceLastVisit() >= 90) {
                        return "LOST";
                    } else if (o.getNextVisitDate() != null && o.getNumberOfSinceLastVisit() >= 90) {
                        return "\u2192";
                    }
                }
            } else if (o.getPeriod() == Period.QUARTERLY) {
                if (o.getEncounterDate() != null) {
                    if (o.getNumberOfSinceLastVisit() < 90)
                        return "\u2713";
                } else {
                    if (o.getDeathDate() != null) {
                        if (o.getEncounterDate() == null)
                            return "DEAD";
                    } else if (o.getEncounterDate() == null && o.isTransferredOut()) {
                        return "TO";
                    } else if (o.getEncounterDate() == null && o.getNumberOfSinceLastVisit() >= 90) {
                        return "LOST";
                    } else if (o.getNextVisitDate() != null && o.getNumberOfSinceLastVisit() >= 90) {
                        return "\u2192";
                    }
                }
            }
        }

        return null;
    }

    /**
     * @see DataConverter#getDataType()
     */
    public Class<?> getDataType() {
        return String.class;
    }

    /**
     * @see DataConverter#getInputDataType()
     */
    public Class<?> getInputDataType() {
        return PatientData.class;
    }
}