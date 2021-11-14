/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.ugandaemrreports.definition.cohort.definition;

import org.openmrs.Location;
import org.openmrs.module.reporting.cohort.definition.DateObsCohortDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;



@Caching(strategy= ConfigurationPropertyCachingStrategy.class)
public class AppointmentDateAtLocationCohortDefinition extends DateObsCohortDefinition {
	
	public static final long serialVersionUID = 1L;

    @ConfigurationProperty
    private Location location;

    public Location getLocation() { return location; }

    public void setLocation(Location location) { this.location = location; }
	

}
