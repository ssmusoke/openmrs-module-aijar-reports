package org.openmrs.module.ugandaemrreports.definition.data.definition;

import org.openmrs.Location;
import org.openmrs.module.reporting.data.person.definition.ObsForPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;


/**
 */
public class ReturnVisitDatePatientDataDefinition extends ObsForPersonDataDefinition implements PersonDataDefinition {

    @ConfigurationProperty
    private Location location;

    public Location getLocation() { return location; }

    public void setLocation(Location location) { this.location = location; }
}
