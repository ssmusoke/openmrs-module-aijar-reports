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

package org.openmrs.module.ugandaemrreports.reporting.utils;

import org.openmrs.EncounterType;
import org.openmrs.GlobalProperty;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import java.util.*;

/**
 * General utility functions
 */
public class CoreUtils {

    /**
     * Merges multiple collections into a list with natural ordering of elements
     *
     * @param collections the collections
     * @param <T>         the element type
     * @return the merged list
     */
    public static <T extends Comparable> List<T> merge(Collection<T>... collections) {
        Set<T> merged = new TreeSet<T>();

        for (Collection<T> list : collections) {
            for (T element : list) {
                merged.add(element);
            }
        }

        return new ArrayList(merged);
    }

    /**
     * Calculates the earliest date of two given dates, ignoring null values
     *
     * @param d1 the first date
     * @param d2 the second date
     * @return the earliest date value
     * @should return null if both dates are null
     * @should return non-null date if one date is null
     * @should return earliest date of two non-null dates
     */
    public static Date earliest(Date d1, Date d2) {
        return OpenmrsUtil.compareWithNullAsLatest(d1, d2) >= 0 ? d2 : d1;
    }

    /**
     * Calculates the latest date of two given dates, ignoring null values
     *
     * @param d1 the first date
     * @param d2 the second date
     * @return the latest date value
     * @should return null if both dates are null
     * @should return non-null date if one date is null
     * @should return latest date of two non-null dates
     */
    public static Date latest(Date d1, Date d2) {
        return OpenmrsUtil.compareWithNullAsEarliest(d1, d2) >= 0 ? d1 : d2;
    }

    /**
     * Add days to an existing date
     *
     * @param date the date
     * @param days the number of days to add (negative to subtract days)
     * @return the new date
     * @should shift the date by the number of days
     */
    public static Date dateAddDays(Date date, int days) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }


    /**
     * Sets an untyped global property
     *
     * @param property the property name
     * @param value    the property value
     */
    public static void setGlobalProperty(String property, String value) {
        GlobalProperty gp = Context.getAdministrationService().getGlobalPropertyObject(property);
        if (gp == null) {
            gp = new GlobalProperty();
            gp.setProperty(property);
        }
        gp.setPropertyValue(value);
        Context.getAdministrationService().saveGlobalProperty(gp);
    }
    
    /**
     * Get the EncounterType for the given identifier
     *
     * @param identifier The uuid of the encounter type
     * @return the EncounterType
     */
    public static EncounterType getEncounterType(String identifier) {
        return Context.getEncounterService().getEncounterTypeByUuid(identifier);

    }
}
